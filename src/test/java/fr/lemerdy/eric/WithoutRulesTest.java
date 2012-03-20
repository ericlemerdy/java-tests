package fr.lemerdy.eric;

import com.mongodb.DB;
import com.mongodb.Mongo;
import fr.lemerdy.eric.norule.MongoServerTest;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class WithoutRulesTest extends MongoServerTest {

    @Test
    public void should_connect_to_the_test_database() throws Exception {
        Mongo mongo = new Mongo();
        try {
            DB test = mongo.getDB("test");
            assertThat(test.getCollectionNames()).isNotNull();
        } finally {
            mongo.close();
        }
    }
    
}
