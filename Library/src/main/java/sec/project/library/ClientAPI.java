package sec.project.library;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PublicKey;

public interface ClientAPI extends Remote {

    void register(PublicKey clientPublicKey) throws RemoteException;
    void post(PublicKey clientPublicKey, String message) throws RemoteException;
    void postGeneral(PublicKey clientPublicKey, String message) throws RemoteException;
    void read(PublicKey clientPublicKey, int number) throws RemoteException;
    void readGeneral(int number) throws RemoteException;
}