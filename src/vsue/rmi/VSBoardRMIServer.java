package vsue.rmi;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;

/**
 * @author Stephan
 * 
 */
public class VSBoardRMIServer {
  /**
	 * 
	 */
  static final int    TCP_PORT = 50000;
  /**
	 * 
	 */
  static final String NAME     = "VSBoard";

  /**
   * @param args
   */
  public static void main(final String[] args) {
    VSBoardRMIServer server = null;

    try {
      server = new VSBoardRMIServer();
    } catch (final RemoteException re) {
      System.err.println("Server konnte nicht gestartet werden.");
      re.printStackTrace();
      System.exit(0);
    }

    try {
      server.start();
      System.out.println("Server gestartet");
    } catch (RemoteException | AlreadyBoundException e) {
      System.err.println("Server konnte nicht gestartet werden.");
      e.printStackTrace();
    }
  }

  /**
	 * 
	 */
  private Registry registry;

  /**
   * @throws RemoteException
   */
  public VSBoardRMIServer() throws RemoteException {
  }

  /**
   * @throws RemoteException
   * @throws AlreadyBoundException
   * @throws ExportException
   */
  public void start() throws RemoteException, AlreadyBoundException, ExportException {
    final VSBoard board = new VSBoardImpl();

    // System.out.printf("Bin ich blöd? ... %b\n"
    // ,UnicastRemoteObject.unexportObject(board, true));

    final VSBoard remBoard = (VSBoard) UnicastRemoteObject.exportObject(board, VSBoardRMIServer.TCP_PORT);

    this.registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
    this.registry.rebind(VSBoardRMIServer.NAME, remBoard);
  }
}
