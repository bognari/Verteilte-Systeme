package vsue.communication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

/**
 * Kann Objekte für TCP versenden
 * 
 * @author Stephan
 *
 */
public class VSObjectConnection extends VSConnection {

  private static final boolean debug = false;

  /**
   * Erstellt eine neue Verbindung
   * 
   * @param socket
   */
  public VSObjectConnection(final Socket socket) {
    super(socket);
  }

  /**
   * deserialisiert das Object aus einem Byte Array
   * 
   * @param data
   * @return
   * @throws IOException
   * @throws ClassNotFoundException
   */
  private Serializable deserialize(final byte[] data) throws IOException, ClassNotFoundException {
    ByteArrayInputStream bais = null;
    ObjectInputStream ois = null;
    try {
      bais = new ByteArrayInputStream(data);
      ois = new ObjectInputStream(bais);

      final Serializable obj = (Serializable) ois.readObject();
      return obj;
    } catch (final IOException ioe) {
      throw ioe;
    } finally {
      if (ois != null)
        ois.close();
      if (bais != null)
        bais.close();
    }
  }

  /**
   * emfängt das Object serialisiert als Byte Array
   * 
   * @return
   * @throws IOException
   * @throws ClassNotFoundException
   */
  public Serializable receiveObject() throws IOException, ClassNotFoundException {
    final byte[] data = this.receiveChunk();
    if (VSObjectConnection.debug)
      System.out.println(this.deserialize(data));
    return this.deserialize(data);
  }

  /**
   * versendet das Object serialisiert als Byte Array
   * 
   * @param object
   * @throws IOException
   */
  public void sendObject(final Serializable object) throws IOException {
    if (VSObjectConnection.debug)
      System.out.println(object);
    final byte[] data = this.serialize(object);
    this.sendChunk(data);
  }

  /**
   * Serialisiert das Object zu einem Byte Array
   * 
   * @param obj
   * @return
   * @throws IOException
   */
  private byte[] serialize(final Serializable obj) throws IOException {
    ByteArrayOutputStream baos = null;
    ObjectOutputStream oos = null;
    try {
      baos = new ByteArrayOutputStream();
      oos = new ObjectOutputStream(baos);
      oos.writeObject(obj); // Serialisiere das Object

      final byte[] data = baos.toByteArray();
      // Baue aus dem Stream ein Byte Array
      return data;
    } catch (final IOException ioe) {
      throw ioe;
    } finally {
      if (baos != null)
        baos.close();
      if (oos != null)
        oos.close();
    }
  }
}
