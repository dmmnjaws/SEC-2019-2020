package sec.project.server;

import org.javatuples.Triplet;
import sec.project.library.AsymmetricCrypto;
import sec.project.library.ClientAPI;

import java.rmi.RemoteException;
import java.security.PublicKey;
import java.util.Map;

public class AsyncSendEcho implements Runnable {

    private ClientAPI stub;
    private PublicKey clientPublicKey;
    private Triplet<Integer, String, byte[]> message;
    private byte[] signature;
    private PublicKey senderServerPublicKey;
    private boolean isReady;

    public AsyncSendEcho(ClientAPI stub, PublicKey clientPublicKey, Triplet<Integer, String, byte[]> message, byte[] signature, PublicKey senderServerPublicKey, boolean isReady){
        this.stub = stub;
        this.clientPublicKey = clientPublicKey;
        this.message = message;
        this.signature = signature;
        this.senderServerPublicKey = senderServerPublicKey;
        this.isReady = isReady;
    }

    @Override
    public void run() {

        try {
            if(isReady){
                stub.ready(this.clientPublicKey, this.message, this.signature, this.senderServerPublicKey);
            } else {
                stub.echo(this.clientPublicKey, this.message, this.signature, this.senderServerPublicKey);
            }
        } catch (RemoteException e){
            e.printStackTrace();
        }
    }

}
