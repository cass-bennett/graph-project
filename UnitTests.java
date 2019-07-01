// These tests were done using JUnit 4.12 and hamcrest 1.3
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class UnitTests {
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
	Graph myGraph = new Graph("A", System.getProperty("user.dir"));
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
}
