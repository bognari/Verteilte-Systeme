package vsue.rmi;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author Stephan
 * 
 */
public class VSBoardImpl implements VSBoard {

  /**
   * Liste für die Nachrichten
   */
  private final LinkedList<VSBoardMessage>  messages;
  /**
   * Liste für die Abonenten
   */
  private final LinkedList<VSBoardListener> listeners;

  /**
   * @throws RemoteException
   */
  public VSBoardImpl() throws RemoteException {
    this.messages = new LinkedList<VSBoardMessage>();
    this.listeners = new LinkedList<VSBoardListener>();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.tu_bs.vsBoard.VSBoard#get(int)
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
   * @see de.tu_bs.vsBoard.VSBoard#listen(de.tu_bs.vsBoard.VSBoardListener)
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
   * @see de.tu_bs.vsBoard.VSBoard#post(de.tu_bs.vsBoard.VSBoardMessage)
   */
  @Override
  public void post(final VSBoardMessage message) throws RemoteException {
    synchronized (this.messages) {
      this.messages.add(0, message);
    }

    synchronized (this.listeners) {
      final Iterator<VSBoardListener> iter = this.listeners.iterator();
      while (iter.hasNext()) {
        try {
          iter.next().newMessage(message);
        } catch (final RemoteException re) {
          iter.remove();
        }
      }
    }
  }
}
