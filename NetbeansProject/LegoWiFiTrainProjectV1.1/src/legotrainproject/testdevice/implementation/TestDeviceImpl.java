/*
 * Not licensed yet, use at your own risk, no warrenties!
 */

package legotrainproject.testdevice.implementation;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import legotrainproject.testdevice.TestDevice;
import legotrainproject.testdevice.TestDeviceListener;
import remotedevices.RemoteDeviceFactory;
import remotedevices.implementation.AbstractRemoteDevice;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public class TestDeviceImpl extends AbstractRemoteDevice implements TestDevice
{
    private final Set<TestDeviceListener> listeners;
    private final int[] writeBuffer;
    private boolean ledState;
    private boolean valueTransmitted;

    public TestDeviceImpl(long deviceId, RemoteDeviceFactory factory)
    {
        super(deviceId, factory);
        listeners = new HashSet<>();
        writeBuffer = new int[1];
    }
    
    
    @Override
    protected void onDeviceConnected(int[] stateData)
    {
        ledState = stateData[0] == 1;
    }

    @Override
    protected void onDeviceDisconnected()
    {
    }

    @Override
    public void onPackageReceived(int[] packageData)
    {
        boolean newState = packageData[0] == 1;
        if(newState != ledState)
        {
            ledState = newState;
            for(TestDeviceListener listener : listeners)
            {
                listener.onLEDChange(ledState);
            }
        }   
    }

    @Override
    public void update(long curTime)
    {
        if(!valueTransmitted && isConnected())
        {
            valueTransmitted = transmitValue();
        }
    }

    @Override
    public void setLED(boolean value)
    {
        if(value != ledState)
        {
            ledState = value;
            valueTransmitted = false;
        } 
    }

    @Override
    public void addListener(TestDeviceListener listener)
    {
        listeners.add(listener);
    }

    @Override
    public void removeListener(TestDeviceListener listener)
    {
        listeners.remove(listener);
    }
    
    
    private boolean transmitValue()
    {
        writeBuffer[0] = ledState ? 1 : 0;
        try
        {
            sendPackage(writeBuffer, 1);      
            return true;
        } catch (IOException ex)
        {
            return false;
        }
    }
}
