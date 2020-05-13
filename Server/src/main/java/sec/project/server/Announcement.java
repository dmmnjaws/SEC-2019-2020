package sec.project.server;

import org.javatuples.Quartet;
import org.javatuples.Quintet;
import org.javatuples.Triplet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

public class Announcement implements Serializable {
    private ArrayList<Integer> references;

    // this is <wts, message, signature>
    private Triplet<Integer, String , byte[]> triplet;
    private Quartet<Integer, String, String, byte[]> quartet;

    public Announcement(Triplet<Integer, String, byte[]> triplet, Map<Integer, Announcement> existingReferences){
        this.triplet = triplet;
        this.references = new ArrayList<>();
        String message = triplet.getValue1();
        String [] ref = message.substring(message.indexOf("|")+1, message.length()).split(" ");

        for(int i=1; i<ref.length; i++){
            if(existingReferences.containsKey(Integer.valueOf(ref[i]))){
                this.references.add(Integer.valueOf(ref[i]));
            }
        }
    }

    public Announcement(Quartet<Integer, String, String, byte[]> quartet, Map<Integer, Announcement> existingReferences){

        System.out.println("DEBUG: " + quartet);
        this.quartet = quartet;
        this.references = new ArrayList<>();
        String message = quartet.getValue1();
        String [] ref = message.substring(message.indexOf("|")+1, message.length()).split(" ");

        for(int i=1; i<ref.length; i++){
            if(existingReferences.containsKey(Integer.valueOf(ref[i]))){
                this.references.add(Integer.valueOf(ref[i]));
            }
        }
    }

    public String printAnnouncement(){
        String ref = "";
        for(int i=0; i<this.references.size(); i++){
            ref += this.references.get(i) + " ";
        }

        if (triplet == null && quartet != null){
            return "\nAnnouncement id: "+ this.quartet.getValue0() + "\n message: " + this.quartet.getValue1().substring(0, this.quartet.getValue1().indexOf("|")) + "\n references: " + ref;
        }

        return "\nAnnouncement id: "+ this.triplet.getValue0() + "\n message: " + this.triplet.getValue1().substring(0, this.triplet.getValue1().indexOf("|")) + "\n references: " + ref;
    }

    public ArrayList<Integer> getReferences() {
        return this.references;
    }

    public Quartet<Integer, String, byte[], ArrayList<Integer>> getTriplet() {
        return new Quartet<>(this.triplet.getValue0(), this.triplet.getValue1(), this.triplet.getValue2(), this.references);
    }

    public Quintet<Integer, String, String, byte[], ArrayList<Integer>> getQuartet() {
        return new Quintet<>(this.quartet.getValue0(), this.quartet.getValue1(), this.quartet.getValue2(), this.quartet.getValue3(), this.references);
    }

}
