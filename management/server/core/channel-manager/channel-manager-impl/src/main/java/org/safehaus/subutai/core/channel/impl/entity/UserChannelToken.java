package org.safehaus.subutai.core.channel.impl.entity;


import java.sql.Timestamp;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * Created by nisakov on 3/3/15.
 */


@Entity
@Table( name = "user_channel_token" )
@Access( AccessType.FIELD )

public class UserChannelToken
{
    @Id
    @Column( name = "user_id" )
    private Long id;

    @Column(name = "valid_period")
    private short validPeriod = 0;

    @Column(name = "status")
    private short status;

    @Column( name = "token")
    private String token;

    @Column (name = "date")
    private Timestamp date;


    /************************************************************************
     *
     */
    public Long getId()
    {
        return id;
    }

    public void setId( Long id )
    {
        this.id = id;
    }



    public short getStatus()
    {
        return status;
    }

    public void setStatus( short status )
    {
        this.status = status;
    }

    public String getToken()
    {
        return token;
    }

    public void setToken( String token )
    {
        this.token = token;
    }

    public Timestamp getDate()
    {
        return date;
    }

    public void setDate( Timestamp date )
    {
        this.date = date;
    }

    public short getValidPeriod()
    {
        return validPeriod;
    }

    public void setValidPeriod( short validPeriod )
    {
        this.validPeriod = validPeriod;
    }

}
