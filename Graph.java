import org.jsoup.Jsoup;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Stack;
import java.util.stream.Collectors;

public class Graph implements java.io.Serializable {
    public static final long serialVersionUID = -509660807948666307L;
    Node root;
    private int numNodes;
    public final String FILEPATH;
    
    // The pattern to be used to match against words in paragraphs
    static final Pattern parPattern = Pattern.compile("\\w(('|-|\\.)?\\w)*");
    static final Pattern linkPattern = Pattern.compile("https://en.wikipedia.org/wiki/[^:\\#/\\s]*");
    
    public Graph( String rootName, String fp ) {
	root = new Node( rootName );
	numNodes = 0;
	FILEPATH = fp;
    }

    /**
     * Searches the graph for all Nodes whose names are contained within a set
     * of Strings.
     * <p>
     * Be aware that this method removes found Strings from the input set.
     * So after this method is run, the returned Set should contain all Nodes
     * whose names were contained in strs, and strs should contain only the
     * Strings for which no corresponding Node was found.
     * @param  strs  a set of strings to find within the graph
     * @return       a set of Nodes found whose names are in strs
     */
    HashSet<Node> findNodes(HashSet<String> strs) {
	Node currentNode;
	HashSet<Node> visited = new HashSet<Node>();
	HashSet<Node> ret = new HashSet<Node>();
	// a stack to keep track of the nodes we still need to visit
    	Stack<Node> nodeStack = new Stack<Node>();
    	nodeStack.push(root);
    	do {
	    currentNode = nodeStack.pop();
	    visited.add(currentNode);
	    if(strs.contains(currentNode.getName())) {
		ret.add(currentNode);
		strs.remove(currentNode.getName());
	    }
	    nodeStack.addAll(currentNode.asNodes().stream()
			     .filter(x -> ! visited.contains(x))
			     .collect(Collectors.toList()));
	} while( ! nodeStack.empty() && ret.size() < strs.size() );
	return ret;
    }

    /**
     * Adds an edge between fromNode and all Nodes with names matching the
     * Strings in addSet, if those edges don't already exist.
     * <p>
     * This method will draw edges between Nodes that already exist in the
     * Graph when possible, and add new Nodes to the Graph otherwise.
     * @param fromName the name of the Node to which new edges are being added
     * @param addSet   a set of Strings that correspond to the names of Nodes
     *                 that we're adding edges to
     */
    void addEdges(Node fromNode, HashSet<String> addSet){
	// Remove names of Nodes for which the edge already exists
	for(Node n : fromNode.asNodes())
	    addSet.remove(n.getName());

	// Find Nodes that are in the Graph but don't have an edge to fromNode
	HashSet<Node> addNodes = findNodes(addSet);

	// For Nodes that aren't in the Graph, make new ones
	for(Node n : addNodes)
	    addSet.remove(n.getName());
	for(String s : addSet)
	    addNodes.add(new Node(s));

	/* ==========================================================
	 * This will have a part where it figures out the distances
	 * for each Node, but for now it uses 1.0 as a placeholder.   */
	for(Node n : addNodes) {
	    fromNode.addEdge(n,1.0);
	    n.addEdge(fromNode,1.0);
	}
	// ==========================================================
    }

    public void writeToFile( String fileName ) {
	try{
	    FileOutputStream fileOut =
		new FileOutputStream(fileName);
	    ObjectOutputStream out = new ObjectOutputStream(fileOut);
	    out.writeObject(this);
	    out.close();
	    fileOut.close();
	} catch(IOException e) {
	    e.printStackTrace();
	}
    }
    public static Graph readFromFile( String fileName ) {
	Graph g = null;
	try {
	    FileInputStream fileIn = new FileInputStream(fileName);
	    ObjectInputStream in = new ObjectInputStream(fileIn);
	    g = (Graph) in.readObject();
	    in.close();
	    fileIn.close();
	}catch(IOException e)  {
	    e.printStackTrace();
	}catch(ClassNotFoundException e)  {
	    e.printStackTrace();
	}
	return g;
    }

}
