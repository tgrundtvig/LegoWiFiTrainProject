/*
 * Not licensed yet, use at your own risk, no warrenties!
 */
package legotrainproject.railroadswitch.implementation;

import java.io.IOException;
import legotrainproject.railroadswitch.RailroadSwitch;
import remotedevices.RemoteDevice;
import remotedevices.RemoteDeviceConnection;
import remotedevices.RemoteDeviceFactory;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public class RailroadSwitchFactoryImpl implements RemoteDeviceFactory
{

    @Override
    public String getDeviceTypeName()
    {
        return "Railroad switch";
    }

    @Override
    public int getDeviceType()
    {
        return 1;
    }

    @Override
    public int getDeviceVersion()
    {
        return 1;
    }

    @Override
    public int getMaxPackageSize()
    {
        return 2;
    }

    @Override
    public RailroadSwitch createNewConnectedDevice(RemoteDeviceConnection connection) throws IOException
    {
        RailroadSwitchImplV1 res = new RailroadSwitchImplV1(connection.getDeviceId(), this);
        connection.acceptConnectionWithDevice(res);
        return res;
    }

    @Override
    public RemoteDevice createNewUnconnectedDevice(long deviceId)
    {
        return new RailroadSwitchImplV1(deviceId, this);
    }

    @Override
    public boolean matches(RemoteDeviceConnection connection)
    {
        return (    getDeviceType() == connection.getDeviceType() &&
                    getDeviceVersion() == connection.getDeviceVersion() &&
                    getMaxPackageSize() == connection.getMaxPackageSize()  );
    }
    
}
