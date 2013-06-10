package vsue.rpc;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.rmi.Remote;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Set;

public class VSRemoteObjectManager {
    private final int                    port;

    private static VSRemoteObjectManager instance;

    public static synchronized VSRemoteObjectManager getInstance() {
        if (VSRemoteObjectManager.instance == null) {
            // wenn es noch keinen manager gibt, dann erzeuge einen
            try {
                VSRemoteObjectManager.instance = new VSRemoteObjectManager();
            } catch (final IOException ioe) {
                System.err.println("kann Socket nicht erstellen");
                ioe.printStackTrace();
            }
        }
        return VSRemoteObjectManager.instance;
    }

    private final Hashtable<Integer, Remote> objects = new Hashtable<>();
    private final Hashtable<Remote, Remote>  proxies = new Hashtable<>();

    private final ServerSocket               socket;

    private Integer                          ids     = new Integer(0);

    private VSRemoteObjectManager() throws IOException {
        this.socket = new ServerSocket(0);
        this.port = this.socket.getLocalPort();
        final VSServer worker = new VSServer(this);
        final Thread thread = new Thread(worker);
        thread.start();
    }

    /**
     * exportiert das Object
     * 
     * @param object
     * @return
     */
    public Remote exportObject(final Remote object) {
        Remote proxy = this.proxies.get(object);
        if (proxy != null) {
            // test ob schon exportiert
            return proxy;
        }

        int id = 0;
        // id generieren
        synchronized (this.ids) {
            id = this.ids;
            this.ids++;
            this.objects.put(id, object); // obj vorhalten im Speicher
        }

        try {
            VSRemoteReference reference;
            reference = new VSRemoteReference(InetAddress.getLocalHost().getHostName(), this.port, id);

            Class<?> realInterface = null;
            LinkedList<Class<?>> realInterfaces = new LinkedList<>();
            // von Remote abgeleitetes Interface suchen
            final Class<?>[] interfaces = object.getClass().getInterfaces();
            //interfaceCheck: for (final Class<?> interf : interfaces) {
            for (final Class<?> interf : interfaces) {
                if (Remote.class.isAssignableFrom(interf)) {
                    realInterface = interf;
                    realInterfaces.add(interf);
                    //break interfaceCheck;
                }
            }

            final ClassLoader remoteLoader = realInterface.getClassLoader();
            final Class<?>[] remoteInterfaces = new Class<?>[realInterfaces.size()];
            realInterfaces.toArray(remoteInterfaces);
            final VSInvocationHandler handler = new VSInvocationHandler(reference);

            proxy = (Remote) Proxy.newProxyInstance(remoteLoader, remoteInterfaces, handler);
            this.proxies.put(object, proxy);
            // der neue Proxy als Remote Obj
            return proxy;
        } catch (final UnknownHostException e) {
            System.err.println("Server is down");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gibt den Key das Objecs wieder, wenn dieser existiert, sonst -1
     * 
     * @param obj
     * @return
     */
    private int getKeyFormObject(final Remote obj) {
        synchronized (this.objects) {
            final Set<Integer> keys = this.objects.keySet();
            for (final int i : keys) {
                if (this.objects.get(i).equals(obj)) {
                    return i;
                }
            }
            return -1;
        }
    }

    public Hashtable<Integer, Remote> getObjects() {
        return this.objects;
    }

    public Remote getProxy(final Remote obj) {
        return this.proxies.get(obj);
    }

    public ServerSocket getSocket() {
        return this.socket;
    }

    /**
     * führt die Methode am Object aus mit den Args
     * 
     * @param objectID
     * @param genericMethodName
     * @param args
     * @return
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    public Object invokeMethod(final int objectID, final String genericMethodName, final Object[] args)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        final Object obj = this.objects.get(objectID);

        if (obj == null) { // teste ob Obj existiert
            throw new IllegalArgumentException("Object nicht vorhanden");
        }

        Class<?> realInterface = null;
        // das Interface holen, das von Remote ableitet
        final Class<?>[] interfaces = obj.getClass().getInterfaces();
        interfaceCheck: for (final Class<?> interf : interfaces) {
            if (Remote.class.isAssignableFrom(interf)) {
                realInterface = interf;
                break interfaceCheck;
            }
        }

        // die Methode suchen im von Remote abgeleiteten Interface
        for (final Method m : realInterface.getMethods()) {
            if (m.toGenericString().equals(genericMethodName)) {
                try {
                    final Object ret = m.invoke(obj, args);
                    if ((ret != null) && Remote.class.isAssignableFrom(ret.getClass())) {
                        if (this.proxies.get(ret) != null) {
                            return this.getProxy((Remote) ret);
                        }
                    }
                    return ret;
                } catch (final InvocationTargetException ite) {
                    return ite.getTargetException();
                }
            }
        }
        return null;
    }

    /**
     * unexpirtiert das Object
     * 
     * @param obj
     */
    public void unexport(final Remote obj) {
        if (this.proxies.get(obj) != null) {
            // test ist schneller da der proxy über das object bestimmbar ist
            this.proxies.remove(obj);
            // wenn es einen proxy gibt, dann gibt es dazu auch eine id
            this.objects.remove(this.getKeyFormObject(obj));
        }
    }
}
