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
public class VSClient implements VSBoardListener, Serializable
{
	private static final long	serialVersionUID	= 6910929956023678719L;

	/**
	 * 
	 */
	private VSBoard								vsBord;

	/**
	 * 
	 */
	static final private boolean	debug			= true;

	static final private String		NAME			= "VSBoard";

	static private Pattern				pExit			= Pattern.compile("^exit$");
	// static private Pattern pHelp = Pattern.compile("^help|?$");
	static private Pattern				pPost			= Pattern
																							.compile("^post\\s+(\\d+)\\s+\"(.*?)\"\\s+\"(.*?)\"$");
	static private Pattern				pGet			= Pattern.compile("^get\\s+(\\-?\\d+)$");
	static private Pattern				pConnect	= Pattern
																							.compile("^connect\\s+\"([a-zA-Z0-9.]+)\"\\s+(\\d+)$");
	static private Pattern				pListen		= Pattern.compile("^listen$");

	/* (non-Javadoc)
	 * @see de.tu_bs.vsBoard.VSBoardListener#newMessage(de.tu_bs.vsBoard.VSBoardMessage)
	 */
	@Override
	public void newMessage(VSBoardMessage message) throws RemoteException
	{
		System.out.println("!!! NEW MESSAGE !!!");
		printMessage(message);
	}

	/**
	 * @param message
	 */
	private void printMessage(VSBoardMessage message)
	{
		System.out.printf("-------\n%s\n-------\n", message.toString());
	}

	/**
	 * @param scanner
	 */
	private void exit(Scanner scanner)
	{
		scanner.close();
		try
		{
			System.out.println("Verbindung getrennt: "
					+ UnicastRemoteObject.unexportObject(this, true));
		} catch (NoSuchObjectException e)
		{
			if (VSClient.debug)
				e.printStackTrace();
		}
		System.exit(0);
	}

	/**
	 * @param input
	 */
	private void get(String input)
	{
		if (this.vsBord == null && !VSClient.debug)
		{
			System.err.println("nicht verbunden");
			return;
		}

		Matcher m = pGet.matcher(input);
		m.matches();

		try
		{
			VSBoardMessage[] messages = this.vsBord.get(Integer.parseInt(m.group(1)));
			for (VSBoardMessage message : messages)
			{
				printMessage(message);
			}
		} catch (RemoteException re)
		{
			System.err.println("Server Fehler");
			if (VSClient.debug)
				re.printStackTrace();
		} catch (IllegalArgumentException iae)
		{
			System.err.println("Index muss eine Zahl größer gleich 0 sein");
			if (VSClient.debug)
				iae.printStackTrace();
		}
	}

	/**
	 * @param input
	 */
	private void post(String input)
	{
		if (this.vsBord == null && !VSClient.debug)
		{
			System.err.println("nicht verbunden");
			return;
		}

		Matcher m = pPost.matcher(input);
		m.matches();

		try
		{
			VSBoardMessage vsMessage = new VSBoardMessage(
					Integer.parseInt(m.group(1)), m.group(2), m.group(3));
			this.vsBord.post(vsMessage);
		} catch (RemoteException re)
		{
			System.err.println("Server Fehler");
			if (VSClient.debug)
				re.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private void listen()
	{
		if (this.vsBord == null && !VSClient.debug)
		{
			System.err.println("nicht verbunden");
			return;
		}
		try
		{
			VSBoardListener callback = (VSBoardListener) UnicastRemoteObject
					.exportObject(this, 0);
			this.vsBord.listen(callback);
			System.out.println("am Server registiert");
		} catch (RemoteException re)
		{
			System.err.println("Server Fehler");
			if (VSClient.debug)
				re.printStackTrace();
		}
	}

	/**
	 * @param input
	 */
	private void connect(String input)
	{
		Matcher m = pConnect.matcher(input);
		m.matches();

		System.out.printf("Verbinde zu %s::%s\n..\n", m.group(1), m.group(2));

		try
		{
			Registry registry = LocateRegistry.getRegistry(m.group(1),
					Integer.parseInt(m.group(2)));
			this.vsBord = (VSBoard) registry.lookup(NAME);
			System.out.println("mit dem Server verbunden");
		} catch (RemoteException re)
		{
			System.err.println("Konnte zum Server nicht verbinden.");
			if (VSClient.debug)
				re.printStackTrace();
		} catch (NotBoundException nbe)
		{
			System.err.println("Dienst existiert nicht auf dem Server");
			if (VSClient.debug)
				nbe.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private void help()
	{
		System.out
				.println("Die Befehle sind: help, ?, exit, post uid \"title\" \"message\", get i, connect \"server\" port, listen");
	}

	/**
	 * 
	 */
	public void run()
	{
		Scanner scanner = new Scanner(System.in);
		while (true)
		{
			String input = scanner.nextLine();
			if (pExit.matcher(input).matches())
				exit(scanner);
			else if (pGet.matcher(input).matches())
				get(input);
			else if (pConnect.matcher(input).matches())
				connect(input);
			else if (pPost.matcher(input).matches())
				post(input);
			else if (pListen.matcher(input).matches())
				listen();
			else
				help();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		VSClient app = new VSClient();
		System.out.println("Client gestartet... gebe help ein für Hilfe");
		app.run();
	}
}