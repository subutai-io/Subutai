package org.safehaus.subutai.core.peer.api;


import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManagerFactory;

import org.safehaus.subutai.common.peer.Peer;
import org.safehaus.subutai.common.peer.PeerException;
import org.safehaus.subutai.common.peer.PeerInfo;


public interface PeerManager
{
    /**
     * Registers remote peer
     */
    boolean register( PeerInfo peerInfo ) throws PeerException;

    /**
     * Updates peer metadata
     */
    boolean update( PeerInfo peerInfo );

    /**
     * Returns all registered peers metadata objects
     */
    public List<PeerInfo> getPeerInfos();

    /**
     * Returns local peer's metadata
     */
    public PeerInfo getLocalPeerInfo();

    /**
     * Returns peer metadata by peer id
     */
    public PeerInfo getPeerInfo( UUID uuid );

    /**
     * Unregisters peer
     */
    boolean unregister( String uuid ) throws PeerException;

    /**
     * Returns peer instance by peer id
     */
    public Peer getPeer( UUID peerId );

    /**
     * Returns peer instance by peer id
     */
    public Peer getPeer( String peerId );

    /**
     * Returns all peer instances
     */
    public List<Peer> getPeers();

    /**
     * Returns local peer instance
     */
    public LocalPeer getLocalPeer();

    public void addRequestListener( RequestListener listener );

    public void removeRequestListener( RequestListener listener );

    public EntityManagerFactory getEntityManagerFactory();
}
