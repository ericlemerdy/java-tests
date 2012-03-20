package fr.lemerdy.eric.rule;

import static junit.framework.Assert.fail;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.util.Files.delete;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.fest.util.Files;
import org.junit.rules.ExternalResource;

import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoURI;

/**
 * Run a mongodb server before each test suite.
 */
public class MongoServerRule extends ExternalResource {
    private String mongodPath = System.getProperty("MONGO_HOME", "../../mongodb-osx-x86_64-2.0.3/bin/mongod");
    private String targetPath = "target";
    private String dbRelativePath = "dbpath";
    private String logRelativePath = "logpath";

    public MongoServerRule() {
    }

    public MongoServerRule(String mongoPath) {
        this.mongodPath = mongoPath;
    }

    public MongoServerRule(MongoServerRuleBuilder builder) {
        this.mongodPath = builder.mongodPath;
        this.targetPath = builder.targetPath;
        this.dbRelativePath = builder.dbRelativePath;
        this.logRelativePath = builder.logRelativePath;
    }

    /**
     * Builder to start mongodb server accordingly to your setup
     */
    public static class MongoServerRuleBuilder {
        private String mongodPath = System.getProperty("MONGO_HOME", "../../mongodb-osx-x86_64-2.0.3/bin/mongod");
        private String targetPath = "target";
        private String dbRelativePath = "dbpath";
        private String logRelativePath = "logpath";

        private MongoServerRuleBuilder() {
        }

        public static MongoServerRuleBuilder newMongoServerRule() {
            return new MongoServerRuleBuilder();
        }

        public MongoServerRuleBuilder mongodPath(String mongodPath) {
            this.mongodPath = mongodPath;
            return this;
        }

        public MongoServerRuleBuilder targetPath(String targetPath) {
            this.targetPath = targetPath;
            return this;
        }

        public MongoServerRuleBuilder dbRelativePath(String dbRelativePath) {
            this.dbRelativePath = dbRelativePath;
            return this;
        }

        public MongoServerRuleBuilder logRelativePath(String logRelativePath) {
            this.logRelativePath = logRelativePath;
            return this;
        }

        public MongoServerRule build() {
            assertThat(mongodPath).isNotNull();
            assertThat(targetPath).isNotNull();
            assertThat(dbRelativePath).isNotNull();
            assertThat(logRelativePath).isNotNull();
            return new MongoServerRule(this);
        }
    }

    @Override
    protected void before() throws Throwable {
        File dbPath = ensureDbPathDoesNotExits();
        assertThat(dbPath.mkdir()).isTrue();
        List<String> lines = startMongoDBAsADaemon();
        assertThat(lines.get(0)).startsWith("forked process: ");
        assertThat(lines.get(1)).startsWith("all output going to: ").endsWith("logpath");
        assertThat(lines).hasSize(2);
        assertThatConnectionToMongodbIsPossible();
    }

    @Override
    protected void after() {
        Mongo mongo = null;
        try {
            mongo = new Mongo();
            DB db = mongo.getDB("admin");
            CommandResult shutdownResult = db.command(new BasicDBObject("shutdown", 1));
            shutdownResult.throwOnError();
            fail("Expecting to loose mongodb connection on shutdown.");
        } catch (Throwable e) {
            assertThat(e.getMessage()).isEqualTo("can't call something : /127.0.0.1:27017/admin");
        } finally {
            mongo.close();
            ensureDbPathDoesNotExits();
        }
    }

    private List<String> startMongoDBAsADaemon() throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(mongodPath, "--dbpath", dbRelativePath, "--fork", "--logpath", logRelativePath);
        processBuilder.directory(new File(targetPath));
        processBuilder.redirectErrorStream(true);
        Process pwd = processBuilder.start();
        BufferedReader outputReader = new BufferedReader(new InputStreamReader(pwd.getInputStream()));
        String output;
        List<String> lines = new ArrayList<String>();
        while ((output = outputReader.readLine()) != null) {
            lines.add(output.toString());
        }
        pwd.waitFor();
        assertThat(pwd.exitValue()).isEqualTo(0);
        return lines;
    }

    private void assertThatConnectionToMongodbIsPossible() throws InterruptedException, UnknownHostException {
        Mongo server = null;
        try {
            while (server == null) {
                TimeUnit.SECONDS.sleep(3);
                server = new MongoURI("mongodb://127.0.0.1").connect();
            }
            assertThat(server.getDatabaseNames()).hasSize(1);
        } finally {
            server.close();
        }
    }

    private File ensureDbPathDoesNotExits() {
        File dbPath = new File(targetPath + "/" + dbRelativePath);
        if (dbPath.exists()) {
            delete(dbPath);
            assertThat(dbPath.exists()).isFalse();
        }
        return dbPath;
    }
}