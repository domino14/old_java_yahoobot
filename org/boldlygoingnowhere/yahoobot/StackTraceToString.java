package org.boldlygoingnowhere.yahoobot;

import java.io.*;

public class StackTraceToString
{
    static public String convert(Exception e)
    {
	try
	    {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return "------\r\n" + sw.toString() + "------\r\n";
	    }
	catch (Exception e2)
	    {
		return "bad stack_to_string";
	    }
    }
}
