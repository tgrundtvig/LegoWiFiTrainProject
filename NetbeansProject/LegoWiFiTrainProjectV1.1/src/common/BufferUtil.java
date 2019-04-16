/*
 * Not licensed yet, use at your own risk, no warrenties!
 */

package common;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public class BufferUtil
{
    public static void writeIntegerToBuffer(long data, int[] buffer, int index, int size)
    {
        for (int i = size - 1; i >= 0; --i)
        {
            buffer[index + i] = (int) ((data >> (i * 8)) & 0xFF);
        }
    }

    public static long readIntegerFromBuffer(int[] buffer, int index, int size)
    {
        long res = 0;
        for (int i = 0; i < size; ++i)
        {
            res <<= 8;
            res += buffer[index + i];
        }
        return res;
    }
}
