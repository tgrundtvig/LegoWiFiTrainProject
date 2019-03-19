/*
 * Not licensed yet, use at your own risk, no warrenties!
 */
package remotedevices;

import java.io.Closeable;
import java.io.IOException;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public interface RemoteDeviceConnection extends Closeable
{
    public long getDeviceId();
    public int getDeviceType();
    public int getDeviceVersion();
    public int getMaxPackageSize();
    public boolean isConnected();
    public void acceptConnectionWithDevice(RemoteDeviceCallbacks device) throws IOException;
    public void rejectConnection(ErrorCode errorCode);
    public void sendPackage(int[] byteBuffer,int off, int len) throws IOException;
    public void sendPackage(int[] byteBuffer,int len) throws IOException;
    public void sendSingleByte(int b) throws IOException;
    
    @Override
    public void close();
    
    public enum ErrorCode {ACCEPTED, UNKNOWN_TYPE, UNKNOWN_VERSION, NOT_AUTHORIZED};
}
