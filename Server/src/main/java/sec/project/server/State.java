package sec.project.server;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.Map;

public class State implements Serializable {
    private Map<PublicKey,ClientLibrary> clientList;
    private GeneralBoard generalBoard;

    public State(Map<PublicKey,ClientLibrary> clientList, GeneralBoard generalBoard){
        this.clientList = clientList;
        this.generalBoard = generalBoard;
    }

    public Map<PublicKey, ClientLibrary> getClientList() {
        return this.clientList;
    }

    public GeneralBoard getGeneralBoard() {
        return this.generalBoard;
    }
}
