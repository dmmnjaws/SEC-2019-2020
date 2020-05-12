package sec.project.client;

import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.javatuples.Quintet;
import sec.project.library.Acknowledge;
import sec.project.library.AsymmetricCrypto;
import sec.project.library.ClientAPI;
import sec.project.library.ReadView;

import java.io.*;
import java.security.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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
    private AtomicInteger numberOfRegistersFinished;
    private Map<PublicKey, Acknowledge> postAcks;
    private AtomicInteger numberOfPostAcks;
    private Map<PublicKey, Acknowledge> postGeneralAcks;
    private AtomicInteger numberOfPostGeneralAcks;
    private Map<PublicKey, ReadView> readResponses;
    private AtomicInteger numberOfReadResponses;
    private Map<PublicKey, ReadView> readGeneralResponses;
    private AtomicInteger numberOfReadGeneralResponses;
    private Map<PublicKey, String> loginResponses;
    private AtomicInteger numberOfLoginResponses;
    private boolean exception;

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

        this.postAcks = new HashMap<>();
        this.postGeneralAcks = new HashMap<>();
        this.readResponses = new HashMap<>();
        this.readGeneralResponses = new HashMap<>();
        this.loginResponses = new HashMap<>();
        this.numberOfRegistersFinished = new AtomicInteger();
        this.numberOfLoginResponses = new AtomicInteger();
        this.numberOfPostAcks = new AtomicInteger();
        this.numberOfReadResponses = new AtomicInteger();
        this.numberOfPostGeneralAcks = new AtomicInteger();
        this.numberOfReadGeneralResponses = new AtomicInteger();
        this.seqNumber = 1;
        this.postWts = 0;
        this.postGeneralWts = 0;
        this.readRid = 0;
        this.readGeneralRid = 0;
        this.exception = false;

        while (true) {

            System.out.println("\n-------------------------------------------------------------\n" + "Write a command:");
            String command = scanner.nextLine();
            String[] tokens = command.split(" ");
            String message;
            String numberOfAnnouncements;
            byte[] signature;
            Acknowledge response;
            int seconds;

            try {

                switch (tokens[0]) {
                    case "login":

                        login();

                        break;

                    case "register":

                        this.numberOfRegistersFinished.set(0);

                        signature = AsymmetricCrypto.wrapDigitalSignature(this.clientNumber, this.clientPrivateKey);

                        for (Map.Entry<PublicKey, ClientAPI> entry : serverPublicKeys.entrySet()) {
                            AsyncRegister post = new AsyncRegister(entry, this.clientPublicKey, this.clientNumber, signature, this);
                            new Thread(post).start();
                        }

                        seconds = 0;
                        while(this.numberOfRegistersFinished.get() <= (this.serverPublicKeys.size() + (this.serverPublicKeys.size() / 3)) / 2){

                            Thread.sleep(10);
                            seconds++;
                            if (seconds > 1000) {
                                System.out.println("\nTIMEOUT: Wasn't able to finish register operation.");
                                break;
                            }

                        }

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

                        this.exception = false;

                        this.postWts++;

                        this.postAcks = new HashMap<>();
                        this.numberOfPostAcks.set(0);

                        signature = AsymmetricCrypto.wrapDigitalSignature(message + this.postWts, this.clientPrivateKey);

                        for (Map.Entry<PublicKey, ClientAPI> entry : this.serverPublicKeys.entrySet()) {
                            AsyncPost post = new AsyncPost(entry, this, message, signature);
                            new Thread(post).start();
                        }

                        seconds = 0;
                        while (this.numberOfPostAcks.get() <= (this.serverPublicKeys.size() + (this.serverPublicKeys.size() / 3)) / 2) {

                            Thread.sleep(10);
                            seconds++;
                            if (seconds > 1000) {
                                System.out.println("\nTIMEOUT: Wasn't able to finish post operation.");
                                break;
                            }

                        }

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

                        this.exception = false;

                        login();

                        this.postGeneralWts++;

                        this.postGeneralAcks = new HashMap<>();
                        this.numberOfPostGeneralAcks.set(0);

                        signature = AsymmetricCrypto.wrapDigitalSignature(message + this.postGeneralWts + this.clientNumber, this.clientPrivateKey);

                        for (Map.Entry<PublicKey, ClientAPI> entry : this.serverPublicKeys.entrySet()) {
                            AsyncPostGeneral postGeneral = new AsyncPostGeneral(entry, this, this.postGeneralWts, message, signature);
                            new Thread(postGeneral).start();
                        }

                        seconds = 0;
                        while (this.numberOfPostGeneralAcks.get() <= (this.serverPublicKeys.size() + (this.serverPublicKeys.size() / 3)) / 2) {

                            Thread.sleep(10);
                            seconds++;
                            if (seconds > 1000) {
                                System.out.println("\nTIMEOUT: Wasn't able to finish postGeneral operation.");
                                break;
                            }

                        }

                        break;

                    case "read":

                        PublicKey toReadClientPublicKey = null;

                        try{

                            System.out.println("\nWrite the number of the client whose announcement board you want to read:");
                            toReadClientPublicKey = AsymmetricCrypto.getPublicKeyFromCert("data/keys/client" + scanner.nextLine() + "_certificate.crt");

                        } catch (FileNotFoundException e){
                            System.out.println("\nThe client you indicated does not exist.");
                            break;
                        }

                        System.out.println("\nHow many announcements do you want to see?");
                        numberOfAnnouncements = scanner.nextLine();

                        this.exception = false;

                        this.readRid++;

                        this.readResponses = new HashMap<>();
                        this.numberOfReadResponses.set(0);

                        signature = AsymmetricCrypto.wrapDigitalSignature(toReadClientPublicKey.toString()
                                + numberOfAnnouncements + this.readRid, this.clientPrivateKey);

                        for (Map.Entry<PublicKey, ClientAPI> entry : this.serverPublicKeys.entrySet()) {
                            AsyncRead read = new AsyncRead(entry, this, toReadClientPublicKey, Integer.parseInt(numberOfAnnouncements), signature);
                            new Thread(read).start();
                        }

                        seconds = 0;
                        while (this.numberOfReadResponses.get() <= (this.serverPublicKeys.size() + (this.serverPublicKeys.size() / 3)) / 2) {

                            Thread.sleep(10);
                            seconds++;
                            if (seconds > 1000) {
                                System.out.println("\nTIMEOUT: Wasn't able to finish read operation.");
                                break;
                            }

                        }

                        Map<Integer, Quartet<Integer, String, byte[], ArrayList<Integer>>> announcements = new HashMap<>();

                        int maxWts = 0;
                        int numberOfAnnoucesServer = 0;
                        for (Map.Entry<PublicKey, ReadView> entry : this.readResponses.entrySet()) {

                            if(entry.getValue() != null){
                                if(entry.getValue().getAnnounces().size() != 0){
                                    for (Quartet<Integer, String, byte[], ArrayList<Integer>> triplet : entry.getValue().getAnnounces()) {

                                        if (maxWts < triplet.getValue0()) {
                                            maxWts = triplet.getValue0();
                                        }

                                        if (!announcements.containsKey(triplet.getValue0())) {
                                            announcements.put(triplet.getValue0(), triplet);
                                            numberOfAnnoucesServer++;
                                        }

                                    }
                                }
                            }
                        }

                        if (maxWts == 0){
                            if(this.exception == false){
                                System.out.println("\nThere are no announcements to show.");
                            }
                            break;
                        }

                        int aux;
                        if (numberOfAnnoucesServer < Integer.parseInt(numberOfAnnouncements)) {
                            aux = numberOfAnnoucesServer;
                        } else {
                            aux = Integer.parseInt(numberOfAnnouncements);
                        }

                        for (int i = maxWts - aux + 1; i <= maxWts; i++) {

                            Quartet<Integer, String, byte[], ArrayList<Integer>> announcement = announcements.get(i);

                            String originalRefs = "";
                            for (int j = 0; j < announcement.getValue3().size(); j++) {
                                originalRefs += announcement.getValue3().get(j) + " ";
                            }

                            String originalMessage = announcement.getValue1();
                            String originalText = originalMessage.substring(0, originalMessage.indexOf("|"));

                            System.out.println("\nAnnouncement id: " + announcement.getValue0() + "\n message: " + originalText + "\n references: " + originalRefs);

                            //writeback

                            this.postAcks = new HashMap<>();
                            this.numberOfPostAcks.set(0);

                            for (Map.Entry<PublicKey, ClientAPI> entry : this.serverPublicKeys.entrySet()) {
                                AsyncPost writeBack = new AsyncPost(entry, this, announcement.getValue0(), toReadClientPublicKey, announcement.getValue1(), announcement.getValue2());
                                new Thread(writeBack).start();
                            }

                            seconds = 0;
                            while (this.numberOfPostAcks.get() <= (this.serverPublicKeys.size() + (this.serverPublicKeys.size() / 3)) / 2) {

                                Thread.sleep(10);
                                seconds++;
                                if (seconds > 1000) {
                                    System.out.println("\nTIMEOUT: Wasn't able to finish writeback operation.");
                                    break;
                                }

                            }


                        }


                        break;

                    case "readGeneral":

                        System.out.println("\nHow many announcements do you want to see?");
                        numberOfAnnouncements = scanner.nextLine();

                        this.exception = false;

                        this.readGeneralRid++;

                        this.readGeneralResponses = new HashMap<>();
                        this.numberOfReadGeneralResponses.set(0);

                        signature = AsymmetricCrypto.wrapDigitalSignature(numberOfAnnouncements + this.readGeneralRid, this.clientPrivateKey);

                        for (Map.Entry<PublicKey, ClientAPI> entry : this.serverPublicKeys.entrySet()) {
                            AsyncReadGeneral readGeneral = new AsyncReadGeneral(entry, this, Integer.parseInt(numberOfAnnouncements), signature);
                            new Thread(readGeneral).start();
                        }

                        seconds = 0;
                        while (this.numberOfReadGeneralResponses.get() <= (this.serverPublicKeys.size() + (this.serverPublicKeys.size() / 3)) / 2) {

                            Thread.sleep(10);
                            seconds++;
                            if (seconds > 1000) {
                                System.out.println("\nTIMEOUT: Wasn't able to finish readGeneral operation.");
                                break;
                            }

                        }

                        int versionGeneral = 0;
                        ReadView mostUpdatedGeneral = null;

                        for (Map.Entry<PublicKey, ReadView> entry : this.readGeneralResponses.entrySet()) {

                            if(entry.getValue() != null) {
                                if (entry.getValue().getAnnouncesGeneral().size() != 0) {
                                    int receivedVersion = entry.getValue().getAnnouncesGeneral().get(entry.getValue().getAnnouncesGeneral().size() - 1).getValue0();

                                    if (receivedVersion > versionGeneral) {
                                        versionGeneral = receivedVersion;
                                        mostUpdatedGeneral = entry.getValue();
                                    }
                                }
                            }
                        }

                        if(versionGeneral == 0){
                            if(this.exception == false){
                                System.out.println("\nThere are no announcements to show.");
                            }
                            break;
                        }


                        for (Quintet<Integer, String, String, byte[], ArrayList<Integer>> announce : mostUpdatedGeneral.getAnnouncesGeneral()) {

                            String originalRefs = "";
                            for (int j = 0; j < announce.getValue4().size(); j++) {
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
    public Map<PublicKey, Acknowledge> getPostAcks() { return postAcks; }
    public Map<PublicKey, Acknowledge> getPostGeneralAcks() { return postGeneralAcks; }
    public Map<PublicKey, ReadView> getReadResponses() { return readResponses; }
    public Map<PublicKey, ReadView> getReadGeneralResponses() { return readGeneralResponses; }
    public Map<PublicKey, String> getLoginResponses() { return loginResponses; }
    protected void incrementNumberOfPostAcks(){ this.numberOfPostAcks.incrementAndGet(); }
    protected void incrementNumberOfPostGeneralAcks(){ this.numberOfPostGeneralAcks.incrementAndGet(); }
    protected void incrementNumberOfReadResponses(){ this.numberOfReadResponses.incrementAndGet(); }
    protected void incrementNumberOfReadGeneralResponses(){ this.numberOfReadGeneralResponses.incrementAndGet(); }
    protected void incrementNumberOfLoginResponses(){ this.numberOfLoginResponses.incrementAndGet(); }
    protected void incrementNumberOfTRegistersFinished(){this.numberOfRegistersFinished.incrementAndGet(); }
    protected void setException(boolean exception){ this.exception = exception; }

    private void login() throws InterruptedException {

        this.numberOfLoginResponses.set(0);
        for (Map.Entry<PublicKey, ClientAPI> entry : serverPublicKeys.entrySet()) {
            AsyncLogin Login = new AsyncLogin(entry, this);
            new Thread(Login).start();
        }

        int seconds = 0;
        while (this.numberOfLoginResponses.get() <= (this.serverPublicKeys.size() + (this.serverPublicKeys.size() / 3)) / 2) {

            Thread.sleep(10);
            seconds++;
            if (seconds > 1000){
                System.out.println("\nTIMEOUT: Wasn't able to finish postGeneral operation.");
                break;
            }

        }

        if (seconds < 1000){

            Map<Integer, Integer> wtsCounts = new HashMap<>();
            Map<Integer, Integer> wtsGeneralCounts = new HashMap<>();
            Pair<Integer, Integer> resultWts = new Pair<>(this.postWts, 0);
            Pair<Integer, Integer> resultWtsGeneral = new Pair<>(this.postGeneralWts, 0);

            for(Map.Entry<PublicKey, String> entry : this.loginResponses.entrySet()){

                if(entry.getValue() != null){

                    String[] responses = entry.getValue().split("|");

                    if(responses.length >= 3){
                        int newWts = Integer.parseInt(responses[0]);
                        int newGeneralWts = Integer.parseInt(responses[2]);

                        if (newWts > this.postWts) {
                            if (wtsCounts.containsKey(newWts)) {
                                wtsCounts.put(newWts, wtsCounts.get(newWts) + 1);
                            } else {
                                wtsCounts.put(newWts, 1);
                            }
                        }

                        if (newGeneralWts > this.postGeneralWts) {
                            if (wtsGeneralCounts.containsKey(newGeneralWts)) {
                                wtsGeneralCounts.put(newGeneralWts, wtsGeneralCounts.get(newGeneralWts) + 1);
                            } else {
                                wtsGeneralCounts.put(newGeneralWts, 1);
                            }
                        }
                    }
                }
            }

            for (Map.Entry<Integer, Integer> wtsCount : wtsCounts.entrySet()){
                if (wtsCount.getKey() > resultWts.getValue0() && wtsCount.getValue() > resultWts.getValue1()){
                    resultWts = new Pair<>(wtsCount.getKey(), wtsCount.getValue());
                }
            }
            for (Map.Entry<Integer, Integer> wtsGeneralCount : wtsGeneralCounts.entrySet()){
                if (wtsGeneralCount.getKey() > resultWtsGeneral.getValue0() && wtsGeneralCount.getValue() > resultWtsGeneral.getValue1()){
                    resultWtsGeneral = new Pair<>(wtsGeneralCount.getKey(), wtsGeneralCount.getValue());
                }
            }

            this.postWts = resultWts.getValue0();
            this.postGeneralWts = resultWtsGeneral.getValue0();
        }

    }
}
