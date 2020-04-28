package sec.project.server;


import com.fasterxml.jackson.databind.ObjectMapper;
import sec.project.library.ClientAPI;

import java.io.File;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class ServerInit {

    private static Registry registry;
    private static ClientAPI stub;
    private static Server server;

    public static void main( String[] args ) {

        System.out.println( "\nInsert the number of servers:" );
        int nServers = Integer.parseInt(System.console().readLine());
        System.out.println( "\nInsert the port number for this server.");
        int serverPort = Integer.parseInt(System.console().readLine());

        ServerInit server = new ServerInit(serverPort, nServers);

        while(true){
        }

    }

    public ServerInit(int server_port, int nServers){

        try {
            this.server = new Server(server_port, nServers);
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
