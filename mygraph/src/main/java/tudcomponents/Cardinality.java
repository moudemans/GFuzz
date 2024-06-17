package tudcomponents;

public enum Cardinality {
    // https://docs.janusgraph.org/v0.3/basics/schema/
    MULTI, // Allows multiple edges of the same label between any pair of vertices.
    SIMPLE, // Allows at most one edge of such label between any pair of vertices.
    MANY2ONE, // Allows at most one outgoing edge of such label on any vertex in the graph but places no constraint on incoming edges.
    ONE2MANY, // Allows at most one outgoing edge of such label on any vertex in the graph but places no constraint on incoming edges.
    ONE2ONE, // Allows at most one incoming and one outgoing edge of such label on any vertex in the graph.
    NxM
}
