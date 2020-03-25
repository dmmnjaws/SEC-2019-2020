package sec.project.server;

import java.util.ArrayList;

public class ClientLibrary {
    private int seqNumber;
    private String id;
    private ArrayList<Announcement> announcements;

    public ClientLibrary(String clientId){
        this.id = clientId;
        this.seqNumber = 1;
    }

    public void addAnnouncement(String message){
        this.announcements.add(new Announcement(announcements.size() + 1, message));
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