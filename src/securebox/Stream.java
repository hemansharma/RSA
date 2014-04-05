
package securebox;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class Stream
{
    
    private FileOutputStream fos;
    private FileChannel fc;
    private FileInputStream fis;
    private String file;

    public Stream(String str)
    {
        fc = null;
        file = str;
    }

    
    public void write(byte[] array)
    {
        try
        {
            fos = new FileOutputStream(file);
            fc = fos.getChannel();
            ByteBuffer bb = ByteBuffer.wrap(array);
            
            fc.write(bb);
            
            fc.close();
            fos.close();
        } catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public byte[] read()
    {
        ByteBuffer bb = null;
        try
        {
            fis = new FileInputStream(file);
            fc = fis.getChannel();
            bb = ByteBuffer.allocate((int) fc.size());
            
            fc.read(bb);
            bb.flip();
            
            fc.close();
            fis.close();
            
        } catch(Exception e) {
            e.printStackTrace();
        }
        return bb.array();
    }

}
