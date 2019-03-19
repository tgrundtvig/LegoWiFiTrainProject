/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package remotedevices;

import java.util.List;

/**
 *
 * @author Tobias
 */
public interface RemoteDeviceServer
{
    public void startServer(int port);
    public void stopServer();
    public boolean containsDevice(long deviceId);
    public void addDevice(RemoteDevice device);
    public void removeDevice(RemoteDevice device);
    public List<RemoteDevice> getAllDevices();
    
}
