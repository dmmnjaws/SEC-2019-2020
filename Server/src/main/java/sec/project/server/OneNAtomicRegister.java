package sec.project.server;

import org.javatuples.Quartet;
import org.javatuples.Triplet;
import sec.project.library.AsymmetricCrypto;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

public class OneNAtomicRegister implements Serializable {

    private Triplet <Integer, String, byte[]> valueTriplet;
    private int wts;
    private int rid;
    private ClientLibrary clientLibrary;

    //Init()
    public OneNAtomicRegister(ClientLibrary clientLibrary){
        this.clientLibrary = clientLibrary;
        this.valueTriplet = new Triplet<>(0, null, null);
        this.rid = 0;
        this.wts = 0;
    }

    public String write(int wts, String value, byte[] signature, PrivateKey serverPrivateKey, PublicKey serverPublicKey) throws NoSuchPaddingException,
            UnsupportedEncodingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException,
            InvalidKeyException, RemoteException, InterruptedException {

        String result = "BADSIGNATURE";
        if (AsymmetricCrypto.validateDigitalSignature(signature, this.clientLibrary.getClientPublicKey(), value + wts)) {

            result = "IGNORED";

            if (!this.clientLibrary.getAnnouncements().containsKey(wts)) {

                result = "BADBROADCAST";

                Triplet<Integer, String, byte[]> auxTriplet = new Triplet<>(wts, value, signature);
                this.valueTriplet = this.clientLibrary.getDoubleEchoBroadcaster().write(auxTriplet, serverPrivateKey, serverPublicKey);

                if (this.valueTriplet != null && this.valueTriplet.getValue1() != null && this.valueTriplet.getValue2() != null) {
                    this.clientLibrary.addAnnouncement(this.valueTriplet);
                    if (wts > this.wts) {
                        this.wts = wts;
                    }
                    return "ACK";
                }
            }
        }

        return result;
    }

    public ArrayList<Quartet<Integer, String, byte[], ArrayList<Integer>>> read(int number, int rid, byte[] signature, PublicKey clientPublicKey) throws NoSuchPaddingException,
            UnsupportedEncodingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {

        if (AsymmetricCrypto.validateDigitalSignature(signature, clientPublicKey,this.clientLibrary.getClientPublicKey().toString() + number + rid)){

            return this.clientLibrary.getAnnouncementsTriplets(number);
        }

        return null;
    }

    public ClientLibrary getClientLibrary() {
        return this.clientLibrary;
    }

    public int getRid() {
        return this.rid;
    }

    public int getWts() {
        return this.wts;
    }

    public Triplet<Integer, String, byte[]> getValueTriplet() {
        return this.valueTriplet;
    }

}