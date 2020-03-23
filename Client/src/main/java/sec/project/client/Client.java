package sec.project.client;

import sec.project.library.AsymmetricCrypto;
import sec.project.library.ClientAPI;

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
/*
        String test = "ola";
        try{
            String secret = AsymmetricCrypto.encryptText(test,this.clientPrivateKey);
            System.out.println(secret);
            String voila = AsymmetricCrypto.decryptText(secret,this.clientPublicKey);
            System.out.println(voila);
        }catch (Exception e){
            e.printStackTrace();
        }
*/
        try{

            while(true){

                String command = scanner.nextLine();
                String[] tokens = command.split(" ");
                String message;

                switch(tokens[0]){

                    case "register":

                        stub.register(this.clientPublicKey,command.substring(command.indexOf(" ") + 1, command.length()));
                        break;

                    case "post":

                        message = command.substring(command.indexOf(" ") + 1, command.length());

                        if(message.length() < 255) {
                            stub.post(this.clientPublicKey, message);
                        }
                        else{
                            System.out.println("The message can only have 255 chars");
                        }
                        break;

                    case "postGeneral":

                        message = command.substring(command.indexOf(" ") + 1, command.length());

                        if(message.length() < 255) {
                            stub.postGeneral(this.clientPublicKey, message);
                        }
                        else{
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
            }

        } catch ( Exception e ){
            e.printStackTrace();
        }
    }
}
