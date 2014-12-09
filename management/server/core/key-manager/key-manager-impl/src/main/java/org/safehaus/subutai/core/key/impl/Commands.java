package org.safehaus.subutai.core.key.impl;


import org.safehaus.subutai.common.command.RequestBuilder;

import com.google.common.collect.Lists;


/**
 * Key management related commands
 */
public class Commands
{

    private static final String KEY_MANAGER_BINDING = "subutai key_manager";


    public RequestBuilder getGenerateKeyCommand( String realName, String email )
    {
        return new RequestBuilder( KEY_MANAGER_BINDING )
                .withCmdArgs( Lists.newArrayList( "generate", realName, email ) ).withTimeout( 90 );
    }


    public RequestBuilder getReadKeyCommand( String keyId )
    {
        return new RequestBuilder( KEY_MANAGER_BINDING ).withCmdArgs( Lists.newArrayList( "export", keyId ) );
    }


    public RequestBuilder getReadSshKeyCommand( String keyId )
    {
        return new RequestBuilder( KEY_MANAGER_BINDING ).withCmdArgs( Lists.newArrayList( "export", "-ssh", keyId ) );
    }


    public RequestBuilder getSignCommand( String keyId, String filePath )
    {
        return new RequestBuilder( KEY_MANAGER_BINDING ).withCmdArgs( Lists.newArrayList( "sign", keyId, filePath ) );
    }


    public RequestBuilder getSendKeyCommand( String keyId )
    {
        return new RequestBuilder( KEY_MANAGER_BINDING ).withCmdArgs( Lists.newArrayList( "send", keyId ) );
    }


    public RequestBuilder getListKeyCommand( String keyId )
    {
        return new RequestBuilder( KEY_MANAGER_BINDING ).withCmdArgs( Lists.newArrayList( "list", keyId ) );
    }


    public RequestBuilder getListKeysCommand()
    {
        return new RequestBuilder( KEY_MANAGER_BINDING ).withCmdArgs( Lists.newArrayList( "list" ) );
    }


    public RequestBuilder getDeleteKeyCommand( String keyId )
    {
        return new RequestBuilder( KEY_MANAGER_BINDING ).withCmdArgs( Lists.newArrayList( "delete", keyId ) );
    }


    public RequestBuilder getRevokeKeyCommand( String keyId )
    {
        return new RequestBuilder( KEY_MANAGER_BINDING ).withCmdArgs( Lists.newArrayList( "revoke", keyId ) );
    }
}
