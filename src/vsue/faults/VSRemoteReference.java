package vsue.faults;

import java.io.Serializable;

@SuppressWarnings("serial")
public class VSRemoteReference implements Serializable {
  private final String host;
  private final int    port;
  private final int    objectID;

  public VSRemoteReference(final String host, final int port, final int objectID) {
    this.host = host;
    this.port = port;
    this.objectID = objectID;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final VSRemoteReference other = (VSRemoteReference) obj;
    if (this.host == null) {
      if (other.host != null) {
        return false;
      }
    } else if (!this.host.equals(other.host)) {
      return false;
    }
    if (this.objectID != other.objectID) {
      return false;
    }
    if (this.port != other.port) {
      return false;
    }
    return true;
  }

  public String getHost() {
    return this.host;
  }

  public int getObjectID() {
    return this.objectID;
  }

  public int getPort() {
    return this.port;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + ((this.host == null) ? 0 : this.host.hashCode());
    result = (prime * result) + this.objectID;
    result = (prime * result) + this.port;
    return result;
  }
}
