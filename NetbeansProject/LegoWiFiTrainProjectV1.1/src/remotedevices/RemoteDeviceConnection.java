/*
 * Not licensed yet, use at your own risk, no warrenties!
 */
package remotedevices;

import java.io.Closeable;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public interface RemoteDeviceConnection extends Closeable, Updateable
{
    public long getDeviceId();
    public int getDeviceType();
    public int getDeviceVersion();
    public int getMaxPackageSize();
    public boolean isAlive();
    public boolean sendPackage(int[] byteBuffer,int off, int len);
    public boolean sendPackage(int[] byteBuffer,int len);
    public boolean sendPackage(int[] byteBuffer);
    public boolean sendSingleByte(int b);
    
    @Override
    public void close();    
    
    @Override
    public void update(long curTime);
}
