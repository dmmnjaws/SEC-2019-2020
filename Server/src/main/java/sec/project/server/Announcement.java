package sec.project.server;

import java.util.ArrayList;

public class Announcement {
    private String info;
    private int id;
    private ArrayList<Integer> references;

    public Announcement(int number,String message){
        this.id = number;
        this.references = new ArrayList<>();
        this.info = message.substring(0,message.indexOf("|"));
        String [] ref = message.substring(message.indexOf("|"), message.length()).split("| ");

        for(int i=0; i<ref.length; i++){
            if(Integer.valueOf(ref[i]) < this.id){
                this.references.add(Integer.valueOf(ref[i]));
            }
        }
    }

    public String printAnnouncement(){
        String ref = "";
        for(int i=0; i<references.size(); i++){
            ref += references.get(i) + " ";
        }

        return "Announcement id: "+ this.id + "\n message: " + this.info + "\n references: " + ref;
    }
}