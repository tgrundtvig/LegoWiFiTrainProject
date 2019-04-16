/*
 * Not licensed yet, use at your own risk, no warrenties!
 */
package remotedevices.implementation;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import remotedevices.RemoteDevice;
import remotedevices.RemoteDeviceConnection;
import remotedevices.RemoteDeviceFactory;
import remotedevices.RemoteDeviceListener;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public abstract class AbstractRemoteDevice implements RemoteDevice
{

    private final long deviceId;
    private final RemoteDeviceFactory factory;
    private final Set<RemoteDeviceListener> listeners;
    private volatile RemoteDeviceConnection connection;
    private boolean connected;
    
    public AbstractRemoteDevice(long deviceId, RemoteDeviceFactory factory)
    {
        this.deviceId = deviceId;
        this.factory = factory;
        this.listeners = new HashSet<>();
        this.connection = null;
        this.connected = false;
    }
    
    protected abstract void onDeviceConnected(int[] stateData);

    protected abstract void onDeviceDisconnected();
    
    protected final int getMaxPackageSize()
    {
        return factory.getMaxPackageSize();
    }
    
    protected final boolean sendPackage(int[] byteBuffer, int off, int len)
    {
        if (connection == null)
        {
            return false;
        }
        return connection.sendPackage(byteBuffer, off, len);
    }
    
    protected final void sendPackage(int[] byteBuffer, int len) throws IOException
    {
        if (connection == null)
        {
            throw new IOException("No connection!");
        }
        connection.sendPackage(byteBuffer, len);
    }

    protected final void sendSingleByte(int b) throws IOException
    {
        if (connection == null)
        {
            throw new IOException("No connection!");
        }
        connection.sendSingleByte(b);
    }
    
    @Override
    public final long getDeviceId()
    {
        return deviceId;
    }
    
    @Override
    public final int getDeviceType()
    {
        return factory.getDeviceType();
    }
    
    @Override
    public final String getDeviceTypeName()
    {
        return factory.getDeviceTypeName();
    }
    
    @Override
    public final int getDeviceVersion()
    {
        return factory.getDeviceVersion();
    }
    
    @Override
    public final boolean isConnected()
    {
        return connected;
    }
    
    @Override
    public final RemoteDeviceConnection getConnection()
    {
        return connection;
    }
    
    @Override
    public final void onIdentified(RemoteDeviceConnection remoteDeviceConnection)
    {
        if(this.connection != null)
        {
            this.connection.close();
            this.connection = null;
        }
        if (getDeviceId() != remoteDeviceConnection.getDeviceId())
        {
            System.out.println("Wrong id");
            throw new IllegalArgumentException("Device ID's must match. This is: " + deviceId + " connection has " + remoteDeviceConnection.getDeviceId());
        }
        if (getDeviceType() != remoteDeviceConnection.getDeviceType())
        {
            System.out.println("Wrong type");
            throw new IllegalArgumentException("This is not a connection to a type " + getDeviceType() + ". Actual type but is " + remoteDeviceConnection.getDeviceType());
        }
        if (getDeviceVersion() != remoteDeviceConnection.getDeviceVersion())
        {
            System.out.println("Wrong version");
            throw new IllegalArgumentException("This is not a connection to a version " + getDeviceVersion() + ". Type should be version 1, but is " + remoteDeviceConnection.getDeviceVersion());
        }
        if (factory.getMaxPackageSize() != remoteDeviceConnection.getMaxPackageSize())
        {
            System.out.println("Wrong max packagesize");
            throw new IllegalArgumentException("This connection does not have the correct maximum package size. It should be: "
                    + factory.getMaxPackageSize() + ", but it is: " + remoteDeviceConnection.getMaxPackageSize());
        }
        this.connection = remoteDeviceConnection;
    }
    
    @Override
    public void onInitialisationPackage(int[] initPackage)
    {
        System.out.println(getDeviceTypeName() + ": " + getDeviceId() + " connected!");
        connected = true;
        onDeviceConnected(initPackage);
        for(RemoteDeviceListener listener : listeners)
        {
            listener.onDeviceConnected(this);
        } 
    }
    
    @Override
    public final void onDisconnected()
    {
        if(this.connection != null)
        {
            this.connection = null;
            if(connected)
            {
                System.out.println(getDeviceTypeName() + ": " + getDeviceId() + " disconnected!");
                onDeviceDisconnected();
                for (RemoteDeviceListener listener : listeners)
                {
                    listener.onDeviceDisonnected(this);
                }
            }
        }
        connected = false;
    }
    
    @Override
    public synchronized final void addListener(RemoteDeviceListener listener)
    {
        listeners.add(listener);
    }
    
    @Override
    public synchronized final boolean removeListener(RemoteDeviceListener listener)
    {
        return listeners.remove(listener);
    }    
}
