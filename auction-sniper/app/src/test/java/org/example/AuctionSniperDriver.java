/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.example;

import com.objogate.wl.swing.AWTEventQueueProber;
import com.objogate.wl.swing.driver.JFrameDriver;
import com.objogate.wl.swing.driver.JLabelDriver;
import com.objogate.wl.swing.gesture.GesturePerformer;

class ApplicationRunner {

    public static final String SNIPER_ID = "sniper";
    public static final String SNIPER_PASSWORD = "sniper";
    public static final String XMPP_HOSTNAME = "localhost"; // this needs to be env variable ?
    private AuctionSniperDriver driver;

    public void startBiddingIn(final FakeAuctionServer auction) {
        Thread thread = new Thread("Test Application") {
            @Override
            public void run() {
                try {
                    AuctionSniper.main(XMPP_HOSTNAME, SNIPER_ID, SNIPER_PASSWORD, auction.getItemId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.setDaemon(true);
        thread.start();
        driver = new AuctionSniperDriver(1000);
        driver.showsSniperStatus(AuctionSniper.STATUS_JOINING);
    }

    public void showsSniperHasLostAuction() {
        driver.showsSniperStatus(AuctionSniper.STATUS_LOST);
    }

    public void stop() {
        if (driver != null) {
            driver.dispose();
        }
    }

}

class AuctionSniperDriver extends JFrameDriver {

    protected AuctionSniperDriver(int timeoutMillis) {
        super(
                new GesturePerformer(),
                JFrameDriver.topLevelFrame(named(MainWindow.MAIN_WINDOW_NAME), showingOnScreen()),
                new AWTEventQueueProber(timeoutMillis, 100));
    }

    protected void showsSniperStatus(String statusText) {
        new JLabelDriver(this, named(AuctionSniper.SNIPER_STATUS_NAME)).hasText(equalTo(statusText));
    }
}
