/*
 * Not licensed yet, use at your own risk, no warrenties!
 */
package legotrainproject;

import java.util.logging.Level;
import java.util.logging.Logger;
import legotrainproject.locomotive.Locomotive;
import legotrainproject.locomotive.implementation.LocomotiveFactoryImpl;
import legotrainproject.railroadswitch.RailroadSwitch;
import legotrainproject.railroadswitch.implementation.RailroadSwitchFactoryImpl;
import remotedevices.RemoteDeviceServer;
import remotedevices.implementation.RemoteDeviceServerImpl;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public class SimpleConnectionTest
{

    private static RailroadSwitch s = null;

    public static void main(String[] args)
    {
        RemoteDeviceServer server = new RemoteDeviceServerImpl();
        LocomotiveFactoryImpl locFactory = new LocomotiveFactoryImpl();
        RailroadSwitchFactoryImpl switchFactory = new RailroadSwitchFactoryImpl();
        server.addFactory(locFactory);
        server.addFactory(switchFactory);
        locFactory.addListener((Locomotive newLocomotive) ->
        {
            System.out.println("Locomotive handle created: " + newLocomotive.getDeviceId());
        });
        switchFactory.addListener(((newRailroadSwitch) ->
        {
            s = newRailroadSwitch;
            System.out.println("RailroadSwitch handle created: " + newRailroadSwitch.getDeviceId());
        }));
        server.startServer(3377);
        System.out.println("Server started!");
        long frameStart = System.currentTimeMillis();
        int frameCount = 0;
        while (true)
        {
            long curTime = System.currentTimeMillis();
            server.update(curTime);
            if (s != null && s.getSwitchState() == 1)
            {
                s.switchTo(2);
            } else if (s != null && s.getSwitchState() == 2)
            {
                s.switchTo(1);
            }
            ++frameCount;
            if(curTime - frameStart >= 1000)
            {
                System.out.println("Frames pr second: " + frameCount);
                frameCount = 0;
                frameStart = curTime;
            }
            try
            {
                Thread.sleep(10);
            } catch (InterruptedException ex)
            {
                Logger.getLogger(SimpleConnectionTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
