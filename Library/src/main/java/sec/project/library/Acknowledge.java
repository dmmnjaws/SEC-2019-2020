package sec.project.library;

import java.io.Serializable;

public class Acknowledge implements Serializable {
    private String message;
    private byte [] signature;

    public Acknowledge(String message, byte [] signature){
        this.message = message;
        this.signature = signature;
    }

    public String getMessage() {
        return message;
    }

    public byte[] getSignature() {
        return signature;
    }
}
