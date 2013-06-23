package vsue.vsboard;

import java.io.Serializable;

/**
 * @author Stephan
 * 
 */
public class VSBoardMessage implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = -4198423796850187816L;

  /**
	 * ID des Absenders
	 */
  private final int         uid;

  /**
	 * Title der Nachricht
	 */
  private final String      title;

  /**
	 * Inhalt der Nachricht
	 */
  private final String      message;

  /**
   * Erstellt eine neue Nachricht
   * 
   * @param uid
   * @param title
   * @param message
   */
  public VSBoardMessage(final int uid, final String title, final String message) {
    this.uid = uid;
    this.title = title;
    this.message = message;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (this.getClass() != obj.getClass())
      return false;
    final VSBoardMessage other = (VSBoardMessage) obj;
    if (this.message == null) {
      if (other.message != null)
        return false;
    } else if (!this.message.equals(other.message))
      return false;
    if (this.title == null) {
      if (other.title != null)
        return false;
    } else if (!this.title.equals(other.title))
      return false;
    if (this.uid != other.uid)
      return false;
    return true;
  }

  /**
   * @return
   */
  public String getMessage() {
    return this.message;
  }

  /**
   * @return
   */
  public String getTitle() {
    return this.title;
  }

  /**
   * @return
   */
  public int getUid() {
    return this.uid;
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
    result = (prime * result) + ((this.message == null) ? 0 : this.message.hashCode());
    result = (prime * result) + ((this.title == null) ? 0 : this.title.hashCode());
    result = (prime * result) + this.uid;
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Form: " + this.uid + " \nTitle: " + this.title + " \nMessage: " + this.message;
  }
}
