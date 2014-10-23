package org.safehaus.subutai.core.environment.ui;


import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.core.environment.ui.wizard.Node2PeerWizard;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static org.mockito.Mockito.when;


/**
 * Created by bahadyr on 9/29/14.
 */
@RunWith( MockitoJUnitRunner.class )
public class EnvironmentContainerNode2PeerWizardTest
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private Node2PeerWizard sut;
    @Mock
    private EnvironmentManagerPortalModule module;
    @Mock
    private PeerManager peerManager;


    @Before
    public void setUp() throws Exception
    {
        EnvironmentBuildTask task = getTask();
        when( module.getPeerManager() ).thenReturn( peerManager );
        when( peerManager.peers() ).thenReturn( Collections.<Peer>emptyList() );
        sut = new Node2PeerWizard( "Wizard", module, task );
    }


    private EnvironmentBuildTask getTask()
    {
        EnvironmentBuildTask task = new EnvironmentBuildTask();
        EnvironmentBlueprint eb = new EnvironmentBlueprint();
        eb.setName( "blueprint" );

        NodeGroup one = genNodeGroup( "hadoop", 5, "intra.lan", "name", true, true, PlacementStrategy.BEST_SERVER );
        NodeGroup two = genNodeGroup( "cassandra", 2, "intra.lan", "name", true, true, PlacementStrategy.BEST_SERVER );
        eb.addNodeGroup( one );
        eb.addNodeGroup( two );

        task.setEnvironmentBlueprint( eb );
        return task;
    }


    private NodeGroup genNodeGroup( String templateName, int non, String domainName, String name, boolean ek,
                                    boolean lh, PlacementStrategy ps )
    {
        NodeGroup ng = new NodeGroup();
        ng.setTemplateName( templateName );
        ng.setNumberOfNodes( non );
        ng.setDomainName( domainName );
        ng.setName( name );
        ng.setExchangeSshKeys( ek );
        ng.setLinkHosts( lh );
        ng.setPlacementStrategy( ps );
        return ng;
    }


    @Test
    public void testName() throws Exception
    {


    }
}
