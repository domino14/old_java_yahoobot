package org.boldlygoingnowhere.yahoobot;


public interface ChatGame
{
    public boolean currently_active();
    public void startgame(int param1, String param2);
    public void sayIntroMessage();
    public void textSent(String textsent, String chatname, boolean intable);
    public void pmSent(String textsent, String chatname);
    public void addMPP(MessageProtocolProcessor mpp);
    public void destroyTimer();
}
