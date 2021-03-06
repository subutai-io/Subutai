package io.subutai.bazaar.share.event.payload;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


@JsonTypeInfo( use = JsonTypeInfo.Id.NAME, property = "type" )
@JsonSubTypes( {
        @JsonSubTypes.Type( value = CustomPayload.class, name = "custom" ),
        @JsonSubTypes.Type( value = LogPayload.class, name = "log" ),
        @JsonSubTypes.Type( value = ProgressPayload.class, name = "progress" )
} )
@JsonAutoDetect( fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE )
@JsonIgnoreProperties( ignoreUnknown = true )
abstract public class Payload
{
}
