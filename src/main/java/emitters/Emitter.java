package emitters;

import retwis.RetwisAction;
import retwis.Token;

public interface Emitter {

	Emitter emit( Action action );

	interface Action {
		RetwisAction action();

		default String 	postsUsername(){ 	throw new UnsupportedOperationException(); }
		default Integer postsPage(){ 		throw new UnsupportedOperationException(); }
		default String 	post(){				throw new UnsupportedOperationException(); }
		default Token 	sessionToken(){		throw new UnsupportedOperationException(); }
		default String 	followTarget(){		throw new UnsupportedOperationException(); }
		default String 	stopFollowTarget(){	throw new UnsupportedOperationException(); }
		default String 	username(){ 		throw new UnsupportedOperationException(); }
		default String 	mentionsUsername(){ throw new UnsupportedOperationException(); }
		default String 	statusPostID(){ 	throw new UnsupportedOperationException(); }

		enum Fields {
			postsUsername,
			postsPage,
			post,
			sessionToken,
			followTarget,
			stopFollowTarget,
			username,
			mentionsUsername,
			statusPostID
		}

	}

	class Posts implements Action {
		private final String postsUsername;
		private final Integer postsPage;
		private final RetwisAction action = RetwisAction.POSTS;

		public Posts( String postsUsername, Integer postsPage ) {
			this.postsUsername = postsUsername;
			this.postsPage = postsPage;
		}

		public String postsUsername() {
			return postsUsername;
		}

		public Integer postsPage() {
			return postsPage;
		}

		public RetwisAction action(){
			return action;
		}
	}

	class Post implements Action {
		private final Token sessionToken;
		private final String post;
		private final String username;
		private final RetwisAction action = RetwisAction.POST;

		public Post( Token sessionToken, String post, String username ) {
			this.username = username;
			this.sessionToken = sessionToken;
			this.post = post;
		}

		public Post( Token sessionToken, String post ) {
			this.username = "";
			this.sessionToken = sessionToken;
			this.post = post;
		}

		@Override
		public Token sessionToken() {
			return sessionToken;
		}

		@Override
		public String post() {
			return post;
		}

		@Override
		public String username() {
			return username;
		}

		@Override
		public RetwisAction action() {
			return action;
		}
	}

	class Follow implements Action {
		private final Token sessionToken;
		private final String followTarget;
		private final String username;
		private final RetwisAction action = RetwisAction.FOLLOW;

		public Follow( Token sessionToken, String followTarget, String username ) {
			this.sessionToken = sessionToken;
			this.followTarget = followTarget;
			this.username = username;
		}

		@Override
		public Token sessionToken() {
			return sessionToken;
		}

		@Override
		public String followTarget() {
			return followTarget;
		}

		@Override
		public String username() {
			return username;
		}

		@Override
		public RetwisAction action() {
			return action;
		}
	}

	class StopFollow implements Action {

		private final Token sessionToken;
		private final String stopFollowTarget;
		private final String username;
		private final RetwisAction action = RetwisAction.STOPFOLLOW;

		public StopFollow( Token sessionToken, String stopFollowTarget, String username ) {
			this.sessionToken = sessionToken;
			this.stopFollowTarget = stopFollowTarget;
			this.username = username;
		}

		@Override
		public Token sessionToken() {
			return sessionToken;
		}

		@Override
		public String stopFollowTarget() {
			return stopFollowTarget;
		}

		@Override
		public String username() {
			return username;
		}

		@Override
		public RetwisAction action() {
			return action;
		}
	}

	class Mentions implements Action {
		private final Token sessionToken;
		private final String mentionsUsername;
		private final RetwisAction action = RetwisAction.MENTIONS;


		public Mentions( Token sessionToken, String mentionsUsername ) {
			this.sessionToken = sessionToken;
			this.mentionsUsername = mentionsUsername;
		}

		@Override
		public Token sessionToken() {
			return sessionToken;
		}

		@Override
		public String mentionsUsername() {
			return mentionsUsername;
		}

		@Override
		public RetwisAction action() {
			return action;
		}
	}

	class Status implements Action {
		private final String statusPostID;
		private final Token sessionToken;
		private final RetwisAction action = RetwisAction.STATUS;

		public Status( Token sessionToken, String statusPostID ) {
			this.sessionToken = sessionToken;
			this.statusPostID = statusPostID;
		}

		@Override
		public String statusPostID() {
			return statusPostID;
		}

		@Override
		public Token sessionToken() {
			return sessionToken;
		}

		@Override
		public RetwisAction action() {
			return action;
		}
	}

	class Logout implements Action {
		private final RetwisAction action = RetwisAction.LOGOUT;
		private final Token sessionToken;

		public Logout( Token sessionToken ) {
			this.sessionToken = sessionToken;
		}

		@Override
		public Token sessionToken() {
			return sessionToken;
		}

		@Override
		public RetwisAction action() {
			return action;
		}
	}

}
