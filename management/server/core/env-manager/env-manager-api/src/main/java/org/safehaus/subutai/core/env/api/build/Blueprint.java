package org.safehaus.subutai.core.env.api.build;


import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.util.CollectionUtil;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;


/**
 * Blueprint for environment creation
 */
public class Blueprint
{
    private UUID id;
    private String name;
    private Set<NodeGroup> nodeGroups;


    public Blueprint( final String name, final Set<NodeGroup> nodeGroups )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( name ), "Invalid name" );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( nodeGroups ), "Invalid node group set" );

        this.id = UUID.randomUUID();
        this.name = name;
        this.nodeGroups = nodeGroups;
    }


    public UUID getId()
    {
        return id;
    }


    public String getName()
    {
        return name;
    }


    public Set<NodeGroup> getNodeGroups()
    {
        return nodeGroups == null ? Sets.<NodeGroup>newHashSet() : Collections.unmodifiableSet( nodeGroups );
    }
}
