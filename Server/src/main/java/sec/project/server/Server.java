package sec.project.server;

import sec.project.library.Acknowledge;
import sec.project.library.AsymmetricCrypto;
import sec.project.library.ClientAPI;
import java.io.*;
import java.rmi.RemoteException;
import java.security.*;
import java.util.*;

public class Server implements ClientAPI {

    private KeyStore serverKeyStore;
    private PrivateKey serverPrivateKey;
    private PublicKey serverPublicKey;
    private String serverNumber;
    private Map<PublicKey, ClientLibrary> clientList;
    private GeneralBoard generalBoard;

    public Server (String serverNumber){

        this.serverNumber = serverNumber;
        File stateFile = new File("data/state.txt");

        try {

            if(!(stateFile.exists())){
                this.clientList = new Hashtable<>();
                this.generalBoard = new GeneralBoard();
            }else{
                FileInputStream file = new FileInputStream(stateFile);
                ObjectInputStream objStream = new ObjectInputStream(file);

                State state = (State) objStream.readObject();

                objStream.close();
                file.close();

                this.clientList = state.getClientList();
                this.generalBoard = state.getGeneralBoard();
            }

            this.serverKeyStore = AsymmetricCrypto.getKeyStore("data/keys/server" + this.serverNumber + "_keystore.jks", "server" + this.serverNumber + "password");
            this.serverPrivateKey = AsymmetricCrypto.getPrivateKey(this.serverKeyStore, "server" + this.serverNumber + "password", "server" + this.serverNumber);
            this.serverPublicKey = AsymmetricCrypto.getPublicKeyFromCert("data/keys/server" + this.serverNumber + "_certificate.crt");

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    public void saveState() throws IOException {

        State state = new State(this.clientList, this.generalBoard);
        FileOutputStream f = new FileOutputStream(new File("data/state.txt"));
        ObjectOutputStream o = new ObjectOutputStream(f);

        o.writeObject(state);

        o.close();
        f.close();
    }

    @Override
    public void register(PublicKey clientPublicKey, String clientNumber, byte [] signature) throws RemoteException {
        try{
            System.out.println("\n-------------------------------------------------------------\n" +
                    "client" + clientNumber + " called register() method.");

            if(AsymmetricCrypto.validateDigitalSignature(signature, clientPublicKey, clientNumber)
                    & this.clientList.get(clientPublicKey)==null) {

                this.clientList.put(clientPublicKey, new ClientLibrary(clientNumber));
                System.out.println("\nRegistered client" + clientNumber + " with Public key: \n\n" + clientPublicKey);

            }else{

                throw new Exception("\nInvalid registration attempt");

            }

            saveState();

        } catch (Exception e){
            //TO DO -> restrict exception catching
            e.printStackTrace();
            throw new RemoteException();
        }
    }

    @Override
    public void post(PublicKey clientPublicKey, String message, int seqNumber, byte [] signature) throws RemoteException {

        try {
            System.out.println("\n-------------------------------------------------------------\n" +
                    "client" + clientList.get(clientPublicKey).getClientNumber() + " called post() method.");

            if (AsymmetricCrypto.validateDigitalSignature(signature, clientPublicKey, message + seqNumber)
                    & clientList.get(clientPublicKey).getSeqNumber() == seqNumber) {

                this.clientList.get(clientPublicKey).addAnnouncement(message);
                this.clientList.get(clientPublicKey).incrementSeqNumber();
                System.out.println(this.clientList.get(clientPublicKey).getSeqNumber());

            } else {

                throw new Exception("\nInvalid signature.");

            }

            saveState();

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
    public void postGeneral(PublicKey clientPublicKey, String message, int seqNumber, byte[] signature) throws RemoteException {

        try{
            System.out.println("\n-------------------------------------------------------------\n" +
                    "client" + clientList.get(clientPublicKey).getClientNumber() + " called postGeneral() method.");

            if(AsymmetricCrypto.validateDigitalSignature(signature, clientPublicKey, message + seqNumber)
                    & clientList.get(clientPublicKey).getSeqNumber() == seqNumber){

                this.generalBoard.addAnnouncement(clientList.get(clientPublicKey).getClientNumber(), message);
                this.clientList.get(clientPublicKey).incrementSeqNumber();
                System.out.println(this.clientList.get(clientPublicKey).getSeqNumber());

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
    public Acknowledge read(PublicKey toReadClientPublicKey, int number, int seqNumber, byte[] signature, PublicKey clientPublicKey) throws RemoteException {

        try{
            System.out.println("\n-------------------------------------------------------------\n" +
                    "A client called the read() method to read client" + clientList.get(toReadClientPublicKey).getClientNumber() + "'s announcements.");

            if(AsymmetricCrypto.validateDigitalSignature(signature, clientPublicKey, toReadClientPublicKey.toString() + number + seqNumber)
                    & clientList.get(clientPublicKey).getSeqNumber() == seqNumber){

                String message = clientList.get(toReadClientPublicKey).getAnnouncements(number);
                this.clientList.get(clientPublicKey).incrementSeqNumber();
                System.out.println(this.clientList.get(clientPublicKey).getSeqNumber());
                return new Acknowledge(message, AsymmetricCrypto.wrapDigitalSignature(message, this.serverPrivateKey));

            }else{

                throw new Exception("\nInvalid signature.");

            }


        } catch (NullPointerException e) {
            System.out.println("\nClient is not registered!");
            throw new RemoteException("\nClient not registered!");
        } catch (Exception e){
            e.printStackTrace();
            throw new RemoteException("\nDecryption error");
        }

    }

    @Override
    public Acknowledge readGeneral(int number, int seqNumber, byte[] signature, PublicKey clientPublicKey) throws RemoteException {

        try {
            System.out.println("\n-------------------------------------------------------------\n" +
                "A client called the readGeneral() method.");

            if(AsymmetricCrypto.validateDigitalSignature(signature, clientPublicKey,"" + number + seqNumber)
                    & clientList.get(clientPublicKey).getSeqNumber() == seqNumber){

                String message = generalBoard.getAnnouncements(number);
                this.clientList.get(clientPublicKey).incrementSeqNumber();
                System.out.println(this.clientList.get(clientPublicKey).getSeqNumber());
                return new Acknowledge(message, AsymmetricCrypto.wrapDigitalSignature(message, this.serverPrivateKey));

            }else{

                throw new Exception("\nInvalid signature.");

            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("\nDecryption error");
        }
    }

}
