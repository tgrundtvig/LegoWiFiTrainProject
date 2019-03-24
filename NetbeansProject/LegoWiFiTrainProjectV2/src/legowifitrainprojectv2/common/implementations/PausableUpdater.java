/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package legowifitrainprojectv2.common.implementations;

import legowifitrainprojectv2.common.Pausable;
import legowifitrainprojectv2.common.Updatable;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public class PausableUpdater implements Updatable, Pausable
{
    private final Updatable updatable;
    private long behind;
    private long pausedPos;
    private int state;  //0 -> running,
                        //1 -> just paused,
                        //2 -> just resumed,
                        //3 -> paused

    public PausableUpdater(Updatable updatable, boolean paused)
    {
        this.updatable = updatable;
        this.behind = 0;
        this.pausedPos = 0;
        this.state = paused ? 1 : 0;
    }
    
    
            
    @Override
    public void update(long curTimeMillis)
    {
        switch(state)
        {
            case 0:
                //we are running now
                updatable.update(curTimeMillis-behind);
                break;
            case 1:
                //Just paused
                pausedPos = curTimeMillis;
                state = 3;
                break;
            case 2:
                //Just resumed
                behind += curTimeMillis - pausedPos;
                update(pausedPos);
            default:
                //We are paused;
                break;
        }
    }

    @Override
    public void pause()
    {
        if(state == 0)
        {
            state = 1;
        }
    }

    @Override
    public boolean isPaused()
    {
        return state == 1 || state == 3;
    }

    @Override
    public void resume()
    {
        if(state == 1)
        {
            state = 0;
        }
        else if(state == 3)
        {
            state = 2;
        }
    }

}
