package sec.project.library;

import java.io.Serializable;

public class Acknowledge implements Serializable {
    private String message;
    private byte [] signature;
    private int seqNumber;

    public Acknowledge(String message, byte [] signature){
        this.message = message;
        this.signature = signature;
    }

    public Acknowledge(int seqNumber, String message, byte [] signature){
        this.message = message;
        this.signature = signature;
        this.seqNumber = seqNumber;
    }

    public String getMessage() {
        return this.message;
    }

    public byte[] getSignature() {
        return this.signature;
    }

    public int getSeqNumber() { return this.seqNumber; }
}
