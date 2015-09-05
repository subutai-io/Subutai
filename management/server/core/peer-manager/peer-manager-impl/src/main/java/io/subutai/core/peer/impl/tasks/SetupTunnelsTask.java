package io.subutai.core.peer.impl.tasks;


import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.net.util.SubnetUtils;

import io.subutai.common.host.Interface;
import io.subutai.common.network.Vni;
import io.subutai.common.peer.PeerException;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.network.api.Tunnel;
import io.subutai.core.peer.impl.entity.ManagementHostEntity;


public class SetupTunnelsTask implements Callable<Integer>
{
    private static final Logger LOG = LoggerFactory.getLogger( SetupTunnelsTask.class );
    private final NetworkManager networkManager;
    private final ManagementHostEntity managementHost;
    private final UUID environmentId;
    private final Map<String, String> peerIps;


    public SetupTunnelsTask( final NetworkManager networkManager, final ManagementHostEntity managementHost,
                             final UUID environmentId, final Map<String, String> peerIps )
    {
        this.networkManager = networkManager;
        this.managementHost = managementHost;
        this.environmentId = environmentId;
        this.peerIps = peerIps;
    }


    @Override
    public Integer call() throws Exception
    {
        //fail if vni is not reserved
        Vni environmentVni = managementHost.findVniByEnvironmentId( environmentId );

        if ( environmentVni == null )
        {
            throw new PeerException(
                    String.format( "Error setting up tunnels: No reserved vni found for environment %s",
                            environmentId ) );
        }

        //setup tunnels to each remote peer
        Set<Tunnel> tunnels = networkManager.listTunnels();

        //remove local IP, just in case
        peerIps.remove( managementHost.getExternalIp() );


        for ( String peerIp : peerIps.keySet() )
        {
            LOG.debug( String.format( "Setting up tunnel on : %s", peerIp ) );
            String tunnelIp = peerIps.get( peerIp );
            int tunnelId = managementHost.findTunnel( tunnelIp, tunnels );
            //tunnel not found, create new one
            if ( tunnelId == -1 )
            {
                //calculate tunnel id
                tunnelId = managementHost.calculateNextTunnelId( tunnels );


                LOG.debug( String.format( "Setting up tunnel: %s %s", tunnelId, tunnelIp ) );
                //create tunnel
                networkManager.setupTunnel( tunnelId, tunnelIp/*peerIp */ );
            }

            //create vni-vlan mapping
            LOG.debug( String.format( "Setting up tunnel for %s: %s", peerIp, environmentVni ) );
            managementHost.setupVniVlanMapping( tunnelId, environmentVni.getVni(), environmentVni.getVlan(),
                    environmentVni.getEnvironmentId() );
        }

        return environmentVni.getVlan();
    }
}