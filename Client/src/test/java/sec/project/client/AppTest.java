package sec.project.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import sec.project.library.AsymmetricCrypto;

import javax.crypto.Cipher;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Unit test for simple App.
 */
public class AppTest 
{

    KeyStore keyStore;
    PrivateKey privatekey;
    PublicKey publicKey;

    @Before
    public void populateForTest(){
        try {
            keyStore = AsymmetricCrypto.getKeyStore("data/keys/client1_keystore.jks", "client1password");
            privatekey = AsymmetricCrypto.getPrivateKey(keyStore, "client1password", "client1");
            publicKey = AsymmetricCrypto.getPublicKeyFromCert("data/keys/client1_certificate.crt");
        }catch (Exception e) {

            e.printStackTrace();

        }
    }


    @Test
    public void wrapAndValidateDigitalSignatureTest(){
        try {
            String test = "ola";
            byte[] bytes = AsymmetricCrypto.wrapDigitalSignature(test, privatekey);
            assertTrue(AsymmetricCrypto.validateDigitalSignature(bytes, publicKey, test));
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

}
