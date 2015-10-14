package org.boldlygoingnowhere.yahoobot;

public interface MessageProtocolProcessor
{
    public void sendToChatRoom(String texttosend);
    public void sendToTable(String texttosend, byte table);
    public void sendToUser(String texttosend, String user);

    public void processData() throws Exception;
    public void sendInvite(String chatplayer, byte table);
    public void createPrivatePlayingArea();
    public byte get_host_table();

    public void make_table_private(byte table);
    public void sit_table(byte table, byte pos);
}
