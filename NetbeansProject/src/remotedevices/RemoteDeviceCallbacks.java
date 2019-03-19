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
public interface RemoteDeviceCallbacks
{
    public void onConnected(RemoteDeviceConnection remoteDeviceConnection, int[] stateData, int size) throws IOException;
    public void onPackageReceived(int[] packageData, int size) throws IOException;
    public void onDisconnected();
}
