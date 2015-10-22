package ru.ds.AndroidHttpServer;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

/**
 * Server socket for the Service (HTTPService)
 */
public class HttpServerSocket {

    private Context mContext;
    private static final String TAG = "HttpServerSocket";
    private SocketStarter mSocketThread;
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

    /**
     * Create HttpServerSocket and listen socket (secure: https)
     * @param port port for ServerSocket
     * @return HttpServerSocket instance if ServerSocket is created successfully or null otherwise
     */
    public static HttpServerSocket ListenSecure(int port, HttpRouter router, Context context, String keyStoreFile, String keyStorePass, String keyPassword) {
        if (context == null || keyStoreFile == null || keyPassword == null || keyStorePass == null) {
            return null;
        }
        try {
            AssetManager am = context.getAssets();

            KeyStore ks = KeyStore.getInstance("BKS");
            ks.load( am.open(keyStoreFile) ,keyStorePass.toCharArray());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, keyPassword.toCharArray());

            SSLContext sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(kmf.getKeyManagers(), null, null);

            Boolean success = new Boolean(true);
            HttpServerSocket socket = new HttpServerSocket(port, success, router, sslcontext, context);
            if (success.booleanValue() == false) {
                return null;
            }
            return socket;

        }
        catch (CertificateException e) {
            e.printStackTrace();
            return null;
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        }
        return null;
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
        if (mSocketThread == null) {
            mSocketThread = new SocketListenerThread(port, mContext);
        }
        mSocketThread.startListen();
        success = true;
    }

    /**
     * HttpServerSocket constructor for secure connection
     * @param port port for ServerSocket
     * @param success reference on Boolean value, if ServerSocket is'n created equally false or true otherwise
     */
    private HttpServerSocket(int port, Boolean success, HttpRouter router, SSLContext sslContext, Context context) {
        this.router = router;
        this.mContext = context;
        if (mSocketThread == null) {
            mSocketThread = new SecureListenerThread(sslContext,port, mContext);
        }
        mSocketThread.startListen();
        success = true;
    }

    private interface SocketStarter {
        public void startListen();
        public void stopListen();
    }

    /**
     * in this thread we are listening socket
     */
    private class SocketListenerThread extends Thread implements SocketStarter
    {
        protected int listeningPort;
        private ServerSocket mServerSocket;
        private AtomicBoolean mListening = new AtomicBoolean(true);
        private Context mContext;

        public SocketListenerThread(int port, Context context) {
            this.mContext      = context;
            this.listeningPort = port;
        }

        /** set ServerSocket and start thread for listening **/
        @Override
        public void startListen() {
            start();
        }

        /** Completion current iteration and stopping thread **/
        @Override
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
                mServerSocket = initServerSocket(serverSocketAddress);
                if (mServerSocket != null) {
                    mListening.set(true);
                    this.listenInThisThread();
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket listening stopped.");
                e.printStackTrace();
            }
        }

        protected ServerSocket initServerSocket(InetAddress serverSocketAddress) {
            try {
                return new ServerSocket(listeningPort, 100,  serverSocketAddress);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }


        /** listen the socket using accept method **/
        protected void listenInThisThread() throws IOException {
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
                mServerSocket = null;
            }
        }
    }

    /**
     * thread for secure connection
     */
    private class SecureListenerThread extends SocketListenerThread{
        private SSLContext mSslContext;

        public SecureListenerThread(SSLContext sslContext, int port, Context context) {
            super(port, context);
            this.mSslContext = sslContext;
        }

        protected ServerSocket initServerSocket(InetAddress serverSocketAddress) {
            try {
                return mSslContext.getServerSocketFactory().createServerSocket(this.listeningPort, 100, serverSocketAddress);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
