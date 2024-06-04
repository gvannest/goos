/*
 * This source file was generated by the Gradle 'init' task
 */
package org.auction;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class AuctionSniperEndToEndTest {

    private final FakeAuctionServer auction = new FakeAuctionServer("item-54321");
    private final ApplicationRunner application = new ApplicationRunner();

    @Test
    void sniperJoinsAuctionUntilAuctionCloses() {
        auction.startSellingItem(); // Step 1
        application.startBiddingIn(auction); // Step 2
        auction.hasReceivedJoinRequestFromSniper(); // Step 3
        auction.announceClosed(); // Step 4
        application.showsSniperHasLostAuction(); // Step 5
    }

    @AfterEach
    public void stopAuction() {
        auction.stop();
    }

    @AfterEach
    public void stopApplication() {
        application.stop();
    }
}