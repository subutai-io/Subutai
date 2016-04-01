package io.subutai.core.localpeer.impl.tasks;


import java.util.Random;
import java.util.concurrent.Callable;

import io.subutai.common.host.NullHostInterface;
import io.subutai.common.network.NetworkResource;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.peer.ResourceHostException;
import io.subutai.common.protocol.P2pIps;
import io.subutai.common.protocol.Tunnel;
import io.subutai.common.protocol.Tunnels;
import io.subutai.core.network.api.NetworkManager;


public class SetupTunnelsTask implements Callable<Boolean>
{
    private final NetworkManager networkManager;
    private final ResourceHost resourceHost;
    private final P2pIps p2pIps;
    private final NetworkResource networkResource;


    public SetupTunnelsTask( final NetworkManager networkManager, final ResourceHost resourceHost, final P2pIps p2pIps,
                             NetworkResource networkResource )
    {
        this.networkManager = networkManager;
        this.resourceHost = resourceHost;
        this.p2pIps = p2pIps;
        this.networkResource = networkResource;
    }


    @Override
    public Boolean call() throws Exception
    {

        Tunnels tunnels = networkManager.getTunnels( resourceHost );


        //setup tunnel to each local and remote RH
        for ( String tunnelIp : p2pIps.getP2pIps() )
        {
            //skip if own IP
            boolean ownIp = !( resourceHost.getHostInterfaces().findByIp( tunnelIp ) instanceof NullHostInterface );
            if ( ownIp )
            {
                continue;
            }

            //see if tunnel exists
            Tunnel tunnel = tunnels.findByIp( tunnelIp );

            //create new tunnel
            if ( tunnel == null )
            {
                String tunnelName = generateTunnelName( tunnels );

                if ( tunnelName == null )
                {
                    throw new ResourceHostException( "Free tunnel name not found" );
                }

                Tunnel newTunnel =
                        new Tunnel( tunnelName, tunnelIp, networkResource.getVlan(), networkResource.getVni() );

                networkManager.createTunnel( resourceHost, newTunnel.getTunnelName(), newTunnel.getTunnelIp(),
                        newTunnel.getVlan(), newTunnel.getVni() );

                //add to avoid duplication in the next iteration
                tunnels.addTunnel( newTunnel );
            }
        }

        return true;
    }


    protected String generateTunnelName( Tunnels tunnels )
    {
        int maxIterations = 10000;
        int currentIteration = 0;
        String name;

        Random rnd = new Random();

        do
        {
            int n = 10000 + rnd.nextInt( 90000 );
            name = String.format( "tunnel-%d", n );
            currentIteration++;
        }
        while ( tunnels.findByName( name ) != null && currentIteration < maxIterations );

        if ( tunnels.findByName( name ) != null )
        {
            return null;
        }

        return name;
    }
}
