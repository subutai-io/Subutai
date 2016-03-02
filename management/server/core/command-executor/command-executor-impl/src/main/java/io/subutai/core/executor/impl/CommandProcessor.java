package io.subutai.core.executor.impl;


import java.util.UUID;

import javax.naming.NamingException;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.subutai.common.cache.ExpiringCache;
import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.Request;
import io.subutai.common.command.ResponseImpl;
import io.subutai.common.command.ResponseWrapper;
import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.settings.Common;
import io.subutai.common.settings.SystemSettings;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.broker.api.Broker;
import io.subutai.core.broker.api.ByteMessageListener;
import io.subutai.core.broker.api.Topic;
import io.subutai.core.hostregistry.api.HostDisconnectedException;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.Session;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.EncryptionTool;


/**
 * Executes commands and processes responses
 */
public class CommandProcessor implements ByteMessageListener
{
    private static final Logger LOG = LoggerFactory.getLogger( CommandProcessor.class.getName() );
    private final Broker broker;
    private final HostRegistry hostRegistry;
    private IdentityManager identityManager;

    protected ExpiringCache<UUID, CommandProcess> commands = new ExpiringCache<>();


    public CommandProcessor( final Broker broker, final HostRegistry hostRegistry,
                             final IdentityManager identityManager )
    {
        Preconditions.checkNotNull( broker );
        Preconditions.checkNotNull( hostRegistry );
        Preconditions.checkNotNull( identityManager );

        this.broker = broker;
        this.hostRegistry = hostRegistry;
        this.identityManager = identityManager;
    }


    @Override
    public Topic getTopic()
    {
        return Topic.RESPONSE_TOPIC;
    }


    public void execute( final Request request, CommandCallback callback ) throws CommandException
    {
        //find target host
        ResourceHostInfo targetHost;
        try
        {
            targetHost = getTargetHost( request.getId() );
        }
        catch ( HostDisconnectedException e )
        {
            throw new CommandException( e );
        }

        //create command process
        CommandProcess commandProcess = new CommandProcess( this, callback, getActiveSession() );
        boolean queued =
                commands.put( request.getCommandId(), commandProcess, Common.INACTIVE_COMMAND_DROP_TIMEOUT_SEC * 1000,
                        new CommandProcessExpiryCallback() );
        if ( !queued )
        {
            throw new CommandException( "This command is already queued for execution" );
        }

        //send command
        try
        {
            commandProcess.start();

            String command = JsonUtil.toJson( new RequestWrapper( request ) );

            LOG.info( String.format( "Sending:%n%s", command ) );

            //TODO here use REST to send message to target RH instead of broker

            broker.sendTextMessage( targetHost.getId(), command );

            LOG.info( String.format( "ENCRYPTING:%n%s", encrypt( command ) ) );
        }
        catch ( Exception e )
        {
            remove( request.getCommandId() );

            commandProcess.stop();

            throw new CommandException( e );
        }
    }


    //todo move this method to SecurityManager
    protected String encrypt( String message ) throws PGPException, NamingException
    {
        if ( SystemSettings.getEncryptionState() )
        {

            EncryptionTool encryptionTool = getSecurityManager().getEncryptionTool();

            RequestImplWrapper requestImplWrapper = JsonUtil.fromJson( message, RequestImplWrapper.class );

            Request originalRequest = requestImplWrapper.getRequest();

            //obtain target host pub key for encrypting
            PGPPublicKey hostKeyForEncrypting =
                    getSecurityManager().getKeyManager().getPublicKey( originalRequest.getId() );

            String encryptedRequestString = new String( encryptionTool
                    .signAndEncrypt( JsonUtil.toJson( originalRequest ).getBytes(), hostKeyForEncrypting, true ) );

            EncryptedRequestWrapper encryptedRequestWrapper =
                    new EncryptedRequestWrapper( encryptedRequestString, originalRequest.getId() );

            return JsonUtil.toJson( encryptedRequestWrapper );
        }

        return message;
    }


    public static SecurityManager getSecurityManager() throws NamingException
    {
        return ServiceLocator.getServiceNoCache( SecurityManager.class );
    }


    protected Session getActiveSession()
    {
        return identityManager.getActiveSession();
    }


    public CommandResult getResult( UUID commandId ) throws CommandException
    {
        Preconditions.checkNotNull( commandId );

        CommandProcess commandProcess = commands.get( commandId );
        if ( commandProcess != null )
        {
            //wait until process completes  & return result
            return commandProcess.waitResult();
        }
        else
        {
            throw new CommandException( String.format( "Command process not found by id: %s", commandId ) );
        }
    }


    @Override
    public void onMessage( final byte[] message )
    {
        try
        {
            String responseString = new String( message, "UTF-8" );

            ResponseWrapper responseWrapper = JsonUtil.fromJson( responseString, ResponseWrapper.class );

            LOG.info( String.format( "Received:%n%s", JsonUtil.toJson( responseWrapper ) ) );

            ResponseImpl response = responseWrapper.getResponse();

            CommandProcess commandProcess = commands.get( response.getCommandId() );

            if ( commandProcess != null )
            {
                //process response
                commandProcess.processResponse( response );
            }
            else
            {
                LOG.warn( String.format( "Callback not found for response: %s", response ) );
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Error processing response", e );
        }
    }


    protected ResourceHostInfo getTargetHost( String hostId ) throws HostDisconnectedException
    {
        ResourceHostInfo targetHost;

        try
        {
            targetHost = hostRegistry.getResourceHostInfoById( hostId );
        }
        catch ( HostDisconnectedException e )
        {
            ContainerHostInfo containerHostInfo = hostRegistry.getContainerHostInfoById( hostId );
            if ( containerHostInfo.getState() != ContainerHostState.RUNNING )
            {
                throw new HostDisconnectedException(
                        String.format( "Container state is %s", containerHostInfo.getState() ) );
            }
            targetHost = hostRegistry.getResourceHostByContainerHost( containerHostInfo );
        }

        return targetHost;
    }


    protected void remove( UUID commandId )
    {
        Preconditions.checkNotNull( commandId );

        commands.remove( commandId );
    }


    public void dispose()
    {
        commands.dispose();
    }
}
