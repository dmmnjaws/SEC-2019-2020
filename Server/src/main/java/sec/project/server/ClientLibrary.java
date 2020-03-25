package sec.project.server;

import java.util.ArrayList;

public class ClientLibrary {
    private int seqNumber;
    private String id;
    private ArrayList<Announcement> announcements;

    public ClientLibrary(String clientId){
        this.id = clientId;
        this.seqNumber = 1;
        this.announcements = new ArrayList<>();
    }

    public void addAnnouncement(String message){
        Announcement announcement = new Announcement(announcements.size() + 1, message);
        this.announcements.add(announcement);
        System.out.println(announcement.printAnnouncement());
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
}