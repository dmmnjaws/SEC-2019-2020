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

        System.err.println( "Client called register() method." );

    }

    @Override
    public void post() throws RemoteException {

        System.err.println( "Client called post() method." );

    }

    @Override
    public void postGeneral() throws RemoteException {

        System.err.println( "Client called postGeneral() method." );

    }

    @Override
    public void read() throws RemoteException {

        System.err.println( "Client called read() method." );

    }

    @Override
    public void readGeneral() throws RemoteException {

        System.err.println( "Client called readGeneral() method." );

    }
}
