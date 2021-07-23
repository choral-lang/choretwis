package tests;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import emitters.Emitter;
import emitters.RetwisEmitter;
import emitters.ScriptedEmitter;
import retwis.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TestCommandInterface {

	public static void main( String[] args ) throws IOException, InterruptedException {
		HttpServer httpServer = HttpServer.create();
		httpServer.bind( new InetSocketAddress( 8080 ), 10 );
		addContexts( httpServer );
		httpServer.start();

		Token sessionToken = new Token( "123" );

		RetwisEmitter emitter = RetwisEmitter.use( new InetSocketAddress( 8080 ) ).setPrefix( "/retwisj" );

		// we run the script
		ScriptedEmitter.use( emitter )
						.emit( new LinkedList<>( List.of(
//										new Emitter.Post( sessionToken, "A retwis.Post "
//														+ LocalDateTime.now().format( DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss" ) ),
//														"pippo" )
//										,
//										new Emitter.Follow( sessionToken, "thesave", "pippo" )
//										,
//										new Emitter.StopFollow( sessionToken, "thesave", "pippo" )
//										,
//										new Emitter.Posts( "pippo", 1 )
//										,
//										new Emitter.Status( sessionToken, "1" )
//										,
//										new Emitter.Mentions( sessionToken, "pippo" )
//										,
										new Emitter.Logout( sessionToken )
						) ) );


	}

	private static Token getToken( HttpExchange exchange ) {
		return new Token( exchange.getRequestHeaders().get( "Cookie" ).get( 0 ).split( "=" )[ 1 ] );
	}

	private static String getUsername( URI uri ) {
		return uri.getPath().split( "!" )[ 1 ].split( "/" )[ 0 ];
	}

	public static void addContexts( HttpServer httpServer ) {
		httpServer.createContext( "/retwisj/!", exchange -> {
			Emitter.Action action;
			URI requestURI = exchange.getRequestURI();
			System.out.println( "Received request: " + requestURI );
			if ( exchange.getRequestMethod().equals( "GET" ) ) {
				// its an action on the user { follow, stopfollow, mentions }
				if ( requestURI.getQuery() != null && requestURI.getQuery().isBlank() ) {
					String[] _l = requestURI.getPath().split( "/" );
					String actionName = _l[ _l.length - 1 ];
					action =
									switch ( actionName ) {
										case "follow" -> new Emitter.Follow(
														getToken( exchange ),
														getUsername( exchange.getRequestURI() ),
														exchange.getRequestHeaders().getFirst( "username" )
										);
										case "stopfollowing" -> new Emitter.StopFollow(
														getToken( exchange ),
														getUsername( exchange.getRequestURI() ),
														exchange.getRequestHeaders().getFirst( "username" )
										);
										case "mentions" -> new Emitter.Mentions(
														getToken( exchange ),
														getUsername( exchange.getRequestURI() )
										);
										default -> throw new IllegalStateException( "Unexpected value: " + actionName );
									};
				} // its the request for showing posts
				else {
					String username = requestURI.getPath().split( "!" )[ 1 ];
					int page = Integer.parseInt( requestURI.getQuery().split( "=" )[ 1 ] );
					action = new Emitter.Posts( username, page );
				}
			} else {
				String username = requestURI.getPath().split( "!" )[ 1 ];
				Token token = getToken( exchange );
				String content = URLDecoder.decode( Arrays.stream( requestURI.getQuery().split( "&" ) )
								.filter( i -> i.contains( "content" ) )
								.collect( Collectors.joining( "" ) )
								.split( "=" )[ 1 ], StandardCharsets.UTF_8 );
				action = new Emitter.Post( token, content );
			}

//			addAction( action );
			handleResponse( exchange );
		} );

		httpServer.createContext( "/retwisj/status", exchange -> {
			Emitter.Action action = new Emitter.Status(
							getToken( exchange ),
							exchange.getRequestURI().getQuery().split( "=" )[ 1 ]
			);
			handleResponse( exchange );
		});


		httpServer.createContext( "/retwisj/logout", exchange -> {
			Emitter.Action action = new Emitter.Logout( getToken( exchange ) );
			handleResponse( exchange );
		});
	}

	private static void handleResponse( HttpExchange exchange ) {
		try {
			String response = String.join( "OK" );
			exchange.sendResponseHeaders( 200, response.getBytes( StandardCharsets.UTF_8 ).length );
			OutputStream os = exchange.getResponseBody();
			os.write( response.getBytes( StandardCharsets.UTF_8 ) );
			os.flush();
			os.close();
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}


}
