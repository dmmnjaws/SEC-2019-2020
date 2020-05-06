package sec.project.server;


import com.fasterxml.jackson.databind.ObjectMapper;
import sec.project.library.ClientAPI;

import java.io.File;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ServerInit {

    private static Registry registry;
    private static ClientAPI stub;
    private static Server server;
    private static Map<String, ClientAPI> stubs;

    public static void main( String[] args ) {

        System.out.println( "\nInsert the ports of known servers separated by ',': " );
        String ports = System.console().readLine();
        String[] portsArray = ports.split(",");

        System.out.println( "\nInsert the port number for this server.");
        String serverPort = System.console().readLine();

        stubs = new HashMap<>();
        for (String serverPortString : portsArray){
            if (!serverPortString.equals(serverPort)){
                stubs.put(serverPort, null);
            }
        }

        ServerInit server = new ServerInit(Integer.parseInt(serverPort), stubs);

        while(true){
        }

    }

    public ServerInit(int server_port, Map<String, ClientAPI> stubs){

        try {
            this.server = new Server(server_port, stubs);
            this.stub = (ClientAPI) UnicastRemoteObject.exportObject(server, server_port);
            this.registry = LocateRegistry.createRegistry(server_port);
            registry.rebind("localhost:" + String.valueOf(server_port) + "/ClientAPI", stub);
            System.err.println( "\nServer ready." );

        } catch(Exception e) {

            System.err.println("\nServer exception: " + e.toString());
            e.printStackTrace();

        }
    }

}
