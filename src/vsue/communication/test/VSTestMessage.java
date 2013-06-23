package vsue.communication.test;

import java.io.Serializable;

@SuppressWarnings("serial")
public class VSTestMessage implements Serializable {
  private final int      integer;
  private final String   string;
  private final Object[] objects;

  public VSTestMessage(final int integer, final String string, final Object[] objs) {
    this.integer = integer;
    this.string = string;
    this.objects = objs;
  }

  public int getInteger() {
    return this.integer;
  }

  public Object[] getObjs() {
    return this.objects;
  }

  public String getString() {
    return this.string;
  }
}
