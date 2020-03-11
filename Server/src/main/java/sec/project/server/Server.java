package sec.project.server;

import sec.project.library.ClientAPI;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Hello world!
 *
 */
public class Server implements ClientAPI
{

    @Override
    public void register() throws RemoteException {

    }

    @Override
    public void post() throws RemoteException {

    }

    @Override
    public void postGeneral() throws RemoteException {

    }

    @Override
    public void read() throws RemoteException {

    }

    @Override
    public void readGeneral() throws RemoteException {

    }
}
