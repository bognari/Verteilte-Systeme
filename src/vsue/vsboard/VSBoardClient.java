package vsue.vsboard;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import vsue.faults.VSRPCsemantics;
import vsue.rpc.VSRemoteObjectManager;

/**
 * @author Stephan
 * 
 */
public class VSBoardClient implements VSBoardListener, Serializable {

  private static final long      serialVersionUID = 899243661902242507L;

  protected static final boolean debug            = false;

  protected static final Pattern pExit            = Pattern.compile("^exit$");
  // static final Pattern pHelp = Pattern.compile("^help|?$");
  protected static final Pattern pPost            = Pattern.compile("^post\\s+(\\d+)\\s+\"(.*?)\"\\s+\"(.*?)\"$");
  protected static final Pattern pGet             = Pattern.compile("^get\\s+(\\-?\\d+)$");
  protected static final Pattern pConnect         = Pattern.compile("^connect\\s+\"([a-zA-Z0-9.]+)\"\\s+(\\d+)$");
  protected static final Pattern pListen          = Pattern.compile("^listen$");

  protected static final String  NAME             = "VSBoard";

  /**
   * startet den Clienten
   * 
   * @param args
   */
  public static void main(final String[] args) {
    VSRemoteObjectManager.setRPCSymmantic(VSRPCsemantics.AMO);
    final VSBoardClient app = new VSBoardClient();
    System.out.println("Client gestartet... gib help ein für Hilfe");
    app.run();
  }

  protected VSBoard vsBord;

  protected boolean listen;

  /**
   * verbindet den Clienten mit dem Server
   * 
   * @param input
   */
  protected void connect(final String input) {
    final Matcher m = VSBoardClient.pConnect.matcher(input);
    m.matches();

    System.out.printf("Verbinde zu %s::%s\n..\n", m.group(1), m.group(2));

    try {
      final Registry registry = LocateRegistry.getRegistry(m.group(1), Integer.parseInt(m.group(2)));
      this.vsBord = (VSBoard) registry.lookup(VSBoardClient.NAME);
      System.out.println("mit dem Server verbunden");
    } catch (final RemoteException re) {
      System.err.println("Konnte zum Server nicht verbinden.");
      if (VSBoardClient.debug)
        re.printStackTrace();
    } catch (final NotBoundException nbe) {
      System.err.println("Dienst existiert nicht auf dem Server");
      if (VSBoardClient.debug)
        nbe.printStackTrace();
    }
  }

  /**
   * beendet den Clienten
   * 
   * @param scanner
   */
  protected void exit(final Scanner scanner) {
    scanner.close();
    if (this.listen)
      VSRemoteObjectManager.getInstance().unexport(this);
    System.exit(0);
  }

  /**
   * Holt i Messages vom Server
   * 
   * @param input
   */
  protected void get(final String input) {
    if ((this.vsBord == null) && !VSBoardClient.debug) {
      System.err.println("nicht verbunden");
      return;
    }

    final Matcher m = VSBoardClient.pGet.matcher(input);
    m.matches();

    try {
      final VSBoardMessage[] messages = this.vsBord.get(Integer.parseInt(m.group(1)));
      for (final VSBoardMessage message : messages)
        this.printMessage(message);
    } catch (final RemoteException re) {
      System.err.println("Server Fehler");
      if (VSBoardClient.debug)
        re.printStackTrace();
    } catch (final IllegalArgumentException iae) {
      System.err.println("Index muss eine Zahl größer gleich 0 sein");
      if (VSBoardClient.debug)
        iae.printStackTrace();
    }
  }

  /**
	 * 
	 */
  protected void help() {
    System.out
        .println("Die Befehle sind: help, ?, exit, post uid \"title\" \"message\", get i, connect \"server\" port, listen");
  }

  /**
   * trägt sich als Listener beim Server ein
   */
  protected void listen() {
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
        if (VSBoardClient.debug)
          re.printStackTrace();
      }
    } else
      System.out.println("schon als Listener registiert");
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
  protected void post(final String input) {
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
      if (VSBoardClient.debug)
        re.printStackTrace();
    }
  }

  /**
   * Callback für neue Messages
   * 
   * @param message
   */
  protected void printMessage(final VSBoardMessage message) {
    System.out.printf("-------\n%s\n-------\n", message.toString());
  }

  /**
   * nimmt die Befehle entgegen
   */
  protected void run() {
    final Scanner scanner = new Scanner(System.in);
    while (true) {
      final String input = scanner.nextLine();
      if (VSBoardClient.pExit.matcher(input).matches())
        this.exit(scanner);
      else if (VSBoardClient.pGet.matcher(input).matches())
        this.get(input);
      else if (VSBoardClient.pConnect.matcher(input).matches())
        this.connect(input);
      else if (VSBoardClient.pPost.matcher(input).matches())
        this.post(input);
      else if (VSBoardClient.pListen.matcher(input).matches())
        this.listen();
      else
        this.help();
    }
  }
}