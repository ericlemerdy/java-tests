package fr.lemerdy.eric.rule;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.mongodb.Mongo;

/**
 * Create a Mongo client before each test, then close the connection afterwards
 */
public class MongoClientRule implements TestRule {

    private Mongo mongo;

    public Mongo getMongo() {
        return mongo;
    }

    public Statement apply(Statement statement, Description description) {
        return new MongoClientStatement(statement);
    }

    public class MongoClientStatement extends Statement {
        private final Statement statement;

        public MongoClientStatement(Statement statement) {
            this.statement = statement;
        }

        @Override
        public void evaluate() throws Throwable {
            try {
                mongo = new Mongo();
                statement.evaluate();
            } finally {
                if (mongo != null) {
                    mongo.close();
                }
            }
        }
    }
}
