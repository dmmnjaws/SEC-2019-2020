package sec.project.library;

import org.javatuples.Quartet;
import org.javatuples.Triplet;
import java.io.Serializable;
import java.util.ArrayList;

public class ReadView implements Serializable {
    private ArrayList<Triplet<Integer, String, byte[]>> announces;
    private ArrayList<Quartet<Integer, String, String, byte[]>> announcesGeneral;
    private int rid;
    private byte[] signature;

    public ReadView(ArrayList<Triplet<Integer, String, byte[]>> announces, int rid, byte[] signature){
        this.announces = announces;
        this.rid = rid;
        this.signature = signature;
    }

    public ReadView(int rid, byte[] signature, ArrayList<Quartet<Integer, String, String, byte[]>> announcesGeneral){
        this.announcesGeneral = announcesGeneral;
        this.rid = rid;
        this.signature = signature;
    }

    public byte[] getSignature() { return signature; }
    public ArrayList<Triplet<Integer, String, byte[]>> getAnnounces() { return this.announces; }
    public ArrayList<Quartet<Integer, String, String, byte[]>> getAnnouncesGeneral() { return this.announcesGeneral; }
    public int getRid() { return rid; }

}
