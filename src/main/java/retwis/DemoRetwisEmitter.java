package retwis;

import emitters.Emitter;
import emitters.RetwisEmitter;
import emitters.ScriptedEmitter;
import retwis.Token;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class DemoRetwisEmitter {

	public static void main( String[] args ) throws IOException, InterruptedException {

		RetwisEmitter emitter = RetwisEmitter.use( new InetSocketAddress( 8080 ) ).setPrefix( "/retwisj" );

		// we get the token
		String username = "pippo";
		String password = "pippo";
		HttpRequest request = HttpRequest.newBuilder()
						.POST( HttpRequest.BodyPublishers.noBody() )
						.uri( URI.create( "http://localhost:8080/retwisj/signIn?name=" + username + "&pass=" + password ) )
						.build();
		HttpResponse< String > response = null;
		HttpClient client = HttpClient.newBuilder().version( HttpClient.Version.HTTP_1_1 ).build();
		response = client.send( request, HttpResponse.BodyHandlers.ofString( StandardCharsets.UTF_8 ) );
		Optional< String > cookies = response.headers().firstValue( "set-cookie" );
		if ( cookies.isEmpty() ) {
			throw new RuntimeException( "Could not find 'set-cookie' header" );
		}
		Token sessionToken = new Token( cookies.get().split( ";" )[ 0 ].split( "=" )[1] );

		// we run the script
		ScriptedEmitter.use( emitter )
						.emit( new LinkedList<>( List.of(
										new Emitter.Post( sessionToken, "A retwis.Post "
														+ LocalDateTime.now().format( DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss" ) ),
														"pippo" ),
										new Emitter.Follow( sessionToken, "thesave", "pippo" ),
										new Emitter.StopFollow( sessionToken, "thesave", "pippo" ),
										new Emitter.Posts( "pippo", 1 ),
										new Emitter.Status( sessionToken, "1" ),
										new Emitter.Mentions( sessionToken, "pippo" ),
										new Emitter.Logout( sessionToken )
						) ) );
	}

}
