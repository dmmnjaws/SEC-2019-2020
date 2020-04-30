package sec.project.server;

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

    public synchronized void addAnnouncement(String message, byte[] signature){
        Announcement announcement = new Announcement(announcements.size() + 1, message, signature);
        this.announcements.add(announcement);
        incrementSeqNumber();
        System.out.println("\nOn client" + clientNumber + "'s board:"+ announcement.printAnnouncement());
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

    public String getClientNumber(){
        return this.clientNumber;
    }

    public int getSeqNumber() {
        return this.seqNumber;
    }

    public PublicKey getClientPublicKey() {
        return this.clientPublicKey;
    }

    public ArrayList<Announcement> getAnnouncements() {
        return this.announcements;
    }

    public void incrementSeqNumber(){
        this.seqNumber++;
    }

    public String write(int wts, int seqNumber, String message, byte[] signature) throws NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        return this.oneNRegularRegister.write(wts, seqNumber, message, signature);
    }
}