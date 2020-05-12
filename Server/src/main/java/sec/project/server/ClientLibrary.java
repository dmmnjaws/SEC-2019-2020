package sec.project.server;

import org.javatuples.Quartet;
import org.javatuples.Triplet;
import sec.project.library.ClientAPI;
import sec.project.library.ReadView;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientLibrary implements Serializable {
    private String clientNumber;
    private Map<Integer, Announcement> announcements;
    private OneNAtomicRegister oneNAtomicRegister;
    private DoubleEchoBroadcaster doubleEchoBroadcaster;
    private PublicKey clientPublicKey;
    private Map<PublicKey, ClientAPI> stubs;
    private PublicKey serverPublicKey;
    private PrivateKey serverPrivateKey;

    public ClientLibrary(String clientNumber, PublicKey clientPublicKey, Map<PublicKey, ClientAPI> stubs, PublicKey serverPublicKey, PrivateKey serverPrivateKey){
        this.clientNumber = clientNumber;
        this.clientPublicKey = clientPublicKey;
        this.announcements = new HashMap<>();
        this.oneNAtomicRegister = new OneNAtomicRegister(this);
        this.stubs = stubs;
        this.serverPrivateKey = serverPrivateKey;
        this.serverPublicKey = serverPublicKey;
    }

    public synchronized void addAnnouncement(Triplet<Integer, String, byte[]> triplet){
        Announcement announcement = new Announcement(announcements.size() + 1, triplet, getAnnouncements());
        this.announcements.put(triplet.getValue0(), announcement);
        System.out.println("\nOn client" + clientNumber + "'s board:"+ announcement.printAnnouncement());
    }

    public ArrayList<Quartet<Integer, String, byte[], ArrayList<Integer>>> getAnnouncementsTriplets(int number){
        ArrayList<Quartet<Integer, String, byte[], ArrayList<Integer>>> result = new ArrayList<>();

        //this must be in the beginning of the method to avoid concurrent changes in the maxWts during this method.
        int maxWts = this.oneNAtomicRegister.getWts();

        int aux;
        if(announcements.size() < number){
            aux = announcements.size();
        }else{
            aux = number;
        }

        for (int i = maxWts - aux + 1; i <= maxWts; i++) {
            result.add(this.announcements.get(i).getTriplet());
        }

        return result;
    }

    public String getClientNumber(){
        return this.clientNumber;
    }

    public PublicKey getClientPublicKey() {
        return this.clientPublicKey;
    }

    public ArrayList<Integer> getExistingReferences(){
        return new ArrayList<>(this.announcements.keySet());
    }

    public Map<Integer, Announcement> getAnnouncements() { return this.announcements; }

    public OneNAtomicRegister getOneNAtomicRegister() { return this.oneNAtomicRegister; }

    public DoubleEchoBroadcaster getDoubleEchoBroadcaster() { return this.doubleEchoBroadcaster; }

    public Map<PublicKey, ClientAPI> getStubs() { return this.stubs; }

    public String write(int wts, String message, byte[] signature) throws NoSuchPaddingException,
            UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, RemoteException, InterruptedException {

        this.doubleEchoBroadcaster = new DoubleEchoBroadcaster(this);
        return this.oneNAtomicRegister.write(wts, message, signature, this.serverPrivateKey, this.serverPublicKey);
    }

    public ArrayList<Quartet<Integer, String, byte[], ArrayList<Integer>>> read(int number, int rid, byte[] signature, PublicKey clientPublicKey) throws NoSuchPaddingException,
            UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {

        return this.oneNAtomicRegister.read(number, rid, signature, clientPublicKey);
    }
}