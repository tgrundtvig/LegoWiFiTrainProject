/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package legowifitrainprojectv2.common;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public interface TimeoutListener<E>
{
    public void onTimeout(E obj, long lastActivityMillis);
}
