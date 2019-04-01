/*
 * Not licensed yet, use at your own risk, no warrenties!
 */
package legotrainproject.railroadswitch.implementation;

import java.util.HashSet;
import java.util.Set;
import legotrainproject.railroadswitch.RailroadSwitchFactory;
import legotrainproject.railroadswitch.RailroadSwitchFactoryListener;
import remotedevices.RemoteDevice;
import remotedevices.RemoteDeviceServer;
import remotedevices.implementation.AbstractRemoteDeviceFactory;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public class RailroadSwitchFactoryImpl extends AbstractRemoteDeviceFactory implements RailroadSwitchFactory
{
    private Set<RailroadSwitchFactoryListener> listeners;

    public RailroadSwitchFactoryImpl()
    {
        this.listeners = new HashSet<>();
    }
    
    
    @Override
    public synchronized String getDeviceTypeName()
    {
        return "Railroad switch";
    }

    @Override
    public synchronized int getDeviceType()
    {
        return 1;
    }

    @Override
    public synchronized int getDeviceVersion()
    {
        return 1;
    }

    @Override
    public synchronized int getMaxPackageSize()
    {
        return 2;
    }


    @Override
    public synchronized void addListener(RailroadSwitchFactoryListener listener)
    {
        listeners.add(listener);
    }

    @Override
    public synchronized boolean removeListener(RailroadSwitchFactoryListener listener)
    {
        return listeners.remove(listener);
    }
    
    @Override
    public synchronized RailroadSwitchImplV1 newRailroadSwitch(long deviceId, RemoteDeviceServer server)
    {
        RailroadSwitchImplV1 res = new RailroadSwitchImplV1(deviceId, this);
        for(RailroadSwitchFactoryListener listener : listeners)
        {
            listener.onNewRailroadSwitch(res);
        }
        server.addDevice(res);
        return res;
    }

    @Override
    public synchronized RemoteDevice newRemoteDevice(long deviceId, RemoteDeviceServer server)
    {
        return newRailroadSwitch(deviceId, server);
    }

}