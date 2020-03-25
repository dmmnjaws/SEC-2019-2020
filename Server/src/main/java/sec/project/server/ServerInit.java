package sec.project.server;


import sec.project.library.ClientAPI;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ServerInit {

    public static void main( String[] args ) {

        System.out.println( "\nHello World!" );
        ServerInit server = new ServerInit(7654);

        while(true){
        }

    }

    public ServerInit(int server_port){

        try {

            Server server = new Server();
            ClientAPI stub = (ClientAPI) UnicastRemoteObject.exportObject(server, server_port);
            Registry registry = LocateRegistry.createRegistry(server_port);
            registry.rebind("localhost:" + String.valueOf(server_port) + "/ClientAPI", stub);
            System.err.println( "\nServer ready." );

        } catch(Exception e) {

            System.err.println("\nServer exception: " + e.toString());
            e.printStackTrace();

        }
    }
}
