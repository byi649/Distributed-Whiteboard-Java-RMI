// Benjamin Yi - 1152795

package WhiteBoardClient;

import remote.IClientCallback;
import remote.IRemoteWhiteBoard;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * The main class for the WhiteBoardClient package.
 * Takes server ip, port number, and username as inputs.
 * Starts up the GUI if connection is successful.
 */

public class Client extends UnicastRemoteObject {

    protected Client() throws RemoteException {}

    public static void main(String[] args) {

        if (args.length != 3) {
            System.err.println("Please enter two arguments (Server IP, Server port number, username)");
            System.exit(0);
        }

        int port = Integer.parseInt(args[1]);

        // Sanity check port number on start-up
        if (port < 1024 || port > 65535) {
            System.err.println("Please enter a port number between 1024 and 65535.");
            System.exit(0);
        }

        try {

            // Connect to RMI registry
            Registry registry = LocateRegistry.getRegistry(args[0], port);
            IRemoteWhiteBoard remoteWhiteBoard = (IRemoteWhiteBoard) registry.lookup("WhiteBoardServer");

            ClientGUI client = new ClientGUI(remoteWhiteBoard);
            IClientCallback clientCallbackServant = new ClientCallbackServant(args[2], client);
            System.out.println("Connecting to server ...");
            client.setServant(clientCallbackServant);

            client.pack();
            client.setVisible(true);

        } catch (Exception e) {
            System.err.println("Could not connect to server.");
        }

    }

}
