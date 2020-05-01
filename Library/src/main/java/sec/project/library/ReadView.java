package sec.project.library;

import org.javatuples.Triplet;
import java.io.Serializable;
import java.util.ArrayList;

public class ReadView implements Serializable {
    private ArrayList<Triplet<Integer, String, byte[]>> announces;
    private int rid;
    private byte[] signature;

    public ReadView(ArrayList<Triplet<Integer, String, byte[]>> announces, int rid, byte[] signature){
        this.announces = announces;
        this.rid = rid;
        this.signature = signature;
    }

    public byte[] getSignature() { return signature; }
    public ArrayList<Triplet<Integer, String, byte[]>> getAnnounces() { return this.announces; }
    public int getRid() { return rid; }

}
