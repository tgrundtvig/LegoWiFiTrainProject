/*
 * Not licensed yet, use at your own risk, no warrenties!
 */
package remotedevices;

import java.io.IOException;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public interface RemoteDeviceCallbacks
{
    public void onConnected(RemoteDeviceConnection remoteDeviceConnection, int[] stateData, int size) throws IOException;
    public void onPackageReceived(int[] packageData, int size) throws IOException;
    public void onDisconnected();
}
