package sec.project.client;

import org.javatuples.Quartet;
import org.javatuples.Quintet;
import org.javatuples.Triplet;
import sec.project.library.AsymmetricCrypto;
import sec.project.library.ClientAPI;
import sec.project.library.ReadView;

import java.rmi.RemoteException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Map;

public class AsyncReadGeneral implements Runnable {

    private Map.Entry<PublicKey, ClientAPI> stub;
    private Client client;
    private int numberOfAnnouncements;
    private byte[] signature;
    private int ridGeneral;

    //Runnable(this, toReadClientPublicKey, Integer.parseInt(numberOfAnnouncements), signature)

    public AsyncReadGeneral(Map.Entry<PublicKey, ClientAPI> stub, Client client, int numberOfAnnouncements, byte[] signature){
        this.client = client;
        this.signature = signature;
        this.stub = stub;
        this.numberOfAnnouncements = numberOfAnnouncements;
        this.ridGeneral = this.client.getReadGeneralRid();

    }

    @Override
    public void run() {

        try {

            ReadView readGeneralResponse = this.stub.getValue().readGeneral(numberOfAnnouncements, this.ridGeneral, signature, this.client.getClientPublicKey());

            if(AsymmetricCrypto.validateDigitalSignature(readGeneralResponse.getSignature(), this.stub.getKey(),
                    AsymmetricCrypto.transformQuintetToString(readGeneralResponse.getAnnouncesGeneral()) + readGeneralResponse.getRid()) && this.ridGeneral == readGeneralResponse.getRid()){

                boolean valid = true;
                for(Quintet<Integer, String, String, byte[], ArrayList<Integer>> announce : readGeneralResponse.getAnnouncesGeneral()){

                    PublicKey clientPublicKey = AsymmetricCrypto.getPublicKeyFromCert("data/keys/client" + announce.getValue2() + "_certificate.crt");

                    if(!(AsymmetricCrypto.validateDigitalSignature(announce.getValue3(), clientPublicKey,
                            announce.getValue1() + announce.getValue0() + announce.getValue2()))){
                        valid = false;
                    }
                }

                if(valid){
                    if(this.ridGeneral == this.client.getReadGeneralRid()){
                        this.client.getReadGeneralResponses().put(this.stub.getKey(), readGeneralResponse);
                        this.client.incrementNumberOfAcks();
                    }
                }

            }else{
                System.out.println("Invalid Response from a server!");
            }


        } catch (RemoteException e1){
            if (this.ridGeneral == this.client.getReadGeneralRid()){
                System.out.println("\n" + e1.getMessage());
                this.client.getReadGeneralResponses().put(this.stub.getKey(), null);
                this.client.incrementNumberOfAborts();
                this.client.setException(true);

            }
            return;

        } catch (Exception e2){
            e2.printStackTrace();
        }
    }

}
