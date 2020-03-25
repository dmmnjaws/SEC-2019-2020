package sec.project.client;

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

    public Client (ClientAPI stub) {

        this.scanner = new Scanner(System.in);
        System.out.println("\nInsert the client number:");
        this.clientNumber = scanner.nextLine();
        System.out.println("\nInsert the server number you want to connect to:");
        this.serverNumber = scanner.nextLine();

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

                        signature = AsymmetricCrypto.wrapDigitalSignature(message, this.clientPrivateKey);
                        stub.post(this.clientPublicKey, message, signature);

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

                        signature = AsymmetricCrypto.wrapDigitalSignature(message, this.clientPrivateKey);
                        stub.postGeneral(this.clientPublicKey, message, signature);

                        System.out.println("\nSuccessfully posted.");
                        break;

                    case "read":
                        
                        System.out.println("\nWrite the number of the client whose announcement board you want to read:");
                        PublicKey toReadClientPublicKey = AsymmetricCrypto.getPublicKey("data/keys/client" + scanner.nextLine() + "_public_key.der");

                        System.out.println("\nHow many announcements do you want to see?");
                        numberOfAnnouncements = scanner.nextLine();

                        System.out.println(stub.read(toReadClientPublicKey, Integer.parseInt(numberOfAnnouncements)));
                        break;

                    case "readGeneral":

                        System.out.println("\nHow many announcements do you want to see?");
                        numberOfAnnouncements = scanner.nextLine();

                        System.out.println(stub.readGeneral(Integer.parseInt(numberOfAnnouncements)));
                        break;

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
