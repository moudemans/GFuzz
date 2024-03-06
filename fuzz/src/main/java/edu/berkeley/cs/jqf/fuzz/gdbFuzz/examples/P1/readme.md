# Detect cycle algorithm
**Functionality** \
This algorithm goes over every edge and looks for a cycle in the graph. The cycle has to be at least of size x in order to execute further logic. 
****


**Motivation** \
The algorithm has been inspired by the [common graph problems in 2024]( https://www.geeksforgeeks.org/top-50-graph-coding-problems-for-interviews ). Most of the existing cycle algorithm's are less prone to bugs as it is usually based on going over edges/nodes without a lot of code logic or branching. 

Addtionally, most graph DB engines also offer some built in function, usually including cycle detection. 
For example, in [neo4j](https://neo4j.com/labs/apoc/4.1/overview/apoc.nodes/apoc.nodes.cycles/) where the nodes.cycles function allows for a cycle detection on a specified subset of nodes. Additionally, this query can be extended with relationship types and a maximum depth. 
In [Gremlin](https://github.com/apache/tinkerpop/blob/master/docs/src/recipes/cycle-detection.asciidoc), it is already less straight forward and requiring a complicated query. 

These implementations provide a mean to query straightforward cycles. But if a user would like to make a cycle detection algorithm relying on a more complex constraint, this will have to be executed by a different application.
****

**Example**
...
