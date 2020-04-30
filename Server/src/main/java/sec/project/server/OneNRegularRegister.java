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

    public String write(int wts, int seqNumber, String value, byte[] signature) throws NoSuchPaddingException, UnsupportedEncodingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        if (AsymmetricCrypto.validateDigitalSignature(signature, this.clientLibrary.getClientPublicKey(), value + seqNumber + wts) && seqNumber == this.clientLibrary.getSeqNumber() && wts > this.valueTriplet.getValue0()){
            this.valueTriplet = new Triplet<>(wts, value, signature);
            this.clientLibrary.addAnnouncement(value, signature);
            this.wts = wts;
        }
        return "ACK";
    }
}
