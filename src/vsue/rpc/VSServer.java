package vsue.rpc;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import vsue.communication.VSObjectConnection;

/**
 * nimmt alle Anfragen entgegen und weist einem Worker diese zu zum Abarbeiten.
 * 
 * @author Stephan
 * 
 */
public class VSServer implements Runnable {

  /**
   * arbeitet die Anfrage ab
   * 
   * @author Stephan
   * 
   */
  class VSServerWorker implements Runnable {
    private final VSObjectConnection connect;
    private final VSServer           server;

    /**
     * Bekommt den Socket zum Antworten und den Server
     * 
     * @param socket
     * @param server
     */
    public VSServerWorker(final Socket socket, final VSServer server) {
      this.connect = new VSObjectConnection(socket);
      this.server = server;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
      System.out.println("connected with client");
      try {
        // Request lesen
        final VSPackageRequest request = (VSPackageRequest) this.connect.receiveObject();
        // Methode aufrufen und ret versenden
        final Object ret = this.server.manager.invokeMethod(request.getObjID(), request.getMethod(), request.getArgs());
        this.connect.sendObject(new VSPackageReply(ret));
      } catch (ClassNotFoundException | IOException e) {
        e.printStackTrace();
      } catch (final InvocationTargetException e) {
        e.printStackTrace();
        try {
          this.connect.sendObject(new VSPackageReply(e.getTargetException()));
        } catch (final IOException e1) {
          // e1.printStackTrace();
        }
      } catch (IllegalAccessException | IllegalArgumentException e) {
        e.printStackTrace();
      } finally {
        System.out.println("finish with working");
      }
    }
  }

  private final VSRemoteObjectManager manager;

  private final ExecutorService       pool;

  public VSServer(final VSRemoteObjectManager manager) {
    this.manager = manager;
    this.pool = Executors.newCachedThreadPool();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run() {
    Socket connectSocket;
    while (true) {
      try {
        System.out.println("waiting for connection");
        connectSocket = this.manager.getSocket().accept();
        this.pool.execute(new VSServerWorker(connectSocket, this));
      } catch (final IOException e) {
        System.err.println("fail to connect with client");
        e.printStackTrace();
      }
    }
  }
}
