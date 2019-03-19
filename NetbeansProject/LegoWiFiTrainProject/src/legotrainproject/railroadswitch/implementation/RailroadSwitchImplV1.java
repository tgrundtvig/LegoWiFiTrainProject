/*
 * Not licensed yet, use at your own risk, no warrenties!
 */
package legotrainproject.railroadswitch.implementation;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import legotrainproject.railroadswitch.RailroadSwitch;
import legotrainproject.railroadswitch.RailroadSwitchListener;
import remotedevices.RemoteDeviceFactory;
import remotedevices.implementation.AbstractRemoteDevice;

//0 -> Unknown position
//1 -> Train goes left
//2 -> Train goes right
//3 -> Moving to the left position
//4 -> Moving to the right position
/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public class RailroadSwitchImplV1 extends AbstractRemoteDevice implements RailroadSwitch
{
    private Set<RailroadSwitchListener> listeners;
    private int[] byteBuffer;
    
    //State
    private int switchDirection;
    private int state;
    

    protected RailroadSwitchImplV1(long deviceId, RemoteDeviceFactory factory)
    {
        super(deviceId, factory);
        switchDirection = 0;
        state = 0;
        listeners = new HashSet<>();
        byteBuffer = new int[getMaxPackageSize()];
    }
    
    @Override
    public synchronized int getSwitchDirection()
    {
        return switchDirection;
    }

    @Override
    public synchronized int getSwitchState()
    {
        return state;
    }

    @Override
    public synchronized boolean switchTo(int position) throws IOException
    {
        if (position == 1 && state != 1 && state != 3)
        {
            setState(3);
            byteBuffer[0] = 1;
            sendPackage(byteBuffer, 1);
            return true;
        }
        if (position == 2 && state != 2 && state != 4)
        {
            setState(4);
            byteBuffer[0] = 2;
            sendPackage(byteBuffer, 1);
            return true;
        }
        return false;
    }

    @Override
    public synchronized void addListener(RailroadSwitchListener listener)
    {
        listeners.add(listener);
    }

    @Override
    public synchronized void removeListener(RailroadSwitchListener listener)
    {
        listeners.remove(listener);
    }

    private void setState(int newState)
    {
        if (newState != state)
        {
            int tmp = state;
            state = newState;
            for(RailroadSwitchListener listener : listeners)
            {
                listener.onRailroadSwitchStateChange(this, tmp, state);
            }
        }
    }

    @Override
    public synchronized void onDeviceConnected(int[] initData, int size) throws IOException
    {
        switchDirection = initData[0];
        state = initData[1];
        System.out.println(getDeviceTypeName() + ": " + getDeviceId() + " connected!");
    }

    @Override
    public synchronized void onPackageReceived(int[] byteData, int size)
    {
        int feedback = byteData[0];
        if(feedback <= 4)
        {
            setState(feedback);
        }
        else if(feedback == 5)
        {
            switchDirection = 1;
        }
        else if(feedback == 6)
        {
            switchDirection = 2;
        }
        else
        {
            System.out.println("Unknown feedback: " + feedback);
        }
    }

    @Override
    public synchronized void onDeviceDisconnected()
    {
        System.out.println(getDeviceTypeName() + ": " + getDeviceId() + " disconnected!");
    }
}
