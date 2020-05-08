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

    //Runnable(this, toReadClientPublicKey, Integer.parseInt(numberOfAnnouncements), signature)

    public AsyncReadGeneral(Map.Entry<PublicKey, ClientAPI> stub, Client client, int numberOfAnnouncements, byte[] signature){
        this.client = client;
        this.signature = signature;
        this.stub = stub;
        this.numberOfAnnouncements = numberOfAnnouncements;
    }

    @Override
    public void run() {
        int ridGeneral = this.client.getReadGeneralRid();

        try {

            ReadView readGeneralResponse = this.stub.getValue().readGeneral(numberOfAnnouncements, ridGeneral, signature, this.client.getClientPublicKey());

            if(AsymmetricCrypto.validateDigitalSignature(readGeneralResponse.getSignature(), this.stub.getKey(),
                    AsymmetricCrypto.transformQuintetToString(readGeneralResponse.getAnnouncesGeneral()) + readGeneralResponse.getRid()) && ridGeneral == readGeneralResponse.getRid()){

                boolean valid = true;
                for(Quintet<Integer, String, String, byte[], ArrayList<Integer>> announce : readGeneralResponse.getAnnouncesGeneral()){

                    PublicKey clientPublicKey = AsymmetricCrypto.getPublicKeyFromCert("data/keys/client" + announce.getValue2() + "_certificate.crt");

                    if(!(AsymmetricCrypto.validateDigitalSignature(announce.getValue3(), clientPublicKey,
                            announce.getValue1() + announce.getValue0() + announce.getValue2()))){
                        valid = false;
                    }
                }

                if(valid){
                    this.client.getReadGeneralResponses().put(this.stub.getKey(), readGeneralResponse);
                    this.client.incrementNumberOfReadGeneralResponses();
                }

            }else{
                System.out.println("Invalid Response from a server!");
            }


        } catch (RemoteException e1){
            System.out.println("\n" + e1.getMessage());
            this.client.getReadGeneralResponses().put(this.stub.getKey(), null);
            this.client.incrementNumberOfReadGeneralResponses();
            this.client.setException(true);
            return;

        } catch (Exception e2){
            e2.printStackTrace();
        }
    }

}
