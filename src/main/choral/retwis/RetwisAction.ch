package retwis;
import choral.runtime.Serializers.KryoSerializable;

@KryoSerializable
public enum RetwisAction@R {
       POSTS, POST, FOLLOW, STOPFOLLOW, MENTIONS, STATUS, LOGOUT
}