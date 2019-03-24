/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package legowifitrainprojectv2.remotedevices.datapackages.implementation;

import java.io.IOException;
import legowifitrainprojectv2.common.DisconnectListener;
import legowifitrainprojectv2.common.Notifiable;
import legowifitrainprojectv2.common.TimeoutListener;
import legowifitrainprojectv2.remotedevices.datapackages.ByteInput;
import legowifitrainprojectv2.remotedevices.datapackages.DataPackage;
import legowifitrainprojectv2.remotedevices.datapackages.DataPackageListener;
import legowifitrainprojectv2.remotedevices.datapackages.DataPackageReader;

/**
 *
 * @author Tobias Grundtvig
 */
public class DataPackageReaderImpl implements DataPackageReader
{

    private static final DataPackage EMPTY_PACKAGE = new EmptyDataPackageImpl();
    private int[] data;
    private int size;
    private ByteInput in;
    private DataPackageListener dataPackagelistener;
    private TimeoutListener<DataPackageReader> inPackageTimeoutListener;
    private TimeoutListener<DataPackageReader> betweenPackagesTimeoutListener;
    private DisconnectListener<DataPackageReader> disconnectListener;
    private Notifiable<DataPackageReader> onStoppedListener;

    private int inPackageTimeout;
    private int betweenPackagesTimeout;
    private long lastActivity;

    private int bytesRead;

    private State state;

    private enum State
    {
        STARTING, RUNNING, STOPPING, STOPPED, CLOSED
    }
    
    public DataPackageReaderImpl()
    {
        data = null;
        this.in = null;
        this.dataPackagelistener = null;
        this.inPackageTimeoutListener = null;
        this.betweenPackagesTimeoutListener = null;
        this.disconnectListener = null;
        this.inPackageTimeout = 0;
        this.betweenPackagesTimeout = 0;
        this.state = State.STOPPED;
        bytesRead = 0;
        size = 0;
    }

    @Override
    public ByteInput getByteInput()
    {
        checkIfConfigurable();
        return in;
    }

    @Override
    public void setByteInput(ByteInput in)
    {
        checkIfConfigurable();
        this.in = in;
    }

    @Override
    public int getInPackageTimeout()
    {
        checkIfConfigurable();
        return this.inPackageTimeout;
    }

    @Override
    public int getBetweenPackageTimeout()
    {
        checkIfConfigurable();
        return this.betweenPackagesTimeout;
    }

    @Override
    public void setInPackageTimeout(int timeout)
    {
        checkIfConfigurable();
        this.inPackageTimeout = timeout;
    }

    @Override
    public void setBetweenPackageTimeout(int timeout)
    {
        checkIfConfigurable();
        this.betweenPackagesTimeout = timeout;
    }

    @Override
    public DataPackageListener getDataPackageListener()
    {
        checkIfConfigurable();
        return dataPackagelistener;
    }

    @Override
    public DisconnectListener<DataPackageReader> getDisconnectListener()
    {
        checkIfConfigurable();
        return disconnectListener;
    }

    @Override
    public TimeoutListener<DataPackageReader> getInPackageTimeoutListener()
    {
        checkIfConfigurable();
        return inPackageTimeoutListener;
    }

    @Override
    public TimeoutListener<DataPackageReader> getBetweenPackagesTimeoutListener()
    {
        checkIfConfigurable();
        return betweenPackagesTimeoutListener;
    }

    @Override
    public void setDataPackageListener(DataPackageListener dataPacketListener)
    {
        checkIfConfigurable();
        this.dataPackagelistener = dataPacketListener;
    }

    @Override
    public void setDisconnectListener(DisconnectListener<DataPackageReader> disconnectListener)
    {
        checkIfConfigurable();
        this.disconnectListener = disconnectListener;
    }

    @Override
    public void setInPackageTimeoutListener(TimeoutListener<DataPackageReader> inPackageTimeoutListener)
    {
        checkIfConfigurable();
        this.inPackageTimeoutListener = inPackageTimeoutListener;
    }

    @Override
    public void setBetweenPackagesTimeoutListener(TimeoutListener<DataPackageReader> betweenPackagesTimeoutListener)
    {
        checkIfConfigurable();
        this.betweenPackagesTimeoutListener = betweenPackagesTimeoutListener;
    }

    @Override
    public void close()
    {
        state = State.CLOSED;
        if (in != null)
        {
            in.close();
            in = null;
        }
        
    }

    @Override
    public void start()
    {
        if(state != State.STOPPED) throw new IllegalStateException("State must be STOPPED before start can be called!");
        state = State.STARTING;
    }

    @Override
    public boolean isRunning()
    {
        return (state == State.STARTING || state == State.RUNNING || state == State.STOPPING);
    }

    @Override
    public void stop(Notifiable<DataPackageReader> onStoppedListener)
    {
        if(state == State.STARTING || (state == State.RUNNING && bytesRead == 0))
        {
            state = State.STOPPED;
            onStoppedListener.notify(this);
            return;
        }
        if(state == State.RUNNING) //bytesRead > 0, we are reading a package
        {
            this.onStoppedListener = onStoppedListener;
            state = State.STOPPING;
            return;
        }
        throw new IllegalStateException("Already stopping or stopped!");
    }

    @Override
    public void update(long curTimeMillis)
    {
        if(state == State.STARTING)
        {
            lastActivity = curTimeMillis;
            state = State.RUNNING;
        }
        if (bytesRead > 0 && (state == State.RUNNING || state == State.STOPPING))
        {
            //We are reading a package...
            if (inPackageTimeout > 0 && curTimeMillis - lastActivity > inPackageTimeout)
            {
                //We have timed out while reading a package...
                close();
                inPackageTimeoutListener.onTimeout(this, lastActivity);
                disconnectListener.onDisconnected(this);
                return;
            }
        }
        if(state == State.RUNNING || state == State.STOPPING)
        {    
            //Lets read from the inputstream
            try
            {
                while (in.available())
                {
                    lastActivity = curTimeMillis;
                    switch (bytesRead)
                    {
                        case 0:
                            size = in.readByte();
                            size <<= 8;
                            ++bytesRead;
                            break;
                        case 1:
                            size += in.readByte();
                            ++bytesRead;
                            if (size == 0)
                            {
                                //We have an empty package
                                bytesRead = 0;
                                dataPackagelistener.onDataPackage(EMPTY_PACKAGE);
                                if(state == State.STOPPING)
                                {
                                    state = State.STOPPED;
                                    onStoppedListener.notify(this);
                                }
                                return;
                            }
                            break;
                        case 2:
                            data = new int[size];
                        //break is intentionally omittesd to let it fall through
                        default:
                            data[bytesRead - 2] = in.readByte();
                            ++bytesRead;
                            if (bytesRead == size + 2)
                            {
                                bytesRead = 0;
                                
                                dataPackagelistener.onDataPackage(new DataPackageImpl(data));
                                data = null;
                                if(state == State.STOPPING)
                                {
                                    state = State.STOPPED;
                                    onStoppedListener.notify(this);
                                }
                                return;
                            }
                    }
                }
            } catch (IOException ex)
            {
                close();
                disconnectListener.onDisconnected(this);
            }                
        }

    }

    private void checkIfConfigurable()
    {
        if (state == State.CLOSED)
        {
            throw new IllegalStateException("DataPackageReader has been closed!");
        }
    }

    private static class EmptyDataPackageImpl implements DataPackage
    {

        @Override
        public int getSize()
        {
            return 0;
        }

        @Override
        public int getByte(int index)
        {
            throw new IndexOutOfBoundsException("No index will work on an empty package!");
        }

    }
}
