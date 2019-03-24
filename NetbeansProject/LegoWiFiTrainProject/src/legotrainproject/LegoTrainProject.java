/*
 * Not licensed yet, use at your own risk, no warrenties!
 */
package legotrainproject;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import legotrainproject.railroadswitch.RailroadSwitch;
import legotrainproject.railroadswitch.implementation.RailroadSwitchFactoryImpl;
import legotrainproject.railroadswitch.test.SwitchTerminal;
import remotedevices.RemoteDevice;
import remotedevices.implementation.RemoteDeviceConnectionImpl;
import remotedevices.RemoteDeviceConnection;
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
        server.addFactory(factory);
        SwitchTerminal terminal = new SwitchTerminal();
        factory.addListener(terminal);
        RailroadSwitch railSwitch = factory.newRailroadSwitch(581645, server);
        server.startServer(3377);
        System.out.println("Main thread is out!");
    }
}
