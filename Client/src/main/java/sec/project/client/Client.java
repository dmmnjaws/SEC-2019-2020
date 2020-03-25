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
    ClientAPI stub;
    Scanner scanner;
    String clientNumber;
    String serverNumber;

    public Client (ClientAPI stub) {

        this.scanner = new Scanner(System.in);
        System.out.println("Insert the client number:");
        this.clientNumber = scanner.nextLine();
        System.out.println("Insert the server number you want to connect to:");
        this.serverNumber = scanner.nextLine();

        try {

            this.clientPrivateKey = AsymmetricCrypto.getPrivateKey("data/keys/client" + clientNumber + "_private_key.der");
            this.clientPublicKey = AsymmetricCrypto.getPublicKey("data/keys/client" + clientNumber + "_public_key.der");
            this.serverPublicKey = AsymmetricCrypto.getPublicKey("data/keys/server" + serverNumber + "_public_key.der");
            System.out.println(serverPublicKey.toString());

        } catch (Exception e) {

            e.printStackTrace();

        }

        this.stub = stub;
    }

    public void execute() {

        while (true) {

            String command = scanner.nextLine();
            String[] tokens = command.split(" ");
            String message;
            byte[] signature;

            try {
                switch (tokens[0]) {
                    case "register":

                        signature = AsymmetricCrypto.wrapDigitalSignature(this.clientNumber, this.clientPrivateKey);
                        System.out.println(signature);
                        stub.register(this.clientPublicKey, this.clientNumber, signature);

                        System.out.println("Successful registration");
                        break;

                    case "post":

                        System.out.println("Write your announcement:");
                        message = System.console().readLine() + "|";
                        if (message.length() > 255) {
                            System.out.println("Message is too long! Failed post");
                            break;
                        }

                        System.out.println("Any references? Insert like id1 id2 id3. If none just press enter");
                        message += System.console().readLine();

                        signature = AsymmetricCrypto.wrapDigitalSignature(message, this.clientPrivateKey);
                        System.out.println(signature);
                        stub.post(this.clientPublicKey, message, signature);

                        System.out.println("Successfully posted");
                        break;

                    case "postGeneral":

                        message = command.substring(command.indexOf(" ") + 1, command.length());

                        if (message.length() < 255) {
                            stub.postGeneral(this.clientPublicKey, message);
                        } else {
                            System.out.println("The message can only have 255 chars");
                        }
                        break;

                    case "read":

                        System.out.println(stub.read(this.clientPublicKey, Integer.parseInt(tokens[1])));
                        break;

                    case "readGeneral":

                        System.out.println(stub.readGeneral(Integer.parseInt(tokens[1])));
                        break;

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
