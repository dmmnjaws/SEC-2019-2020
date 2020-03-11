package sec.project.library;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientAPI extends Remote {

    void register() throws RemoteException;
    void post() throws RemoteException;
    void postGeneral() throws RemoteException;
    void read() throws RemoteException;
    void readGeneral() throws RemoteException;
}