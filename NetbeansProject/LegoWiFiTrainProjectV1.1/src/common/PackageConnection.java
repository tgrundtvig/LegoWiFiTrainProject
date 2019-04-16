/*
 * Not licensed yet, use at your own risk, no warrenties!
 */
package common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import remotedevices.Updateable;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public class PackageConnection implements Updateable
{

    private Socket socket;
    private final InputStream in;
    private final OutputStream out;
    private int[] byteBuffer;
    private int[] readyPackage;
    private int bytesRead;
    private int packageSize;

    //Ping Pong control:
    private int pingInterval;
    private int pongTimeout;
    
    private boolean pingSent;
    private long pingSentTime;

    public PackageConnection(Socket socket) throws IOException
    {
        try
        {
            this.socket = socket;
            this.in = socket.getInputStream();
            this.out = socket.getOutputStream();
            this.byteBuffer = null;
            this.readyPackage = null;
            this.bytesRead = 0;
            this.packageSize = -1;
            this.pingInterval = -1;
            this.pongTimeout = -1;
            this.pingSentTime = -1;
            this.pingSent = false;
        } catch (IOException e)
        {
            socket.close();
            throw e;
        }
    }

    public void setPingPongTiming(int pingInterval, int pongTimeout)
    {
        this.pingInterval = pingInterval;
        this.pongTimeout = pongTimeout;
    }

    @Override
    public void update(long curTime)
    {
        readyPackage = null;
        if (socket == null)
        {
            return;
        }
        try
        {
            handlePingPong(curTime);
            readPackage(curTime);
        } catch (IOException e)
        {
            closeSocket();
        }
    }
    
    public int[] getPackageIfAvailable()
    {
        return readyPackage;
    }

    public void disconnect()
    {
        closeSocket();
    }

    public boolean isConnected()
    {
        return socket != null;
    }

    public void writePackage(int[] byteBuffer, int off, int len) throws IOException
    {
        try
        {
            out.write(len >> 8);
            out.write(len);
            for (int i = off; i < off + len; ++i)
            {
                out.write(byteBuffer[i]);
            }
            out.flush();
        } catch (IOException e)
        {
            closeSocket();
            throw e;
        }
    }

    public void writePackage(int[] byteBuffer, int len) throws IOException
    {
        writePackage(byteBuffer, 0, len);
    }

    public void writePackage(int[] byteBuffer) throws IOException
    {
        writePackage(byteBuffer, 0, byteBuffer.length);
    }
    
    private void handlePingPong(long curTime) throws IOException
    {
        if(pingInterval <= 0)
        {
            return;
        }
        if(pingSent)
        {
            if (curTime - pingSentTime > pongTimeout)
            {
                //We did not recieve a pong in due time
                System.out.println("Pong not received!");
                System.out.println("curTime: " + curTime);
                System.out.println("pingSentTime: " + pingSentTime);
                System.out.println("curTime - pingSentTime: " + (curTime- pingSentTime));
                System.out.println("pongTimeout: " + pongTimeout);
                throw new IOException("Did not get pong in due time.");
            }
        }
        else
        {
            if(curTime - pingSentTime > pingInterval)
            {
                //It is time to sent a ping
                //System.out.println("Ping sent!");
                out.write(0);
                out.write(0);
                out.flush();
                pingSent = true;
                pingSentTime = curTime;
            }
        }
    }
    
    private void readPackage(long curTime) throws IOException
    {
        while (in.available() > 0)
        {
            switch (bytesRead)
            {
                case 0:
                    byteBuffer = null;
                    packageSize = readByte();
                    packageSize <<= 8;
                    ++bytesRead;
                    break;
                case 1:
                    packageSize += readByte();
                    ++bytesRead;
                    //Check if connection has timed out
                    if (packageSize == 65535)
                    {
                        throw new IOException("Connection has timed out");
                    }
                    if (packageSize == 0)
                    {
                        //We have an empty package.
                        //This is a pong.
                        bytesRead = 0;
                        pingSent = false;
                        //System.out.println("Pong return time: " + (curTime - pingSentTime));
                    }
                    break;
                case 2:
                    byteBuffer = new int[packageSize];
                //break is intentionally omitted to let it fall through
                default:
                    byteBuffer[bytesRead - 2] = readByte();
                    ++bytesRead;
                    if (bytesRead == packageSize + 2)
                    {
                        bytesRead = 0;
                        readyPackage = byteBuffer;
                        byteBuffer = null;
                        //System.out.println("Recieved package of size: " + packageSize);
                        /*for(int i = 0; i < packageSize; ++i)
                        {
                            System.out.println("data["+i+"] : "+ readyPackage[i]);
                        }*/
                    }
            }
        }
    }

    private int readByte() throws IOException
    {
        int b = in.read();
        return b < 0 ? 256 + b : b;
    }

    private void closeSocket()
    {
        try
        {
            if (socket != null)
            {
                socket.close();
            }
        } catch (IOException ex)
        {
            System.err.println("Could not close socket!");
            System.err.println(ex);
        }
        socket = null;
    }

}
