package sec.project.client;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.javatuples.Quartet;
import org.javatuples.Quintet;
import org.javatuples.Triplet;
import sec.project.library.Acknowledge;
import sec.project.library.AsymmetricCrypto;
import sec.project.library.ClientAPI;
import sec.project.library.ReadView;

import java.io.*;
import java.rmi.ConnectException;
import java.security.*;
import java.util.*;

public class Client {

    private KeyStore clientKeyStore;
    private PrivateKey clientPrivateKey;
    private PublicKey clientPublicKey;
    private Map<PublicKey, ClientAPI> serverPublicKeys;
    private Scanner scanner;
    private String clientNumber;
    private String keyStorePassword;
    private String privateKeyPassword;
    private int seqNumber;
    private int postWts;
    private int postGeneralWts;
    private int readRid;
    private int readGeneralRid;
    private ArrayList<Acknowledge> postAcks;
    private ArrayList<Acknowledge> postGeneralAcks;
    private ArrayList<ReadView> readResponses;
    private ArrayList<ReadView> readGeneralResponses;

    public Client (Map<Integer, ClientAPI> stubs) {

        this.scanner = new Scanner(System.in);
        System.out.println("\nInsert the client number:");
        this.clientNumber = scanner.nextLine();

        while (true){
            System.out.println("\nInsert your KeyStore's password:");
            this.keyStorePassword = new String(System.console().readPassword());
            System.out.println("\nInsert your Private Key's password:");
            this.privateKeyPassword = new String(System.console().readPassword());
            this.serverPublicKeys = new HashMap<>();

            try {

                this.clientKeyStore = AsymmetricCrypto.getKeyStore("data/keys/client" + this.clientNumber + "_keystore.jks", this.keyStorePassword);
                this.clientPrivateKey = AsymmetricCrypto.getPrivateKey(this.clientKeyStore, this.privateKeyPassword, "client" + this.clientNumber);
                this.clientPublicKey = AsymmetricCrypto.getPublicKeyFromCert("data/keys/client" + this.clientNumber + "_certificate.crt");

                for (Map.Entry<Integer, ClientAPI> entry : stubs.entrySet()) {
                    PublicKey serverPublicKey = AsymmetricCrypto.getPublicKeyFromCert("data/keys/server" + entry.getKey().intValue() + "_certificate.crt");
                    serverPublicKeys.put(serverPublicKey, entry.getValue());
                }

            } catch (IOException e) {
                System.out.println("\n-------------------------------------------------------------\n" + "Wrong credentials. Access Denied.");
                continue;
            } catch (UnrecoverableKeyException e){
                System.out.println("\n-------------------------------------------------------------\n" + "Wrong credentials. Access Denied.");
                continue;
            } catch (Exception e) {
                e.printStackTrace();
            }
            break;
        }

    }

    public void execute() {

        this.postAcks = new ArrayList<>();
        this.postGeneralAcks = new ArrayList<>();
        this.readResponses = new ArrayList<>();
        this.readGeneralResponses = new ArrayList<>();

        while (true) {

            System.out.println("\n-------------------------------------------------------------\n" + "Write a command:");
            String command = scanner.nextLine();
            String[] tokens = command.split(" ");
            String message;
            String numberOfAnnouncements;
            byte[] signature;
            Acknowledge response;
            ArrayList<ReadView> readResponsesGeneral = new ArrayList<>();
            int seconds;

            try {

                switch (tokens[0]) {
                    case "login":

                        for (Map.Entry<PublicKey, ClientAPI> entry : serverPublicKeys.entrySet()) {
                            response = entry.getValue().login(this.clientPublicKey);
                            if (AsymmetricCrypto.validateDigitalSignature(response.getSignature(), entry.getKey(), response.getMessage())) {
                                String[] responses = response.getMessage().split("|");
                                if(responses.length >= 3){
                                    if (Integer.parseInt(responses[0]) > this.postWts){
                                        this.postWts = Integer.parseInt(responses[0]);
                                    }
                                    if (Integer.parseInt(responses[2]) > this.postGeneralWts){
                                        this.postGeneralWts = Integer.parseInt(responses[2]);
                                    }
                                }
                            }
                        }

                        break;

                    case "register":

                        signature = AsymmetricCrypto.wrapDigitalSignature(this.clientNumber, this.clientPrivateKey);

                        for (Map.Entry<PublicKey, ClientAPI> entry : serverPublicKeys.entrySet()) {
                            entry.getValue().register(this.clientPublicKey, this.clientNumber, signature);
                        }

                        this.seqNumber = 1;
                        this.postWts = 0;
                        this.postGeneralWts = 0;
                        this.readRid = 0;
                        this.readGeneralRid = 0;

                        System.out.println("\nSuccessful registration.");
                        break;

                    case "post":

                        System.out.println("\nWrite your announcement:");
                        message = scanner.nextLine() + "| ";
                        if (message.length() > 255) {
                            System.out.println("\nMessage is too long! Failed post.");
                            break;
                        }

                        System.out.println("\nAny references? Insert like id1 id2 id3. If none just press enter.");
                        message += scanner.nextLine();

                        this.postWts++;

                        this.postAcks = new ArrayList<>();

                        signature = AsymmetricCrypto.wrapDigitalSignature(message + this.postWts, this.clientPrivateKey);

                        for (Map.Entry<PublicKey, ClientAPI> entry : this.serverPublicKeys.entrySet()) {
                            AsyncPost post = new AsyncPost(entry.getValue(), this, message, signature);
                            new Thread(post).start();
                        }

                        seconds = 0;
                        while (this.postAcks.size() <= (this.serverPublicKeys.size() + (this.serverPublicKeys.size() / 3)) / 2) {

                            Thread.sleep(10);
                            seconds++;
                            if (seconds > 1000){
                                System.out.println("TIMEOUT: Wasn't able to finish post operation.");
                                break;
                            }

                        }

                        System.out.println("\nSuccessfully posted.");
                        break;

                    case "postGeneral":

                        System.out.println("\nWrite your announcement:");
                        message = scanner.nextLine() + "| ";
                        if (message.length() > 255) {
                            System.out.println("\nMessage is too long! Failed post.");
                            break;
                        }

                        System.out.println("\nAny references? Insert like id1 id2 id3. If none just press enter.");
                        message += scanner.nextLine();

                        this.postGeneralWts++;

                        this.postGeneralAcks = new ArrayList<>();

                        signature = AsymmetricCrypto.wrapDigitalSignature(message + this.postGeneralWts + this.clientNumber, this.clientPrivateKey);

                        for (Map.Entry<PublicKey, ClientAPI> entry : this.serverPublicKeys.entrySet()) {
                            AsyncPostGeneral postGeneral = new AsyncPostGeneral(entry.getValue(),this, this.postGeneralWts, message, signature);
                            new Thread(postGeneral).start();
                        }

                        seconds = 0;
                        while (this.postGeneralAcks.size() <= (this.serverPublicKeys.size() + (this.serverPublicKeys.size() / 3)) / 2) {

                            Thread.sleep(10);
                            seconds++;
                            if (seconds > 1000){
                                System.out.println("TIMEOUT: Wasn't able to finish postGeneral operation.");
                                break;
                            }

                        }

                        System.out.println("\nSuccessfully posted.");
                        break;

                    case "read":

                        System.out.println("\nWrite the number of the client whose announcement board you want to read:");
                        PublicKey toReadClientPublicKey = AsymmetricCrypto.getPublicKeyFromCert("data/keys/client" + scanner.nextLine() + "_certificate.crt");

                        System.out.println("\nHow many announcements do you want to see?");
                        numberOfAnnouncements = scanner.nextLine();

                        this.readRid++;

                        this.readResponses = new ArrayList<>();

                        signature = AsymmetricCrypto.wrapDigitalSignature(toReadClientPublicKey.toString()
                                + numberOfAnnouncements + this.readRid, this.clientPrivateKey);

                        for (Map.Entry<PublicKey, ClientAPI> entry : this.serverPublicKeys.entrySet()) {
                            AsyncRead read = new AsyncRead(entry, this, toReadClientPublicKey, Integer.parseInt(numberOfAnnouncements), signature);
                            new Thread(read).start();
                        }

                        seconds = 0;
                        while (this.readResponses.size() <= (this.serverPublicKeys.size() + (this.serverPublicKeys.size() / 3)) / 2) {

                            Thread.sleep(10);
                            seconds++;
                            if (seconds > 1000){
                                System.out.println("TIMEOUT: Wasn't able to finish read operation.");
                                break;
                            }

                        }

                        Map<Integer, Quartet<Integer, String, byte[], ArrayList<Integer>>> announcements = new HashMap<>();

                        int maxWts = 0;
                        int numberOfAnnoucesServer = 0;
                        for (ReadView readView : this.readResponses) {
                            for (Quartet<Integer, String, byte[], ArrayList<Integer>> triplet : readView.getAnnounces()) {

                                if (maxWts < triplet.getValue0()) {
                                    maxWts = triplet.getValue0();
                                }

                                if(!announcements.containsKey(triplet.getValue0())) {
                                    announcements.put(triplet.getValue0(), triplet);
                                    numberOfAnnoucesServer++;
                                }

                            }
                        }

                        int aux;
                        if(numberOfAnnoucesServer < Integer.parseInt(numberOfAnnouncements)){
                            aux = numberOfAnnoucesServer;
                        }else{
                            aux = Integer.parseInt(numberOfAnnouncements);
                        }

                        for (int i = maxWts - aux + 1; i <= maxWts; i++) {

                            Quartet<Integer, String, byte[], ArrayList<Integer>> announcement = announcements.get(i);

                            String originalRefs = "";
                            for(int j=0; j<announcement.getValue3().size(); j++){
                                originalRefs += announcement.getValue3().get(j) + " ";
                            }

                            String originalMessage = announcement.getValue1();
                            String originalText = originalMessage.substring(0, originalMessage.indexOf("|"));

                            System.out.println("\nAnnouncement id: " + announcement.getValue0() + "\n message: " + originalText + "\n references: " + originalRefs);

                            //writeback

                            for (Map.Entry<PublicKey, ClientAPI> entry : this.serverPublicKeys.entrySet()) {
                                AsyncPost writeBack = new AsyncPost(entry.getValue(), this, announcement.getValue0(), toReadClientPublicKey, announcement.getValue1(), announcement.getValue2());
                                new Thread(writeBack).start();
                            }

                            seconds = 0;
                            while (this.postAcks.size() <= (this.serverPublicKeys.size() + (this.serverPublicKeys.size() / 3)) / 2) {

                                Thread.sleep(10);
                                seconds++;
                                if (seconds > 1000){
                                    System.out.println("TIMEOUT: Wasn't able to finish writeback operation.");
                                    break;
                                }

                            }


                        }


                        break;

                    case "readGeneral":

                        System.out.println("\nHow many announcements do you want to see?");
                        numberOfAnnouncements = scanner.nextLine();

                        this.readGeneralRid++;

                        this.readGeneralResponses = new ArrayList<>();

                        signature = AsymmetricCrypto.wrapDigitalSignature(numberOfAnnouncements + this.readGeneralRid, this.clientPrivateKey);

                        for (Map.Entry<PublicKey, ClientAPI> entry : this.serverPublicKeys.entrySet()) {
                            AsyncReadGeneral readGeneral = new AsyncReadGeneral(entry,this, Integer.parseInt(numberOfAnnouncements), signature);
                            new Thread(readGeneral).start();
                        }

                        seconds = 0;
                        while (this.readGeneralResponses.size() <= (this.serverPublicKeys.size() + (this.serverPublicKeys.size() / 3)) / 2) {

                            Thread.sleep(10);
                            seconds++;
                            if (seconds > 1000){
                                System.out.println("TIMEOUT: Wasn't able to finish readGeneral operation.");
                                break;
                            }

                        }

                        int versionGeneral = 0;
                        ReadView mostUpdatedGeneral = null;

                        for (ReadView readView : this.readGeneralResponses) {
                            int receivedVersion = readView.getAnnouncesGeneral().get(readView.getAnnouncesGeneral().size() - 1).getValue0();

                            if (receivedVersion > versionGeneral) {
                                versionGeneral = receivedVersion;
                                mostUpdatedGeneral = readView;
                            }
                        }

                        for (Quintet<Integer, String, String, byte[], ArrayList<Integer>> announce : mostUpdatedGeneral.getAnnouncesGeneral()) {

                            String originalRefs = "";
                            for(int j=0; j<announce.getValue4().size(); j++){
                                originalRefs += announce.getValue4().get(j) + " ";
                            }

                            String originalMessage = announce.getValue1();
                            String originalText = originalMessage.substring(0, originalMessage.indexOf("|"));

                            System.out.println("\nAnnouncement id: " + announce.getValue0() + "\n message: " + originalText + "\n references: " + originalRefs);
                        }

                        break;

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Deprecated
    public void saveState() throws IOException {

        FileOutputStream f = new FileOutputStream(new File("data/client" + this.clientNumber + "state.txt"));
        ObjectOutputStream o = new ObjectOutputStream(f);

        o.writeObject(this.seqNumber);
        System.out.println("\nDEBUG: Saving client state:\n" + this.seqNumber);

        o.close();
        f.close();
    }

    @Deprecated
    public void loadState() throws IOException, ClassNotFoundException {

        File stateFile = new File("data/client" + this.clientNumber + "state.txt");

        if (!(stateFile.exists())) {

            this.seqNumber = 1;

        } else {

            FileInputStream file = new FileInputStream(stateFile);
            ObjectInputStream objStream = new ObjectInputStream(file);

            this.seqNumber = (int) objStream.readObject();
            System.out.println("\nDEBUG: Loading client state:\n" + this.seqNumber);

            objStream.close();
            file.close();
        }

    }

    public int getPostWts() { return postWts; }
    public int getPostGeneralWts() { return postGeneralWts; }
    public int getReadRid() { return readRid; }
    public int getReadGeneralRid() { return readGeneralRid; }
    public Map<PublicKey, ClientAPI> getServerPublicKeys() { return serverPublicKeys; }
    public PublicKey getClientPublicKey() { return clientPublicKey; }
    public ArrayList<Acknowledge> getPostAcks() { return postAcks; }
    public ArrayList<Acknowledge> getPostGeneralAcks() { return postGeneralAcks; }
    public ArrayList<ReadView> getReadResponses() { return readResponses; }
    public ArrayList<ReadView> getReadGeneralResponses() { return readGeneralResponses; }

}
