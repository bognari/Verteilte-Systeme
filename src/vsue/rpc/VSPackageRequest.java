package vsue.rpc;

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
  private final String   method;
  private final Object[] args;

  public VSPackageRequest(final int objID, final Method method, final Object[] args) {
    if (method == null) {
      throw new IllegalArgumentException("method is null!");
    }
    this.method = method.toGenericString();
    this.objID = objID;
    this.args = args;
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

  @Override
  public String toString() {
    return String.format("%s, %d, %s", this.method, this.objID, this.args.toString());
  }
}
