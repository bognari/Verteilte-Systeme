package vsue.faults;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * Wrapper für die "Anfragen"
 * 
 * @author Stephan
 * 
 */
@SuppressWarnings("serial")
public class VSPackageRequest implements Serializable {
  private final int      objID;
  private byte           age;
  private final String   method;
  private final Object[] args;
  private final int      threadHash;
  private final long     timeStamp;

  public VSPackageRequest(final int objID, final Method method, final Object[] args) {
    if (method == null) {
      throw new IllegalArgumentException("method is null!");
    }
    this.age = 1;
    this.method = method.toGenericString();
    this.objID = objID;
    this.args = args;
    this.threadHash = Thread.currentThread().hashCode();
    this.timeStamp = System.nanoTime();
  }

  public void age() {
    this.age += 1;
  }

  public Object[] getArgs() {
    return this.args;
  }

  public String getMethod() {
    return this.method;
  }

  public int getObjID() {
    return this.objID;
  }

  public byte getAge() {
    return this.age;
  }

  public int getThreadHash() {
    return this.threadHash;
  }

  public long getID() {
    return this.timeStamp;
  }

  @Override
  public String toString() {
    return String.format("%s, %d, %s", this.method, this.objID, this.args.toString());
  }
}
