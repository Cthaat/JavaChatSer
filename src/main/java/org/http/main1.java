package org.http;

import org.Sql.p2pRoomChat;
import org.Sql.publicRoomfind;

public class main1
{
    public static void main(String[] args)
    {
        p2pRoomChat p2p = new p2pRoomChat();
        p2p.loadMessagesFromTwoUsers("admin" , "testUser3");
    }
}
