/*
 * Not licensed yet, use at your own risk, no warrenties!
 */
package legotrainproject.locomotive.implementation;

import java.util.HashSet;
import java.util.Set;
import legotrainproject.locomotive.LocomotiveFactory;
import legotrainproject.locomotive.LocomotiveFactoryListener;
import remotedevices.RemoteDevice;
import remotedevices.RemoteDeviceServer;
import remotedevices.implementation.AbstractRemoteDeviceFactory;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public class LocomotiveFactoryImpl extends AbstractRemoteDeviceFactory implements LocomotiveFactory
{
    private Set<LocomotiveFactoryListener> listeners;
    
    public LocomotiveFactoryImpl()
    {
        this.listeners = new HashSet<>();
    }
    
    
    @Override
    public synchronized String getDeviceTypeName()
    {
        return "Locomotive";
    }

    @Override
    public synchronized int getDeviceType()
    {
        return 2;
    }

    @Override
    public synchronized int getDeviceVersion()
    {
        return 1;
    }

    @Override
    public synchronized int getMaxPackageSize()
    {
        return 7;
    }


    @Override
    public synchronized void addListener(LocomotiveFactoryListener listener)
    {
        listeners.add(listener);
    }

    @Override
    public synchronized boolean removeListener(LocomotiveFactoryListener listener)
    {
        return listeners.remove(listener);
    }
    
    @Override
    public synchronized LocomotiveImpl newLocomotive(long deviceId, RemoteDeviceServer server)
    {
        LocomotiveImpl res = new LocomotiveImpl(deviceId, this);
        for(LocomotiveFactoryListener listener : listeners)
        {
            listener.onNewLocomotive(res);
        }
        server.addDevice(res);
        return res;
    }

    @Override
    public synchronized RemoteDevice newRemoteDevice(long deviceId, RemoteDeviceServer server)
    {
        return newLocomotive(deviceId, server);
    }

}
