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
    private String mode;
    private PublicKey toWriteClientPublicKey;
    private int writeBackWts;

    public AsyncPost(Client client, String message, byte[] signature){
        this.message = message;
        this.signature = signature;
        this.client = client;
        this.mode = "POST";
    }

    public AsyncPost(Client client, int writeBackWts, PublicKey toWriteClientPublicKey, String message, byte[] signature) {
        this.message = message;
        this.signature = signature;
        this.client = client;
        this.writeBackWts = writeBackWts;
        this.toWriteClientPublicKey = toWriteClientPublicKey;
        this.mode = "WRITEBACK";
    }

    @Override
    public void run() {

        int postWts = 0;
        PublicKey clientPublicKey = null;

        if (mode.equals("POST")) {
            postWts = this.client.getPostWts();
            clientPublicKey = this.client.getClientPublicKey();

        } else if (mode.equals("WRITEBACK")) {
            postWts = this.writeBackWts;
            clientPublicKey = this.toWriteClientPublicKey;

        } else {
            return;
        }


        try {
            for (Map.Entry<PublicKey, ClientAPI> entry : this.client.getServerPublicKeys().entrySet()) {

                Acknowledge acknowledge = entry.getValue().post(clientPublicKey, message, postWts, signature);

                if (acknowledge.getWts() == postWts) {
                    this.client.getPostAcks().add(acknowledge);
                }

            }
        } catch (RemoteException e){
            e.printStackTrace();
        }
    }

}
