/*
 * Not licensed yet, use at your own risk, no warrenties!
 */
package legotrainproject.locomotive;

import legotrainproject.locomotive.Locomotive.Direction;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public interface LocomotiveListener
{
    public void onPositionChange(long pos);
    public void onSpeedChange(int speed);
    public void onDirectionChange(Direction dir);
}
