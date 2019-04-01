/*
 * Not licensed yet, use at your own risk, no warrenties!
 */
package remotedevices;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public interface RemoteDeviceCallbacks
{
    public void onConnected(RemoteDeviceConnection remoteDeviceConnection, int[] stateData);
    public void onPackageReceived(int[] packageData);
    public void onDisconnected();
}
