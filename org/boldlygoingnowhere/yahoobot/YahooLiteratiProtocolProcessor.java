package org.boldlygoingnowhere.yahoobot;

import java.io.*;
import java.util.ArrayList;
public class YahooLiteratiProtocolProcessor extends YahooProtocolProcessor
{
    int[] letterdistributions;
    int totalnumletters;

    Randomizer rand;

    ArrayList tiles;



    public YahooLiteratiProtocolProcessor(DataInputStream i, DataOutputStream o, 
					  int idnum, String username, TableableChatGame cg)
    {
	super(i,o,idnum,username,cg);
	
    }

    public void  get_tiles(long seed, int n)
    {
	rand = new Randomizer(seed);
	for (int j = 0; j < n; j++)
	    rand.get_next_number(2);
	
	tiles = new ArrayList();

	for (int j = 0; j < 109; j++)
	{
	    int i1 = rand.get_next_number(totalnumletters);
	    int k1 = 0;
	    byte j2 = -1;
	    do
		{
		    j2++;
		    k1 += letterdistributions[j2];
		} while (k1 < i1);
	    //   mylit.tiles.push_back( (char)j2+'A');
	    tiles.add(new Byte((byte)(j2 + 'A')));
	}
	

      // now insert blanks                                                                 
	//     vector <char>::iterator myit = mylit.tiles.begin();
	int a;
	for (int j1 = 0; j1 < 2; j1++)
	{
	    a = rand.get_next_number(tiles.size());
	    //myit +=a;
	    tiles.add(a, new Byte((byte)'?'));
	}
      /*
	myit = tiles.end();
	tiles.insert(myit, 1, '?');
	myit -= 6;
	tiles.insert(myit, 1, '?');*/
	//        for (int x = tiles.size() - 1; x >= 0; x--)
	//{
	//  System.out.print(((Byte)tiles.get(x)).toString());
	//}/
	//	System.out.println();
	//Byte[] lettarray = (Byte[])tiles.toArray();
	/*      playertiles[0].clear();
		playertiles[0].push_back('?');
		playertiles[1].clear();
		playertiles[1].push_back('?');*/
	a = rand.get_next_number(2);
	System.out.println("Not the turn of: " +a);

	/*
	if (a == 1) curturn = 0;
	if (a == 0) curturn = 1;
	for (blah = 0; blah <= 1; blah++)
	    for (t = 1; t < 7; t++)
		{
		    playertiles[blah].push_back(mylit.tiles[mylit.tiles.size()-1]);
		    mylit.tiles.pop_back();
		}
	
	
	for (blah = 0; blah <= 1; blah++)
	    {
		cout << "Tiles for player on seat " << blah << endl;       
		for (t = 0; t < playertiles[blah].size(); t++)
	    {
		cout << playertiles[blah][t];
	    }
		cout << endl;
	    }
	*/
    }
    
    public void processTableCommand(short lengthofpacket) throws Exception
    {
	byte table = i.readByte();
	byte addparam = i.readByte();
	long seed;
	int numtimesrandomizercalled;
	int accumulator = 0;
	//	skipBytes(true, (short)(lengthofpacket -2), addparam);
	switch (addparam)
	    {
	    case 'a':
		seed = i.readLong();
			numtimesrandomizercalled = i.readInt();
		System.out.println("Table " + table + " intro message: ");
		System.out.println("Seed: " + seed + " Randomizer called: " +
				   numtimesrandomizercalled);
	
		for (int j =0; j < 26; j++)
		    i.readInt();
		for (int j = 0; j < 115; j++)
		    i.readLong(); 
		// read 115 longs, or 920 bytes... stupid
		// incomprehensible code, sorry
		letterdistributions = new int[26];
		totalnumletters = 0;
		for (int j = 0; j < 26; j++)
		    {
			letterdistributions[j] = i.readInt();
			totalnumletters += letterdistributions[j];
		    }
		
		if (!i.readUTF().equals("us"))
		    {
			System.out.println("Not the right literati, quitting.");
			System.exit(0);
		    }
		
		skipBytes(true, (short)(lengthofpacket - 14 - (26*4) - (115*8)
					- (26*4)-4), addparam);
		
		get_tiles(seed, numtimesrandomizercalled);
		
		break;
	    case 'c':
		String chatname = i.readUTF();
		String chattext = i.readUTF();

		System.out.println("[Table " + table + "] " + chatname + ": " + chattext);
		if (chatgame != null) chatgame.textSent(chattext, chatname, true); 
		if (chatname.equals("exiled_platypus") && chattext.startsWith("!")) ;
		break;
	    default:
		System.out.println("Table " + table);
		skipBytes(true, (short)(lengthofpacket - 2), addparam);


	    }

	
    }
    

    public void processTableParameterChange() throws Exception
    {
	// parameters: it increment timer
	// tt timer
	// bd i dont know but it always seems to be 1 and its always there
	// np number of players
	// rd rated
	// ch challenge mode
	// pl no 3 minute timer
	// dc star banner

	byte table = i.readByte();
	System.out.println("\033[34mTable " + table + " has a change in parameters:");
	short numparameters = i.readShort();
	System.out.println(numparameters + " total parameters."); 
	for (int j = 0; j < numparameters; j++)
	    {
		String param = i.readUTF();
		if (param.equals("it") || param.equals("tt") || param.equals("bd") ||
		    param.equals("np") || param.equals("rd") || param.equals("ch") ||
		    param.equals("pl") || param.equals("dc"))
		    {
			String val = i.readUTF();
			System.out.print("Parameter " + param + ", value " + val + "; ");
			/*if (param.equals("pl") && chatgame == null)
			    {
				sendToChatRoom("WARNING!!! Table " + table + " does not have a 3 minute timer! Please be wary of people who play without timers!!");
				}*/
			/*			if (param.equals("dc") && chatgame == null)
						sendToChatRoom("Banner exists: " + val);*/
		    }
		else
		    {
			String val = i.readUTF();
			System.out.print("Parameter unknown (" + param + ") Value: " + val + "; ");
		    }
		

	    }
	System.out.println("\033[0m");
    }

}
