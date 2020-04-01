package sec.project.server;

import java.io.Serializable;
import java.util.ArrayList;

public class Announcement implements Serializable {
    private String info;
    private int id;
    private ArrayList<Integer> references;
    private String clientNumber;

    public Announcement(int number, String message, String clientNumber){
        this.clientNumber = clientNumber;
        this.id = number;
        this.references = new ArrayList<>();
        this.info = message.substring(0,message.indexOf("|"));
        String [] ref = message.substring(message.indexOf("|")+1, message.length()).split(" ");

        for(int i=1; i<ref.length; i++){
            if(Integer.valueOf(ref[i]) < this.id){
                this.references.add(Integer.valueOf(ref[i]));
            }
        }
    }

    public String printAnnouncement(){
        String ref = "";
        for(int i=0; i<this.references.size(); i++){
            ref += this.references.get(i) + " ";
        }

        return "\nAnnouncement id: "+ this.id + "\n message: " + this.info + "\n references: " + ref;
    }

    public ArrayList<Integer> getReferences() {
        return this.references;
    }

    public int getId() {
        return this.id;
    }

    public String getClientNumber() {
        return this.clientNumber;
    }

    public String getInfo() {
        return this.info;
    }
}