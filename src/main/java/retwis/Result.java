package retwis;

import choral.runtime.Serializers.KryoSerializable;
import choral.annotations.Choreography;

@KryoSerializable
@Choreography( role = "R", name = "retwis.Result" )
public enum Result {
	OK, ERROR
}
