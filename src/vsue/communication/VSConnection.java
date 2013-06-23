package vsue.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Versedet Bytes über TCP
 * 
 * @author Stephan
 *
 */
public class VSConnection {
  private final Socket         socket;
  private static final boolean Debug = false;

  /**
   * Erstellt eine neue Verbindung
   * 
   * @param socket
   */
  public VSConnection(final Socket socket) {
    this.socket = socket;
  }

  /**
   * liest ein Byte Array vom Socket ein. Es wir zuerst die Größe übertragen (4
   * Byte) danach wird diese Anzahl an Byte gelesen
   * 
   * @return
   * @throws IOException
   */
  public byte[] receiveChunk() throws IOException {
    final InputStream is = this.socket.getInputStream();
    byte[] chunk;
    synchronized (is) {
      final byte[] lengthData = new byte[4];
      is.read(lengthData, 0, 4); // wir lesen immer zuerst die länge des
                                 // objectes
      final int length = ByteBuffer.wrap(lengthData).getInt();

      if (VSConnection.Debug)
        System.out.printf("lenght of message: %d as %s\n", length, Arrays.toString(lengthData));
      chunk = new byte[length];
      int read = 0;
      while (read < length)
        read += is.read(chunk, read, length - read);
      // lese was im buffer steht und pack es in das array
    }

    if (VSConnection.Debug)
      System.out.printf("receive:\n%s\n", Arrays.toString(chunk));
    return chunk;
  }

  /**
   * Schreibt das Byte Array in den Socket. zuerst wird die Größe übertragen (4
   * Byte) danach das Array
   * 
   * @param chunk
   * @throws IOException
   */
  public void sendChunk(final byte[] chunk) throws IOException {
    final int length = chunk.length;
    final byte[] lengthData = ByteBuffer.allocate(4).putInt(length).array();
    final OutputStream os = this.socket.getOutputStream();
    synchronized (os) {
      if (VSConnection.Debug)
        System.out.printf("size:%d as %s\n", length, Arrays.toString(lengthData));
      os.write(lengthData); // zuerst senden wir die länge des objektes
      os.flush();
      if (VSConnection.Debug)
        System.out.printf("send:\n%s\n", Arrays.toString(chunk));
      os.write(chunk); // danach senden wir das objekt an sich
      os.flush();
    }
  }
}
