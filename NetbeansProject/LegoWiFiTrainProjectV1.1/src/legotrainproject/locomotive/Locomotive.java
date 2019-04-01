/*
 * Not licensed yet, use at your own risk, no warrenties!
 */
package legotrainproject.locomotive;

import java.io.IOException;
import remotedevices.RemoteDevice;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public interface Locomotive extends RemoteDevice
{
    public Direction getDirection();
    public void setDirection(Direction direction) throws IOException;
    public int getSpeed();
    public void setSpeed(int speed) throws IOException; //0-255
    public long getPos();
    public void addListener(LocomotiveListener listener);
    public void removeListener(LocomotiveListener listener);
    public enum Direction {FORWARD, BACKWARD};
}
