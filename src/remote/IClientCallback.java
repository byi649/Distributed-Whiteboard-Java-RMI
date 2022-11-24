// Benjamin Yi - 1152795

package remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * RMI interface for callback client object.
 */
public interface IClientCallback extends Remote {
    String getUsername() throws RemoteException;
    String getFullUsername() throws RemoteException;
    void setId(Integer id) throws RemoteException;
    Integer getId() throws RemoteException;
    void updateWhiteBoard(ArrayList<IShape> shapeList) throws RemoteException;
    void updateMessageBoard(ArrayList<String> messageList) throws RemoteException;
    void updatePeerList(ArrayList<String> peerList) throws RemoteException;
    boolean notifyNewPeer(IClientCallback client) throws RemoteException;
    void notifyFailure() throws RemoteException;
    void notifyKick() throws RemoteException;
    void notifyKill() throws RemoteException;
}
