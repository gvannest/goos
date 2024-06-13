/*
 * This source file was generated by the Gradle 'init' task
 */
package org.auction;

import org.auction.domain.Auction;
import org.auction.domain.AuctionSniper;
import org.auction.domain.SniperListener;
import org.auction.ui.MainWindow;
import org.auction.xmpp.AuctionMessageTranslator;
import org.auction.xmpp.XMPPAuction;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetAddress;

public class Main {
    private static final int ARG_HOSTNAME = 0;
    private static final int ARG_USERNAME = 1;
    private static final int ARG_PASSWORD = 2;
    private static final int ARG_ITEM_ID = 3;

    private static final String ITEM_ID_AS_LOGIN = "auction-%s";
    private static final String RESOURCE = "Auction";
    private static final String AUCTION_ID_FORMAT = ITEM_ID_AS_LOGIN + "@%s/" + RESOURCE;
    public static final String JOIN_COMMAND_FORMAT = "SQLVersion: 1.1; Command: JOIN;";
    public static final String BID_COMMAND_FORMAT = "SQLVersion: 1.1; Command: BID; Price: %d;";

    MainWindow ui;
    private Chat chatNotToBeGCd;

    public Main() throws Exception {
        startUserInterface();
    }

    public static void main(String... args) throws Exception {
        Main main = new Main();
        main.joinAuction(connection(args[ARG_HOSTNAME], args[ARG_USERNAME], args[ARG_PASSWORD]), args[ARG_ITEM_ID]);

    }

    private void joinAuction(XMPPTCPConnection connection, String itemId)
            throws Exception {
        disconnectWhenUICloses(connection);
        ChatManager chatManager = ChatManager.getInstanceFor(connection);

        // Create a new chat session with the desired recipient
        EntityBareJid jid = JidCreate.entityBareFrom(auctionId(itemId, connection));
        Chat chat = chatManager.chatWith(jid);
        this.chatNotToBeGCd = chat;

        Auction auction = new XMPPAuction(chat);
        chatManager.addIncomingListener(
                new AuctionMessageTranslator(connection.getUser().toString(), new AuctionSniper(new SniperStateDisplayer(), auction)));

        // Send a message to the recipient
        auction.join();
    }

    private void disconnectWhenUICloses(XMPPTCPConnection connection) {
        ui.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent windowEvent) {
                connection.disconnect();
            }
        });
    }

    private static XMPPTCPConnection connection(String hostname, String username, String password)
            throws Exception {
        InetAddress hostAddress = InetAddress.getByName(hostname);
        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                .setHostAddress(hostAddress)
                .setXmppDomain(hostname)
                .setUsernameAndPassword(username, password)
                .setResource(RESOURCE)
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

    private class SniperStateDisplayer implements SniperListener {

        @Override
        public void sniperLost() {
            showStatus(MainWindow.STATUS_LOST);
        }

        @Override
        public void sniperWon() {
            showStatus(MainWindow.STATUS_WON);
        }

        @Override
        public void sniperBidding() {
            showStatus(MainWindow.STATUS_BIDDING);
        }

        @Override
        public void sniperWinning() {
            showStatus(MainWindow.STATUS_WINNING);
        }

        private void showStatus(final String status) {
            SwingUtilities.invokeLater(() -> {
                ui.showStatus(status);
            });
        }

    }

}
