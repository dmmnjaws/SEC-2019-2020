package sec.project.library;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PublicKey;

public interface ClientAPI extends Remote {

    void register(PublicKey clientPublicKey, String clientId, byte [] signature) throws RemoteException;
    void post(PublicKey clientPublicKey, String message, byte [] signature) throws RemoteException;
    void postGeneral(PublicKey clientPublicKey, String message, byte[] signature) throws RemoteException;
    String read(PublicKey clientPublicKey, int number) throws RemoteException;
    String readGeneral(int number) throws RemoteException;
}