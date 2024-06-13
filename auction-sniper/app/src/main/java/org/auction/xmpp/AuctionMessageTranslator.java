package org.auction.xmpp;

import org.auction.domain.AuctionEventListener;
import org.auction.domain.AuctionEventListener.PriceSource;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;

import java.util.HashMap;

public class AuctionMessageTranslator implements IncomingChatMessageListener {
    private final AuctionEventListener listener;
    private final String sniperId;

    public AuctionMessageTranslator(String sniperId, AuctionEventListener listener) {
        this.listener = listener;
        this.sniperId = sniperId;
    }

    @Override
    public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
        AuctionEvent event = AuctionEvent.from(message.getBody());
        String eventType = event.type();
        System.out.println("sniperId : " + sniperId);
        switch (eventType) {
            case "CLOSE" -> listener.auctionClosed();
            case "PRICE" -> listener.currentPrice(event.currentPrice(),
                    event.increment(), event.isFrom(sniperId));
            default -> {
            }
        }
    }


    private static class AuctionEvent {
        private final HashMap<String, String> fields = new HashMap<>();

        static AuctionEvent from(String messageBody) {
            AuctionEvent event = new AuctionEvent();
            for (String field : fieldsIn(messageBody)) {
                event.addField(field);
            }
            return event;
        }

        public PriceSource isFrom(String sniperId) {
            return bidder().equals(sniperId) ? PriceSource.FromSniper : PriceSource.FromOtherBidder;
        }

        private String type() {
            return get("Event");
        }

        private int currentPrice() {
            return getInt("CurrentPrice");
        }

        private int increment() {
            return getInt("Increment");
        }

        private int getInt(String fieldName) {
            return Integer.parseInt(get(fieldName));
        }

        private String get(String fieldName) {
            return fields.get(fieldName);
        }

        private String bidder() {
            return get("Bidder");
        }


        private static String[] fieldsIn(String messageBody) {
            return messageBody.split(";");
        }

        private void addField(String field) {
            String[] pair = field.split(":");
            fields.put(pair[0].trim(), pair[1].trim());
        }
    }

}
