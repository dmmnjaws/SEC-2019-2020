package sec.project.server;


import org.javatuples.Quartet;
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

public class GeneralBoard implements Serializable {

    private ArrayList<Announcement> announcements;
    private NNRegularRegister nNRegularRegister;

    public GeneralBoard(){
        this.announcements = new ArrayList<>();
        this.nNRegularRegister = new NNRegularRegister(this);
    }

    public String getAnnouncements(int number){
        String print = "";

        if(this.announcements.size() - number < 1) {
            for (int i = 0; i < announcements.size(); i++) {
                print += announcements.get(i).printAnnouncement();
            }
        }else{
            for (int i = number; i < announcements.size(); i++) {
                print += announcements.get(i).printAnnouncement();
            }
        }

        return print;
    }

    public synchronized void addAnnouncement(Quartet<Integer, String, String, byte[]> quartet){
        Announcement announcement = new Announcement(announcements.size()+1, quartet);
        this.announcements.add(announcement);
        System.out.println("\nOn the General Board:"+ announcement.printAnnouncement());
    }

    public ArrayList<Announcement> getAnnouncements() {
        return this.announcements;
    }

    public ArrayList<Quartet<Integer, String, String, byte[]>> getAnnouncementsQuartets(int number){
        ArrayList<Quartet<Integer, String, String, byte[]>> result = new ArrayList<>();

        if (number < announcements.size()) {
            List<Announcement> resultAnnouncements = this.announcements.subList(announcements.size() - number, announcements.size());
            for (Announcement announcement : resultAnnouncements){
                result.add(announcement.getQuartet());
            }
        } else {
            for (Announcement announcement : this.announcements){
                result.add(announcement.getQuartet());
            }
        }


        return result;
    }

    public String write(int wts, String message, String clientNumber, byte[] signature, PublicKey clientPublicKey) throws NoSuchPaddingException,
            UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {

        return this.nNRegularRegister.write(wts, message, clientNumber, signature, clientPublicKey);
    }

    public ArrayList<Quartet<Integer, String, String, byte[]>> read(int number, int rid, byte[] signature, PublicKey clientPublicKey) throws NoSuchPaddingException,
            UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {

        return this.nNRegularRegister.read(number, rid, signature, clientPublicKey);
    }
}

