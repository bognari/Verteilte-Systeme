package vsue.communication.test;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import vsue.communication.VSObjectConnection;

public class VSServer {
  public class VSServerHandler implements Runnable {
    private final Socket             socket;
    private final VSObjectConnection conn;

    public VSServerHandler(final Socket socket) throws SocketException {
      this.socket = socket;
      this.socket.setKeepAlive(true);
      this.conn = new VSObjectConnection(this.socket);
    }

    @Override
    public void run() {
      try {
        System.out.println("connected with client");
        final Serializable obj = this.conn.receiveObject();
        this.conn.sendObject(obj);
        this.socket.close();
      } catch (final ClassNotFoundException cnfe) {
        System.err.println("fail byte[] to obj");
        cnfe.printStackTrace();
      } catch (final IOException ioe) {
        System.err.println("fail echo");
        ioe.printStackTrace();
      }
    }
  }

  public static final int TCP_PORT = 50000;

  public static void main(final String[] args) {
    try {
      final VSServer server = new VSServer();
      System.out.println("server started");
      server.run();
    } catch (final IOException e) {
      System.err.println("fail to start server");
      e.printStackTrace();
    }
  }

  private final ServerSocket server;

  public VSServer() throws IOException {
    this.server = new ServerSocket(VSServer.TCP_PORT);
  }

  public void run() {
    Socket connecSocket;
    while (true)
      try {
        System.out.println("waiting for connection");
        connecSocket = this.server.accept();
        (new Thread(new VSServerHandler(connecSocket))).start();
      } catch (final IOException e) {
        System.err.println("fail to connect with client");
        e.printStackTrace();
      }
  }
}
