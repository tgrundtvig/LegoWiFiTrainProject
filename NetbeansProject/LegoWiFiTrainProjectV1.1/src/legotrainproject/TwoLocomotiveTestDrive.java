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
public class TwoLocomotiveTestDrive
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
        Locomotive locoA = locFactory.newLocomotive(758391);
        server.addDevice(locoA);
        Locomotive locoB = locFactory.newLocomotive(77992);
        server.addDevice(locoB);
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
                    if( switchA.isConnected() &&
                        switchB.isConnected() &&
                        switchC.isConnected() && 
                        locoA.isConnected() &&
                        locoB.isConnected() )
                    {
                        switchA.switchTo(1);
                        switchB.switchTo(1);
                        switchC.switchTo(2);
                        ++state;
                    }
                    break;
                case 1:
                    if( switchA.getSwitchState() == 1 &&
                        switchB.getSwitchState() == 1 &&
                        switchC.getSwitchState() == 2 &&
                        locoA.isConnected() &&
                        locoB.isConnected()             )
                    {
                        locoA.setTargetPosition(12);
                        locoB.setTargetPosition(14);
                        ++state;
                    }
                    break;
                case 2:
                    if(locoB.getPosition() > 12)
                    {
                        switchC.switchTo(1);
                        ++state;
                    }
                    break;
                case 3:
                    if(switchC.getSwitchState() == 1)
                    {
                        locoA.setTargetPosition(round);
                        ++state;
                    }
                    break;
                case 4:
                    if(locoA.getPosition() > 19)
                    {
                        switchC.switchTo(2);
                        ++state;
                    }
                    break;
                case 5:
                    if(switchC.getSwitchState() == 2)
                    {
                        locoB.setTargetPosition(9);
                        ++state;
                    }
                    break;
                case 6:
                    if(locoB.getPosition() == 9 && locoA.getPosition() == round)
                    {
                        switchA.switchTo(2);
                        switchC.switchTo(1);
                        ++state;
                    }
                    break;
                case 7:
                    if(switchA.getSwitchState() == 2 && switchC.getSwitchState() == 1)
                    {
                        locoB.setTargetPosition(2*round);
                        ++state;
                    }
                    break;
                case 8:
                    if(locoB.getPosition() > round + 4)
                    {
                        switchA.switchTo(1);
                        ++state;
                    }
                    break;
                case 9:
                    if(switchA.getSwitchState() == 1)
                    {
                        locoA.setTargetPosition(round - 9);
                        switchC.switchTo(2);
                        ++state;
                    }
                    break;
                case 10:
                    if(switchC.getSwitchState() == 2)
                    {
                        locoA.setTargetPosition(round - 19);
                        ++state;
                    }
                case 11:
                    if(locoA.getPosition() < round - 13)
                    {
                        locoB.setTargetPosition(2*round - 16);
                        switchC.switchTo(1);
                        ++state;
                    }
                    break;
                case 12:
                    if(switchC.getSwitchState() == 1)
                    {
                        locoB.setTargetPosition(round);
                        ++state;
                    }
                    break;
                case 13:
                    if(locoB.getPosition() < round+9)
                    {
                        switchC.switchTo(2);
                        ++state;
                    }
                    break;
                case 14:
                    if(switchC.getSwitchState() == 2)
                    {
                        locoA.setTargetPosition(round-10);
                        ++state;
                    }
                    break;
                case 15:
                    if(locoA.getPosition() == round-10)
                    {
                        switchC.switchTo(1);
                        ++state;
                    }
                    break;
                case 16:
                    if(switchC.getSwitchState() == 1)
                    {
                        locoA.setTargetPosition(0);
                        ++state;
                    }
                    break;
                case 17:
                    if(locoA.getPosition() == 0)
                    {
                        switchB.switchTo(2);
                        ++state;
                    }
                    break;
                case 18:
                    if(switchB.getSwitchState() == 2)
                    {
                        locoB.setTargetPosition(0);
                        ++state;
                    }
                    break;
                case 19:
                    if(locoB.getPosition() == 0)
                    {
                        stopTime = curTime;
                        ++state;
                    }
                    break;
                case 20:
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
