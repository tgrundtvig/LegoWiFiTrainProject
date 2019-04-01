/*
 * Not licensed yet, use at your own risk, no warrenties!
 */

package remotedevices.implementation;

import remotedevices.RemoteDeviceConnection;
import remotedevices.RemoteDeviceFactory;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public abstract class AbstractRemoteDeviceFactory implements RemoteDeviceFactory
{
    @Override
    public synchronized final boolean matches(RemoteDeviceConnection connection)
    {
        return (    getDeviceType() == connection.getDeviceType() &&
                    getDeviceVersion() == connection.getDeviceVersion() &&
                    getMaxPackageSize() == connection.getMaxPackageSize()  );
    }
}
