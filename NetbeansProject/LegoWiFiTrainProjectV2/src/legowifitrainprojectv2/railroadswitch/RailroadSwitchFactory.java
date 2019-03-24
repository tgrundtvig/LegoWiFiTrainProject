/*
 * Not licensed yet, use at your own risk, no warrenties!
 */
package legotrainproject.railroadswitch;

import remotedevices.RemoteDeviceServer;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public interface RailroadSwitchFactory
{
    public RailroadSwitch newRailroadSwitch(long deviceId, RemoteDeviceServer server);
    public void addListener(RailroadSwitchFactoryListener listener);
    public boolean removeListener(RailroadSwitchFactoryListener listener);
}
