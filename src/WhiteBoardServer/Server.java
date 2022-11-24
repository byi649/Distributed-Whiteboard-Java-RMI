// Benjamin Yi - 1152795

package WhiteBoardServer;

import remote.IRemoteWhiteBoard;

import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * The main class for the WhiteBoardServer package.
 * Takes port number as input and creates a registry, binding
 * to it with name "WhiteBoardServer".
 */
public class Server {

    public static void main(String[] args) {

        // Validate arguments
        if (args.length != 1) {
            System.err.println("Please enter one argument (Server port number)");
            System.exit(0);
        }

        int port = Integer.parseInt(args[0]);

        // Sanity check port number on start-up
        if (port < 1024 || port > 65535) {
            System.err.println("Please enter a port number between 1024 and 65535.");
            System.exit(0);
        }

        try {

            // Start servant class and registry
            IRemoteWhiteBoard remoteWhiteBoard = new RemoteWhiteBoardServant();
            Registry registry = LocateRegistry.createRegistry(port);
            registry.bind("WhiteBoardServer", remoteWhiteBoard);
            System.out.println("Server ready");

            // Clean-up before shutdown by unexporting and unbinding to RMI server
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down server ...");
                try {
                    UnicastRemoteObject.unexportObject(remoteWhiteBoard, true);
                } catch (NoSuchObjectException e) {
                    e.printStackTrace();
                }
                try {
                    registry.unbind("WhiteBoardServer");
                } catch (RemoteException | NotBoundException e) {
                    e.printStackTrace();
                }
            }));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
