package vsue.rmi;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;

import vsue.vsboard.VSBoard;
import vsue.vsboard.VSBoardServer;

/**
 * @author Stephan
 * 
 */
public class VSBoardRMIServer extends VSBoardServer {

  /**
   * @param args
   */
  public static void main(final String[] args) {
    final VSBoardRMIServer server = new VSBoardRMIServer();

    try {
      server.start();
      System.out.println("Server gestartet");
    } catch (RemoteException | AlreadyBoundException e) {
      System.err.println("Server konnte nicht gestartet werden.");
      e.printStackTrace();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see vsue.vsboard.VSBoardServer#start()
   */
  @Override
  protected void start() throws RemoteException, AlreadyBoundException, ExportException {
    if (VSBoardServer.debug)
      System.out.println("board created");

    final VSBoard remBoard = (VSBoard) UnicastRemoteObject.exportObject(this.vsBoad);

    if (VSBoardServer.debug)
      System.out.println("board exported");

    LocateRegistry.createRegistry(Registry.REGISTRY_PORT).rebind(VSBoardServer.NAME, remBoard);
    if (VSBoardServer.debug)
      System.out.println("board in registry");
  }
}