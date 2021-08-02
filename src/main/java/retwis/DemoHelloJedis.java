package retwis;

import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.samples.retwisj.RetwisRepository;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisShardInfo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DemoHelloJedis {
    public static void main(String[] args) throws IOException {
        String host = "localhost";
        Integer port = 6379;
        try(InputStream input = DemoHelloJedis.class.getClassLoader().getResourceAsStream("redis.properties")) {
            if (input != null) {
                Properties prop = new Properties();
                prop.load(input);
                host = prop.getProperty("redis.host", host);
                port = Integer.parseInt(prop.getProperty("redis.port", port.toString()));
            }
        }
//        Jedis jedis = new Jedis(host,port);
//        jedis.set("foo", "bar");
//        String value = jedis.get("foo");
//        System.out.println(("foo".equals(value)) ? "OK" : "KO");

        JedisShardInfo info = new JedisShardInfo(host,port);
        JedisConnectionFactory factory = new JedisConnectionFactory(info);
        StringRedisTemplate template = new StringRedisTemplate(factory);
        RetwisRepository repo = new RetwisRepository(template);
        System.out.println("> " + repo.addUser("username","password"));
        System.out.println("> " + repo.isUserValid("username"));
    }
}
