package sec.project.client;

import sec.project.library.ClientAPI;

import java.security.*;

public class Client {

    private KeyPairGenerator keyGen;
    private KeyPair keyPair;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    ClientAPI stub;

    public Client (ClientAPI stub) {

        try {

            this.keyGen = KeyPairGenerator.getInstance("DSA", "SUN");
            this.keyPair = keyGen.generateKeyPair();
            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();

        } catch (NoSuchAlgorithmException e) {

            e.printStackTrace();

        } catch (NoSuchProviderException e) {

            e.printStackTrace();

        }

        this.stub = stub;
    }

    public void execute(){

        try{

            while(true){

                String command = System.console().readLine();

                switch(command){

                    case "register":
                        stub.register();
                        
                    case "post":


                }
            }

        } catch ( Exception e ){
            e.printStackTrace();
        }
    }
}
