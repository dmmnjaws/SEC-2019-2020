package sec.project.server;

import org.javatuples.Quartet;
import org.javatuples.Quintet;
import org.javatuples.Triplet;
import sec.project.library.AsymmetricCrypto;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;

public class NNRegularRegister implements Serializable {

    //THIS IS STILL A (1,N) REGULAR REGISTER, IT MUST BE TRANSFORMER INTO A (N,N) REGULAR REGISTER

    private Quartet<Integer, String, String, byte[]> valueQuartet;
    private int wts;
    private int rid;
    private GeneralBoard generalBoard;
    private ArrayList<String> ackList;
    boolean iswriting = false;
    int clientid;

    public NNRegularRegister(GeneralBoard generalBoard){
        this.generalBoard = generalBoard;
        this.valueQuartet = new Quartet<>(0, null, null, null);
        this.rid = 0;
        this.wts = 0;

        this.ackList = new ArrayList<>();
    }

    public String write(int wts, String value, String clientNumber, byte[] signature, PublicKey clientPublicKey) throws NoSuchPaddingException,
            UnsupportedEncodingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException,
            InvalidKeyException {
        //Substituir por maior
        if (AsymmetricCrypto.validateDigitalSignature(signature, clientPublicKey,
                value + wts + clientNumber) && !this.generalBoard.getAnnouncements().containsKey(wts)){

            if(Integer.parseInt(ackList.get(0)) < 1/*client-id*/ && !ackList.isEmpty()){
                //substitui tudo, ou seja volta a fazer
            } else if(Integer.parseInt(ackList.get(0)) == wts){
                ackList.add("ACK");
            }

            this.valueQuartet = new Quartet<>(wts, value, clientNumber, signature);
            this.generalBoard.addAnnouncement(this.valueQuartet);

            while(ackList.size() < 69){
                if(ackList.get(0) != "clientInicial"){
                    //fecha a thread porque existe um cliente prioritario a tentar por
                    //throw new Exception();
                }
            }

            ArrayList<String> ackList = new ArrayList<>();

            if(wts > this.wts){
                this.wts = wts;
            }

        }

        //merely representative, the method never returns this.
        return "FAIL";
    }

    public ArrayList<Quintet<Integer, String, String, byte[], ArrayList<Integer>>> read(int number, int rid, byte[] signature, PublicKey clientPublicKey) throws NoSuchPaddingException,
            UnsupportedEncodingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {


        if (AsymmetricCrypto.validateDigitalSignature(signature, clientPublicKey,"" + number + rid)){

            return this.generalBoard.getAnnouncementsQuartets(number);
        }

        return null;
    }

    public int getWts() { return this.wts; }

    public int getRid() { return this.rid; }

    public Quartet<Integer, String, String, byte[]> getValueQuartet() { return this.valueQuartet; }

    public GeneralBoard getGeneralBoard() { return this.generalBoard; }
}
