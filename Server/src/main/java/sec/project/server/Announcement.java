package sec.project.server;

public class Announcement {
    private String message;
    private int number;

    public Announcement(int number,String message){
        this.number = number;
        this.message = message;
    }

    public String printAnnouncement(){
        return this.message + "\n";
    }
}
