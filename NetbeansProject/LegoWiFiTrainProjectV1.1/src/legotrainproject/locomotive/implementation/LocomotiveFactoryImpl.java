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
    private final Set<LocomotiveFactoryListener> listeners;
    
    public LocomotiveFactoryImpl()
    {
        this.listeners = new HashSet<>();
    }
    
    
    @Override
    public String getDeviceTypeName()
    {
        return "LegoLocomotive";
    }

    @Override
    public int getDeviceType()
    {
        return 2;
    }

    @Override
    public int getDeviceVersion()
    {
        return 1;
    }

    @Override
    public int getMaxPackageSize()
    {
        return 4;
    }


    @Override
    public void addListener(LocomotiveFactoryListener listener)
    {
        listeners.add(listener);
    }

    @Override
    public boolean removeListener(LocomotiveFactoryListener listener)
    {
        return listeners.remove(listener);
    }
    
    @Override
    public LocomotiveV2Impl newLocomotive(long deviceId)
    {
        LocomotiveV2Impl res = new LocomotiveV2Impl(deviceId, this);
        for(LocomotiveFactoryListener listener : listeners)
        {
            listener.onNewLocomotive(res);
        }
        return res;
    }

    @Override
    public RemoteDevice newRemoteDevice(long deviceId)
    {
        return newLocomotive(deviceId);
    }

}
