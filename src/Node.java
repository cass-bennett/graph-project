import java.lang.Iterable;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

interface NodeIterable {
    Iterable<Node> asNodes();
    Iterable<SimpleImmutableEntry<Node,Double>> asEdges();
}

public class Node implements NodeIterable{
    final String name;
    private ArrayList<SimpleImmutableEntry<Node,Double>> edges;
    
    public Node(String s) {
	name = s;
	edges = new ArrayList<SimpleImmutableEntry<Node,Double>>();
    }

    public String getName() {
	return name;
    }

    void addEdge(Node node, double dist) {
	edges.add( new SimpleImmutableEntry<Node,Double>(node, dist) );
    }

    public boolean hasEdgeTo( Node n ) {
	for ( SimpleImmutableEntry<Node,Double> e : edges )
	    if ( e.getKey() == n )
		return true;
	return false;
    }

    public List<SimpleImmutableEntry<Node,Double>> asEdges() {
	return edges;
    }
    public List<Node> asNodes() {
	return edges.stream().map(e -> e.getKey()).collect(Collectors.toList());
    }
}
