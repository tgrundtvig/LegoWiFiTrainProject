/*
 * Not licensed yet, use at your own risk, no warrenties!
 */
package remotedevices.implementation;

import common.AsyncSocketServer;
import common.PackageConnection;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
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

    private final LinkedList<RemoteDeviceConnection> connections;
    private final Set<RemoteDeviceFactory> factories;
    private final Map<Long, RemoteDevice> devices;
    private final AsyncSocketServer socketServer;
    private int connectionCount;

    public RemoteDeviceServerImpl()
    {
        connections = new LinkedList<>();
        factories = new HashSet<>();
        devices = new HashMap<>();
        socketServer = new AsyncSocketServer();
        connectionCount = 0;
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
        socketServer.start(port);
    }

    @Override
    public synchronized void stopServer()
    {
        socketServer.stop();
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
        return new ArrayList<>(devices.values());
    }

    @Override
    public void update(long curTime)
    {
        if(connections.size() != connectionCount)
        {
            connectionCount = connections.size();
            System.out.println("Number of connections: " + connectionCount);
        }
        updateNewConnections();
        //Update connections.
        Iterator<RemoteDeviceConnection> it = connections.iterator();
        while(it.hasNext())
        {
            RemoteDeviceConnection connection = it.next();
            if(connection.isAlive())
            {
                connection.update(curTime);
            }
            else
            {
                it.remove();
            }
        }
        
        //Update devices
        for (RemoteDevice device : devices.values())
        {
            device.update(curTime);
        }
    }

    private void updateNewConnections()
    {
        Socket socket = socketServer.getSocketIfAvailable();
        if (socket != null)
        {
            try
            {
                RemoteDeviceConnection connection = new RemoteDeviceConnectionImpl(new PackageConnection(socket), this);
                connections.add(connection);
            } catch (IOException ex)
            {
                try
                {
                    socket.close();
                } catch (IOException ex1)
                {
                }
            }
        }
    }

    protected RemoteDevice getDevice(RemoteDeviceConnection connection)
    {
        RemoteDevice device = devices.get(connection.getDeviceId());
        if (device == null)
        {
            for (RemoteDeviceFactory factory : factories)
            {
                if (factory.matches(connection))
                {
                    device = factory.newRemoteDevice(connection.getDeviceId(), this);
                    break;
                }
            }
        }
        return device;
    }
}
