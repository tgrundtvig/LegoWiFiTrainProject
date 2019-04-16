/*
 * Not licensed yet, use at your own risk, no warrenties!
 */

package common;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public class AsyncSocketServer
{
    private volatile boolean stopServer;
    private Thread serverThread;
    private final SyncBox<Socket> socketBox;

    public AsyncSocketServer()
    {
        stopServer = false;
        serverThread = null;
        socketBox = new SyncBox<>();
    }
    
    
    public synchronized void start(int port)
    {
        stopServer = false;
        serverThread = new Thread(new ServerThread(port));
        serverThread.start();
    }

    
    public synchronized void stop()
    {
        stopServer = true;
        socketBox.stop();
        serverThread.interrupt();
    }
    
    public Socket getSocketIfAvailable()
    {
        return socketBox.getIfAvailable();
    }
    
    private class ServerThread implements Runnable
    {

        private final int port;

        public ServerThread(int port)
        {
            this.port = port;
        }

        @Override
        public void run()
        {
            while (!stopServer)
            {
                try
                {
                    try (ServerSocket serverSocket = new ServerSocket(port))
                    {
                        while (!stopServer)
                        {
                            Socket socket = null;
                            try
                            {
                                socket = serverSocket.accept();
                                socketBox.put(socket);
                            } catch (IOException e)
                            {
                                if (socket != null)
                                {
                                    socket.close();
                                }
                            }
                        }
                    }
                } catch (IOException ex)
                {
                    System.out.println(ex);
                    System.out.println("Problem running server, will retry in 5 seconds...");
                    try
                    {
                        Thread.sleep(5000);
                    } catch (InterruptedException e)
                    {
                    }
                }
            }
        }
    }
}
