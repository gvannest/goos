package org.auction.xmpp;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class AuctionSniperUnitTest {
    private final SniperListener sniperListener = new SniperListenerSpy();
    private final Auction auction = new AuctionSpy();
    private final AuctionSniper sniper = new AuctionSniper(sniperListener, auction);

    @Test
    public void reportsLostWhenAuctionCloses() {
        sniper.auctionClosed();
        assertTrue(((SniperListenerSpy) sniperListener).sniperLostHasBeenCalledAtLeastOnce());
    }

    @Test
    public void bidsHigherAndReportsBiddingWhenNewPriceArrives() {
        sniper.currentPrice(1000, 98);
        assertTrue(((SniperListenerSpy) sniperListener).sniperBiddingHasBeenCalledAtLeastOnce());
        assertTrue(((AuctionSpy) auction).bidHasBeenCalledOnceWith(1098));
    }

}

class SniperListenerSpy implements SniperListener {
    private int sniperLostCallCount = 0;
    private int sniperBiddingCallCount = 0;

    @Override
    public void sniperLost() {
        sniperLostCallCount++;
    }

    @Override
    public void sniperBidding() {
        sniperBiddingCallCount++;
    }

    public boolean sniperLostHasBeenCalledAtLeastOnce() {
        return sniperLostCallCount >= 1;
    }

    public boolean sniperBiddingHasBeenCalledAtLeastOnce() {
        return sniperBiddingCallCount >= 1;
    }

}

class AuctionSpy implements Auction {
    private int bidPrice;
    private int bidCallCount = 0;

    public boolean bidHasBeenCalledOnceWith(int bidPrice) {
        return this.bidPrice == bidPrice && bidCallCount == 1;
    }

    @Override
    public void bid(int biddingPrice) {
        bidCallCount++;
        this.bidPrice = biddingPrice;
    }

}
