# Transportation algorithm
**Functionality** \
The applications reads the property value from the edges, expecting specific properties dependent on the edge label.

Function descriptions: \
**getTimesFirstStation** \
Sum the time values stored in edge properties where the edge label is ‘WALK’ or ‘DRT’

**getFirstStation** \
Return the first end location where the edge label is either ‘WALK’ or ‘DRT’

**getTransport** \
Return the transportation method from node. If both methods are available return ‘DRT/WALK’


```
getTimesFirstStation(Graph g):
    inter_times:= 0
    waiting_times:= 0
    DRT_time:= 0
    walking_time:= 0
    
    for edge e:
        if e.label == DRT
            inter_times += e.inter_times
            waiting_times += e.waiting_times
            DRT_time += e.DRT_time
        if e.label == WALK
            walking_time += e.walking_time    
    return times
    
getFirstStation(Graph g):
    for edge e:
        if e.label == DRT
            return e.to
        if e.label == WALK
            return e.to
                 
getTransport(Graph g):
    transport_methods:= []
    transport_count:= 0
    for edge e:
        if e.label == DRT or e.label == WALK:
            transport_methods += e.label
            transport_count += 1
            
    if transport_count == 2:
        return "DRT/WALK"
    else:
        return transport_methods.first()
        
```
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
