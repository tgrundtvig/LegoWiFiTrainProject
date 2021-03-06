/*
 * Not licensed yet, use at your own risk, no warrenties!
 */
package legotrainproject.railroadswitch;

import remotedevices.RemoteDevice;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public interface RailroadSwitch extends RemoteDevice
{
    @Override
    public long getDeviceId();
    //0 -> unknown, 1 -> left, 2 -> right
    public int getSwitchDirection();
    @Override
    public boolean isConnected();
    //1 -> left, 2 -> right
    public void switchTo(int position);
    
    //0 -> Unknown position
    //1 -> Train goes left
    //2 -> Train goes right
    //3 -> Moving to the left position
    //4 -> Moving to the right position
    public int getSwitchState();
    public void addListener(RailroadSwitchListener listener);
    public void removeListener(RailroadSwitchListener listener);
}
