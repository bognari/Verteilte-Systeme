package vsue.rpc;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import vsue.communication.VSObjectConnection;
import vsue.faults.VSBuggyObjectConnection;
import vsue.faults.VSRPCsemantics;

/**
 * nimmt alle Anfragen entgegen und weist einem Worker diese zu zum Abarbeiten.
 * 
 * @author Stephan
 * 
 */
public class VSServer implements Runnable {
  class VSRequestDBEntry {
    /**
     * ID des Request (hier die Systemzeit des Absenders)
     */
    private long   id;
    /**
     * Anzahl des Versuchs
     */
    private byte   age;
    /**
     * Der gespeicherte Antworts Inhalt
     */
    private Object ret;

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (this.getClass() != obj.getClass())
        return false;
      final VSRequestDBEntry other = (VSRequestDBEntry) obj;
      if (this.id != other.id)
        return false;
      return true;
    }

    /**
     * @return
     */
    public byte getAge() {
      return this.age;
    }

    /**
     * @return
     */
    public long getID() {
      return this.id;
    }

    /**
     * @return
     */
    public Object getRet() {
      return this.ret;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = (prime * result) + (int) (this.id ^ (this.id >>> 32));
      return result;
    }

    /**
     * @param age
     */
    public void setAge(final byte age) {
      this.age = age;
    }

    /**
     * @param id
     */
    public void setID(final long id) {
      this.id = id;
    }

    /**
     * @param ret
     */
    public void setRet(final Object ret) {
      this.ret = ret;
    }
  }

  /**
   * Enum für den Status eines Requests
   * 
   * @author Stephan
   */
  public enum VSRequestStatus {
    NEW_CLIENT, OLD_GENERATION, OLD_AGE, NEW_GENERATION, NEW_AGE
  }

  /**
   * arbeitet die Anfrage ab
   * 
   * @author Stephan
   * 
   */
  class VSRequestWorker implements Runnable {
    /**
     * Das Verbindungsobjekt zum Clienten
     */
    private final VSObjectConnection connect;
    /**
     * Der Socket
     */
    private final Socket             socket;
    /**
     * Gibt an, welche RPC Semantik bei der "Erstellung" aktiv war
     */
    private final VSRPCsemantics     rpc;
    private final static boolean     DEBUG = false;

    /**
     * Bekommt den Socket zum Antworten und den Server
     * 
     * @param socket
     * @param server
     */
    public VSRequestWorker(final Socket socket) {
      this.connect = new VSBuggyObjectConnection(socket);
      this.socket = socket;
      this.rpc = VSRemoteObjectManager.getRPCSymmantic();
    }

    /**
     * arbeitet die at moste once Semantik ab
     */
    private void amo(VSPackageRequest request) {
      final String ipAdress = this.socket.getInetAddress().getHostAddress();
      VSRequestDBEntry dbEntry = VSServer.requestLog.get(ipAdress + request.getSourceThread());
      VSRequestStatus status = null;

      /*
       * Zustand feststellen
       */
      if (dbEntry == null)
        status = VSRequestStatus.NEW_CLIENT;
      else if (request.getRequestTime() > dbEntry.getID())
        status = VSRequestStatus.NEW_GENERATION;
      else
        status = VSRequestStatus.OLD_GENERATION;

      /*
       * Zustand abarbeiten
       */
      switch (status) {
        case NEW_CLIENT: {
          if (DEBUG)
            System.out.printf("new client ip: %s ; thread: %d \n", ipAdress, request.getSourceThread());
          dbEntry = new VSRequestDBEntry();
          VSServer.requestLog.put(ipAdress + request.getSourceThread(), dbEntry);
        }
        case NEW_GENERATION: {
          if (DEBUG)
            System.out.printf("new generation: %d \n", request.getRequestTime());
          dbEntry.setID(request.getRequestTime());
          dbEntry.setRet(this.calculateResult(request));
        }
        case OLD_GENERATION: {
          this.sendReply(dbEntry.getRet(), request);
        }
        default: {
        }
      }
    }

    /**
     * Berechnet das Ergebnis für die Anfrage
     * 
     * @param request
     * @return
     */
    private Object calculateResult(final VSPackageRequest request) {
      Object ret = null;

      try {
        if (DEBUG)
          System.out.printf("invoke method: %s\n", request.getMethod());
        ret = VSRemoteObjectManager.getInstance().invokeMethod(request.getObjID(), request.getMethod(), request.getArgs());
      } catch (final InvocationTargetException e) {
        e.printStackTrace();
        ret = e.getTargetException();
      } catch (IllegalAccessException | IllegalArgumentException e) {
        e.printStackTrace();
        ret = e;
      }
      return ret;
    }

    /**
     * arbeitet die last of many Semantik ab
     */
    private void lom(VSPackageRequest request) {
      final String ipAdress = this.socket.getInetAddress().getHostAddress();      
      VSRequestDBEntry dbEntry = VSServer.requestLog.get(ipAdress + request.getSourceThread());
      VSRequestStatus status = null;
      
      /*
       * Zustand feststellen
       */
      if (dbEntry == null)
        status = VSRequestStatus.NEW_CLIENT;
      else if (request.getRequestTime() > dbEntry.getID())
        status = VSRequestStatus.NEW_GENERATION;
      else if (request.getRequestTime() == dbEntry.getID()) {
        if (request.getAge() > dbEntry.getAge())
          status = VSRequestStatus.NEW_AGE;
        else
          status = VSRequestStatus.OLD_AGE;
      } else
        status = VSRequestStatus.OLD_GENERATION;
       
      /*
       * Zustand abarbeiten
       */
      switch (status) {
        case OLD_GENERATION: {
          if (DEBUG)
            System.err.printf("old request generation: %d\n", request.getRequestTime());
        }
          break;
        case OLD_AGE: {
          if (DEBUG)
            System.err.printf("old request age: %d\n", request.getAge());
        }
          break;
        case NEW_CLIENT: {
          if (DEBUG)
            System.out.printf("new client ip: %s ; thread: %d \n", ipAdress, request.getSourceThread());
          dbEntry = new VSRequestDBEntry();
          VSServer.requestLog.put(ipAdress + request.getSourceThread(), dbEntry);
        }
        case NEW_GENERATION: {
          if (DEBUG)
            System.out.printf("new generation: %d \n", request.getRequestTime());
          dbEntry.setID(request.getRequestTime());
        }
        case NEW_AGE: {
          if (DEBUG)
            System.out.printf("new age: %d \n", request.getAge());
          dbEntry.setAge(request.getAge());
          this.sendReply(this.calculateResult(request), request);
        }
      }
    }

    /**
     * arbeitet die maybe Semantik ab
     */
    private void maybe(VSPackageRequest request) {
      
      this.sendReply(this.calculateResult(request), request);

      try {
        this.socket.close();
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
      System.out.printf("connected with client: %s\n", this.socket);
      getRequest: do {
        VSPackageRequest request = null;
        
        try {
          // Request lesen
          request = (VSPackageRequest) this.connect.receiveObject();
          if (DEBUG)
            System.out.println("new income request");
          // Methode aufrufen und ret versenden
        } catch (final IOException ioe) {
          if (DEBUG)
            System.err.println("socket is closed, could not get new request");
          break getRequest;
        } catch (final Exception e) {
          if (DEBUG)
            System.err.println(e.getMessage());
          continue getRequest;
        }
        
        switch (this.rpc) {
          case MAYBE:
            this.maybe(request);
            break;
          case LOM:
            this.lom(request);
            break;
          case AMO:
            this.amo(request);
            break;
        }
      } while (!this.socket.isClosed());
    }

    /**
     * Sendet die Antwort für den Request ab
     * 
     * @param ret
     * @param request
     */
    private void sendReply(final Object ret, final VSPackageRequest request) {
      try {
        if (DEBUG)
          System.out.printf("send reply\n");
        this.connect.sendObject(new VSPackageReply(ret, request, this.rpc));
      } catch (final IOException e) {
        if (DEBUG)
          System.err.printf("could not send reply\n");
        // e.printStackTrace();
      }
    }

  }

  /**
   * Threadpool für die Handler
   */
  private final ExecutorService                     pool;

  /**
   * Datenbank für die Anfragen
   */
  private static final Hashtable<String, VSRequestDBEntry> requestLog = new Hashtable<>();

  /**
   * Der Socket des Servers
   */
  private final ServerSocket                        socket;
  
  /**
   * Die Instance des Servers
   */
  private static VSServer                           instance;
  
  /**
   * Gibt die Server Instance zurück
   * 
   * @return
   * @throws IOException
   */
  static synchronized VSServer getInstance() throws IOException {
    if (VSServer.instance == null)
      VSServer.instance = new VSServer();
    return VSServer.instance;
  }
  
  /**
   * Erstellt einen Server für die Anfragen
   * 
   * @throws IOException
   */
  private VSServer() throws IOException {
    this.socket = new ServerSocket(0);
    this.pool = Executors.newCachedThreadPool();
  }
  
  /**
   * @return
   */
  public int getPort() {
    return this.socket.getLocalPort();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run() {
    while (true)
      try {
        System.out.println("waiting for connection");
        this.pool.execute(new VSRequestWorker(this.socket.accept()));
      } catch (final IOException e) {
        System.err.println("fail to connect with client");
        e.printStackTrace();
      }
  }
}
