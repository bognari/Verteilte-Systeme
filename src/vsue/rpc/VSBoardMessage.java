package vsue.rpc;

import java.io.Serializable;

@SuppressWarnings("serial")
public class VSBoardMessage implements Serializable {
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
