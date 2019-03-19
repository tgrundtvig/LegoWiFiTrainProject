/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package legotrainproject.railroadswitch;

/**
 *
 * @author Tobias
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
