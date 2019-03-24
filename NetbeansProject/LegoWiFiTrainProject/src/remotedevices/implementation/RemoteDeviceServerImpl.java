/*
 * Not licensed yet, use at your own risk, no warrenties!
 */
package remotedevices.implementation;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import remotedevices.RemoteDevice;
import remotedevices.RemoteDeviceConnection;
import remotedevices.RemoteDeviceFactory;
import remotedevices.RemoteDeviceServer;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public class RemoteDeviceServerImpl implements RemoteDeviceServer
{

    private Set<RemoteDeviceFactory> factories;
    private Map<Long, RemoteDevice> devices;
    private volatile boolean stopServer;
    private Thread serverThread;

    public RemoteDeviceServerImpl()
    {
        factories = new HashSet<>();
        devices = new HashMap<>();
        stopServer = true;
        serverThread = null;
    }

    @Override
    public synchronized void addFactory(RemoteDeviceFactory factory)
    {
        factories.add(factory);
    }

    @Override
    public synchronized boolean removeFactory(RemoteDeviceFactory factory)
    {
        return factories.remove(factory);
    }

    @Override
    public synchronized void startServer(int port)
    {
        stopServer = false;
        serverThread = new Thread(new ServerThread(port));
        serverThread.start();
    }

    @Override
    public synchronized void stopServer()
    {
        stopServer = true;
        serverThread.interrupt();
    }

    @Override
    public synchronized boolean containsDevice(long deviceId)
    {
        return devices.containsKey(deviceId);
    }

    @Override
    public synchronized void addDevice(RemoteDevice device)
    {
        devices.put(device.getDeviceId(), device);
    }

    @Override
    public synchronized boolean removeDevice(RemoteDevice device)
    {
        return devices.remove(device.getDeviceId()) != null;
    }

    @Override
    public synchronized List<RemoteDevice> getAllDevices()
    {
        ArrayList<RemoteDevice> res = new ArrayList<>(devices.size());
        for (RemoteDevice device : devices.values())
        {
            res.add(device);
        }
        return res;
    }

    private class ServerThread implements Runnable
    {

        private int port;

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
                                RemoteDeviceConnection connection = new RemoteDeviceConnectionImpl(socket);
                                RemoteDevice device = getDevice(connection);
                                if (device == null)
                                {
                                    connection.rejectConnection(RemoteDeviceConnection.ErrorCode.NO_MATCHING_FACTORY);
                                }
                                else
                                {
                                    connection.acceptConnectionWithDevice(device);
                                }
                            }
                            catch(IOException e)
                            {
                                if(socket != null)
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

        private RemoteDevice getDevice(RemoteDeviceConnection connection)
        {
            RemoteDevice device = devices.get(connection.getDeviceId());
            if (device == null)
            {
                for (RemoteDeviceFactory factory : factories)
                {
                    if (factory.matches(connection))
                    {
                        device = factory.newRemoteDevice(connection.getDeviceId(), RemoteDeviceServerImpl.this);
                        break;
                    }
                }
            }
            return device;
        }
    }

}
