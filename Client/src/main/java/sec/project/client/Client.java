package sec.project.client;

import jdk.internal.net.http.common.Pair;
import sec.project.library.Acknowledge;
import sec.project.library.AsymmetricCrypto;
import sec.project.library.ClientAPI;

import java.io.Console;
import java.security.*;
import java.util.Scanner;

public class Client {

    private PrivateKey clientPrivateKey;
    private PublicKey clientPublicKey;
    private PublicKey serverPublicKey;
    private ClientAPI stub;
    private Scanner scanner;
    private String clientNumber;
    private String serverNumber;
    private int seqNumber;

    public Client (ClientAPI stub) {

        this.scanner = new Scanner(System.in);
        System.out.println("\nInsert the client number:");
        this.clientNumber = scanner.nextLine();
        System.out.println("\nInsert the server number you want to connect to:");
        this.serverNumber = scanner.nextLine();
        this.seqNumber = 1;

        try {

            this.clientPrivateKey = AsymmetricCrypto.getPrivateKey("data/keys/client" + clientNumber + "_private_key.der");
            this.clientPublicKey = AsymmetricCrypto.getPublicKey("data/keys/client" + clientNumber + "_public_key.der");
            this.serverPublicKey = AsymmetricCrypto.getPublicKey("data/keys/server" + serverNumber + "_public_key.der");

        } catch (Exception e) {

            e.printStackTrace();

        }

        this.stub = stub;
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

            try {
                switch (tokens[0]) {
                    case "register":

                        signature = AsymmetricCrypto.wrapDigitalSignature(this.clientNumber, this.clientPrivateKey);
                        stub.register(this.clientPublicKey, this.clientNumber, signature);

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

                        signature = AsymmetricCrypto.wrapDigitalSignature(message + this.seqNumber, this.clientPrivateKey);
                        stub.post(this.clientPublicKey, message, this.seqNumber, signature);
                        this.seqNumber++;

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
                        stub.postGeneral(this.clientPublicKey, message, this.seqNumber, signature);
                        this.seqNumber++;

                        System.out.println("\nSuccessfully posted.");
                        break;

                    case "read":
                        
                        System.out.println("\nWrite the number of the client whose announcement board you want to read:");
                        PublicKey toReadClientPublicKey = AsymmetricCrypto.getPublicKey("data/keys/client" + scanner.nextLine() + "_public_key.der");

                        System.out.println("\nHow many announcements do you want to see?");
                        numberOfAnnouncements = scanner.nextLine();

                        response = stub.read(toReadClientPublicKey, Integer.parseInt(numberOfAnnouncements), this.seqNumber,
                                AsymmetricCrypto.wrapDigitalSignature(toReadClientPublicKey.toString() + numberOfAnnouncements + this.seqNumber, this.clientPrivateKey), this.clientPublicKey);
                        this.seqNumber++; //this has to be incremented before the signature's validation, otherwise, the client's seqNumber may become < then the server's seqNumber if server signature invalid.

                        if(AsymmetricCrypto.validateDigitalSignature(response.getSignature(), this.serverPublicKey, response.getMessage())){
                            System.out.println(response.getMessage());
                        }else{
                            System.out.println("Invalid Response!");
                        }

                        break;

                    case "readGeneral":

                        System.out.println("\nHow many announcements do you want to see?");
                        numberOfAnnouncements = scanner.nextLine();

                        response = stub.readGeneral(Integer.parseInt(numberOfAnnouncements), this.seqNumber,
                                AsymmetricCrypto.wrapDigitalSignature(numberOfAnnouncements + this.seqNumber, this.clientPrivateKey), this.clientPublicKey);
                        this.seqNumber++; //this has to be incremented before the signature's validation, otherwise, the client's seqNumber may become < then the server's seqNumber if server signature invalid.

                        if(AsymmetricCrypto.validateDigitalSignature(response.getSignature(), this.serverPublicKey, response.getMessage())){
                            System.out.println(response.getMessage());
                        }else{
                            System.out.println("Invalid Response!");
                        }

                        break;

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
