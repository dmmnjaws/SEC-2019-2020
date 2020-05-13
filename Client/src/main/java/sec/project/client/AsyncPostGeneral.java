package sec.project.client;

import sec.project.library.Acknowledge;
import sec.project.library.ClientAPI;

import java.rmi.RemoteException;
import java.security.PublicKey;
import java.util.Map;

public class AsyncPostGeneral implements Runnable {

    private Map.Entry<PublicKey, ClientAPI> stub;
    private Client client;
    private int postGeneralWts;
    private String message;
    private byte[] signature;

    public AsyncPostGeneral(Map.Entry<PublicKey, ClientAPI> stub, Client client, int postGeneralWts, String message, byte[] signature){
        this.stub = stub;
        this.client = client;
        this.postGeneralWts = postGeneralWts;
        this.message = message;
        this.signature = signature;
    }

    @Override
    public void run() {

        try {
            Acknowledge acknowledge = this.stub.getValue().postGeneral(this.client.getClientPublicKey(), message, this.postGeneralWts, signature, null, null);

            if (acknowledge.getWts() == this.postGeneralWts){
                if(this.postGeneralWts == this.client.getPostGeneralWts()){
                    this.client.getPostGeneralAcks().put(this.stub.getKey(), acknowledge);
                    this.client.incrementNumberOfAcks();
                }
            }

        } catch (RemoteException e1) {
            if(this.postGeneralWts == this.client.getPostGeneralWts()){
                System.out.println("\n" + e1.getMessage());
                this.client.getPostGeneralAcks().put(this.stub.getKey(), null);
                this.client.setException(true);
                this.client.incrementNumberOfAborts();

            }

            return;

        }
    }

}
