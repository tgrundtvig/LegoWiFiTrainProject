/*
 * Not licensed yet, use at your own risk, no warrenties!
 */
package legotrainproject;

import java.io.IOException;
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
public class OneLocomotiveTestDrive
{

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws java.lang.InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException
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
            System.out.println("RailroadSwitch handle created: " + newRailroadSwitch.getDeviceId());
        }));
        //Locomotive loco = locFactory.newLocomotive(14815215, server);
        Locomotive loco = locFactory.newLocomotive(77992);
        server.addDevice(loco);
        RailroadSwitch switchA = switchFactory.newRailroadSwitch(759149);
        server.addDevice(switchA);
        RailroadSwitch switchB = switchFactory.newRailroadSwitch(169822);
        server.addDevice(switchB);
        RailroadSwitch switchC = switchFactory.newRailroadSwitch(959028);
        server.addDevice(switchC);
        server.startServer(3377);
        System.out.println("Server started!");
        int round = 29;
        int state = 0;
        int lastState = -1;
        long stopTime = 0;
        while(true)
        {
            long curTime = System.currentTimeMillis();
            server.update(curTime);
            if(state != lastState)
            {
                System.out.println("State: " + state);
                lastState = state;
            }
            switch(state)
            {
                case 0:
                    switchA.switchTo(1);
                    switchB.switchTo(2);
                    switchC.switchTo(1);
                    ++state;
                    break;
                case 1:
                    if( switchA.getSwitchState() == 1 &&
                        switchB.getSwitchState() == 2 &&
                        switchC.getSwitchState() == 1   )
                    {
                        loco.setTargetPosition(round-5);
                        ++state;
                    }
                    break;
                case 2:
                    if(loco.getPosition() > 5)
                    {
                        switchA.switchTo(2);
                        switchB.switchTo(1);
                        ++state;
                    }
                    break;
                case 3:
                    if(switchB.getSwitchState() == 1 && switchA.getSwitchState() == 2)
                    {
                        loco.setTargetPosition(2*round);
                        ++state;
                    }
                    break;
                case 4:
                    if(loco.getPosition() == 2*round)
                    {
                        stopTime = curTime;
                        ++state;
                    }
                    break;
                case 5:
                    if(switchA.getSwitchState() == 2 && curTime - stopTime > 10000)
                    {
                        switchC.switchTo(2);
                        loco.setTargetPosition(2*round + 8);
                        ++state;
                    }
                    break;
                case 6:
                    if(switchC.getSwitchState() == 2)
                    {
                        loco.setTargetPosition(2*round + 19);
                        ++state;
                    }
                    break;
                case 7:
                    if(loco.getPosition() == 2*round + 19)
                    {
                        stopTime = curTime;
                        ++state;
                    }
                    break;
                case 8:
                    if(curTime - stopTime > 10000)
                    {
                        loco.setTargetPosition(2*round + 9);
                        ++state;
                    }
                    break;
                case 9:
                    if(loco.getPosition() == 2*round + 9)
                    {
                        switchC.switchTo(1);
                        ++state;
                    }
                    break;
                case 10:
                    if(switchC.getSwitchState() == 1)
                    {
                        loco.setTargetPosition(4*round);
                        ++state;
                    }
                case 11:
                    if(loco.getPosition() == 4*round)
                    {
                        stopTime = curTime;
                        ++state;
                    }
                    break;
                case 12:
                    if(curTime - stopTime > 10000)
                    {
                        loco.setTargetPosition(round + 4);
                        ++state;
                    }
                    break;
                case 13:
                    if(loco.getPosition() <= 2*round-1)
                    {
                        switchA.switchTo(1);
                        ++state;
                    }
                    break;
                case 14:
                    if(loco.getPosition() <= round + round - 5)
                    {
                        switchB.switchTo(2);
                        ++state;
                    }
                    break;
                case 15:
                    if(switchA.getSwitchState() == 1)
                    {
                        loco.setTargetPosition(round);
                        ++state;
                    }
                    break;
                case 16:
                    if(switchB.getSwitchState() == 2)
                    {
                        loco.setTargetPosition(0);
                        ++state;
                    }
                    break;
                case 17:
                    if(loco.getPosition() == 0)
                    {
                        stopTime = curTime;
                        ++state;
                    }
                    break;
                case 18:
                    if(curTime - stopTime > 10000)
                    {
                        state = 0;
                    }
                    break; 
                default:
            }
        }
    }
}
