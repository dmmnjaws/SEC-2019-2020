package sec.project.client;

import sec.project.library.ClientAPI;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Scanner;

public class Client {

    private KeyPairGenerator keyGen;
    private KeyPair keyPair;
    private PrivateKey clientPrivateKey;
    private PublicKey clientPublicKey;
    private PublicKey serverPublicKey;
    ClientAPI stub;

    public Client (ClientAPI stub) {

        try {

            this.keyGen = KeyPairGenerator.getInstance("DSA", "SUN");
            this.keyPair = keyGen.generateKeyPair();
            this.clientPrivateKey = keyPair.getPrivate();
            this.clientPublicKey = keyPair.getPublic();
            this.serverPublicKey = getPublicKey("Client/data/keys/server_public_key.der");
            System.out.println(serverPublicKey.toString());

        } catch (Exception e) {

            e.printStackTrace();

        }

        this.stub = stub;
    }

    public void execute() {

        try{
            Scanner scanner = new Scanner(System.in);
            while(true){

                String command = scanner.nextLine();
                String[] tokens = command.split(" ");
                String message;

                switch(tokens[0]){

                    case "register":

                        stub.register(this.clientPublicKey);
                        break;

                    case "post":

                        message = command.substring(command.indexOf("(") + 1);
                        message = message.substring(0, message.indexOf(")"));
                        if(message.length() < 255) {
                            stub.post(this.clientPublicKey, message);
                        }
                        else{
                            System.out.println("The message can only have 255 chars");
                        }
                        break;

                    case "postGeneral":

                        message = command.substring(command.indexOf("(") + 1);
                        message = message.substring(0, message.indexOf(")"));
                        if(message.length() < 255) {
                            stub.postGeneral(this.clientPublicKey, message);
                        }
                        else{
                            System.out.println("The message can only have 255 chars");
                        }
                        break;

                    case "read":

                        stub.read(this.clientPublicKey, Integer.parseInt(tokens[1]));
                        break;

                    case "readGeneral":

                        stub.readGeneral(Integer.parseInt(tokens[1]));
                        break;

                }
            }

        } catch ( Exception e ){
            e.printStackTrace();
        }
    }

    public static PublicKey getPublicKey(String filename) throws Exception {

        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");

        return kf.generatePublic(spec);
    }
}
