package org.boldlygoingnowhere.yahoobot;

import java.io.*;
import java.net.*;

public class ServerConnector
{
    private String mycookie = "";

    private String yogserver = "";
    private String yport = "";
    private int port = 11999;

    private final String loginserver = "login.yahoo.com";
    private final String gamesserver = "games.yahoo.com";

    private int idnum = 0;
    DataInputStream comm_input = null;
    DataOutputStream comm_output = null;
    MessageProtocolProcessor mpp = null;
    TableableChatGame chatgame = null;

    
    public String getCookieFromServer(String username, String password, String loginserver)
    {
	Socket cookie_socket = null;
	PrintWriter out = null;
	BufferedReader in = null;
	System.out.println("About to request cookie.");
	try
	    {
		cookie_socket = new Socket (loginserver, 80);
		out = new PrintWriter(cookie_socket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(cookie_socket.getInputStream()));
	    }
	catch (UnknownHostException e)
	    {
		System.err.println("Don't know about host: " + loginserver);
		System.exit(1);
	    }
	catch (IOException e)
	    {
		System.err.println("Couldn't get I/O.");
		System.exit(1);
	    }

	String cookierequest = formulate_cookie_request(username, password);
	out.print(cookierequest);
	//System.out.println("Sent " + cookierequest);
	out.flush();

	System.out.println("Sent cookie request..");
	String extracted_cookie = null;
	char[] cbuf = new char[10000];
	try
	    {
		in.read(cbuf, 0, 10000);
		extracted_cookie = extractcookie(new String(cbuf));
	    }
	catch (Exception e)
	    {
		System.out.println("Exception...");
	    }
	
	return extracted_cookie;
    }

    public String formulate_cookie_request(String username, String password)
    {
	return ("GET /config/login?.src=&login=" + username + "&passwd=" + password +
		  "&n=1 HTTP/1.0\r\nAccept: */*\r\nAccept: text/html\r\n\r\n");
    }
    public String extractcookie(String HTTPReplyString)
    {
	if (HTTPReplyString.indexOf("Y=v=") == -1)
	    {
		System.out.println("Cookie is invalid.");
		return null;
	    }
	System.out.println("Cookie is valid.");
	
	int first_part_loc = HTTPReplyString.indexOf("Y=v=");
	int first_semicolon_loc = HTTPReplyString.indexOf(";", first_part_loc);
	int second_part_loc = HTTPReplyString.indexOf("T=z=");
	int second_semicolon_loc = HTTPReplyString.indexOf(";", second_part_loc);

	return (HTTPReplyString.substring(first_part_loc, first_semicolon_loc + 1) + " " + 
		HTTPReplyString.substring(second_part_loc, second_semicolon_loc));

    }

    public void getAppletFromServer(String gameserver, String gameroom)
    {
	Socket games_socket = null;
	PrintWriter out = null;
	BufferedReader in = null;
	try
	    {
		games_socket = new Socket(gameserver, 80);
		out = new PrintWriter(games_socket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(games_socket.getInputStream()));
	    }
	catch (UnknownHostException e)
	    {
		System.err.println("Don't know about host: " + gameserver);
		System.exit(1);
	    }
	catch (IOException e)
	    {
		System.err.println("Couldn't get I/O from " + gameserver);
		System.exit(1);
	    }
	String appletrequest = formulate_applet_request(gameroom);
	out.print(appletrequest);
	out.flush();
	//System.out.println("Sent applet request: " + appletrequest);
	String appinput;
	String applet = "";
	try
	    {
		while ((appinput = in.readLine()) != null)
		    {
			applet += appinput;
		    }
	    }
	catch (Exception e)
	    {
		System.out.println("Exception!");
	    }
	getParametersFromApplet(applet);

    }

    public void getParametersFromApplet(String applet)
    {
	int yogservloc = applet.indexOf("codebase");
	yogservloc = applet.indexOf("http://", yogservloc);
	int yogservendloc = applet.indexOf("com", yogservloc);

	yogserver = applet.substring(yogservloc+7, yogservendloc+3);
	System.out.println("Yog server: " + yogserver + "-----");

	int yportloc = applet.indexOf("yport");
	yportloc = applet.indexOf("value=", yportloc);
	int yportendloc = applet.indexOf(">", yportloc);
	yport = applet.substring(yportloc +7, yportendloc-1);
	System.out.println("Yport: " + yport + "-----");


    }

    public String formulate_applet_request(String gameroom)
    {
	return ("GET /games/applet.html?room=" + gameroom + 
		"&prof_id=chat_pf_1&small=no&follow=&nosignedcab=yes HTTP/1.1\r\n" +
		"Host: games.yahoo.com\r\n" +
		"User-Agent: Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)\r\n" +
		"Accept: application/x-shockwave-flash, */*\r\n" +
		"Accept-Language: en-us\r\n" +
		"Connection: Keep-Alive\r\n" +
		"Cookie: " + mycookie + "\r\n\r\n");

    }
    
    public void connectToYogServer(String username)
    {
	Socket client_socket = null;
	
	XorInputStream xor_in = null;
	XorOutputStream xor_out = null;
	BufferedInputStream bi = null;
	BufferedOutputStream bo = null;
	try
	    {
		client_socket = new Socket(yogserver, port);
		bi = new BufferedInputStream(client_socket.getInputStream());
		bo = new BufferedOutputStream(client_socket.getOutputStream());
	    }
	catch (Exception e)
	    {
		System.out.println(StackTraceToString.convert(e));
		System.exit(1);
	    }

	System.out.println("Connected to " + yogserver + ":" + port);
	try
	    {
		byte [] bbuf = new byte[100];
		int strlen = bi.read(bbuf, 0, 6);
		System.out.println("Strlen = " + strlen);
		String ystring = new String(bbuf, 0, 6, "US-ASCII");
		
		if (ystring.equals("YAHOO!"))
		    {
			System.out.println("Client: Y");
			bo.write('Y');
			bo.flush();
		    }
		else
		    {
			System.out.println("General error.");
			System.exit(1);
		    }
	      	strlen = bi.read(bbuf, 0, 8);
		System.out.println("Server: " + "(" + strlen + ")");
		if (strlen == 8)
		    {
			System.out.println("Received key.");
			System.out.println("Send key: " + bbuf[3] + " Receive key: " + bbuf[7]);
		    }
		else
		    {
			System.out.println("General error 2.");
			System.exit(1);
		    }

		// send "o" + yport


		comm_input = new DataInputStream(new XorInputStream(bi, bbuf[7]));     // "receive" key
		comm_output = new DataOutputStream(new XorOutputStream(bo, bbuf[3]));   // "send" key


		comm_output.writeByte('o');
		comm_output.writeUTF(yport);
		comm_output.flush();
		//		strlen = i.read(bbuf);
		//System.out.println("Bytes read: " + strlen);
		//for (int j = 0; j < strlen; j++)
		//  {
		//System.out.print(bbuf[j] + " ");
		//  }
		if (comm_input.readByte() == 'o' && comm_input.readUTF().equals(yport))
		    {
			idnum = comm_input.readInt();
			System.out.println("ID: " + idnum);
		    }
		else
		    {
			System.out.println("General error 3.");
			System.exit(1);
		    }

		if (comm_input.readByte() == 'd' && comm_input.readInt() == idnum)
		    {
			// comm_input.readUTF().equals("GAMES") && 
			//comm_input.readUTF().equals("t") && comm_input.readUTF().equals("4"))
			//{
			short pl = comm_input.readShort();
			/*for (short i = 0; i < pl; i++)
			    {
				System.out.print("("+i+": "+comm_input.readByte()+")");
				}*/
			System.out.println(comm_input.readUTF());
			System.out.println(comm_input.readUTF());
			System.out.println(comm_input.readUTF());
			String addinfo = "Mozilla/4.0(compatible; MSIE 6.0; Windows NT 5.1)";
			String addinfo2 = "us";
			String idinfo = "id=" + username;
			comm_output.write('d');
			comm_output.writeInt(idnum);
			comm_output.writeShort(1+ idinfo.length() + 2 + mycookie.length() + 2 + 
				     addinfo.length() + 2 + addinfo2.length() + 2);
			comm_output.writeByte(0);
			comm_output.writeUTF(idinfo);
			comm_output.writeUTF(mycookie);
			comm_output.writeUTF(addinfo);
			comm_output.writeUTF(addinfo2);
			comm_output.flush();

		    }
		//		else
		//  {
		//System.out.println("General error 4.");
		//System.exit(1);
		//  }
	
	    }
	catch (Exception e)
	    {
		System.out.println(StackTraceToString.convert(e));

		System.exit(1);
	    }

    }

    

    public void startconnecting(String username, String password, String specificroom, String game, boolean reconnected) 
	throws Exception
    {
	mycookie = getCookieFromServer(username, password, loginserver);
	if (mycookie == null)
	    {
		System.out.println("Quitting.");
		System.exit(1);
	    }
	System.out.println("Cookie: " + mycookie);
	getAppletFromServer(gamesserver, specificroom);
	connectToYogServer(username);

	

	if (game.equals("trivia"))
	    {
		chatgame = new Trivia();
		mpp = new YahooLiteratiProtocolProcessor(comm_input, comm_output, idnum, username, chatgame);
		if (reconnected == true) 
		    mpp.sendToChatRoom("There was an internal error and the bot had to reconnect. We apologize for any inconvenience."); 
	    }
	else if (game.equals("triviatable"))
	    {
		chatgame = new Trivia();
		chatgame.settablemode();
		mpp = new YahooLiteratiProtocolProcessor(comm_input, comm_output, idnum, username, chatgame);
		if (reconnected == true)
		    mpp.sendToChatRoom("There was an internal error and the bot had to reconnect. We apologize for any inconvenience."); 
	    }
	else if (game.equals("none"))
	    {
		mpp = new YahooLiteratiProtocolProcessor(comm_input, comm_output, idnum, username, null);
	    }
	else
	    {
		System.out.println("That is not a valid game. Use 'none' for no game.");
		System.exit(1);
	    }
	mpp.processData();
	System.out.println("I shouldn't be printing.");
    }
    public void initialize(String[] args)
    {
	boolean reconnected = false;
	while (true)
	    {
		try
		    {
			if (chatgame != null) chatgame.destroyTimer();
			startconnecting(args[0], args[1], args[2], args[3], reconnected);
		    }
		catch (Exception e)
		    {
			System.out.println(StackTraceToString.convert(e));
			System.out.println("Exception caught. Starting to reconnect...");
			reconnected = true;
		    }
		
	    }
    }

    public static void main(String[] args)
    {
	ServerConnector sc = new ServerConnector();
	
	if (args.length != 4) 
	    {
		System.out.println("Parameters: username password specificroom game");
		System.exit(1);
	    }

	sc.initialize(args);

    }

}
