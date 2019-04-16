/*
 * Not licensed yet, use at your own risk, no warrenties!
 */
package legotrainproject.testdevice;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public interface TestDeviceFactory
{
    public TestDevice newTestDevice(long deviceId);
    public void addListener(TestDeviceFactoryListener listener);
    public boolean removeListener(TestDeviceFactoryListener listener);
}
