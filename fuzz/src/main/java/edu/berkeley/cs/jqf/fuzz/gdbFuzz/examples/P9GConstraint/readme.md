# Constraint algorithm
**Functionality** \
This algorithm verifies whether after a change of the database the constraint of how many relations there are is maintained. 

Is this a good example? First of all is this a check which is mostly dependent on the update and not on the DB/DB schema. 
****


**Motivation** \ 
The code from which the example is based can be found [here](https://gitlab.com/Novo-Nordisk/nn-public/openstudybuilder/OpenStudyBuilder-Solution/-/blob/main/clinical-mdr-api/clinical_mdr_api/domain_repositories/generic_repository.py?ref_type=heads).
****

**psuedocode example**
```javascript
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
		if previous_item is specific instance (StudySelectionMetadata):
			prev_connected_nodes := all previously connected nodes with the current relationship name
			connected_nodes      := latest assigned version of previously assigned nodes
		else:
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
*Snippet 1: Example pseudocode*
***
The code above can be simplified


**simplified  example**
```javascript
function_1 (
        previous_node := Previous node from which relationships should be maintained
	value := value from which the previous node should be disconnected
	new_node := New node to link the existing relationships
	)
	
	relationships_to_maintain   := relationships connected to previous_node that should be updated 
	value_relationship_name     := relationship name where the target/endpoint node corresponds 
                                        with function parameter _value_
	
	for relationship_name in relationships_to_maintain:
		if previous_node instanceOf <class_1>:
			connected_nodes := latest assigned version of previously assigned nodes
		else:
			connected_nodes := function_2(previous_node, relationship_name, value, True, False)
			
		for cn in connected_nodes:
			add relationship [new_node -- relationship_name --> cn]
	
	# Check cardinality new_item, old_item for all previous relationships 
	getattr(previous_item, relationships_to_maintain) = single
	getattr(new_item, relationships_to_maintain) = single
	
	if value_relationship_name exists
		if value_relationship_name not single
			raise exception
		remove relationship between previous_item and value_node on the value_relationship_name

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
```
*Snippet 2: simplified pseudocode*


**Database states needed** \

The most straightforward DS that can make some coverage is 
- a previous_node with no relationships. The for will never be entered in function 1. The if is not covered
- There is a relationship, but it is not the relationship with the value passed in the function call. The previous node is of the specific class. A new relationship is made between the new node and previously connected node. The last if is not covered
- There is a relationship and it has the value passed in the function call. The previous node is of the specific class. A new relationship is made between the new node and previously connected node. The last if is entered, but no exception is raised
- Same as above and there are 2 relationships with the same relationship value name. The last exception is triggered

Below DS are for function 1 to access function. The node is not of the class type:
- There is a relationship and it has the value passed in the function call. one connected node is returned from function 2
- Same as above and there are 2 relationships with the same relationship value name. function 2 returns 2 nodes and the last exception is triggered

Below DS are for function 2:
- The prev node is of a different class. Since from function 1 the multiple nodes is always true, the last conditionals can never be reached from function 1
- 

**CFG**

Snippet 1 shows an abstracted version of the example in pseudocode.
The 2 functions could be combined in a single version, but would result in less readability due to the nested conditions.
The purpose of the function is to replace the previous node with the new item node, re-referencing all previous relationships.
In the picture below the different decisions in the code are highlighted.

![system.png](..%2F..%2F..%2F..%2F..%2F..%2F..%2F..%2F..%2F..%2F..%2F..%2Fdocs%2Fmedia%2FColored decisions.png)
*Figure 1: Colored decision paths*

The control flow graph can be seen below. The cfg also includes the
![cfg1.png](..%2F..%2F..%2F..%2F..%2F..%2F..%2F..%2F..%2F..%2F..%2F..%2Fdocs%2Fmedia%2Fcfg1.png)
![cfg2.png](..%2F..%2F..%2F..%2F..%2F..%2F..%2F..%2F..%2F..%2F..%2F..%2Fdocs%2Fmedia%2Fcfg2.png)
![cfg3.png](..%2F..%2F..%2F..%2F..%2F..%2F..%2F..%2F..%2F..%2F..%2F..%2Fdocs%2Fmedia%2Fcfg3.png)


