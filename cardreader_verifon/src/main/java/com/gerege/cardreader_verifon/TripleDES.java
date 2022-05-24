package com.gerege.cardreader_verifon;

import android.util.Base64;
import android.util.Log;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class TripleDES {

    public static String ALGO = "RSA";
    public static String ALGO_3DES = "DESede/ECB/NoPadding";
    public static String ALGO_3DES_PDD = "DESede/CBC/PKCS7Padding";
    public static String ALG = "RSA";
    public static String ALG_3DES = "DESede";

    public static String _encrypt(String message, String secretKey) throws Exception {

        Cipher cipher = Cipher.getInstance(ALGO);
        cipher.init(Cipher.ENCRYPT_MODE, getSecreteKey(secretKey));

        byte[] plainTextBytes = message.getBytes("UTF-8");
        byte[] buf = cipher.doFinal(plainTextBytes);
        byte[] base64Bytes = Base64.encode(buf, Base64.DEFAULT);
        String base64EncryptedString = new String(base64Bytes);
        return base64EncryptedString;
    }

    public static String _decrypt(String encryptedText, String secretKey) throws Exception {

        byte[] message = Base64.decode(encryptedText.getBytes(), Base64.DEFAULT);

        Cipher decipher = Cipher.getInstance(ALGO);
        decipher.init(Cipher.DECRYPT_MODE, getSecreteKey(secretKey));

        byte[] plainText = decipher.doFinal(message);

        return new String(plainText, "UTF-8");
    }

    public static SecretKey getSecreteKey(String secretKey) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] digestOfPassword = md.digest(secretKey.getBytes("utf-8"));
        byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
        SecretKey key = new SecretKeySpec(keyBytes, "DESede");
        return key;
    }

    public static byte[] _encrypt(byte[] message, byte[] secretKey, boolean hasP) throws Exception {

        Cipher cipher = Cipher.getInstance(hasP ? ALGO_3DES_PDD : ALGO_3DES);
        SecretKey key = new SecretKeySpec(secretKey, ALG_3DES);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] buf = cipher.doFinal(message);
        //byte[] base64Bytes = Base64.encode(buf, Base64.DEFAULT);
        //String base64EncryptedString = new String(base64Bytes);
        return buf;
    }

    public static boolean verify(byte[] sk, byte[] pk) throws Exception {
        KeyGenerator gen = KeyGenerator.getInstance("DESede");
        gen.init(112);
        SecretKey key = gen.generateKey();

        byte[] b = key.getEncoded();
        Log.d("qweqwe", "gneerated key len: " + b.length);
        if (b.length > 0) {
            Log.d("qweqwe", "gneerated key: " + TLVUtils.b2h(b));
        }

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DESede");
        KeyPair pair = keyGen.genKeyPair();
        Log.d("qweqwe", "gneerated-1 key len: " + pair.getPublic().getEncoded().length);
        Log.d("qweqwe", "gneerated-2 key len: " + pair.getPrivate().getEncoded().length);

        //KeyPair keyPair = keyGen.generateKeyPair();
//        PublicKey publicKey = keyPair.getPublic();
//        PrivateKey privateKey = keyPair.getPrivate();
        X509EncodedKeySpec spec = new X509EncodedKeySpec(pk);
        KeyFactory keyFactory = KeyFactory.getInstance(ALG_3DES);
        PublicKey publicKey = keyFactory.generatePublic(spec);
        PrivateKey privateKey = keyFactory.generatePrivate(new X509EncodedKeySpec(pk));

        // create a challenge
        byte[] challenge = new byte[10000];
        ThreadLocalRandom.current().nextBytes(challenge);

        // sign using the private key
        Signature sig = Signature.getInstance(ALGO_3DES);
        sig.initSign(privateKey);
        sig.update(challenge);
        byte[] signature = sig.sign();

        // verify signature using the public key
        sig.initVerify(publicKey);
        sig.update(challenge);

        return sig.verify(signature);
    }

    public static byte[] _decrypt3DES(byte[] encryptedData, byte[] secretKey) throws Exception {

        Cipher decipher = Cipher.getInstance(ALGO_3DES);
        SecretKey key = new SecretKeySpec(secretKey, ALG_3DES);
        decipher.init(Cipher.DECRYPT_MODE, key);

        return decipher.doFinal(encryptedData);
    }

    public static byte[] _decrypt(byte[] encryptedData, byte[] secretKey) throws Exception {

        Cipher decipher = Cipher.getInstance(ALGO);
        //SecretKey key = new SecretKeySpec(secretKey, ALG);
        //byte[] keyBytes = Base64.decode(secretKey, Base64.DEFAULT);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(secretKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSa", new BouncyCastleProvider());
        PublicKey key = keyFactory.generatePublic(spec);
        //PublicKey key = generateRsaPublicKey(new BigInteger(secretKey), new BigInteger(new byte[]{0x03}));

        Log.d("qweqwe", "key, IsNul: " + (key == null));
        if (key != null) {
            Log.d("qweqwe", "key format: " + key.getFormat());
            Log.d("qweqwe", "key algorithm: " + key.getAlgorithm());

        }
        decipher.init(Cipher.DECRYPT_MODE, key);

        return decipher.doFinal(encryptedData);
    }

    public static byte[] performRSA(byte[] dataBytes, byte[] expBytes, byte[] modBytes) {

        int inBytesLength = dataBytes.length;

        if (expBytes[0] >= (byte) 0x80) {
            //Prepend 0x00 to modulus
            byte[] tmp = new byte[expBytes.length + 1];
            tmp[0] = (byte) 0x00;
            System.arraycopy(expBytes, 0, tmp, 1, expBytes.length);
            expBytes = tmp;
        }

        if (modBytes[0] >= (byte) 0x80) {
            //Prepend 0x00 to modulus
            byte[] tmp = new byte[modBytes.length + 1];
            tmp[0] = (byte) 0x00;
            System.arraycopy(modBytes, 0, tmp, 1, modBytes.length);
            modBytes = tmp;
        }

        if (dataBytes[0] >= (byte) 0x80) {
            //Prepend 0x00 to signed data to avoid that the most significant bit is interpreted as the "signed" bit
            byte[] tmp = new byte[dataBytes.length + 1];
            tmp[0] = (byte) 0x00;
            System.arraycopy(dataBytes, 0, tmp, 1, dataBytes.length);
            dataBytes = tmp;
        }

        BigInteger exp = new BigInteger(expBytes);
        BigInteger mod = new BigInteger(modBytes);
        BigInteger data = new BigInteger(dataBytes);

        byte[] result = data.modPow(exp, mod).toByteArray();

        if (result.length == (inBytesLength + 1) && result[0] == (byte) 0x00) {
            //Remove 0x00 from beginning of array
            byte[] tmp = new byte[inBytesLength];
            System.arraycopy(result, 1, tmp, 0, inBytesLength);
            result = tmp;
        }

        return result;
    }

    public static PublicKey generateRsaPublicKey(BigInteger modulus, BigInteger publicExponent) {
        try {
            return KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus, publicExponent));
        } catch (Exception e) {
            Log.d("qweqwe, ", "key failed: " + e);
        }
        return null;
    }
}
