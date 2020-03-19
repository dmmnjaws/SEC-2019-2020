package sec.project.library;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


public class AsymmetricCrypto {
    private Cipher cipher;

    public AsymmetricCrypto() throws NoSuchAlgorithmException, NoSuchPaddingException {
        this.cipher = Cipher.getInstance("RSA");
    }

    public String encryptText(String msg, PrivateKey key) throws NoSuchAlgorithmException, NoSuchPaddingException,
            UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {

        this.cipher.init(Cipher.ENCRYPT_MODE, key);
        return Base64.encode(cipher.doFinal(msg.getBytes("UTF-8")));

    }

    public String decryptText(String msg, PublicKey key) throws InvalidKeyException, UnsupportedEncodingException,
                IllegalBlockSizeException, BadPaddingException {

        this.cipher.init(Cipher.DECRYPT_MODE, key);
        return new String(cipher.doFinal(Base64.decode(msg)), "UTF-8");
    }

}
