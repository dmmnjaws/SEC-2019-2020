package sec.projeto.client;

import sec.projeto.library.ClientAPI;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {

    public Client() {
        try {
            Registry registry = LocateRegistry.getRegistry(null);
            ClientAPI stub = (ClientAPI) registry.lookup("Server");
            String response = stub.hello();
            System.out.println("response: " + response);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }

}
