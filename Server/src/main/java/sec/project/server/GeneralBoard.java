package sec.project.server;


import org.javatuples.Quartet;
import org.javatuples.Quintet;
import org.javatuples.Triplet;

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

public class GeneralBoard implements Serializable {

    private Map<Integer, Announcement> announcements;
    private NNRegularRegister nNRegularRegister;

    public GeneralBoard(){
        this.announcements = new HashMap<>();
        this.nNRegularRegister = new NNRegularRegister(this);
    }

    public synchronized void addAnnouncement(Quartet<Integer, String, String, byte[]> quartet){
        Announcement announcement = new Announcement(announcements.size()+1, quartet, getAnnouncements());
        this.announcements.put(quartet.getValue0(), announcement);
        System.out.println("\nOn the General Board:"+ announcement.printAnnouncement());
    }

    public ArrayList<Quintet<Integer, String, String, byte[], ArrayList<Integer>>> getAnnouncementsQuartets(int number){
        ArrayList<Quintet<Integer, String, String, byte[], ArrayList<Integer>>> result = new ArrayList<>();

        //this must be in the beginning of the method to avoid concurrent changes in the maxWts during this method.
        int maxWts = this.nNRegularRegister.getWts();

        int aux;
        if(announcements.size() < number){
            aux = announcements.size();
        }else{
            aux = number;
        }

        for (int i = maxWts - aux + 1; i <= maxWts; i++) {
            result.add(this.announcements.get(i).getQuartet());
        }

        return result;
    }

    public ArrayList<Integer> getExistingReferences(){
        ArrayList<Integer> result = new ArrayList<>(this.announcements.keySet());
        if (result == null){
            return new ArrayList<>();
        }

        return result;
    }

    public NNRegularRegister getnNRegularRegister() { return this.nNRegularRegister; }

    public Map<Integer, Announcement> getAnnouncements() { return this.announcements; }

    public String write(int wts, String message, String clientNumber, byte[] signature, PublicKey clientPublicKey) throws NoSuchPaddingException,
            UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {

        return this.nNRegularRegister.write(wts, message, clientNumber, signature, clientPublicKey);
    }

    public ArrayList<Quintet<Integer, String, String, byte[], ArrayList<Integer>>> read(int number, int rid, byte[] signature, PublicKey clientPublicKey) throws NoSuchPaddingException,
            UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {

        return this.nNRegularRegister.read(number, rid, signature, clientPublicKey);
    }
}

