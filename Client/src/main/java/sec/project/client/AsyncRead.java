package sec.project.client;

import org.javatuples.Triplet;
import sec.project.library.Acknowledge;
import sec.project.library.AsymmetricCrypto;
import sec.project.library.ClientAPI;
import sec.project.library.ReadView;

import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.security.PublicKey;
import java.util.Map;

public class AsyncRead implements Runnable {

    private Client client;
    private PublicKey toReadClientPublicKey;
    private int numberOfAnnouncements;
    private byte[] signature;

    //Runnable(this, toReadClientPublicKey, Integer.parseInt(numberOfAnnouncements), signature)

    public AsyncRead(Client client, PublicKey toReadClientPublicKey, int numberOfAnnouncements, byte[] signature){
        this.client = client;
        this.toReadClientPublicKey = toReadClientPublicKey;
        this.signature = signature;
        this.numberOfAnnouncements = numberOfAnnouncements;
    }

    @Override
    public void run() {
        int rid = this.client.getReadRid();

        try {
            for (Map.Entry<PublicKey, ClientAPI> entry : this.client.getServerPublicKeys().entrySet()) {
                ReadView readResponse = entry.getValue().read(this.toReadClientPublicKey, this.numberOfAnnouncements,
                        rid, this.signature, this.client.getClientPublicKey());

                if (AsymmetricCrypto.validateDigitalSignature(readResponse.getSignature(), entry.getKey(),
                        AsymmetricCrypto.transformTripletToString(readResponse.getAnnounces()) + readResponse.getRid())
                        && rid == readResponse.getRid()) {

                    boolean valid = true;
                    for (Triplet<Integer, String, byte[]> announce : readResponse.getAnnounces()) {
                        if (!(AsymmetricCrypto.validateDigitalSignature(announce.getValue2(), toReadClientPublicKey,
                                announce.getValue1() + announce.getValue0()))) {
                            valid = false;
                        }
                    }

                    if (valid && readResponse.getAnnounces().size() != 0) {
                        this.client.getReadResponses().add(readResponse);
                    }

                } else {
                    System.out.println("\nInvalid Response from a server!");
                }

            }
        } catch (RemoteException e1){
            e1.printStackTrace();
        } catch (Exception e2){
            e2.printStackTrace();
        }
    }

}