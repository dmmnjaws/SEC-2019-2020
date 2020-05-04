package sec.project.client;

import sec.project.library.Acknowledge;
import sec.project.library.ClientAPI;

import java.rmi.RemoteException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Map;

public class AsyncPost implements Runnable {

    private Client client;
    private String message;
    private byte[] signature;

    public AsyncPost(Client client, String message, byte[] signature){
        this.client = client;
        this.message = message;
        this.signature = signature;
    }

    @Override
    public void run() {

        int postWts = this.client.getPostWts();

        try {
            for (Map.Entry<PublicKey, ClientAPI> entry : this.client.getServerPublicKeys().entrySet()) {

                Acknowledge acknowledge = entry.getValue().post(this.client.getClientPublicKey(), message, postWts, signature);

                if (acknowledge.getWts() == postWts) {
                    this.client.getPostAcks().add(acknowledge);
                }

            }
        } catch (RemoteException e){
            e.printStackTrace();
        }

    }

}
