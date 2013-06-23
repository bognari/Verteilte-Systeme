package vsue.rpc;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * Wrapper für die "Anfragen"
 * 
 * @author Stephan
 * 
 */
public class VSPackageRequest implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = -5523431956269583944L;
  /**
   * ID des Objects
   */
  private final int      objID;
  /**
   * Anfragen Versuch
   */
  private byte           age;
  /**
   * auf zurufende Methode
   */
  private final String   method;
  /**
   * Übergebene Argumente
   */
  private final Object[] args;
  /**
   * Quell Thread
   */
  private final int      threadHash;
  /**
   * Timestamp beim ersten Versuch
   */
  private final long     timestamp;

  /**
   * Erstellt einen neuen Request
   * 
   * @param objID
   * @param method
   * @param args
   */
  public VSPackageRequest(final int objID, final Method method, final Object[] args) {
    if (method == null)
      throw new IllegalArgumentException("method is null!");
    this.age = 1;
    this.method = method.toGenericString();
    this.objID = objID;
    this.args = args;
    this.threadHash = Thread.currentThread().hashCode();
    this.timestamp = System.nanoTime();
  }

  /**
   * lässt den Requst altern
   */
  public void age() {
    this.age++;
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
  public Object[] getArgs() {
    return this.args;
  }

  /**
   * @return
   */
  public long getRequestTime() {
    return this.timestamp;
  }

  /**
   * @return
   */
  public String getMethod() {
    return this.method;
  }

  /**
   * @return
   */
  public int getObjID() {
    return this.objID;
  }

  /**
   * @return
   */
  public int getSourceThread() {
    return this.threadHash;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return String.format("%s, %d, %s", this.method, this.objID, this.args.toString());
  }
}
