package fr.lemerdy.eric;

import com.mongodb.*;
import org.fest.util.Files;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import static org.fest.assertions.Assertions.assertThat;

public class WithoutRulesTest {

    @BeforeClass
    public static void start_database_as_a_forked_process() throws IOException, InterruptedException {
        File dbPath = ensureDbPathDoesNotExits();
        assertThat(dbPath.mkdir()).isTrue();
        List<String> lines = startMongoDBAsADaemon();
        assertThat(lines.get(0)).startsWith("forked process: ");
        assertThat(lines.get(1)).startsWith("all output going to: ").endsWith("logpath");
        assertThat(lines).hasSize(2);
        assertThatConnectionToMongodbIsPossible();
    }

    private static List<String> startMongoDBAsADaemon() throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("../../mongodb-osx-x86_64-2.0.3/bin/mongod", "--dbpath",
                "dbpath", "--fork", "--logpath", "logpath");
        processBuilder.directory(new File("target"));
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

    private static void assertThatConnectionToMongodbIsPossible() throws InterruptedException, UnknownHostException {
        Mongo server = null;
        try {
            while (server == null) {
                Thread.sleep(250);
                server = new MongoURI("mongodb://127.0.0.1").connect();
            }
            assertThat(server.getDatabaseNames()).hasSize(1);
        } finally {
            server.close();
        }
    }

    private static File ensureDbPathDoesNotExits() throws IOException {
        File dbPath = new File("target/dbpath");
        if (dbPath.exists()) {
            Files.delete(dbPath);
            assertThat(dbPath.exists()).isFalse();
        }
        return dbPath;
    }

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

    @AfterClass
    public static void shutdown_mongodb() throws IOException {
        Mongo mongo = new Mongo();
        try {
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
}