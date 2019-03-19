/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package remotedevices;

import java.io.IOException;

/**
 *
 * @author Tobias
 */
public interface RemoteDeviceFactory
{
    public String getDeviceTypeName();
    public int getDeviceType();
    public int getDeviceVersion();
    public int getMaxPackageSize();
    public boolean matches(RemoteDeviceConnection connection);
    public RemoteDevice createNewConnectedDevice(RemoteDeviceConnection connection) throws IOException;
    public RemoteDevice createNewUnconnectedDevice(long deviceId);
}
