package sec.project.server;

import org.javatuples.Pair;
import org.javatuples.Triplet;
import sec.project.library.AsymmetricCrypto;
import sec.project.library.ClientAPI;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public class DoubleEchoBroadcaster implements Serializable {

    private PrivateKey serverPrivateKey;

    private ClientLibrary clientLibrary;
    private boolean sentEcho;
    private boolean sentReady;
    private boolean delivered;
    private Map<PublicKey, Triplet<Integer, String, byte[]>> echoes; /* Overhead the albatross... */
    private Map<Pair<Integer, String>, Integer> echoMessagesCount;
    private Triplet<Integer, String, byte[]> echoedMessage;
    private Map<PublicKey, Triplet<Integer, String, byte[]>> readys;
    private Map<Pair<Integer, String>, Integer> readyMessagesCount;
    private Triplet<Integer, String, byte[]> readyedMessage;

    public DoubleEchoBroadcaster(ClientLibrary clientLibrary){
        this.clientLibrary = clientLibrary;
    }

    public Triplet<Integer, String, byte[]> write(Triplet <Integer, String, byte[]> valueTriplet, PrivateKey serverPrivateKey, PublicKey serverPublicKey) throws UnsupportedEncodingException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, RemoteException {

        System.out.println("DEBUG: Server received broadcast request. Args: " + AsymmetricCrypto.transformTripletToString(valueTriplet));
        this.serverPrivateKey = serverPrivateKey;
        this.echoes = new HashMap<>();
        this.echoMessagesCount = new HashMap<>();
        this.readys = new HashMap<>();
        this.readyMessagesCount = new HashMap<>();
        this.sentEcho = false;
        this.sentReady = false;
        this.delivered = false;

        if(!this.sentEcho){
            this.sentEcho = true;
            for (Map.Entry<PublicKey, ClientAPI> stub : this.clientLibrary.getStubs().entrySet()){
                stub.getValue().echo(this.clientLibrary.getClientPublicKey(), valueTriplet, AsymmetricCrypto.wrapDigitalSignature(
                        this.clientLibrary.getClientPublicKey() + AsymmetricCrypto.transformTripletToString(valueTriplet), this.serverPrivateKey), serverPublicKey);
            }

        }

        while(!delivered){ }

        System.out.println("DEBUG: Server completed READY phase." );
        System.out.println("DEBUG: Server completed broadcast request.");

        return readyedMessage;

    }

    public synchronized void echo(PublicKey clientPublicKey, Triplet<Integer, String, byte[]> message, byte[] signature, PublicKey serverPublicKey) throws UnsupportedEncodingException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, RemoteException {
        System.out.println("DEBUG: Server received ECHO. Args: " + AsymmetricCrypto.transformTripletToString(message));

        if (this.clientLibrary.getStubs().containsKey(serverPublicKey) && AsymmetricCrypto.validateDigitalSignature(signature, serverPublicKey,
                clientPublicKey + AsymmetricCrypto.transformTripletToString(message)) && this.echoes.get(serverPublicKey) == null) {


            System.out.println("DEBUG: Server validated ECHO signature.");

            this.echoes.put(serverPublicKey, message);
            Pair<Integer, String> rawMessage = new Pair<>(message.getValue0(), message.getValue1());

            if(this.echoMessagesCount.get(rawMessage) == null){
                this.echoMessagesCount.put(rawMessage, 1);
            } else {
                int count = this.echoMessagesCount.get(rawMessage);
                this.echoMessagesCount.put(rawMessage, count + 1);
            }

            System.out.println("DEBUG: State of the server's ECHO message count: " + this.echoMessagesCount);

            if(this.echoMessagesCount.get(rawMessage) > (this.clientLibrary.getStubs().size() + (this.clientLibrary.getStubs().size() / 3)) / 2 && this.sentReady == false){
                this.sentReady = true;
                this.echoedMessage = message;
                for (Map.Entry<PublicKey, ClientAPI> stub : this.clientLibrary.getStubs().entrySet()){
                    stub.getValue().ready(this.clientLibrary.getClientPublicKey(), message, AsymmetricCrypto.wrapDigitalSignature(
                            this.clientLibrary.getClientPublicKey() + AsymmetricCrypto.transformTripletToString(message), this.serverPrivateKey), serverPublicKey);

                    System.out.println("DEBUG: Server sent READY from ECHO. Args: " + this.clientLibrary.getClientPublicKey() + " / " + AsymmetricCrypto.transformTripletToString(message));
                }
            }
        }
    }

    public synchronized void ready(PublicKey clientPublicKey, Triplet<Integer, String, byte[]> message, byte[] signature, PublicKey serverPublicKey) throws UnsupportedEncodingException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, RemoteException {
        System.out.println("DEBUG: Server received READY. Args: " + clientPublicKey + " / " + AsymmetricCrypto.transformTripletToString(message));

        if (this.clientLibrary.getStubs().containsKey(serverPublicKey) && AsymmetricCrypto.validateDigitalSignature(signature, serverPublicKey,
                clientPublicKey + AsymmetricCrypto.transformTripletToString(message)) && this.readys.get(serverPublicKey) == null) {

            System.out.println("DEBUG: Server validated READY signature.");

            this.readys.put(serverPublicKey, message);
            Pair<Integer, String> rawMessage = new Pair<>(message.getValue0(), message.getValue1());

            if(this.readyMessagesCount.get(rawMessage) == null){
                this.readyMessagesCount.put(rawMessage, 1);
            } else {
                int count = this.readyMessagesCount.get(rawMessage);
                this.readyMessagesCount.put(rawMessage, count + 1);
            }

            System.out.println("DEBUG: State of the server's READY message count: " + this.readyMessagesCount);

            if(this.readyMessagesCount.get(rawMessage) > 2 * (this.clientLibrary.getStubs().size() / 3) && this.delivered == false){
                this.readyedMessage = message;
                this.delivered = true;
                return;
            }

            if(this.readyMessagesCount.get(rawMessage) > (this.clientLibrary.getStubs().size() / 3) && this.sentReady == false){
                this.sentReady = true;
                this.readyedMessage = message;
                for (Map.Entry<PublicKey, ClientAPI> stub : this.clientLibrary.getStubs().entrySet()){
                    stub.getValue().ready(this.clientLibrary.getClientPublicKey(), message, AsymmetricCrypto.wrapDigitalSignature(
                            this.clientLibrary.getClientPublicKey() + AsymmetricCrypto.transformTripletToString(message), this.serverPrivateKey), serverPublicKey);
                }

            }
        }
    }
}
