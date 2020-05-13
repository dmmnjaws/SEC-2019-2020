package sec.project.server;

import org.javatuples.Quartet;
import org.javatuples.Quintet;
import org.javatuples.Triplet;
import sec.project.library.Acknowledge;
import sec.project.library.AsymmetricCrypto;
import sec.project.library.ClientAPI;
import sec.project.library.ReadView;
import java.io.*;
import java.rmi.RemoteException;
import java.security.*;
import java.util.*;

public class Server implements ClientAPI {

    private static Map<PublicKey, ClientAPI> stubs;
    private int serverPort;
    private KeyStore serverKeyStore;
    private PrivateKey serverPrivateKey;
    private PublicKey serverPublicKey;
    private Map<PublicKey, ClientLibrary> clientList;
    private GeneralBoard generalBoard;
    private boolean isBeingSaved;

    public Server (int serverPort){

        this.serverPort = serverPort;
        this.isBeingSaved = false;
        this.stubs = new HashMap<>();

        try {

            loadState();

            this.serverKeyStore = AsymmetricCrypto.getKeyStore("data/keys/server" + serverPort + "_keystore.jks", "server" + serverPort + "password");
            this.serverPrivateKey = AsymmetricCrypto.getPrivateKey(this.serverKeyStore, "server" + serverPort + "password", "server" + serverPort);
            this.serverPublicKey = AsymmetricCrypto.getPublicKeyFromCert("data/keys/server" + serverPort + "_certificate.crt");

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    public synchronized void saveState() {

        try{

            this.isBeingSaved = true;

            State state = new State(this.clientList, this.generalBoard);

            FileOutputStream f = new FileOutputStream(new File("data/state" + this.serverPort + ".txt"));
            ObjectOutputStream o = new ObjectOutputStream(f);
            o.writeObject(state);

            o.close();
            f.close();

            FileOutputStream fBackup = new FileOutputStream(new File("data/state" + this.serverPort + "_backup.txt"));
            ObjectOutputStream oBackup = new ObjectOutputStream(fBackup);
            oBackup.writeObject(state);

            oBackup.close();
            fBackup.close();

            this.isBeingSaved = false;

        } catch (IOException e){
            System.out.println("WARNING: There was an error while saving the server's state.");
        }


    }

    public void loadState() throws ClassNotFoundException {

        File stateFile = new File("data/state" + this.serverPort + ".txt");
        File stateFileBackup = new File("data/state" + this.serverPort + "_backup.txt");

        try{

            FileInputStream file = new FileInputStream(stateFile);
            ObjectInputStream objStream = new ObjectInputStream(file);

            State state = (State) objStream.readObject();

            objStream.close();
            file.close();

            this.clientList = state.getClientList();
            this.generalBoard = state.getGeneralBoard();
            return;

        } catch (IOException e1){

            try {
                FileInputStream file = new FileInputStream(stateFileBackup);
                ObjectInputStream objStream = new ObjectInputStream(file);

                State state = (State) objStream.readObject();

                objStream.close();
                file.close();

                this.clientList = state.getClientList();
                this.generalBoard = state.getGeneralBoard();
                return;

            } catch (IOException e2){

                this.clientList = new Hashtable<>();
                this.generalBoard = new GeneralBoard();

            }

        }

    }

    public void setStubs(Map<PublicKey, ClientAPI> stubs) {
        this.stubs = stubs;
    }

    @Override
    public void register(PublicKey clientPublicKey, String clientNumber, byte [] signature) throws RemoteException {

        try{
            System.out.println("\n-------------------------------------------------------------\n" +
                    "client" + clientNumber + " called register() method.");

            if(AsymmetricCrypto.validateDigitalSignature(signature, clientPublicKey, clientNumber)
                    & this.clientList.get(clientPublicKey)==null) {

                this.clientList.put(clientPublicKey, new ClientLibrary(clientNumber, clientPublicKey, this.serverPublicKey, this.serverPrivateKey));
                System.out.println("\nRegistered client" + clientNumber + " with Public key: \n\n" + clientPublicKey);

            }else{

                throw new Exception("\nInvalid registration attempt");

            }

            saveState();

        } catch (Exception e){
            throw new RemoteException("\nThe server registered in port " + this.serverPort + " reports that you are already registered.");
        }
    }

    @Override
    public Acknowledge post(PublicKey clientPublicKey, String message, int wts, byte [] signature, boolean isWriteBack) throws RemoteException {

        try {
            try {
                System.out.println("\n-------------------------------------------------------------\n" +
                        "client" + clientList.get(clientPublicKey).getClientNumber() + " called post() method.");
            } catch (NullPointerException e) {
                throw new RemoteException("\nThe server registered in port " + this.serverPort + " reports that you are not registered yet. \nIf you're unsure if this is right, please type the 'register' command.");
            }

            String ack = this.clientList.get(clientPublicKey).write(wts, message, signature, this.stubs);

            if (ack.equals("BADSIGNATURE") || ack.equals("BADBROADCAST")){
                throw new RemoteException("\nSomething went wrong in the server registered in port " + this.serverPort + "... (" + ack + ")");
            }

            if (ack.equals("IGNORED") && !isWriteBack){
                throw new RemoteException("\nThe server registered in port " + this.serverPort + " reports that you are registered but not logged in. \nIf you're unsure if this is right, please type the 'login' command.");
            }

            saveState();
            return new Acknowledge(wts, ack, AsymmetricCrypto.wrapDigitalSignature(ack + wts, this.serverPrivateKey));

        } catch (RemoteException e) {
            throw new RemoteException(e.getMessage());

        } catch (NullPointerException e) {
            e.printStackTrace();
            System.out.println("\nInvalid Request!");
            throw new RemoteException("\nInvalid Request!");

        } catch (Exception e){
            //TO DO -> restrict exception catching
            e.printStackTrace();
            throw new RemoteException();
        }

    }

    @Override
    public Acknowledge postGeneral(PublicKey clientPublicKey, String message, int wts, byte[] signature,
                                   byte[] senderServerSignature, PublicKey senderServerPublicKey) throws RemoteException {

        try{
            try {
                System.out.println("\n-------------------------------------------------------------\n" +
                        "client" + clientList.get(clientPublicKey).getClientNumber() + " called postGeneral() method.");
            } catch (NullPointerException e) {
                throw new RemoteException("\nThe server registered in port " + this.serverPort + " reports that you are not registered yet. \nIf you're unsure if this is right, please type the 'register' command.");
            }


            String ack = this.generalBoard.write(wts, message, clientList.get(clientPublicKey).getClientNumber(), signature, clientPublicKey,
                    senderServerSignature, senderServerPublicKey, this.serverPrivateKey, this.serverPublicKey, this.stubs);

            saveState();
            return new Acknowledge(wts, ack, AsymmetricCrypto.wrapDigitalSignature(ack + wts, this.serverPrivateKey));

        } catch (RemoteException e) {
            throw new RemoteException(e.getMessage());

        } catch (NullPointerException e) {
            e.printStackTrace();
            System.out.println("\nInvalid Request!");
            throw new RemoteException("\nInvalid Request!");

        } catch (Exception e){
            //TO DO -> restrict exception catching
            e.printStackTrace();
            throw new RemoteException();
        }

    }

    @Override
    public ReadView read(PublicKey toReadClientPublicKey, int number, int rid , byte[] signature, PublicKey clientPublicKey) throws RemoteException {

        try {
            try {
                clientList.get(clientPublicKey).getClientNumber();
            } catch (NullPointerException e) {
                throw new RemoteException("\nThe server registered in port " + this.serverPort + " reports that you are not registered yet. \nIf you're unsure if this is right, please type the 'register' command.");
            }

            try {
                System.out.println("\n-------------------------------------------------------------\n" +
                        "client called the read() method to read client" + clientList.get(toReadClientPublicKey).getClientNumber() + "'s announcements.");
            } catch (NullPointerException e) {
                throw new RemoteException("\nThe server registered in port " + this.serverPort + " reports the client you indicated is not registered.");
            }


            ArrayList<Quartet<Integer, String, byte[], ArrayList<Integer>>> triplets = this.clientList.get(toReadClientPublicKey).read(number, rid, signature, clientPublicKey);
            return new ReadView(triplets, rid, AsymmetricCrypto.wrapDigitalSignature(AsymmetricCrypto.transformQuartetToString(triplets) + rid, this.serverPrivateKey));

        } catch (RemoteException e) {
            throw new RemoteException(e.getMessage());

        } catch (NullPointerException e) {
            e.printStackTrace();
            System.out.println("\nInvalid Request!");
            throw new RemoteException("\nInvalid Request!");

        } catch (Exception e){
            e.printStackTrace();
            throw new RemoteException("\nDecryption error");
        }

    }

    @Override
    public ReadView readGeneral(int number, int rid, byte[] signature, PublicKey clientPublicKey) throws RemoteException {

        try {
            try {
                clientList.get(clientPublicKey).getClientNumber();
            } catch (NullPointerException e) {
                throw new RemoteException("\nThe server registered in port " + this.serverPort + " reports that you are not registered yet. \nIf you're unsure if this is right, please type the 'register' command.");
            }

            System.out.println("\n-------------------------------------------------------------\n" +
                "A client called the readGeneral() method.");

            ArrayList<Quintet<Integer, String, String, byte[], ArrayList<Integer>>> quartets = this.generalBoard.read(number, rid, signature, clientPublicKey);
            return new ReadView(rid, AsymmetricCrypto.wrapDigitalSignature(AsymmetricCrypto.transformQuintetToString(quartets) + rid, this.serverPrivateKey), quartets);

        } catch (RemoteException e) {
            throw new RemoteException(e.getMessage());

        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("\nDecryption error");
        }
    }

    @Override
    public Acknowledge login(PublicKey clientPublicKey) throws RemoteException {

        try {
            String message = "" + this.clientList.get(clientPublicKey).getOneNAtomicRegister().getWts() + "|" + this.generalBoard.getnNRegularRegister().getWts();
            System.out.println("DEBUG: " + message);
            return new Acknowledge(message, AsymmetricCrypto.wrapDigitalSignature(message, this.serverPrivateKey));

        } catch (NullPointerException e) {
            throw new RemoteException("\nThe server registered in port " + this.serverPort + " reports that you are not registered yet. \nIf you're unsure if this is right, please type the 'register' command.");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("\nLogin error");
        }

    }

    @Override
    public void echo(PublicKey clientPublicKey, Triplet<Integer, String, byte[]> message, byte[] signature, PublicKey serverPublicKey) throws RemoteException {
        try{
            this.clientList.get(clientPublicKey).getDoubleEchoBroadcaster().echo(clientPublicKey, message, signature, serverPublicKey);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void ready(PublicKey clientPublicKey, Triplet<Integer, String, byte[]> message, byte[] signature, PublicKey serverPublicKey) throws RemoteException {
        try{
            this.clientList.get(clientPublicKey).getDoubleEchoBroadcaster().ready(clientPublicKey, message, signature, serverPublicKey);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void addCommitRequest(PublicKey clientPublicKey, Quartet<Integer, String, String, byte[]> valueQuartet, byte[] sSSignature, PublicKey serverPublicKey) throws RemoteException{
        try{
            this.generalBoard.getnNRegularRegister().addCommitRequest(clientPublicKey, valueQuartet, sSSignature, serverPublicKey);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    protected boolean isBeingSaved(){ return this.isBeingSaved; }

}
