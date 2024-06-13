/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.auction;

import com.objogate.wl.swing.AWTEventQueueProber;
import com.objogate.wl.swing.driver.JFrameDriver;
import com.objogate.wl.swing.driver.JLabelDriver;
import com.objogate.wl.swing.gesture.GesturePerformer;
import org.auction.ui.MainWindow;

import static org.hamcrest.Matchers.equalTo;

class ApplicationRunner {

    public static final String SNIPER_ID = "sniper";
    public static final String SNIPER_PASSWORD = "sniper";
    public static final String XMPP_HOSTNAME = "localhost"; // this needs to be env variable ?
    public static final String AUCTION_RESOURCE = "Auction"; // this needs to be env variable ?
    public static final String SNIPER_XMPP_ID = SNIPER_ID + "@" + XMPP_HOSTNAME + "/" + AUCTION_RESOURCE;
    ;
    private AuctionSniperDriver driver;

    public void startBiddingIn(final FakeAuctionServer auction) {
        Thread thread = new Thread("Test Application") {
            @Override
            public void run() {
                try {
                    Main.main(XMPP_HOSTNAME, SNIPER_ID, SNIPER_PASSWORD, auction.getItemId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.setDaemon(true);
        thread.start();
        driver = new AuctionSniperDriver(1000);
        driver.showsSniperStatus("Joining");
    }

    public void showsSniperHasLostAuction() {
        driver.showsSniperStatus("Lost");
    }

    public void hasShownSniperIsBidding() {
        driver.showsSniperStatus(MainWindow.STATUS_BIDDING);
    }

    public void stop() {
        if (driver != null) {
            driver.dispose();
        }
    }

    public void hasShownSniperIsWinning() {
        driver.showsSniperStatus(MainWindow.STATUS_WINNING);
    }

    public void showsSniperHasWonAuction() {
        driver.showsSniperStatus(MainWindow.STATUS_WON);
    }

}

class AuctionSniperDriver extends JFrameDriver {

    @SuppressWarnings("unchecked")
    protected AuctionSniperDriver(int timeoutMillis) {
        super(
                new GesturePerformer(),
                JFrameDriver.topLevelFrame(named(MainWindow.MAIN_WINDOW_NAME), showingOnScreen()),
                new AWTEventQueueProber(timeoutMillis, 100));
    }

    @SuppressWarnings("unchecked")
    protected void showsSniperStatus(String statusText) {
        new JLabelDriver(this, named(MainWindow.SNIPER_STATUS_NAME)).hasText(equalTo(statusText));
    }
}
