package vsue.rpc;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import vsue.communication.VSObjectConnection;

public class VSServerWorker implements Runnable {

    private VSRemoteObjectManager manager;
    private ExecutorService       pool;

    @Override
    public void run() {
        Socket connectSocket;
        while (true) {
            try {
                System.out.println("waiting for connection");
                connectSocket = this.manager.getSocket().accept();
                this.pool.execute(new VSServerWorkerHandler(connectSocket, this));
            } catch (IOException e) {
                System.err.println("fail to connect with client");
                e.printStackTrace();
            }
        }
    }

    public VSServerWorker(VSRemoteObjectManager manager) {
        this.manager = manager;
        this.pool = Executors.newCachedThreadPool();
    }

    class VSServerWorkerHandler implements Runnable {
        private VSObjectConnection connect;
        private VSServerWorker     worker;

        public VSServerWorkerHandler(Socket socket, VSServerWorker worker) {
            this.connect = new VSObjectConnection(socket);
            this.worker = worker;
        }

        @Override
        public void run() {
            try {
                VSPackageRequest request = (VSPackageRequest) connect.receiveObject();
                Object ret = this.worker.manager.invokeMethod(request.getObjID(), request.getMethod(), request.getArgs());
                this.connect.sendObject(new VSPackageReply(ret));
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
                try {
                    this.connect.sendObject(e);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}
