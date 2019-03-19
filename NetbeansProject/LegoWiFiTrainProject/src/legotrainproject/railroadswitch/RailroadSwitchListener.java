/*
 * Not licensed yet, use at your own risk, no warrenties!
 */
package legotrainproject.railroadswitch;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public interface RailroadSwitchListener
{
    //0 -> Unknown position
    //1 -> Train goes left
    //2 -> Train goes right
    //3 -> Moving to the left position
    //4 -> Moving to the right position
    public void onRailroadSwitchStateChange(RailroadSwitch device, int oldState, int newState);
}
