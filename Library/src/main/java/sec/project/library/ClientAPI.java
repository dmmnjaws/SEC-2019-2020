package sec.project.library;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PublicKey;

public interface ClientAPI extends Remote {

    void register(PublicKey clientPublicKey, String clientId, byte [] signature) throws RemoteException;
    void post(PublicKey clientPublicKey, String message, int seqNumber, byte [] signature) throws RemoteException;
    void postGeneral(PublicKey clientPublicKey, String message, int seqNumber, byte[] signature) throws RemoteException;
    Acknowledge read(PublicKey toReadClientPublicKey, int number, int seqNumber, byte[] signature, PublicKey clientPublicKey) throws RemoteException;
    Acknowledge readGeneral(int number, int seqNumber, byte[] signature, PublicKey clientPublicKey) throws RemoteException;
    Acknowledge login(PublicKey receiverPublicKey) throws RemoteException;
}