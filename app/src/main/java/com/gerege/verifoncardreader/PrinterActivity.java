package com.gerege.verifoncardreader;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.gerege.verifoncardreader.helpers.PrinterFonts;
import com.gerege.verifoncardreader.verifoneprinter.PrinterEx;
import com.vfi.smartpos.deviceservice.aidl.IDeviceInfo;
import com.vfi.smartpos.deviceservice.aidl.IDeviceService;
import com.vfi.smartpos.deviceservice.aidl.IPrinter;
import com.vfi.smartpos.deviceservice.aidl.PrinterConfig;
import com.vfi.smartpos.deviceservice.aidl.PrinterListener;

import java.io.IOException;
import java.io.InputStream;

public class PrinterActivity extends AppCompatActivity {
    private static final String TAG = "VFI";

    ScrollView scrollView;
    IDeviceService idevice;
    IPrinter printer;
    // for print
    public static PrinterEx printerEx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer);
//        setUpToolbar();
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

        }

        scrollView = findViewById(R.id.scroll_view);
        InitializeFontFiles();
    }

    // connect service -- start
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            idevice = IDeviceService.Stub.asInterface(service);
            try {

                printer = idevice.getPrinter();


                IDeviceInfo iDeviceInfo = idevice.getDeviceInfo();
                String versions = "\nROM:" + iDeviceInfo.getROMVersion();
                versions += "\nSecurity:" + iDeviceInfo.getK21Version();
                versions += "\nHW:" + iDeviceInfo.getHardwareVersion();
                versions += "\nAndroid kernel:" + iDeviceInfo.getAndroidKernelVersion();
                versions += "\nFW Version:" + iDeviceInfo.getFirmwareVersion();
                Log.d(TAG, "Versions:" +versions);


            } catch (RemoteException e) {
                e.printStackTrace();
            }
            Toast.makeText(PrinterActivity.this, "bind service sucess", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    // connect service -- end

//    private void setUpToolbar(){
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
////        if(getSupportActionBar() != null){
////            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
////            getSupportActionBar().setDisplayShowHomeEnabled(true);
////        }
//    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent();
        intent.setAction("com.vfi.smartpos.device_service");
        intent.setPackage("com.vfi.smartpos.deviceservice");
        boolean isSucc = bindService(intent, conn, Context.BIND_AUTO_CREATE);
        if (!isSucc) {
            Log.i("TAG", "deviceService connect fail!");
        } else {
            Log.i("TAG", "deviceService connect success");
        }

//        bindService(new Intent("com.VFI.smartpos.device_service"), conn, Context.BIND_AUTO_CREATE);
    }

    // check assets fonts and copy to file system for Service -- start
    protected void InitializeFontFiles () {
        PrinterFonts.initialize(this.getAssets());
    }

    int gray = 0;
    private void print(String data){
        printerEx = new PrinterEx();
        try {
//            Bitmap scaledDown = null;
//            Bitmap ebarimtBitmap = null;
//            try {
//                ebarimtBitmap = Helper.getBitmapFromView(scrollView, scrollView.getChildAt(0).getHeight(), scrollView.getChildAt(0).getWidth());
//                scaledDown = Helper.scaleDown(ebarimtBitmap, 380, false);
//            } catch (Exception e){
//                Toast.makeText(this, "Түр хүлээнэ үү", Toast.LENGTH_SHORT).show();
//                e.printStackTrace();
//            }



            // bundle format for addText
            Bundle format = new Bundle();
            // bundle formate for AddTextInLine
            Bundle fmtAddTextInLine = new Bundle();

//            format.putInt(PrinterConfig.addText.FontSize.BundleName, PrinterConfig.addText.FontSize.NORMAL_DH_24_48_IN_BOLD);
//            format.putInt(PrinterConfig.addText.Alignment.BundleName, PrinterConfig.addText.Alignment.CENTER);
//            printer.addText(format, "Hello!");

//            format.putInt(PrinterConfig.addText.FontSize.BundleName, PrinterConfig.addText.FontSize.LARGE_DH_32_64_IN_BOLD);
//            format.putInt(PrinterConfig.addText.Alignment.BundleName, PrinterConfig.addText.Alignment.CENTER);
//            printer.addText(format, "Hello!");

            format.putInt(PrinterConfig.addText.FontSize.BundleName, PrinterConfig.addText.FontSize.HUGE_48);
            format.putInt(PrinterConfig.addText.Alignment.BundleName, PrinterConfig.addText.Alignment.CENTER);

            printer.addText(format, "--------------------------------");
            printer.addText(format, "Hello" );

//            Bundle fmtAddQRCode = new Bundle();
//            fmtAddQRCode.putInt(PrinterConfig.addQrCode.Height.BundleName, 128);
//            fmtAddQRCode.putInt(PrinterConfig.addQrCode.Offset.BundleName, 128);
//            printer.addBarCode(fmtAddQRCode, "1234565789");



//            byte[] buffer = null;
//            try {
//                //
//                InputStream is = this.getAssets().open("verifone_logo.jpg");
//                // get the size
//                int size = is.available();
//                // crete the array of byte
//                buffer = new byte[size];
//                is.read(buffer);
//                // close the stream
//                is.close();
//
//            } catch (IOException e) {
//                // Should never happen!
//                throw new RuntimeException(e);
//            }
            Bitmap bitmap = convertBitmap( "verifone_logo.jpg");

            if(bitmap != null) {
                printer.addText(format, "pic on11-------------------------");
                Bundle fmtImage = new Bundle();
//                fmtImage.putInt("offset", (384-200)/2);
//                fmtImage.putInt("width", 250);  // bigger then actual, will print the actual
//                fmtImage.putInt("height", 128); // bigger then actual, will print the actual

//                printer.addText(format, "pic on22-------------------------");
//                fmtImage.putInt("offset", 50 );
//                fmtImage.putInt("width", 100 ); // smaller then actual, will print the setting
//                fmtImage.putInt("height", 24); // smaller then actual, will print the setting
//                printer.addImage(fmtImage, buffer );


                fmtImage.putInt("offset", 0);
                fmtImage.putInt("width", 384);  // bigger then actual, will print the actual
                fmtImage.putInt("height", 128); // bigger then actual, will print the actual
                printerEx.addImage(fmtImage, bitmap);
                printer.addImage(fmtImage, printerEx.getData(false));


            } else {
                printer.addText(format, "pic off-------------------------");
            }

//            printer.addText(format, "--------------------------------");
//            Bundle fmtAddQRCode = new Bundle();
//            fmtAddQRCode.putInt(PrinterConfig.addQrCode.Height.BundleName, 128);
//            fmtAddQRCode.putInt(PrinterConfig.addQrCode.Offset.BundleName, 128);
//            printer.addBarCode(fmtAddQRCode, "1234565789");


            printer.addText(format, "--------------------------------");
            printer.addText(format, "\n\n");



            // start print here
            printer.startPrint(new MyListener());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    class MyListener extends PrinterListener.Stub {
        @Override
        public void onError(int error) throws RemoteException {
            Message msg = new Message();
            msg.getData().putString("msg", "print error,errno:" + error);
            handler.sendMessage(msg);
        }

        @Override
        public void onFinish() throws RemoteException {
            Message msg = new Message();
            msg.getData().putString("msg", "print finished");
            handler.sendMessage(msg);
        }
    }

    // log & display
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String string = msg.getData().getString("string");
            if( null != string )
                if( string.length()>0) {
//                    editText1.setText(string);
                }
            super.handleMessage(msg);
            Log.d(TAG, msg.getData().getString("msg"));
            Toast.makeText(PrinterActivity.this, msg.getData().getString("msg"), Toast.LENGTH_SHORT).show();

        }
    };

    public Bitmap convertBitmap(String data) {
        try {
            Bitmap bitmap;
            if (data!=null && !data.isEmpty()) {
                InputStream is = getAssets().open(data);
                bitmap = BitmapFactory.decodeStream(is);
//                                bitmap.getPixels();
            } else {
                bitmap = Bitmap.createBitmap(384, 4, Bitmap.Config.ARGB_8888);
                bitmap.eraseColor(Color.WHITE);
            }

            int height = bitmap.getHeight();
            int width = bitmap.getWidth();


//        int colorThreshold=0xFFa0a0a0;    //  0xFF------ for revert
//        int colorThreshold=0xFF606060;    //  0xFF------ for revert, 60 much white
//                        int colorThreshold=0xFF404040;    //  0xFF------ for revert, 40 more white
            int colorThreshold = 0;
//            if ((printerElement.style & PrinterDefine.PStyle_image_revert) == PrinterDefine.PStyle_image_revert) {
//                colorThreshold |= 0xFF000000;
//            }
//            if ((printerElement.style & PrinterDefine.PStyle_image_contrast_light) == PrinterDefine.PStyle_image_contrast_light) {
//                colorThreshold |= 0x00a0a0a0;
//            } else if ((printerElement.style & PrinterDefine.PStyle_image_contrast_normal) == PrinterDefine.PStyle_image_contrast_normal) {
//                colorThreshold |= 0x00808080;
//            } else if ((printerElement.style & PrinterDefine.PStyle_image_contrast_heavy) == PrinterDefine.PStyle_image_contrast_heavy) {
//                colorThreshold |= 0x00404040;
//            }

//                            printerEx.addImage(fmtImage, bitmap);
//                            printerEx.feedPixel( null, 8);

            // convert bitmap -- start
            int r, g, b;
            int r_t, g_t, b_t;
            r_t = ((colorThreshold & 0x00FF0000) >> 16);
            g_t = ((colorThreshold & 0x0000FF00) >> 8);
            b_t = ((colorThreshold & 0x000000FF));
            Log.d(TAG, "Color Threshold:" + r_t + ", " + g_t + ", " + b_t);

            int pixels[] = new int[width * height];
            int pixels2[] = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
            int n = 0;
            for (int i = 0; i < height; i++) {
//                                String line ="Pixel:";
                for (int j = 0; j < width; j++) {
                    int pixel = pixels[n];
//                                    if( j < 64 ){
//                                        line += Integer.toHexString(pixel);
////                                        line += ",";
//                                    }
                    r = ((pixel & 0x00FF0000) >> 16);
                    g = ((pixel & 0x0000FF00) >> 8);
                    b = ((pixel & 0x000000FF));
                    if ((colorThreshold & 0x00FFFFFF) > 0) {
                        // convert color
                        if (r > r_t) {
                            r = 0xFF;
                        }
                        if (g > g_t) {
                            g = 0xFF;
                        }
                        if (b > b_t) {
                            b = 0xFF;
                        }

                        int c = (r < g) ? r : g;
                        c = (c < b) ? c : b;

                        r = c;
                        g = c;
                        b = c;

//                                    pixels[n] = (0xFF000000 + (r << 16) + (g << 8) + b );
                        pixels[n] = (0xFF000000 + (c << 16) + (c << 8) + c);
                        pixels2[n] = pixels[n];
                    }

                    if ((colorThreshold & 0xFF000000) == 0xFF000000) {
                        // revert
                        if ((r + g + b) < 600) {
                            r = 0xFF;
                            g = 0xFF;
                            b = 0xFF;
                        } else {
                            r = 0;
                            g = 0;
                            b = 0;
//                                            r = 0xFF - r;
//                                            g = 0xFF - g;
//                                            b = 0xFF - b;
                        }
                        pixels[n] = (0xFF000000 + (r << 16) + (g << 8) + b);
                    }

//                                    if( j < 64 ){
//                                        line += "-";
//                                        line += Integer.toHexString(pixels[n]);
//                                        line += ",";
//                                    }

                    n++;
                }
//                                Log.d("PIXEL", line);
            }
//                            bitmap = Bitmap.createBitmap(pixels2, 0, width, width, height, Bitmap.Config.ARGB_8888);
//                            // convert bitmap -- end
//                            printerEx.addImage(fmtImage, bitmap);
//                            printerEx.feedPixel( null, 8);

            bitmap = Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.ARGB_8888);
            // convert bitmap -- end
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_printer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_print) {
            print("a");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
