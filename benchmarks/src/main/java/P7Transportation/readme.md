# Transportation algorithm
**Functionality** \
This algoritm reads the property values of different nodes and edges and depending on the value range takes different paths. 
****


**Motivation** \
The below program has been collected from the following github [repository](https://github.com/CathiaLH/GraphDatabase-CombinedTransportNetwork/blob/main/Res_DataFrames.py)

The repository shows an implementation of a graph-database approach to assess the impact of demand-responsive services on public transit accessibility.
The graph contains nodes with 
- Stop (physical stations) and; 
- Stoptime (stop times)

The relationships: 
- PRECEDED (succession of stops, direction and direction of the train);
- CORRESPONDENCE and; 
- LOCATED_AT (stop time located at a station).

The possibility of moving on foot from a centroid to a station is represented by the WALK relation.
To each relation (= arc in the graph) the inter_time property is assigned: the time necessary to complete the journey represented by the arc. It is this property which will be considered as the cost when searching for a shortest path.

The code reads the data from a neo4j database and passes that to a dataframe. The calls to the DB are defined in specific functions like shortest_path, which are then put in a DataFrame and passed to the applications logic. In which then the nodes are filtered on specific attributes like 
![Sample code](../../../../../docs/media/benchmarks/P7-1.png)
The result is put in a dataframe on which further logic is applied, e.g. counting or selecting different properties dependent on the label:
![Sample code](../../../../../docs/media/benchmarks/P7-2.png)
![Sample code](../../../../../docs/media/benchmarks/P7-3.png)

****
