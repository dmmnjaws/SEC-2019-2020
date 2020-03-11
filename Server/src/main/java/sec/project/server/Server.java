package sec.project.server;

import sec.project.library.ClientAPI;

import java.rmi.RemoteException;

/**
 * Hello world!
 *
 */
public class Server implements ClientAPI
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
    }

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
