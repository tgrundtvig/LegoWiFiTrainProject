/*
 * Not licensed yet, use at your own risk, no warrenties!
 */

package legotrainproject.locomotive.implementation;

import java.io.IOException;
import legotrainproject.locomotive.Locomotive;
import legotrainproject.locomotive.Locomotive.Direction;
import remotedevices.RemoteDeviceFactory;
import remotedevices.implementation.AbstractRemoteDevice;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public class LocomotiveImpl extends AbstractRemoteDevice implements Locomotive
{
    private volatile Direction curDirection;
    private volatile int curSpeed;
    private volatile int curPos;
    private volatile int[] dataBuffer;

    public LocomotiveImpl(long deviceId, RemoteDeviceFactory factory)
    {
        super(deviceId, factory);
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
    public int getPos()
    {
        return curPos;
    }
    
    @Override
    protected void onDeviceConnected(int[] stateData, int size) throws IOException
    {
        curDirection = Direction.values()[stateData[0]];
        curSpeed = stateData[1];
        curPos = stateData[2];
        curPos <<= 8;
        curPos += stateData[3];
    }

    @Override
    protected void onDeviceDisconnected()
    {
        System.out.println("Locomotive disconnected!");
    }

    @Override
    public void onPackageReceived(int[] packageData, int size) throws IOException
    {
        curDirection = Direction.values()[packageData[0]];
        curSpeed = packageData[1];
        curPos = packageData[2];
        curPos <<= 8;
        curPos += packageData[3];
    }

    

    private void sendState() throws IOException
    {
        dataBuffer[0] = curDirection.ordinal();
        dataBuffer[1] = curSpeed;
        sendPackage(dataBuffer, 2);
    }

    

}
