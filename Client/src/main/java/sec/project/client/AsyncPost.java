package sec.project.client;

import sec.project.library.Acknowledge;
import sec.project.library.ClientAPI;

import java.rmi.RemoteException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Map;

public class AsyncPost implements Runnable {

    private Map.Entry<PublicKey, ClientAPI> stub;
    private Client client;
    private String message;
    private byte[] signature;
    private String mode;
    private PublicKey toWriteClientPublicKey;
    private int writeBackWts;

    public AsyncPost(Map.Entry<PublicKey, ClientAPI>stub, Client client, String message, byte[] signature){
        this.message = message;
        this.signature = signature;
        this.client = client;
        this.stub = stub;
        this.mode = "POST";
    }

    public AsyncPost(Map.Entry<PublicKey, ClientAPI> stub, Client client, PublicKey toWriteClientPublicKey, String message, byte[] signature) {
        this.message = message;
        this.signature = signature;
        this.client = client;
        this.writeBackWts = this.client.getWriteBackWts();
        this.toWriteClientPublicKey = toWriteClientPublicKey;
        this.stub = stub;
        this.mode = "WRITEBACK";
    }

    @Override
    public void run() {

        int postWts = 0;
        PublicKey clientPublicKey = null;
        boolean isWriteBack;

        if (mode.equals("POST")) {
            postWts = this.client.getPostWts();
            clientPublicKey = this.client.getClientPublicKey();
            isWriteBack = false;

        } else if (mode.equals("WRITEBACK")) {
            postWts = this.writeBackWts;
            clientPublicKey = this.toWriteClientPublicKey;
            isWriteBack = true;

        } else {
            return;
        }


        try {

            Acknowledge acknowledge = this.stub.getValue().post(clientPublicKey, message, postWts, signature, isWriteBack);

            if (acknowledge.getWts() == postWts) {
                if((mode.equals("POST") && postWts == this.client.getPostWts()) || ((mode.equals("WRITEBACK") && postWts == this.client.getWriteBackWts()))){
                    this.client.getPostAcks().put(this.stub.getKey(), acknowledge);
                    this.client.incrementNumberOfAcks();
                }
            }


        } catch (RemoteException e1) {

            if((mode.equals("POST") && postWts == this.client.getPostWts()) || ((mode.equals("WRITEBACK") && postWts == this.client.getWriteBackWts()))){
                System.out.println("\n" + e1.getMessage());
                this.client.getPostAcks().put(this.stub.getKey(), null);
                this.client.incrementNumberOfAborts();
                this.client.setException(true);
            }

            return;

        }
    }

}
