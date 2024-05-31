/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.example;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.StanzaBuilder;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.stringprep.XmppStringprepException;

class FakeAuctionServer {

    private final SingleMessageListener messageListener = new SingleMessageListener();

    private static final String ITEM_ID_AS_LOGIN = "auction-%s";
    private static final String AUCTION_PASSWORD = "auction-%s";
    private static final String XMPP_HOSTNAME = "localhost";
    private static final int XMPP_PORT = 5222; // Default XMPP port

    private final String itemId;
    private final XMPPTCPConnection connection;
    private ChatManager chatManager;
    private Chat currentChat;

    public FakeAuctionServer(String itemId) {
        this.itemId = itemId;
        try {
            XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                    .setHost(XMPP_HOSTNAME)
                    .setPort(XMPP_PORT)
                    .setXmppDomain(XMPP_HOSTNAME) // Replace with your domain
                    .setUsernameAndPassword(String.format(ITEM_ID_AS_LOGIN, itemId), AUCTION_PASSWORD) // Optional: Set
                                                                                                       // username and
                                                                                                       // password if
                                                                                                       // needed
                    .build();
            this.connection = new XMPPTCPConnection(config);
        } catch (XmppStringprepException e) {
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
        chatManager.addIncomingListener(messageListener);
    }

    public String getItemId() {
        return itemId;
    }

    public void hasReceivedJoinRequestFromSniper() {
        try {
            messageListener.receivesAMessage((chat) -> {
                assertNotNull(chat);
                currentChat = chat;
            });
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void announceClosed() {
        assertNotNull(currentChat);
        Message message = StanzaBuilder.buildMessage()
                .ofType(Message.Type.chat)
                .to(currentChat.getXmppAddressOfChatPartner())
                .setBody("")
                .build();
        try {
            currentChat.send(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        connection.disconnect();
    }
}

class SingleMessageListener implements IncomingChatMessageListener {

    private final ArrayBlockingQueue<IncomingMessage> messages = new ArrayBlockingQueue<>(1);

    @Override
    public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
        messages.add(new IncomingMessage(from, message, chat));
    }

    public void receivesAMessage(Consumer<Chat> setAuctionServerChatSession) throws InterruptedException {
        var message = messages.poll(5, TimeUnit.SECONDS);
        assertNotNull(message);
        setAuctionServerChatSession.accept(message.chat);
    }

    private record IncomingMessage(EntityBareJid from, Message body, Chat chat) {
    }

}
