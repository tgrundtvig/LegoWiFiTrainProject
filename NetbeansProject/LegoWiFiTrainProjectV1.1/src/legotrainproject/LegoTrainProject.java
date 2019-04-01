/*
 * Not licensed yet, use at your own risk, no warrenties!
 */
package legotrainproject;

import java.io.IOException;
import legotrainproject.locomotive.Locomotive;
import legotrainproject.locomotive.LocomotiveListener;
import legotrainproject.locomotive.implementation.LocomotiveFactoryImpl;
import legotrainproject.railroadswitch.RailroadSwitch;
import legotrainproject.railroadswitch.implementation.RailroadSwitchFactoryImpl;
import remotedevices.RemoteDeviceServer;
import remotedevices.implementation.RemoteDeviceServerImpl;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public class LegoTrainProject
{

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws java.lang.InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException
    {
        RemoteDeviceServer server = new RemoteDeviceServerImpl();
        RailroadSwitchFactoryImpl factory = new RailroadSwitchFactoryImpl();
        LocomotiveFactoryImpl locFactory = new LocomotiveFactoryImpl();
        server.addFactory(factory);
        server.addFactory(locFactory);
        locFactory.addListener((Locomotive newLocomotive) ->
        {
            System.out.println("Locomotive connected: " + newLocomotive.getDeviceId());
        });
        //RailroadSwitch railSwitch = factory.newRailroadSwitch(581645, server);
        RailroadSwitch railSwitch = factory.newRailroadSwitch(959028, server);
        railSwitch.addListener((RailroadSwitch device, int oldState, int newState) ->
        {
            System.out.println("Device id: " + device.getDeviceId() + " Switch state: " + device.getSwitchState());
        });
        Locomotive loco = locFactory.newLocomotive(14815215, server);
        loco.addListener(new LocomotiveListener()
        {
            @Override
            public void onPositionChange(long pos)
            {
                System.out.println("New position: " + pos);
            }

            @Override
            public void onSpeedChange(int speed)
            {
                System.out.println("New speed: " + speed);
            }

            @Override
            public void onDirectionChange(Locomotive.Direction dir)
            {
                System.out.println("New direction: " + dir);
            }
        });
        
        
        server.startServer(3377);
        int curSide = 1;
        long lastTime = System.currentTimeMillis();
        while(true)
        {
            long curTime = System.currentTimeMillis();
            server.update(curTime);
            if(curTime - lastTime > 10000 && railSwitch.isConnected())
            {
                System.out.println("Switching...");
                lastTime = curTime;
                if(curSide == 1)
                {
                    railSwitch.switchTo(2);
                    curSide = 2;
                }
                else
                {
                    railSwitch.switchTo(1);
                    curSide = 1;
                }
            }
            
        }
    }
}
