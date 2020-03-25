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
    private Scanner scanner;
    private String serverNumber;
    private Dictionary<PublicKey, ClientLibrary> clientList;
    private GeneralBoard generalBoard;

    public Server (){

        this.scanner = new Scanner(System.in);
        System.out.println("\nInsert the server number:");
        this.serverNumber = scanner.nextLine();

        try {

            serverPrivateKey = AsymmetricCrypto.getPrivateKey("data/keys/server" + serverNumber + "_private_key.der");
            serverPublicKey = AsymmetricCrypto.getPublicKey("data/keys/server" + serverNumber + "_public_key.der");

        } catch (Exception e) {

            e.printStackTrace();

        }
        this.clientList = new Hashtable<>();
        this.generalBoard = new GeneralBoard();

    }

    @Override
    public void register(PublicKey clientPublicKey, String clientNumber, byte [] signature) throws RemoteException {
        try{
            System.out.println("\n-------------------------------------------------------------\n" +
                    "client" + clientNumber + " called register() method.");

            if(AsymmetricCrypto.validateDigitalSignature(signature,clientPublicKey,clientNumber)){
                this.clientList.put(clientPublicKey, new ClientLibrary(clientNumber));
                System.out.println("\nRegistered client" + clientNumber + " with Public key: \n\n" + clientPublicKey);
            }else{
                throw new Exception("\nInvalid signature");
            }
        }catch (Exception e){
            //TO DO -> restrict exception catching
            e.printStackTrace();
            throw new RemoteException();
        }
    }

    @Override
    public void post(PublicKey clientPublicKey, String message, byte [] signature) throws RemoteException {

        try{
            System.out.println("\n-------------------------------------------------------------\n" +
                    "client" + clientList.get(clientPublicKey).getClientNumber() + " called post() method.");

        } catch (NullPointerException e) {
            System.out.println("\nClient is not registered!");
            throw new RemoteException("\nClient not registered!");
        }

        try{
            if(AsymmetricCrypto.validateDigitalSignature(signature,clientPublicKey,message)){
                this.clientList.get(clientPublicKey).addAnnouncement(message);
            }else{
                throw new Exception("\nInvalid signature.");
            }
        }catch (Exception e){
            //TO DO -> restrict exception catching
            e.printStackTrace();
            throw new RemoteException();
        }

    }

    @Override
    public void postGeneral(PublicKey clientPublicKey, String message, byte[] signature) throws RemoteException {

        try{
            System.out.println("\n-------------------------------------------------------------\n" +
                    "client" + clientList.get(clientPublicKey).getClientNumber() + " called postGeneral() method.");

        } catch (NullPointerException e) {
            System.out.println("\nClient is not registered!");
            throw new RemoteException("Client not registered!");
        }

        try{
            if(AsymmetricCrypto.validateDigitalSignature(signature,clientPublicKey,message)){
                this.generalBoard.addAnnouncement(clientList.get(clientPublicKey).getClientNumber(), message);
            }else{
                throw new Exception("\nInvalid signature.");
            }
        }catch (Exception e){
            //TO DO -> restrict exception catching
            e.printStackTrace();
            throw new RemoteException();
        }

    }

    @Override
    public String read(PublicKey clientPublicKey, int number) throws RemoteException {

        try{
            System.out.println("\n-------------------------------------------------------------\n" +
                    "A client called the read() method to read client" + clientList.get(clientPublicKey).getClientNumber() + "'s announcements.");

        } catch (NullPointerException e) {
            System.out.println("\nClient is not registered!");
            throw new RemoteException("\nClient not registered!");
        }

        return clientList.get(clientPublicKey).getAnnouncements(number);
    }

    // este metodo envia tambem a chave publica e imprime isso, portanto se aparecer, nao pensem que é um erro. Depois temos de arranjar uma solução melhor.
    @Override
    public String readGeneral(int number) throws RemoteException {

        System.out.println("\n-------------------------------------------------------------\n" +
                "A client called the readGeneral() method.");

        return generalBoard.getAnnouncements(number);

    }

}
