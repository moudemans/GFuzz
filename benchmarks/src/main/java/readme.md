This folder contains example applications which rely 
data retrieved from a graph database. Most applications 
used have an neo4j backend. The
examples have been modified s.t. they can be used for 
the PGFuzz testing. Functionality is similar to the 
original programs, but needed extraction from an 
extensive framework or had to be re-written from python 
to java.

The main focus of these examples is to create a broad and
distinct range of graph applications which can be expected
to be created in the real world. Using those examples, the
fuzz method created in this project can be evaluated to give
an indication of the potential effectiveness. For a fuzzer
to be effective, an application needs to have different
branches which are dependent on the data that is in the
graph. There are some applications like ML and
visualisation tools which do not have data dependent
branching which are not included in the test framework as
this is not the intended use.



## Ideal examples

The scope is a schema aware-coverage guided fuzzer for
applications with a graph DB backend. What does that mean?
Fuzzer --> an automatic test approach which is based on a
continuous loop where an input is generated and fed to an
application. Once the application is done processing, the
fuzzer selects a new input by mutating a previous one. This
new input is again fed to the application.
Schema aware --> we are aware of the schema of the graph and
can therefore generate sensible DB states which we can also
mutate
Coverage guided -> When the fuzzers passes an input to the
application, the lines of code being executed in the
application are being tracked. Each time the application is
done, the fuzzers checks whether new lines of code have been
executed with the input. If the new input has covered new
lines, it might be interesting to mutate on it to see if we
can cover more. If the input has not covered any new lines,
we might want to select an input which covers other
branches, less popular branches or some other guidance
heuristic.
What kind of applications do we need then?

- Applications with a graph DB backend
- Applications with different execution paths dependent on
  the data in the graph
- Different branches present in the application source code
  and not for example in a library or query language (as
  this would be a different application and is not in the
  scope).

It is important that the functions/logic we want to test
still depend on the interconnectivity of the data. For
example, if the function would call the api/query all
hobbies of the users in the graph, and the application
function would get a list of string which is used to select
different execution paths, existing fuzzers could already
test this easily as it would just generate list of strings,
eventually covering each hobby.
The functions we test need to have certain logic that
depends on either:

- A graph structure
- A simple data structure which still has a dependency on
  the interconnectivity of the data. Due to abstracting away
  the graph structure and only focusing on the returned
  data (e.g. list of string). New paths are always never
  created due to the interdependency of the data. This one
  is hard to give an example for.

To further describe what kind of graphs can be useful for
benchmarking. Like said before, the decision tree has to
depend on the graph.
A property graph has the following elements which can be
used

- Nodes / edges
- Labels
- Relationships
- Properties
- Property values and types
- Constraints (singleton, mandatory, etc.)

It is important that for example the constraint is assumed,
but can be violated even though the schema dictates
differently.