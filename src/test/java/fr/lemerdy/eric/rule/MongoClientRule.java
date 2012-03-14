package fr.lemerdy.eric.rule;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.mongodb.DB;
import com.mongodb.Mongo;

/**
 * Connect to a given mongo database before each test, then close the connection afterwards
 */
public class MongoClientRule implements TestRule {
    final String databaseName;

    private DB database;

    public MongoClientRule(String databaseName) {
        this.databaseName = databaseName;
    }

    public DB getDatabase() {
        return database;
    }

    public Statement apply(Statement base, Description description) {
        return new MongoClientStatement(base);
    }

    public class MongoClientStatement extends Statement {
        private final Statement statement;

        public MongoClientStatement(Statement base) {
            statement = base;
        }

        @Override
        public void evaluate() throws Throwable {
            Mongo mongo = null;
            try {
                mongo = new Mongo();
                database = mongo.getDB(databaseName);
                statement.evaluate();
            } finally {
                if (mongo != null) {
                    mongo.close();
                }
            }
        }
    }

}
