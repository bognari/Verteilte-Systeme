package vsue.communication.test;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

import vsue.communication.VSObjectConnection;

public class VSClient {
  private static final String HOST = "localhost";
  private static final int    PORT = 50000;

  public static void main(final String[] args) {
    final VSClient app = new VSClient();
    app.run();
  }

  private Socket             socket;

  private VSObjectConnection conn;

  public void run() {
    try {
      this.socket = new Socket(VSClient.HOST, VSClient.PORT);
      this.socket.setKeepAlive(true);
    } catch (final UnknownHostException e) {
      System.err.println("fail to find host");
      e.printStackTrace();
      return;
    } catch (final IOException e) {
      System.err.println("fail to connect with server");
      e.printStackTrace();
      return;
    }

    this.conn = new VSObjectConnection(this.socket);

    System.out.println("connected with server");

    final String[] strings = new String[3];
    strings[0] = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper nisi. Aenean vulputate eleifend tellus. Aenean leo ligula, porttitor eu, consequat vitae, eleifend ac, enim. Aliquam lorem ante, dapibus in, viverra quis, feugiat a, tellus. Phasellus viverra nulla ut metus varius laoreet. Quisque rutrum. Aenean imperdiet. Etiam ultricies nisi vel augue. Curabitur ullamcorper ultricies nisi. Nam eget dui. Etiam rhoncus. Maecenas tempus, tellus eget condimentum rhoncus, sem quam semper libero, sit amet adipiscing sem neque sed ipsum. Nam quam nunc, blandit vel, luctus pulvinar, hendrerit id, lorem. Maecenas nec odio et ante tincidunt tempus. Donec vitae sapien ut libero venenatis faucibus. Nullam quis ante. Etiam sit amet orci eget eros faucibus tincidunt. Duis leo. Sed fringilla mauris sit amet nibh. Donec sodales sagittis magna. Sed consequat, leo eget bibendum sodales, augue velit cursus nunc,";
    strings[1] = "test";
    strings[2] = "dsfahstrfaueolig obsnözgcef89aöes";

    final VSTestMessage message = new VSTestMessage(42, "Hello World", strings);

    try {
      this.conn.sendObject(message);
    } catch (final IOException e) {
      System.err.println("fail to send obj to server");
      e.printStackTrace();
    }

    try {
      final Object obj = this.conn.receiveObject();
      final VSTestMessage echoMessage = (VSTestMessage) obj;
      final int integer = echoMessage.getInteger();
      final String string = echoMessage.getString();
      final Object[] objs = echoMessage.getObjs();
      System.out.printf("Integer: %d\nString: %s\nObjs: %s\n", integer, string, Arrays.toString(objs));
    } catch (final ClassNotFoundException e) {
      System.err.println("fail to deserialize obj");
      e.printStackTrace();
    } catch (final IOException e) {
      System.err.println("fail to receiveObj from server");
      e.printStackTrace();
    }
  }
}
