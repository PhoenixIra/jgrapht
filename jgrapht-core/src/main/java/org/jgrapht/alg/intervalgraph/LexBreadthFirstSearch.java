package org.jgrapht.alg.intervalgraph;

import org.jgrapht.*;
import org.jgrapht.traverse.*;

import java.util.*;

public class LexBreadthFirstSearch<V, E>
{
    
    /**
     * Performs a lexicographical BFS starting at {@code startingVertex}.
     *
     * @param graph the graph we want to perform LBFS on
     * @param startingVertex the starting vertex of the LBFS
     * @return an array of vertices representing the order in which the vertices were found
     */
    public static <V, E> HashMap<V, Integer> lexBreadthFirstSearch(Graph<V, E> graph, V startingVertex)
    {
       HashMap<V, Integer> result = new HashMap<>(graph.vertexSet().size());
       LexBreadthFirstIterator<V,E> lbfIterator = new LexBreadthFirstIterator<>(graph, startingVertex);
       
       for(int i = 0; i < graph.vertexSet().size(); i++) {
           result.put(lbfIterator.next(), i);
       }
       
       return result;
    }
    
    /**
     * Performs LBFS+ starting at {@code startingVertex} using the previous ordering {@code prevOrdering}.
     *
     * @param graph the graph we want to perform LBFS on
     * @param startingVertex the starting vertex of the LBFS
     * @param priority the priority of vertices for tiebreaking
     * @return an array of vertices representing the order in which the vertices were found
     */
    
    public static <V, E> HashMap<V, Integer> lexBreadthFirstSearchPlus(Graph<V, E> graph, HashMap<V, Integer> priority)
    {
       HashMap<V, Integer> result = new HashMap<>(graph.vertexSet().size());
       LexBreadthFirstIterator<V, E> lbfIterator = new LexBreadthFirstIterator<>(graph, priority);
       
       for(int i = 0; i < graph.vertexSet().size(); i++) {
           result.put(lbfIterator.next(), i);
       }
       
       return result;
    }
    
    /**
     * Performs LBFS* starting at {@code startingVertex} using two previous orderings {@code prevOrdering1} and {@code prevOrdering2}.
     *
     * @param graph the graph we want to perform LBFS on
     * @param priorityA the first priority of vertices for tiebreaking
     * @param priorityB the second priority of vertices for tiebreaking
     * @return an array of vertices representing the order in which the vertices were found
     */
    
    public static <V, E> HashMap<V, Integer> lexBreadthFirstSearchStar(Graph<V, E> graph, HashMap<V, Integer> priorityA, HashMap<V, Integer> priorityB)
    {
       HashMap<V, Integer> neighborIndexA = new HashMap<>(); 
       HashMap<V, Integer> neighborIndexB = new HashMap<>();
       
       HashMap<V, Set<V>> ASets = new HashMap<>();
       HashMap<V, Set<V>> BSets = new HashMap<>();
       
       for(V vertex : graph.vertexSet()) {
           // Compute indexA, indexB
           int maxNeighborA = 0;
           int maxNeighborB = 0;
           
           List<V> neighbors = Graphs.neighborListOf(graph, vertex);
           neighbors.add(vertex);
           
           for(V neighbor : neighbors) {
               maxNeighborA = Math.max(maxNeighborA, priorityA.get(neighbor));
               maxNeighborB = Math.max(maxNeighborB, priorityB.get(neighbor));
           }
           
           neighborIndexA.put(vertex, maxNeighborA);
           neighborIndexB.put(vertex, maxNeighborB);
       }
       
       for(V vertex : graph.vertexSet()) {
           HashSet<V> Av = new HashSet<>();
           HashSet<V> Bv = new HashSet<>();
           
           for(V neighbor : Graphs.neighborListOf(graph, vertex)) {
               if(priorityA.get(neighbor) < priorityA.get(vertex) && neighborIndexA.get(neighbor) > priorityA.get(vertex)) {
                   Av.add(neighbor);
               }
               
               if(priorityB.get(neighbor) < priorityB.get(vertex) && neighborIndexB.get(neighbor) > priorityB.get(vertex)) {
                   Bv.add(neighbor);
               }
           }
           
           ASets.put(vertex, Av);
           BSets.put(vertex, Bv);
       }
       
       HashMap<V, Integer> result = new HashMap<>(graph.vertexSet().size()); 
       LexBreadthFirstIterator<V, E> lbfIterator = new LexBreadthFirstIterator<>(graph, priorityA, priorityB, neighborIndexA, neighborIndexB, ASets, BSets);
       
       for(int i = 0; i < graph.vertexSet().size(); i++) {
           result.put(lbfIterator.next(), i);
       }
       return result;
    }
}
