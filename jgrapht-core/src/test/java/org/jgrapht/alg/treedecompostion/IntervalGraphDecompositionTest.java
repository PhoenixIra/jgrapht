package org.jgrapht.alg.treedecompostion;

import static org.junit.Assert.*;

import java.util.*;
import java.util.Map.Entry;

import org.jgrapht.*;
import org.jgrapht.alg.treedecomposition.*;
import org.jgrapht.graph.*;
import org.jgrapht.intervalgraph.interval.*;
import org.junit.*;

public class IntervalGraphDecompositionTest
{

    private <V,E,W> void testNiceDecomposition(Graph<V,E> decomposition, Map<V,Set<W>> map, V root){
        
        Queue<V> queue = new LinkedList<V>();
        
        //test and add root
        assertTrue(root+" is no valid root"
                + "\n in decomposition "+decomposition
                + "\n and map"+map, map.get(root).size() == 0);
       queue.add(root);
        
        while(!queue.isEmpty())
        {
            V current = queue.poll();
            List<V> successor = Graphs.successorListOf(decomposition, current);
            if(successor.size() == 0 && map.get(current).isEmpty()) continue; //leaf node
            if(successor.size() == 1) //forget or introduce
            {
                V next = successor.get(0);
                queue.add(next);
                Set<W> union = new HashSet<W>(map.get(current));
                union.addAll(map.get(next));
                if(union.size() == map.get(next).size() || union.size() == map.get(current).size())
                {
                    if(map.get(current).size() == map.get(next).size()-1) continue; //introduce
                    else if(map.get(current).size()-1 == map.get(next).size()) continue; //forget
                }
            }
            if(successor.size() == 2) //join
            {
                V first = successor.get(0);
                V second = successor.get(1);
                queue.add(first);
                queue.add(second);
                Set<W> union = new HashSet<W>(map.get(current));
                union.addAll(map.get(first));
                union.addAll(map.get(second));
                if(union.size() == map.get(current).size() 
                && union.size() == map.get(first).size() 
                && union.size() == map.get(second).size()) 
                    continue; //join node!
            }
            assertFalse("Vertex Set "+current+" is not a valid node for a nice decomposition"
                + "\nin decomposition "+decomposition
                + "\nwith map "+map, true); //no valid node!
        }
        assertTrue(true);
    }
    
    private <V,E,W,F> void testDecomposition(Graph<W,F> oldGraph, Graph<V,E> decomposition, Map<V,Set<W>> map){
    	Set<F> edgeSet = oldGraph.edgeSet();
    	Set<V> vertexSet = decomposition.vertexSet();
    	//Every edge is represented
    	for(F e : edgeSet)
    	{
    		boolean hasVertex = false;
    		for(V v : vertexSet)
    		{
    			if(map.get(v).contains(oldGraph.getEdgeSource(e)) && map.get(v).contains(oldGraph.getEdgeTarget(e))) {
    				hasVertex = true;
    				continue; 
    			}
    		}
    		assertTrue("Edge "+e+" is not found"
    		    + "\nin graph "+decomposition
    		    + "\nwith map "+map, hasVertex);
    	}
    	//every vertex has non-empty connected set of vertex sets
    	Set<W> oldVertexSet = oldGraph.vertexSet();
    	for(W w : oldVertexSet)
    	{
    		Set<V> keySet = new HashSet<V>();
    		for(Entry<V,Set<W>> entry : map.entrySet())
    		{
    			if(entry.getValue().contains(w))
    				keySet.add(entry.getKey());
    		}
    		//not empty
    		assertFalse("Vertex "+w+" is not represented\n in decomposition "+decomposition+"\n and map "+map, keySet.isEmpty());
    		//connected
    		Graph<V,E> subgraph = new AsSubgraph<V,E>(decomposition,keySet);
    		assertTrue("Vertex "+w+" is not connected\n in decomposition "+decomposition+"\n and map "+map,GraphTests.isConnected(subgraph));
    	}
    }
    
    /**
     * Interval Graph representation of the graph to test:
     * _ ___________ ___________
     *    ____________ ___________
     *     _____________ ___________
     *     ...
     */
    @Test
    public void testIntervalgraphDecompositionForRegularGraphs()
    {
        //graph
        Graph<Integer,DefaultEdge> g = new DefaultUndirectedGraph<>(DefaultEdge.class);
        g.addVertex(-1);
        for(int i = 0; i<10; i++)
        	g.addVertex(i);
        
        for(int i = 0; i<10; i++) {
        	for(int j = i+1; j<10; j++) {
        		if((i%2)==0  && (j%2)==0)
        			g.addEdge(i, j);
        		
        		if((i%2)==1 && (j%2)==0 && i<j)
        			g.addEdge(i, j);
        		
        		if((i%2)==1 && (j%2)==1)
        			g.addEdge(i, j);
        	}
        }
        
        //compute decomposition
        IntervalGraphNiceDecomposition<Integer,Integer> decomp = IntervalGraphNiceDecomposition.<Integer,DefaultEdge>create(g);
        assertNotNull("graph was detected as not an interval graph", decomp);
        
        //test for nice decomposition
        testNiceDecomposition(decomp.getDecomposition(), decomp.getMap(), decomp.getRoot());
        testDecomposition(g, decomp.getDecomposition(), decomp.getMap());
    }

    @Test
    public void testIntervalgraphDecompositionForIntervalGraphs()
    {
        //TODO
    }
    
    @Test
    public void testIntervalgraphDecompositionForSortedIntervalLists() 
    {
        //TODO
    }

    /**
     * Test for the create method of lists of intervals
     * Representation:
     * 
     * __ __ __
     *        __
     *         __
     *           ...
     *             __
     *             ___
     *             ____
     *             ...
     */
    @Test
    public void testIntervalgraphDecompositionForIntervalLists()
    {
        List<Interval<Integer>> list = new ArrayList<Interval<Integer>>();
        //unconnected
        list.add(new Interval<Integer>(-4,-3));
        list.add(new Interval<Integer>(-2,-1));
        //just path
        for(int i = 0; i<10; i++)
        {
            list.add(new Interval<Integer>(i,i+1));
        }
        //and to spice it up, a clique
        for(int i = 0; i<10; i++)
        {
            list.add(new Interval<Integer>(10,10+i));
        }
        IntervalGraphNiceDecomposition<Integer,Interval<Integer>> decompalg = IntervalGraphNiceDecomposition.<Integer>create(list);
        Graph<Integer,DefaultEdge> decomp = decompalg.getDecomposition();
        Map<Integer,Set<Interval<Integer>>> map = decompalg.getMap();
        Integer root = decompalg.getRoot();
        testNiceDecomposition(decomp,map,root);
        
    }

}
