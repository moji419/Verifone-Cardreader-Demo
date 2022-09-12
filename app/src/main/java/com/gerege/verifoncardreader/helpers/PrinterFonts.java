package com.gerege.verifoncardreader.helpers;

import android.content.res.AssetManager;
import android.os.Environment;

public class PrinterFonts {
    public static final String FONT_AGENCYB = "agencyb.TTF";
    public static final String FONT_ALGER = "alger.TTF";
    public static final String FONT_BROADW = "broadw.TTF";
    public static final String FONT_CURLZ___ = "curlz.TTF";
    public static final String FONT_FORTE = "forte.TTF";
    public static final String FONT_KUNSTLER = "kunstler.TTF";
    public static final String FONT_segoesc = "segoescb.ttf";
    public static final String FONT_SHOWG = "showg.TTF";
    public static final String FONT_WINGDNG2 = "wingdng2.TTF";
    public static final String FONT_HuaWenLiShu = "stliti.TTF";
    public static final String FONT_HuaWenZhongSong = "stzhongs.TTF";

    public static String path = "";

    public static void initialize( AssetManager assets ) {
        String fileName = PrinterFonts.FONT_AGENCYB;
        path = Environment.getExternalStorageDirectory().getPath().concat("/fonts/");
        ExtraFiles.copy("fonts/" + fileName, path , fileName, assets, false );

        fileName = PrinterFonts.FONT_ALGER;
        ExtraFiles.copy("fonts/" + fileName, path , fileName, assets, false );

        fileName = PrinterFonts.FONT_BROADW;
        ExtraFiles.copy("fonts/" + fileName, path , fileName, assets, false );

        fileName = PrinterFonts.FONT_CURLZ___;
        ExtraFiles.copy("fonts/" + fileName, path , fileName, assets, false );

        fileName = PrinterFonts.FONT_FORTE;
        ExtraFiles.copy("fonts/" + fileName, path , fileName, assets, false );

        fileName = PrinterFonts.FONT_KUNSTLER;
        ExtraFiles.copy("fonts/" + fileName, path , fileName, assets, false );

        fileName = PrinterFonts.FONT_segoesc;
        ExtraFiles.copy("fonts/" + fileName, path , fileName, assets, false );

        fileName = PrinterFonts.FONT_SHOWG;
        ExtraFiles.copy("fonts/" + fileName, path , fileName, assets, false );

        fileName = PrinterFonts.FONT_WINGDNG2;
        ExtraFiles.copy("fonts/" + fileName, path , fileName, assets, false );

        fileName = PrinterFonts.FONT_HuaWenLiShu;
        ExtraFiles.copy("fonts/" + fileName, path , fileName, assets, false );

        fileName = PrinterFonts.FONT_HuaWenZhongSong;
        ExtraFiles.copy("fonts/" + fileName, path , fileName, assets, false );

    }
}
