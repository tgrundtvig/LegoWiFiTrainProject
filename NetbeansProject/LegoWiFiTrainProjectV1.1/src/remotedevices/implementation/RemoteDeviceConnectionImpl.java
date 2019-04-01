/*
 * Not licensed yet, use at your own risk, no warrenties!
 */
package remotedevices.implementation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import remotedevices.RemoteDeviceConnection;
import remotedevices.RemoteDeviceCallbacks;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public class RemoteDeviceConnectionImpl implements RemoteDeviceConnection
{

    private final long deviceId;
    private final int deviceType;
    private final int deviceVersion;
    private final int maxPackageSize;
    private final Socket socket;
    private final InputStream in;
    private final OutputStream out;
    private int[] byteBuffer;
    private int size;
    private boolean connected;
    private int bytesRead;
    //private RemoteDevice device;
    private RemoteDeviceCallbacks device;

    public RemoteDeviceConnectionImpl(Socket socket) throws IOException
    {
        connected = false;
        bytesRead = 0;
        this.socket = socket;
        in = socket.getInputStream();
        out = socket.getOutputStream();
        deviceId = readInteger(8);
        deviceType = (int) readInteger(4);
        deviceVersion = (int) readInteger(4);
        maxPackageSize = (int) readInteger(2);
        byteBuffer = null;
        size = 0;
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
    public boolean acceptConnectionWithDevice(RemoteDeviceCallbacks device)
    {
        try
        {
            this.device = device;

            sendResponse(ErrorCode.ACCEPTED);

            //read initialization package
            int size = (int) readInteger(2);
            //Check if connection has timed out
            if (size == 65535)
            {
                throw new IOException("Connection has timed out");
            }
            byteBuffer = new int[size];
            for (int i = 0; i < size; ++i)
            {
                byteBuffer[i] = readByte();
            }
            connected = true;
            device.onConnected(this, byteBuffer);
            byteBuffer = null;
            return true;
        } catch (IOException ex)
        {
            onIOException();
            return false;
        }
    }

    @Override
    public void rejectConnection(ErrorCode errorCode)
    {
        if (errorCode == ErrorCode.ACCEPTED)
        {
            throw new IllegalArgumentException("Should use acceptDevice instead of errorCode: ACCEPTED.");
        }
        try
        {
            sendResponse(errorCode);
            close();
        } catch (IOException ex)
        {
            onIOException();
        }
    }

    @Override
    public boolean sendPackage(int[] byteBuffer, int off, int len)
    {
        if (!connected)
        {
            return false;
        }
        try
        {
            writeInteger(len, 2);
            for (int i = off; i < off + len; ++i)
            {
                out.write(byteBuffer[i]);
            }
            out.flush();
            return true;
        } catch (IOException ex)
        {
            onIOException();
            return false;
        }
    }

    @Override
    public boolean sendPackage(int[] byteBuffer, int len)
    {
        return sendPackage(byteBuffer, 0, len);
    }

    @Override
    public boolean sendSingleByte(int b)
    {
        if (!connected)
        {
            return false;
        }
        try
        {

            out.write(1);
            out.write(b);
            out.flush();
            return true;
        } catch (IOException ex)
        {
            onIOException();
            return false;
        }
    }

    @Override
    public boolean isConnected()
    {
        return connected;
    }

    private long readInteger(int size) throws IOException
    {
        long res = 0;
        for (int i = 0; i < size; ++i)
        {
            int b = in.read();
            res <<= 8;
            //make byte unsigned
            res += b < 0 ? 256 + b : b;
        }
        return res;
    }

    private void writeInteger(int data, int size) throws IOException
    {
        for (int i = size - 1; i >= 0; --i)
        {

            int tmp = data >> (i * 8);
            out.write(tmp);
        }

    }

    private void sendResponse(ErrorCode response) throws IOException
    {
        out.write(response.ordinal());
        out.flush();
    }

    private void onIOException()
    {
        connected = false;
        //ToDo: what about reader thread...
        if (device != null)
        {
            device.onDisconnected();
            device = null;
        }
        if (socket != null)
        {
            try
            {
                socket.close();
            } catch (IOException ex)
            {
            }
        }
    }

    @Override
    public void update(long curTime)
    {
        if (!connected)
        {
            return;
        }
        try
        {
            while (in.available() > 0)
            {
                switch (bytesRead)
                {
                    case 0:
                        size = readByte();
                        size <<= 8;
                        ++bytesRead;
                        break;
                    case 1:
                        size += readByte();
                        ++bytesRead;
                        if (size == 0)
                        {
                            //We have an empty package
                            bytesRead = 0;
                            device.onPackageReceived(null);
                            return;
                        }
                        break;
                    case 2:
                        byteBuffer = new int[size];
                    //break is intentionally omittesd to let it fall through
                    default:
                        byteBuffer[bytesRead - 2] = readByte();
                        ++bytesRead;
                        if (bytesRead == size + 2)
                        {
                            bytesRead = 0;
                            device.onPackageReceived(byteBuffer);
                            byteBuffer = null;
                            size = 0;
                            return;
                        }
                }
            }
        } catch (IOException e)
        {
            onIOException();
        }
    }

    @Override
    public void close()
    {
        connected = false;
        if (device != null)
        {
            device.onDisconnected();
            device = null;
        }
        if (socket != null)
        {
            try
            {
                socket.close();
            } catch (IOException ex)
            {
            }
        }
    }

    private int readByte() throws IOException
    {
        int b = in.read();
        return b < 0 ? 256 + b : b;
    }
}
