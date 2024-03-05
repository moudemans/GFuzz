# GraphFuzz
The work found in this directory is made for the Master Thesis Project about automated testing of graph processing applications.

# How to run
Be sure the project is built correctly, e.g. 
```bash
mvn package
```
---
The driver for the automated testing can be found in GFuzzDriver.java file. Run the main method to start the program.
The program expects some program arguments, listed below
```json
<application class name>
<Method name inside the listed class that should be run>
<Output directory>
<Seed file>
```
Below is an example of how to run the first application:
```json
edu.berkeley.cs.jqf.fuzz.gdbFuzz.examples.P1.P1Driver
test1
fuzz-results
fuzz/src/main/java/edu/berkeley/cs/jqf/fuzz/gdbFuzz/seed.txt
```

# Contribute
In order to add new examples, new applications can be added to the examples directory. A new application needs to have a driver for the application. The driver class needs to be annotated with @RunWith(JQF.class), and the method that is to be run needs to be annotated with @Fuzz. Example:

```java
@RunWith(JQF.class)
public class ExampleClass {
    @Fuzz
    public void test1(String fileName) throws IOException {
        ...
    }

```
The driver should take the filename on which it is to be run in order for the fuzzer to pass an input. 