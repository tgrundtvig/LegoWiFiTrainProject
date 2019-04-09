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

    private final ArrayList<RemoteDeviceConnection> connections;
    private final ArrayList<RemoteDeviceConnection> deadConnections;
    private final Set<RemoteDeviceFactory> factories;
    private final Map<Long, RemoteDevice> devices;
    private volatile boolean stopServer;
    private Thread serverThread;
    private SyncBox<Socket> socketBox = new SyncBox<>();

    public RemoteDeviceServerImpl()
    {
        connections = new ArrayList<>();
        deadConnections = new ArrayList<>();
        factories = new HashSet<>();
        devices = new HashMap<>();
        socketBox = new SyncBox<>();
        stopServer = true;
        serverThread = null;
    }

    @Override
    public void addFactory(RemoteDeviceFactory factory)
    {
        factories.add(factory);
    }

    @Override
    public boolean removeFactory(RemoteDeviceFactory factory)
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
        socketBox.stop();
        serverThread.interrupt();
    }

    @Override
    public synchronized boolean containsDevice(long deviceId)
    {
        return devices.containsKey(deviceId);
    }

    @Override
    public void addDevice(RemoteDevice device)
    {
        devices.put(device.getDeviceId(), device);
        if (device.isConnected())
        {
            connections.add(device.getConnection());
        }
    }

    @Override
    public boolean removeDevice(RemoteDevice device)
    {
        return devices.remove(device.getDeviceId()) != null;
    }

    @Override
    public List<RemoteDevice> getAllDevices()
    {
        ArrayList<RemoteDevice> res = new ArrayList<>(devices.size());
        for (RemoteDevice device : devices.values())
        {
            res.add(device);
        }
        return res;
    }

    @Override
    public void update(long curTime)
    {
        updateNewConnections();
        //Update connections.
        for(RemoteDeviceConnection connection : connections)
        {
            if(connection.isConnected())
            {
                connection.update(curTime);
            }
            else
            {
                deadConnections.add(connection);
            }
        }
        connections.removeAll(deadConnections);
        deadConnections.clear();
        //Update devices
        for(RemoteDevice device : devices.values())
        {
            device.update(curTime);
        }
    }

    public void updateNewConnections()
    {
        Socket socket = socketBox.getIfAvailable();
        if (socket != null)
        {
            RemoteDeviceConnection connection;
            try
            {
                connection = new RemoteDeviceConnectionImpl(socket);
            } catch (IOException ex)
            {
                try
                {
                    socket.close();
                } catch (IOException ex1)
                {
                }
                return;
            }
            RemoteDevice device = getDevice(connection);
            if (device == null)
            {
                connection.rejectConnection(RemoteDeviceConnection.ErrorCode.NO_MATCHING_FACTORY);
            } else
            {
                if(connection.acceptConnectionWithDevice(device))
                {
                    connections.add(connection);
                    devices.put(device.getDeviceId(), device);
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
