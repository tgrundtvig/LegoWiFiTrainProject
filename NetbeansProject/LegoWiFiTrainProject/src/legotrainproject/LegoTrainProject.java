/*
 * Not licensed yet, use at your own risk, no warrenties!
 */
package legotrainproject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import legotrainproject.railroadswitch.RailroadSwitch;
import legotrainproject.railroadswitch.implementation.RailroadSwitchFactoryImpl;
import remotedevices.implementation.RemoteDeviceConnectionImpl;
import remotedevices.RemoteDeviceConnection;

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
        RailroadSwitchFactoryImpl factory = new RailroadSwitchFactoryImpl();
        ServerSocket serverSocket = new ServerSocket(3377);
        System.out.println("Sleeping");
        Thread.sleep(10000);
        System.out.println("Done sleeping");
        Socket client = serverSocket.accept();
        RemoteDeviceConnection deviceConnection = new RemoteDeviceConnectionImpl(client);
        System.out.println("Device ID: " + deviceConnection.getDeviceId());
        System.out.println("Device type: " + deviceConnection.getDeviceType());
        System.out.println("Device Version: " + deviceConnection.getDeviceVersion());
        System.out.println("Max package size: " + deviceConnection.getMaxPackageSize());
        RailroadSwitch railSwitch = null;
        if(factory.matches(deviceConnection))
        {
            railSwitch = factory.createNewConnectedDevice(deviceConnection);
        }
        else
        {
            System.out.println("Connection does not match!");
            return;
        }
        
        System.out.println("Switch direction is " + railSwitch.getSwitchDirection());
        System.out.println("Switch state is " + railSwitch.getSwitchState());
    
        
        Scanner input = new Scanner(System.in);
        while (true)
        {
            System.out.print("\nEnter command: ");
            String cmd = input.nextLine(); 
            if("left".equals(cmd))
            {
                if(railSwitch.switchTo(1))
                {
                    System.out.println("Switching to left");
                    while(railSwitch.getSwitchState() != 1){}
                    System.out.println("Switch is now left.");
                }
                else
                {
                    System.out.println("Could not switch");
                }
            }
            else if("right".equals(cmd))
            {
                if(railSwitch.switchTo(2))
                {
                    System.out.println("Switching to right");
                    while(railSwitch.getSwitchState() != 2){}
                    System.out.println("Switch is now right.");
                }
                else
                {
                    System.out.println("Could not switch");
                }
            }
            else
            {
                System.out.println("Unknown command: " + cmd);
            }
        } 
    }

}
