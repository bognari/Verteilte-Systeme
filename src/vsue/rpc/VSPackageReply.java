package vsue.rpc;

import java.io.Serializable;

/**
 * Wrapper für die "Antworten"
 * 
 * @author Stephan
 * 
 */
@SuppressWarnings("serial")
public class VSPackageReply implements Serializable {
  private final Object obj;

  public VSPackageReply(final Object obj) {
    this.obj = obj;
  }

  public Object getObj() {
    return this.obj;
  }

}
