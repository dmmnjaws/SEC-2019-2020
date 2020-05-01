package sec.project.server;

import org.javatuples.Quartet;
import org.javatuples.Triplet;
import sec.project.library.Acknowledge;
import sec.project.library.AsymmetricCrypto;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;

public class OneNRegularRegister {

    private Triplet <Integer, String, byte[]> valueTriplet;
    private ArrayList<Object> readList;
    private int wts;
    private int rid;
    private ClientLibrary clientLibrary;

    //Init()
    public OneNRegularRegister(ClientLibrary clientLibrary){
        this.clientLibrary = clientLibrary;
        this.valueTriplet = new Triplet<>(0, null, null);
        this.readList = new ArrayList<>();
        this.rid = 0;
        this.wts = 0;
    }

    public String write(int wts, String value, byte[] signature) throws NoSuchPaddingException,
            UnsupportedEncodingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException,
            InvalidKeyException {

        if (AsymmetricCrypto.validateDigitalSignature(signature, this.clientLibrary.getClientPublicKey(),
                value + wts) && wts > this.wts){

            this.valueTriplet = new Triplet<>(wts, value, signature);
            this.clientLibrary.addAnnouncement(value, wts, signature, this.valueTriplet);
            this.wts = wts;
        }

        return "ACK";
    }

    public ArrayList<Triplet<Integer, String, byte[]>> read(int number, int rid, byte[] signature, PublicKey clientPublicKey) throws NoSuchPaddingException,
            UnsupportedEncodingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {

        if (AsymmetricCrypto.validateDigitalSignature(signature, clientPublicKey,this.clientLibrary.getClientPublicKey().toString() + number + rid)){

            return this.clientLibrary.getAnnouncementsTriplets(number);
        }

        return null;
    }

}