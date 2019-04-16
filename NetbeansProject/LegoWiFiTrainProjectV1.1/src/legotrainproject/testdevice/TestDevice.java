/*
 * Not licensed yet, use at your own risk, no warrenties!
 */
package legotrainproject.testdevice;

import remotedevices.RemoteDevice;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public interface TestDevice extends RemoteDevice
{
    @Override
    public long getDeviceId();
    @Override
    public boolean isConnected();
    
    public void setLED(boolean value);
    
    public void addListener(TestDeviceListener listener);
    public void removeListener(TestDeviceListener listener); 
}
