package sec.project.library;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


public class AsymmetricCrypto {

    public static String encryptText(String msg, Key key) throws NoSuchAlgorithmException, NoSuchPaddingException,
            UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return Base64.encode(cipher.doFinal(msg.getBytes("UTF-8")));

    }

    public static String decryptText(String msg, Key key) throws InvalidKeyException, UnsupportedEncodingException,
            IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException {

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return new String(cipher.doFinal(Base64.decode(msg)), "UTF-8");
    }

    public static byte [] wrapDigitalSignature(String msg, PrivateKey privateKey) throws
            NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException,
            IllegalBlockSizeException, UnsupportedEncodingException {

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        byte [] hash = digestMessage(msg);
        return cipher.doFinal(hash);
    }

    public static boolean validateDigitalSignature(byte [] receivedHash, PublicKey publicKey, String msg) throws
            NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException,
            IllegalBlockSizeException, UnsupportedEncodingException {

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        byte [] localHash = digestMessage(msg);

        return localHash.equals(receivedHash);
    }

    public static byte[] digestMessage(String message) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] messageBytes = message.getBytes("UTF-8");
        return messageDigest.digest(messageBytes);
    }

    /*
    public static String prepareToSend(String msg, PrivateKey clientPrivateKey, PublicKey serverPublicKey) {

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(msg.getBytes("UTF-8"));
            String digestedMessage = messageDigest.digest().toString();
            String secureMessage = encryptText(encryptText(digestedMessage, clientPrivateKey), serverPublicKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
     */

    public static PrivateKey getPrivateKey(String filename) throws Exception {

        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");

        return kf.generatePrivate(spec);

    }

    public static PublicKey getPublicKey(String filename) throws Exception {

        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");

        return kf.generatePublic(spec);
    }
}
