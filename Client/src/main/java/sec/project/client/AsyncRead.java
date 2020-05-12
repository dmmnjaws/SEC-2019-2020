package sec.project.client;

import org.javatuples.Quartet;
import org.javatuples.Triplet;
import sec.project.library.Acknowledge;
import sec.project.library.AsymmetricCrypto;
import sec.project.library.ClientAPI;
import sec.project.library.ReadView;

import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Map;

public class AsyncRead implements Runnable {

    private Map.Entry<PublicKey, ClientAPI> stub;
    private Client client;
    private PublicKey toReadClientPublicKey;
    private int numberOfAnnouncements;
    private byte[] signature;

    //Runnable(this, toReadClientPublicKey, Integer.parseInt(numberOfAnnouncements), signature)

    public AsyncRead(Map.Entry<PublicKey, ClientAPI> stub, Client client, PublicKey toReadClientPublicKey, int numberOfAnnouncements, byte[] signature){
        this.client = client;
        this.toReadClientPublicKey = toReadClientPublicKey;
        this.signature = signature;
        this.stub = stub;
        this.numberOfAnnouncements = numberOfAnnouncements;
    }

    @Override
    public void run() {
        int rid = this.client.getReadRid();

        try {

            ReadView readResponse = this.stub.getValue().read(this.toReadClientPublicKey, this.numberOfAnnouncements,
                    rid, this.signature, this.client.getClientPublicKey());

            if (AsymmetricCrypto.validateDigitalSignature(readResponse.getSignature(), this.stub.getKey(),
                    AsymmetricCrypto.transformQuartetToString(readResponse.getAnnounces()) + readResponse.getRid())
                    && rid == readResponse.getRid()) {

                boolean valid = true;
                for (Quartet<Integer, String, byte[], ArrayList<Integer>> announce : readResponse.getAnnounces()) {
                    if (!(AsymmetricCrypto.validateDigitalSignature(announce.getValue2(), toReadClientPublicKey,
                            announce.getValue1() + announce.getValue0()))) {
                        valid = false;
                    }
                }

                if (valid) {
                    this.client.getReadResponses().put(this.stub.getKey(), readResponse);
                    this.client.incrementNumberOfReadResponses();
                }

            } else {
                System.out.println("\nInvalid Response from a server!");
            }

        } catch (RemoteException e1){
            System.out.println("\n" + e1.getMessage());
            this.client.getReadResponses().put(this.stub.getKey(), null);
            this.client.incrementNumberOfReadResponses();
            this.client.setException(true);
            return;

        } catch (Exception e2){
            e2.printStackTrace();
        }
    }

}