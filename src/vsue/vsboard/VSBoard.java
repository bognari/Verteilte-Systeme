package vsue.vsboard;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Stephan
 * 
 */
public interface VSBoard extends Remote {
  /**
   * Gibt die neutes n Nachrichten zurück
   * 
   * @param n
   * @return
   * @throws IllegalArgumentException n < 0
   * @throws RemoteException
   */
  public VSBoardMessage[] get(int n) throws IllegalArgumentException, RemoteException;

  /**
   * Registriert sich als Listener am Board
   * 
   * @param listener
   * @throws RemoteException
   */
  public void listen(VSBoardListener listener) throws RemoteException;

  /**
   * Sendet eine Nachricht an das Board und verteilt diese an alle Listener
   * 
   * @param message
   * @throws RemoteException
   */
  public void post(VSBoardMessage message) throws RemoteException;
}
