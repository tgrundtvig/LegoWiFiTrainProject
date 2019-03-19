/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package remotedevices;

/**
 *
 * @author Tobias
 */
public interface RemoteDeviceListener
{
    public void onDeviceConnected(RemoteDevice device);
    public void onDeviceDisonnected(RemoteDevice device);
}
