

package org.boldlygoingnowhere.yahoobot;
import java.io.*;

import java.util.Date;

public abstract class YahooProtocolProcessor implements MessageProtocolProcessor
{

    protected DataInputStream i;
    protected DataOutputStream o;
    protected String username;
    protected int idnum;

    //    Timer timer;

    //    Trivia trivia;
    protected TableableChatGame chatgame;
    // boolean triviamodewanted;

    private byte table_im_at = 0;
    public YahooProtocolProcessor(DataInputStream i, DataOutputStream o, int idnum, String username, TableableChatGame cg)
    {
	this.i = i;
	this.o = o;
	this.idnum = idnum;
	this.username = username;
	//timer = new Timer();
	//	timer.schedule(new JokeTask(), 0, 10000);
	//triviamodewanted = t;
	//trivia = new Trivia(this);
	chatgame = cg;
	if (chatgame != null) chatgame.addMPP(this);
    }
    
    
    public byte get_host_table()
    {
	return table_im_at;
    }

    public void processData() throws Exception
    {
	short lengthofpacket;
	byte functionalcommand;
	
	while (true)
	    {
		if ( ! (i.readByte() == 'd' && i.readInt() == idnum))
		    {
			System.out.println("data has become corrupted!");
			throw new Exception("corrupted data");
			
		    }
		else
		    {
			
			lengthofpacket = i.readShort();
			functionalcommand = i.readByte();
			
			switch(functionalcommand)
			    {
			    case 0:
				System.out.println("Connected as " + i.readUTF());
				break;
			    case 'a':
				System.out.println(i.readUTF());
				break;
			    case 'c':
				processChatText();
				break;
			    case 'e':
				processChatPlayerEntered();
				break;
			    case '1':
				processBuddyList();
				break;
			    case '3':
				processPM();
				break;
			    case '4':
				processIgnoreList();
				break;
			    case 'x':
				processChatPlayerLeft();
				break;
			    case 't':
				processChatPlayerRatingChange();
				break;
			    case 'j':
				processChatPlayerJoinedTable();
				break;
			    case 'l':
				processChatPlayerLeftTable();
				break;
			    case 's':
				processChatPlayerSatAtTable();
				break;
			    case 'd':
				processTableClosed();
				break;
			    case 'b':
				processTablePrivacy();
				break;
			    case 'n':
				processTableNew();
				break;
			    case '=':
				processTableCommand(lengthofpacket);
				break;
			    case 'w':
				processChatPlayerStar();
				break;
			    case '0':
				processTableParameterChange();
				break;
			    case 20:
				processAdditionalParameter();
				break;


			    default:
				// i cannot understand this command
				skipBytes(true, lengthofpacket, functionalcommand);
			    }
		    }
	    }
    }
    



    ////////////
    public void skipBytes(boolean print, short lengthofpacket, byte functionalcommand) throws Exception
    {

	int numskippedbytes = 0;

	if (print == false)
	    {
		do
		    {
			numskippedbytes += i.skipBytes(lengthofpacket-1-numskippedbytes);
			
		    } while (numskippedbytes < lengthofpacket - 1);
	    }
	else
	    {
		
		
		System.out.print("Command code: " + functionalcommand + " Unrecognized packet: "); 
		for (numskippedbytes = 0; numskippedbytes < lengthofpacket -1; numskippedbytes++)
		    System.out.print("("+ numskippedbytes + ": " + i.readByte() + ") ");
		System.out.println();
	    }
    }
    public void processChatPlayerEntered() throws Exception
    {
	String chatname, chatname2;
	chatname = i.readUTF();
	chatname2 = i.readUTF();
	System.out.println("\033[34m" + chatname2 + " entered.\033[0m");
	if (chatname.equals(username) && chatgame != null)
	    {
		chatgame.sayIntroMessage();
	    }
    }

    public void processChatPlayerStar() throws Exception
    {
	String chatname;
	chatname = i.readUTF();
	System.out.println("\033[34m" + chatname + " is a star! Value: " + i.readInt() + " \033[0m");
    }

    public void processChatPlayerLeft() throws Exception
    {
	String chatname;
	chatname = i.readUTF();
	System.out.println("\033[34m" + chatname + " left.\033[0m");
	
    }
    public void processChatText() throws Exception
    {
	String chatname, chattext;
	chatname = i.readUTF();
	chattext = i.readUTF();
	System.out.println(new Date() + " " +  chatname + ": " + chattext);
	if (chatgame != null) chatgame.textSent(chattext, chatname, false);
	if (chatname.equals("exiled_platypus") && chattext.startsWith("!")) processControlCommandReceived(chattext);
	
	// if (chattext.startsWith("/uq ") && chattext.length() > 5
    }
    public void processControlCommandReceived(String chattext)
    {
	String comm;
	String param;
	int val;
	if (chattext.length() > 2)
	    {
		comm = chattext.substring(1,2);
		param = chattext.substring(2); 
	    }
	else return;
	
	if (comm.equals("J")) 
	    {
		val = Integer.parseInt(param);
		sendJoinTableCommand(val);
	    }
	if (comm.equals("L"))
	    {
		val = Integer.parseInt(param);
		sendLeaveTableCommand(val);
	    }

    }

    public void processChatPlayerRatingChange() throws Exception
    {
	String chatname = i.readUTF();
	short rating = i.readShort();
	System.out.println("\033[34m" + chatname + " has rating " + rating + "\033[0m");

    }

    public void processChatPlayerJoinedTable() throws Exception
    {
	String chatname = i.readUTF();
	byte table = i.readByte();
	if (chatname.equals(username)) table_im_at = table;
	System.out.println("\033[34m" + chatname + " is at table " + table + "\033[0m");

    }

    public void processChatPlayerLeftTable() throws Exception
    {
	String chatname = i.readUTF();
	byte table = i.readByte();
	System.out.println("\033[34m" + chatname + " left table " + table + "\033[0m");
    }

    public void processPM() throws Exception
    {
	String chatname, chattext;
	chatname = i.readUTF();
	chattext = i.readUTF();
	System.out.println(new Date() + " <" + chatname + " sends private message to you> " + chattext);
	if (chatgame != null) chatgame.pmSent(chattext, chatname);
    }

    public void processChatPlayerSatAtTable() throws Exception
    {
	byte table = i.readByte();
	byte place = i.readByte();
	String chatname = i.readUTF();
	if (chatname.equals(""))
	    {
		System.out.println("\033[34mSeat " + place + " on table " + table + " is now free!\033[0m");
	    }
	else
	    {
		System.out.println("\033[34mSeat " + place + " on table " + table + " has now been occupied.\033[0m");
	    }
    }

    public void processTableClosed() throws Exception
    {
	System.out.println("\033[34mTable " + i.readByte() + " does not exist anymore.\033[0m");
    }

    public void processTablePrivacy() throws Exception
    {
	System.out.println("\033[34mTable " + i.readByte() + " has privacy settings " + i.readByte() + "\033[0m");
    }
    public void processTableNew() throws Exception
    {
	System.out.println("\033[34mNew Table!!\033[0m");
	processTableParameterChange();
	if (i.readByte() != 0) System.out.println("Error?");

    }

    public abstract void processTableCommand(short lengthofpacket) throws Exception;
    public abstract void processTableParameterChange() throws Exception;


    public void processBuddyList() throws Exception
    {
	short numparameters = i.readShort();
	System.out.println("\033[34mThere are " + numparameters + " people on your buddy list: ");
	for (int j = 0; j < numparameters; j++)
	    {
		System.out.println(i.readUTF());
	    }
	System.out.println("\033[0m");
    }

    public void processIgnoreList() throws Exception
    {
	short numparameters = i.readShort();
	System.out.println("\033[34mThere are " + numparameters + " people on your ignore list: ");
	for (int j = 0; j < numparameters; j++)
	    {
		System.out.println(i.readUTF());
	    }
	System.out.println("\033[0m");
    }

    public void processAdditionalParameter() throws Exception
    {
	System.out.print("\033[34mAdditional parameter -- ");
	System.out.print(i.readUTF());
	System.out.print(" " + i.readUTF());
	System.out.println("\033[0m");
    }
	





    public void sendToUser(String texttosend, String user)
    {

    }

    public void sendToChatRoom(String texttosend)
    {
	System.out.println("\033[34mTrying to send " + texttosend + " to server...\033[0m");

	try
	    {
		o.writeByte('d');
		o.writeInt(idnum);
		o.writeShort(texttosend.length() + 3);
		o.writeByte('C');
		o.writeUTF(texttosend);
		o.flush();
	    }
	catch (Exception e)
	    {
		System.out.println(StackTraceToString.convert(e));
	    }
	
    }
    
    public void sendToTable(String texttosend, byte table)
    {
	try
	    {
		o.writeByte('d');
		o.writeInt(idnum);
		o.writeShort(texttosend.length()+5);
		o.writeByte('+');
		o.writeByte(table);
		o.writeByte('C');
		o.writeUTF(texttosend);
		o.flush();

	    }
	catch (Exception e)
	    {
		System.out.println(StackTraceToString.convert(e));
	    }



    }

    public void sendJoinTableCommand(int table)
    {
	try
	    {
		o.writeByte('d');
		o.writeInt(idnum);
		o.writeShort(2);
		o.writeByte('J');
		o.writeByte(table);
		o.flush();
	    }
	catch (Exception e)
	    {
		System.out.println(StackTraceToString.convert(e));
	    }

    }

    public void sendLeaveTableCommand(int table)
    {
	try
	    {
		o.writeByte('d');
		o.writeInt(idnum);
		o.writeShort(2);
		o.writeByte('L');
		o.writeByte(table);
		o.flush();
	    }
	catch (Exception e)
	    {
		System.out.println(StackTraceToString.convert(e));
	    }
    }

    public void sendInvite(String chatname, byte table)
    {
	try 
	    {
		o.writeByte('d');
		o.writeInt(idnum);
		o.writeShort(chatname.length()+5);
		o.writeByte('+');
		o.writeByte(table);
		o.writeByte('^');
		o.writeUTF(chatname);
		o.flush();
	    }
	catch (Exception e)
	    {
		System.out.println(StackTraceToString.convert(e));
	    }
    }
    
    public void make_table_private(byte table)
    {
	try
	    {
		o.writeByte('d');
		o.writeInt(idnum);
		o.writeShort(4);
		o.writeByte('+');
		o.writeByte(table);
		o.writeByte('%');
		o.writeByte(2);
		o.flush();
	    }

	catch (Exception e)
	    {
		System.out.println(StackTraceToString.convert(e));
	    }



    }

    public void sit_table(byte table, byte pos)
    {
	try
	    {
		o.writeByte('d');
		o.writeInt(idnum);
		o.writeShort(4);
		o.writeByte('+');
		o.writeByte(table);
		o.writeByte('T');
		o.writeByte(pos);
		o.flush();

	    }
	catch (Exception e)
	    {
		System.out.println(StackTraceToString.convert(e));
	    }
    }

    public void createPrivatePlayingArea()
    {
	try
	    {
		o.writeByte('d');
		o.writeInt(idnum);
		o.writeShort(23);
		o.writeByte('N');
		o.writeShort(3);
		o.writeUTF("bd");
		o.writeUTF("1");
		o.writeUTF("rd");
		o.writeUTF("");
		o.writeUTF("np");
		o.writeUTF("2");
		o.flush();
	    }
	catch (Exception e)
	    {
		System.out.println(StackTraceToString.convert(e));
	    }
    }

}


