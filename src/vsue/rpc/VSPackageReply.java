package vsue.rpc;

import java.io.Serializable;

import vsue.faults.VSRPCsemantics;

/**
 * Wrapper für die "Antworten"
 * 
 * @author Stephan
 * 
 */
public class VSPackageReply implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = -9067268780305550556L;
  /**
   * Ergebis des Requests
   */
  private final Object         obj;
  /**
   * Version des Requests
   */
  private final byte           requestAge;
  /**
   * Verwendete RPC Sematik des Servers bei der Bearbeitung
   */
  private final VSRPCsemantics rpc;

  /**
   * Erstellt eine neue Antwort auf die Anfrage
   * 
   * @param obj
   * @param request
   * @param rpc
   */
  public VSPackageReply(final Object obj, final VSPackageRequest request, final VSRPCsemantics rpc) {
    this.obj = obj;
    this.requestAge = request.getAge();
    this.rpc = rpc;
  }

  /**
   * @return
   */
  public Object getObj() {
    return this.obj;
  }

  /**
   * @return
   */
  public byte getRequestAge() {
    return this.requestAge;
  }

  /**
   * @return
   */
  public VSRPCsemantics getRPC() {
    return this.rpc;
  }
}
