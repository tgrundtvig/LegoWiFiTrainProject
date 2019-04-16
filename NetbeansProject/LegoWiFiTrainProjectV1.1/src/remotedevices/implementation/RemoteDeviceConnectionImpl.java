/*
 * Not licensed yet, use at your own risk, no warrenties!
 */
package remotedevices.implementation;

import common.BufferUtil;
import common.PackageConnection;
import java.io.IOException;
import remotedevices.RemoteDeviceConnection;
import remotedevices.RemoteDeviceCallbacks;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public class RemoteDeviceConnectionImpl implements RemoteDeviceConnection
{

    private final RemoteDeviceServerImpl serverImpl;
    private PackageConnection packageConnection;
    private RemoteDeviceCallbacks device;
    private int state;
    private long deviceId;
    private int deviceType;
    private int deviceVersion;
    private int maxPackageSize;
    private long stateStartTime;

    public RemoteDeviceConnectionImpl(PackageConnection packageConnection, RemoteDeviceServerImpl serverImpl)
    {
        this.serverImpl = serverImpl;
        this.packageConnection = packageConnection;
        this.device = null;
        this.state = 0;

        //Handshake data
        this.deviceId = -1;
        this.deviceType = -1;
        this.deviceVersion = -1;
        this.maxPackageSize = -1;
        this.stateStartTime = -1;
    }

    @Override
    public long getDeviceId()
    {
        return deviceId;
    }

    @Override
    public int getDeviceType()
    {
        return deviceType;
    }

    @Override
    public int getDeviceVersion()
    {
        return deviceVersion;
    }

    @Override
    public int getMaxPackageSize()
    {
        return maxPackageSize;
    }

    @Override
    public boolean sendPackage(int[] byteBuffer, int off, int len)
    {
        if (packageConnection == null)
        {
            return false;
        }
        try
        {
            packageConnection.writePackage(byteBuffer, off, len);
            return true;
        } catch (IOException ex)
        {
            onException(ex);
            return false;
        }
    }

    @Override
    public boolean sendPackage(int[] byteBuffer, int len)
    {
        return sendPackage(byteBuffer, 0, len);
    }

    @Override
    public boolean sendPackage(int[] byteBuffer)
    {
        return sendPackage(byteBuffer, 0, byteBuffer.length);
    }

    @Override
    public boolean sendSingleByte(int b)
    {
        int[] tmp = new int[1];
        tmp[0] = b;
        return sendPackage(tmp, 0, 1);
    }

    @Override
    public boolean isAlive()
    {
        return packageConnection != null;
    }

    @Override
    public void update(long curTime)
    {
        try
        {
            switch (state)
            {
                case 0:
                    stateStartTime = curTime;
                    ++state;
                //Break intentionally omitted to let it fall through.
                case 1:
                    //Handshake state
                    if (handleHandshake(curTime))
                    {
                        stateStartTime = curTime;
                        ++state;
                    }
                    break;
                case 2:
                    //Initialization state
                    if (handleInitialization(curTime))
                    {
                        stateStartTime = curTime;
                        ++state;
                    }
                    break;
                case 3:
                    //Normal update
                    handleUpdate(curTime);
                    break;
                case 4:
                    //We are disconnected
                    break;
                default:
                    throw new IllegalStateException("Unkonwn state: " + state);

            }
        } catch (IOException e)
        {
            onException(e);
        }
    }

    private void onException(IOException e)
    {
        System.out.println(deviceId + ": IOException -> " + e);
        close();
    }

    private boolean handleHandshake(long curTime) throws IOException
    {
        if (curTime - stateStartTime > 5000)
        {
            //We should have got the handshake by now...
            throw new IOException("Handshake did not arrive in due time!");
        }
        packageConnection.update(curTime);
        int[] packageBuffer = packageConnection.getPackageIfAvailable();
        if (packageBuffer == null)
        {
            //No package yet...
            return false;
        }
        //The expected size of the handshake package
        final int expectedSize = 16;

        if (packageBuffer.length != expectedSize)
        {
            throw new IOException("Handshake package has the wrong size. It should be " + expectedSize + " but was " + packageBuffer.length);
        }
        this.deviceId = BufferUtil.readIntegerFromBuffer(packageBuffer, 0, 8);
        this.deviceType = (int) BufferUtil.readIntegerFromBuffer(packageBuffer, 8, 2);
        this.deviceVersion = (int) BufferUtil.readIntegerFromBuffer(packageBuffer, 10, 2);
        this.maxPackageSize = (int) BufferUtil.readIntegerFromBuffer(packageBuffer, 12, 2);
        int pingInterval = (int) BufferUtil.readIntegerFromBuffer(packageBuffer, 14, 2);
        int pongTimeout = 1000;
        System.out.println("Handshake received!");
        //Get the device from the device server
        this.device = serverImpl.getDevice(this);
        int[] response = new int[1];
        if (device != null)
        {
            //Device accepted
            System.out.println("Device accepted!");
            response[0] = 0;
            packageConnection.writePackage(response);
            packageConnection.setPingPongTiming(pingInterval, pongTimeout);
            return true;
        }
        //Device rejected
        System.out.println("Device rejected!");
        response[0] = 1;
        packageConnection.writePackage(response);
        packageConnection.disconnect();
        throw new IOException("The device was rejected!");
    }

    private boolean handleInitialization(long curTime) throws IOException
    {
        if (curTime - stateStartTime > 5000)
        {
            //We should have got the initialization package by now...
            throw new IOException("Initialization package did not arrive in due time!");
        }
        packageConnection.update(curTime);
        int[] packageBuffer = packageConnection.getPackageIfAvailable();
        if (packageBuffer == null)
        {
            //No package yet...
            return false;
        }
        device.onConnected(this, packageBuffer);
        return true;
    }

    private void handleUpdate(long curTime) throws IOException
    {
        packageConnection.update(curTime);
        int[] packageBuffer = packageConnection.getPackageIfAvailable();
        if (packageBuffer != null)
        {
            device.onPackageReceived(packageBuffer);
        }
    }

    @Override
    public void close()
    {
        if (packageConnection != null)
        {
            packageConnection.disconnect();
            packageConnection = null;
        }
        if (device != null && state == 3)
        {
            device.onDisconnected();
        }
        device = null;
        state = 4;
    }
}
