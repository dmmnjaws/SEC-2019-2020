package sec.project.library;

import org.javatuples.Quartet;
import org.javatuples.Triplet;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

public interface ClientAPI extends Remote {

    void register(PublicKey clientPublicKey, String clientId, byte [] signature) throws RemoteException;
    Acknowledge post(PublicKey clientPublicKey, String message, int wts, byte [] signature, boolean isWriteBack) throws RemoteException;
    Acknowledge postGeneral(PublicKey clientPublicKey, String message, int wts, byte[] signature, byte[] serverSignature, PublicKey serverPublicKey) throws RemoteException;
    ReadView read(PublicKey toReadClientPublicKey, int number, int rid , byte[] signature, PublicKey clientPublicKey) throws RemoteException;
    ReadView readGeneral(int number, int rid, byte[] signature, PublicKey clientPublicKey) throws RemoteException;
    Acknowledge login(PublicKey receiverPublicKey) throws RemoteException;
    void echo(PublicKey clientPublicKey, Triplet<Integer, String, byte[]> message, byte[] signature, PublicKey serverPublicKey) throws RemoteException;
    void ready(PublicKey clientPublicKey, Triplet<Integer, String, byte[]> message, byte[] signature, PublicKey serverPublicKey) throws RemoteException;
    void addCommitRequest(PublicKey clientPublicKey, Quartet<Integer, String, String , byte[]> valueQuartet, byte[] sSSignature, PublicKey serverPublicKey) throws RemoteException;
}