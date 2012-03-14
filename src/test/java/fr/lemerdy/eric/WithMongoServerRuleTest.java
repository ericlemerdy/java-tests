package fr.lemerdy.eric;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.ClassRule;
import org.junit.Test;

import com.mongodb.DB;
import com.mongodb.Mongo;

import fr.lemerdy.eric.rule.MongoServerRule;

/**
 * {@link MongoServerRule} starts the mongo db server before all the tests are run, then stopped after all tests are run.
 */
public class WithMongoServerRuleTest {
    @ClassRule
    public static MongoServerRule mongoServer = new MongoServerRule();

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
