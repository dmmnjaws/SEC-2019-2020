package sec.project.server;

import org.javatuples.Quartet;
import sec.project.library.ClientAPI;

import java.rmi.RemoteException;
import java.security.PublicKey;

public class AsyncReadyToCommit implements Runnable {

    private ClientAPI stub;
    private PublicKey clientPublicKey;
    private Quartet valueQuartet;
    private byte[] sSSignature;
    private PublicKey serverPublicKey;

    public AsyncReadyToCommit(ClientAPI stub, PublicKey clientPublicKey, Quartet valueQuartet, byte[] sSSignature,
                              PublicKey serverPublicKey){

        this.stub = stub;
        this.clientPublicKey = clientPublicKey;
        this.valueQuartet = valueQuartet;
        this.sSSignature = sSSignature;
        this.serverPublicKey = serverPublicKey;
    }

    @Override
    public void run() {

        try {
            if(valueQuartet == null){
                stub.addCommitRequest(clientPublicKey, null, sSSignature, serverPublicKey);
            } else {
                stub.addCommitRequest(clientPublicKey, valueQuartet, sSSignature, serverPublicKey);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
