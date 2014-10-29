package org.safehaus.subutai.core.environment.api;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


@Ignore
public class EnvironmentTest
{

    private static final String NAME = "name";
    Environment environment;


    @Before
    public void setUp() throws Exception
    {

        this.environment = new Environment( NAME );
    }


    @Test
    public void testSetGetContainers() throws Exception
    {

        Set<ContainerHost> set = new HashSet<>();
        //        environment.setContainers( set );
        assertEquals( set, environment.getContainers() );
    }


    @Test
    public void testGetName() throws Exception
    {
        assertEquals( NAME, environment.getName() );
    }


    @Test
    public void testNodesNotNull() throws Exception
    {
        Set<ContainerHost> environmentContainers = environment.getContainers();
        assertNotNull( environmentContainers );
    }


    @Test
    public void testUUIDNotNull() throws Exception
    {
        UUID uuid = environment.getId();
        assertNotNull( uuid );
    }
}
