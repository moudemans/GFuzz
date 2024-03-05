# Label algorithm
**Functonality** \
This algorithm goes over every edge and looks for a cycle in the graph. The cycle has to be at least of size x in order to execute further logic. 
****


**Motivation** \
The algorithm has been inspired by the [common graph problems in 2024](
fuzz/src/main/java/edu/berkeley/cs/jqf/fuzz/gdbFuzz/fuzz-input/seed.txt). Most of the existing cycle algorithm's are less prone to bugs as it is usually based on going over edges/nodes without a lot of code logic or branching. Depending on the size of a cycle
****
