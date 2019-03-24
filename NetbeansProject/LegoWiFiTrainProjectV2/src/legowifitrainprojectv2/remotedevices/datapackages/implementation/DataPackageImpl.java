/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package legowifitrainprojectv2.remotedevices.datapackages.implementation;

import legowifitrainprojectv2.remotedevices.datapackages.DataPackage;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public class DataPackageImpl implements DataPackage
{
    private final int[] data;

    public DataPackageImpl(int[] data)
    {
        this.data = data;
    }

    @Override
    public int getSize()
    {
        return data.length;
    }

    @Override
    public int getByte(int index)
    {
        return data[index];
    }
    
    
}
