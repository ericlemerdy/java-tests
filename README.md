# MongoDB Rules

Eric le Merdy @ericlemerdy wrote a blog about mongo db integration tests

http://eric.lemerdy.free.fr/dotclear/index.php?post/2012/03/11/Start-and-stop-mongodb-with-Junit-in-java

I wanted to play with junit @rule https://github.com/KentBeck/junit/blob/master/src/main/java/org/junit/Rule.java 
and see if you could do a rule for a class.

Turns out it's possible, and pretty straightforward using @ClassRule https://github.com/KentBeck/junit/blob/master/src/main/java/org/junit/ClassRule.java

# Example

Using rules you end up with the following kind of code:

``` java
public class WithMongoServerRuleBuilderAndClientServerRuleTest {
    @ClassRule
    public static MongoServerRule mongoServer = new MongoServerRule();
    @Rule
    public MongoClientRule mongo = new MongoClientRule("test");

    @Test
    public void should_connect_to_the_test_database() throws Exception {
        assertThat(mongo.getDatabase().getCollectionNames()).isNotNull();
    }
}
``` 

which is strictly equivalent to the following code:

``` java
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
```

# Builder

Just for the fun of it, I added a little builder to setup your mongodb install

``` java
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
```
