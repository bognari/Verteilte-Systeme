package vsue.faults;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Stephan
 * 
 */
public interface VSBoard extends Remote {
  /**
   * @param n
   * @return
   * @throws IllegalArgumentException
   * @throws RemoteException
   */
  public VSBoardMessage[] get(int n) throws IllegalArgumentException, RemoteException;

  /**
   * @param listener
   * @throws RemoteException
   */
  public void listen(VSBoardListener listener) throws RemoteException;

  /**
   * @param message
   * @throws RemoteException
   */
  public void post(VSBoardMessage message) throws RemoteException;
}
