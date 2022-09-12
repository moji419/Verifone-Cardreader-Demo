package com.gerege.verifoncardreader.verifoneprinter;

public enum PrinterItemType {
    STRING,
    LOGO_ASSETS,
    LOGO_STORAGE,
    LINE,
    FEED,   // the title.size is the pixel for feed
    BARCODE, // the title.size is for the height of barcode
    QRCODE, //  the title.size is for the size of qr rcode
    IMG_BCD,
}
