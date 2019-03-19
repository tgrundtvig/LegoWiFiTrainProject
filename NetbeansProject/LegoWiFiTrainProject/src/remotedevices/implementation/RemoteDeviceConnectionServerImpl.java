/*
 * Not licensed yet, use at your own risk, no warrenties!
 */
package remotedevices.implementation;

import remotedevices.RemoteDeviceServer;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public class RemoteDeviceConnectionServerImpl implements Runnable
{
    private RemoteDeviceServer manager;

    public RemoteDeviceConnectionServerImpl(RemoteDeviceServer manager)
    {
        this.manager = manager;
    }
    
    @Override
    public void run()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
