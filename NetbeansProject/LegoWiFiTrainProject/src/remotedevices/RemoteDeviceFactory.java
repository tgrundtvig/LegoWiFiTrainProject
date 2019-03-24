/*
 * Not licensed yet, use at your own risk, no warrenties!
 */
package remotedevices;

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
    public RemoteDevice newRemoteDevice(long deviceId, RemoteDeviceServer server);
}
