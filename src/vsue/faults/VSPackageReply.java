package vsue.faults;

import java.io.Serializable;

/**
 * Wrapper für die "Antworten"
 * 
 * @author Stephan
 * 
 */
@SuppressWarnings("serial")
public class VSPackageReply implements Serializable {
  private final Object         obj;
  private final byte           requestAge;
  private final VSRPCsemantics rpc;

  public VSPackageReply(final Object obj, final VSPackageRequest request, final VSRPCsemantics rpc) {
    this.obj = obj;
    this.requestAge = request.getAge();
    this.rpc = rpc;
  }

  public Object getObj() {
    return this.obj;
  }

  public byte getRequestAge() {
    return this.requestAge;
  }

  public VSRPCsemantics getRPC() {
    return this.rpc;
  }
}
