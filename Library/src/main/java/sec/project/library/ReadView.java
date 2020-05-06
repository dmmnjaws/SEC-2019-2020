package sec.project.library;

import org.javatuples.Quartet;
import org.javatuples.Quintet;
import org.javatuples.Triplet;
import java.io.Serializable;
import java.util.ArrayList;

public class ReadView implements Serializable {
    private ArrayList<Quartet<Integer, String, byte[], ArrayList<Integer>>> announces;
    private ArrayList<Quintet<Integer, String, String, byte[], ArrayList<Integer>>> announcesGeneral;
    private int rid;
    private byte[] signature;

    public ReadView(ArrayList<Quartet<Integer, String, byte[], ArrayList<Integer>>> announces, int rid, byte[] signature){
        this.announces = announces;
        this.rid = rid;
        this.signature = signature;
    }

    public ReadView(int rid, byte[] signature, ArrayList<Quintet<Integer, String, String, byte[], ArrayList<Integer>>> announcesGeneral){
        this.announcesGeneral = announcesGeneral;
        this.rid = rid;
        this.signature = signature;
    }

    public byte[] getSignature() { return signature; }
    public ArrayList<Quartet<Integer, String, byte[], ArrayList<Integer>>> getAnnounces() { return this.announces; }
    public ArrayList<Quintet<Integer, String, String, byte[], ArrayList<Integer>>> getAnnouncesGeneral() { return this.announcesGeneral; }
    public int getRid() { return rid; }

}
