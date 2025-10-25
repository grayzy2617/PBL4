package Sever;

/**
 * Listener interface used by server/session to notify the UI about
 * auction lifecycle events and runtime updates.
 */
public interface AuctionResultListener {
    // update summary numbers (revenue/resources)
    void Update();

    // auction started: session id and duration (seconds)
    void onAuctionStarted(String sessionId, int duration);

    // auction ended: winner id (may be null), winning bid
    void onAuctionEnded(String winnerId, double winningBid);

    // a node has connected to server (so its dot can light up)
    void onNodeConnected(String nodeId);

    // a bid was received from node for a link within a session
    void onBidForLink(String nodeId, String linkId, String sessionId);
}