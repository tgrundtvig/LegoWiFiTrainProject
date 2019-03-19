/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package remotedevices.implementation;

import remotedevices.RemoteDeviceServer;

/**
 *
 * @author Tobias
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
