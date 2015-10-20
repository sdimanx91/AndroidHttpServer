package ru.ds.AndroidHttpServer;

import android.content.Context;
import android.util.Log;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Server socket for the Service (HTTPService)
 */
public class HttpServerSocket {

    private Context mContext;
    private static final String TAG = "HttpServerSocket";
    private SocketListenerThread mSocketThread;
    private HttpRouter router;

    /**
     * Create HttpServerSocket and listen socket
     * @param port port for ServerSocket
     * @return HttpServerSocket instance if ServerSocket is created successfully or null otherwise
     */
    public static HttpServerSocket Listen(int port, HttpRouter router, Context context) {
        if (router == null) {
            Log.e(TAG, "HttpRouter is Null");
            return null;
        }
        Boolean createSuccess = true;
        HttpServerSocket creatingSocket = new HttpServerSocket(port, createSuccess, router, context);
        if (!createSuccess) {
            creatingSocket = null;
        }
        return creatingSocket;
    }

    /** Stop listening **/
    public void stop() {
        if (mSocketThread != null) {
            mSocketThread.stopListen();
        }
    }

    /**
     * HttpServerSocket constructor
     * @param port port for ServerSocket
     * @param success reference on Boolean value, if ServerSocket is'n created equally false or true otherwise
     */
    private HttpServerSocket(int port, Boolean success, HttpRouter router, Context context) {
        this.router = router;
        this.mContext = context;
        try {
            this.listenSocket(port);
        } catch (IOException e) {
            success = false;
            Log.e(TAG, "Can't create ServerSocket on port "
                    + Integer.toString(port));
            e.printStackTrace();
            return ;
        }
        success = true;
    }

    /**
     * Listen the opened server socket
     */
    private void listenSocket(int port) throws IOException {
        if (mSocketThread == null) {
            mSocketThread = new SocketListenerThread();
        }
        mSocketThread.startListen(port, mContext);
    }

    /**
     * in this thread we are listening socket
     */
    private class SocketListenerThread extends Thread
    {
        private int listeningPort;
        private ServerSocket mServerSocket;
        private AtomicBoolean mListening = new AtomicBoolean(true);
        private Context mContext;

        /** set ServerSocket and start thread for listening **/
        public void startListen(int port, Context context) {
            mContext = context;
            listeningPort = port;
            start();
        }

        /** Completion current iteration and stopping thread **/
        public void stopListen()  {
            mListening.set(false);
            try {
                mServerSocket.close();
            }
            catch (NullPointerException e) {
                Log.e(TAG, "mServerSocket = null");
            }
            catch (IOException e) {
                Log.e(TAG, "server socket closed");
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            super.run();
            try {
                InetAddress serverSocketAddress = NetUtils.getIPAddress();
                if (serverSocketAddress == null) {
                    Log.e(TAG, "Net connection error.");
                    return;
                }
                Log.d(TAG, "try start at: " +serverSocketAddress + ", and port " + Integer.toString(listeningPort));
                mServerSocket = new ServerSocket(listeningPort, 100,  serverSocketAddress);
                mListening.set(true);
                this.listenInThisThread();
            } catch (IOException e) {
                Log.e(TAG, "Socket listening stopped.");
                e.printStackTrace();
            }
        }

        /** listen the socket using accept method **/
        private void listenInThisThread() throws IOException {
            try {
                // todo: provide non-blocking socket
                while (mListening.get()) {
                    Socket socket = mServerSocket.accept();
                    new HttpRequestProcessor(socket, router, mContext).start();
                }
            }
            catch (SocketException se) {
                Log.e(TAG, "Socket closed.");
            }
            finally {
                mServerSocket.close();
            }
        }
    }
}
