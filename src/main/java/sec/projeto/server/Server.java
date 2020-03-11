package sec.projeto.server;

import sec.projeto.library.ClientAPI;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server implements ClientAPI
{
    public void Server(){
        System.out.println( "Hello!" );

        try {
            Server obj = new Server();
            ClientAPI stub = (ClientAPI) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.bind("Server", stub);

            System.err.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }

    @Override
    public String hello() {
        return "Hello!";
    }

    @Override
    public void register() {

    }

    @Override
    public void post() {

    }

    @Override
    public void postGeneral() {

    }

    @Override
    public void read() {

    }

    @Override
    public void readGeneral() {

    }
}
