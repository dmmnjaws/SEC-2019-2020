package sec.project.server;

import sec.project.library.AsymmetricCrypto;
import sec.project.library.ClientAPI;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Hello world!
 *
 */
public class Server implements ClientAPI {

    private PrivateKey serverPrivateKey;
    private PublicKey serverPublicKey;

    public Server (){

        try {

            serverPrivateKey = AsymmetricCrypto.getPrivateKey("data/keys/server_private_key.der");
            System.out.println(serverPrivateKey.toString());
            serverPublicKey = AsymmetricCrypto.getPublicKey("data/keys/server_public_key.der");
            System.out.println(serverPublicKey.toString());

        } catch (Exception e) {

            e.printStackTrace();

        }
    }

    @Override
    public void register(PublicKey clientPublicKey) throws RemoteException {

        System.err.println( "Client called register() method." );

    }

    @Override
    public void post(PublicKey clientPublicKey, String message) throws RemoteException {

        System.err.println( "Client called post() method." );

    }

    @Override
    public void postGeneral(PublicKey clientPublicKey, String message) throws RemoteException {

        System.err.println( "Client called postGeneral() method." );

    }

    @Override
    public void read(PublicKey clientPublicKey, int number) throws RemoteException {

        System.err.println( "Client called read() method." );

    }

    @Override
    public void readGeneral(int number) throws RemoteException {

        System.err.println( "Client called readGeneral() method." );

    }

}
