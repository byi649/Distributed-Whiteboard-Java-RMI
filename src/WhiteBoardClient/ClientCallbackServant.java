// Benjamin Yi - 1152795

package WhiteBoardClient;

import remote.IClientCallback;
import remote.IShape;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Client interface with RMI server.
 * Passes messages through to the GUI.
 */
public class ClientCallbackServant extends UnicastRemoteObject implements IClientCallback {
    private ClientGUI clientGUI;
    private Integer id = null;
    private String username;

    protected ClientCallbackServant(String username, ClientGUI clientGUI) throws RemoteException {
        this.clientGUI = clientGUI;
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public String getFullUsername() {
        return username + "#" + id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void updateWhiteBoard(ArrayList<IShape> shapeList) throws RemoteException {
        clientGUI.updateWhiteBoard(shapeList);
    }

    public void updateMessageBoard(ArrayList<String> messageList) throws RemoteException {
        clientGUI.updateMessageBoard(messageList);
    }

    public void updatePeerList(ArrayList<String> peerList) throws RemoteException {
        clientGUI.updatePeerList(peerList);
    }

    public boolean notifyNewPeer(IClientCallback c) throws RemoteException {
        return clientGUI.notifyNewPeer(c);
    }

    public void notifyFailure() throws RemoteException {
        clientGUI.notifyFailure();
    }

    public void notifyKick() throws RemoteException {
        clientGUI.notifyKick();
    }

    public void notifyKill() throws RemoteException {
        clientGUI.notifyKill();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ClientCallbackServant that = (ClientCallbackServant) o;
        return id.equals(that.id) &&
                clientGUI.equals(that.clientGUI) &&
                username.equals(that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), clientGUI, id, username);
    }
}
