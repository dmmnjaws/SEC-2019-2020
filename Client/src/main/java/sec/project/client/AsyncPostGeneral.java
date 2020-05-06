package sec.project.client;

import sec.project.library.Acknowledge;
import sec.project.library.ClientAPI;

import java.rmi.RemoteException;
import java.security.PublicKey;
import java.util.Map;

public class AsyncPostGeneral implements Runnable {

    private Client client;
    private String message;
    private byte[] signature;

    public AsyncPostGeneral(Client client, String message, byte[] signature){
        this.client = client;
        this.message = message;
        this.signature = signature;
    }

    @Override
    public void run() {

        int postGeneralWts = this.client.getPostGeneralWts();

        try {
            for (Map.Entry<PublicKey, ClientAPI> entry : this.client.getServerPublicKeys().entrySet()) {

                Acknowledge acknowledge = entry.getValue().postGeneral(this.client.getClientPublicKey(), message, postGeneralWts, signature);

                if (acknowledge.getWts() == postGeneralWts){
                    client.getPostGeneralAcks().add(acknowledge);
                }

            }

        } catch (RemoteException e){
            e.printStackTrace();
        }

    }

}
