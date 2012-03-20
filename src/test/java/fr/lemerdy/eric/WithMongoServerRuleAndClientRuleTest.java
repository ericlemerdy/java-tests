package fr.lemerdy.eric;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import fr.lemerdy.eric.rule.MongoClientRule;
import fr.lemerdy.eric.rule.MongoServerRule;

/**
 * {@link MongoServerRule} starts the mongo db server before all the tests are run, then stopped after all tests are run.
 * <p>
 * {@link MongoClientRule} will connect to the given mongo database before each method, then close the connection after each test.
 */
public class WithMongoServerRuleAndClientRuleTest {
    
    @ClassRule
    public static MongoServerRule mongoServer = new MongoServerRule();
    
    @Rule
    public MongoClientRule mongo = new MongoClientRule();

    @Test
    public void should_connect_to_the_test_database() throws Exception {
        assertThat(mongo.getMongo().getDB("test").getCollectionNames()).isNotNull();
    }
}
