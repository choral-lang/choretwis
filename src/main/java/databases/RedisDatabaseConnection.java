package databases;

import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.samples.retwisj.Range;
import org.springframework.data.redis.samples.retwisj.RetwisRepository;
import org.springframework.data.redis.samples.retwisj.WebPost;
import redis.clients.jedis.JedisShardInfo;
import retwis.DatabaseConnection;
import retwis.Mentions;
import retwis.Post;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class RedisDatabaseConnection  implements DatabaseConnection {

    private final RetwisRepository repo;

    private static final RedisDatabaseConnection INSTANCE = new RedisDatabaseConnection();

    public static RedisDatabaseConnection instance() {
        return INSTANCE;
    }

    public RedisDatabaseConnection() {
        String host = "localhost";
        Integer port = 6379;
        try(InputStream input = RedisDatabaseConnection.class.getClassLoader().getResourceAsStream("redis.properties")) {
            if (input != null) {
                Properties prop = new Properties();
                prop.load(input);
                host = prop.getProperty("redis.host", host);
                port = Integer.parseInt(prop.getProperty("redis.port", port.toString()));
            }
        } catch ( IOException e){

        }
        JedisShardInfo info = new JedisShardInfo(host,port);
        JedisConnectionFactory factory = new JedisConnectionFactory(info);
        StringRedisTemplate template = new StringRedisTemplate(factory);
        this.repo = new RetwisRepository(template);
    }

    @Override
    public List< String > getFollowers(String name ) {
        return repo.getFollowers(repo.findUid(name));
    }

    @Override
    public List< String > getFollowed( String name ) {
        return repo.getFollowing(repo.findUid(name));
    }

    @Override
    public List< Post > getPostsPage( String name, Integer page ) {
        return repo.getPosts(repo.findUid(name), new Range(page)).stream().map(
                webPost -> new Post(webPost.getContent(), webPost.getPid(), webPost.getName())
        ).collect(Collectors.toList());
    }

    @Override
    public void post( String name, String content ) {
        WebPost post = new WebPost();
        post.setContent(content);
        post.setName(name);
        repo.post(name,post);
    }

    @Override
    public void follow( String name, String followTarget ) {
        repo.follow(name,followTarget);
    }

    @Override
    public void stopFollow( String name, String stopFollowTarget ) {
        repo.stopFollowing(name,stopFollowTarget);
    }

    @Override
    public Mentions mentions(String mentionsName, Boolean selfMentions ) {
        return null;
    }

    @Override
    public Post getPost( String postId ) {
        List<WebPost> posts = repo.getPost(postId);
        if (posts.isEmpty()) {
            return null;
        } else {
            WebPost post = posts.get(0);
            return new Post(post.getContent(),postId,post.getName());
        }
    }

    @Override
    public Boolean isUserValid( String username ) {
        return repo.isUserValid(username);
    }

    @Override
    public Boolean isPostValid( String postId ) {
        return repo.isPostValid(postId);
    }

    @Override
    public Boolean isFollower( String name, String followTarget ) {
        return repo.isFollowing(repo.findUid(name),repo.findUid(followTarget));
    }

    @Override
    public void addUser( String name, String pswd ) {
        repo.addUser(name,pswd);
    }

    @Override
    public Boolean auth( String name, String pswd ) {
       return repo.auth(name,pswd);
    }

}
