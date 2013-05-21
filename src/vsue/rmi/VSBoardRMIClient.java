package vsue.rmi;

import java.io.Serializable;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Stephan
 * 
 */
public class VSBoardRMIClient implements VSBoardListener, Serializable {
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

  static private Pattern       pExit            = Pattern.compile("^exit$");
  // static private Pattern pHelp = Pattern.compile("^help|?$");
  static private Pattern       pPost            = Pattern.compile("^post\\s+(\\d+)\\s+\"(.*?)\"\\s+\"(.*?)\"$");
  static private Pattern       pGet             = Pattern.compile("^get\\s+(\\-?\\d+)$");
  static private Pattern       pConnect         = Pattern.compile("^connect\\s+\"([a-zA-Z0-9.]+)\"\\s+(\\d+)$");
  static private Pattern       pListen          = Pattern.compile("^listen$");

  /**
   * @param args
   */
  public static void main(final String[] args) {
    final VSBoardRMIClient app = new VSBoardRMIClient();
    System.out.println("Client gestartet... gebe help ein für Hilfe");
    app.run();
  }

  /**
   * @param input
   */
  private void connect(final String input) {
    final Matcher m = VSBoardRMIClient.pConnect.matcher(input);
    m.matches();

    System.out.printf("Verbinde zu %s::%s\n..\n", m.group(1), m.group(2));

    try {
      final Registry registry = LocateRegistry.getRegistry(m.group(1), Integer.parseInt(m.group(2)));
      this.vsBord = (VSBoard) registry.lookup(VSBoardRMIClient.NAME);
      System.out.println("mit dem Server verbunden");
    } catch (final RemoteException re) {
      System.err.println("Konnte zum Server nicht verbinden.");
      if (VSBoardRMIClient.debug) {
        re.printStackTrace();
      }
    } catch (final NotBoundException nbe) {
      System.err.println("Dienst existiert nicht auf dem Server");
      if (VSBoardRMIClient.debug) {
        nbe.printStackTrace();
      }
    }
  }

  /**
   * @param scanner
   */
  private void exit(final Scanner scanner) {
    scanner.close();
    try {
      System.out.println("Verbindung getrennt: " + UnicastRemoteObject.unexportObject(this, true));
    } catch (final NoSuchObjectException e) {
      if (VSBoardRMIClient.debug) {
        e.printStackTrace();
      }
    }
    System.exit(0);
  }

  /**
   * @param input
   */
  private void get(final String input) {
    if ((this.vsBord == null) && !VSBoardRMIClient.debug) {
      System.err.println("nicht verbunden");
      return;
    }

    final Matcher m = VSBoardRMIClient.pGet.matcher(input);
    m.matches();

    try {
      final VSBoardMessage[] messages = this.vsBord.get(Integer.parseInt(m.group(1)));
      for (final VSBoardMessage message : messages) {
        this.printMessage(message);
      }
    } catch (final RemoteException re) {
      System.err.println("Server Fehler");
      if (VSBoardRMIClient.debug) {
        re.printStackTrace();
      }
    } catch (final IllegalArgumentException iae) {
      System.err.println("Index muss eine Zahl größer gleich 0 sein");
      if (VSBoardRMIClient.debug) {
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
	 * 
	 */
  private void listen() {
    if ((this.vsBord == null) && !VSBoardRMIClient.debug) {
      System.err.println("nicht verbunden");
      return;
    }
    try {
      final VSBoardListener callback = (VSBoardListener) UnicastRemoteObject.exportObject(this, 0);
      this.vsBord.listen(callback);
      System.out.println("am Server registiert");
    } catch (final RemoteException re) {
      System.err.println("Server Fehler");
      if (VSBoardRMIClient.debug) {
        re.printStackTrace();
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * de.tu_bs.vsBoard.VSBoardListener#newMessage(de.tu_bs.vsBoard.VSBoardMessage
   * )
   */
  @Override
  public void newMessage(final VSBoardMessage message) throws RemoteException {
    System.out.println("!!! NEW MESSAGE !!!");
    this.printMessage(message);
  }

  /**
   * @param input
   */
  private void post(final String input) {
    if ((this.vsBord == null) && !VSBoardRMIClient.debug) {
      System.err.println("nicht verbunden");
      return;
    }

    final Matcher m = VSBoardRMIClient.pPost.matcher(input);
    m.matches();

    try {
      final VSBoardMessage vsMessage = new VSBoardMessage(Integer.parseInt(m.group(1)), m.group(2), m.group(3));
      this.vsBord.post(vsMessage);
    } catch (final RemoteException re) {
      System.err.println("Server Fehler");
      if (VSBoardRMIClient.debug) {
        re.printStackTrace();
      }
    }
  }

  /**
   * @param message
   */
  private void printMessage(final VSBoardMessage message) {
    System.out.printf("-------\n%s\n-------\n", message.toString());
  }

  /**
	 * 
	 */
  public void run() {
    final Scanner scanner = new Scanner(System.in);
    while (true) {
      final String input = scanner.nextLine();
      if (VSBoardRMIClient.pExit.matcher(input).matches()) {
        this.exit(scanner);
      } else if (VSBoardRMIClient.pGet.matcher(input).matches()) {
        this.get(input);
      } else if (VSBoardRMIClient.pConnect.matcher(input).matches()) {
        this.connect(input);
      } else if (VSBoardRMIClient.pPost.matcher(input).matches()) {
        this.post(input);
      } else if (VSBoardRMIClient.pListen.matcher(input).matches()) {
        this.listen();
      } else {
        this.help();
      }
    }
  }
}