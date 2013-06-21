package vsue.faults;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class VSBuggyObjectConnection extends VSObjectConnection {

  private static final boolean     ENABLE_DEBUG_OUTPUT   = true;

  private static final int         DROP_PROBABILITY      = 30;
  private static final int         DUPLICATE_PROBABILITY = 30;
  private static final int         DELAY_PROBABILITY     = 30;

  private static final int         MAX_DELAY             = 500;

  private final List<Serializable> incomingObjects;

  public VSBuggyObjectConnection(final Socket socket) {
    super(socket);
    this.incomingObjects = new LinkedList<Serializable>();
  }

  @Override
  public Serializable receiveObject() throws IOException, ClassNotFoundException {
    final Random random = new Random();

    while (this.incomingObjects.isEmpty()) {
      final Serializable object = super.receiveObject();
      if (object == null) {
        return null;
      }

      // Drop object?
      if (random.nextInt(100) < DROP_PROBABILITY) {
        if (ENABLE_DEBUG_OUTPUT) {
          System.err.println("DROP: " + object);
        }
        continue;
      }
      this.incomingObjects.add(object);

      // Duplicate object?
      if (random.nextInt(100) < DUPLICATE_PROBABILITY) {
        if (ENABLE_DEBUG_OUTPUT) {
          System.err.println("DUPLICATE: " + object);
        }
        this.incomingObjects.add(object);
      }
    }

    // Delay object?
    if (random.nextInt(100) < DELAY_PROBABILITY) {
      final int delay = random.nextInt(MAX_DELAY);
      if (ENABLE_DEBUG_OUTPUT) {
        System.err.println("DELAY: " + this.incomingObjects.get(0));
      }
      try {
        Thread.sleep(delay);
      } catch (final InterruptedException e) {
      }
    }

    return this.incomingObjects.remove(0);
  }

}
