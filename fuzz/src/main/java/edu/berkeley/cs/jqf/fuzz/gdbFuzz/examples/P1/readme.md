# Detect cycle algorithm
**Functionality** \
This algorithm goes over every edge and looks for a cycle in the graph. The cycle has to be at least of size x in order to execute further logic. 
****


**Motivation** \
The algorithm has been inspired by the [common graph problems in 2024]( https://www.geeksforgeeks.org/top-50-graph-coding-problems-for-interviews ). Most of the existing cycle algorithm's are less prone to bugs as it is usually based on going over edges/nodes without a lot of code logic or branching. 

Addtionally, most graph DB engines also offer some built in function, usually including cycle deetection. For example, in [neo4j](https://neo4j.com/labs/apoc/4.1/overview/apoc.nodes/apoc.nodes.cycles/). 
In [Gremlin](https://github.com/apache/tinkerpop/blob/master/docs/src/recipes/cycle-detection.asciidoc), it is already less straight forward and besides having a complicated query it is not that easily to extend the algorithm with extra constraints


****
