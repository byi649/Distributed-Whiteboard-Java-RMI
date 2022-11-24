// Benjamin Yi - 1152795

package WhiteBoardServer;

import remote.IClientCallback;
import remote.IRemoteWhiteBoard;
import remote.IShape;

import java.awt.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

/**
 * RMI remote servant class.
 * All whiteboard operations go through this class.
 * Contains whiteboard operations and also maintains a copy of the whiteboard
 * state at all times through a shape array list.
 */
public class RemoteWhiteBoardServant extends UnicastRemoteObject implements IRemoteWhiteBoard {
    private ArrayList<IShape> shapeArrayList = new ArrayList<>();
    private ArrayList<IClientCallback> clientArrayList = new ArrayList<>();
    private ArrayList<String> messageArrayList = new ArrayList<>();
    private int idCounter = 0;
    private IClientCallback admin = null;

    protected RemoteWhiteBoardServant() throws RemoteException {}

    /**
     * Registers clients to the server.
     * Needed for peer list and client call back operations.
     * @param client callback client object
     * @throws RemoteException
     */
    public void register(IClientCallback client) throws RemoteException {
        if (idCounter == 0) {
            admin = client;
            addClient(client);
        } else {
            boolean accept = admin.notifyNewPeer(client);
            if (accept) {
                addClient(client);
            } else {
                client.notifyFailure();
            }
        }
    }

    /**
     * Used by register() to add client to client list.
     * Notifies all clients via callback object to update peer list.
     * @param c callback client object
     * @throws RemoteException
     */
    private void addClient(IClientCallback c) throws RemoteException {
        c.setId(idCounter);
        clientArrayList.add(c);
        idCounter++;
        notifyAllClients();
    }

    /**
     * Removes client from client list.
     * If called by admin, kills server
     * @param client callback client object
     * @throws RemoteException
     */
    public void unregister(IClientCallback client) throws RemoteException {
        if (client.getId() == 0) {
            killServer();
        } else {
            clientArrayList.remove(client);
            notifyAllClients();
        }
    }

    /**
     * Kills the server. Called only by admin.
     * @throws RemoteException
     */
    private void killServer() throws RemoteException {
        for (IClientCallback c : clientArrayList) {
            try {
                c.notifyKill();
            } catch (Exception ignored) {} // Connection resets, so ignore error
        }
        System.exit(0);
    }

    /**
     * Add a shape to the whiteboard. Then notifies all clients of the change.
     * @param x1 left coord
     * @param y1 upper coord
     * @param x2 right coord
     * @param y2 bottom coord
     * @param colour colour of shape
     * @param shape shape type
     * @throws RemoteException
     */
    public void drawShape(int x1, int y1, int x2, int y2, Color colour, IShape.ShapeType shape) throws RemoteException {
        shapeArrayList.add(new Shape(x1, y1, x2-x1, y2-y1, colour, shape));
        notifyAllClients();
    }

    /**
     * Adds a text string to the whiteboard. Then notifies all clients of the
     * change.
     * @param x1 x pos of string
     * @param y1 y pos of string
     * @param colour colour of text
     * @param text string
     * @throws RemoteException
     */
    public void drawText(int x1, int y1, Color colour, String text) throws RemoteException {
        shapeArrayList.add(new Shape(x1, y1, colour, text));
        notifyAllClients();
    }

    /**
     * Updates whiteboard for a client
     * @param c callback client object
     * @throws RemoteException
     */
    public void getShapeList(IClientCallback c) throws RemoteException {
        c.updateWhiteBoard(shapeArrayList);
    }

    /**
     * Clears the whiteboard.
     * Called by admin in "new"
     * @throws RemoteException
     */
    public void clearAll() throws RemoteException {
        shapeArrayList.clear();
        notifyAllClients();
    }

    /**
     * Loads in a saved whiteboard.
     * Called by admin in "open"
     * @param shapeList saved whiteboard
     * @throws RemoteException
     */
    public void replaceAll(ArrayList<IShape> shapeList) throws RemoteException {
        shapeArrayList = shapeList;
        notifyAllClients();
    }

    /**
     * Send a message to chat. Then notifies all clients.
     * @param message string messaged always prefixed by username:
     * @param c callback client object
     * @throws RemoteException
     */
    public void sendMessage(String message, IClientCallback c) throws RemoteException {
        messageArrayList.add(message);
        notifyAllClients();
    }

    /**
     * Kicks a peer from the whiteboard. Then notifies all clients.
     * Called by admin via "/kick" text command through chat.
     * @param clientName full name (including identifier eg JSmith#23) of kicked
     * @throws RemoteException
     */
    public void kickPeer(String clientName) throws RemoteException {
        for (IClientCallback c : clientArrayList) {
           if (c.getFullUsername().equals(clientName.trim()) && c.getId() != 0) {
               c.notifyKick();
               notifyAllClients();
           }
        }
    }

    /**
     * Updates chat for a client.
     * @param c callback client object
     * @throws RemoteException
     */
    public void getMessageList(IClientCallback c) throws RemoteException {
        c.updateMessageBoard(messageArrayList);
    }

    /**
     * Generate human-readable peer list from internal peer list
     * @return arraylist of full usernames including identifier
     * @throws RemoteException
     */
    private ArrayList<String> makePeerList() throws RemoteException {
        ArrayList<String> peerList = new ArrayList<>();
        for (IClientCallback c : clientArrayList) {
            peerList.add(c.getFullUsername());
        }
        return peerList;
    }

    /**
     * Updates peer list for a client.
     * @param c callback client object
     * @throws RemoteException
     */
    public void getPeerList(IClientCallback c) throws RemoteException {
        c.updatePeerList(makePeerList());
    }

    /**
     * Full updates all clients.
     * Updates whiteboard, chat, and peer list.
     * @throws RemoteException
     */
    private void notifyAllClients() throws RemoteException {
        for (IClientCallback c : clientArrayList) {
            c.updateWhiteBoard(shapeArrayList);
            c.updateMessageBoard(messageArrayList);
            c.updatePeerList(makePeerList());
        }
    }
}
