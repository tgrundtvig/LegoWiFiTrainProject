/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

/**
 *
 * @author Tobias Grundtvig
 */
public class SyncBox<E>
{
    private E obj = null;
    private boolean stop = false;
    
    public synchronized void stop()
    {
        stop = true;
    }
    
    public synchronized E get()
    {
        while(obj == null && !stop)
        {
            try
            {
                wait();
            } catch (InterruptedException ex)
            {
                //Do nothing...
            }
        }
        if(!stop)
        {
            E res = obj;
            obj = null;
            notifyAll();
            return res;
        }
        return null;
    }
    
    public synchronized E getIfAvailable()
    {
        if(obj == null) return null;
        E res = obj;
        obj = null;
        notifyAll();
        return res;
    }
    
    public synchronized void put(E obj)
    {
        while(this.obj != null && !stop)
        {
            try
            {
                wait();
            } catch (InterruptedException ex)
            {
                //Do nothing...
            }
        }
        if(!stop)
        {
            this.obj = obj;
            notifyAll();
        }
    }
}
