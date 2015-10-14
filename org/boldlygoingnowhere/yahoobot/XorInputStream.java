package org.boldlygoingnowhere.yahoobot;
import java.io.*;

public class XorInputStream extends FilterInputStream
{
    byte receive_key;
    final byte multiplier = 83;
    public XorInputStream(InputStream in, byte receive_key)
    {
	super(in);
	this.receive_key = receive_key;
    }

    public int read() throws IOException
    {
	int result = super.read();
	receive_key *= multiplier;
	return ((result ^ receive_key) & 0xFF); // 0 to 255
    }
        
    public int read(byte[] b) throws IOException
    {
	/*int numbytesread = super.read(b);
	for (int i = 0; i < numbytesread; i++)
	    {
		receive_key *= multiplier;
		b[i] ^= receive_key;
	    }
	    return numbytesread;*/
	return read(b, 0, b.length);
    }
    
    public int read(byte[] b, int off, int len) throws IOException
    {
	int numbytesread = super.read(b, off, len);
	for (int i = off; i < off + numbytesread; i++)
	    {
		receive_key *= multiplier;
		b[i] ^= receive_key;
	    }
	return numbytesread;
    }

    public long skip(long n) throws IOException
    {
	long res = super.skip(n);
	for (long i = 0; i < res; i++)
	    receive_key *= multiplier;
	return res;
    }
    
}
