package retwis;

public interface SessionManager{

	Token createSession( String name );

    void closeSession( Token token );

    Boolean checkLoggedUser( Token token );

    String getUsernameFromToken( Token token );

}