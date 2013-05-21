package vsue.rpc;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Stephan
 * 
 */
public class VSBoardClient implements VSBoardListener, Serializable {
  private static final long    serialVersionUID = 6910929956023678719L;

  /**
	 * 
	 */
  private VSBoard              vsBord;

  /**
	 * 
	 */
  static final private boolean debug            = false;

  static final private String  NAME             = "VSBoard";

  private boolean              listen           = false;

  static private Pattern       pExit            = Pattern.compile("^exit$");
  // static private Pattern pHelp = Pattern.compile("^help|?$");
  static private Pattern       pPost            = Pattern.compile("^post\\s+(\\d+)\\s+\"(.*?)\"\\s+\"(.*?)\"$");
  static private Pattern       pGet             = Pattern.compile("^get\\s+(\\-?\\d+)$");
  static private Pattern       pConnect         = Pattern.compile("^connect\\s+\"([a-zA-Z0-9.]+)\"\\s+(\\d+)$");
  static private Pattern       pListen          = Pattern.compile("^listen$");

  /**
   * startet den Clienten
   * 
   * @param args
   */
  public static void main(final String[] args) {
    final VSBoardClient app = new VSBoardClient();
    System.out.println("Client gestartet... gib help ein für Hilfe");
    app.run();
  }

  /**
   * verbindet den Clienten mit dem Server
   * 
   * @param input
   */
  private void connect(final String input) {
    final Matcher m = VSBoardClient.pConnect.matcher(input);
    m.matches();

    System.out.printf("Verbinde zu %s::%s\n..\n", m.group(1), m.group(2));

    try {
      final Registry registry = LocateRegistry.getRegistry(m.group(1), Integer.parseInt(m.group(2)));
      this.vsBord = (VSBoard) registry.lookup(VSBoardClient.NAME);
      System.out.println("mit dem Server verbunden");
    } catch (final RemoteException re) {
      System.err.println("Konnte zum Server nicht verbinden.");
      if (VSBoardClient.debug) {
        re.printStackTrace();
      }
    } catch (final NotBoundException nbe) {
      System.err.println("Dienst existiert nicht auf dem Server");
      if (VSBoardClient.debug) {
        nbe.printStackTrace();
      }
    }
  }

  /**
   * beendet den Clienten
   * 
   * @param scanner
   */
  private void exit(final Scanner scanner) {
    scanner.close();
    if (this.listen) {
      VSRemoteObjectManager.getInstance().unexport(this);
    }
    System.exit(0);
  }

  /**
   * Holt i Messages vom Server
   * 
   * @param input
   */
  private void get(final String input) {
    if ((this.vsBord == null) && !VSBoardClient.debug) {
      System.err.println("nicht verbunden");
      return;
    }

    final Matcher m = VSBoardClient.pGet.matcher(input);
    m.matches();

    try {
      final VSBoardMessage[] messages = this.vsBord.get(Integer.parseInt(m.group(1)));
      for (final VSBoardMessage message : messages) {
        this.printMessage(message);
      }
    } catch (final RemoteException re) {
      System.err.println("Server Fehler");
      if (VSBoardClient.debug) {
        re.printStackTrace();
      }
    } catch (final IllegalArgumentException iae) {
      System.err.println("Index muss eine Zahl größer gleich 0 sein");
      if (VSBoardClient.debug) {
        iae.printStackTrace();
      }
    }
  }

  /**
	 * 
	 */
  private void help() {
    System.out
        .println("Die Befehle sind: help, ?, exit, post uid \"title\" \"message\", get i, connect \"server\" port, listen");
  }

  /**
   * trägt sich als Listener beim Server ein
   */
  private void listen() {
    if (!this.listen) {
      if ((this.vsBord == null) && !VSBoardClient.debug) {
        System.err.println("nicht verbunden");
        return;
      }
      try {
        final VSBoardListener callback = (VSBoardListener) VSRemoteObjectManager.getInstance().exportObject(this);
        this.vsBord.listen(callback);
        this.listen = true;
        System.out.println("am Server registiert");
      } catch (final RemoteException re) {
        System.err.println("Server Fehler");
        if (VSBoardClient.debug) {
          re.printStackTrace();
        }
      }
    } else {
      System.out.println("schon als Listener registiert");
    }
  }

  @Override
  public void newMessage(final VSBoardMessage message) throws RemoteException {
    System.out.println("!!! NEW MESSAGE !!!");
    this.printMessage(message);
  }

  /**
   * schreibt eine Message an den Server
   * 
   * @param input
   */
  private void post(final String input) {
    if ((this.vsBord == null) && !VSBoardClient.debug) {
      System.err.println("nicht verbunden");
      return;
    }

    final Matcher m = VSBoardClient.pPost.matcher(input);
    m.matches();

    try {
      final VSBoardMessage vsMessage = new VSBoardMessage(Integer.parseInt(m.group(1)), m.group(2), m.group(3));
      this.vsBord.post(vsMessage);
    } catch (final RemoteException re) {
      System.err.println("Server Fehler");
      if (VSBoardClient.debug) {
        re.printStackTrace();
      }
    }
  }

  /**
   * Callback für neue Messages
   * 
   * @param message
   */
  private void printMessage(final VSBoardMessage message) {
    System.out.printf("-------\n%s\n-------\n", message.toString());
  }

  /**
   * nimmt die Befehle entgegen
   */
  public void run() {
    final Scanner scanner = new Scanner(System.in);
    while (true) {
      final String input = scanner.nextLine();
      if (VSBoardClient.pExit.matcher(input).matches()) {
        this.exit(scanner);
      } else if (VSBoardClient.pGet.matcher(input).matches()) {
        this.get(input);
      } else if (VSBoardClient.pConnect.matcher(input).matches()) {
        this.connect(input);
      } else if (VSBoardClient.pPost.matcher(input).matches()) {
        this.post(input);
      } else if (VSBoardClient.pListen.matcher(input).matches()) {
        this.listen();
      } else {
        this.help();
      }
    }
  }
}