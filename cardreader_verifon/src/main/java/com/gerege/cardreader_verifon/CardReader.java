package com.gerege.cardreader_verifon;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.gerege.cardreader_verifon.dialogs.PinDialog;
import com.gerege.cardreader_verifon.helpers.Helper;
import com.gerege.cardreader_verifon.interfaces.CardReaderListener;
import com.gerege.cardreader_verifon.models.EmvApplication;
import com.gerege.cardreader_verifon.models.ICCard;
import com.gerege.cardreader_verifon.models.MagCard;
import com.vfi.smartpos.deviceservice.aidl.IDeviceInfo;
import com.vfi.smartpos.deviceservice.aidl.IDeviceService;
import com.vfi.smartpos.deviceservice.aidl.IInsertCardReader;
import com.vfi.smartpos.deviceservice.aidl.IMagCardReader;
import com.vfi.smartpos.deviceservice.aidl.ISmartCardReader;
import com.vfi.smartpos.deviceservice.aidl.MagCardListener;

import java.util.List;
import java.util.concurrent.CountDownLatch;


public class CardReader {
    private static final String TAG = "qweqweC";

    private boolean isMagSwiped;
    private ReadCardThread thread;
    private ReadCardHandler handler;
    private final EMV emv;
    private String amount;
    private ICCard icCard;
    private MagCard magCard;
    private AppCompatActivity activity;

    //#region Verifon
    private IDeviceService idevice;
    private IMagCardReader magReader;
    private IInsertCardReader insertCardReader;
    private ISmartCardReader iSmartCardReader;
    //#endregion Verifon

    public CardReader() {
        this("B7D0540813DED532C0F19D6FACB916EE", "FB89C1086BBC3E1A4F8AEC1C94C41929");   // TODO: load key
    }

    public CardReader(String key, String masterKey) {
        handler = new ReadCardHandler(Looper.myLooper());
        emv = new EMV(key, masterKey);
        EMVUtils.addSupportedCAPKs();
    }

    public void connect(AppCompatActivity activity) {
        Intent intent = new Intent();
        intent.setAction("com.vfi.smartpos.device_service");
        intent.setPackage("com.vfi.smartpos.deviceservice");
        boolean isSuccessful = activity.bindService(intent, conn, Context.BIND_AUTO_CREATE);
        if (isSuccessful) {
            Log.i("TAG", "deviceService connect fail!");
        } else {
            Log.i("TAG", "deviceService connect success");
        }
    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            idevice = IDeviceService.Stub.asInterface(service);
            try {
                magReader = idevice.getMagCardReader();
                insertCardReader = idevice.getInsertCardReader(0);
                iSmartCardReader = idevice.getSmartCardReader(0);
                Log.d(TAG, "SmartCardReader 0- isNull:" + (iSmartCardReader == null));
                Log.d(TAG, "SmartCardReader 1- isNull:" + (idevice.getSmartCardReader(1) == null));
                Log.d(TAG, "SmartCardReader 2- isNull:" + (idevice.getSmartCardReader(2) == null));
                Log.d(TAG, "SmartCardReaderEx- isNull:" + (idevice.getSmartCardReaderEx() == null));

                IDeviceInfo iDeviceInfo = idevice.getDeviceInfo();
                String versions = "\nROM:" + iDeviceInfo.getROMVersion();
                versions += "\nSecurity:" + iDeviceInfo.getK21Version();
                versions += "\nHW:" + iDeviceInfo.getHardwareVersion();
                versions += "\nAndroid kernel:" + iDeviceInfo.getAndroidKernelVersion();
                versions += "\nFW Version:" + iDeviceInfo.getFirmwareVersion();
                Log.d(TAG, "Versions:" + versions);
                Log.d(TAG, "Connection DONE");
            } catch (RemoteException e) {
                e.printStackTrace();
                Log.d(TAG, "Conn-Catch:" + e);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
        }
    };

    public void readCard(AppCompatActivity activity, long amount, CardReaderListener listener) {
        this.amount = String.valueOf(amount);
        handler.setListener(listener);
        this.activity = activity;

        if (thread != null) {
            thread.interrupt();
            thread = null;
        }


        isMagSwiped = false;
        thread = new ReadCardThread();
        thread.start();
        Log.d(TAG, "Read card");
    }

    public void stop() {
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }

    private void readMagCardData(Bundle track) {
        Log.d(TAG, "mag card is swiped !");
        String cardNo = "";
        String pan = track.getString("PAN", "Get Pan fails");
        String track1 = track.getString("TRACK1", "Get Track1 fails");
        String track2 = track.getString("TRACK2", "Get Track2 fails");
        String track3 = track.getString("TRACK3", "Get Track3 fails");
        String serviceCode = track.getString("SERVICE_CODE", "fails");
        String expiredDate = track.getString("EXPIRED_DATE", "fails");

        Log.d(TAG, "SUCCESS" + "\n" +
                "PAN:" + pan + "\n" +
                "TRACK1:" + track1 + "\n" +
                "TRACK2:" + track2 + "\n" +
                "TRACK3:" + track3 + "\n" +
                "SERVICE_CODE:" + serviceCode + "\n" +
                "EXPIRED_DATE:" + expiredDate + "\n");

        if (!track2.isEmpty()) {
            int index = track2.indexOf("=");
            if (index != -1) {
                cardNo = track2.substring(0, index);
            }
        }

        String pin = getPin();

        if (pin == null) {
            handler.msgCancelled();
            return;
        }

        Log.d(TAG, "Mag-Track1: " + track1);

        handler.msgMagCard(
                new MagCard("", cardNo, Helper.b2h(track1.getBytes()), Helper.b2h(track2.getBytes()), track3, ""),
                emv.encPin(cardNo, pin)
        );
    }

    private void readIcCardData() {
        emv.setIsContactless(false);
        emv.setInsertCardReader(insertCardReader);


        try {
            if (!insertCardReader.powerUp()) {
                Log.e(TAG, "Smart card power up fails!");
            }
            if (!insertCardReader.isCardIn()) {
                Log.e(TAG, "Smart card isCardIn fails!");
                return;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            Log.e(TAG, "EREREREREE: " + e);
            return;
        }


        Log.d(TAG, "IC step - 2");
        try {
            List<EmvApplication> applications = emv.readApplications();
            Log.d(TAG, "11111 size: " + applications.size());
            if (applications.size() == 0) {
                Log.d(TAG, "22222");
                handler.msgError("get AID failed !");
                return;
            }


            Log.d(TAG, "IC step - 3,  len = " + applications.size());
            int i = 0;
            for (EmvApplication e : applications) {
                Log.d(TAG, "application-" + i + ", Label: " + e.getLabel());
                Log.d(TAG, "application-" + i + ", Priority: " + e.getPriority());
                Log.d(TAG, "application-" + i + ", AID: " + Helper.b2h(e.getAID()));
                i++;
            }
            EmvApplication application = applications.get(0);
            for (EmvApplication app : applications) {
                if (app.getPriority() > application.getPriority()) {
                    application = app;
                }
            }

            Log.d(TAG, "selected-application: " + (Helper.b2h(application.getAID())));

            Log.d(TAG, "IC step - 4");
            if (!emv.selectApplication(application)) {
                handler.msgError("application selection failed !");
                return;
            }

            Log.d(TAG, "IC step - 5");
            if (!emv.initializeTransaction()) {
                handler.msgError("init transaction failed !");
                return;
            }

            Log.d(TAG, "IC step - 6");
            if (!emv.readApplicationData()) {
                handler.msgError("read data failed !");
                return;
            }

            Log.d(TAG, "IC step - 7");
            emv.doOfflineDataAuthentication();

            String pin = getPin();
            if (pin == null) {
                handler.msgCancelled();
                return;
            }

            Log.d("qweqwe", "PIN: " + pin);
            String cardNo = emv.getCardNo();
            String track2 = emv.getTrack2();
            String track3 = emv.getTrack3(amount);// amount
            Log.d("qweqwe", "carNo: " + cardNo);
            Log.d("qweqwe", "track2: " + track2); // 343230373333333935373935363637333D3233303432323133393537393539353433
            Log.d("qweqwe", "track3: " + track3);
            Log.d("qweqwe", "PIN: " + emv.encPin(cardNo, pin));

            handler.msgIcCard(
                    new ICCard(cardNo, track2, track3, "", ""),
                    emv.encPin(cardNo, pin)
            );

        } catch (Exception e) {
            Log.d(TAG, "read application - CATCH: " + e);
        }
//        byte apdu_cmd_test[] = {(byte) 0x00, (byte) 0xA4, (byte) 0x00, (byte) 0x00,
//                (byte) 0x02, (byte) 0x3F, (byte) 0x00};
//
//        byte[] req = new byte[]{
//                (byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00,
//                (byte) 0x0E,  // "1PAY.SYS.DDF01".length() == 0x0E (14)
//                (byte) 0x31, (byte) 0x50, (byte) 0x41, (byte) 0x59, (byte) 0x2E, (byte) 0x53, (byte) 0x59, (byte) 0x53, (byte) 0x2E, (byte) 0x44, (byte) 0x44, (byte) 0x46, (byte) 0x30, (byte) 0x31,
//                (byte) 0x00
//        };
//
//        byte[] apdu_ret = null;
//        byte apdu_card_number[] = {(byte) 0x00, (byte) 0xB2, (byte) 0x01, (byte) 0x0C, (byte) 0x00};
//        byte apdu_card_number2[] = {(byte) 0x00, (byte) 0xB2, (byte) 0x01, (byte) 0x14, (byte) 0x00};
//        byte[] apdu_ret_card_number1 = null;
//        byte[] apdu_ret_card_number2 = null;
//
//        Log.d(TAG, "Try read smart card ...");
//
//        try {
//
//            if (!insertCardReader.isCardIn()) {
//                Log.e(TAG, "Smart card isCardIn fails!");
//            }
//            if (!insertCardReader.powerUp()) {
//                Log.e(TAG, "Smart card power up fails!");
//            }
//
//            if (insertCardReader.isCardIn()) {
//                apdu_ret = insertCardReader.exchangeApdu(req);  // apdu_cmd_test
//                Log.d(TAG, "exchangeApdu ret:" + Helper.b2h(apdu_ret));
//
//                apdu_ret_card_number1 = insertCardReader.exchangeApdu(apdu_card_number);
//                Log.d(TAG, "Card number 1:" + Helper.b2h(apdu_ret_card_number1));
//
//                apdu_ret_card_number2 = insertCardReader.exchangeApdu(apdu_card_number2);
//                Log.d(TAG, "Card number 2:" + Helper.b2h(apdu_ret_card_number2));
//
//                Log.d(TAG, "IC Card done:" + Helper.b2h(apdu_ret_card_number1) + ", " + Helper.b2h(apdu_ret_card_number2));
//            }
//
//            insertCardReader.powerDown();
//        } catch (RemoteException e) {
//            e.printStackTrace();
//            Log.d(TAG, "IC Card - CATCH: " + e);
//            try {
//                insertCardReader.powerDown();
//            } catch (RemoteException e2) {
//                e2.printStackTrace();
//            }
//        }
//
//        Log.d(TAG, "Try read smart card ... return");
    }

    private void readContactlessCardData() {
    }

    private String getPin() {
        final String[] pin = {null};

        CountDownLatch latch = new CountDownLatch(1);

        activity.runOnUiThread(() -> {
            PinDialog pinDialog = PinDialog.newInstance();
            pinDialog.setListener(new PinDialog.PinDialogListener() {
                @Override
                public void onInputDone(String input) {
                    pin[0] = input;
                    latch.countDown();
                }

                @Override
                public void onCancel() {
                    latch.countDown();
                }
            });
            pinDialog.show(activity.getSupportFragmentManager(), "");
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return pin[0];
    }

    private MagCardListener magListener = new MagCardListener.Stub() {
        @Override
        public void onSuccess(Bundle track) {
            isMagSwiped = true;
            Log.d(TAG, "Read-onSuccess- THREAD: " + Thread.currentThread().getId());
            readMagCardData(track);
        }

        @Override
        public void onError(int error, String message) {
            isMagSwiped = true;
            Log.d(TAG, "Read-onError: " + error + ", sg: " + message);
        }

        @Override
        public void onTimeout() {
            isMagSwiped = true;
            Log.d(TAG, "Read-onTimeout: ");
        }
    };

    class ReadCardThread extends Thread {

        private void detect() throws Exception {
            Log.d(TAG, "Detect, THREAD: " + Thread.currentThread().getId());

            magReader.searchCard(60, magListener);
            Log.d(TAG, "Reading ... ");

            while (!Thread.interrupted()) {
                Log.d(TAG, "IC Card detected");
                if (isMagSwiped) {
                    insertCardReader.powerDown();
                    break;
                } else if (insertCardReader.isCardIn()) {
                    magReader.stopSearch();
                    readIcCardData();
                    break;
                } else {
                    SystemClock.sleep(500);
                }
            }

            Log.d(TAG, "Endddddddddddddd");
        }

        @Override
        public void run() {
            super.run();
            try {
                handler.msgStarted();
                detect();
            } catch (Exception e) {
                Log.d(TAG, "Thread - CATCH: " + e);
                if (handler != null) {
                    handler.msgError(e);
                }
            }
        }
    }
}
