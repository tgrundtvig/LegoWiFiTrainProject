/*
 * Not licensed yet, use at your own risk, no warrenties!
 */
package legotrainproject.railroadswitch.test;

import java.io.IOException;
import java.util.Scanner;
import legotrainproject.railroadswitch.RailroadSwitch;
import legotrainproject.railroadswitch.RailroadSwitchFactoryListener;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public class SwitchTerminal implements RailroadSwitchFactoryListener
{

    @Override
    public void onNewRailroadSwitch(RailroadSwitch newRailroadSwitch)
    {
        new Thread(new TerminalThread(newRailroadSwitch)).start();
    }

    private class TerminalThread implements Runnable
    {

        private RailroadSwitch sw;

        public TerminalThread(RailroadSwitch sw)
        {
            this.sw = sw;
        }

        @Override
        public void run()
        {
            try
            {
                System.out.println("Got new switch: " + sw.getDeviceId());
                System.out.println("Waiting for switch to connect...");
                while (!sw.isConnected())
                {
                }
                System.out.println(sw.getDeviceId() + " connected!");

                Scanner input = new Scanner(System.in);
                while (sw.isConnected())
                {
                    System.out.print("\nEnter command: ");
                    String cmd = input.nextLine();
                    if ("left".equals(cmd))
                    {
                        if (sw.switchTo(1))
                        {
                            System.out.println("Switching to left");
                            while (sw.getSwitchState() != 1)
                            {
                            }
                            System.out.println("Switch is now left.");
                        } else
                        {
                            System.out.println("Already at left");
                        }
                    } else if ("right".equals(cmd))
                    {
                        if (sw.switchTo(2))
                        {
                            System.out.println("Switching to right");
                            while (sw.getSwitchState() != 2)
                            {
                            }
                            System.out.println("Switch is now right.");
                        } else
                        {
                            System.out.println("Could not switch");
                        }
                    } else
                    {
                        System.out.println("Unknown command: " + cmd);
                    }
                }
                while (sw.isConnected())
                {
                    System.out.println("");
                }
            }
            catch(IOException e)
            {
                System.out.println(e);
            }

        }

    }
}
