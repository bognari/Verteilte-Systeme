package vsue.rpc;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.Socket;
import java.rmi.Remote;
import java.rmi.RemoteException;

import vsue.communication.VSObjectConnection;

@SuppressWarnings("serial")
public class VSInvocationHandler implements Serializable, InvocationHandler {
  private final VSRemoteReference remoteReference;

  public VSInvocationHandler(final VSRemoteReference remoteReference) {
    this.remoteReference = remoteReference;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
   * java.lang.reflect.Method, java.lang.Object[])
   */
  @Override
  public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
    if (Remote.class.isAssignableFrom(method.getDeclaringClass())) {
      // nur fernaufruf wenn die methode von da kommt
      try {
        final Socket socket = new Socket(this.remoteReference.getHost(), this.remoteReference.getPort());

        final VSObjectConnection conn = new VSObjectConnection(socket);
        try {
          if (args != null) { // leere Paramerer sind null -.-
            for (int i = 0; i < args.length; i++) {
              if (Remote.class.isAssignableFrom(args[i].getClass())) {
                // testen ob Parameter Remote Objects sind. (nur erste
                // Interation)
                final Remote argProxy = VSRemoteObjectManager.getInstance().getProxy((Remote) args[i]);
                if (argProxy != null) {
                  args[i] = argProxy;
                }
              }
            }
          }

          final VSPackageRequest request = new VSPackageRequest(this.remoteReference.getObjectID(), method, args);
          conn.sendObject(request);
          final VSPackageReply reply = (VSPackageReply) conn.receiveObject();
          if ((reply.getObj() != null) && Throwable.class.isInstance(reply.getObj())) {
            // testen ob ret eine Exception ist
            throw (Throwable) reply.getObj();
          }
          return reply.getObj();
        } catch (final IOException ioe) { // verbindung tot also weiterleiten
          throw ioe;
        } finally {
          socket.close();
        }
      } catch (final ConnectException ce) {
        throw new RemoteException("ConnectionException");
      }
    } else { // kein fernaufruf
      // alle aufrufe auf den Proxy erzeugen einen "Kreis", deswegen wird die
      // RemoteReference benutzt
      return method.invoke(this.remoteReference, args);
    }
  }
}
