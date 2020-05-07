package sec.project.server;

import org.javatuples.Quartet;
import org.javatuples.Quintet;
import org.javatuples.Triplet;
import sec.project.library.AsymmetricCrypto;
import sec.project.library.ClientAPI;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NNRegularRegister implements Serializable {

    private Quartet<Integer, String, String, byte[]> valueQuartet;
    private int wts;
    private int rid;
    private GeneralBoard generalBoard;
    private Map<PublicKey, String> ackList;
    private int acks;
    private int nThreads;
    private int byzantineWrite;
    private transient Object lock = new Object();

    public NNRegularRegister(GeneralBoard generalBoard){
        this.generalBoard = generalBoard;
        this.valueQuartet = null;
        this.rid = 0;
        this.wts = 0;
        this.acks = 0;
        this.nThreads = 0;
        this.byzantineWrite = 0;

        this.ackList = new HashMap<>();
    }

    public String write(int wts, String value, String clientNumber, byte[] signature, PublicKey clientPublicKey,
                        byte[] senderServerSignature, PublicKey senderServerPublicKey , PrivateKey serverPrivateKey,
                        PublicKey serverPublicKey, Map<PublicKey, ClientAPI> stubs) throws NoSuchPaddingException,
            UnsupportedEncodingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException,
            Exception {

        if(this.lock == null){
            this.lock = new Object();
        }

        if(senderServerPublicKey != null && senderServerSignature != null && !AsymmetricCrypto.validateDigitalSignature(senderServerSignature,
                senderServerPublicKey, value + wts + clientNumber + new String(signature, "UTF-8"))){

            return "Invalid server response";
        }

        if (AsymmetricCrypto.validateDigitalSignature(signature, clientPublicKey,
                value + wts + clientNumber) && wts > this.wts){

            synchronized (this.lock) {
                if (this.valueQuartet == null) {

                    this.valueQuartet = new Quartet<>(wts, value, clientNumber, signature);
                    this.acks++;
                    System.out.println("Client receive");
                    byte[] sSSignature = AsymmetricCrypto.wrapDigitalSignature(value + wts + clientNumber + new String(signature, "UTF-8"), serverPrivateKey);

                    for (Map.Entry<PublicKey, ClientAPI> entry : stubs.entrySet()) {

                        AsyncSendAck sendAck = new AsyncSendAck(entry.getValue(), clientPublicKey, value, wts, signature,
                                sSSignature, serverPublicKey);
                        new Thread(sendAck).start();

                    }

                    this.nThreads++;

                } else if (Integer.parseInt(this.valueQuartet.getValue2()) > Integer.parseInt(clientNumber)
                        || this.valueQuartet.getValue0() < wts) {

                    System.out.println("Switch write value");
                    this.valueQuartet = new Quartet<>(wts, value, clientNumber, signature);
                    this.ackList = new HashMap<>();
                    this.acks = 1;
                    this.nThreads++;

                    byte[] sSSignature = AsymmetricCrypto.wrapDigitalSignature(value + wts + clientNumber + new String(signature, "UTF-8"), serverPrivateKey);

                    for (Map.Entry<PublicKey, ClientAPI> entry : stubs.entrySet()) {

                        AsyncSendAck sendAck = new AsyncSendAck(entry.getValue(), clientPublicKey, value, wts, signature,
                                sSSignature, serverPublicKey);
                        new Thread(sendAck).start();

                    }
                } else if (this.valueQuartet.getValue0() == wts && !this.valueQuartet.getValue1().equals(value)
                        && Integer.parseInt(this.valueQuartet.getValue2()) == Integer.parseInt(clientNumber)){

                    //Byzantine client write detected
                    this.byzantineWrite++;
                    return null;

                } else if (this.valueQuartet.getValue0() == wts
                        && Integer.parseInt(this.valueQuartet.getValue2()) == Integer.parseInt(clientNumber)) {

                    if (stubs.containsKey(senderServerPublicKey) && !ackList.containsKey(senderServerPublicKey)) {
                        this.ackList.put(senderServerPublicKey, "Ack");
                        this.acks++;
                        System.out.println("Receive ack");
                        return null;
                    }
                    return null;
                }
            }

            while(this.acks <= (stubs.size() + 1 + ((stubs.size() + 1) / 3)) / 2
                    && this.byzantineWrite < (stubs.size() + 1) - (stubs.size() + 1 + ((stubs.size() + 1) / 3)) / 2){

                Thread.sleep(250);
                System.out.println("Waiting for acks "+ (((stubs.size() + 1 + ((stubs.size() + 1) / 3)) / 2) + 1) +"... currently -> " + this.acks);

            }

            System.out.println("Finishing an thread");

            synchronized (this.lock) {
                if (this.byzantineWrite >= (stubs.size() + 1) - (stubs.size() + 1 + ((stubs.size() + 1) / 3)) / 2) {
                    this.nThreads--;
                    if (this.nThreads == 0) {
                        this.ackList = new HashMap<>();
                        this.acks = 0;
                        this.byzantineWrite = 0;
                        this.valueQuartet = null;
                    }
                    throw new Exception("Client " + clientNumber + " attempted byzantine write");
                } else if (clientNumber.equals(this.valueQuartet.getValue2())) {
                    this.generalBoard.addAnnouncement(this.valueQuartet);
                    this.wts = wts;
                    this.nThreads--;
                    if (this.nThreads == 0) {
                        this.ackList = new HashMap<>();
                        this.acks = 0;
                        this.byzantineWrite = 0;
                        this.valueQuartet = null;
                    }
                    return "ACK";
                } else {
                    this.nThreads--;
                    if (this.nThreads == 0) {
                        this.byzantineWrite = 0;
                        this.ackList = new HashMap<>();
                        this.acks = 0;
                        this.valueQuartet = null;
                    }

                    throw new Exception("Write from " + clientNumber + " was unsuccessful");
                }
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
