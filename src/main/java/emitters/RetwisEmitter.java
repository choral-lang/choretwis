package emitters;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static emitters.Emitter.Action.Fields;

public class RetwisEmitter implements Emitter {

	private final InetSocketAddress serverAddress;
	private String prefix = null;

	private RetwisEmitter( InetSocketAddress serverAddress ) {
		this.serverAddress = serverAddress;
	}

	public static RetwisEmitter use( InetSocketAddress serverAddress ) {
		return new RetwisEmitter( serverAddress );
	}

	public RetwisEmitter setPrefix( String prefix ) {
		this.prefix = prefix;
		return this;
	}

	private String getServerPrefix() {
		return "http://" + serverAddress.getHostName() + ":" + serverAddress.getPort() +
						( ( prefix != null ) ? prefix : "" );
	}

	@Override
	public Emitter emit( Action action ) {
		try {
			HttpRequest request = null;
			switch ( action.action() ) {
				// ✅
				case POSTS -> {
					request = HttpRequest.newBuilder()
									.GET()
									.uri( URI.create( getServerPrefix() + "/!" + action.postsUsername() + "?" + "page=" + action.postsPage() ) )
									.build();
				}
				// ✅
				case POST -> {
					request = HttpRequest.newBuilder()
									.POST( HttpRequest.BodyPublishers.noBody() )
									.uri( URI.create( getServerPrefix() + "/!" + action.username()
													+ "?content=" + URLEncoder.encode( action.post(), StandardCharsets.UTF_8 )
													+ "&replyTo=&replyPid=" ) )
									.header( "Cookie", "retwisauth=" + action.sessionToken().id() )
									.build();
				}
				// ✅
				case FOLLOW -> {
					request = HttpRequest.newBuilder()
									.GET()
									.uri( URI.create( getServerPrefix() + "/!" + action.followTarget() + "/follow" ) )
									.header( "Cookie", "retwisauth=" + action.sessionToken().id() )
									.build();
				}
				// ✅
				case STOPFOLLOW -> {
					request = HttpRequest.newBuilder()
									.GET()
									.uri( URI.create( getServerPrefix() + "/!" + action.stopFollowTarget() + "/stopfollowing" ) )
									.header( "Cookie", "retwisauth=" + action.sessionToken().id() )
									.build();
				}
				// ✅
				case MENTIONS -> {
					request = HttpRequest.newBuilder()
									.GET()
									.uri( URI.create( getServerPrefix() + "/!" + action.mentionsUsername() + "/mentions" ) )
									.header( "Cookie", "retwisauth=" + action.sessionToken().id() )
									.build();
				}
				// ✅
				case STATUS -> {
					request = HttpRequest.newBuilder()
									.GET()
									.uri( URI.create( getServerPrefix() + "/status?pid=" + action.statusPostID() ) )
									.header( "Cookie", "retwisauth=" + action.sessionToken().id() )
									.build();
				}
				// ✅
				case LOGOUT -> {
					request = HttpRequest.newBuilder()
									.GET()
									.uri( URI.create( getServerPrefix() + "/logout" ) )
									.header( "Cookie", "retwisauth=" + action.sessionToken().id() )
									.build();
				}
			}
			HttpClient client = HttpClient.newBuilder()
							.version( HttpClient.Version.HTTP_1_1 )
							.build();
			HttpResponse< String > response = null;
			response = client.send( request,
							HttpResponse.BodyHandlers.ofString( StandardCharsets.UTF_8 ) );
			System.out.println( "HTTP_EMITTER Received response: "
							+ response + "\n"
							+ response.body() );
		} catch ( IOException | InterruptedException e ) {
			e.printStackTrace();
		}
		return this;
	}
}
