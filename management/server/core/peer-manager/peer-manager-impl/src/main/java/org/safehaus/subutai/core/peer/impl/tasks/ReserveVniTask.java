package org.safehaus.subutai.core.peer.impl.tasks;


import java.util.concurrent.Callable;

import org.safehaus.subutai.common.network.Vni;
import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.peer.impl.entity.ManagementHostEntity;


public class ReserveVniTask implements Callable<Integer>
{
    private final NetworkManager networkManager;
    private final Vni vni;
    private final ManagementHostEntity managementHost;


    public ReserveVniTask( final NetworkManager networkManager, final Vni vni,
                           final ManagementHostEntity managementHost )
    {
        this.networkManager = networkManager;
        this.vni = vni;
        this.managementHost = managementHost;
    }


    @Override
    public Integer call() throws Exception
    {

        //check if vni is already reserved
        Vni existingVni = managementHost.findVniByEnvironmentId( vni.getEnvironmentId() );
        if ( existingVni != null )
        {
            return existingVni.getVlan();
        }

        //figure out available vlan
        int vlan = managementHost.findAvailableVlanId();

        //reserve vni & vlan for environment

        networkManager.reserveVni( new Vni( vni.getVni(), vlan, vni.getEnvironmentId() ) );

        return vlan;
    }
}