package sec.project.library;

import java.io.Serializable;

public class Acknowledge implements Serializable {
    private String message;
    private byte [] signature;
    private int wts;

    public Acknowledge(String message, byte [] signature){
        this.message = message;
        this.signature = signature;
    }

    public Acknowledge(int wts, String message, byte [] signature){
        this.message = message;
        this.signature = signature;
        this.wts = wts;
    }

    public String getMessage() {
        return this.message;
    }

    public byte[] getSignature() {
        return this.signature;
    }

    public int getWts() { return this.wts; }
}
