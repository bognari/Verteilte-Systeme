package vsue.rpc;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Stephan
 * 
 */
public interface VSBoardListener extends Remote {
  /**
   * @param message
   * @throws RemoteException
   */
  public void newMessage(VSBoardMessage message) throws RemoteException;
}
