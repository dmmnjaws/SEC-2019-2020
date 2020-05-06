package sec.project.server;

import org.javatuples.Quartet;
import org.javatuples.Triplet;
import sec.project.library.ReadView;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientLibrary implements Serializable {
    private String clientNumber;
    private Map<Integer, Announcement> announcements;
    private OneNAtomicRegister oneNAtomicRegister;
    private PublicKey clientPublicKey;

    public ClientLibrary(String clientNumber, PublicKey clientPublicKey){
        this.clientNumber = clientNumber;
        this.clientPublicKey = clientPublicKey;
        this.announcements = new HashMap<>();
        this.oneNAtomicRegister = new OneNAtomicRegister(this);
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

    public OneNAtomicRegister getOneNRegularRegister() { return this.oneNAtomicRegister; }

    public String write(int wts, String message, byte[] signature) throws NoSuchPaddingException,
            UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {

        return this.oneNAtomicRegister.write(wts, message, signature);
    }

    public ArrayList<Quartet<Integer, String, byte[], ArrayList<Integer>>> read(int number, int rid, byte[] signature, PublicKey clientPublicKey) throws NoSuchPaddingException,
            UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {

        return this.oneNAtomicRegister.read(number, rid, signature, clientPublicKey);
    }
}