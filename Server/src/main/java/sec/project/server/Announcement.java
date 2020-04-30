package sec.project.server;

import org.javatuples.Triplet;

import java.io.Serializable;
import java.util.ArrayList;

public class Announcement implements Serializable {
    private int id;
    private ArrayList<Integer> references;
    private String info;
    private byte[] signature;

    public Announcement(int number, String message, byte[] signature){
        this.info = message;
        this.id = number;
        this.signature = signature;
        this.references = new ArrayList<>();
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

    public String getInfo() {
        return this.info;
    }
    public byte[] getSignature() { return this.signature; }

}