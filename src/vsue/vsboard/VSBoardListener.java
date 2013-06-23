package vsue.vsboard;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Stephan
 * 
 */
public interface VSBoardListener extends Remote {
  /**
   * Methode zum Anzeigen von neuen Nachrichten
   * 
   * @param message
   * @throws RemoteException
   */
  public void newMessage(VSBoardMessage message) throws RemoteException;
}
