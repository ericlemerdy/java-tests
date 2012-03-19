package fr.lemerdy.eric;

import static fr.lemerdy.eric.rule.MongoServerRule.MongoServerRuleBuilder.newMongoServerRule;
import static org.fest.assertions.Assertions.assertThat;

import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import fr.lemerdy.eric.rule.MongoClientRule;
import fr.lemerdy.eric.rule.MongoServerRule;

/**
 * {@link MongoServerRule} starts the mongo db server before all the tests are run, then stopped after all tests are run.
 * <p>
 * {@link MongoClientRule} will connect to the given mongo database before each method, then close the connection after each test.
 */
@Ignore("Mongo server path is dependant of the system that passes the tests.")
public class WithMongoServerRuleAndClientServerRuleTest {
    @ClassRule
    public static MongoServerRule mongoServer = newMongoServerRule() //
            .mongodPath("c:\\dev\\mongodb\\bin\\mongodb.exe") //
            .targetPath("target/mongo-temp") //
            .dbRelativePath("db") //
            .logRelativePath("log") //
            .build();
    @Rule
    public MongoClientRule mongo = new MongoClientRule("test");

    @Test
    public void should_connect_to_the_test_database() throws Exception {
        assertThat(mongo.getDatabase().getCollectionNames()).isNotNull();
    }
}
