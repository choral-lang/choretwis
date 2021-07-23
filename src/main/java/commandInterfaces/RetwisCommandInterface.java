package commandInterfaces;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import emitters.Emitter;
import retwis.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class RetwisCommandInterface implements CommandInterface {

	private final List< CompletableFuture< Emitter.Action > > actions;
	private final HttpServer httpServer;
	private Emitter.Action currentAction;
	private ResponseMessage currentResponseMessage;
	private final List< CompletableFuture< ResponseMessage > > actionResponses;

	public RetwisCommandInterface( InetSocketAddress socketAddress ) throws IOException {
		actions = new LinkedList<>();
		actionResponses = new LinkedList<>();
		httpServer = HttpServer.create();
		httpServer.bind( socketAddress, 10 );
		addContexts();
		httpServer.start();
	}


	private static Token getToken( HttpExchange exchange ) {
		return new Token( exchange.getRequestHeaders().get( "Cookie" ).get( 0 ).split( "=" )[ 1 ] );
	}

	private static String getUsername( URI uri ) {
		return uri.getPath().split( "!" )[ 1 ].split( "/" )[ 0 ];
	}

	public void addContexts() {
		httpServer.createContext( "/retwisj/!", exchange -> {
			Emitter.Action action = null;
			try {
				URI requestURI = exchange.getRequestURI();
				System.out.println( "Received request: " + requestURI );
				if ( exchange.getRequestMethod().equals( "GET" ) ) {
					// its an action on the user { follow, stopfollow, mentions }
					if ( requestURI.getQuery() == null || requestURI.getQuery().isBlank() ) {
						String[] _l = requestURI.getPath().split( "/" );
						String actionName = _l[ _l.length - 1 ];
						action = switch ( actionName ) {
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
							default -> {
								System.err.println( "ERROR, could not match action " + actionName );
								throw new IllegalStateException( "Unexpected value: " + actionName );
							}
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
			} catch ( Exception e ) {
				e.printStackTrace();
			}
			addAction( action );
			handleResponse( exchange );
		} );

		httpServer.createContext( "/retwisj/status", exchange -> {
			Emitter.Action action = new Emitter.Status(
							getToken( exchange ),
							exchange.getRequestURI().getQuery().split( "=" )[ 1 ]
			);
			addAction( action );
			handleResponse( exchange );
		} );


		httpServer.createContext( "/retwisj/logout", exchange -> {
			Emitter.Action action = new Emitter.Logout( getToken( exchange ) );
			addAction( action );
			handleResponse( exchange );
		} );
	}

	private void handleResponse( HttpExchange exchange ) {
		try {
			CompletableFuture< ResponseMessage > frm = new CompletableFuture<>();
			synchronized ( actionResponses ) {
				actionResponses.add( frm );
			}
			ResponseMessage responseMessage = frm.get();
			synchronized ( actionResponses ) {
				actionResponses.remove( 0 );
			}
			String response = String.join( "\n", responseMessage.messages );
			exchange.sendResponseHeaders(
							responseMessage.isError() ? 500 : 200,
							response.getBytes( StandardCharsets.UTF_8 ).length );
			OutputStream os = exchange.getResponseBody();
			os.write( response.getBytes( StandardCharsets.UTF_8 ) );
			os.flush();
			os.close();
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}

	public void stop() {
		synchronized ( actionResponses ) {
			actionResponses.get( 0 ).complete( currentResponseMessage );
		}
		httpServer.stop( 1000 );
	}

	boolean firstLoop = true;

	@Override
	public RetwisAction action() {
		if ( firstLoop ) {
			firstLoop = false;
		} else {
			synchronized ( actionResponses ) {
				actionResponses.get( 0 ).complete( currentResponseMessage );
			}
		}
		currentResponseMessage = new ResponseMessage();
		CompletableFuture< Emitter.Action > cc;
		synchronized ( actions ) {
			if ( !actions.isEmpty() && actions.get( 0 ).isDone() ) {
				cc = actions.remove( 0 );
			} else {
				cc = new CompletableFuture<>();
				actions.add( cc );
			}
		}
		try {
			currentAction = cc.get();
		} catch ( InterruptedException | ExecutionException e ) {
			e.printStackTrace();
		}
		return currentAction.action();
	}

	public void addAction( Emitter.Action action ) {
		synchronized ( actions ) {
			if ( actions.isEmpty() || actions.get( 0 ).isDone() ) {
				CompletableFuture< Emitter.Action > cc = new CompletableFuture<>();
				cc.complete( action );
				actions.add( cc );
			} else {
				actions.remove( 0 ).complete( action );
			}
		}
	}

	@Override
	public String getPostsUsername() {
		return currentAction.postsUsername();
	}

	@Override
	public String getUsername() {
		return currentAction.username();
	}

	@Override
	public Integer getPostsPage() {
		return currentAction.postsPage();
	}

	@Override
	public Token getSessionToken() {
		return currentAction.sessionToken();
	}

	@Override
	public String getPost() {
		return currentAction.post();
	}

	@Override
	public String getFollowTarget() {
		return currentAction.followTarget();
	}

	@Override
	public String getStatusPostID() {
		return currentAction.statusPostID();
	}

	@Override
	public String promptPassword() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getMentionsUsername() {
		return currentAction.mentionsUsername();
	}

	@Override
	public String getStopFollowTarget() {
		return currentAction.stopFollowTarget();
	}

	// OUTPUT METHODS

	@Override
	public void showPosts( Posts posts ) {
		currentResponseMessage.add( posts.toString() );
	}

	@Override
	public void showPost( Post post ) {
		currentResponseMessage.add( post.toString() );
	}

	@Override
	public void showErrorMessage( String message ) {
		currentResponseMessage.add( message, true );
	}

	@Override
	public void showSuccessMessage( String message ) {
		currentResponseMessage.add( message );
	}

	@Override
	public void showMentions( Mentions mentions ) {
		currentResponseMessage.add( mentions.toString() );
	}


	private static class ResponseMessage {

		boolean isError;
		List< String > messages;

		private ResponseMessage() {
			isError = false;
			messages = new LinkedList<>();
		}

		void add( String message, boolean isError ) {
			this.isError |= isError;
			messages.add( message );
		}

		void add( String message ) {
			add( message, false );
		}

		public boolean isError() {
			return isError;
		}

		public List< String > messages() {
			return messages;
		}
	}

}
