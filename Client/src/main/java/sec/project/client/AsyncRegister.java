package sec.project.client;

import sec.project.library.Acknowledge;
import sec.project.library.AsymmetricCrypto;
import sec.project.library.ClientAPI;

import java.rmi.RemoteException;
import java.security.PublicKey;
import java.util.Map;

public class AsyncRegister implements Runnable {

    private Map.Entry<PublicKey, ClientAPI> stub;
    private PublicKey clientPublicKey;
    private String clientNumber;
    private byte[] signature;
    private Client client;

    public AsyncRegister(Map.Entry<PublicKey, ClientAPI> stub, PublicKey clientPublicKey, String clientNumber, byte[] signature, Client client){
        this.stub = stub;
        this.clientNumber = clientNumber;
        this.clientPublicKey = clientPublicKey;
        this.signature = signature;
        this.client = client;
    }

    @Override
    public void run() {

        try{

            this.stub.getValue().register(this.clientPublicKey, this.clientNumber, signature);
            this.client.incrementNumberOfTRegistersFinished();

        } catch (RemoteException e1) {
            System.out.println("\n" + e1.getMessage());
            this.client.incrementNumberOfTRegistersFinished();
            return;

        } catch (Exception e){
            e.printStackTrace();
            System.out.println("\nFailed to register in one of the servers.");
        }

    }
}