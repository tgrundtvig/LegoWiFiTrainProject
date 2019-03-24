/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package legowifitrainprojectv2.remotedevices.datapackages.implementation;

import java.io.IOException;
import java.io.OutputStream;
import legowifitrainprojectv2.common.DisconnectListener;
import legowifitrainprojectv2.remotedevices.datapackages.DataPackage;
import legowifitrainprojectv2.remotedevices.datapackages.DataPackageWriter;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public class DataPackageWriterImpl implements DataPackageWriter
{
    private OutputStream out;
    private DisconnectListener<DataPackageWriter> disconnectListener;

    public DataPackageWriterImpl(OutputStream out, DisconnectListener<DataPackageWriter> disconnectListener)
    {
        this.out = out;
        this.disconnectListener = disconnectListener;
    }
    
    
    
    @Override
    public boolean writeDataPackage(DataPackage dataPackage)
    {
        try
        {
            int size = dataPackage.getSize();
            out.write(size >> 8);
            out.write(size);
            for(int i = 0; i < size; ++i)
            {
                out.write(dataPackage.getByte(i));
            }
            return true;
        } catch (IOException ex)
        {
            if(out != null)
            {
                try
                {
                    out.close();
                } catch (IOException ex1){}
            }
            disconnectListener.onDisconnected(this);
            return false;
        }
    }
    
}
