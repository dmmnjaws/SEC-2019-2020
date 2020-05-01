package sec.project.server;

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
import java.util.List;

public class ClientLibrary implements Serializable {
    private int seqNumber;
    private String clientNumber;
    private ArrayList<Announcement> announcements;
    private OneNRegularRegister oneNRegularRegister;
    private PublicKey clientPublicKey;

    public ClientLibrary(String clientNumber, PublicKey clientPublicKey){
        this.clientNumber = clientNumber;
        this.clientPublicKey = clientPublicKey;
        this.seqNumber = 1;
        this.announcements = new ArrayList<>();
        this.oneNRegularRegister = new OneNRegularRegister(this);
    }

    public synchronized void addAnnouncement(Triplet<Integer, String, byte[]> triplet){
        Announcement announcement = new Announcement(announcements.size() + 1, triplet);
        this.announcements.add(announcement);
        incrementSeqNumber();
        System.out.println("\nOn client" + clientNumber + "'s board:"+ announcement.printAnnouncement());
        System.out.println("\nDEBUG: Triplet: " + triplet.toString());
    }

    public String getAnnouncements(int number){
        String print = "";

        if (number < announcements.size()) {
            List<Announcement> resultAnnouncements = this.announcements.subList(announcements.size() - number, announcements.size());
            for (Announcement announcement : resultAnnouncements){
                print += "\n" + announcement.printAnnouncement();
            }
        } else {
            for (Announcement announcement : this.announcements){
                print += "\n" + announcement.printAnnouncement();
            }
        }

        return print;
    }

    public ArrayList<Triplet<Integer, String, byte[]>> getAnnouncementsTriplets(int number){
        ArrayList<Triplet<Integer, String, byte[]>> result = new ArrayList<>();

        if (number < announcements.size()) {
            List<Announcement> resultAnnouncements = this.announcements.subList(announcements.size() - number, announcements.size());
            for (Announcement announcement : resultAnnouncements){
                result.add(announcement.getTriplet());
            }
        } else {
            for (Announcement announcement : this.announcements){
                result.add(announcement.getTriplet());
            }
        }

        return result;
    }

    public String getClientNumber(){
        return this.clientNumber;
    }

    public int getSeqNumber() {
        return this.seqNumber;
    }

    public PublicKey getClientPublicKey() {
        return this.clientPublicKey;
    }

    public OneNRegularRegister getOneNRegularRegister() { return this.oneNRegularRegister; }

    public ArrayList<Announcement> getAnnouncements() {
        return this.announcements;
    }

    public void incrementSeqNumber(){
        this.seqNumber++;
    }

    public String write(int wts, String message, byte[] signature) throws NoSuchPaddingException,
            UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {

        return this.oneNRegularRegister.write(wts, message, signature);
    }

    public ArrayList<Triplet<Integer, String, byte[]>> read(int number, int rid, byte[] signature, PublicKey clientPublicKey) throws NoSuchPaddingException,
            UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {

        return this.oneNRegularRegister.read(number, rid, signature, clientPublicKey);
    }
}