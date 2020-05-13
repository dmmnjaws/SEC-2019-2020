package sec.project.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.javatuples.Quartet;
import org.javatuples.Triplet;
import org.junit.*;
import sec.project.library.Acknowledge;
import sec.project.library.AsymmetricCrypto;
import sec.project.library.ReadView;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.security.*;
import java.util.Scanner;

/**
 * Unit test for simple App.
 */
public class ServerTest
{
    PublicKey clientPublicKey;
    PrivateKey clientPrivatekey;
    KeyStore clientKeyStore;
    Server server;

    @Before
    public void populateForTest() {
        this.server = new Server(8000);
        try {
            this.clientKeyStore = AsymmetricCrypto.getKeyStore("data/test/clienttest_keystore.jks", "clienttest");
            this.clientPrivatekey = AsymmetricCrypto.getPrivateKey(clientKeyStore, "clienttest", "clienttest");
            this.clientPublicKey = AsymmetricCrypto.getPublicKeyFromCert("data/test/clienttest_certificate.crt");
        } catch (Exception e) {

            e.printStackTrace();

        }
    }


    @Test(expected = RemoteException.class)
    public void postWithoutRegisterTest() throws RemoteException {


        String testString = "ola1| ";
        Acknowledge ack;

        byte[] testBytes = new byte[0];
        try {
            testBytes = AsymmetricCrypto.wrapDigitalSignature(testString + "1", this.clientPrivatekey);
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        ack = server.post(this.clientPublicKey, testString, 1, testBytes, false);


    }

    @Test(expected = RemoteException.class)
    public void postGeneralWithoutRegisterTest() throws RemoteException {


        String testString = "ola1| ";
        Acknowledge ack;

        byte[] testBytes = new byte[0];
        try {
            testBytes = AsymmetricCrypto.wrapDigitalSignature(testString + "1" +"1", this.clientPrivatekey);
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        ack = server.postGeneral(this.clientPublicKey, testString, 1, testBytes, null, null);

    }

    @Test(expected = RemoteException.class)
    public void readWithoutRegisterTest() throws RemoteException {


        String testString = "ola1| ";
        ReadView readView;

        byte[] testBytes = new byte[0];
        try {
            testBytes = AsymmetricCrypto.wrapDigitalSignature(this.clientPublicKey.toString() + "1" + "1", this.clientPrivatekey);
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        readView = server.read(this.clientPublicKey,1,1, testBytes,this.clientPublicKey);

    }
    @Test(expected = RemoteException.class)
    public void readGeneralWithoutRegisterTest() throws RemoteException {


        String testString = "ola1| ";
        ReadView readView;

        byte[] testBytes = new byte[0];
        try {
            testBytes = AsymmetricCrypto.wrapDigitalSignature("1" + "1", this.clientPrivatekey);
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        readView = server.readGeneral(1,1, testBytes, this.clientPublicKey);

    }

    @Test(expected = RemoteException.class)
    public void doubleRegisterTest() throws RemoteException {
        Acknowledge ack;
        ReadView readView;
        byte[] testBytes;
        try {
            testBytes = AsymmetricCrypto.wrapDigitalSignature("1", this.clientPrivatekey);
            server.register(this.clientPublicKey, "1", testBytes);
            server.register(this.clientPublicKey, "1", testBytes);
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void postAndReadTest() {

        String testString = "ola2| ";
        Acknowledge ack;
        ReadView readView;
        byte[] testBytes;

        try{

        testBytes = AsymmetricCrypto.wrapDigitalSignature("1", this.clientPrivatekey);
        server.register(this.clientPublicKey, "1", testBytes);
        testBytes = AsymmetricCrypto.wrapDigitalSignature(testString + "1", this.clientPrivatekey);
        ack = server.post(this.clientPublicKey, testString, 1, testBytes, false);
        testBytes = AsymmetricCrypto.wrapDigitalSignature(this.clientPublicKey.toString() + "1" + "1", this.clientPrivatekey);
        readView = server.read(this.clientPublicKey,1,1, testBytes,this.clientPublicKey);
        assertEquals(testString, readView.getAnnounces().get(0).getValue1());

        } catch (Exception e){

            e.printStackTrace();

        }

    }

    @Test
    public void postAndReadGeneralTest() {
        String testString = "ola2| ";
        Acknowledge ack;
        ReadView readView;
        byte[] testBytes;

        try{

            testBytes = AsymmetricCrypto.wrapDigitalSignature("1", this.clientPrivatekey);
            server.register(this.clientPublicKey, "1", testBytes);
            testBytes = AsymmetricCrypto.wrapDigitalSignature(testString + "1" + "1", this.clientPrivatekey);
            ack = server.postGeneral(this.clientPublicKey, testString, 1, testBytes, null, null);
            testBytes = AsymmetricCrypto.wrapDigitalSignature("1" + "1", this.clientPrivatekey);
            readView = server.readGeneral(1,1, testBytes,this.clientPublicKey);
            assertEquals(testString, readView.getAnnouncesGeneral().get(0).getValue1());

        } catch (Exception e){

            e.printStackTrace();

        }
    }

    @After
    public void cleanState() {
        File file = new File("data/state8000.txt");
        file.delete();
        file = new File("data/state8000_backup.txt");
        file.delete();
    }

}
