// Benjamin Yi - 1152795

package remote;

import java.awt.*;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * RMI interface for Shape object.
 */
public interface IShape extends Remote {
    enum ShapeType {CIRCLE, OVAL, RECTANGLE, LINE, TEXT};
    int getX() throws RemoteException;
    int getY() throws RemoteException;
    int getWidth() throws RemoteException;
    int getHeight() throws RemoteException;
    Color getColour() throws RemoteException;
    String getText() throws RemoteException;
    ShapeType getShape() throws RemoteException;
}
