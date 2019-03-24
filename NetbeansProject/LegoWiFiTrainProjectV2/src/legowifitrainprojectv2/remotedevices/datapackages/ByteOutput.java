/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package legowifitrainprojectv2.remotedevices.datapackages;

import java.io.Closeable;
import java.io.IOException;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public interface ByteOutput extends Closeable
{
    public void writeByte(int b) throws IOException;
    @Override
    public void close();
}
