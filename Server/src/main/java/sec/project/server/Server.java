package sec.project.server;


import jdk.internal.net.http.common.Pair;
import sec.project.library.Acknowledge;
import sec.project.library.AsymmetricCrypto;
import sec.project.library.ClientAPI;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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

        try {
            System.out.println("\n-------------------------------------------------------------\n" +
                    "client" + clientList.get(clientPublicKey).getClientNumber() + " called post() method.");

            if (AsymmetricCrypto.validateDigitalSignature(signature, clientPublicKey, message)) {
                this.clientList.get(clientPublicKey).addAnnouncement(message);
            } else {
                throw new Exception("\nInvalid signature.");
            }

        } catch (NullPointerException e) {
            System.out.println("\nClient is not registered!");
            throw new RemoteException("\nClient not registered!");

        } catch (Exception e){
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

            if(AsymmetricCrypto.validateDigitalSignature(signature,clientPublicKey,message)){
                this.generalBoard.addAnnouncement(clientList.get(clientPublicKey).getClientNumber(), message);
            }else{
                throw new Exception("\nInvalid signature.");
            }

        } catch (NullPointerException e) {
            System.out.println("\nClient is not registered!");
            throw new RemoteException("Client not registered!");

        } catch (Exception e){
            //TO DO -> restrict exception catching
            e.printStackTrace();
            throw new RemoteException();
        }

    }

    @Override
    public Acknowledge read(PublicKey clientPublicKey, int number) throws RemoteException {

        try{
            System.out.println("\n-------------------------------------------------------------\n" +
                    "A client called the read() method to read client" + clientList.get(clientPublicKey).getClientNumber() + "'s announcements.");
            String msg = clientList.get(clientPublicKey).getAnnouncements(number);
            return new Acknowledge(msg,AsymmetricCrypto.wrapDigitalSignature(msg,this.serverPrivateKey));

        } catch (NullPointerException e) {
            System.out.println("\nClient is not registered!");
            throw new RemoteException("\nClient not registered!");
        }catch (Exception e){
            e.printStackTrace();
            throw new RemoteException("\nDecryption error");
        }

    }

    @Override
    public Acknowledge readGeneral(int number) throws RemoteException {

        System.out.println("\n-------------------------------------------------------------\n" +
                "A client called the readGeneral() method.");

        String message = generalBoard.getAnnouncements(number);
        try {
            return new Acknowledge(message,AsymmetricCrypto.wrapDigitalSignature(message,this.serverPrivateKey));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("\nDecryption error");
        }
    }

}
