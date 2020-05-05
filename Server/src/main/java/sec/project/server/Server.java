package sec.project.server;

import org.javatuples.Quartet;
import org.javatuples.Quintet;
import org.javatuples.Triplet;
import sec.project.library.Acknowledge;
import sec.project.library.AsymmetricCrypto;
import sec.project.library.ClientAPI;
import sec.project.library.ReadView;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.rmi.RemoteException;
import java.security.*;
import java.util.*;

public class Server implements ClientAPI {

    private int nServers;
    private int serverPort;
    private KeyStore serverKeyStore;
    private PrivateKey serverPrivateKey;
    private PublicKey serverPublicKey;
    private Map<PublicKey, ClientLibrary> clientList;
    private GeneralBoard generalBoard;

    public Server (int serverPort, int nServers){

        this.nServers = nServers;
        this.serverPort = serverPort;

        try {

            loadState();

            this.serverKeyStore = AsymmetricCrypto.getKeyStore("data/keys/server" + serverPort + "_keystore.jks", "server" + serverPort + "password");
            this.serverPrivateKey = AsymmetricCrypto.getPrivateKey(this.serverKeyStore, "server" + serverPort + "password", "server" + serverPort);
            this.serverPublicKey = AsymmetricCrypto.getPublicKeyFromCert("data/keys/server" + serverPort + "_certificate.crt");

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    public void saveState() throws IOException {

        State state = new State(this.clientList, this.generalBoard);
        FileOutputStream f = new FileOutputStream(new File("data/state" + this.serverPort + ".txt"));
        ObjectOutputStream o = new ObjectOutputStream(f);

        o.writeObject(state);

        o.close();
        f.close();
    }

    public void loadState() throws IOException, ClassNotFoundException {

        File stateFile = new File("data/state" + this.serverPort + ".txt");

        if (!(stateFile.exists())) {

            this.clientList = new Hashtable<>();
            this.generalBoard = new GeneralBoard();

        } else {

            FileInputStream file = new FileInputStream(stateFile);
            ObjectInputStream objStream = new ObjectInputStream(file);

            State state = (State) objStream.readObject();

            objStream.close();
            file.close();

            this.clientList = state.getClientList();
            this.generalBoard = state.getGeneralBoard();

        }

    }

    @Override
    public void register(PublicKey clientPublicKey, String clientNumber, byte [] signature) throws RemoteException {

        try{
            System.out.println("\n-------------------------------------------------------------\n" +
                    "client" + clientNumber + " called register() method.");

            if(AsymmetricCrypto.validateDigitalSignature(signature, clientPublicKey, clientNumber)
                    & this.clientList.get(clientPublicKey)==null) {

                this.clientList.put(clientPublicKey, new ClientLibrary(clientNumber, clientPublicKey));
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
    public Acknowledge post(PublicKey clientPublicKey, String message, int wts, byte [] signature) throws RemoteException {

        try {
            System.out.println("\n-------------------------------------------------------------\n" +
                    "client" + clientList.get(clientPublicKey).getClientNumber() + " called post() method.");

            String ack = this.clientList.get(clientPublicKey).write(wts, message, signature);
            saveState();
            return new Acknowledge(wts, ack, AsymmetricCrypto.wrapDigitalSignature(ack + wts, this.serverPrivateKey));

            //if (AsymmetricCrypto.validateDigitalSignature(signature, clientPublicKey, message + seqNumber)
            //        & clientList.get(clientPublicKey).getSeqNumber() == seqNumber) {

            //    System.out.println("\nDEBUG: seqNumber in client:\n" + (seqNumber));
            //    System.out.println("\nDEBUG: seqNumber in server:\n" + (clientList.get(clientPublicKey).getSeqNumber()));
            //    this.clientList.get(clientPublicKey).addAnnouncement(message);
            //    this.clientList.get(clientPublicKey).incrementSeqNumber();
            //    saveState();
            //    System.out.println("\nDEBUG: incremented sequence number:\n" + this.clientList.get(clientPublicKey).getSeqNumber());

            //} else {

            //    System.out.println("\nDEBUG: seqNumber in client:\n" + (seqNumber));
            //    System.out.println("\nDEBUG: seqNumber in server:\n" + (clientList.get(clientPublicKey).getSeqNumber()));
            //    System.out.println("\nDEBUG: boolean - equal seqNumbers?\n" + (clientList.get(clientPublicKey).getSeqNumber() == seqNumber));
            //    throw new Exception("\nInvalid signature.");


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
    public Acknowledge postGeneral(PublicKey clientPublicKey, String message, int wts, byte[] signature) throws RemoteException {

        try{
            System.out.println("\n-------------------------------------------------------------\n" +
                    "client" + clientList.get(clientPublicKey).getClientNumber() + " called postGeneral() method.");

            String ack = this.generalBoard.write(wts, message, clientList.get(clientPublicKey).getClientNumber(), signature, clientPublicKey);
            saveState();
            return new Acknowledge(wts, ack, AsymmetricCrypto.wrapDigitalSignature(ack + wts, this.serverPrivateKey));

            //if(AsymmetricCrypto.validateDigitalSignature(signature, clientPublicKey, message + seqNumber)
            //        & clientList.get(clientPublicKey).getSeqNumber() == seqNumber){

            //    System.out.println("\nDEBUG: seqNumber in client:\n" + (seqNumber));
            //    System.out.println("\nDEBUG: seqNumber in server:\n" + (clientList.get(clientPublicKey).getSeqNumber()));
            //    this.generalBoard.addAnnouncement(clientList.get(clientPublicKey).getClientNumber(), message);
            //    this.clientList.get(clientPublicKey).incrementSeqNumber();
            //    saveState();
            //    System.out.println("\nDEBUG: incremented sequence number:\n" + this.clientList.get(clientPublicKey).getSeqNumber());

            //}else{

            //    System.out.println("\nDEBUG: seqNumber in client:\n" + (seqNumber));
            //    System.out.println("\nDEBUG: seqNumber in server:\n" + (clientList.get(clientPublicKey).getSeqNumber()));
            //    System.out.println("\nDEBUG: boolean - equal seqNumbers?\n" + (clientList.get(clientPublicKey).getSeqNumber() == seqNumber));
            //    throw new Exception("\nInvalid signature.");

            //}


        } catch (NullPointerException e) {
            e.printStackTrace();
            System.out.println("\nInvalid Request!");
            throw new RemoteException("Invalid Request!");

        } catch (Exception e){
            //TO DO -> restrict exception catching
            e.printStackTrace();
            throw new RemoteException();
        }

    }

    @Override
    public ReadView read(PublicKey toReadClientPublicKey, int number, int rid , byte[] signature, PublicKey clientPublicKey) throws RemoteException {

        try{
            System.out.println("\n-------------------------------------------------------------\n" +
                    "client called the read() method to read client" + clientList.get(toReadClientPublicKey).getClientNumber() + "'s announcements.");

            ArrayList<Quartet<Integer, String, byte[], ArrayList<Integer>>> triplets = this.clientList.get(toReadClientPublicKey).read(number, rid, signature, clientPublicKey);
            return new ReadView(triplets, rid, AsymmetricCrypto.wrapDigitalSignature(AsymmetricCrypto.transformTripletToString(triplets) + rid, this.serverPrivateKey));

            //if(AsymmetricCrypto.validateDigitalSignature(signature, clientPublicKey, toReadClientPublicKey.toString() + number + seqNumber)
            //        & clientList.get(clientPublicKey).getSeqNumber() == seqNumber){

            //    System.out.println("\nDEBUG: seqNumber in client:\n" + (seqNumber));
            //    System.out.println("\nDEBUG: seqNumber in server:\n" + (clientList.get(clientPublicKey).getSeqNumber()));
            //    String message = clientList.get(toReadClientPublicKey).getAnnouncements(number);
            //    this.clientList.get(clientPublicKey).incrementSeqNumber();
            //    System.out.println("\nDEBUG: incremented sequence number:\n" + this.clientList.get(clientPublicKey).getSeqNumber());

            //    saveState();
            //    return new Acknowledge(message, AsymmetricCrypto.wrapDigitalSignature(message, this.serverPrivateKey));

            //}else{
            //    System.out.println("\nDEBUG: seqNumber in client:\n" + (seqNumber));
            //    System.out.println("\nDEBUG: seqNumber in server:\n" + (clientList.get(clientPublicKey).getSeqNumber()));
            //    System.out.println("\nDEBUG: boolean - equal seqNumbers?\n" + (clientList.get(clientPublicKey).getSeqNumber() == seqNumber));
            //    throw new Exception("\nInvalid signature.");

            //}

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
            System.out.println("\n-------------------------------------------------------------\n" +
                "A client called the readGeneral() method.");

            ArrayList<Quintet<Integer, String, String, byte[], ArrayList<Integer>>> quartets = this.generalBoard.read(number, rid, signature, clientPublicKey);
            return new ReadView(rid, AsymmetricCrypto.wrapDigitalSignature(AsymmetricCrypto.transformQuartetToString(quartets) + rid, this.serverPrivateKey), quartets);

            //if(AsymmetricCrypto.validateDigitalSignature(signature, clientPublicKey,"" + number + seqNumber)
            //& clientList.get(clientPublicKey).getSeqNumber() == seqNumber){

            //    System.out.println("\nDEBUG: seqNumber in client:\n" + (seqNumber));
            //    System.out.println("\nDEBUG: seqNumber in server:\n" + (clientList.get(clientPublicKey).getSeqNumber()));
            //    String message = generalBoard.getAnnouncements(number);
            //    this.clientList.get(clientPublicKey).incrementSeqNumber();
            //    System.out.println("\nDEBUG: incremented sequence number:\n" + this.clientList.get(clientPublicKey).getSeqNumber());

            //    saveState();
            //    return new Acknowledge(message, AsymmetricCrypto.wrapDigitalSignature(message, this.serverPrivateKey));

            //}else{
            //    System.out.println("\nDEBUG: seqNumber in client:\n" + (seqNumber));
            //    System.out.println("\nDEBUG: seqNumber in server:\n" + (clientList.get(clientPublicKey).getSeqNumber()));
            //    System.out.println("\nDEBUG: boolean - equal seqNumbers?\n" + (clientList.get(clientPublicKey).getSeqNumber() == seqNumber));
            //    throw new Exception("\nInvalid signature.");

            //}

        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("\nDecryption error");
        }
    }

    @Override
    public Acknowledge login(PublicKey clientPublicKey) throws RemoteException {

        try {
            String message = "" + this.clientList.get(clientPublicKey).getOneNRegularRegister().getWts() + "|" + this.generalBoard.getnNRegularRegister().getRid();
            return new Acknowledge(message, AsymmetricCrypto.wrapDigitalSignature(message, this.serverPrivateKey));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("\nLogin error");
        }

    }

}
