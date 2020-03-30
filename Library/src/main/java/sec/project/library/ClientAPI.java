package sec.project.library;

import jdk.internal.net.http.common.Pair;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PublicKey;

public interface ClientAPI extends Remote {

    void register(PublicKey clientPublicKey, String clientId, byte [] signature) throws RemoteException;
    void post(PublicKey clientPublicKey, String message, int seqNumber, byte [] signature) throws RemoteException;
    void postGeneral(PublicKey clientPublicKey, String message, int seqNumber, byte[] signature) throws RemoteException;
    Acknowledge read(PublicKey clientPublicKey, int number) throws RemoteException;
    Acknowledge readGeneral(int number) throws RemoteException;
}