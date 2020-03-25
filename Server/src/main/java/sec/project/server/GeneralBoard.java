package sec.project.server;


import java.security.PublicKey;
import java.util.ArrayList;

public class GeneralBoard {

    private ArrayList<Announcement> announcements;

    public GeneralBoard(){
        this.announcements = new ArrayList<>();
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

    public void addAnnouncement(String clientNumber, String message){
        Announcement announcement = new Announcement(announcements.size() + 1, message, clientNumber);
        this.announcements.add(announcement);
        System.out.println("\nOn the General Board:"+ announcement.printAnnouncement());
    }
}
