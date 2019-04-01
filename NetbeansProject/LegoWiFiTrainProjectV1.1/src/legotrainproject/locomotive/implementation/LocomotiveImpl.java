/*
 * Not licensed yet, use at your own risk, no warrenties!
 */

package legotrainproject.locomotive.implementation;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import legotrainproject.locomotive.Locomotive;
import legotrainproject.locomotive.Locomotive.Direction;
import legotrainproject.locomotive.LocomotiveListener;
import remotedevices.RemoteDeviceFactory;
import remotedevices.implementation.AbstractRemoteDevice;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public class LocomotiveImpl extends AbstractRemoteDevice implements Locomotive
{
    private final Set<LocomotiveListener> listeners;
    private Direction curDirection;
    private int curSpeed;
    private long curPos;
    private int[] dataBuffer;

    public LocomotiveImpl(long deviceId, RemoteDeviceFactory factory)
    {
        super(deviceId, factory);
        this.listeners = new HashSet<>();
        curDirection = Direction.FORWARD;
        curSpeed = 0;
        curPos = 0;
        dataBuffer = new int[4];
    }
    
    @Override
    public int getSpeed()
    {
        return curSpeed;
    }
    
    @Override
    public Direction getDirection()
    {
        return curDirection;
    }
    
    @Override
    public void setDirection(Direction direction) throws IOException
    {
        curDirection = direction;
        sendState();
    }

    @Override
    public void setSpeed(int speed) throws IOException
    {
        curSpeed = speed;
        sendState();
    }

    @Override
    public long getPos()
    {
        return curPos;
    }
    
    @Override
    protected void onDeviceConnected(int[] stateData)
    {
        readState(stateData);
    }

    @Override
    protected void onDeviceDisconnected()
    {
        System.out.println("Locomotive disconnected!");
    }

    @Override
    public void onPackageReceived(int[] packageData)
    {
        readState(packageData);
    }

    

    private void sendState() throws IOException
    {
        dataBuffer[0] = curDirection.ordinal();
        dataBuffer[1] = curSpeed >> 8;
        dataBuffer[2] = curSpeed;
        
        sendPackage(dataBuffer, 3);
    }

    @Override
    public void addListener(LocomotiveListener listener)
    {
        listeners.add(listener);
    }

    @Override
    public void removeListener(LocomotiveListener listener)
    {
        listeners.remove(listener);
    }

    private void readState(int[] state)
    {
        long newPos = state[0];
        newPos <<= 8;
        newPos += state[1];
        newPos <<= 8;
        newPos += state[2];
        newPos <<= 8;
        newPos += state[3];
        Direction newDirection = Direction.values()[state[4]];
        int newSpeed = state[5];
        newSpeed <<= 8;
        newSpeed += state[6];
        boolean updPos = curPos != newPos;
        curPos = newPos;
        boolean updDirection = curDirection != newDirection;
        curDirection = newDirection;
        boolean updSpeed = curSpeed != newSpeed;
        curSpeed = newSpeed;
        for(LocomotiveListener listener : listeners)
        {
            if(updPos)
            {
                listener.onPositionChange(curPos);
            }
            if(updDirection)
            {
                listener.onDirectionChange(curDirection);
            }
            if(updSpeed)
            {
                listener.onSpeedChange(curSpeed);
            }
        }
    }

}
