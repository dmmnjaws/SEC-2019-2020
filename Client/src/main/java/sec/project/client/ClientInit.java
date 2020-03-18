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

        while(true){
        }

    }

    public ClientInit(int server_port){

        System.out.println( "Hello World!" );

        try {

            Registry registry = LocateRegistry.getRegistry(server_port);
            ClientAPI stub = (ClientAPI) registry.lookup("localhost:" + String.valueOf(server_port) + "/ClientAPI");
            Client client = new Client(stub);
            System.err.println( "Client ready." );
            client.execute();

        } catch (Exception e) {

            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();

        }
    }
}
