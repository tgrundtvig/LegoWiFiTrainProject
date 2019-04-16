/*
 * Not licensed yet, use at your own risk, no warrenties!
 */
package legotrainproject.locomotive;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public interface LocomotiveListener
{
    public void onPositionChange(Locomotive loco, int pos, int magnetTime);
}
