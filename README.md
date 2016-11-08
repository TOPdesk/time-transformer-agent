<img align="right" src="/timetransformer.png?raw=true" alt="TimeTranformer logo">
The `TimeTransformer` is a Java agent to manipulate the time returned by `System.currentTimeMillis()` and `System.nanoTime()`. See also [the example project](https://github.com/TOPdesk/time-transformer-examples).

# Usage:
## In unit tests:
1. Add the time-transformer-agent.jar to your lib folder or add it as a test dependency.

  **Maven example:**
  ```maven
  <dependency>
      <groupId>com.topdesk</groupId>
      <artifactId>time-transformer-agent</artifactId>
      <version>1.0.0</version>
      <scope>test</scope>
  </dependency>
  ```

2. In your unit tests: set the desired `Time` implementation, this example uses the built-in `TransformingTime`, and manipulate it. Don't forget to clean up after yourself!

  **Example:**

  ```java
  @Test
	public void testFiveMinutesAgo() {
		try {
			TimeTransformer.setTime(TransformingTime.INSTANCE);
			long time = 1_000_000_000l;
			TransformingTime.INSTANCE.stopTimeAt(time);
			assertEquals(time - 5 * 60 * 1000, TimeUtils.fiveMinutesAgo());
		}
		finally {
			TimeTransformer.setTime(DefaultTime.INSTANCE);
		}
	}
  ```

3. Add the following line to the command you use to run your tests:
```
-javaagent:/path/to/time-transformer-agent-1.0.0.jar
```

  **Examples:**

  *Maven:*
  ```maven
  <build>
    <plugins>
      <plugin>
        <!-- Goal that sets a property pointing to the artifact file for each project dependency. -->
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-dependency-plugin</artifactId>
        <executions>
          <execution>
            <goals>
              <goal>properties</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <configuration>
          <argLine>-javaagent:${com.topdesk:time-transformer-agent:jar}</argLine>
        </configuration>
      </plugin>
    </plugins>
  </build>
  ```

  *Eclipse:*

  It is also possible to use the TimeTransformer in the Eclipse JUnit runner. Edit your Run configuration -> *Arguments* ->  *VM arguments* -> `-javaagent:/path/to/time-transformer-agent-1.0.0.jar`

# How the TimeTransformer works:
The `TimeTransformer` uses bytecode weaving to replace all calls to `System.currentTimeMillis()` and `System.nanoTime()` for calls to `TimeTransformer.currentTimeMillis()` and `TimeTransformer.nanoTime()`. Bytecode weaving (a.k.a. bytecode instrumentation, or just in time bytecode manipulation) is the process of modifying the Java bytecode when a class is loaded by a ClassLoader.

The interceptor class `TimeTransformer` delegates all calls to an implementation of the `Time` interface. You can set your own implementation of the `Time` interface or use one of the two defaults: `DefaultTime` or `TransformingTime`.

Implementation detail: the default implementation of `TimeTransformer.nanoTime()` returns `System.currentTimeMillis() * 1_000_000`.

# Contributing:
By adding your name to the `AUTHORS` file, you accept that your changes will become public under the license specified in the `LICENSE` file.
