/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.accumulo.ui.manager;


import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.ProductOperationView;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.accumulo.api.Accumulo;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.accumulo.api.NodeType;

import com.google.common.base.Strings;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Window;


/**
 * @author dilshat
 */
public class AddNodeWindow extends Window
{

    private final TextArea outputTxtArea;
    private final Label indicator;
    private volatile boolean track = true;


    public AddNodeWindow( final Accumulo accumulo, final ExecutorService executorService, final Tracker tracker,
                          final AccumuloClusterConfig accumuloClusterConfig, Set<Agent> nodes, final NodeType nodeType )
    {
        super( "Add New Node" );
        setModal( true );

        setWidth( 650, Unit.PIXELS );
        setHeight( 450, Unit.PIXELS );

        GridLayout content = new GridLayout( 1, 3 );
        content.setSizeFull();
        content.setMargin( true );
        content.setSpacing( true );

        HorizontalLayout topContent = new HorizontalLayout();
        topContent.setSpacing( true );

        content.addComponent( topContent );
        topContent.addComponent( new Label( "Nodes:" ) );

        final ComboBox hadoopNodes = new ComboBox();
        hadoopNodes.setImmediate( true );
        hadoopNodes.setTextInputAllowed( false );
        hadoopNodes.setNullSelectionAllowed( false );
        hadoopNodes.setRequired( true );
        hadoopNodes.setWidth( 200, Unit.PIXELS );
        for ( Agent node : nodes )
        {
            hadoopNodes.addItem( node );
            hadoopNodes.setItemCaption( node, node.getHostname() );
        }
        hadoopNodes.setValue( nodes.iterator().next() );

        topContent.addComponent( hadoopNodes );

        final Button addNodeBtn = new Button( "Add" );
        topContent.addComponent( addNodeBtn );

        addNodeBtn.addClickListener( new Button.ClickListener()
        {

            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                addNodeBtn.setEnabled( false );
                showProgress();
                Agent agent = ( Agent ) hadoopNodes.getValue();
                final UUID trackID =
                        accumulo.addNode( accumuloClusterConfig.getClusterName(), agent.getHostname(), nodeType );
                executorService.execute( new Runnable()
                {

                    public void run()
                    {
                        while ( track )
                        {
                            ProductOperationView po =
                                    tracker.getProductOperation( AccumuloClusterConfig.PRODUCT_KEY, trackID );
                            if ( po != null )
                            {
                                setOutput(
                                        po.getDescription() + "\nState: " + po.getState() + "\nLogs:\n" + po.getLog() );
                                if ( po.getState() != ProductOperationState.RUNNING )
                                {
                                    hideProgress();
                                    break;
                                }
                            }
                            else
                            {
                                setOutput( "Product operation not found. Check logs" );
                                break;
                            }
                            try
                            {
                                Thread.sleep( 1000 );
                            }
                            catch ( InterruptedException ex )
                            {
                                break;
                            }
                        }
                    }
                } );
            }
        } );

        outputTxtArea = new TextArea( "Operation output" );
        outputTxtArea.setRows( 13 );
        outputTxtArea.setColumns( 43 );
        outputTxtArea.setImmediate( true );
        outputTxtArea.setWordwrap( true );

        content.addComponent( outputTxtArea );

        indicator = new Label();
        indicator.setIcon( new ThemeResource( "img/spinner.gif" ) );
        indicator.setContentMode( ContentMode.HTML );
        indicator.setHeight( 11, Unit.PIXELS );
        indicator.setWidth( 50, Unit.PIXELS );
        indicator.setVisible( false );

        Button ok = new Button( "Ok" );
        ok.addClickListener( new Button.ClickListener()
        {

            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                //close window
                track = false;
                close();
            }
        } );

        HorizontalLayout bottomContent = new HorizontalLayout();
        bottomContent.addComponent( indicator );
        bottomContent.setComponentAlignment( indicator, Alignment.MIDDLE_RIGHT );
        bottomContent.addComponent( ok );

        content.addComponent( bottomContent );
        content.setComponentAlignment( bottomContent, Alignment.MIDDLE_RIGHT );

        setContent( content );
    }


    private void showProgress()
    {
        indicator.setVisible( true );
    }


    private void setOutput( String output )
    {
        if ( !Strings.isNullOrEmpty( output ) )
        {
            outputTxtArea.setValue( output );
            outputTxtArea.setCursorPosition( outputTxtArea.getValue().toString().length() - 1 );
        }
    }


    private void hideProgress()
    {
        indicator.setVisible( false );
    }


    @Override
    public void close()
    {
        super.close();
        track = false;
    }
}
