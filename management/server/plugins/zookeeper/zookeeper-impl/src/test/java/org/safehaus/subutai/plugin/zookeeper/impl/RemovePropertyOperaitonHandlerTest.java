package org.safehaus.subutai.plugin.zookeeper.impl;


import org.junit.Test;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.OperationState;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.container.api.container.ContainerManager;
import org.safehaus.subutai.plugin.common.mock.TrackerMock;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.zookeeper.impl.handler.RemovePropertyOperationHandler;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class RemovePropertyOperaitonHandlerTest
{
    @Test
    public void testWithoutCluster()
    {
        ZookeeperImpl zookeeperMock = mock( ZookeeperImpl.class );
        when( zookeeperMock.getHadoopManager() ).thenReturn( mock( Hadoop.class ) );
        when( zookeeperMock.getAgentManager() ).thenReturn( mock( AgentManager.class ) );
        when( zookeeperMock.getCommandRunner() ).thenReturn( mock( CommandRunner.class ) );
        when( zookeeperMock.getTracker() ).thenReturn( new TrackerMock() );
        when( zookeeperMock.getContainerManager() ).thenReturn( mock( ContainerManager.class ) );
        when( zookeeperMock.getHadoopManager() ).thenReturn( mock( Hadoop.class ) );
        when( zookeeperMock.getCluster( anyString() ) ).thenReturn( null );
        AbstractOperationHandler operationHandler =
                new RemovePropertyOperationHandler( zookeeperMock, "test-cluster", "test-file", "test-property" );
        operationHandler.run();

        assertTrue( operationHandler.getTrackerOperation().getLog().contains( "not exist" ) );
        assertEquals( operationHandler.getTrackerOperation().getState(), OperationState.FAILED );
    }
}
