package sec.project.server;

import java.util.ArrayList;
import java.util.List;

public class ClientLibrary {
    private int seqNumber;
    private String clientNumber;
    private ArrayList<Announcement> announcements;

    public ClientLibrary(String clientNumber){
        this.clientNumber = clientNumber;
        this.seqNumber = 1;
        this.announcements = new ArrayList<>();
    }

    public void addAnnouncement(String message){
        Announcement announcement = new Announcement(announcements.size() + 1, message, this.clientNumber);
        this.announcements.add(announcement);
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

    public void incrementSeqNumber(){
        this.seqNumber++;
    }
}