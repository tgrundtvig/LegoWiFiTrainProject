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
public class RailroadSwitchImpl extends AbstractRemoteDevice implements RailroadSwitch
{
    private final Set<RailroadSwitchListener> listeners;
    private final int[] byteBuffer;
    
    //State
    private int switchDirection;
    private int state;
    private int targetState;
    private boolean targetTransmitted;
    private long targetTransmittedTime;
    
    

    protected RailroadSwitchImpl(long deviceId, RemoteDeviceFactory factory)
    {
        super(deviceId, factory);
        switchDirection = 0;
        state = 0;
        targetState = 1;
        listeners = new HashSet<>();
        byteBuffer = new int[getMaxPackageSize()];
        targetTransmittedTime = 0;
    }
    
    @Override
    public int getSwitchDirection()
    {
        return switchDirection;
    }

    @Override
    public int getSwitchState()
    {
        return state;
    }

    @Override
    public void switchTo(int position)
    {
        switch(position)
        {
            case 1:   
                if(state != 1 && state != 3)
                {
                    targetState = 1;
                    targetTransmitted = false;
                    newState(3);
                }
                break;
            case 2:
                if(state != 2 && state != 4)
                {
                    targetState = 2;
                    targetTransmitted = false;
                    newState(4);
                }
                break;
            default:
                throw new RuntimeException("Invalid switch target state: " + position);
        }      
    }

    @Override
    public void addListener(RailroadSwitchListener listener)
    {
        listeners.add(listener);
    }

    @Override
    public void removeListener(RailroadSwitchListener listener)
    {
        listeners.remove(listener);
    }

    private void newState(int newState)
    {
        int tmp = state;
        state = newState;
        for(RailroadSwitchListener listener : listeners)
        {
            listener.onRailroadSwitchStateChange(this, tmp, state);
        }
    }

    @Override
    public void onDeviceConnected(int[] initData)
    {
        if(initData.length != 2)
        {
            throw new RuntimeException("Init data has wrong size, should be 2, but was " + initData.length);
        }
        switchDirection = initData[0];
        if(initData[1] != state)
        {
            newState(initData[1]);
        }
        targetTransmitted = false;
    }

    @Override
    public void onPackageReceived(int[] byteData)
    {
        int feedback = byteData[0];
        if(feedback <= 4)
        {
            if(feedback != state)
            {
                newState(feedback);
            }
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
    public void onDeviceDisconnected()
    {
        
    }

    @Override
    public void update(long curTime)
    {
        if(targetState != state && curTime - targetTransmittedTime > 5000)
        {
            targetTransmitted = false;
            if(isConnected())
            {
                //Ask for an update
                System.out.println("Asking for an update on the state...");
                byteBuffer[0] = 0;
                try
                {
                    sendPackage(byteBuffer, 1);
                } catch (IOException ex)
                {       
                }
            }
        }
        if(!targetTransmitted && isConnected())
        {
            System.out.print(getDeviceTypeName() + ": " + getDeviceId() + " transmitting target: " + targetState + " ... ");
            byteBuffer[0] = targetState;
            try
            {
                sendPackage(byteBuffer, 1);
                targetTransmitted = true;
                targetTransmittedTime = curTime;
            } catch (IOException ex)
            {
            }
            System.out.println(targetTransmitted);
        }
    }
}
