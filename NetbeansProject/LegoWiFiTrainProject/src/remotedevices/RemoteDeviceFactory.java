/*
 * Not licensed yet, use at your own risk, no warrenties!
 */
package remotedevices;

import java.io.IOException;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public interface RemoteDeviceFactory
{
    public String getDeviceTypeName();
    public int getDeviceType();
    public int getDeviceVersion();
    public int getMaxPackageSize();
    public boolean matches(RemoteDeviceConnection connection);
    public RemoteDevice createNewConnectedDevice(RemoteDeviceConnection connection) throws IOException;
    public RemoteDevice createNewUnconnectedDevice(long deviceId);
}
