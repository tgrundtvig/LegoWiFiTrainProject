/*
 * Not licensed yet, use at your own risk, no warrenties!
 */
package legotrainproject.locomotive;

import remotedevices.RemoteDeviceServer;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public interface LocomotiveFactory
{
    public Locomotive newLocomotive(long deviceId, RemoteDeviceServer server);
    public void addListener(LocomotiveFactoryListener listener);
    public boolean removeListener(LocomotiveFactoryListener listener);
}
