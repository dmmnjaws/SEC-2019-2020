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
    private Scanner scanner;

    public static void main( String[] args ) {

        System.out.println( "\nHello World!" );

        ServerInit server = new ServerInit(7654);

        while(true){
        }

    }

    public ServerInit(int server_port){

        try {
            this.scanner = new Scanner(System.in);
            System.out.println("\nInsert the server number:");
            String serverNumber = scanner.nextLine();

            this.server = new Server(serverNumber);
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
