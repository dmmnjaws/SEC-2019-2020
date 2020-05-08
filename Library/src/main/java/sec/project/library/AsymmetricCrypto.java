package sec.project.library;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.javatuples.Quartet;
import org.javatuples.Quintet;
import org.javatuples.Triplet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


public class AsymmetricCrypto {

    public static byte [] wrapDigitalSignature(String msg, PrivateKey senderPrivateKey) throws
            NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException,
            IllegalBlockSizeException, UnsupportedEncodingException {

        //System.out.println("\nDEBUG: Wrapping Signature:\n" + msg);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, senderPrivateKey);
        byte [] hash = digestMessage(msg);
        return cipher.doFinal(hash);
    }

    public static boolean validateDigitalSignature(byte [] receivedHash, PublicKey senderPublicKey, String msg) throws
            NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException,
            IllegalBlockSizeException, UnsupportedEncodingException {

        //System.out.println("\nDEBUG: Unwrapping Signature:\n" + msg);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, senderPublicKey);
        byte [] localHash = digestMessage(msg);
        byte [] receivedDecryptHash = cipher.doFinal(receivedHash);
        //System.out.println("\nDEBUG: boolean - equal signatures?\n" + Arrays.equals(receivedDecryptHash,localHash));

        return Arrays.equals(receivedDecryptHash,localHash);
    }

    public static byte[] digestMessage(String msg) throws NoSuchAlgorithmException, UnsupportedEncodingException {

        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] messageBytes = msg.getBytes("UTF-8");

        return messageDigest.digest(messageBytes);
    }

    @Deprecated
    public static PrivateKey getPrivateKey(String filename) throws Exception {

        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");

        return kf.generatePrivate(spec);

    }

    @Deprecated
    public static PublicKey getPublicKey(String filename) throws Exception {

        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");

        return kf.generatePublic(spec);
    }

    public static KeyStore getKeyStore(String filename, String keystorePassword) throws Exception {

        KeyStore keyStore = KeyStore.getInstance("JKS");
        InputStream readStream = new FileInputStream(filename);
        keyStore.load(readStream, keystorePassword.toCharArray());
        readStream.close();

        return keyStore;
    }

    public static PrivateKey getPrivateKey(KeyStore keyStore, String privateKeyPassword, String allias) throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException {

        KeyStore.ProtectionParameter passwordParameter = new KeyStore.PasswordProtection(privateKeyPassword.toCharArray());
        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(allias, passwordParameter);

        return privateKeyEntry.getPrivateKey();
    }

    public static PublicKey getPublicKeyFromCert(String filename) throws FileNotFoundException, CertificateException {
        FileInputStream fin = new FileInputStream(filename);
        CertificateFactory f = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate)f.generateCertificate(fin);

        return certificate.getPublicKey();
    }

    public static String transformTripletToString(Triplet<Integer, String, byte[]> message) throws UnsupportedEncodingException {
        return "" + message.getValue0() + message.getValue1() + new String(message.getValue2(), "UTF-8");
    }

    public static String transformQuartetToString(ArrayList<Quartet<Integer, String, byte[], ArrayList<Integer>>> quartets) throws UnsupportedEncodingException {

        String result = "";

        for (Quartet<Integer, String, byte[], ArrayList<Integer>> quartet : quartets){
            result += "" + quartet.getValue0() + quartet.getValue1() + new String(quartet.getValue2(), "UTF-8") + quartet.getValue3();

        }

        return result;
    }

    public static String transformQuintetToString(ArrayList<Quintet<Integer, String, String, byte[], ArrayList<Integer>>> quintets) throws UnsupportedEncodingException {

        String result = "";

        for (Quintet<Integer, String, String, byte[], ArrayList<Integer>> quintet : quintets){
            result += "" + quintet.getValue0() + quintet.getValue1() + quintet.getValue2() + new String(quintet.getValue3(), "UTF-8") + quintet.getValue4();

        }

        return result;
    }
}
