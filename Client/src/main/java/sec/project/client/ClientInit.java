package sec.project.client;

import sec.project.library.ClientAPI;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Hello world!
 *
 */
public class ClientInit {

    private static Registry registry;
    private static Map<Integer, ClientAPI> stubs;
    private static Client client;

    public static void main( String[] args ) {

        System.out.println( "\nInsert the ports of known servers separated by ',': " );
        String ports = System.console().readLine();
        String[] portsArray = ports.split(",");
        stubs = new HashMap<>();
        ClientInit clientInit = new ClientInit(portsArray);

        while(true){
        }

    }

    public ClientInit(String[] portsArray){

        try {

            for(int i = 0; i < portsArray.length; i++){
                this.registry = LocateRegistry.getRegistry(Integer.parseInt(portsArray[i]));
                this.stubs.put(Integer.parseInt(portsArray[i]), (ClientAPI) registry.lookup("localhost:" + portsArray[i] + "/ClientAPI"));
            }
            this.client = new Client(stubs);

            System.err.println( "\nClient ready." );
            client.execute();

        } catch (Exception e) {

            System.err.println("\nClient exception: " + e.toString());
            e.printStackTrace();

        }
    }
}
