package sec.project.client;

import org.javatuples.Triplet;
import sec.project.library.Acknowledge;
import sec.project.library.AsymmetricCrypto;
import sec.project.library.ClientAPI;
import sec.project.library.ReadView;

import java.io.*;
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
    private int rid;

    public Client (Map<Integer, ClientAPI> stubs) {

        this.scanner = new Scanner(System.in);
        System.out.println("\nInsert the client number:");
        this.clientNumber = scanner.nextLine();
        System.out.println("\nInsert your KeyStore's password:");
        this.keyStorePassword = new String(System.console().readPassword());
        System.out.println("\nInsert your Private Key's password:");
        this.privateKeyPassword = new String(System.console().readPassword());
        this.serverPublicKeys = new HashMap<>();

        try {

            this.clientKeyStore = AsymmetricCrypto.getKeyStore("data/keys/client" + this.clientNumber + "_keystore.jks", this.keyStorePassword);
            this.clientPrivateKey = AsymmetricCrypto.getPrivateKey(this.clientKeyStore, this.privateKeyPassword, "client" + this.clientNumber);
            this.clientPublicKey = AsymmetricCrypto.getPublicKeyFromCert("data/keys/client" + this.clientNumber + "_certificate.crt");

            for(Map.Entry<Integer, ClientAPI> entry : stubs.entrySet()){
                PublicKey serverPublicKey = AsymmetricCrypto.getPublicKeyFromCert("data/keys/server" + entry.getKey().intValue() + "_certificate.crt");
                serverPublicKeys.put(serverPublicKey, entry.getValue());
            }

        } catch (Exception e) {

            e.printStackTrace();

        }
    }

    public void execute() {

        while (true) {

            System.out.println("\n-------------------------------------------------------------\n" + "Write a command:");
            String command = scanner.nextLine();
            String[] tokens = command.split(" ");
            String message;
            String numberOfAnnouncements;
            byte[] signature;
            Acknowledge response;
            ArrayList<ReadView> readResponses = new ArrayList<>();

            try {

                switch (tokens[0]) {
                    case "login":

                        for (Map.Entry<PublicKey, ClientAPI> entry : serverPublicKeys.entrySet()) {
                            response = entry.getValue().login(this.clientPublicKey);
                            if (AsymmetricCrypto.validateDigitalSignature(response.getSignature(), entry.getKey(), response.getMessage())){
                                this.seqNumber = Integer.parseInt(response.getMessage());
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
                        this.rid = 0;

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

                        ArrayList<Acknowledge> acknowledges = new ArrayList<>();

                        signature = AsymmetricCrypto.wrapDigitalSignature(message + this.postWts, this.clientPrivateKey);

                        for (Map.Entry<PublicKey, ClientAPI> entry : serverPublicKeys.entrySet()) {
                            Acknowledge acknowledge = entry.getValue().post(this.clientPublicKey, message, this.postWts, signature);

                            if (acknowledge.getWts() == this.postWts){
                                acknowledges.add(acknowledge);
                            }

                            // if (#ACK >= (N + f) / 2 (int, rounded down) with f = (N / 3) (int, rounded down)
                            if (acknowledges.size() >= (this.serverPublicKeys.size() + (this.serverPublicKeys.size() / 3)) / 2){
                                acknowledges = new ArrayList<>();
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

                        signature = AsymmetricCrypto.wrapDigitalSignature(message + this.seqNumber, this.clientPrivateKey);

                        for (Map.Entry<PublicKey, ClientAPI> entry : serverPublicKeys.entrySet()) {
                            entry.getValue().postGeneral(this.clientPublicKey, message, this.seqNumber, signature);
                        }

                        this.seqNumber++;

                        System.out.println("\nSuccessfully posted.");
                        break;

                    case "read":

                        System.out.println("\nWrite the number of the client whose announcement board you want to read:");
                        PublicKey toReadClientPublicKey = AsymmetricCrypto.getPublicKeyFromCert("data/keys/client" + scanner.nextLine() + "_certificate.crt");

                        System.out.println("\nHow many announcements do you want to see?");
                        numberOfAnnouncements = scanner.nextLine();

                        this.rid++;

                        for (Map.Entry<PublicKey, ClientAPI> entry : serverPublicKeys.entrySet()){
                            ReadView readResponse = entry.getValue().read(toReadClientPublicKey, Integer.parseInt(numberOfAnnouncements), this.rid,
                                    AsymmetricCrypto.wrapDigitalSignature(toReadClientPublicKey.toString() + numberOfAnnouncements + this.rid, this.clientPrivateKey), this.clientPublicKey);

                            if(AsymmetricCrypto.validateDigitalSignature(readResponse.getSignature(), entry.getKey(),
                                    readResponse.getAnnounces().toString() + readResponse.getRid()) && this.rid == readResponse.getRid()){

                                boolean valid = true;
                                for(Triplet<Integer,String,byte[]> announce : readResponse.getAnnounces()){
                                    if(!(AsymmetricCrypto.validateDigitalSignature(announce.getValue2(),toReadClientPublicKey,
                                            announce.getValue1() + announce.getValue0()))){
                                        valid = false;
                                    }
                                }

                                if(valid){
                                    readResponses.add(readResponse);
                                }

                            }else{
                                System.out.println("\nInvalid Response from a server!");
                            }

                        }

                        int version = 0;
                        ReadView mostUpdated = null;
                        for(ReadView x : readResponses){
                            int localVersion = x.getAnnounces().get(x.getAnnounces().size() - 1).getValue0();

                            if (localVersion > version) {
                                version = localVersion;
                                mostUpdated = x;
                            }
                        }

                        readResponses = new ArrayList<>();

                        for(Triplet<Integer, String, byte[]> announce : mostUpdated.getAnnounces()){
                            System.out.println(announce.getValue1());
                        }

                        break;

                    case "readGeneral":

                        System.out.println("\nHow many announcements do you want to see?");
                        numberOfAnnouncements = scanner.nextLine();

                        for (Map.Entry<PublicKey, ClientAPI> entry : serverPublicKeys.entrySet()){

                            response = entry.getValue().readGeneral(Integer.parseInt(numberOfAnnouncements), this.seqNumber,
                                    AsymmetricCrypto.wrapDigitalSignature(numberOfAnnouncements + this.seqNumber, this.clientPrivateKey), this.clientPublicKey);

                            if(AsymmetricCrypto.validateDigitalSignature(response.getSignature(), entry.getKey(), response.getMessage())){
                                System.out.println(response.getMessage());
                            }else{
                                System.out.println("Invalid Response!");
                            }

                        }

                        this.seqNumber++;

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
}
