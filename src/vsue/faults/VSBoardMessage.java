package vsue.faults;

import java.io.Serializable;

@SuppressWarnings("serial")
public class VSBoardMessage implements Serializable {
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + ((this.message == null) ? 0 : this.message.hashCode());
    result = (prime * result) + ((this.title == null) ? 0 : this.title.hashCode());
    result = (prime * result) + this.uid;
    return result;
  }

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
    final VSBoardMessage other = (VSBoardMessage) obj;
    if (this.message == null) {
      if (other.message != null) {
        return false;
      }
    } else if (!this.message.equals(other.message)) {
      return false;
    }
    if (this.title == null) {
      if (other.title != null) {
        return false;
      }
    } else if (!this.title.equals(other.title)) {
      return false;
    }
    if (this.uid != other.uid) {
      return false;
    }
    return true;
  }

  /**
	 * 
	 */
  private int    uid;
  /**
	 * 
	 */
  private String title;
  /**
	 * 
	 */
  private String message;

  /**
   * @param uid
   * @param title
   * @param message
   */
  public VSBoardMessage(final int uid, final String title, final String message) {
    this.setUid(uid);
    this.setTitle(title);
    this.setMessage(message);
  }

  public String getMessage() {
    return this.message;
  }

  public String getTitle() {
    return this.title;
  }

  public int getUid() {
    return this.uid;
  }

  protected void setMessage(final String message) {
    if (message == null) {
      throw new IllegalArgumentException("message darf nicht null sein.");
    }
    this.message = message;
  }

  protected void setTitle(final String title) {
    if (title == null) {
      throw new IllegalArgumentException("title darf nicht null sein.");
    }
    this.title = title;
  }

  protected void setUid(final int uid) {
    if (uid == 0) {
      throw new IllegalArgumentException("uid darf nicht 0 sein.");
    }
    this.uid = uid;
  }

  @Override
  public String toString() {
    return "Form: " + this.uid + " \nTitle: " + this.title + " \nMessage: " + this.message;
  }
}
