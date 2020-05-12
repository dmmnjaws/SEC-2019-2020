package sec.project.server;

import sec.project.library.Acknowledge;
import sec.project.library.ClientAPI;

import java.rmi.RemoteException;
import java.security.PublicKey;

public class AsyncSendAck implements Runnable {

    private ClientAPI stub;
    private PublicKey clientPublicKey;
    private String value;
    private int wts;
    private byte[] signature;
    private byte[] sSSignature;
    private PublicKey serverPublicKey;

    public AsyncSendAck(ClientAPI stub, PublicKey clientPublicKey, String value, int wts, byte[] signature,
                        byte[] sSSignature, PublicKey serverPublicKey){

        this.stub = stub;
        this.clientPublicKey = clientPublicKey;
        this.value = value;
        this.wts = wts;
        this.signature = signature;
        this.sSSignature = sSSignature;
        this.serverPublicKey = serverPublicKey;
    }

    @Override
    public void run() {

        try {
            stub.postGeneral(this.clientPublicKey, this.value, this.wts, this.signature, this.sSSignature, this.serverPublicKey);
        } catch (RemoteException e){
            e.printStackTrace();
        }
    }

}
