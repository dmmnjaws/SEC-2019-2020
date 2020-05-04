package sec.project.client;

import org.javatuples.Quartet;
import org.javatuples.Triplet;
import sec.project.library.AsymmetricCrypto;
import sec.project.library.ClientAPI;
import sec.project.library.ReadView;

import java.rmi.RemoteException;
import java.security.PublicKey;
import java.util.Map;

public class AsyncReadGeneral implements Runnable {

    private Client client;
    private int numberOfAnnouncements;
    private byte[] signature;

    //Runnable(this, toReadClientPublicKey, Integer.parseInt(numberOfAnnouncements), signature)

    public AsyncReadGeneral(Client client, int numberOfAnnouncements, byte[] signature){
        this.client = client;
        this.signature = signature;
        this.numberOfAnnouncements = numberOfAnnouncements;
    }

    @Override
    public void run() {
        int ridGeneral = this.client.getReadGeneralRid();

        try {
            for (Map.Entry<PublicKey, ClientAPI> entry : this.client.getServerPublicKeys().entrySet()){

                ReadView readGeneralResponse = entry.getValue().readGeneral(numberOfAnnouncements, ridGeneral, signature, this.client.getClientPublicKey());

                if(AsymmetricCrypto.validateDigitalSignature(readGeneralResponse.getSignature(), entry.getKey(),
                        AsymmetricCrypto.transformQuartetToString(readGeneralResponse.getAnnouncesGeneral()) + readGeneralResponse.getRid()) && ridGeneral == readGeneralResponse.getRid()){

                    boolean valid = true;
                    for(Quartet<Integer, String, String, byte[]> announce : readGeneralResponse.getAnnouncesGeneral()){

                        PublicKey clientPublicKey = AsymmetricCrypto.getPublicKeyFromCert("data/keys/client" + announce.getValue2() + "_certificate.crt");

                        if(!(AsymmetricCrypto.validateDigitalSignature(announce.getValue3(), clientPublicKey,
                                announce.getValue1() + announce.getValue0() + announce.getValue2()))){
                            valid = false;
                        }
                    }

                    if(valid && readGeneralResponse.getAnnouncesGeneral().size() != 0){
                        this.client.getReadGeneralResponses().add(readGeneralResponse);
                    }

                }else{
                    System.out.println("Invalid Response from a server!");
                }

            }

        } catch (RemoteException e1){
            e1.printStackTrace();
        } catch (Exception e2){
            e2.printStackTrace();
        }
    }

}
