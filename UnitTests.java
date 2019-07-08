// These tests were done using JUnit 4.12 and hamcrest 1.3
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class UnitTests {
    // name of the directory for tests to store files in
    final String FP = "unit-test-files";
    
    @Test
    public void twoNodeCycle() {
	Node yin = new Node("yin");
	Node yang = new Node("yang");
	yin.addEdge(yang, 3.1415926535);
	yang.addEdge(yin, 3.1415926535);
	for(Entry<Node,Double> e : yin.asEdges()) {
	    assertSame(e.getKey() , yang);
	    assertEquals(e.getValue().doubleValue() , 3.1415926535, 0.0001);
	}
	assertTrue(yang.hasEdgeTo(yin));
	assertFalse(yang.hasEdgeTo(new Node("abc")));
	assertFalse(yang.hasEdgeTo(null));
    }

    @Test
    public void addEdgesToSingleNode() {
	Graph myGraph = new Graph("A", FP + "/add-edges-single-node");
	HashSet<String> mySet = new HashSet<String>(Arrays.asList("B","C"));
	myGraph.addEdges(myGraph.root, mySet);
	List<Node> myList = myGraph.root.asNodes();
	assertEquals(myList.size() , 2);
	Node n1 = myList.get(0);
	Node n2 = myList.get(1);
	assertTrue((n1.getName().equals("B") && n2.getName().equals("C"))
		   ||(n1.getName().equals("C") && n2.getName().equals("B")));
	assertTrue(n1.hasEdgeTo(myGraph.root));
	assertTrue(n2.hasEdgeTo(myGraph.root));
    }

    @Test
    public void computeDistances() {
    	Hashtable<String,Integer> t1 = new Hashtable<String,Integer>();
    	Hashtable<String,Integer> t2 = new Hashtable<String,Integer>();
	assertEquals(Graph.computeDistance(t1,t2) , 0 , 0.00000000001);

	t1.put("a",2);
	t1.put("b",3);
	t1.put("c",5);
	t2.put("c",5);
	t2.put("b",3);
	t2.put("a",2);
	assertEquals(Graph.computeDistance(t1,t2) , 0 , 0.00000000001);
	//     "a"  "b"  "c"
	//t1    2    3    5
	//t2    2    3    5

	t1.compute("b",(k,v) -> 6);
	t2.compute("c",(k,v) -> 7);
	t1.put("d",2);
	t2.put("e",3);
	//     "a"  "b"  "c"  "d"  "e"
	//t1    2    6    5    2
	//t2    2    3    7         3
	//diff  0   +3   -2   +2   -3
	// 3² + (-2)² + (+2)² + (-3)² = 26 ; √(26) ≈ 5.099019513592785
	assertEquals(Graph.computeDistance(t1,t2)
		     , 5.099019513592785
		     , 0.00000000001);
    }

    @Test
    public void tableFileIO() {
	Graph g = new Graph("root", FP );
	Hashtable<String,Integer> t = new Hashtable<String,Integer>();
	t.put("a",1);
	t.put("b",2);
	t.put("31258719235879k13 12309 123098102936   123",311111);
	t.put("七転び八起き",78);
	t.put("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZqwertyuiop[]asdfghjkl;'zxcvbkmé,.",2);
	g.writeTableToFile("t",t);
	assertEquals(t,g.readTableFromFile("t"));
	File f = new File(FP + "/t");
	f.delete();
	assertEquals(null,g.readTableFromFile("t"));
    }

}
