package vsue.vsboard;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;

import vsue.faults.VSRPCsemantics;
import vsue.rpc.VSRemoteObjectManager;

/**
 * @author Stephan
 * 
 */
public class VSBoardServer {
  /**
	 * Name des VSBoard für die Veröffendlichung
	 */
  protected static final String  NAME  = "VSBoard";
  protected static final boolean debug = false;

  /**
   * @param args
   */
  public static void main(final String[] args) {
    VSRemoteObjectManager.setRPCSymmantic(VSRPCsemantics.AMO);
    final VSBoardServer server = new VSBoardServer();
    try {
      server.start();
      System.out.println("server is running");
    } catch (RemoteException | AlreadyBoundException e) {
      System.err.println("Server could not be started");
      e.printStackTrace();
    }
  }

  protected final VSBoard vsBoad;

  /**
   * Erstellt einen Server mit einem VSBoard
   */
  public VSBoardServer() {
    this.vsBoad = new VSBoardImpl();
  }

  /**
   * Startet den VSBoardServer
   * 
   * @throws RemoteException
   * @throws AlreadyBoundException
   * @throws ExportException
   */
  protected void start() throws RemoteException, AlreadyBoundException, ExportException {
    if (VSBoardServer.debug)
      System.out.println("board created");

    final VSBoard remBoard = (VSBoard) VSRemoteObjectManager.getInstance().exportObject(this.vsBoad);
    if (VSBoardServer.debug)
      System.out.println("board exported");

    LocateRegistry.createRegistry(Registry.REGISTRY_PORT).rebind(VSBoardServer.NAME, remBoard);
    if (VSBoardServer.debug)
      System.out.println("board in registry");
  }
}
