package sec.project.client;

import sec.project.library.Acknowledge;
import sec.project.library.ClientAPI;

import java.rmi.RemoteException;
import java.security.PublicKey;
import java.util.Map;

public class AsyncPostGeneral implements Runnable {

    private ClientAPI stub;
    private Client client;
    private int postGeneralWts;
    private String message;
    private byte[] signature;

    public AsyncPostGeneral(ClientAPI stub, Client client, int postGeneralWts, String message, byte[] signature){
        this.stub = stub;
        this.client = client;
        this.postGeneralWts = postGeneralWts;
        this.message = message;
        this.signature = signature;
    }

    @Override
    public void run() {

        try {
            Acknowledge acknowledge = this.stub.postGeneral(this.client.getClientPublicKey(), message, this.postGeneralWts, signature, null, null);

            if (acknowledge.getWts() == this.postGeneralWts){
                this.client.getPostGeneralAcks().add(acknowledge);
            }

        } catch (RemoteException e){
            e.printStackTrace();
        }
    }

}
