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
    public void onPositionChange(int pos, int magnetTime);
}
