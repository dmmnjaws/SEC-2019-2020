package sec.project.server;

import org.javatuples.Triplet;

import java.io.Serializable;
import java.util.ArrayList;

public class Announcement implements Serializable {
    private int id;
    private ArrayList<Integer> references;

    // this is <wts, message, signature>
    private Triplet<Integer, String , byte[]> triplet;

    public Announcement(int number, Triplet<Integer, String, byte[]> triplet){
        this.id = number;
        this.triplet = triplet;
        this.references = new ArrayList<>();
        String message = triplet.getValue1();
        String [] ref = message.substring(message.indexOf("|")+1, message.length()).split(" ");

        for(int i=1; i<ref.length; i++){
            if(Integer.valueOf(ref[i]) < this.id){
                this.references.add(Integer.valueOf(ref[i]));
            }
        }
    }

    public Announcement(int number, String message, String clientNumber){}

    public String printAnnouncement(){
        String ref = "";
        for(int i=0; i<this.references.size(); i++){
            ref += this.references.get(i) + " ";
        }


        return "\nAnnouncement id: "+ this.id + "\n message: " + this.triplet.getValue1().substring(0, this.triplet.getValue1().indexOf("|")) + "\n references: " + ref;
    }

    public ArrayList<Integer> getReferences() {
        return this.references;
    }

    public int getId() {
        return this.id;
    }
    public Triplet<Integer, String, byte[]> getTriplet() { return triplet; }

}