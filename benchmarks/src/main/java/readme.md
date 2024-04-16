This folder contains example applications which rely on a data source which is retrieved from a Graph Database. The examples have been created specifically for the GFuzz thesis project and are not copied from existing repositories / applications. Instead, the examples have been created from scratch and are motivated by the corresponding readme.md in each subfolder.


The common denominator across these examples is a graph database backend. While the graph DB's are not implemented, the functions and logic of these examples assume it is and the results of the DB calls are which is passed in the function parameters.

Most of the examples have been inspired by one of the 
following items:

- [Transportation](https://github.com/CathiaLH/GraphDatabase-CombinedTransportNetwork) - The problems in transportation tasks rely often on some logic which is based on the data in the graph. More specifically it is using the properties, labels and constraint of the graph to provide for example routes or handle congestions. 
- TODO: Cycle detection
- [Common graph related problems]( https://www.geeksforgeeks.org/top-50-graph-coding-problems-for-interviews ) - These coding challenges consider a graph structured input. The tasks are based on common interview questions. They are therefore not necessarly very ellaborate and are not big applications, but they do show an importance for developers.


The main focus of these examples is to create a broad and distinct range of graph applications which can be expected to be created in the real world. Using those examples, the fuzz method created in this project can be evaluated to give an indication of the potential effectiveness. For a fuzzer to be effective, an application needs to have different branches which are dependent on the data that is in the graph. For this reason, applications like the transportation is suited. There are some applications like ML and visualisation tools which do not have data dependent branching which are not included in the test framework as this is not the intended use.



![system.png](..%2F..%2F..%2F..%2F..%2F..%2F..%2F..%2F..%2F..%2F..%2Fdocs%2Fmedia%2Fsystem.png)
*Figure 1: Example system*

The goal of the fuzzer is to test applications which work with a graph database backend. Such an application will most likely look like a system like in figure 1 above. 
An application has a graph database (green), a query language to interact with said database (blue) and appliction with logic which can get the data through database calls. 
GFuzz's focus is the pink functions of the applications, which contain the application logic. 
The hypothesis is that GFuzz would be able to effectivly traverse the decision branches of these functions as it can generate and mutate realistic database states. One might argue that these function would retrieve different kind of data structures from the database calls. Rather then getting the whole graph, some functions might only get single variables, lists of nodes/edges or tuples. Existing fuzzers already are perfectly capable of generating such data structures. However, as seen in previous works (e.g. RDBMS backend testing, interconnected data testing), fuzzers can benefit from creating the data without the abstracting away the data structures.


### Transportation [1](https://neo4j.com/case-studies/transport-for-london/), [2](https://neo4j.com/case-studies/transport-for-london/)
***
Within transportation the graph which can describe individuals travel paths and routes contain different properties which all should be included in the scheduling.
These properties can be a route which describes the way of travel (e.g. bike, walk, car) or congestion orientated like how busy a public bus is. 
One of the success stories of Neo4J is where it is applied as a twin system to test if/then scenarios, also pointing to a system showing different conditional branches in the system which depenend on the data in the graph (rather than configuration or user input).


### [Financial](https://neo4j.com/case-studies/forsakringskassan/)
***
In this case, a financial institution needs to decide whether benefits are payed while preventing fraud. 
Such a system needs to model a complex network with dependencies between payment decisions where each decision affect each other.
In a large scale implementation, there could be a lot of interdependencies, requiring the graph to have many different connections and dependencies.

### [Supply and logistics]()
***
Eventhough currently mostly used for analysis, a graph system would allow for a extensive suppy inventory management system, requireing to store different product, shipping options, costs, time required and more. This will translate in many different kind of labels and property values

### [Congestion](https://neo4j.com/case-studies/sopra-steria/)
***

### Knowledge graphs [1](https://neo4j.com/case-studies/novo-nordisk/) [2](https://neo4j.com/case-studies/nasa/)
There are already many case studies where extensive knwoledge graphs are built. These graphs require a DB state to have many different kind of properties.

## Ideal examples
The scope is a schema aware-coverage guided fuzzer for applications with a graph DB backend. What does that mean? 
Fuzzer --> an automatic test approach which is based on a continuous loop where an input is generated and fed to an application. Once the application is done processing, the fuzzer selects a new input by mutating a previous one. This new input is again fed to the application. 
Schema aware --> we are aware of the schema of the graph and can therefore generate sensible DB states which we can also mutate
Coverage guided -> When the fuzzers passes an input to the application, the lines of code being executed in the application are being tracked. Each time the application is done, the fuzzers checks whether new lines of code have been executed with the input. If the new input has covered new lines, it might be interesting to mutate on it to see if we can cover more. If the input has not covered any new lines, we might want to select an input which covers other branches, less popular branches or some other guidance heuristic.
What kind of applications do we need then?
-	Applications with a graph DB backend
-	Applications with different execution paths dependent on the data in the graph
-	Different branches present in the application source code and not for example in a library or query language (as this would be a different application and is not in the scope).

It is important that the functions/logic we want to test still depend on the interconnectivity of the data. For example, if the function would call the api/query all hobbies of the users in the graph, and the application function would get a list of string which is used to select different execution paths, existing fuzzers could already test this easily as it would just generate list of strings, eventually covering each hobby.
The functions we test need to have certain logic that depends on either:
-	A graph structure
-	A simple data structure which still has a dependency on the interconnectivity of the data. Due to abstracting away the graph structure and only focusing on the returned data (e.g. list of string). New paths are always never created due to the interdependency of the data. This one is hard to give an example for.

To further describe what kind of graphs can be useful for benchmarking. Like said before, the decision tree has to depend on the graph.
A property graph has the following elements which can be used
- Nodes / edges
- Labels
- Relationships
- Properties
- Property values and types
- Constraints (singleton, mandatory, etc.)

It is important that for example the constraint is assumed, but can be violated even though the schema dictates differently.

## GMARK
***
Gmark schema documentation:
predicates refer to edges/relationships
Types refer to the node labels

The schema then defines a source type--> target type with a symbol (edge) and an edge type which increments with each definition