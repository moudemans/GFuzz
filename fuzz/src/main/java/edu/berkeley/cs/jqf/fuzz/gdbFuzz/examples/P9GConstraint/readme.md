# Constraint algorithm
**Functionality** \
This algorithm verifies whether after a change of the database the constraint of how many relations there are is maintained. 

Is this a good example? First of all is this a check which is mostly dependent on the update and not on the DB/DB schema. 
****


**Motivation** \ 
The code from which the example is based can be found [here](https://gitlab.com/Novo-Nordisk/nn-public/openstudybuilder/OpenStudyBuilder-Solution/-/blob/main/clinical-mdr-api/clinical_mdr_api/domain_repositories/generic_repository.py?ref_type=heads).
****

**psuedocode example**
```python

def function_1( 
	previous_item := Previous item from which relationships should be maintained
	value_node := StudyValue node from which the previous item should be disconnected
	new_item := New item to link the existing relationships
	exclude_relationships :=  Excluded relationships to keep because they are maintained (linked)
	)
	
	prev_item_relationships := all relationships with the previous item (used at the end)
	
	study_relationships := all relationships in this study
	
	relationships_to_maintain := study_relationships and -(exclude_relationships) - value_node
	
	value_relationship_name := relationship name where the target node corresponds with the function parameter
	
	for relationship_name, type in relationships to maintain:
		if previous_item is specific instance:
			previous_connected_nodes := all previously connected nodes with the current relationship name
			connected nodes:= latest assigned version of previously assigned nodes
		else:
			connected_nodes := function_2(previous_item, relationship_name, value_node True, False)
			

		for cn in connected_nodes:
			add relationship new_item+relationship_name
	
	#Check cardinality new_item, old_item
	getattr(previous_item, prev_item_relationships) = single
	getattr(new_item, prev_item_relationships) = single
	
	if value_relationship_name exists
		if value_relationship_name not single
			raise exception
		remove relationship between previous_item and value_node on the value_relationship_name
		
		
		
def function(
    node: Any,
    RelationShip: str,
    value_node: Any = None,
    multiple_returned_nodes: bool = False,
    at_least_one_returned: bool = True,
	):

	connected_nodes:=  all nodes with the provided relationship
	value_relationship_name := relationship name where the target node corresponds with the function parameter
	
	for cn in connected_nodes:
		connected_values_nodes := nodes where cn is connected trhoug by value_relationship_name
		
		for value in connected_values_nodes:
			if value == value node
				connected_node_with_study_value := connected_node_with_study_value + cn
	
	if multiple_returned_nodes
		return connected_node_with_study_value
	
	if len(connected_node_with_study_value) > 1
		return error
		
	if at_least_one_returned
		if len(connected_node_with_study_value) == 0
			return error
		return connected_node_with_study_value.first
		
	return connected_node_with_study_value.first
	
```