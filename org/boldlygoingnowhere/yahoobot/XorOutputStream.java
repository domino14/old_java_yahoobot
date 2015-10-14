package org.boldlygoingnowhere.yahoobot;
import java.io.*;

public class XorOutputStream extends FilterOutputStream
{
    private byte send_key;
    private final byte multiplier = 83; 
    public XorOutputStream(OutputStream out, byte send_key)
    {
	super(out);
	this.send_key = send_key;
    }

    public void write(int b) throws IOException
    {
	send_key *= multiplier;
	super.write( (b ^ send_key) & 0xFF);
    }

}
