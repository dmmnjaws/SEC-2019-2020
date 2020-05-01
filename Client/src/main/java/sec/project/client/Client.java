package sec.project.client;

import org.javatuples.Quartet;
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
    private int postGeneralWts;
    private int readRid;
    private int readGeneralRid;

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
            ArrayList<ReadView> readResponsesGeneral = new ArrayList<>();

            try {

                switch (tokens[0]) {
                    case "login":

                        for (Map.Entry<PublicKey, ClientAPI> entry : serverPublicKeys.entrySet()) {
                            response = entry.getValue().login(this.clientPublicKey);
                            if (AsymmetricCrypto.validateDigitalSignature(response.getSignature(), entry.getKey(), response.getMessage())){
                                this.postWts = Integer.parseInt(response.getMessage());
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

                        ArrayList<Acknowledge> postAcknowledges = new ArrayList<>();

                        signature = AsymmetricCrypto.wrapDigitalSignature(message + this.postWts, this.clientPrivateKey);

                        for (Map.Entry<PublicKey, ClientAPI> entry : serverPublicKeys.entrySet()) {

                            Acknowledge acknowledge = entry.getValue().post(this.clientPublicKey, message, this.postWts, signature);

                            if (acknowledge.getWts() == this.postWts){
                                postAcknowledges.add(acknowledge);
                            }

                            // if (#ACK > (N + f) / 2 (int, rounded down) with f = (N / 3) (int, rounded down)
                            if (postAcknowledges.size() > (this.serverPublicKeys.size() + (this.serverPublicKeys.size() / 3)) / 2){
                                postAcknowledges = new ArrayList<>();
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

                        ArrayList<Acknowledge> postGeneralAcknowledges = new ArrayList<>();

                        System.out.println(message + "|" + this.postGeneralWts + "|" + this.clientNumber + "|" );

                        signature = AsymmetricCrypto.wrapDigitalSignature(message + this.postGeneralWts + this.clientNumber, this.clientPrivateKey);

                        for (Map.Entry<PublicKey, ClientAPI> entry : serverPublicKeys.entrySet()) {

                            Acknowledge acknowledge = entry.getValue().postGeneral(this.clientPublicKey, message, this.postGeneralWts, signature);

                            if (acknowledge.getWts() == this.postGeneralWts){
                                postGeneralAcknowledges.add(acknowledge);
                            }

                            // if (#ACK > (N + f) / 2 (int, rounded down) with f = (N / 3) (int, rounded down)
                            if (postGeneralAcknowledges.size() > (this.serverPublicKeys.size() + (this.serverPublicKeys.size() / 3)) / 2){
                                postGeneralAcknowledges = new ArrayList<>();
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

                        for (Map.Entry<PublicKey, ClientAPI> entry : serverPublicKeys.entrySet()){
                            ReadView readResponse = entry.getValue().read(toReadClientPublicKey, Integer.parseInt(numberOfAnnouncements), this.readRid,
                                    AsymmetricCrypto.wrapDigitalSignature(toReadClientPublicKey.toString() + numberOfAnnouncements + this.readRid, this.clientPrivateKey), this.clientPublicKey);

                            if(AsymmetricCrypto.validateDigitalSignature(readResponse.getSignature(), entry.getKey(),
                                    AsymmetricCrypto.transformTripletToString(readResponse.getAnnounces()) + readResponse.getRid()) && this.readRid == readResponse.getRid()){

                                boolean valid = true;
                                for(Triplet<Integer, String, byte[]> announce : readResponse.getAnnounces()){
                                    if(!(AsymmetricCrypto.validateDigitalSignature(announce.getValue2(),toReadClientPublicKey,
                                            announce.getValue1() + announce.getValue0()))){
                                        valid = false;
                                    }
                                }

                                if(valid && readResponse.getAnnounces().size() != 0){
                                    readResponses.add(readResponse);
                                }

                            }else{
                                System.out.println("\nInvalid Response from a server!");
                            }

                        }

                        int version = 0;
                        ReadView mostUpdated = null;

                        for(ReadView readView : readResponses){
                            int receivedVersion = readView.getAnnounces().get(readView.getAnnounces().size() - 1).getValue0();

                            if (receivedVersion > version) {
                                version = receivedVersion;
                                mostUpdated = readView;
                            }
                        }

                        readResponses = new ArrayList<>();

                        for(Triplet<Integer, String, byte[]> announce : mostUpdated.getAnnounces()){

                            String originalMessage = announce.getValue1();
                            String originalText = originalMessage.substring(0, originalMessage.indexOf("|"));
                            String originalRefs = originalMessage.substring(originalMessage.indexOf("|")+1, originalMessage.length());

                            System.out.println("\nAnnouncement id: "+ announce.getValue0() + "\n message: " + originalText + "\n references: " + originalRefs);
                        }

                        break;

                    case "readGeneral":

                        System.out.println("\nHow many announcements do you want to see?");
                        numberOfAnnouncements = scanner.nextLine();

                        this.readGeneralRid++;

                        for (Map.Entry<PublicKey, ClientAPI> entry : serverPublicKeys.entrySet()){

                            ReadView readGeneralResponse = entry.getValue().readGeneral(Integer.parseInt(numberOfAnnouncements), this.readGeneralRid,
                                    AsymmetricCrypto.wrapDigitalSignature(numberOfAnnouncements + this.readGeneralRid, this.clientPrivateKey), this.clientPublicKey);

                            if(AsymmetricCrypto.validateDigitalSignature(readGeneralResponse.getSignature(), entry.getKey(),
                                    AsymmetricCrypto.transformQuartetToString(readGeneralResponse.getAnnouncesGeneral()) + readGeneralResponse.getRid()) && this.readGeneralRid == readGeneralResponse.getRid()){

                                boolean valid = true;
                                for(Quartet<Integer, String, String, byte[]> announce : readGeneralResponse.getAnnouncesGeneral()){

                                    PublicKey clientPublicKey = AsymmetricCrypto.getPublicKeyFromCert("data/keys/client" + announce.getValue2() + "_certificate.crt");

                                    if(!(AsymmetricCrypto.validateDigitalSignature(announce.getValue3(), clientPublicKey,
                                            announce.getValue1() + announce.getValue0() + announce.getValue2()))){
                                        valid = false;
                                    }
                                }

                                if(valid && readGeneralResponse.getAnnouncesGeneral().size() != 0){
                                    readResponsesGeneral.add(readGeneralResponse);
                                }

                            }else{
                                System.out.println("Invalid Response from a server!");
                            }

                        }

                        int versionGeneral = 0;
                        ReadView mostUpdatedGeneral = null;

                        for(ReadView readView : readResponsesGeneral){
                            int receivedVersion = readView.getAnnouncesGeneral().get(readView.getAnnouncesGeneral().size() - 1).getValue0();

                            if (receivedVersion > versionGeneral) {
                                versionGeneral = receivedVersion;
                                mostUpdatedGeneral = readView;
                            }
                        }

                        readResponsesGeneral = new ArrayList<>();

                        for(Quartet<Integer, String, String, byte[]> announce : mostUpdatedGeneral.getAnnouncesGeneral()){

                            String originalMessage = announce.getValue1();
                            String originalText = originalMessage.substring(0, originalMessage.indexOf("|"));
                            String originalRefs = originalMessage.substring(originalMessage.indexOf("|")+1, originalMessage.length());

                            System.out.println("\nAnnouncement id: "+ announce.getValue0() + "\n message: " + originalText + "\n references: " + originalRefs);
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
}
