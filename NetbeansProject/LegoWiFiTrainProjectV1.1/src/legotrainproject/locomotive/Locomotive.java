/*
 * Not licensed yet, use at your own risk, no warrenties!
 */
package legotrainproject.locomotive;

import remotedevices.RemoteDevice;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public interface Locomotive extends RemoteDevice
{
    
    @Override
    public long getDeviceId();
    @Override
    public boolean isConnected();
    
    public void setTargetPosition(int targetPosition);
    public int getTargetPosition();
    public int getPosition();
    public void addLocomotiveListener(LocomotiveListener listener);
    public void removeLocomotiveListener(LocomotiveListener listener); 
}
