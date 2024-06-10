/*
 * This source file was generated by the Gradle 'init' task
 */
package org.auction;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetAddress;

import javax.swing.SwingUtilities;

import org.auction.ui.MainWindow;
import org.auction.xmpp.AuctionEventListener;
import org.auction.xmpp.AuctionMessageTranslator;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;

public class Main implements AuctionEventListener {
    private static final int ARG_HOSTNAME = 0;
    private static final int ARG_USERNAME = 1;
    private static final int ARG_PASSWORD = 2;
    private static final int ARG_ITEM_ID = 3;

    private static final String ITEM_ID_AS_LOGIN = "auction-%s";
    private static final String AUCTION_ID_FORMAT = ITEM_ID_AS_LOGIN + "@%s";
    public static final String JOIN_COMMAND_FORMAT = "SQLVersion: 1.1; Command: JOIN;";
    public static final String BID_COMMAND_FORMAT = "SQLVersion: 1.1; Command: BID; Price: %d;";

    private MainWindow ui;
    private Chat chatNotToBeGCd;

    public Main() throws Exception {
        startUserInterface();
    }

    public static void main(String... args) throws Exception {
        Main main = new Main();
        main.joinAuction(conection(args[ARG_HOSTNAME], args[ARG_USERNAME], args[ARG_PASSWORD]), args[ARG_ITEM_ID]);

    }

    private void joinAuction(XMPPTCPConnection connection, String itemId)
            throws Exception {
        disconnectWhenUICloses(connection);
        ChatManager chatManager = ChatManager.getInstanceFor(connection);
        chatManager.addIncomingListener(new AuctionMessageTranslator(this));

        // Create a new chat session with the desired recipient
        EntityBareJid jid = JidCreate.entityBareFrom(auctionId(itemId, connection));
        Chat chat = chatManager.chatWith(jid);
        this.chatNotToBeGCd = chat;
        // Send a message to the recipient
        chat.send(JOIN_COMMAND_FORMAT);
    }

    private void disconnectWhenUICloses(XMPPTCPConnection connection) {
        ui.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent windowEvent) {
                connection.disconnect();
            }
        });
    }

    private static XMPPTCPConnection conection(String hostname, String username, String password)
            throws Exception {
        InetAddress hostAddress = InetAddress.getByName(hostname);
        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                .setHostAddress(hostAddress)
                .setXmppDomain(hostname)
                .setUsernameAndPassword(username, password)
                .setSecurityMode(XMPPTCPConnectionConfiguration.SecurityMode.disabled) // Disable security for local
                // development
                .build();
        var connection = new XMPPTCPConnection(config);
        connection.connect();
        connection.login();
        return connection;
    }

    private void startUserInterface() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            ui = new MainWindow();
        });
    }

    private static String auctionId(String itemId, XMPPTCPConnection connection) {
        return String.format(AUCTION_ID_FORMAT, itemId, connection.getXMPPServiceDomain().toString());
    }

    @Override
    public void auctionClosed() {
        SwingUtilities.invokeLater(() -> {
            ui.showStatus(MainWindow.STATUS_LOST);
        });
    }

    @Override
    public void currentPrice(int i, int j) {
    }
}
