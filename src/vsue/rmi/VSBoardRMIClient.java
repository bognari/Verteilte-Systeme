package vsue.rmi;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

import vsue.vsboard.VSBoardClient;
import vsue.vsboard.VSBoardListener;

/**
 * @author Stephan
 * 
 */
public class VSBoardRMIClient extends VSBoardClient {
  private static final long serialVersionUID = 6910929956023678719L;

  /**
   * @param args
   */
  public static void main(final String[] args) {
    final VSBoardRMIClient app = new VSBoardRMIClient();
    System.out.println("Client gestartet... gebe help ein für Hilfe");
    app.run();
  }

  /*
   * (non-Javadoc)
   * 
   * @see vsue.vsboard.VSBoardClient#exit(java.util.Scanner)
   */
  @Override
  protected void exit(final Scanner scanner) {
    scanner.close();
    if (this.listen)
      try {
        UnicastRemoteObject.unexportObject(this, true);
      } catch (final NoSuchObjectException e) {
        if (VSBoardClient.debug)
          e.printStackTrace();
      }
    System.exit(0);
  }

  /*
   * (non-Javadoc)
   * 
   * @see vsue.vsboard.VSBoardClient#listen()
   */
  @Override
  protected void listen() {
    if (!this.listen) {
      if ((this.vsBord == null) && !VSBoardClient.debug) {
        System.err.println("nicht verbunden");
        return;
      }
      try {
        final VSBoardListener callback = (VSBoardListener) UnicastRemoteObject.exportObject(this, 0);
        this.vsBord.listen(callback);
        this.listen = true;
        System.out.println("am Server registiert");
      } catch (final RemoteException re) {
        System.err.println("Server Fehler");
        if (VSBoardClient.debug)
          re.printStackTrace();
      }
    } else
      System.out.println("schon als Listener registiert");
  }
}