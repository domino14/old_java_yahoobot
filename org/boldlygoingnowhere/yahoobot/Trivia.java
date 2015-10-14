package org.boldlygoingnowhere.yahoobot;

import java.sql.*;
import java.util.*;
import java.util.Date;

public class Trivia implements TableableChatGame
{
    Connection question_database_connector = null;
    Random generator;
    int number_of_questions_in_database;
    final int noq_in_current_round = 15;
    int current_question = 0;
    TriviaDatum currently_active_datum;
    MessageProtocolProcessor mpp;
    final String beginmessagestring = "*****    ";
    Hashtable players;
    Timer timer;
    boolean gameon;
    boolean alreadyanswered;
    Date date_when_asked, date_when_answered;

    int[] idNumsOfQuestions;

    String playerlastanswered;
    int streakofrightanswers;
    boolean tablemode;
    byte table;
    public Trivia()
    {
	connectToQuestionDatabase();
	generator = new Random();
	players = new Hashtable();
	timer = new Timer();
    }

    public void settablemode()
    {
	tablemode = true;
    }
    
    public void destroyTimer()
    {
	if (timer != null) timer.cancel();
    }

    public void addMPP(MessageProtocolProcessor mpp)
    {
	this.mpp = mpp;
    }

    public boolean currently_active()
    {
	// is trivia game running?
	return gameon;
    }

    public boolean gettablemode()
    {
	return tablemode;
    }
    
    public void sendtext(String texttosend, boolean gametext)
    {
	if (tablemode == true)
	    {
		if (gametext == true) mpp.sendToTable(texttosend, table);
		if (gametext == false) mpp.sendToChatRoom(texttosend);


	    }
	else mpp.sendToChatRoom(texttosend);
    }
    
    public void connectToQuestionDatabase()
    {
	try
	    {
		Class.forName("com.mysql.jdbc.Driver");
		String url = "jdbc:mysql://localhost/Trivia";
		String user = "cesar";
		String password = "triviapw";
		question_database_connector = DriverManager.getConnection(url, user, password);
	    }
	catch (Exception e)
	    {
		System.out.println(StackTraceToString.convert(e));

	    }

    }
    
    public void update_number_of_questions_in_database()
    {
	String select;
	ResultSet rows;

	int numquestions = 0;
	
	try
	    {
		Statement s = question_database_connector.createStatement();
		select = "select count(*) from questions";
		rows = s.executeQuery(select);
		

		while (rows.next())
		    {
			numquestions = rows.getInt("count(*)");
		    }


	    }
	catch (Exception e)
	    {
		System.out.println(StackTraceToString.convert(e));

		System.exit(0);
	    }
	finally
	    {
		number_of_questions_in_database = numquestions;
	    }
    }

    public void update_question(int id, String newquestion)
    {

	String select;
	try
	    {
		Statement s = question_database_connector.createStatement();
		select = "UPDATE questions SET question = \"" + newquestion + "\" where id = " + id;
		s.executeUpdate(select);
	    }

	catch(Exception e)
	    {
		System.out.println(StackTraceToString.convert(e));

	    }
    }




    public void update_answer(int id, String newanswer)
    {

	String select;


	try
	    {
		Statement s = question_database_connector.createStatement();
		select = "UPDATE questions SET answer = \"" + newanswer + "\" where id = " + id;
		s.executeUpdate(select);
	    }

	catch(Exception e)
	    {
		System.out.println(StackTraceToString.convert(e));

	    }


    }



    public void reindexTable()
    {
	String select;

	try
	    {
		Statement s = question_database_connector.createStatement();
		select = "alter table questions drop column id";
		s.executeUpdate(select);
		select = "alter table questions add id int unsigned not null auto_increment, add primary key(id)";
		s.executeUpdate(select);
		System.out.println("Table has been reindexed and is ready to receive queries");
	    }
	catch (Exception e)
	    {
		System.out.println(StackTraceToString.convert(e));

		System.exit(0);
	    }
    }
    
    boolean checkifrecord(String category, double record, String player)
    {
	if (idNumsOfQuestions.length < 1000) return false;
	// blah blah
	String select;
	ResultSet rows = null;
	Statement s = null;
	String orderstring;
	double storedrecord = 0.0;
	int recordid = 0;
	boolean update = false;
	try
	    {
		s = question_database_connector.createStatement();
		if (category.equals("Fastest times")) 
		    {
			orderstring = "DESC";
			if (record < 0.7) return false; // temporary workaround
		    }
		else orderstring = "ASC";
		select = "select record, id from records where category = '" + category + "' order by record " + orderstring;
		
		rows = s.executeQuery(select);
		if (rows.next())
		    {
			storedrecord = rows.getDouble("record");
			recordid = rows.getInt("id");
		    }
		else throw new Exception("wtf");
		if (category.equals("Fastest times"))
		    {
			// since they are ordered in DESCENDING order, the highest time should be first.
			if (record < storedrecord) 
			    update = true;
		    }
		else
		    {
			/// in ascending order, the lowest score should be first
			if (record > storedrecord)
			    update = true;
		    }

		if (update)
		    {
			Date time = new Date();
			select = "update records set record = " + record + ", qid = " + currently_active_datum.qid + 
			    ", time = '" + time.toString() + "', playername = '" + player + "' where id = " + recordid;
			s.executeUpdate(select);


		    }

	    }
	catch (Exception e)
	    {
		System.out.println(StackTraceToString.convert(e));
		
	    }
	finally
	    {
		try
		    {
		        if (rows!=null) rows.close();
			if (s!=null) s.close();
			System.out.println("After closing rows and s in checkifrecord");
			
		    }
		catch (SQLException sqlEx) { }
	    }
   
	return update;
    }

    
    void getIndexNums(String WhereCondition)
    {
	
	String select;
	ResultSet rows = null;
	int numquestions = 0;
	Statement s = null;
	try
	    {
		s = question_database_connector.createStatement();
		if (WhereCondition != null)
		    select = "SELECT count(*) FROM questions WHERE question like '%" + WhereCondition + "%'";
		else
		    select = "SELECT count(*) from questions";
		rows = s.executeQuery(select);
		while (rows.next())
		    {
			numquestions = rows.getInt("count(*)");
		    }
		System.out.println(numquestions + " questions satisfy condition: " + WhereCondition);
		idNumsOfQuestions = new int[numquestions];
		System.out.println("After allocating retarr");
		if (WhereCondition != null)
		    select = "SELECT id FROM questions WHERE question like '%" + WhereCondition + "%'";
		else
		    select = "SELECT id FROM questions";
		rows = s.executeQuery(select);
		System.out.println("After executing select query");
		int i = 0;
		while (rows.next())
		    {
			idNumsOfQuestions[i] = rows.getInt("id");
			i++;

		    }
		System.out.println("After assigning all of retarr");
		
		
	    }
	catch (Exception e)
	    {
		System.out.println(WhereCondition + " is a malformed query.");
	       
		idNumsOfQuestions = null;
		
	    }

	finally
	    {
		try
		    {
		        if (rows!=null) rows.close();
			if (s!=null) s.close();
			System.out.println("After closing rows and s in getindexnums");
			
		    }
		catch (SQLException sqlEx) { }
	    }

    }


    public TriviaDatum getDatum(int idnum)
    {
	String select;
	ResultSet rows = null;
	Statement s = null;
	TriviaDatum td = null;
	try
	    {
		s  = question_database_connector.createStatement();
		//int  = generator.nextInt(number_of_questions_in_database) + 1;
		select = "select question,answer from questions where id = " + idnum;
		rows = s.executeQuery(select);
		System.out.println("\t\tQuestion id = " + idnum);
		while (rows.next())
		    {
			td = new TriviaDatum(rows.getString("question"), rows.getString("answer"), idnum);
		    }

	    }
	catch (Exception e)
	    {
		System.out.println(StackTraceToString.convert(e));

		System.exit(0);
	    }

	finally
	    {
		try
		    {
			if (rows!= null) rows.close();
			if (s!=null) s.close();
		    }
		catch (SQLException sqlEx) { }

	    }


	return td;
    } 


    public static void main(String args[])
    {
	Trivia app = new Trivia();
	
    }

    public void resetgame()
    {
	current_question = 1;
	players.clear();
	timer.cancel();
	timer = new Timer();
	gameon = false;
	alreadyanswered = true; // say it's true because no questions have been asked yet
	playerlastanswered = "";
	streakofrightanswers = 0;
    }

    public void sayIntroMessage()
    {
	if (tablemode == false)
	    {
		sendtext("Cesar's Trivia Bot Version 4.0", false);
		sendtext("To start, type in /trivia", false);
	    }
	else
	    {

		// create table
		mpp.createPrivatePlayingArea();
		timer.schedule (new GetTableTask(), 5000);
		
	    }
    }

    public void startgame(int interval, String query)
    {
	resetgame();
	

	getIndexNums(query);
	

	if (idNumsOfQuestions != null)
	    {
	
		if (!(idNumsOfQuestions.length < 100)) 
		    {  
			sendtext(beginmessagestring + " TRIVIA BOT STARTING IN " + interval + " SECONDS", true);
			sendtext("Cesar's Trivia Bot Version 4.0", true);
		        sendtext("Running on Linux, created in Java with Emacs", true);
			sendtext("Questions loaded: " + idNumsOfQuestions.length, true);
			if (tablemode) sendtext("Here's a tip: make the table window bigger to see more of the chat!", true);
			timer.schedule (new AskAQuestionTask(), interval * 1000);
			
			gameon = true;
		    }
		else 
		    {
			idNumsOfQuestions = null;
		        sendtext("There are either too few questions, or you used the incorrect syntax. Try something like /trivia science or /trivia geography", true);
		    }
	    }
	else
	    sendtext("Malformed query. Try something like /trivia simpsons or /trivia science", true);

    }

    public void prepareForNextQuestion()
    {
	if (current_question > noq_in_current_round)
	    {
		sendtext(beginmessagestring + "That was the final question of the round. Scores coming"+
				   " in 8 seconds", true);
		timer.schedule(new PrintFinalScoreBoardTask(), 8000);
	    }
	else
	    {
		sendtext(beginmessagestring + "Question " + current_question + " of " +
				   noq_in_current_round + " coming in a few seconds...", true);
		timer.schedule(new AskAQuestionTask(), 12000);
	    }
	
	
    }

    public void pmSent(String textsent, String chatname)
    {
    }

    public void textSent(String textsent, String chatname, boolean intable)
    {
	if (!currently_active())
	    {
		if (!tablemode)
		    {
			if (textsent.equals("/trivia"))
			    startgame(20, null);
			if (textsent.startsWith("/trivia ") && textsent.length() > 8)
			    startgame(20, textsent.substring(8));
		    }
		else
		    {
			if (textsent.equals("/invite"))
			    mpp.sendInvite(chatname, table);
		    }
		    
	    }
	else
	    {
		if (textsent.equals("/invite"))
		    mpp.sendInvite(chatname, table);
		else
		    if ( (intable == false && tablemode == false) || 
			 (intable == true && tablemode == true)) playerAnswered(textsent, chatname);
		
	    }
    }
    public void playerAnswered(String playeranswer, String chatname)
    {
	if (alreadyanswered == true) return;
	date_when_answered = new Date();
	int scoreforthisq;
	playeranswer = playeranswer.toUpperCase().replaceAll("\\s", "");
	playeranswer = playeranswer.replaceAll("\\W", "");

	//	System.out.println("player: " + playeranswer + " simplified: " + currently_active_datum.simplifiedanswer);
	if (currently_active_datum.simplifiedanswer.equals(playeranswer))
	    {
		long millisellapsed = date_when_answered.getTime() - date_when_asked.getTime();
		double timeelapsed = (double) millisellapsed / (double)1000.0;
		scoreforthisq = (int)Math.round((double)currently_active_datum.simplifiedanswer.length() * 
					   (double)600.0 / timeelapsed);
		alreadyanswered = true;
		// kill timer and restart it
		timer.cancel();
		timer = new Timer();
		sendtext(beginmessagestring + chatname + " got the right answer in " + 
				   Math.round(timeelapsed*100)/100D +
				   " seconds!   (" + currently_active_datum.answer + ")", true);
	       
		if (checkifrecord("Fastest times", timeelapsed, chatname) | checkifrecord("Most points per question", (double)scoreforthisq, chatname))
		    sendtext(chatname + " has a new record in the Hall of Fame!", true);
		
		if (chatname.equals(playerlastanswered)) 
		    {
			streakofrightanswers++;
			
		    }
		else
		    {
			playerlastanswered = chatname;
			streakofrightanswers = 1;
		    }
		if (streakofrightanswers > 2) sendtext(beginmessagestring + chatname + " has answered the last " +
								 streakofrightanswers + " questions in a row!", true);
		Integer currentscore = (Integer)players.get(chatname);
		int scoresofar;
		if (currentscore == null)
		    {
			players.put(chatname, new Integer(scoreforthisq));
			scoresofar = scoreforthisq;
		    }
		else 
		    {
			scoresofar = currentscore.intValue();
			scoresofar += scoreforthisq;
			players.put(chatname, new Integer(scoresofar));
		    }

		sendtext(beginmessagestring + chatname + " now has " + scoresofar
				   + " points!   (" + 
				   scoreforthisq + " from last question.)", true);
		prepareForNextQuestion();
	    }
    }

    class TriviaDatum
    {
	String question;
	String answer;
	String hint;
	int numwords;
	int qid;
	String simplifiedanswer;
	public TriviaDatum (String q, String a, int qid)
	{
	    this.qid = qid;
	    question = q.replaceAll("[^\\p{ASCII}]", "").trim();
	    
	    answer = a.trim();
	    System.out.println("..........New Question: " + question + ", " + answer);
	    hint = answer.substring(0, answer.length()/2);

	    String[] temp = answer.split(" ");
	    numwords = temp.length;

	    simplifiedanswer = answer.toUpperCase().replaceAll("\\s", "").replaceAll("\\W", ""); // only alphanumeric
	}

	public String toString()
	{
	    return "Question: " + question + ", Answer: " + answer + "(Words: " + numwords + "), Hint: " + hint;

	}
    }
    

    class AskAQuestionTask extends TimerTask
    {
	public void run()
	{
	    int idnum = idNumsOfQuestions[generator.nextInt(idNumsOfQuestions.length)];
	    currently_active_datum = getDatum(idnum);
	    sendtext(beginmessagestring + currently_active_datum.question.toUpperCase() + "    (" +
			       currently_active_datum.numwords +
			       (currently_active_datum.numwords > 1 ? " words)" : " word)"), true);
	    date_when_asked = new Date();
	    alreadyanswered = false;
	    current_question++;
	    timer.schedule(new SayHintTask(), 15000);
	}
    }

    class SayHintTask extends TimerTask
    {
	public void run()
	{
	    sendtext(beginmessagestring + "HINT: " + currently_active_datum.hint.toUpperCase() + "...", true);
	    timer.schedule(new SayFinalAnswerTask(), 20000);

	}

    }

    class SayFinalAnswerTask extends TimerTask
    {
	public void run()
	{
	    alreadyanswered = true;
	    sendtext(beginmessagestring + 
			       "Time ran out! The answer was " + currently_active_datum.answer.toUpperCase(), true);
	    playerlastanswered = "";
	    streakofrightanswers = 0;
	    prepareForNextQuestion();
	}
    }

    class RechargeTask extends TimerTask
    {
	public void run()
	{
	    startgame(10, null);
	    //	    gameon = false;
	    //sendtext("Trivia Bot has now fully recharged. Type /trivia to turn it on again!", true);
	}
    }
    
    class GetTableTask extends TimerTask
    {
	public void run()
	{
	    table = mpp.get_host_table();
	    if (table != 0)
		{
		    // make table private
		    // sit at table

		    mpp.make_table_private(table);
		    mpp.sit_table(table, (byte)0);

		    sendtext("*****      Cesar's Trivia Bot Version 4.0", false);
		    sendtext("*****      Trivia is starting in 3 minutes in table " + table + ". If you would like to be invited, type /invite", false);
		    // set new task
		    timer.schedule (new RechargeTask(), 3*60000);
		}
	    else
		{
		    timer.schedule(new GetTableTask(), 5000);
		    
		}
	}
    }


    class PrintFinalScoreBoardTask extends TimerTask
    {
	public void run()
	{
	    	    
	    //mpp.sendToChatRoom(beginmessagestring + players.toString());
	    
	    //    Vector v = new Vector(players.keyS
	    Enumeration e = players.keys();
	    if (players.size() > 0)
		{
		    PlayerScore[] pscores = new PlayerScore[players.size()];
		    int i = 0;
		    while (e.hasMoreElements())
			{
			    String tempplay = (String)e.nextElement();
			    Integer value = (Integer)(players.get(tempplay));
			    pscores[i] = new PlayerScore(tempplay, value.intValue());
			    checkifrecord("Most points per round", pscores[i].score, pscores[i].player);
			    //		    System.out.println((String)e.nextElement() + " " + value.doubleValue());
			    i++;
			}
		    Arrays.sort(pscores); //Math.round(scoresofar*100) 
		    sendtext(beginmessagestring+"WINNER: " + pscores[0].player + " - " + 
				       pscores[0].score, true);

		    String strtosend;
		    for (int ii = 1; ii < pscores.length; ii+=3)
			{
			    strtosend = pscores[ii].abbreviatedplayer + " - " + pscores[ii].score;
			    if (ii + 1 < pscores.length) strtosend += " | " + pscores[ii+1].abbreviatedplayer 
							     + " - " + pscores[ii + 1].score;
			    if (ii + 2 < pscores.length) strtosend += " | " + pscores[ii+2].abbreviatedplayer
							     + " - " + pscores[ii+2].score;
			    sendtext(strtosend, true);
			    //		    System.out.println(pscores[ii].toString());
			}
		    
		    //	    resetgame();
		    
		}
	    
	    else
		{
		    sendtext("Strangely enough, no one got any points. What the fuck is wrong with you people??", true);
		}
	    
	    
	    sendtext(beginmessagestring + " To see the hall of fame, please go to http://trivia.hobby-site.org.", true);
	    sendtext("New game will start in approximately 3 minutes.", true);
	    timer.schedule(new RechargeTask(), 3*60000);
	    gameon = false;
	    if (tablemode) 
		sendtext("*****    Trivia is starting in 3 minutes in table " + table + ". If you would like to be invited, type /invite", false);	   
	}

    }


    class PlayerScore implements Comparable
    {
	String player;
	int score;
	String abbreviatedplayer;
	public PlayerScore (String p, int s)
	{
	    player = p;
	    score = s;
	    if (player.length() >= 10) abbreviatedplayer = p.substring(0, 10);
	    else abbreviatedplayer = player;
	}
	
	public String toString()
	{
	    return player + " " + score;
	}

	public int compareTo(Object o)
	{
	    PlayerScore x = (PlayerScore)o;
	    if ( score < x.score) return 1;
	    if ( score == x.score) return 0;
	    if ( score > x.score) return -1;
	    return 0;
	}
    }




}




