package org.safehaus.subutai.plugin.mongodb.impl;


import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.safehaus.subutai.common.exception.ClusterConfigurationException;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandCallback;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentContainer;
import org.safehaus.subutai.plugin.mongodb.api.MongoClusterConfig;
import org.safehaus.subutai.plugin.mongodb.api.NodeType;
import org.safehaus.subutai.plugin.mongodb.impl.common.CommandType;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;


/**
 * This is a mongodb cluster setup strategy.
 */
public class MongoDbSetupStrategy implements ClusterSetupStrategy
{

    private MongoImpl mongoManager;
    private ProductOperation po;
    private MongoClusterConfig config;
    private Environment environment;


    public MongoDbSetupStrategy( Environment environment, MongoClusterConfig config, ProductOperation po,
                                 MongoImpl mongoManager )
    {

        Preconditions.checkNotNull( environment, "Environment is null" );
        Preconditions.checkNotNull( config, "Cluster config is null" );
        Preconditions.checkNotNull( po, "Product operation tracker is null" );
        Preconditions.checkNotNull( mongoManager, "Mongo manager is null" );

        this.environment = environment;
        this.mongoManager = mongoManager;
        this.po = po;
        this.config = config;
    }


    public static PlacementStrategy getNodePlacementStrategyByNodeType( NodeType nodeType )
    {
        switch ( nodeType )
        {
            case CONFIG_NODE:
                return PlacementStrategy.MORE_RAM;
            case ROUTER_NODE:
                return PlacementStrategy.MORE_CPU;
            case DATA_NODE:
                return PlacementStrategy.MORE_HDD;
            default:
                return PlacementStrategy.ROUND_ROBIN;
        }
    }


    @Override
    public MongoClusterConfig setup() throws ClusterSetupException
    {

        if ( Strings.isNullOrEmpty( config.getClusterName() ) ||
                Strings.isNullOrEmpty( config.getDomainName() ) ||
                Strings.isNullOrEmpty( config.getReplicaSetName() ) ||
                Strings.isNullOrEmpty( config.getTemplateName() ) ||
                !Sets.newHashSet( 1, 3 ).contains( config.getNumberOfConfigServers() ) ||
                !Range.closed( 1, 3 ).contains( config.getNumberOfRouters() ) ||
                !Sets.newHashSet( 3, 5, 7 ).contains( config.getNumberOfDataNodes() ) ||
                !Range.closed( 1024, 65535 ).contains( config.getCfgSrvPort() ) ||
                !Range.closed( 1024, 65535 ).contains( config.getRouterPort() ) ||
                !Range.closed( 1024, 65535 ).contains( config.getDataNodePort() ) )
        {
            throw new ClusterSetupException( "Malformed cluster configuration" );
        }

        if ( mongoManager.getCluster( config.getClusterName() ) != null )
        {
            throw new ClusterSetupException(
                    String.format( "Cluster with name '%s' already exists", config.getClusterName() ) );
        }

        if ( environment.getEnvironmentContainerNodes().isEmpty() )
        {
            throw new ClusterSetupException( "Environment has no nodes" );
        }

        int totalNodesRequired =
                config.getNumberOfRouters() + config.getNumberOfConfigServers() + config.getNumberOfDataNodes();
        if ( environment.getEnvironmentContainerNodes().size() < totalNodesRequired )
        {
            throw new ClusterSetupException(
                    String.format( "Environment needs to have %d but has %d nodes", totalNodesRequired,
                            environment.getEnvironmentContainerNodes().size() ) );
        }

        Set<Agent> mongoAgents = new HashSet<>();
        Set<EnvironmentContainer> mongoEnvironmentContainers = new HashSet<>();
        for ( EnvironmentContainer environmentContainer : environment.getEnvironmentContainerNodes() )
        {
            if ( environmentContainer.getTemplate().getProducts().contains( Common.PACKAGE_PREFIX + MongoClusterConfig.PRODUCT_NAME ) )
            {
                mongoAgents.add( environmentContainer.getAgent() );
                mongoEnvironmentContainers.add( environmentContainer );
            }
        }

        if ( mongoAgents.size() < totalNodesRequired )
        {
            throw new ClusterSetupException( String.format(
                    "Environment needs to have %d with MongoDb installed but has only %d nodes with MongoDb installed",
                    totalNodesRequired, mongoAgents.size() ) );
        }

        Set<Agent> configServers = new HashSet<>();
        Set<Agent> routers = new HashSet<>();
        Set<Agent> dataNodes = new HashSet<>();
        for ( EnvironmentContainer environmentContainer : mongoEnvironmentContainers )
        {
            if ( NodeType.CONFIG_NODE.name().equalsIgnoreCase( environmentContainer.getNodeGroupName() ) )
            {
                configServers.add( environmentContainer.getAgent() );
            }
            else if ( NodeType.ROUTER_NODE.name().equalsIgnoreCase( environmentContainer.getNodeGroupName() ) )
            {
                routers.add( environmentContainer.getAgent() );
            }
            else if ( NodeType.DATA_NODE.name().equalsIgnoreCase( environmentContainer.getNodeGroupName() ) )
            {
                dataNodes.add( environmentContainer.getAgent() );
            }
        }

        mongoAgents.removeAll( configServers );
        mongoAgents.removeAll( routers );
        mongoAgents.removeAll( dataNodes );

        if ( configServers.size() < config.getNumberOfConfigServers() )
        {
            //take necessary number of nodes at random
            int numNeededMore = config.getNumberOfConfigServers() - configServers.size();
            Iterator<Agent> it = mongoAgents.iterator();
            for ( int i = 0; i < numNeededMore; i++ )
            {
                configServers.add( it.next() );
                it.remove();
            }
        }

        if ( routers.size() < config.getNumberOfRouters() )
        {
            //take necessary number of nodes at random
            int numNeededMore = config.getNumberOfRouters() - routers.size();
            Iterator<Agent> it = mongoAgents.iterator();
            for ( int i = 0; i < numNeededMore; i++ )
            {
                routers.add( it.next() );
                it.remove();
            }
        }

        if ( dataNodes.size() < config.getNumberOfDataNodes() )
        {
            //take necessary number of nodes at random
            int numNeededMore = config.getNumberOfDataNodes() - dataNodes.size();
            Iterator<Agent> it = mongoAgents.iterator();
            for ( int i = 0; i < numNeededMore; i++ )
            {
                dataNodes.add( it.next() );
                it.remove();
            }
        }

        config.setConfigServers( configServers );
        config.setRouterServers( routers );
        config.setDataNodes( dataNodes );


        try
        {
            configureMongoCluster();
        }
        catch ( ClusterConfigurationException e )
        {
            throw new ClusterSetupException( e.getMessage() );
        }

        po.addLog( "Saving cluster information to database..." );

        mongoManager.getPluginDAO().saveInfo( MongoClusterConfig.PRODUCT_KEY, config.getClusterName(), config );
        po.addLog( "Cluster information saved to database" );


        return config;
    }


    private void configureMongoCluster() throws ClusterConfigurationException
    {

        po.addLog( "Configuring cluster..." );

        List<Command> installationCommands = mongoManager.getCommands().getInstallationCommands( config );

        for ( Command command : installationCommands )
        {
            po.addLog( String.format( "Running command: %s", command.getDescription() ) );
            final AtomicBoolean commandOK = new AtomicBoolean();

            if ( command.getData() == CommandType.START_CONFIG_SERVERS || command.getData() == CommandType.START_ROUTERS
                    || command.getData() == CommandType.START_DATA_NODES )
            {
                mongoManager.getCommandRunner().runCommand( command, new CommandCallback()
                {

                    @Override
                    public void onResponse( Response response, AgentResult agentResult, Command command )
                    {

                        int count = 0;
                        for ( AgentResult result : command.getResults().values() )
                        {
                            if ( result.getStdOut().contains( "child process started successfully, parent exiting" ) )
                            {
                                count++;
                            }
                        }
                        if ( command.getData() == CommandType.START_CONFIG_SERVERS )
                        {
                            if ( count == config.getConfigServers().size() )
                            {
                                commandOK.set( true );
                            }
                        }
                        else if ( command.getData() == CommandType.START_ROUTERS )
                        {
                            if ( count == config.getRouterServers().size() )
                            {
                                commandOK.set( true );
                            }
                        }
                        else if ( command.getData() == CommandType.START_DATA_NODES )
                        {
                            if ( count == config.getDataNodes().size() )
                            {
                                commandOK.set( true );
                            }
                        }
                        if ( commandOK.get() )
                        {
                            stop();
                        }
                    }
                } );
            }
            else
            {
                mongoManager.getCommandRunner().runCommand( command );
            }

            if ( command.hasSucceeded() || commandOK.get() )
            {
                po.addLog( String.format( "Command %s succeeded", command.getDescription() ) );
            }
            else
            {
                throw new ClusterConfigurationException(
                        String.format( "Command %s failed: %s", command.getDescription(), command.getAllErrors() ) );
            }
        }
    }
}
