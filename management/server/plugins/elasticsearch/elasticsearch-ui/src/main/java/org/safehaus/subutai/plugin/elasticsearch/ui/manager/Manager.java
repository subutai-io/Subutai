package org.safehaus.subutai.plugin.elasticsearch.ui.manager;


import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.NamingException;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.elasticsearch.api.Elasticsearch;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;
import org.safehaus.subutai.server.ui.component.ProgressWindow;
import org.safehaus.subutai.server.ui.component.TerminalWindow;

import com.google.common.collect.Sets;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.Sizeable;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;


public class Manager
{

    private static final Pattern elasticsearchPattern = Pattern.compile( ".*(elasticsearch.+?g).*" );
    private final Table nodesTable;
    private final String message = "No cluster is installed !";
    private final Embedded progressIcon = new Embedded( "", new ThemeResource( "img/spinner.gif" ) );
    private final ExecutorService executorService;
    private final Tracker tracker;
    private final AgentManager agentManager;
    private final Elasticsearch elasticsearch;
    private final CommandRunner commandRunner;
    private GridLayout contentRoot;
    private ComboBox clusterCombo;
    private ElasticsearchClusterConfiguration config;


    public Manager( final ExecutorService executorService, ServiceLocator serviceLocator ) throws NamingException
    {

        this.elasticsearch = serviceLocator.getService( Elasticsearch.class );
        this.executorService = executorService;
        this.tracker = serviceLocator.getService( Tracker.class );
        this.agentManager = serviceLocator.getService( AgentManager.class );
        this.commandRunner = serviceLocator.getService( CommandRunner.class );


        contentRoot = new GridLayout();
        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );
        contentRoot.setSizeFull();
        contentRoot.setRows( 10 );
        contentRoot.setColumns( 1 );

        //tables go here
        nodesTable = createTableTemplate( "Cluster nodes" );

        HorizontalLayout controlsContent = new HorizontalLayout();
        controlsContent.setSpacing( true );
        controlsContent.setHeight( 100, Sizeable.Unit.PERCENTAGE );

        Label clusterNameLabel = new Label( "Select the cluster" );
        controlsContent.addComponent( clusterNameLabel );
        controlsContent.setComponentAlignment( clusterNameLabel, Alignment.MIDDLE_CENTER );


        /**  Combo box  */
        clusterCombo = new ComboBox();
        clusterCombo.setImmediate( true );
        clusterCombo.setTextInputAllowed( false );
        clusterCombo.setWidth( 200, Sizeable.Unit.PIXELS );
        clusterCombo.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                config = ( ElasticsearchClusterConfiguration ) event.getProperty().getValue();
                refreshUI();
                checkAllNodes();
            }
        } );

        controlsContent.addComponent( clusterCombo );
        controlsContent.setComponentAlignment( clusterCombo, Alignment.MIDDLE_CENTER );

        /**  Refresh clusters button */
        Button refreshClustersBtn = new Button( "Refresh clusters" );
        refreshClustersBtn.addStyleName( "default" );
        refreshClustersBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                refreshClustersInfo();
            }
        } );

        controlsContent.addComponent( refreshClustersBtn );
        controlsContent.setComponentAlignment( refreshClustersBtn, Alignment.MIDDLE_CENTER );


        /** Check all button */
        Button checkAllBtn = new Button( "Check all" );
        checkAllBtn.addStyleName( "default" );
        checkAllBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( config == null )
                {
                    show( message );
                }
                else
                {
                    checkAllNodes();
                }
            }
        } );

        controlsContent.addComponent( checkAllBtn );
        controlsContent.setComponentAlignment( checkAllBtn, Alignment.MIDDLE_CENTER );


        /**  Start all button */
        Button startAllBtn = new Button( "Start all" );
        startAllBtn.addStyleName( "default" );
        startAllBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( config == null )
                {
                    show( message );
                }
                else
                {
                    startAllNodes();
                }
            }
        } );

        controlsContent.addComponent( startAllBtn );
        controlsContent.setComponentAlignment( startAllBtn, Alignment.MIDDLE_CENTER );


        /**  Stop all button  */
        Button stopAllBtn = new Button( "Stop all" );
        stopAllBtn.addStyleName( "default" );
        stopAllBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( config == null )
                {
                    show( message );
                }
                else
                {
                    stopAllNodes();
                }
            }
        } );

        controlsContent.addComponent( stopAllBtn );
        controlsContent.setComponentAlignment( stopAllBtn, Alignment.MIDDLE_CENTER );


        /**  Destroy cluster button  */
        Button destroyClusterBtn = new Button( "Destroy cluster" );
        destroyClusterBtn.addStyleName( "default" );
        destroyClusterBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( config != null )
                {
                    ConfirmationDialog alert = new ConfirmationDialog(
                            String.format( "Do you want to destroy the %s cluster?", config.getClusterName() ), "Yes",
                            "No" );
                    alert.getOk().addClickListener( new Button.ClickListener()
                    {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent )
                        {

                            UUID trackID = elasticsearch.uninstallCluster( config.getClusterName() );

                            ProgressWindow window = new ProgressWindow( executorService, tracker, trackID,
                                    ElasticsearchClusterConfiguration.PRODUCT_KEY );

                            window.getWindow().addCloseListener( new Window.CloseListener()
                            {
                                @Override
                                public void windowClose( Window.CloseEvent closeEvent )
                                {
                                    refreshClustersInfo();
                                }
                            } );
                            contentRoot.getUI().addWindow( window.getWindow() );
                        }
                    } );

                    contentRoot.getUI().addWindow( alert.getAlert() );
                }
                else
                {
                    show( "Please, select cluster" );
                }
            }
        } );

        controlsContent.addComponent( destroyClusterBtn );
        controlsContent.setComponentAlignment( destroyClusterBtn, Alignment.MIDDLE_CENTER );

        controlsContent.addComponent( progressIcon );
        contentRoot.addComponent( controlsContent, 0, 0 );
        contentRoot.addComponent( nodesTable, 0, 1, 0, 9 );
    }


    /**
     * Clicks all "Check" buttons on table in which on nodes are listed. "Check" button is made hidden deliberately on
     * this table.
     */
    public void checkAllNodes()
    {
        for ( Object o : nodesTable.getItemIds() )
        {
            int rowId = ( Integer ) o;
            Item row = nodesTable.getItem( rowId );
            Button checkBtn = ( Button ) ( row.getItemProperty( "Check" ).getValue() );
            checkBtn.addStyleName( "default" );
            checkBtn.click();
        }
    }


    public void stopAllNodes()
    {
        for ( Object o : nodesTable.getItemIds() )
        {
            int rowId = ( Integer ) o;
            Item row = nodesTable.getItem( rowId );
            Button checkBtn = ( Button ) ( row.getItemProperty( "Stop" ).getValue() );
            checkBtn.click();
        }
    }


    public void startAllNodes()
    {
        for ( Object o : nodesTable.getItemIds() )
        {
            int rowId = ( Integer ) o;
            Item row = nodesTable.getItem( rowId );
            Button checkBtn = ( Button ) ( row.getItemProperty( "Start" ).getValue() );
            checkBtn.click();
        }
    }


    private Table createTableTemplate( String caption )
    {
        final Table table = new Table( caption );
        table.addContainerProperty( "Host", String.class, null );
        table.addContainerProperty( "IP", String.class, null );
        table.addContainerProperty( "Master/Data", String.class, null );
        table.addContainerProperty( "Check", Button.class, null );
        table.addContainerProperty( "Start", Button.class, null );
        table.addContainerProperty( "Stop", Button.class, null );
        table.addContainerProperty( "Service Status", Label.class, null );

        table.setSizeFull();
        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setImmediate( true );
        table.setColumnCollapsingAllowed( true );
        table.setColumnCollapsed( "Check", true );

        table.addItemClickListener( new ItemClickEvent.ItemClickListener()
        {
            @Override
            public void itemClick( ItemClickEvent event )
            {
                if ( event.isDoubleClick() )
                {
                    String lxcHostname =
                            ( String ) table.getItem( event.getItemId() ).getItemProperty( "Host" ).getValue();
                    Agent lxcAgent = agentManager.getAgentByHostname( lxcHostname );
                    if ( lxcAgent != null )
                    {
                        TerminalWindow terminal =
                                new TerminalWindow( Sets.newHashSet( lxcAgent ), executorService, commandRunner,
                                        agentManager );
                        contentRoot.getUI().addWindow( terminal.getWindow() );
                    }
                    else
                    {
                        show( "Agent is not connected" );
                    }
                }
            }
        } );
        return table;
    }


    private void show( String notification )
    {
        Notification.show( notification );
    }


    private void refreshUI()
    {
        if ( config != null )
        {
            populateTable( nodesTable, config.getNodes() );
        }
        else
        {
            nodesTable.removeAllItems();
        }
    }


    /**
     * Fill out the table in which all nodes in the cluster are listed.
     *
     * @param table table to be filled
     * @param agents nodes
     */
    private void populateTable( final Table table, Set<Agent> agents )
    {
        table.removeAllItems();
        for ( final Agent agent : agents )
        {
            final Label resultHolder = new Label();
            final Button checkButton = new Button( "Check" );
            checkButton.addStyleName( "default" );
            checkButton.setVisible( true );

            final Button startButton = new Button( "Start" );
            startButton.addStyleName( "default" );
            startButton.setVisible( true );

            final Button stopButton = new Button( "Stop" );
            stopButton.addStyleName( "default" );
            stopButton.setVisible( true );

            startButton.setEnabled( false );
            stopButton.setEnabled( false );
            progressIcon.setVisible( false );

            String isMaster = checkIfMaster( agent );

            final Object rowId = table.addItem( new Object[] {
                    agent.getHostname(), parseIPList( agent.getListIP().toString() ), isMaster, checkButton,
                    startButton, stopButton, resultHolder
            }, null );

            checkButton.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( Button.ClickEvent event )
                {
                    progressIcon.setVisible( true );
                    executorService.execute(
                            new CheckTask( elasticsearch, tracker, config.getClusterName(), agent.getHostname(),
                                    new CompleteEvent()
                                    {
                                        public void onComplete( String result )
                                        {
                                            synchronized ( progressIcon )
                                            {
                                                String status = parseServiceResult( result );
                                                resultHolder.setValue( status );
                                                if ( status.contains( "not" ) )
                                                {
                                                    startButton.setEnabled( true );
                                                    stopButton.setEnabled( false );
                                                }
                                                else
                                                {
                                                    startButton.setEnabled( false );
                                                    stopButton.setEnabled( true );
                                                }
                                                progressIcon.setVisible( false );
                                            }
                                        }
                                    } ) );
                }
            } );

            startButton.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( Button.ClickEvent clickEvent )
                {
                    progressIcon.setVisible( true );
                    startButton.setEnabled( false );
                    stopButton.setEnabled( false );
                    executorService.execute(
                            new StartTask( elasticsearch, tracker, config.getClusterName(), agent.getHostname(),
                                    new CompleteEvent()
                                    {
                                        @Override
                                        public void onComplete( String result )
                                        {
                                            synchronized ( progressIcon )
                                            {
                                                checkButton.click();
                                            }
                                        }
                                    } ) );
                }
            } );

            stopButton.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( Button.ClickEvent clickEvent )
                {
                    progressIcon.setVisible( true );
                    startButton.setEnabled( false );
                    stopButton.setEnabled( false );
                    executorService.execute(
                            new StopTask( elasticsearch, tracker, config.getClusterName(), agent.getHostname(),
                                    new CompleteEvent()
                                    {
                                        @Override
                                        public void onComplete( String result )
                                        {
                                            synchronized ( progressIcon )
                                            {
                                                checkButton.click();
                                            }
                                        }
                                    } ) );
                }
            } );
        }
    }


    /**
     * Parses output of 'service cassandra status' command
     */
    public static String parseServiceResult( String result )
    {
        StringBuilder parsedResult = new StringBuilder();
        Matcher tracersMatcher = elasticsearchPattern.matcher( result );
        if ( tracersMatcher.find() )
        {
            parsedResult.append( tracersMatcher.group( 1 ) ).append( " " );
        }

        return parsedResult.toString();
    }


    /**
     * Parses supplied string argument to extract external IP.
     *
     * @param ipList ex: [10.10.10.10, 127.0.0.1]
     *
     * @return 10.10.10.10
     */
    public String parseIPList( String ipList )
    {
        return ipList.substring( ipList.indexOf( "[" ) + 1, ipList.indexOf( "," ) );
    }


    /**
     * @param agent agent
     *
     * @return Yes if give agent is among seeds, otherwise returns No
     */
    public String checkIfMaster( Agent agent )
    {
        if ( config.getMasterNodes().contains( agent ) )
        {
            return "Master Node";
        }
        return "Data Node";
    }


    public void refreshClustersInfo()
    {
        List<ElasticsearchClusterConfiguration> elasticsearchClusterConfigurationList = elasticsearch.getClusters();
        ElasticsearchClusterConfiguration clusterInfo = ( ElasticsearchClusterConfiguration ) clusterCombo.getValue();
        clusterCombo.removeAllItems();

        if ( elasticsearchClusterConfigurationList == null || elasticsearchClusterConfigurationList.isEmpty() )
        {
            return;
        }

        for ( ElasticsearchClusterConfiguration elasticsearchClusterConfiguration :
                elasticsearchClusterConfigurationList )
        {
            clusterCombo.addItem( elasticsearchClusterConfiguration );
            clusterCombo.setItemCaption( elasticsearchClusterConfiguration,
                    elasticsearchClusterConfiguration.getClusterName() );
        }

        if ( clusterInfo != null )
        {
            for ( ElasticsearchClusterConfiguration cassandraInfo : elasticsearchClusterConfigurationList )
            {
                if ( cassandraInfo.getClusterName().equals( clusterInfo.getClusterName() ) )
                {
                    clusterCombo.setValue( cassandraInfo );
                    return;
                }
            }
        }
        else
        {
            clusterCombo.setValue( elasticsearchClusterConfigurationList.iterator().next() );
        }
    }


    public Component getContent()
    {
        return contentRoot;
    }
}