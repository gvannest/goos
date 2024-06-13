/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.auction;

import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.StanzaBuilder;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import java.net.InetAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FakeAuctionServer {
    private final SingleMessageListener messageListener = new SingleMessageListener();

    private static final String ITEM_ID_AS_LOGIN = "auction-%s";
    private static final String AUCTION_PASSWORD = "auction";
    private static final String RESOURCE = "Auction";
    private static final String XMPP_HOSTNAME = "localhost";
    private static final int XMPP_PORT = 5222;

    private final String itemId;
    private final XMPPTCPConnection connection;
    private ChatManager chatManager;
    private Chat currentChat;

    public FakeAuctionServer(String itemId) {
        this.itemId = itemId;
        try {
            InetAddress hostAddress = InetAddress.getByName(XMPP_HOSTNAME);
            XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                    .setHostAddress(hostAddress)
                    .setPort(XMPP_PORT)
                    .setXmppDomain(XMPP_HOSTNAME)
                    .setUsernameAndPassword(String.format(ITEM_ID_AS_LOGIN, itemId), AUCTION_PASSWORD)
                    .setResource(RESOURCE)
                    .setSecurityMode(XMPPTCPConnectionConfiguration.SecurityMode.disabled) // Disable security for local
                    // development
                    .build();
            this.connection = new XMPPTCPConnection(config);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void startSellingItem() {
        try {
            connection.connect();
            connection.login();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // Get the ChatManager instance
        chatManager = ChatManager.getInstanceFor(connection);
        chatManager.addIncomingListener((from, message, chat) -> {
            currentChat = chat;
            messageListener.newIncomingMessage(message);
        });

    }

    public String getItemId() {
        return itemId;
    }

    public void reportPrice(int price, int increment, String bidder) {
        try {
            currentChat
                    .send(String.format("SOLVersion: 1.1; Event: PRICE; CurrentPrice: %d; Increment: %d; Bidder: %s;",
                            price, increment, bidder));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void hasReceivedBid(int bid, String sniperId) {
        receivesAMessageMatching(sniperId,
                is(String.format(Main.BID_COMMAND_FORMAT, bid)));
    }

    public void hasReceivedJoinRequestFromSniper(String sniperId) {
        receivesAMessageMatching(sniperId, is(Main.JOIN_COMMAND_FORMAT));
    }

    public void announceClosed() {
        assertNotNull(currentChat);
        Message message = StanzaBuilder.buildMessage()
                .ofType(Message.Type.chat)
                .to(currentChat.getXmppAddressOfChatPartner())
                .setBody("SOL Version: 1.1; Event: CLOSE;")
                .build();
        try {
            currentChat.send(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void receivesAMessageMatching(String sniperId, Matcher<? super String> messageMatcher) {
        messageListener.receivesAMessage(messageMatcher);
        assertEquals(currentChat.getXmppAddressOfChatPartner() + "/" + RESOURCE, sniperId);
    }

    public void stop() {
        connection.disconnect();
    }
}

class SingleMessageListener {

    private final ArrayBlockingQueue<Message> messages = new ArrayBlockingQueue<>(1);

    public void newIncomingMessage(Message message) {
        messages.add(message);
    }

    public void receivesAMessage(Matcher<? super String> messageMatcher) {
        try {
            var message = messages.poll(5, TimeUnit.SECONDS);
            assertNotNull(message);
            MatcherAssert.assertThat(message.getBody(), messageMatcher);
        } catch (InterruptedException e) {
            throw new RuntimeException();
        }
    }

}
