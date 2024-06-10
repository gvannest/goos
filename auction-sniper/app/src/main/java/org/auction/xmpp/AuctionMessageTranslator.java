package org.auction.xmpp;

import java.util.HashMap;

import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;

public class AuctionMessageTranslator implements IncomingChatMessageListener {
    private final AuctionEventListener listener;

    public AuctionMessageTranslator(AuctionEventListener listener) {
        this.listener = listener;
    }

    @Override
    public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
        HashMap<String, String> event = unpackEventFrom(message);
        String eventType = event.get("Event");
        switch (eventType) {
            case "CLOSE" -> listener.auctionClosed();
            case "PRICE" -> listener.currentPrice(Integer.parseInt(event.get("CurrentPrice")),
                    Integer.parseInt(event.get("Increment")));
            default -> {
            }
        }
    }

    private HashMap<String, String> unpackEventFrom(Message message) {
        HashMap<String, String> event = new HashMap<>();
        for (String element : message.getBody().split(";")) {
            String[] pair = element.split(":");
            event.put(pair[0].trim(), pair[1].trim());
        }
        return event;
    }

}
