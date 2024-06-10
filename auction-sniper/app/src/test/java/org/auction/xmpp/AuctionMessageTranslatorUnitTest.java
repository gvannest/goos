package org.auction.xmpp;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.StanzaBuilder;
import org.junit.jupiter.api.Test;
import org.jxmpp.jid.EntityBareJid;

public class AuctionMessageTranslatorUnitTest {
    public static final Chat UNUSED_CHAT = null;
    public static final EntityBareJid UNUSED_ENTITY_BARE_JID = null;
    private final AuctionEventListener listener = new AuctionEventListenerSpy();
    private final AuctionMessageTranslator translator = new AuctionMessageTranslator(listener);

    @Test
    public void notifiesAuctionClosedWhenCloseMessageReceived() {
        Message message = StanzaBuilder.buildMessage()
                .ofType(Message.Type.chat)
                .setBody("SOLVersion: 1.1; Event: CLOSE;")
                .build();

        translator.newIncomingMessage(UNUSED_ENTITY_BARE_JID, message, UNUSED_CHAT);

        assertTrue(((AuctionEventListenerSpy) listener).auctionClosedHasBeenCalledOnce(),
                ((AuctionEventListenerSpy) listener).auctionEventListenerError());
    }

    @Test
    public void notifiesBidDetailsWhenCurrentPriceMessageReceived() {
        Message message = StanzaBuilder.buildMessage()
                .ofType(Message.Type.chat)
                .setBody("SOLVersion: 1.1; Event: PRICE; CurrentPrice: 192; Increment: 7; Bidder: Someone else;")
                .build();

        translator.newIncomingMessage(UNUSED_ENTITY_BARE_JID, message, UNUSED_CHAT);

        assertTrue(((AuctionEventListenerSpy) listener).currentPriceHasBeenCalledOnceWith(192, 7),
                ((AuctionEventListenerSpy) listener).auctionEventListenerError());
    }
}

class AuctionEventListenerSpy implements AuctionEventListener {
    public int auctionClosedTimesCalled = 0;
    private int currentPriceTimesCalled = 0;
    private int currentPrice = 0;
    private int increment = 0;

    @Override
    public void auctionClosed() {
        auctionClosedTimesCalled += 1;
    }

    @Override
    public void currentPrice(int price, int increment) {
        currentPriceTimesCalled += 1;
        this.currentPrice = price;
        this.increment = increment;
    }

    boolean auctionClosedHasBeenCalledOnce() {
        return auctionClosedTimesCalled == 1;
    }

    String auctionEventListenerError() {
        return "\n" + String.format("AuctionClosed was called %d times\n", auctionClosedTimesCalled)
                + String.format("currentPrice was called %d times with price %d and increment %d\n",
                        currentPriceTimesCalled, this.currentPrice, this.increment);
    }

    boolean currentPriceHasBeenCalledOnceWith(int currentPrice, int increment) {
        return currentPriceTimesCalled == 1 && this.currentPrice == currentPrice && this.increment == increment;

    }

}
