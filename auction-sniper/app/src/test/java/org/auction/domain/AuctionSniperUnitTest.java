package org.auction.domain;

import org.auction.domain.AuctionEventListener.PriceSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuctionSniperUnitTest {
    private final SniperListener sniperListener = new SniperListenerSpy();
    private final Auction auction = new AuctionSpy();
    private final AuctionSniper sniper = new AuctionSniper(sniperListener, auction);

    @Test
    public void reportsLostWhenAuctionClosesImmediately() {
        sniper.auctionClosed();
        assertTrue(((SniperListenerSpy) sniperListener).sniperLostHasBeenCalledAtLeastOnce());
    }

    @Test
    public void reportsLostIfAuctionClosesWhenBidding() {
        sniper.currentPrice(1000, 98, PriceSource.FromOtherBidder);
        sniper.auctionClosed();
        assertTrue(((SniperListenerSpy) sniperListener).sniperLostHasBeenCalledAtLeastOnceWithState("bidding"));
    }

    @Test
    public void reportsWonIfAuctionClosesWhenWinning() {
        sniper.currentPrice(1000, 98, PriceSource.FromSniper);
        sniper.auctionClosed();
        assertTrue(((SniperListenerSpy) sniperListener).sniperWonHasBeenCalledAtLeastOnceWithState("winning"));
    }

    @Test
    public void bidsHigherAndReportsBiddingWhenNewPriceArrives() {
        final int price = 1000;
        final int increment = 98;
        sniper.currentPrice(1000, 98, PriceSource.FromOtherBidder);
        assertTrue(((SniperListenerSpy) sniperListener).sniperBiddingHasBeenCalledAtLeastOnce());
        assertTrue(((AuctionSpy) auction).bidHasBeenCalledOnceWith(price + increment));
    }

    @Test
    public void reportsIsWinningWhenCurrentPriceComesFromSniper() {
        sniper.currentPrice(123, 45, PriceSource.FromSniper);
        assertTrue(((SniperListenerSpy) sniperListener).sniperWinningHasBeenCalledAtLeastOnce());
    }

}

class SniperListenerSpy implements SniperListener {
    private int sniperLostCallCount = 0;
    private int sniperWonCallCount = 0;
    private int sniperBiddingCallCount = 0;
    private int sniperWinningCallCount = 0;
    private String state = null;

    @Override
    public void sniperLost() {
        sniperLostCallCount++;
    }

    @Override
    public void sniperWon() {
        sniperWonCallCount++;
    }

    @Override
    public void sniperBidding() {
        sniperBiddingCallCount++;
        state = "bidding";
    }

    @Override
    public void sniperWinning() {
        sniperWinningCallCount++;
        state = "winning";
    }

    public boolean sniperLostHasBeenCalledAtLeastOnce() {
        return sniperLostCallCount >= 1;
    }

    public boolean sniperLostHasBeenCalledAtLeastOnceWithState(String expectedState) {
        return sniperLostCallCount >= 1 && state.equals(expectedState);
    }

    public boolean sniperBiddingHasBeenCalledAtLeastOnce() {
        return sniperBiddingCallCount >= 1;
    }

    public boolean sniperWinningHasBeenCalledAtLeastOnce() {
        return sniperWinningCallCount >= 1;
    }

    public boolean sniperWonHasBeenCalledAtLeastOnceWithState(String expectedState) {
        return sniperWonCallCount >= 1 && state.equals(expectedState);
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

    @Override
    public void join() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'join'");
    }

}
