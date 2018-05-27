package org.jgrapht.alg.treedecomposition;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;

public class NiceDecompositionBuilder<V>
{
    
    // resulting forest of the decomposition
    private Graph<Integer, DefaultEdge> decomposition;
    
    // map from decomposition nodes to the interval sets
    private Map<Integer,Set<V>> decompositionMap;
    
    // the root of the tree
    private Integer root;
    
    //next integer for vertex generation
    private Integer nextInteger;
    
    /**
     * constructor for all methods used in the abstract method
     */
    public NiceDecompositionBuilder()
    {
        // creating objects
        decomposition = new DefaultDirectedGraph<Integer, DefaultEdge>(DefaultEdge.class);
        decompositionMap = new HashMap<Integer, Set<V>>();
        
        // create root
        root = 0;
        nextInteger = 1;
        decompositionMap.put(root, new HashSet<V>());
        decomposition.addVertex(root);
    }
    
    /**
     * getter for the next free Integer
     * @return unused integer
     */
    private Integer getNextInteger()
    {
        return nextInteger++;
    }

    /**
     * Method for adding a new join node
     * @param toJoin which nodes should get a join node
     * @return the new children of the join node, 
     *         first element has no children, 
     *         second element has the children of toJoin
     */
    public Pair<Integer,Integer> addJoin(Integer toJoin)
    {
        Set<V> vertexSet = null;
        
        // new 
        Integer vertexChildLeft = getNextInteger();
        decomposition.addVertex(vertexChildLeft);
        vertexSet = new HashSet<V>(decompositionMap.get(toJoin));
        decompositionMap.put(vertexChildLeft, vertexSet);
        
        // new current root
        Integer vertexChildRight = getNextInteger();
        decomposition.addVertex(vertexChildRight);
        vertexSet = new HashSet<V>(decompositionMap.get(toJoin));
        decompositionMap.put(vertexChildRight, vertexSet);
        
        // redirect all edges to new parent (should be just one!)
        for(Integer successor : Graphs.successorListOf(decomposition, toJoin))
        {
            decomposition.removeEdge(toJoin,successor);
            decomposition.addEdge(vertexChildRight,successor);
        }
        //make children of parent vertex
        decomposition.addEdge(toJoin, vertexChildLeft);
        decomposition.addEdge(toJoin, vertexChildRight);
        

        return new Pair<Integer,Integer>(vertexChildLeft,vertexChildRight);
    }

    /**
     * Method for adding introducing nodes
     * 
     * @param introducingElement the element, which is introduced
     * @param currentVertex the vertex this element is introduced to
     * @return the next vertex
     */
    public Integer addIntroduce(V introducingElement, Integer currentVertex)
    {
        Set<V> nextVertexSet = new HashSet<>(decompositionMap.get(currentVertex));
        nextVertexSet.add(introducingElement);
        Integer nextVertex = getNextInteger();
        decomposition.addVertex(nextVertex);
        decomposition.addEdge(currentVertex, nextVertex);
        decompositionMap.put(nextVertex, nextVertexSet);

        return nextVertex;
    }

    /**
     * method for adding forget nodes
     * 
     * @param forgettingElement the element, which is forgotten
     * @param currentVertex the vertex this element is forgotten
     * @return the next vertex
     */
    public Integer addForget(V forgettingElement, Integer currentVertex)
    {
        Set<V> nextVertexSet = new HashSet<>(decompositionMap.get(currentVertex));
        nextVertexSet.remove(forgettingElement);
        Integer nextVertex = getNextInteger();
        decomposition.addVertex(nextVertex);
        decomposition.addEdge(currentVertex, nextVertex);
        decompositionMap.put(nextVertex, nextVertexSet);

        return nextVertex;
    }
    
    /**
     * Adds to all current leaves in the decomposition forget nodes until only empty sets are leaves
     */
    public void leafClosure()
    {
        //make leave nodes
        for(Integer leaf : decompositionMap.keySet()) {
            //leaf is not a leaf
            if(Graphs.vertexHasSuccessors(decomposition,leaf))
                continue;
            
            //otherwise add forget until empty set
            Set<V> vertexSet = decompositionMap.get(leaf);
            Integer current = leaf;
            for(V forget : vertexSet) {
                current = addForget(forget, current);
            }
        }
    }
    
    
    /**
     * getter for the decomposition as an directed graph
     * 
     * @return the computed decomposition
     */
    public Graph<Integer, DefaultEdge> getDecomposition()
    {
        return decomposition;
    }

    /**
     * getter for the map from integer nodes of the decomposition to the intervals of the interval
     * graph
     * 
     * @return a nodes to interval map
     */
    public Map<Integer, Set<V>> getMap()
    {
        return decompositionMap;
    }

    /**
     * getter for all roots of the decomposition
     * 
     * @return a set of roots
     */
    public Integer getRoot()
    {
        return root;
    }
}
