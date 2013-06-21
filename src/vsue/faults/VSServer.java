package vsue.faults;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
  class VSRequestWorker implements Runnable {
    private final VSObjectConnection connect;
    private final Socket             socket;
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

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
      System.out.printf("connected with client: %s\n", this.socket);
      switch (this.rpc) {
        case MAYBE:
          this.maybe();
          break;
        case LOM:
          this.lom();
          break;
        case AMO:
          this.amo();
          break;
      }
    }

    private Object calculateResult(final VSPackageRequest request) {
      Object ret = null;

      try {
        if (DEBUG) {
          System.out.printf("invoke method: %s\n", request.getMethod());
        }
        ret = VSServer.this.manager.invokeMethod(request.getObjID(), request.getMethod(), request.getArgs());
      } catch (final InvocationTargetException e) {
        e.printStackTrace();
        ret = e.getTargetException();
      } catch (IllegalAccessException | IllegalArgumentException e) {
        e.printStackTrace();
        ret = e;
      }
      return ret;
    }

    private void sendReply(final Object ret, final VSPackageRequest request) {
      try {
        if (DEBUG) {
          System.out.printf("send reply\n");
        }
        this.connect.sendObject(new VSPackageReply(ret, request, this.rpc));
      } catch (final IOException e) {
        if (DEBUG) {
          System.err.printf("could not send reply\n");
          // e.printStackTrace();
        }
      }
    }

    private void maybe() {
      VSPackageRequest request = null;

      try {
        // Request lesen
        request = (VSPackageRequest) this.connect.receiveObject();
        if (DEBUG) {
          System.out.println("new income request");
          // Methode aufrufen und ret versenden
        }
      } catch (final IOException ioe) {
        if (DEBUG) {
          System.err.println("socket is closed, could not get new request");
        }
      } catch (final Exception e) {
        if (DEBUG) {
          System.err.println(e.getMessage());
        }
      }

      this.sendReply(this.calculateResult(request), request);

      try {
        this.socket.close();
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }

    private void lom() {
      final String ipAdress = this.socket.getInetAddress().getHostAddress();
      VSRequestDBEntry dbEntry = null;
      VSRequestStatus status = null;
      newTry: do {
        VSPackageRequest request = null;
        try {
          // Request lesen
          request = (VSPackageRequest) this.connect.receiveObject();
          if (DEBUG) {
            System.out.println("new income request");
            // Methode aufrufen und ret versenden
          }
        } catch (final IOException ioe) {
          if (DEBUG) {
            System.err.println("socket is closed, could not get new request");
          }
          break newTry;
        } catch (final Exception e) {
          if (DEBUG) {
            System.err.println(e.getMessage());
          }
          continue newTry;
        }

        dbEntry = VSServer.this.requestLog.get(ipAdress + request.getThreadHash());

        if (dbEntry == null) {
          status = VSRequestStatus.NEW_CLIENT;
        } else {
          if (request.getID() > dbEntry.getID()) {
            status = VSRequestStatus.NEW_GENERATION;
          } else {
            if (request.getID() == dbEntry.getID()) {
              if (request.getAge() > dbEntry.getAge()) {
                status = VSRequestStatus.NEW_AGE;
              } else {
                status = VSRequestStatus.OLD_AGE;
              }
            } else {
              status = VSRequestStatus.OLD_GENERATION;
            }
          }
        }

        switch (status) {
          case OLD_GENERATION: {
            if (DEBUG) {
              System.err.printf("old request generation: %d\n", request.getID());
            }
          }
            break;
          case OLD_AGE: {
            if (DEBUG) {
              System.err.printf("old request age: %d\n", request.getAge());
            }
          }
            break;
          case NEW_CLIENT: {
            if (DEBUG) {
              System.out.printf("new client ip: %s ; thread: %d \n", ipAdress, request.getThreadHash());
            }
            dbEntry = new VSRequestDBEntry();
            VSServer.this.requestLog.put(ipAdress + request.getThreadHash(), dbEntry);
          }
          case NEW_GENERATION: {
            if (DEBUG) {
              System.out.printf("new generation: %d \n", request.getID());
            }
            dbEntry.setID(request.getID());
          }
          case NEW_AGE: {
            if (DEBUG) {
              System.out.printf("new age: %d \n", request.getAge());
            }
            dbEntry.setAge(request.getAge());
            this.sendReply(this.calculateResult(request), request);
          }
        }
      } while (!this.socket.isClosed());
    }

    private void amo() {
      final String ipAdress = this.socket.getInetAddress().getHostAddress();
      VSRequestDBEntry dbEntry = null;
      VSRequestStatus status = null;
      newTry: do {
        VSPackageRequest request = null;
        try {
          // Request lesen
          request = (VSPackageRequest) this.connect.receiveObject();
          if (DEBUG) {
            System.out.println("new income request");
            // Methode aufrufen und ret versenden
          }
        } catch (final IOException ioe) {
          if (DEBUG) {
            System.err.println("socket is closed, could not get new request");
          }
          break newTry;
        } catch (final Exception e) {
          System.err.println(e.getMessage());
          continue newTry;
        }

        dbEntry = VSServer.this.requestLog.get(ipAdress + request.getThreadHash());

        if (dbEntry == null) {
          status = VSRequestStatus.NEW_CLIENT;
        } else {
          if (request.getID() > dbEntry.getID()) {
            status = VSRequestStatus.NEW_GENERATION;
          } else {
            status = VSRequestStatus.OLD_GENERATION;
          }
        }

        switch (status) {
          case NEW_CLIENT: {
            if (DEBUG) {
              System.out.printf("new client ip: %s ; thread: %d \n", ipAdress, request.getThreadHash());
            }
            dbEntry = new VSRequestDBEntry();
            VSServer.this.requestLog.put(ipAdress + request.getThreadHash(), dbEntry);
          }
          case NEW_GENERATION: {
            if (DEBUG) {
              System.out.printf("new generation: %d \n", request.getID());
            }
            dbEntry.setID(request.getID());
            dbEntry.setRet(this.calculateResult(request));
          }
          case OLD_GENERATION: {
            this.sendReply(dbEntry.getRet(), request);
          }
            break;
          default: {
          }
        }
      } while (!this.socket.isClosed());
    }

  }

  class VSRequestDBEntry {
    private long   id;
    private byte   age;
    private Object ret;

    public byte getAge() {
      return this.age;
    }

    public void setAge(final byte age) {
      this.age = age;
    }

    public long getID() {
      return this.id;
    }

    public void setID(final long id) {
      this.id = id;
    }

    public Object getRet() {
      return this.ret;
    }

    public void setRet(final Object ret) {
      this.ret = ret;
    }

    private VSServer getOuterType() {
      return VSServer.this;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = (prime * result) + (int) (this.id ^ (this.id >>> 32));
      return result;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (this.getClass() != obj.getClass()) {
        return false;
      }
      final VSRequestDBEntry other = (VSRequestDBEntry) obj;
      if (!this.getOuterType().equals(other.getOuterType())) {
        return false;
      }
      if (this.id != other.id) {
        return false;
      }
      return true;
    }
  }

  private final VSRemoteObjectManager               manager;

  private final ExecutorService                     pool;

  private final Hashtable<String, VSRequestDBEntry> requestLog;

  public VSServer(final VSRemoteObjectManager manager) {
    this.manager = manager;
    this.pool = Executors.newCachedThreadPool();
    this.requestLog = new Hashtable<>();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run() {
    while (true) {
      try {
        System.out.println("waiting for connection");
        this.pool.execute(new VSRequestWorker(this.manager.getSocket().accept()));
      } catch (final IOException e) {
        System.err.println("fail to connect with client");
        e.printStackTrace();
      }
    }
  }

  public enum VSRequestStatus {
    NEW_CLIENT, OLD_GENERATION, OLD_AGE, NEW_GENERATION, NEW_AGE
  }
}
