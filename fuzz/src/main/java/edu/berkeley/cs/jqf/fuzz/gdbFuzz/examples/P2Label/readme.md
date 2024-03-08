# Label algorithm
**Functionality** \
This algorithm applies specific logic depending on the available properties in the graph. Depending on the property present, different methods are called.
****

**Motivation** \
This algorithm is inspired by the availability of properties and therefore the available search space of databases states.
Graph databases like Neo4J are praised for their flexibility and the freedom to start implementing and changing without a schema.
It is therefore not unlickely you get similar datapoints with different formats (e.g. date in different timezones, different metric systems, Languages, postal codes, etc.), without proper DB management.

Additionally, there are examples where the application using the graph DB needs to retrieve the nodes and perform specific logic dependent on the kind of node and kind of connection. 
One of such examples could be a transportation applications which needs to know what kind of station it is starting from and what kind of travel methods are available from there [1](https://github.com/CathiaLH/GraphDatabase-CombinedTransportNetwork/blob/main/Res_DataFrames.py).
Another example where the labels of nodes are used to make a sub-selection is [here](https://github.com/grapheco/InteractiveGraph/blob/master/src/main/typescript/mainframe.ts).




****

**Example** 
This example recreates the different execution paths depending on the avaialble labels present in the graph. Both the node labels and outgoing edge labels determine the path, resulting in different branches and methods to be explored.
The example uses a relative small graph where the first node passes is started from. The node labels can be seen as location indicators and the labels on the edges can be seen as possible travel methods. A larger logistic graph would most likely have a more dynamic starting point, but this should not mather as it is randomly selected/generated.

