# Medical Algorithm
**Structure**
- P1 implementation - this will describe the benchmark program eventually created from the motivating example below
- Source and motivation - references the original program and explains why it is an interesting example
## P1 implementation 
This algorithm verifies whether after a change of the database the constraint of how many relations there are is maintained. 
 
****

## Source and motivation 

The code from which the example is based can be found [here](https://gitlab.com/Novo-Nordisk/nn-public/openstudybuilder/OpenStudyBuilder-Solution/-/blob/main/clinical-mdr-api/clinical_mdr_api/domain_repositories/generic_repository.py?ref_type=heads). This example shows to have different conditionals, depending on the node values and the relationships between the nodes. It also shows a constraint on the amount of relationships allowed, throwing errors of the relationship is not a [single](https://neo4j.com/docs/cypher-manual/current/functions/predicate/#functions-single). 
****

## Source functionality
The function extracted from the source is a function that updates a previous node's relationships to a new node. The previous node should have a relationship with a node with a specific kind of value, which is also passed in the function call. The following steps are performed:
- Collect relationships and nodes
- Update nodes+relationships from previous node to new node if the value corresponds with the value in function call
- Check constraints
- Remove relationship between previous node and value node


**Pseudocode**
```
function_1 (
    previous_node := Previous node from which relationships should be maintained
    value := value from which the previous node should be disconnected
    new_node := New node to link the existing relationships
    )
    
    relationships_to_maintain   := relationships connected to previous_node that should be updated 
    value_relationship_name     := relationship name where the target/endpoint node corresponds 
                                    with function parameter value
    
    connected nodes := nodes connected to the previous node, connected by one of the relationships 
                        in relationships_to_maintain
    
    addRelationships( [new_node -- relationship_name --> connected_nodes] )
    
    # Check cardinality new_item, old_item for all previous relationships 
    isSingle (previous_item, relationships_to_maintain)
    isSingle (new_item, relationships_to_maintain)
    
    
    isSingle (value_relationship_name)
    RemoveRelationship (previous_node -- relationship_name --> value_node) 

	
function_2 (
        previous_node       := Previous item from which relationships should be maintained
        relationship_name   := a relationship in this study connected with the previous_item
        value               := Value  from which the previous item should be disconnected
        multiple_nodes      := False
        at_least_one        := True
	)

	connected_nodes:=  all nodes with the provided relationship
	value_relationship_name := relationship name where the target/endpoint node corresponds with the function 
                                    parameter value
	
	connected_nodes_with_value := nodes where the outgoing node of the relationship with value_relationship_name corresponds with 
	                                function parameter value
	
	if multiple_nodes
		return connected_node_with_study_value
	
	if len(connected_node_with_study_value) > 1
		return error
		
	if at_least_one && len(connected_node_with_study_value) == 0
        return error
		
	return connected_node_with_study_value.first
```
*Snippet 2:  Pseudocode*
***

## Control flow graph

**Simplified source code**
```
function_1 ( 
	previous_item := Previous item from which relationships should be maintained
	value_node := StudyValue node from which the previous item should be disconnected
	new_item := New item to link the existing relationships
	exclude_relationships :=  Excluded relationships to keep because they are maintained (linked)
	)
	
        prev_item_relationships     := all relationships in this study connected with the previous_item
	relationships_to_maintain   := prev_item_relationships and -(exclude_relationships) - value_node
	value_relationship_name     := relationship name where the target/endpoint node corresponds with the function parameter 'value_node'
	
	for relationship_name in relationships_to_maintain: 
	    connected_nodes := function_2(previous_item, relationship_name, value_node True, False)
	    for cn in connected_nodes:
		add relationship [new_item -- relationship_name --> cn]
	
	# Check cardinality new_item, old_item for all previous relationships 
	getattr(previous_item, prev_item_relationships) = single
	getattr(new_item, prev_item_relationships) = single
	
	if value_relationship_name exists
		if value_relationship_name not single
			raise exception
		remove relationship between previous_item and value_node on the value_relationship_name
		
		
		
function_2 (
        previous_item       := Previous item from which relationships should be maintained
        relationship_name   := a relationship in this study connected with the previous_item
        value_node          := StudyValue node from which the previous item should be disconnected
        multiple_nodes      := False
        at_least_one        := True
	)

	connected_nodes:=  all nodes with the provided relationship
	value_relationship_name := relationship name where the target/endpoint node corresponds with the function 
                                    parameter value_node
	
	for cn in connected_nodes:
		connected_values_nodes := nodes where cn is connected by value_relationship_name
		
		for value in connected_values_nodes:
			if value == value_node
				connected_node_with_study_value := connected_node_with_study_value + cn
	
	if multiple_nodes
		return connected_node_with_study_value
	
	if len(connected_node_with_study_value) > 1
		return error
		
	if at_least_one
		if len(connected_node_with_study_value) == 0
			return error
		return connected_node_with_study_value.first
		
	return connected_node_with_study_value.first
```
*Snippet 1: Simplified source code*
***


**CFG**

Snippet 1 shows an abstracted version of the example in pseudocode.
In the picture below the different decisions in the code are highlighted.

![system.png](2F..%2F..%2F..%2F..%2F..%2F..%2F..%2Fdocs%2Fmedia%2Fbenchmarks%2FP9%2FColored decisions.png)
*Figure 1: Colored decision paths*
***

![decision_tree_function1.png](2F..%2F..%2F..%2F..%2F..%2F..%2F..%2Fdocs%2Fmedia%2Fbenchmarks%2FP9%2Fdecision_tree_function1.png)
*Figure 2: Decision tree function 1*

![decision_tree_function2.png](2F..%2F..%2F..%2F..%2F..%2F..%2F..%2Fdocs%2Fmedia%2Fbenchmarks%2FP9%2Fdecision_tree_function2.png)
*Figure 3: Decision tree function 2*

**Database states needed**
In total, 8 different states need to be provided to cover all lines of the 2 functions. If the other function parameters can be modified, this number can be decreased to 2 for function 2, and 5 for function (by changing the value_node).
The following database descriptions should cover the whole program
- **No relationships**
  - A previous_node with no relationships. The for will never be entered in function 1. The last if is also not covered
- **No matching relationship**
  - There is a relationship, but it is not the relationship with the value passed in the function call. 
  - The previous node is of the specific class. 
  - A new relationship is made between the new node and the node which was connected to the previous node. 
  - The last if is not covered as the value relationship name does not exists
- **Duplicate non-matching relationship**
  - same as above and there are 2 relationships with a similar relationship, not corresponding to the relationship value name.
  - The last if is not covered as the value relationship name does not exists
- **Matching relationship**
  - There is a relationship and it has the value passed in the function call. 
  - The previous node is of the specific class. 
  - A new relationship is made between the new node and node connected to the previous node. 
  - The last if is entered, but no exception is raised
- **Duplicate matching relationship**
  - Same as above and there are 2 relationships with the same relationship value name. 
  - The last exception is triggered
- **matching class - function 2 call**
  - The states above can be duplicated where the node is of the specified class. A different approach is used to select the connected nodes this branch always call the function2 with mutliple branches=true, so the function 2 will always return a list of connected nodes


Below DS are for function 2:
- **No node**
  - There is no relationship at all
  - Multiple nodes is disabled
  - One case at_least_one is true, the other false
- **Single node**
  - There is a single relationship where the value of the node corresponds with the passed value
  - multiple nodes is disabled
  - At_least_one is true
- **Multiple nodes**
  - There are 2 relationships where the value of the node corresponds with the passed value
  - multiple nodes is disabled
  - the system throws an error

In the figure below the database states which are needed to cover every branch is visualized.

**TODO:**



## Appendix: Generated CFG

The control flow graph can be seen below. 

![cfg1.png](2F..%2F..%2F..%2F..%2F..%2F..%2F..%2Fdocs%2Fmedia%2Fbenchmarks%2FP9%2Fcfg1.png)

![cfg2.png](2F..%2F..%2F..%2F..%2F..%2F..%2F..%2Fdocs%2Fmedia%2Fbenchmarks%2FP9%2Fcfg2.png)

![cfg3.png](2F..%2F..%2F..%2F..%2F..%2F..%2F..%2Fdocs%2Fmedia%2Fbenchmarks%2FP9%2Fcfg3.png)


