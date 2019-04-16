/*
 * Not licensed yet, use at your own risk, no warrenties!
 */
package legotrainproject.testdevice;

import remotedevices.RemoteDeviceServer;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public interface TestDeviceFactory
{
    public TestDevice newTestDevice(long deviceId, RemoteDeviceServer server);
    public void addListener(TestDeviceFactoryListener listener);
    public boolean removeListener(TestDeviceFactoryListener listener);
}
