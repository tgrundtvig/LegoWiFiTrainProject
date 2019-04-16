/*
 * Not licensed yet, use at your own risk, no warrenties!
 */

package legotrainproject.testdevice;

import java.util.HashSet;
import java.util.Set;
import legotrainproject.testdevice.implementation.TestDeviceFactoryImpl;
import remotedevices.RemoteDeviceServer;
import remotedevices.implementation.RemoteDeviceServerImpl;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public class TestDeviceTestRun
{
    public static void main(String[] args)
    {
        RemoteDeviceServer server = new RemoteDeviceServerImpl();
        TestDeviceFactoryImpl testDeviceFactory = new TestDeviceFactoryImpl();
        server.addFactory(testDeviceFactory);
        Set<TestDevice> testDevices = new HashSet<>();
        testDeviceFactory.addListener((newTestDevice) ->
        {
            System.out.println("TestDevice created: " + newTestDevice.getDeviceId());
            testDevices.add(newTestDevice);
        });
        server.startServer(3377);
        System.out.println("Server started!");
        long lastTime = 0;
        boolean ledValue = true;
        while (true)
        {
            long curTime = System.currentTimeMillis();
            server.update(curTime);
            if(curTime - lastTime > 500)
            {
                for(TestDevice device : testDevices)
                {
                    device.setLED(ledValue);
                }
                lastTime = curTime;
                ledValue = !ledValue;
            }
        }
    }
}
