/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package legowifitrainprojectv2.remotedevices.datapackages;

import legowifitrainprojectv2.common.DisconnectListener;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public interface DataPackageWriter
{
    public void setDisconnectListener(DisconnectListener<DataPackageReader> disconnectListener);
    public DisconnectListener<DataPackageReader> getDisconnectListener();
    boolean writeDataPackage(DataPackage dataPackage);
}
