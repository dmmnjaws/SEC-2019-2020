package sec.project.server;

import java.util.ArrayList;

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