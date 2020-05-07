package sec.project.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import sec.project.library.AsymmetricCrypto;
import sec.project.library.ClientAPI;

import java.io.File;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ServerInit {

    private static Registry registry;
    private static ClientAPI stub;
    private static Server server;

    public static void main( String[] args ) {
        try {
            System.out.println("\nInsert the ports of known servers separated by ',': ");
            String ports = System.console().readLine();
            String[] portsArray = ports.split(",");

            System.out.println("\nInsert the port number for this server.");
            String serverPort = System.console().readLine();



            ServerInit server = new ServerInit(Integer.parseInt(serverPort), portsArray);

            while (true) {}

        } catch (Exception e){
            e.printStackTrace();
        }

    }

    public ServerInit(int server_port, String[] portsArray){

        try {
            this.server = new Server(server_port);
            this.stub = (ClientAPI) UnicastRemoteObject.exportObject(server, server_port);
            this.registry = LocateRegistry.createRegistry(server_port);
            registry.rebind("localhost:" + String.valueOf(server_port) + "/ClientAPI", stub);
            System.err.println( "\nServer setting up. When all servers are at this stage press enter");
            System.console().readLine();
            Map<PublicKey, ClientAPI> stubs = new HashMap<>();
            for (String serverPortString : portsArray) {
                if (!serverPortString.equals(server_port)) {
                    Registry registryAux = LocateRegistry.getRegistry(Integer.parseInt(serverPortString));
                    stubs.put(AsymmetricCrypto.getPublicKeyFromCert("data/keys/server" + serverPortString + "_certificate.crt"),
                            (ClientAPI) registryAux.lookup("localhost:" + serverPortString + "/ClientAPI"));
                }
            }

            this.server.setStubs(stubs);
            System.out.println("Server ready");

        } catch(Exception e) {

            System.err.println("\nServer exception: " + e.toString());
            e.printStackTrace();

        }
    }

}
