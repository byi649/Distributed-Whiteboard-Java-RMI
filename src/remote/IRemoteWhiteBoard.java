// Benjamin Yi - 1152795

package remote;

import java.awt.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * RMI interface for server whiteboard object.
 */
public interface IRemoteWhiteBoard extends Remote {
    void register(IClientCallback client) throws RemoteException;
    void unregister(IClientCallback client) throws RemoteException;
    void drawShape(int x1, int y1, int x2, int y2, Color colour, IShape.ShapeType shape) throws RemoteException;
    void drawText(int x1, int y1, Color colour, String text) throws RemoteException;
    void getShapeList(IClientCallback client) throws RemoteException;
    void clearAll() throws RemoteException;
    void replaceAll(ArrayList<IShape> shapeList) throws RemoteException;
    void sendMessage(String message, IClientCallback client) throws RemoteException;
    void getMessageList(IClientCallback client) throws RemoteException;
    void getPeerList(IClientCallback client) throws RemoteException;
    void kickPeer(String clientName) throws RemoteException;
}
