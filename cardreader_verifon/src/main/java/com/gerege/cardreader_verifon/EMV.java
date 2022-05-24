package com.gerege.cardreader_verifon;

import android.util.Log;

import com.gerege.cardreader_verifon.helpers.Helper;
import com.gerege.cardreader_verifon.models.EmvApplication;
import com.vfi.smartpos.deviceservice.aidl.IInsertCardReader;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * TODO:
 * 1. build TVR
 * 2. check all condition SDA, DDA, CDA
 * 3. Terminal id, capability ...   (9F1E, 9F35, 9F33 ...)
 * 4. CVM (9F34):   Cardholder Verification Method (CVM) Results  -  Indicates the results of the last CVM performed
 * 5. initTransaction and Track3's order ?
 */
public class EMV {

    private static final String TAG = "qweqwe_emv";
    public static final String SUCCESS = "9000";
    private static final Map<String, String> supportedCAPK = new HashMap<>();


    private IInsertCardReader insertCardReader;
    private static boolean isContactless = false;
    private String TPK = "";
    private String TMK = "";
    private final Map<String, TLV> tags;
    private EmvApplication application;
    private List<TLV> PDOL;                // Processing Options Data Object List
    private byte[] AIP = null;          // Application Interchange Profile
    private byte[] AFL = null;          // Application File Locator (identifies the files and records which are necessary for the transaction). (list of record (AEF))

    public EMV(String TPK, String TMK) {
        isContactless = false;
        this.TPK = TPK;
        this.TMK = TMK;
        tags = new HashMap<>();
        PDOL = null;
        AIP = null;
        AFL = null;
        application = null;

        tags.put("5F2A", new TLV("5F2A", 2, Helper.h2b("0496")));
        tags.put("9F1A", new TLV("9F1A", 2, Helper.h2b("0496")));
        tags.put("95", new TLV("95", 5, Helper.h2b("0880048000"))); // DDA - 0880048000, SDA - 0480048000
        tags.put("9F1E", new TLV("9F1E", 8, Helper.h2b("3030303030393035"))); //Interface Device (IFD) Serial Number
        tags.put("9F35", new TLV("9F35", 1, Helper.h2b("22")));     // Terminal type
        tags.put("9F33", new TLV("9F33", 3, Helper.h2b("E0F8C8")));     // Terminal Capabilities
        tags.put("9F34", new TLV("9F34", 3, Helper.h2b("420300")));     // Cardholder Verification Method (CVM) Results  -  Indicates the results of the last CVM performed
        tags.put("9F41", new TLV("9F41", 4, Helper.h2b(getTransactionCounter())));
        tags.put("9B", new TLV("9B", 2, Helper.h2b("E800")));
        tags.put("9C", new TLV("9C", 1, Helper.h2b("00")));
        tags.put("9F03", new TLV("9F03", 6, Helper.h2b("000000000000")));


        //
        tags.put("9F66", new TLV("9F66", 4, Helper.h2b("75800080"))); // Terminal Transaction Qualifiers (TTQ)
        //tags.put("9F02", new TLV("9F02", 6, Helper.h2b("00")));
        //tags.put("9F03", new TLV("9F03", 6, Helper.h2b("00")));
        //tags.put("9F1A", new TLV("9F1A", 2, Helper.h2b("00")));
        //tags.put("95", new TLV("95", 5, Helper.h2b("00")));
        //tags.put("5F2A", new TLV("5F2A", 2, Helper.h2b("00")));
        //tags.put("9A", new TLV("9A", 3, Helper.h2b("00")));       // Transaction Date
        //tags.put("9C", new TLV("9C", 1, Helper.h2b("00")));
        //tags.put("9F37", new TLV("9F37", 4, Helper.h2b("00")));
    }

    public void setInsertCardReader(IInsertCardReader insertCardReader) {
        this.insertCardReader = insertCardReader;
    }

    public void setIsContactless(boolean contactless) {
        isContactless = contactless;
    }

    public String encPin(String pan, String pin) {
        try {
            byte[] pkBytes = TripleDES._decrypt3DES(TLVUtils.hex2Bytes(TPK), TLVUtils.hex2Bytes(TMK));

            String len = (pin.length() > 9 ? "" : "0") + pin.length();
            String s1 = (len + pin + "FFFFFFFFFFFFFFFF").substring(0, 16);
            String s2 = "0000" + pan.substring(3, 15);

            byte[] bs1 = TLVUtils.hex2Bytes(s1);
            byte[] bs2 = TLVUtils.hex2Bytes(s2);

            byte[] pinBlock = new byte[bs2.length];
            for (int i = 0; i < bs2.length; i++) {
                pinBlock[i] = (byte) (bs1[i] ^ bs2[i]);
            }

            return TLVUtils.b2h(
                    TripleDES._encrypt(
                            pinBlock,
                            pkBytes,
                            false
                    )
            );
        } catch (Exception e) {
            return "";
        }
    }

    public String getCardNo() {
        return Helper.b2h(getData("5A"));
    }

    public String getTrack2() {
        byte[] b = getData("57");
        String s = b != null ? Helper.b2h(b) : "";
        int i = s.indexOf('D');

        String s2;
        if ((i + 18) <= s.length()) {
            s2 = s.substring(0, i) + "=" + s.substring(i + 1, i + 18);
        } else {
            s2 = s.replace("D", "=");
        }

        return Helper.b2h(s2.getBytes());
    }

    public String getTrack3(String amount) {
//        String[] tagList = {"0x9F27", "0x9F1A", "0x9A", "0x9F26", "0x82", "0x9F36", "0x9F37",
//                "0x95", "0x9C", "0x9F10", "0x5F2A", "0x9F02", "0x5F34", "0x9F35", "0x9F03",
//                "0x9F1E", "0x9F33", "0x9F34", "0x9F41", "0x9F08", "0x9F06", "0x9B", "0x9F6E",
//                "0x9F6E", "0x9F5B", "05F20x9F4C", "0x84", "0x9F7C", "0x9F63", "0x5F20", "0x9F0B", "0x9F4B"};
        String[] tagList = {
                "5F20", "5F2A", "5F34",
                "82", "84",
                "95", "9A", "9B", "9C",
                "9F02", "9F03", "9F06", "9F08", "9F0B",
                "9F10", "9F1A", "9F1E",
                "9F26", "9F27", "9F33", "9F34", "9F35", "9F36", "9F37", "9F41",
                "9F4B", "9F4C",
                "9F63", "9F6E",
                "9F7C"};
        // 9F5B

        String s = new SimpleDateFormat("yyMMdd").format(Calendar.getInstance().getTime());
        String a = "000000000000" + amount;
        String amount1 = a.substring(a.length() - 12);
        Random random = new Random();
        byte[] t9F37 = new byte[]{
                (byte) random.nextInt(256),
                (byte) random.nextInt(256),
                (byte) random.nextInt(256),
                (byte) random.nextInt(256),
        };
        tags.put("9A", new TLV("9A", 3, Helper.h2b(s)));
        tags.put("9F02", new TLV("9F02", 6, Helper.h2b(amount1)));
        tags.put("9F06", new TLV("9F06", application.getAID().length, application.getAID()));
        tags.put("9F37", new TLV("9F37", t9F37.length, t9F37));

        generateAC();

        Map<String, TLV> map = new LinkedHashMap<>();
        for (String key : tagList) {
            //Log.d("qweqwe", "==> " + key + ": " + tags.get(key));
            if (tags.containsKey(key)) {
                map.put(key, tags.get(key));
            } else {
                map.put(key, new TLV(key, 0, new byte[]{}));
            }
        }

        Log.d(TAG, "Before ber tlv: " + map);
        for (String key : tagList) {
            Log.d("qweqwe", "==> " + key + ": " + Helper.b2h(map.get(key).getData()));
        }

        return Helper.formatBerTlv(map);
    }

    private void generateAC() {
        List<TLV> cdol1 = getCdol1();
        Log.d(TAG, "Generate AC: len = " + cdol1.size());
        Log.d(TAG, "CDOL-1: " + Helper.b2h(getData("8C", new byte[]{})));
        Log.d(TAG, "CDOL-2: " + Helper.b2h(getData("8D", new byte[]{})));
        // state 9F02069F03069F1A0295055F2A029A039C019F37049F35019F45029F4C089F34039F21039F7C14
        // ok 9F02-06
        // ok 9F03-06
        // ok 9F1A-02
        // ok 95-05
        // ok 5F2A-02
        // ok 9A-03
        // ok 9C-01
        // ok 9F37-04
        // 9F35-01          T   Terminal Type
        // 9F45-02          C   	Data Authentication Code  ??  (An issuer assigned value that is retained by the terminal during the verification process of the Signed Static Application Data)
        // 9F4C-08          C        ICC Dynamic Number  (	Time-variant number generated by the ICC, to be captured by the terminal)
//                // $$ (9F46) GET CHALLENGE : get unpredictable number from the ICC
//                byte[] getChallengeCmd = new byte[]{(byte) 0x00, (byte) 0x84, (byte) 0x00, (byte) 0x00, (byte) 0x00};
//                EMV.command(getChallengeCmd, "Get Challenge - ");   // FF413BE5ECA8BEB39000
        // 9F34-03          T   CVM:   Cardholder Verification Method (CVM) Results
        // 9F21-03          T       Transaction Time   (HHMMSS)
        // 9F7C-14          C       ??


        // 8C - CDOL 1,
        // 8D - CDOL 2
        // 9F02069F03069F1A0295055F2A029A039C019F3704  (8C ? 8D ..)   (CDOL1 - Card Risk Management Data Object List 1)
        // 9F02-06          Amount, Authorised (Numeric)
        // 9F03-06          Amount, Other (Numeric)
        // 9F1A-02    496   Terminal Country Code (Indicates the country of the terminal, represented according to ISO 3166)
        // 95-05            Terminal Verification Results (TVR)  (Annex C.5 of [EMV Book 3].)
        // 5F2A-02  MNT-496 Transaction Currency Code (Indicates the currency code of the transaction according to ISO 4217)
        // 9A-03            Transaction Date  (YYMMDD)
        // 9C-01            Transaction Type
        // 9F37-04          Unpredictable Number (UN)    (Value to provide variability and uniqueness to the generation of a cryptogram)

//        byte[] t9F02 = tags.get("9F02").getData();      // 100 -> 1.00
//        byte[] t9F03 = tags.get("9F03").getData();
//        byte[] t9F1A = tags.get("F1A").getData();
//        byte[] t95 = tags.get("95").getData();
//        byte[] t5F2A = tags.get("5F2A").getData();
//        byte[] t9A = tags.get("9A").getData();
//        byte[] t9C = tags.get("9C").getData();      // 21
//        byte[] t9F37 = tags.get("9F37").getData();


        // 80AE80001D000000099900000000000000049608800480000496220330003669850D00
//        int dataLen = t9F02.length + t9F03.length + t9F1A.length + t95.length + t5F2A.length + t9A.length + t9C.length + t9F37.length;
//        ByteBuffer bf = ByteBuffer.allocate(6 + dataLen);
//        byte p1 = (byte) 0x40; // offline transaction
//        p1 = (byte) 0x80; // online transaction
//        bf.put(new byte[]{(byte) 0x80, (byte) 0xAE, p1, (byte) 0x00});
//        bf.put((byte) dataLen).put(t9F02).put(t9F03).put(t9F1A).put(t95).put(t5F2A).put(t9A).put(t9C).put(t9F37);
//        bf.put((byte) 0x00);

        byte[] cdolData = prepareCdolData();
        Log.d(TAG, "cdol-data: " + cdolData.length + ",  " + Helper.b2h(cdolData));
        ByteBuffer bf = ByteBuffer.allocate(6 + cdolData.length);
        byte p1 = (byte) 0x40; // offline transaction
        p1 = (byte) 0x80; // online transaction
        bf.put(new byte[]{(byte) 0x80, (byte) 0xAE, p1, (byte) 0x00});
        bf.put((byte) cdolData.length).put(cdolData);
        bf.put((byte) 0x00);

        ApduRes apduResp = command(bf.array(), "Generate AC: ");

        // 80128004F753B4E3A47FE168D206010A03A0A0009000
        // 80-12 8004F753B4E3A47FE168D206010A03A0A000

        // if 80
        //      Cryptogram Information Data (CID)
        //      Application Transaction Counter (ATC)
        //      Application Cryptogram (AC)
        //      Issuer Application Data (IAD) (optional data object)     (tag: 9F10)
        // 80-12
        //      80
        //      04C2
        //      CF192C115C9D34C9
        //      06010A03A0A802        book 3 - page.208        (..... Go Online on Next Transaction Was Set ???)
        // else if 77
        //Tag: '9F27' - Cryptogram Information Data (CID)
        //Tag: '9F36' - Application Transaction Counter (ATC)
        //Tag: '9F26' - Application Cryptogram (AC)
        //TAG: '9F10' - Issuer Application Data (IAD)


        Map<String, TLV> map = new HashMap<>();
        if (apduResp.isSuccessful()) {

            TLVUtils.parseData(map, apduResp.getData());        // todo: remove (for log)
            TLVUtils.parseData(tags, apduResp.getData());
            byte format = apduResp.getData()[0];

            TLV tlv = format == (byte) 0x80 ? tags.get("80") : tags.get("77");
            if (tlv != null) {
                byte[] data = tlv.getData();
                int i = 0;
                tags.put("9F27", new TLV("9F27", 1, new byte[]{data[0]}));
                tags.put("9F36", new TLV("9F36", 2, new byte[]{data[1], data[2]}));
                byte[] ac = new byte[8];
                System.arraycopy(data, 3, ac, 0, 8);
                tags.put("9F26", new TLV("9F26", 2, ac));

                if (format == (byte) 0x80 && data.length > 11) {
                    int len = data.length - 11;
                    byte[] iad = new byte[len];
                    System.arraycopy(data, 11, iad, 0, len);
                    tags.put("9F10", new TLV("9F10", len, iad));
                }
            }

            Log.d(TAG, "map: " + map);
        }
    }


    // region Read card

    /**
     *
     */
    public List<EmvApplication> readApplications() {
        // 1. select PSE
        byte[] req = new byte[]{
                (byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00,
                (byte) 0x0E,  // "1PAY.SYS.DDF01".length() == 0x0E (14)
                (byte) 0x31, (byte) 0x50, (byte) 0x41, (byte) 0x59, (byte) 0x2E, (byte) 0x53, (byte) 0x59, (byte) 0x53, (byte) 0x2E, (byte) 0x44, (byte) 0x44, (byte) 0x46, (byte) 0x30, (byte) 0x31,
                (byte) 0x00
        };
        ApduRes apduResp = command(req, "Select File|PSE - ");

        byte sfi = 0x01;
        if (apduResp.isSuccessful()) {
            TLVUtils.parseData(tags, apduResp.getData());

            TLV tlv = tags.get("88");
            if (tlv != null && tlv.getLen() > 0) {
                sfi = tlv.getData()[0];
            }
        } else {
            return new ArrayList<>();
        }

        Log.d(TAG, "after readApplications: " + tags);

        // 2. READ RECORDS in SFI   (Get AID)
        return getApplications(sfi);
    }

    private List<EmvApplication> getApplications(byte sfi) {
        Log.d(TAG, "getApplications: " + sfi);
        List<EmvApplication> applications = new ArrayList<>();

        for (int recNo = 1; recNo <= 16; recNo++) {
            byte[] readRecordReq = new byte[]{(byte) 0x00, (byte) 0xB2, (byte) recNo, (byte) ((sfi << 3) | 0x04), (byte) 0x00};
            ApduRes apduRes = command(readRecordReq, "(SFI" + sfi + ", Rec" + recNo + ") - ");

            if (apduRes.isSuccessful()) {
                Map<String, TLV> data = new HashMap<>();
                TLVUtils.parseData(data, apduRes.getData());

                TLV aid = data.get("4F");
                TLV label = data.get("50");
                TLV priority = data.get("87");
                TLV preferredName = data.get("9F12");

                if (aid != null) {
                    applications.add(new EmvApplication(
                            aid.getData(),
                            (label != null) ? Arrays.toString(label.getData()) : "",
                            priority != null ? (int) priority.getData()[0] : 0,
                            preferredName != null ? Arrays.toString(preferredName.getData()) : ""
                    ));
                }
            } else {
                break;
            }
        }

        Log.d(TAG, "after getApplications: " + tags);
        return applications;
    }

    /**
     * Select application by AID
     */
    public boolean selectApplication(EmvApplication application) {
        byte[] reqHeader = new byte[]{(byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00, (byte) application.getAID().length};
        ByteBuffer buffer = ByteBuffer.allocate(reqHeader.length + application.getAID().length + 1);
        buffer.put(reqHeader)
                .put(application.getAID())
                .put((byte) 0x00);
        ApduRes apduRes = command(buffer.array(), "Select(AID)--");
        // 6F36 8407A0000000031010A52B500A564953412044454249548701019F38039F1A025F2D026D6E9F1101019F120A56495341204445424954 9000
        // 84 07 A0000000031010
        // A5 2B 500A564953412044454249548701019F38039F1A025F2D026D6E9F1101019F120A56495341204445424954
        //      50 0A 56495341204445424954
        //      87 01 01
        //      9F38 03 9F1A02
        //      5F2D 02 6D6E
        //      9F11 01 01
        //      9F12 0A 56495341204445424954
        /**
         6F 3A         	                        (6F -  File Control Information (FCI) Template)
         84 07 A0000000031010                    (Dedicated File (DF) Name)   (AID) ?
         A5 2F                                   (File Control Information (FCI) Proprietary Template)
         50 0A 56495341204445424954          (Application Label)
         87 01 01                            (Application Priority Indicator)
         5F2D 04 6D6E656E                    (Language Preference)
         9F11 01 01                          (Issuer Code Table Index)
         9F12 0A 56495341204445424954        (Application Preferred Name)
         BF0C 05 5F55024D4E                  (File Control Information (FCI) Issuer Discretionary Data)
         9000
         * */
        if (apduRes.isSuccessful()) {
            this.application = application;
            TLVUtils.parseData(tags, apduRes.getData());
        }
        PDOL = getPdol();

        return apduRes.isSuccessful();
    }

    /**
     * GET PROCESSING OPTIONS - initialize transaction:
     * Send GET PROCESSING OPTIONS command
     */
    public boolean initializeTransaction() {
        /* TODO: check if PDOL presents on card, if not, only 8300 should be added to DATA filed of APDU
             // 9F38 - Processing Options Data Object List (PDOL)
             // exp:   pdol = new ByteString("830B0000000000000000000000", HEX);   // VISA
             // 83 - command code, 0 length of data (PDOL data ) /terminal should send to card/
              // exp: 83 0A 00 00 00 00 00 00 00 00 00 00
         */

        // prepare PDOL data
        for (TLV t : PDOL) {
            Log.d(TAG, "pdol-tag: " + t.getTag());
        }
        byte[] pdolData = preparePdolData();
        Log.d(TAG, "PDOL-DATA: " + Helper.b2h(pdolData));

        // build command
        byte[] reqHeader = new byte[]{(byte) 0x80, (byte) 0xA8, (byte) 0x00, (byte) 0x00, (byte) pdolData.length};
        ByteBuffer buffer = ByteBuffer.allocate(reqHeader.length + pdolData.length + 1);
        buffer.put(reqHeader)
                .put(pdolData)
                .put((byte) 0x00);

        // send command (Get  AIP, AFL)
        ApduRes apduRes = command(buffer.array(), "Get(AIP, AFL)-- Init --");

        if (apduRes.isSuccessful()) {
            byte[] res = apduRes.getData();
            byte format = res[0];
            TLVUtils.parseData(tags, res);

            if (format == (byte) 0x80) {
                byte[] data = getData("80", new byte[]{});
                AIP = new byte[]{data[0], data[1]};
                AFL = new byte[data.length - 2];
                System.arraycopy(data, 2, AFL, 0, data.length - 2);
                tags.put("82", new TLV("82", AIP.length, AIP));
                tags.put("94", new TLV("94", AFL.length, AFL));
            } else if (format == (byte) 0x77) {
                AIP = getData("82", new byte[]{});
                AFL = getData("94", new byte[]{});
            }

            return true;
        }

        return false;
    }

    /**
     * AFL -н дагуу файлуудаас дата унших
     */
    public boolean readApplicationData() {
        Log.d(TAG, "Read app data " + (AFL.length % 4));
        if (AFL.length % 4 != 0) return false;

        int i = 0;
        while (i < AFL.length - 1) {
            byte sfi = AFL[i];
            int startRecord = AFL[i + 1];
            int endRecord = AFL[i + 2];
            int countSDA = AFL[i + 3];      // TODO: ?? Number of records included in data authentication

            for (int j = startRecord; j <= endRecord; j++) {
                byte[] req = new byte[]{(byte) 0x00, (byte) 0xB2, (byte) j, (byte) ((sfi & 248) | 0x04), (byte) 0x00};
                ApduRes apduRes = command(req, "Read App Data-");
                if (apduRes.isSuccessful()) {
                    TLVUtils.parseData(tags, apduRes.getData());
                }
            }

            i += 4;
        }

        Log.d(TAG, "TAGS: " + tags);
        return true;
    }

    /**
     * Offline data authentication is a cryptographic check to validate the card authenticity.
     */
    public void doOfflineDataAuthentication() {
        // TODO: EMV 4.3 Book 3 Table 42: Terminal Verification Results Terminal Verification Result (TVR):

        // TODO: check does terminal supports (CDA, DDA, SDA) ?
        Log.d("qweqwe", "AIP: " + Helper.b2h(AIP) + ", len = " + AIP.length);

        if (EMVUtils.isCDASupported(AIP)) {
            Log.d("qweqwe", "CDA Supported.");
            //CDA();
            DDA();
        } else if (EMVUtils.isDDASupported(AIP)) {
            Log.d("qweqwe", "DDA Supported.");
            DDA();
        } else if (EMVUtils.isSDASupported(AIP)) {
            Log.d("qweqwe", "SDA Supported.");
            SDA();
        } else {
            Log.d("qweqwe", "nothing supported.");
        }
    }

    private byte[] preparePdolData() {
        byte[] pdolData;
        if (PDOL != null && PDOL.size() > 0) {
            int len = 0;
            for (TLV tlv : PDOL) len += tlv.getLen();
            ByteBuffer buffer = ByteBuffer.allocate(len + 2);
            buffer.put((byte) 0x83).put((byte) len);

            for (TLV tlv : PDOL) {
                byte[] bytes = new byte[tlv.getLen()];
                TLV t = tags.get(tlv.getTag());
                if (t != null && t.getData() != null && t.getLen() == tlv.getLen()) {
                    System.arraycopy(t.getData(), 0, bytes, 0, tlv.getLen());
                } else {
                    Arrays.fill(bytes, (byte) 0x00);
                }
                buffer.put(bytes);
            }
            pdolData = buffer.array();
        } else {
            return new byte[]{(byte) 0x83, (byte) 0x00};
        }

        return pdolData;
    }

    private byte[] prepareCdolData() {
        byte[] cdolData;
        List<TLV> cdol = getCdol1();
        if (cdol.size() > 0) {
            int len = 0;
            for (TLV tlv : cdol) len += tlv.getLen();
            ByteBuffer buffer = ByteBuffer.allocate(len);

            for (TLV tlv : cdol) {
                byte[] bytes = new byte[tlv.getLen()];
                TLV t = tags.get(tlv.getTag());
                if (t != null && t.getData() != null && t.getLen() == tlv.getLen()) {
                    System.arraycopy(t.getData(), 0, bytes, 0, tlv.getLen());
                } else {
                    Arrays.fill(bytes, (byte) 0x00);
                }
                buffer.put(bytes);
            }
            cdolData = buffer.array();
        } else {
            return new byte[]{};
        }

        return cdolData;
    }

    /**
     * SDA - Static Data Authentication
     */
    private void SDA() {
        Log.d(TAG, "SDA init");

        try {
            byte[] exponent = getCAPKExponent();
            byte[] rid = new byte[5];
            System.arraycopy(application.getAID(), 0, rid, 0, 5);
            Log.d(TAG, "rid: " + Helper.b2h(rid));
            byte[] modules = Helper.h2b(getCAPK(
                    Helper.b2h(exponent),
                    Helper.b2h(getCAPKIndex()),
                    Helper.b2h(rid)
            ));

            byte[] decryptedIssuerPKCert = TripleDES.performRSA(
                    getIssuerPKCertificate(),
                    exponent,
                    modules
            );
            int len = decryptedIssuerPKCert.length;
            Log.d(TAG, "decryptedIssuerPKCert: " + Helper.b2h(decryptedIssuerPKCert));

            // 6A02420733FF08280413580101B001CE9CEB1B852F03D625F75DDA946D8EB02867F4655C0165D1B84223FEF5EA29270B2198DE6B887C006E8CD023ED0DFDFACB6B44D8770B775C1AB0789F377510BEA41E773EA52503628B1269BCB50F3B9E629ADC8E9C13F2DAEB57F0DAD6D855BEC21BAC3374B5A36D5998700A618B89713D7F57F6834DA0D6CA4C74B66C65FCE588101D81A273D190811AB95EE7B1341AEA44EAE98CF1C2F63D08683CD451462CDC018EBA6127E2D1471D04C399B91C49BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB9C3923B017C19F6C055DAF1DD62EF86962CEB3A5BC

            // Step 1
            if (modules.length == decryptedIssuerPKCert.length) {
            }

            // Step 2
            if (decryptedIssuerPKCert[len - 1] == (byte) 0xBC) {
            }

            // Step 3
            if (decryptedIssuerPKCert[0] == (byte) 0x6A) {
            }

            // Step 4
            if (decryptedIssuerPKCert[1] == (byte) 0x02) {
            }

            // Step 5
            int start = 15;
            int end = start + (len - 36);
            byte[] pkRemainder = getIssuerPKRemainder();
            byte[] issuerPK = new byte[(end - start) + pkRemainder.length];

            System.arraycopy(decryptedIssuerPKCert, start, issuerPK, 0, end - start);
            if (pkRemainder.length > 0) {
                System.arraycopy(pkRemainder, 0, issuerPK, end - start, pkRemainder.length);
            }

            Log.d(TAG, "issuerPK Full = " + Helper.b2h(issuerPK));

            // Step 6, 7, 8, 9, 10, 11, 12
            if (getSSAD() != null) {
                byte[] decryptedSSAD = TripleDES.performRSA(
                        getSSAD(),
                        exponent,
                        issuerPK
                );
                int l = decryptedSSAD.length;

                // Step 1
                if (issuerPK.length == decryptedSSAD.length) {
                }
                // Step 2
                if (decryptedSSAD[l - 1] == (byte) 0xBC) {
                }
                // Step 3
                if (decryptedSSAD[0] == (byte) 0x6A) {
                }
                // Step 4
                if (decryptedSSAD[1] == (byte) 0x03) {
                }
                // Step 5, 6, 7
            }


        } catch (Exception e) {
            Log.d(TAG, "SDA failed: " + e);
        }
    }

    /**
     * DDA - Dynamic Data Authentication
     */
    private void DDA() {

        Log.d(TAG, "DDA init");

        try {
            byte[] exponent = getCAPKExponent();
            byte[] rid = new byte[5];
            System.arraycopy(application.getAID(), 0, rid, 0, 5);
            Log.d(TAG, "rid: " + Helper.b2h(rid));
            byte[] modules = Helper.h2b(getCAPK(
                    Helper.b2h(exponent),
                    Helper.b2h(getCAPKIndex()),
                    Helper.b2h(rid)
            ));

            byte[] decryptedIssuerPKCert = TripleDES.performRSA(
                    getIssuerPKCertificate(),
                    exponent,
                    modules
            );
            int len = decryptedIssuerPKCert.length;
            Log.d(TAG, "decryptedIssuerPKCert: " + Helper.b2h(decryptedIssuerPKCert));

            // 6A02420733FF08280413580101B001CE9CEB1B852F03D625F75DDA946D8EB02867F4655C0165D1B84223FEF5EA29270B2198DE6B887C006E8CD023ED0DFDFACB6B44D8770B775C1AB0789F377510BEA41E773EA52503628B1269BCB50F3B9E629ADC8E9C13F2DAEB57F0DAD6D855BEC21BAC3374B5A36D5998700A618B89713D7F57F6834DA0D6CA4C74B66C65FCE588101D81A273D190811AB95EE7B1341AEA44EAE98CF1C2F63D08683CD451462CDC018EBA6127E2D1471D04C399B91C49BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB9C3923B017C19F6C055DAF1DD62EF86962CEB3A5BC

            // Step 1
            if (modules.length == decryptedIssuerPKCert.length) {
            }

            // Step 2
            if (decryptedIssuerPKCert[len - 1] == (byte) 0xBC) {
            }

            // Step 3
            if (decryptedIssuerPKCert[0] == (byte) 0x6A) {
            }

            // Step 4
            if (decryptedIssuerPKCert[1] == (byte) 0x02) {
            }

            // Step 5
            byte[] iccCert = getICCCert();
            int encDataLen = iccCert.length;

            int availLen = len - 36;
            int start = 15;
            int end = start + availLen;
            byte[] pkRemainder = getIssuerPKRemainder();
            byte[] issuerPK = new byte[encDataLen]; // new byte[(end - start) + pkRemainder.length];

            if (encDataLen <= availLen) {
                System.arraycopy(decryptedIssuerPKCert, start, issuerPK, 0, encDataLen);
            } else {
                System.arraycopy(decryptedIssuerPKCert, start, issuerPK, 0, availLen);
                System.arraycopy(pkRemainder, 0, issuerPK, availLen, pkRemainder.length);
            }

            Log.d(TAG, "issuerPK Full len = " + issuerPK.length + ", data = " + Helper.b2h(issuerPK)); // len = 424, 212 byte
            // decrypted issuer PK  6A02420733FF08280413580101B001CE9CEB1B852F03D625F75DDA946D8EB02867F4655C0165D1B84223FEF5EA29270B2198DE6B887C006E8CD023ED0DFDFACB6B44D8770B775C1AB0789F377510BEA41E773EA52503628B1269BCB50F3B9E629ADC8E9C13F2DAEB57F0DAD6D855BEC21BAC3374B5A36D5998700A618B89713D7F57F6834DA0D6CA4C74B66C65FCE588101D81A273D190811AB95EE7B1341AEA44EAE98CF1C2F63D08683CD451462CDC018EBA6127E2D1471D04C399B91C49BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB9C3923B017C19F6C055DAF1DD62EF86962CEB3A5BC
            // issuer PK            CE9CEB1B852F03D625F75DDA946D8EB02867F4655C0165D1B84223FEF5EA29270B2198DE6B887C006E8CD023ED0DFDFACB6B44D8770B775C1AB0789F377510BEA41E773EA52503628B1269BCB50F3B9E629ADC8E9C13F2DAEB57F0DAD6D855BEC21BAC3374B5A36D5998700A618B89713D7F57F6834DA0D6CA4C74B66C65FCE588101D81A273D190811AB95EE7B1341AEA44EAE98CF1C2F63D08683CD451462CDC018EBA6127E2D1471D04C399B91C49

            // Step 6, 7, 8, 9, 10, 11, 12
            Log.d(TAG, "iccCert = len = " + iccCert.length + ", data = " + Helper.b2h(iccCert));
            // iccCert = 868407E7373B347E031203AA08278E801615CA588BE97D025CCFF09F7A0AE52E8806F500DA8D24E05D2E7732C829C913929CD7299E8723CC0E7A2C8FD7DFE207DD2831C07A2788AEC0C4D741949A075FBB737384B46AB173074AD3BF1080510B75D223256D0EF9DDC8ED307F52C7B5A1826221959C51D52E9AED0949815212ADA5E8222FF9C1E2910AAEDD5949B512DF24995C42EA7C5DD0EB3CE007706A0A6B04B785BC59406492D686BF7ED5A49C19

            byte[] iccPK = TripleDES.performRSA(
                    iccCert,
                    exponent,
                    issuerPK
            );
            // iccPK = 6A044207333957956673FFFF082820100101018001DBB793A8B916D8981D7A72FB20BA62073DA00A6BD0EAFBAF201118D6F55AFB654DD58977897E2A6A248155EC220B04E32BDCDA28EF87FF653BDEB82A64E47980CF052576868CE117BE94DD0498A56ED9747D8CA23C8FA19168E8624439F05BBA5C06CA1C4A847B9940AF9E836DEE233E403815B162C5BFF562E3020A59E8D23FBBBBBBBBBBBB0A6CA9F7E033D30BB2434B8CB8021A00EE320D7FBC
            Log.d(TAG, "icc PK, len = " + iccPK.length + ", data = " + Helper.b2h(iccPK));

        } catch (Exception e) {
            Log.d(TAG, "DDA failed: " + e);
        }

    }

    /**
     * CDA - Combined Dynamic Data Authentication
     */
    private void CDA() {

    }
    // endregion

    // region SDA (Static data authentication)

    /**
     * Issuer Public Key signed by CA private key (VISA ...)
     */
    private byte[] getIssuerPKCertificate() {
        return getData("90", new byte[]{});
    }

    /**
     * index for CA public key
     */
    private byte[] getCAPKIndex() {
        return getData("8F", new byte[]{});
    }

    /**
     * index for CA public key
     */
    private byte[] getCAPKExponent() {
        return getData("9F32", new byte[]{});
    }

    private byte[] getIssuerPKRemainder() {
        return getData("92", new byte[]{});
    }

    /**
     * SSAD - signed static app data
     */
    private byte[] getSSAD() {
        return getData("93");
    }
    // endregion

    // region DDA (Dynamic data authentication)

    /**
     * ICC Public Key certified by the issuer
     */
    private byte[] getICCCert() {
        return getData("9F46");
    }
    // endregion

    // region private methods
    private List<TLV> getPdol() {
        return TLVUtils.parsePdolData(getData("9F38", new byte[]{}));
    }

    private List<TLV> getCdol1() {
        return TLVUtils.parsePdolData(getData("8C", new byte[]{}));
    }

    private List<TLV> getCdol2() {
        return TLVUtils.parsePdolData(getData("8D", new byte[]{}));
    }

    public static boolean isSuccess(String status) {
        return status != null && status.equals(SUCCESS);
    }

    private byte[] getData(String tag) {
        return getData(tag, null);
    }

    private byte[] getData(String tag, byte[] defaultValue) {
        return tags.containsKey(tag) ? tags.get(tag).getData() : defaultValue;
    }

    /**
     * Terminal transaction counter
     */
    private String getTransactionCounter() {
        PosStorage.putIntegerInSP(
                PosStorage.TRANSACTION_COUNTER,
                PosStorage.getIntegerFromSP(PosStorage.TRANSACTION_COUNTER) + 1
        );
        String s = "00000000" + PosStorage.getIntegerFromSP(PosStorage.TRANSACTION_COUNTER);

        return s.substring(s.length() - 8);
    }
    // endregion

    // region <send command>
    public ApduRes command(byte[] cmd) {
        return command(cmd, false, null);
    }

    public ApduRes command(byte[] cmd, String logAlias) {
        return command(cmd, true, logAlias);
    }

    public ApduRes command(byte[] cmd, boolean log, String alias) {
        try {
            if (BuildConfig.DEBUG && log) {
                Log.d(TAG, alias + "Command: " + Helper.b2h(cmd));
                byte[] res = insertCardReader.exchangeApdu(cmd);
                Log.d(TAG, alias + "Res: " + Helper.b2h(res));

                return new ApduRes(res);
            }
            return new ApduRes(insertCardReader.exchangeApdu(cmd));
        } catch (Exception e) {
            Log.d(TAG, alias + "-CATCH: " + e);
            return new ApduRes(null);
        }
    }
    // endregion

    // region CAPK
    public static void addCAPK(String exponent, String ridIndex, String rid, String modules) {
        try {
            supportedCAPK.put(exponent + ridIndex + rid, modules);
        } catch (Exception ignored) {
        }
    }

    public static String getCAPK(String exponent, String ridIndex, String rid) {
        try {
            Log.d(TAG, "exponent: " + exponent);
            Log.d(TAG, "ridIndext: " + ridIndex);
            Log.d(TAG, "rid: " + rid);
            Log.d(TAG, "CAPK: " + supportedCAPK);
            return supportedCAPK.get(exponent + ridIndex + rid);
        } catch (Exception ignored) {
            Log.d(TAG, "get capk failed: " + ignored);
            return null;
        }
    }
    // endregion

    public static class ApduRes {
        private final byte[] data;
        private final boolean isSuccessful;

        public ApduRes(byte[] data) {
            this.data = data;
            isSuccessful = (data != null && data.length >= 2 &&
                    data[data.length - 2] == (byte) 0x90 &&
                    data[data.length - 1] == (byte) 0x00);
        }

        public byte[] getData() {
            return data != null ? data : new byte[]{};
        }

        public boolean isSuccessful() {
            return isSuccessful;
        }
    }
}
