package sec.project.client;

import sec.project.library.ClientAPI;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.*;

/**
 * Hello world!
 *
 */
public class ClientInit {

    public static void main( String[] args )
    {

        ClientInit clientInit = new ClientInit(7654);
        System.out.println( "Hello World!" );

        while(true){
        }

    }

    public ClientInit(int server_port){

        try {

            Registry registry = LocateRegistry.getRegistry(server_port);
            ClientAPI stub = (ClientAPI) registry.lookup("localhost:" + String.valueOf(server_port) + "/ClientAPI");
            Client client = new Client(stub);
            client.execute();
            System.err.println( "Client ready." );

        } catch (Exception e) {

            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();

        }
    }
}
