package vsue.rpc;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import vsue.communication.VSObjectConnection;
import vsue.faults.VSBuggyObjectConnection;
import vsue.faults.VSRPCsemantics;

public class VSInvocationHandler implements Serializable, InvocationHandler {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * Dieser Worker versucht eine Anwort zu erhalten
   * 
   * @author Stephan
   *
   */
  class VSReciveWorker implements Callable<VSPackageReply> {
    private final VSObjectConnection conn;

    /**
     * Erstellt einen Worker
     * 
     * @param conn
     */
    public VSReciveWorker(final VSObjectConnection conn) {
      this.conn = conn;
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public VSPackageReply call() throws Exception {
      return ((VSPackageReply) this.conn.receiveObject());
    }
  }

  /**
   * Status für eine Antwort
   * 
   * @author Stephan
   *
   */
  enum VSReplyStatus {
    CONNECTION_LOST, OLD, TIMEOUT, INITIAL, SUCCSESS,
  }

  /**
   * Reference zum Remoteobject
   */
  private final VSRemoteReference remoteReference;
  /**
   * Maximal Anzahl der Versuche bis zur RemoteException
   */
  final static byte               MAX_TRYS = 16;

  /**
   * Timeout für die Verbindung
   */
  final static int                TIMEOUT  = 50;

  final static boolean            DEBUG    = false;

  /**
   * Erstellt einen InvocationHandler
   * @param remoteReference
   */
  public VSInvocationHandler(final VSRemoteReference remoteReference) {
    this.remoteReference = remoteReference;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
   * java.lang.reflect.Method, java.lang.Object[])
   */
  @Override
  public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
    if (Remote.class.isAssignableFrom(method.getDeclaringClass())) {
      // nur fernaufruf wenn die methode von da kommt
      byte tryCounter = 0;
      if (args != null)
        for (int i = 0; i < args.length; i++)
          if (Remote.class.isAssignableFrom(args[i].getClass())) {
            // testen ob Parameter Remote Objects sind. (nur erste
            // Interation)
            final Remote argProxy = VSRemoteObjectManager.getInstance().getProxy((Remote) args[i]);
            if (argProxy != null)
              args[i] = argProxy;
          }

      final ExecutorService pool = Executors.newFixedThreadPool(1);
      Future<VSPackageReply> future = null;
      VSPackageReply reply = null;
      VSObjectConnection conn = null;
      Socket socket = null;
      VSPackageRequest request = null;
      VSReplyStatus status = VSReplyStatus.INITIAL;
      int timeout = 0;

      newTry: do {
        if (tryCounter >= VSInvocationHandler.MAX_TRYS) {
          // mehr als 16 Versuche
          pool.shutdownNow();
          throw new RemoteException("more then 16 connection errors");
        }

        switch (status) {
          case INITIAL: {
            tryCounter = 1;
            timeout = VSInvocationHandler.TIMEOUT;
            request = new VSPackageRequest(this.remoteReference.getObjectID(), method, args);
          }
          case CONNECTION_LOST: {
            try {
              socket = new Socket(this.remoteReference.getHost(), this.remoteReference.getPort());
            } catch (final UnknownHostException ue) {
              throw new RemoteException("unknown server");
            } catch (final IOException ioe) {
              throw new RemoteException("could not connect to server");
            }
            conn = new VSBuggyObjectConnection(socket);
          }
          case OLD: {
            future = pool.submit(new VSReciveWorker(conn));
          }
          case TIMEOUT: {
            try {
              conn.sendObject(request);
            } catch (final SocketException se) {
              status = VSReplyStatus.CONNECTION_LOST;
              continue newTry;
            }
          }
          case SUCCSESS: {
          }
        }

        // versuche eine antwort zu bekommen
        try {
          reply = future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (final TimeoutException te) { // laeuft weiter bis er
                                              // endet
                                              // oder beendet wurde
          if (DEBUG)
            System.err.println("TIMEOUT");
          status = VSReplyStatus.TIMEOUT;
          request.age();
          tryCounter++;
          if (tryCounter < 10)
            timeout = VSInvocationHandler.TIMEOUT * tryCounter;
          continue newTry;
        } catch (final ExecutionException ee) {
          if (DEBUG)
            System.err.println(ee.getMessage());
          status = VSReplyStatus.CONNECTION_LOST;
          try {
            socket.close();
          } catch (final IOException ioe) {
            // wayne
          }
          request.age();
          tryCounter++;
          continue newTry;
        }

        // teste ob es die "richtige" antwort ist
        if (!reply.getRPC().equals(VSRPCsemantics.LOM) || (reply.getRequestAge() == request.getAge())) {
          status = VSReplyStatus.SUCCSESS;
          break newTry;
        } else {
          if (DEBUG)
            System.err.printf("OLD REPLY recive: %d,\t expected: %d\n", reply.getRequestAge(), request.getAge());
          status = VSReplyStatus.OLD;
          continue newTry;
        }

      } while (status != VSReplyStatus.SUCCSESS);
      if (DEBUG)
        System.out.printf("accept reply\n");
      socket.close();

      if ((reply.getObj() != null) && Throwable.class.isInstance(reply.getObj()))
        // testen ob ret eine Exception ist
        throw (Throwable) reply.getObj();
      return reply.getObj();
    } else
      // alle aufrufe auf den Proxy erzeugen einen "Kreis", deswegen wird
      // die RemoteReference benutzt
      return method.invoke(this.remoteReference, args);
  }
}
