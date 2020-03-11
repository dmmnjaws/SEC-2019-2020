package sec.projeto.library;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientAPI extends Remote {

    public String hello();

    public void register();

    public void post();

    public void postGeneral();

    public void read();

    public void readGeneral();

}
