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
        auction.hasReceivedJoinRequestFromSniper(ApplicationRunner.SNIPER_XMPP_ID); // Step 3
        auction.announceClosed(); // Step 4
        application.showsSniperHasLostAuction(); // Step 5
    }

    @Test
    void sniperMakesAHigherBidButLoses() {
        auction.startSellingItem(); // Step 1
        application.startBiddingIn(auction); // Step 2
        auction.hasReceivedJoinRequestFromSniper(ApplicationRunner.SNIPER_XMPP_ID); // Step 3

        auction.reportPrice(1000, 98, "other bidder"); // Step 4
        application.hasShownSniperIsBidding(); // Step 5

        auction.hasReceivedBid(1098, ApplicationRunner.SNIPER_XMPP_ID); // Step 6

        auction.announceClosed(); // Step 4
        application.showsSniperHasLostAuction(); // Step 5
    }

    @Test
    void sniperWinsAnAuctionByBiddingHigher() {
        auction.startSellingItem(); // Step 1
        application.startBiddingIn(auction); // Step 2
        auction.hasReceivedJoinRequestFromSniper(ApplicationRunner.SNIPER_XMPP_ID); // Step 3

        auction.reportPrice(1000, 98, "other bidder"); // Step 4
        application.hasShownSniperIsBidding(); // Step 5

        auction.hasReceivedBid(1098, ApplicationRunner.SNIPER_XMPP_ID); // Step 6
        auction.reportPrice(1098, 97, ApplicationRunner.SNIPER_XMPP_ID);// Step 4
        application.hasShownSniperIsWinning();

        auction.announceClosed(); // Step 4
        application.showsSniperHasWonAuction(); // Step 5
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
