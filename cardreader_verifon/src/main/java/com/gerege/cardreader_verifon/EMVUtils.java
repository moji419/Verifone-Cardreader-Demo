package com.gerege.cardreader_verifon;

public class EMVUtils {

    /**
     * EMV book 3 -> Annex C1
     */
    public static boolean isSDASupported(byte[] AIP) {
        return AIP != null && AIP.length == 2 && (AIP[0] & 0x40) == 0x40;
    }

    /**
     * EMV book 3 -> Annex C1
     */
    public static boolean isDDASupported(byte[] AIP) {
        return AIP != null && AIP.length == 2 && (AIP[0] & 0x20) == 0x20;
    }

    /**
     * EMV book 3 -> Annex C1
     * Cardholder verification
     */
    public static boolean isCHVSupported(byte[] AIP) {
        return AIP != null && AIP.length == 2 && (AIP[0] & 0x10) == 0x10;
    }

    /**
     * EMV book 3 -> Annex C1
     * Terminal risk management
     */
    public static boolean needTRMSupported(byte[] AIP) {
        return AIP != null && AIP.length == 2 && (AIP[0] & 0x08) == 0x08;
    }

    /**
     * EMV book 3 -> Annex C1
     */
    public static boolean isCDASupported(byte[] AIP) {
        return AIP != null && AIP.length == 2 && (AIP[0] & 0x01) == 0x01;
    }

    public static void addSupportedCAPKs() {
        // VISA
        EMV.addCAPK("03", "01", "A000000003", "C696034213D7D8546984579D1D0F0EA519CFF8DEFFC429354CF3A871A6F7183F1228DA5C7470C055387100CB935A712C4E2864DF5D64BA93FE7E63E71F25B1E5F5298575EBE1C63AA617706917911DC2A75AC28B251C7EF40F2365912490B939BCA2124A30A28F54402C34AECA331AB67E1E79B285DD5771B5D9FF79EA630B75");
        EMV.addCAPK("03", "03", "A000000003", "B3E5E667506C47CAAFB12A2633819350846697DD65A796E5CE77C57C626A66F70BB630911612AD2832909B8062291BECA46CD33B66A6F9C9D48CED8B4FC8561C8A1D8FB15862C9EB60178DEA2BE1F82236FFCFF4F3843C272179DCDD384D541053DA6A6A0D3CE48FDC2DC4E3E0EEE15F");
        EMV.addCAPK("03", "05", "A000000003", "D0135CE8A4436C7F9D5CC66547E30EA402F98105B71722E24BC08DCC80AB7E71EC23B8CE6A1DC6AC2A8CF55543D74A8AE7B388F9B174B7F0D756C22CBB5974F9016A56B601CCA64C71F04B78E86C501B193A5556D5389ECE4DEA258AB97F52A3");
        EMV.addCAPK("03", "05", "A000000003", "D0135CE8A4436C7F9D5CC66547E30EA402F98105B71722E24BC08DCC80AB7E71EC23B8CE6A1DC6AC2A8CF55543D74A8AE7B388F9B174B7F0D756C22CBB5974F9016A56B601CCA64C71F04B78E86C501B193A5556D5389ECE4DEA258AB97F52A3");
        EMV.addCAPK("03", "06", "A000000003", "F934FC032BE59B609A9A649E04446F1B365D1D23A1E6574E490170527EDF32F398326159B39B63D07E95E6276D7FCBB786925182BC0667FBD8F6566B361CA41A38DDF227091B87FA4F47BAC780AC47E15A6A0FB65393EB3473E8D193A07EB579");
        EMV.addCAPK("03", "07", "A000000003", "A89F25A56FA6DA258C8CA8B40427D927B4A1EB4D7EA326BBB12F97DED70AE5E4480FC9C5E8A972177110A1CC318D06D2F8F5C4844AC5FA79A4DC470BB11ED635699C17081B90F1B984F12E92C1C529276D8AF8EC7F28492097D8CD5BECEA16FE4088F6CFAB4A1B42328A1B996F9278B0B7E3311CA5EF856C2F888474B83612A82E4E00D0CD4069A6783140433D50725F");
        EMV.addCAPK("03", "08", "A000000003", "D9FD6ED75D51D0E30664BD157023EAA1FFA871E4DA65672B863D255E81E137A51DE4F72BCC9E44ACE12127F87E263D3AF9DD9CF35CA4A7B01E907000BA85D24954C2FCA3074825DDD4C0C8F186CB020F683E02F2DEAD3969133F06F7845166ACEB57CA0FC2603445469811D293BFEFBAFAB57631B3DD91E796BF850A25012F1AE38F05AA5C4D6D03B1DC2E568612785938BBC9B3CD3A910C1DA55A5A9218ACE0F7A21287752682F15832A678D6E1ED0B");
        EMV.addCAPK("03", "09", "A000000003", "9D912248DE0A4E39C1A7DDE3F6D2588992C1A4095AFBD1824D1BA74847F2BC4926D2EFD904B4B54954CD189A54C5D1179654F8F9B0D2AB5F0357EB642FEDA95D3912C6576945FAB897E7062CAA44A4AA06B8FE6E3DBA18AF6AE3738E30429EE9BE03427C9D64F695FA8CAB4BFE376853EA34AD1D76BFCAD15908C077FFE6DC5521ECEF5D278A96E26F57359FFAEDA19434B937F1AD999DC5C41EB11935B44C18100E857F431A4A5A6BB65114F174C2D7B59FDF237D6BB1DD0916E644D709DED56481477C75D95CDD68254615F7740EC07F330AC5D67BCD75BF23D28A140826C026DBDE971A37CD3EF9B8DF644AC385010501EFC6509D7A41");
        EMV.addCAPK("03", "10", "A000000003", "9F2701C0909CCBD8C3ED3E071C69F776160022FF3299807ED7A035ED5752770E232D56CC3BE159BD8F0CA8B59435688922F406F55C75639457BBABEFE9A86B2269EF223E34B91AA6DF2CCAD03B4AD4B443D61575CA960845E6C69040101E231D9EF811AD99B0715065A0E661449C41B4B023B7716D1E4AFF1C90704E55AE1225");

    }
}
