package org.auction.xmpp;

import org.auction.Main;
import org.auction.domain.Auction;
import org.jivesoftware.smack.chat2.Chat;

public class XMPPAuction implements Auction {
    private Chat chat;

    public XMPPAuction(Chat chat) {
        this.chat = chat;
    }

    @Override
    public void bid(int biddingPrice) {
        sendMessage(String.format(Main.BID_COMMAND_FORMAT, biddingPrice));
    }

    @Override
    public void join() {
        sendMessage(Main.JOIN_COMMAND_FORMAT);
    }

    private void sendMessage(final String message) {
        try {
            chat.send(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    };
}