/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package legowifitrainprojectv2.remotedevices.datapackages;

import java.io.Closeable;
import legowifitrainprojectv2.common.DisconnectListener;
import legowifitrainprojectv2.common.Notifiable;
import legowifitrainprojectv2.common.TimeoutListener;
import legowifitrainprojectv2.common.Updatable;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public interface DataPackageReader extends Updatable, Closeable
{
    public ByteInput getByteInput();
    public void setByteInput(ByteInput in);
    
    public int getInPackageTimeout();
    public int getBetweenPackageTimeout();
       
    public void setInPackageTimeout(int timeout);
    public void setBetweenPackageTimeout(int timeout);   

    //Liseners
    public DataPackageListener getDataPackageListener();
    public DisconnectListener<DataPackageReader> getDisconnectListener();
    
    public TimeoutListener<DataPackageReader> getInPackageTimeoutListener();
    public TimeoutListener<DataPackageReader> getBetweenPackagesTimeoutListener();
    
    public void setDataPackageListener(DataPackageListener dataPacketListener);
    public void setDisconnectListener(DisconnectListener<DataPackageReader> disconnectListener);
    
    public void setInPackageTimeoutListener(TimeoutListener<DataPackageReader> inPackageTimeoutListener);
    public void setBetweenPackagesTimeoutListener(TimeoutListener<DataPackageReader> betweenPackagesTimeoutListener);
    
    //Start and stop
    public void start();
    public boolean isRunning();
    public void stop(Notifiable<DataPackageReader> onStopped);
      
    //Close
    @Override
    public void close();
    
    //Update
    @Override
    public void update(long curTimeMillis);
}
