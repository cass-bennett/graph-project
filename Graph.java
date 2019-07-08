import org.jsoup.Jsoup;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Scanner;
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
     * When a Node is found, the corresponding String is removed from the
     * input set. 
     * <p>
     * In other words, after this method is run, the returned set
     * should contain all Nodes whose names were contained in strs,
     * and strs should contain only the Strings for which no corresponding
     * Node was found.
     * @param  strs  a HashSet of strings to find within the graph
     * @return       a HashSet of Nodes found whose names are in strs
     */
    HashSet<Node> findNodesRemoveStrings(HashSet<String> strs) {
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
     * Adds edges between fromNode and all Nodes with names matching the
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
	HashSet<Node> addNodes = findNodesRemoveStrings(addSet);

	// For Nodes that aren't in the Graph, make new ones
	for(String s : addSet)
	    addNodes.add(new Node(s));

	// ==========================================================
	// Currently the following section finds the distance by reading a
	// frequency table of words from a file.
	// Forthcoming is the functionality to read frequency tables from
	// the web if the files don't yet exist on the hard drive.
	// ==========================================================
	
	// Find the distances of the Nodes and add them as edges
	Hashtable<String,Integer> fromTable
	    = readTableFromFile(fromNode.getName());
	for(Node n : addNodes) {
	    Hashtable<String,Integer> t = readTableFromFile(n.getName());
	    double dist = computeDistance(fromTable,t);
	    fromNode.addEdge(n,dist);
	    n.addEdge(fromNode,dist);
	}
	// ==========================================================
    }

    /**
     * Takes two frequency tables of all the words in an article and
     * computes the distance between them, in the following way:
     * for each word that occurs in either article, take the difference in the
     * number of occurrences between the two articles, then square that
     * difference, then add all the differences together and return the
     * square root of that sum
     */
    static double computeDistance( Hashtable<String,Integer> t1,
				   Hashtable<String,Integer> t2 ) {
	double subtotal = 0;
	for( Entry<String,Integer> e1 : t1.entrySet() ) {
	    Integer other = t2.get( e1.getKey() );
	    if( other == null )
		other = 0;
	    subtotal += (e1.getValue() - other) * (e1.getValue() - other);
	}
	for( Entry<String,Integer> e2 : t2.entrySet() ) {
	    if( ! t1.containsKey( e2.getKey() ) )
		subtotal += (e2.getValue() * e2.getValue());
	}
	return Math.sqrt(subtotal);
    }
    
    public void writeToFile() {
	try{
	    FileOutputStream fileOut =
		new FileOutputStream(FILEPATH + "/graph.ser");
	    ObjectOutputStream out = new ObjectOutputStream(fileOut);
	    out.writeObject(this);
	    out.close();
	    fileOut.close();
	} catch(IOException e) {
	    e.printStackTrace();
	}
    }
    
    public static Graph readFromFile(String dirPath) {
	Graph g = null;
	try {
	    FileInputStream fileIn =
		new FileInputStream(dirPath + "/graph.ser");
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
    
    void writeTableToFile(String fileName, Hashtable<String,Integer> table) {
	try{
	    FileWriter fileOut = new FileWriter(FILEPATH + "/" + fileName);
	    fileOut.write(String.valueOf(table.size()));
	    for(Entry<String,Integer> e : table.entrySet())
		fileOut.write("\n" + e.getValue() + " " + e.getKey());
	    fileOut.close();
	} catch(IOException e) {
	    e.printStackTrace();
	}
    }
    
    Hashtable<String,Integer> readTableFromFile(String fileName) {
	Hashtable<String,Integer> table = null;
	try {
	    File f = new File(FILEPATH + "/" + fileName);
	    if(f.exists()) {
		Scanner sc = new Scanner(f);
		table = new Hashtable<String,Integer>((sc.nextInt()*4)/3+1);
		while(sc.hasNext()) {
		    int freq = sc.nextInt();
		    sc.skip(" ");
		    String word = sc.nextLine();
		    table.put(word,freq);
		}
		sc.close();
	    }
	}catch(IOException e)  {
	    e.printStackTrace();
	}
	return table;
    }
}
