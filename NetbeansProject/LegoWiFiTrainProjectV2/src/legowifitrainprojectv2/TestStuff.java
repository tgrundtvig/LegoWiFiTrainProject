/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package legowifitrainprojectv2;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public class TestStuff
{
    public static void main(String[] args)
    {
        byte a = (byte) 127;
        byte b = 5;
        byte c = (byte) (a + b);
        byte d = (byte) (c - a);
        int e = c - a;
        System.out.println(c);
        System.out.println(e);
        System.out.println(d);
        
    }
}
