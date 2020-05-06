package sec.project.library;

import org.javatuples.Quartet;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PublicKey;
import java.util.ArrayList;

public interface ClientAPI extends Remote {

    void register(PublicKey clientPublicKey, String clientId, byte [] signature) throws RemoteException;
    Acknowledge post(PublicKey clientPublicKey, String message, int wts, byte [] signature) throws RemoteException;
    Acknowledge postGeneral(PublicKey clientPublicKey, String message, int wts, byte[] signature) throws RemoteException;
    ReadView read(PublicKey toReadClientPublicKey, int number, int rid , byte[] signature, PublicKey clientPublicKey) throws RemoteException;
    ReadView readGeneral(int number, int rid, byte[] signature, PublicKey clientPublicKey) throws RemoteException;
    Acknowledge login(PublicKey receiverPublicKey) throws RemoteException;
}