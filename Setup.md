Had to update [pom.xml](jqf/maven-plugin/pom.xml) of maven_plugin to 3.12.0

```xml
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-plugin-plugin</artifactId>
                <version>3.12.0</version>
            </plugin>
        </plugins>
    </build>
```
***
Build by:

``` bash
mvn package
```

Created a [directory](tutorial/src/main/java/) with the first tutorial. Tutorial can be found [here](https://github.com/rohanpadhye/JQF/wiki/Fuzzing-with-Zest#step-5-fuzz-with-zest)
- [Logic](tutorial/src/main/java/CalendarLogic.java)
- [Test](tutorial/src/test/java/CalendarTest.java)
- [Generator](tutorial/src/main/java/CalendarGenerator.java)

Compile by:
```bash
PATH1="tutorial/src/main/java/"

javac -cp .:$(jqf/scripts/classpath.sh) ${PATH1}CalendarLogic.java ${PATH1}CalendarGenerator.java ${PATH1}CalendarTest.java
```

Run by going into specific directory, and running command below:
```bash
cd tutorial/src/test/java/
java -cp .:$(jqf/scripts/classpath.sh) org.junit.runner.JUnitCore CalendarTest
```


Fuzz with zest:
```bash
../../../../jqf/bin/jqf-zest -c .:$(../../../../jqf/scripts/classpath.sh) CalendarTest testLeapYear
```

A new guidance file has been made [here](jqf/fuzz/src/main/java/edu/berkeley/cs/jqf/fuzz/mo/)
the program can be run with: 
```bash
../../../../jqf/bin/jqf-mo -c .:$(../../../../jqf/scripts/classpath.sh) CalendarTest testLeapYear
```

***
New file with integers:
```
PATH1="tutorial/src/main/java/"
FILE1="int"
javac -cp .:$(jqf/scripts/classpath.sh) ${PATH1}${FILE1}Logic.java ${PATH1}${FILE1}Generator.java ${PATH1}${FILE1}Test.java
```