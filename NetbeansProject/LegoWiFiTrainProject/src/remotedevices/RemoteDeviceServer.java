/*
 * Not licensed yet, use at your own risk, no warrenties!
 */
package remotedevices;

import java.util.List;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public interface RemoteDeviceServer
{ 
    public void addFactory(RemoteDeviceFactory factory);
    public boolean removeFactory(RemoteDeviceFactory factory);
    public void startServer(int port);
    public void stopServer();
    public boolean containsDevice(long deviceId);
    public void addDevice(RemoteDevice device);
    public boolean removeDevice(RemoteDevice device);
    public List<RemoteDevice> getAllDevices();
    
}
