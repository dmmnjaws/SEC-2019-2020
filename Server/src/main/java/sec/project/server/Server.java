package sec.project.server;

import com.sun.tools.javac.util.Pair;
import sec.project.library.AsymmetricCrypto;
import sec.project.library.ClientAPI;

import java.rmi.RemoteException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;

/**
 * Hello world!
 *
 */
public class Server implements ClientAPI {

    private PrivateKey serverPrivateKey;
    private PublicKey serverPublicKey;
    Scanner scanner;
    String serverNumber;

    // PublicKey - identifies the client
    // ArrayList<String> - his announcement board;
    Dictionary<PublicKey, ArrayList<String>> clientBoards;
    ArrayList<Pair<PublicKey, String>> generalBoard;

    public Server (){

        this.scanner = new Scanner(System.in);
        System.out.println("Insert the server number:");
        this.serverNumber = scanner.nextLine();

        try {

            serverPrivateKey = AsymmetricCrypto.getPrivateKey("data/keys/server" + serverNumber + "_private_key.der");
            System.out.println(serverPrivateKey.toString());
            serverPublicKey = AsymmetricCrypto.getPublicKey("data/keys/server" + serverNumber + "_public_key.der");
            System.out.println(serverPublicKey.toString());

        } catch (Exception e) {

            e.printStackTrace();

        }

        this.clientBoards = new Hashtable<>();
        this.generalBoard = new ArrayList<>();

    }

    @Override
    public void register(PublicKey clientPublicKey) throws RemoteException {

        System.err.println( "Client called register() method." );
        this.clientBoards.put(clientPublicKey, new ArrayList<String>());

    }

    @Override
    public void post(PublicKey clientPublicKey, String message) throws RemoteException {

        System.err.println( "Client called post() method." );
        this.clientBoards.get(clientPublicKey).add(message);

    }

    @Override
    public void postGeneral(PublicKey clientPublicKey, String message) throws RemoteException {

        System.err.println( "Client called postGeneral() method." );
        this.generalBoard.add(new Pair<>(clientPublicKey, message));

    }

    @Override
    public String read(PublicKey clientPublicKey, int number) throws RemoteException {

        System.err.println( "Client called read() method." );

        ArrayList<String> clientBoard = this.clientBoards.get(clientPublicKey);

        if (number >= clientBoard.size() || number == 0){
            return clientBoard.toString();

        } else {
            return clientBoard.subList(clientBoard.size() - number,clientBoard.size()).toString();

        }
    }

    // este metodo envia tambem a chave publica e imprime isso, portanto se aparecer, nao pensem que é um erro. Depois temos de arranjar uma solução melhor.
    @Override
    public String readGeneral(int number) throws RemoteException {

        System.err.println( "Client called readGeneral() method." );

        if (number >= generalBoard.size() || number == 0){
            return generalBoard.toString();

        } else {
            return generalBoard.subList(generalBoard.size() - number,generalBoard.size()).toString();

        }

    }

}
