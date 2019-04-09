/*
 * Not licensed yet, use at your own risk, no warrenties!
 */

package legotrainproject.locomotive.implementation;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import legotrainproject.locomotive.Locomotive;
import legotrainproject.locomotive.LocomotiveListener;
import remotedevices.RemoteDeviceFactory;
import remotedevices.implementation.AbstractRemoteDevice;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public class LocomotiveV2Impl extends AbstractRemoteDevice implements Locomotive
{
    private final Set<LocomotiveListener> listeners;
    private final int[] dataBuffer;
    private int position;
    private int targetPosition;
    private boolean targetTransmitted;
    
    public LocomotiveV2Impl(long deviceId, RemoteDeviceFactory factory)
    {
        super(deviceId, factory);
        this.listeners = new HashSet<>();
        dataBuffer = new int[4];
        this.position = 0;
        this.targetPosition = 0;
        this.targetTransmitted = false;
    }
    
    @Override
    public int getTargetPosition()
    {
        return targetPosition;
    }
    
    @Override
    public int getPosition()
    {
        return position;
    }
    
    @Override
    protected void onDeviceConnected(int[] stateData)
    {
        targetTransmitted = false;
        readState(stateData);
    }

    @Override
    protected void onDeviceDisconnected()
    {
    }

    @Override
    public void onPackageReceived(int[] packageData)
    {
        readState(packageData);
    }

    @Override
    public void setTargetPosition(int targetPosition)
    {
        if(this.targetPosition != targetPosition)
        {
            this.targetPosition = targetPosition;
            this.targetTransmitted = false;
        }
        
    }

    @Override
    public void addLocomotiveListener(LocomotiveListener listener)
    {
        listeners.add(listener);
    }

    @Override
    public void removeLocomotiveListener(LocomotiveListener listener)
    {
        listeners.remove(listener);
    }

    private void readState(int[] state)
    {
        int newPos = state[0];
        newPos <<= 8;
        newPos += state[1];
        newPos <<= 8;
        newPos += state[2];
        newPos <<= 8;
        newPos += state[3];
        int magnetTime = state[4];
        magnetTime <<= 8;
        magnetTime += state[5];
        magnetTime <<= 8;
        magnetTime += state[6];
        magnetTime <<= 8;
        magnetTime += state[7];
        this.position = newPos;
        for(LocomotiveListener listener : listeners)
        {
            listener.onPositionChange(newPos, magnetTime);
        }
    }

    @Override
    public void update(long curTime)
    {
        if(!targetTransmitted && isConnected())
        {
            System.out.print(getDeviceTypeName() + ": " + getDeviceId() + " transmitting target: " + targetPosition + " ... ");
            targetTransmitted = transmitTarget();
            System.out.println(targetTransmitted);
        }
    }
    
    private boolean transmitTarget()
    {
        dataBuffer[0] = targetPosition >> 24;
        dataBuffer[1] = targetPosition >> 16;
        dataBuffer[2] = targetPosition >> 8;
        dataBuffer[3] = targetPosition;
        try
        {
            sendPackage(dataBuffer, 4);      
            return true;
        } catch (IOException ex)
        {
            return false;
        }
    }
}
