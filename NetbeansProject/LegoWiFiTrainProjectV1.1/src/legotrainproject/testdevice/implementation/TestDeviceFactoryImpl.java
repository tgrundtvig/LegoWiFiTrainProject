/*
 * Not licensed yet, use at your own risk, no warrenties!
 */

package legotrainproject.testdevice.implementation;

import java.util.HashSet;
import java.util.Set;
import legotrainproject.testdevice.TestDevice;
import legotrainproject.testdevice.TestDeviceFactory;
import legotrainproject.testdevice.TestDeviceFactoryListener;
import remotedevices.RemoteDevice;
import remotedevices.implementation.AbstractRemoteDeviceFactory;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public class TestDeviceFactoryImpl extends AbstractRemoteDeviceFactory implements TestDeviceFactory
{
    private final Set<TestDeviceFactoryListener> listeners;

    public TestDeviceFactoryImpl()
    {
        this.listeners = new HashSet<>();
    }

    @Override
    public void addListener(TestDeviceFactoryListener listener)
    {
        listeners.add(listener);
    }

    @Override
    public boolean removeListener(TestDeviceFactoryListener listener)
    {
        return listeners.remove(listener);
    }

    @Override
    public String getDeviceTypeName()
    {
        return "TestDevice";
    }

    @Override
    public int getDeviceType()
    {
        return 0;
    }

    @Override
    public int getDeviceVersion()
    {
        return 1;
    }

    @Override
    public int getMaxPackageSize()
    {
        return 1;
    }
    
    @Override
    public TestDevice newTestDevice(long deviceId)
    {
        TestDeviceImpl res = new TestDeviceImpl(deviceId, this);
        for(TestDeviceFactoryListener listener : listeners)
        {
            listener.onNewTestDevice(res);
        }
        return res;
    }

    @Override
    public RemoteDevice newRemoteDevice(long deviceId)
    {
        return newTestDevice(deviceId);
    }

}
