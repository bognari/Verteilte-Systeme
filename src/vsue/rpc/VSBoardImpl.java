package vsue.rpc;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Stephan
 * 
 */
public class VSBoardImpl implements VSBoard {
  /**
   * Liste für die Nachrichten
   */
  private final ArrayList<VSBoardMessage>  messages;
  /**
   * Liste für die Abonenten
   */
  private final ArrayList<VSBoardListener> listeners;

  /**
   * @throws RemoteException
   */
  public VSBoardImpl() throws RemoteException {
    this.messages = new ArrayList<VSBoardMessage>();
    this.listeners = new ArrayList<VSBoardListener>();
  }

  /*
   * (non-Javadoc)
   * 
   * @see vsue.rpc.VSBoard#get(int)
   */
  @Override
  public VSBoardMessage[] get(int n) throws IllegalArgumentException, RemoteException {
    if (n < 0) {
      throw new IllegalArgumentException("der Index von get()  muss größergleich als 0 sein");
    }
    if (this.messages.isEmpty()) {
      return new VSBoardMessage[0];
    }
    if (n > this.messages.size()) {
      n = this.messages.size();
    }
    VSBoardMessage[] ret = new VSBoardMessage[0];
    synchronized (this.messages) {
      ret = this.messages.subList(0, n).toArray(ret);
    }
    return ret;
  }

  /*
   * (non-Javadoc)
   * 
   * @see vsue.rpc.VSBoard#listen(vsue.rpc.VSBoardListener)
   */
  @Override
  public void listen(final VSBoardListener listener) throws RemoteException {
    if (listener == null) {
      throw new IllegalArgumentException("kein null bei Listener erlaubt");
    }
    synchronized (this.listeners) {
      this.listeners.add(listener);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see vsue.rpc.VSBoard#post(vsue.rpc.VSBoardMessage)
   */
  @Override
  public void post(final VSBoardMessage message) throws RemoteException {
    synchronized (this.messages) {
      this.messages.add(0, message);
    }

    synchronized (this.listeners) {
      final Iterator<VSBoardListener> iter = this.listeners.iterator();
      while (iter.hasNext()) {
        final VSBoardListener listerner = iter.next();

        System.out.println("send to " + listerner);
        try {
          listerner.newMessage(message);
        } catch (final RemoteException re) {
          System.out.println("remove zombie" + listerner);
          iter.remove();
        }
      }
    }
  }
}
