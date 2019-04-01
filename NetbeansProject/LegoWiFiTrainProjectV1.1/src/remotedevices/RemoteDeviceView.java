/*
 * Not licensed yet, use at your own risk, no warrenties!
 */
package remotedevices;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public interface RemoteDeviceView
{
    public long getDeviceId();
    public int getDeviceType();
    public String getDeviceTypeName();
    public int getDeviceVersion();
    public boolean isConnected();
    public RemoteDeviceConnection getConnection();
    public void addListener(RemoteDeviceListener listener);
    public boolean removeListener(RemoteDeviceListener listener);
}
