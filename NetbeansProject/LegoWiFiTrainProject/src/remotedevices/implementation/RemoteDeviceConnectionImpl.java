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

    long deviceId;
    int deviceType;
    int deviceVersion;
    int maxPackageSize;
    private volatile boolean connected;
    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private int[] byteBuffer;
    //private RemoteDevice device;
    private RemoteDeviceCallbacks device;

    public RemoteDeviceConnectionImpl(Socket socket) throws IOException
    {
        connected = false;
        deviceId = 0;
        deviceType = 0;
        deviceVersion = 0;
        maxPackageSize = 0;
        try
        {
            in = socket.getInputStream();
            out = socket.getOutputStream();
            deviceId = readInteger(8);
            deviceType = (int) readInteger(4);
            deviceVersion = (int) readInteger(4);
            maxPackageSize = (int) readInteger(2);
            byteBuffer = new int[maxPackageSize];
        } catch (IOException ex)
        {
            onIOException(ex);
        }
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
    public synchronized void acceptConnectionWithDevice(RemoteDeviceCallbacks device) throws IOException
    {
        try
        {
            this.device = device;

            sendResponse(ErrorCode.ACCEPTED);

            //read initialization package
            int size = (int) readInteger(2);
            System.out.println("Initialization package size: " + size);
            //Check if connection has timed out
            if(size == 65535) throw new IOException("Connection has timed out");
            for (int i = 0; i < size; ++i)
            {
                byteBuffer[i] = in.read();
                System.out.println(byteBuffer[i]);
            }
            System.out.println("Initalization package read!");
            connected = true;
            device.onConnected(this, byteBuffer, size);
            Thread reader = new Thread(new ReaderThread());
            reader.start();
        } catch (IOException ex)
        {
            onIOException(ex);
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
            try
            {
                onIOException(ex);
            } catch (IOException ex1)
            {
                
            }
        }
    }

    @Override
    public void sendPackage(int[] byteBuffer, int off, int len) throws IOException
    {
        if (!connected)
        {
            throw new IOException("Not connected!");
        }
        try
        {
            writeInteger(len, 2);
            for (int i = off; i < off + len; ++i)
            {
                out.write(byteBuffer[i]);
            }
            out.flush();
        } catch (IOException ex)
        {
            onIOException(ex);
        }
    }

    @Override
    public void sendPackage(int[] byteBuffer, int len) throws IOException
    {
        sendPackage(byteBuffer, 0, len);
    }

    @Override
    public void sendSingleByte(int b) throws IOException
    {
        if (!connected)
        {
            throw new IOException("Connection lost!");
        }
        try
        {

            out.write(1);
            out.write(b);
            out.flush();
        } catch (IOException ex)
        {
            onIOException(ex);
        }
    }

    @Override
    public synchronized boolean isConnected()
    {
        return connected;
    }

    private synchronized long readInteger(int size) throws IOException
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

    private synchronized void onIOException(IOException e) throws IOException
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
            socket = null;
        }
        throw e;
    }

    @Override
    public synchronized void close()
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
            socket = null;
        }
    }

    private class ReaderThread implements Runnable
    {
        @Override
        public void run()
        {
            while (connected)
            {
                try
                {
                    int size = (int) readInteger(2);
                    for (int i = 0; i < size; ++i)
                    {
                        int b = in.read();
                        byteBuffer[i] = b < 0 ? 256 + b : b;
                    }
                    device.onPackageReceived(byteBuffer, size);
                } catch (IOException ex)
                {
                    try
                    {
                        onIOException(ex);
                    } catch (IOException ex1)
                    {
                        
                    }
                }
            }
            System.out.println("Readerthread has stopped!");
        }
    }
    
    

}
