/*
 * Not licensed yet, use at your own risk, no warrenties!
 */
package legotrainproject.locomotive;

import java.io.IOException;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public interface Locomotive
{
    public Direction getDirection();
    public void setDirection(Direction direction) throws IOException;
    public int getSpeed();
    public void setSpeed(int speed) throws IOException; //0-255
    public int getPos();
    public enum Direction {FORWARD, BACKWARD};
}
