package retwis;

import choral.channels.SymChannel_A;
import choral.channels.SymChannel_B;
import choral.choralUnit.testUtils.TestUtils;
import choral.utils.Pair;
import commandInterfaces.HTTPCommandInterface;
import commandInterfaces.RetwisCommandInterface;
import databases.InMemoryDatabaseConnection;
import emitters.Emitter;
import emitters.HTTPEmitter;
import emitters.RetwisEmitter;
import emitters.ScriptedEmitter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DemoRetwis {

	private static void startRepository(
			ExecutorService executor, SymChannel_B< Object > chSR
	) {
		executor.submit( () -> {
			try {
				new Retwis_Repository( chSR, InMemoryDatabaseConnection.instance() ).loop();
				executor.shutdown();
			} catch( Exception e ) {
				e.printStackTrace();
			}
		} );
	}

	private static void startServer(
			ExecutorService executor, SymChannel_B< Object > chCS, SymChannel_A< Object > chSR
	) {
		executor.submit( () -> {
			try {
				new Retwis_Server( chCS, chSR, SimpleSessionManager.instance() ).loop();
				executor.shutdown();
			} catch( Exception e ) {
				e.printStackTrace();
			}
		} );
	}

	public static void main( String[] args ) throws InterruptedException, ExecutionException {

		Token sToken = SimpleSessionManager.instance().createSession( "Save" );
		Token mToken = SimpleSessionManager.instance().createSession( "Marco" );
		Token fToken = SimpleSessionManager.instance().createSession( "Fabrizio" );

		InMemoryDatabaseConnection.instance().addUser( "Save", "pswd" );
		InMemoryDatabaseConnection.instance().addUser( "Marco", "pswd" );
		InMemoryDatabaseConnection.instance().addUser( "Fabrizio", "pswd" );

		Pair< SymChannel_A< Object >, SymChannel_B< Object > > chSR =
				TestUtils.newLocalChannel( "chSR" );
		Pair< SymChannel_A< Object >, SymChannel_B< Object > > chCS =
				TestUtils.newLocalChannel( "chCS" );

		startRepository( Executors.newSingleThreadExecutor(), chSR.right() );
		startServer( Executors.newSingleThreadExecutor(), chCS.right(), chSR.left() );

		InetSocketAddress commandInterfaceAddress = new InetSocketAddress( 8888 );

		try {
			RetwisCommandInterface HTTP_CI = new RetwisCommandInterface( commandInterfaceAddress );

			ScriptedEmitter.use( RetwisEmitter.use( commandInterfaceAddress ).setPrefix( "/retwisj" ) )
					.emit( new LinkedList<>( List.of(
							new Emitter.Post( sToken, "A retwis.Post from Save", "Save" ),
							new Emitter.Follow( fToken, "Save", "Fabrizio" ),
							new Emitter.Follow( sToken, "Marco", "Save" ),
							new Emitter.Posts( "Save", 0 ),
							new Emitter.Logout( sToken )
					) ) );

			new Retwis_Client( chCS.left(), HTTP_CI ).loop();

			HTTP_CI.stop();

		} catch( IOException e ) {
			e.printStackTrace();
		}
	}

}
