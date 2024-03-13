# Graph API framework for fuzz testing
***
This file describes the minimum requirements for a graph API to perform fuzz testing. 
These requirements are based on the functions and interactions described by the benchmark applications and the functionalities provided in popular query languages (Cypher and Gremlin).
Nodes (entities) and edges (relationships) will not be listed seperatly as these are trivial

### Benchmark functionalities

Starting with P9, shows it needs to be able to reference nodes, relationships and values. 
The relationships have a name and the nodes have a value. 
Having a specific node, it should be able to get all the relationships/edges going from that node.
Also, it needs to have a function to collect all nodes connected to a provided node with a certain relationship.
Lastly, it should be able to be able to call .single(), which returns true if exactly one element matches the constraint provided.
The library used by this example is NeoModel, which is the implementation of neo4j for python.


### Gremlin functionalities


### Neo4j/Cypher functionalities
All features built into Neo4J are described in the manual of its query language: [Cypher](https://neo4j.com/docs/cypher-manual/3.5/).

The language can be catogorized in the following types:
- **Property types** - (int, string, boolean, date, list,  etc.)
- **Structural types** - (nodes, relationships, paths)
- **Composite types** - (list and maps of other type categories)

Cypher has an expressive [set of clauses](https://neo4j.com/docs/cypher-manual/3.5/clauses/) in order to query data and modify the DB. 
We can abstract away all the clauses by just looking at the return values of said queries. 
While the front end application will most likely still construct queries, how the queries are processed is not the concern of the application. It only needs to consider all the possible result values. 
This is covered in each driver implementation specifically, discussed later in this file.

Cypher also provides built-in [functions](https://neo4j.com/docs/cypher-manual/3.5/functions/). Similar to the  clauses, we only have to consider the possible outputs.

Cypher provides also some support for a [graph schema](https://neo4j.com/docs/cypher-manual/3.5/schema/), described as indecis and constraints. The indexes or labels on nodes, properties and relationships already discussed above. 
The possible constraints are:
- **Unique property constraints** - property values are unique for all nodes with a specific label
- **Property existence constraints**  (enterprise edition) - property exists for all nodes/relationships with a specific label
- **Node Keys**  - given label and set of properties:
  - All the properties exist on all the nodes with that label.
  - The combination of the property values is unique.

#### Java Driver 
Looking at the [java driver](https://neo4j.com/docs/api/java-driver/current/), there are ~3100 functions which can be called. 
This includes duplicate functions due to support for different typing or inheritance, but the amount would be infeasible to include all. 
Instead, some packages should be omitted. below is a list of existing packages. the packages that will be removed are not expected to provide different outcomes/functionalities (e.g. only providing wrappers, run configurations and different programming paradigms).

- ~~org.neo4j.driver.async~~ - provide means to run queries, transactions, session, results asynchronously
- ~~org.neo4j.driver.exceptions~~ - exceptions related to DB configurations, permissions and connections
- ~~org.neo4j.driver.net~~ - Functionalities that provide access over the internet 
- ~~org.neo4j.driver.reactive~~ - wrapper for reactive functionality
- ~~org.neo4j.driver.reactivestreams~~ - wrapper for reactive functionality
- ~~org.neo4j.driver.summary~~ - Analytics information about query execution


Leaving the following packages to go into further:
- org.neo4j.driver
- org.neo4j.driver.exceptions.value
- org.neo4j.driver.types
- org.neo4j.driver.util


## Proposed test Framework
From the collected features across benchmarks, query languages and research, the following framework is proposed.

**Nodes and Relationships** \
Each discussed paradigm uses an implementation of nodes and edges/relationships. Therefore, the graph can be described by a set of nodes and relationships:

G := {N: set of nodes} \
N := {l: Label, P: set of properties (key,Value), E: set of relationships}
E := {l: Label, a: first node, b: second node}

Typing is required in the examples and supported in cypher, typing will has to be included in the api framework. 
The datatypes included in this framework will either be the primitive types in [java](https://www.w3schools.com/java/java_data_types.asp), or the types supported in the [Neo4J driver for java](https://neo4j.com/docs/api/java-driver/current/org.neo4j.driver/org/neo4j/driver/types/package-summary.html).




