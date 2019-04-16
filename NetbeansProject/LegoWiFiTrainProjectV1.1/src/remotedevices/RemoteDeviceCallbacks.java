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
    public void onIdentified(RemoteDeviceConnection remoteDeviceConnection);
    public void onInitialisationPackage(int[] initPackage);
    public void onPackageReceived(int[] packageData);
    public void onDisconnected();
}
