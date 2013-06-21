package vsue.faults;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Stephan
 */
public class VSBoardImpl implements VSBoard {
  /**
   * Liste für die Nachrichten
   */
  private final Set<VSBoardMessage>  messages;
  /**
   * Liste für die Abonenten
   */
  private final Set<VSBoardListener> listeners;

  /**
   * @throws RemoteException
   */
  public VSBoardImpl() throws RemoteException {
    this.messages = new LinkedHashSet<VSBoardMessage>();
    this.listeners = new HashSet<VSBoardListener>();
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

    int size = 0;
    VSBoardMessage[] allMessages = null;
    synchronized (this.messages) {
      size = this.messages.size();
      if (n > size) {
        n = size;
      }
      allMessages = this.messages.toArray(new VSBoardMessage[size]);
    }

    final VSBoardMessage[] ret = new VSBoardMessage[n];

    for (int i = 0; i < n; i++) {
      ret[i] = allMessages[size - 1 - i];
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
      this.messages.add(message);
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
