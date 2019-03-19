/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package remotedevices;

import java.io.Closeable;
import java.io.IOException;

/**
 *
 * @author Tobias
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
