/*
 * (C) Copyright 2016-2018, by Ira Justus Fesefeldt and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
package org.jgrapht.alg.decomposition;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;

/**
 * An abstract builder class for nice tree decompositions, which builds the tree decomposition in a
 * top-down manner. <br>
 * A tree decomposition of a graph G is a tree T and a map b:V(T) &rarr; Set&lt;V(G)&gt;, which
 * satisfies the properties:
 * <ul>
 * <li>for every edge e in E(G), there is a node t in V(T) with e is a subset of b(v)</li>
 * <li>for all vertices v in V(G) the set {t &isin; V(T) | v &isin; b(t)} is non-empty and connected
 * in T</li>
 * </ul>
 * <br>
 * A nice tree decomposition is a special tree decomposition, which satisfies the properties:
 * <ul>
 * <li>for root r &isin; V(T) and leaf l &isin; V(T): b(r)=b(t)=&empty;</li>
 * <li>every non-leaf node t &isin; V(T) is of one of the following three types:
 * <ul>
 * <li>forget node: t has exactly one child d and b(t) = b(d) &cup; w for some w &isin; V(G)</li>
 * <li>introduce node: t has exactly one child d and b(t) &cup; w = b(d) for some w &isin;
 * V(G)\b(t)</li>
 * <li>join node: t has exactly two child d_1, d_2 and b(t)=b(d_1)=b(d_2)</li>
 * </ul>
 * </ul>
 * <br>
 * See:<br>
 * <href=https://www.researchgate.net/publication/220896817_Better_Algorithms_for_the_Pathwidth_and_Treewidth_of_Graphs>
 * Bodlaender, Hans &amp; Kloks, Ton. (1991). Better Algorithms for the Pathwidth and Treewidth of
 * Graphs. 544-555. 10.1007/3-540-54233-7_162.</href>
 * 
 * @author Ira Justus Fesefeldt (PhoenixIra)
 *
 * @param <V> the vertices of the graph
 */
abstract public class NiceDecompositionBuilder<V>
{

    // resulting decomposition
    private Graph<Integer, DefaultEdge> decomposition;

    // map from decomposition nodes to the interval sets
    private Map<Integer, Set<V>> decompositionMap;

    // the root of the tree
    private Integer root;

    // next integer for vertex generation
    private Integer nextInteger;

    /**
     * Constructor for all methods used in the abstract method This constructor instantiates the
     * tree of the decomposition, the map from tree vertices to vertex sets and adds the root to the
     * tree.
     */
    protected NiceDecompositionBuilder()
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
     * Getter for the next free Integer Supplies the add vertex methods with new vertices
     * 
     * @return unused integer
     */
    private Integer getNextInteger()
    {
        return nextInteger++;
    }

    /**
     * Method for adding a new join node.<br>
     * {@code toJoin} J0 is copied two times J1 and J2. J0 afterwards becomes the root of the
     * subtree. J1 becomes the successor of J0 and has the successors of J0 as successor. J2 becomes
     * a leaf and the successor of J0. This can be used to make a join node retrospectively to
     * branch of this node. J1 continues the path while J2 adds another path.
     * 
     * @param currentVertex which nodes should get a join node
     * @return the new children of the join node, first element has no children, second element has
     *         the children of toJoin
     */
    protected Pair<Integer, Integer> addJoin(Integer currentVertex)
    {
        Set<V> currentVertexBag = null;

        // new
        Integer vertexChildLeft = getNextInteger();
        decomposition.addVertex(vertexChildLeft);
        currentVertexBag = new HashSet<V>(decompositionMap.get(currentVertex));
        decompositionMap.put(vertexChildLeft, currentVertexBag);

        // new current root
        Integer vertexChildRight = getNextInteger();
        decomposition.addVertex(vertexChildRight);
        currentVertexBag = new HashSet<V>(decompositionMap.get(currentVertex));
        decompositionMap.put(vertexChildRight, currentVertexBag);

        // redirect all edges to new parent (should be just one!)
        for (Integer successor : Graphs.successorListOf(decomposition, currentVertex)) {
            decomposition.removeEdge(currentVertex, successor);
            decomposition.addEdge(vertexChildRight, successor);
        }
        // make children of parent vertex
        decomposition.addEdge(currentVertex, vertexChildLeft);
        decomposition.addEdge(currentVertex, vertexChildRight);

        return new Pair<Integer, Integer>(vertexChildLeft, vertexChildRight);
    }

    /**
     * Method for adding forget nodes. It is only usable if {@code currentVertex} cV is a leaf. It
     * then adds the new forget node I as the child of {@code currentVertex} with the set of
     * {@code currentVertex} plus {@code forgottenElement}.
     * 
     * @param forgottenElement the element, which gets forgotten
     * @param currentVertex the vertex this element gets forgotten to
     * @return the newly created vertex, null and no change if either introducedElement is in the
     *         bag of currentVertex or currentVertex is not a leaf.
     */
    protected Integer addForget(V forgottenElement, Integer currentVertex)
    {
        if (!Graphs.successorListOf(decomposition, currentVertex).isEmpty())
            return null;
        if (decompositionMap.get(currentVertex).contains(forgottenElement))
            return null;

        Set<V> nextVertexBag = new HashSet<>(decompositionMap.get(currentVertex));
        nextVertexBag.add(forgottenElement);
        Integer nextVertex = getNextInteger();
        decomposition.addVertex(nextVertex);
        decomposition.addEdge(currentVertex, nextVertex);
        decompositionMap.put(nextVertex, nextVertexBag);

        return nextVertex;
    }

    /**
     * Method for adding introduce nodes. It is only usable if {@code currentVertex} cV is a leaf.
     * It then adds the new introduce node F as the child of {@code currentVertex} with the set of
     * {@code currentVertex} minus {@code introducedElement}.
     * 
     * @param introducedElement the element, which is introduced
     * @param currentVertex the vertex this element is introduced
     * @return the next vertex, null and no change if either introducedElement is in the bag of
     *         currentVertex or currentVertex is not a leaf.
     */
    protected Integer addIntroduce(V introducedElement, Integer currentVertex)
    {
        if (!Graphs.successorListOf(decomposition, currentVertex).isEmpty())
            return null;
        if (!decompositionMap.get(currentVertex).contains(introducedElement))
            return null;

        Set<V> nextVertexBag = new HashSet<>(decompositionMap.get(currentVertex));
        nextVertexBag.remove(introducedElement);
        Integer nextVertex = getNextInteger();
        decomposition.addVertex(nextVertex);
        decomposition.addEdge(currentVertex, nextVertex);
        decompositionMap.put(nextVertex, nextVertexBag);

        return nextVertex;
    }

    /**
     * Adds to all current leaves in the decomposition forget/introduce nodes until only empty sets
     * are leaves.
     */
    protected void leafClosure()
    {
        Set<Integer> vertices = new HashSet<Integer>(decomposition.vertexSet());
        // make leave nodes
        for (Integer leaf : vertices) {
            // leaf is not a leaf
            if (Graphs.vertexHasSuccessors(decomposition, leaf))
                continue;

            // otherwise add nodes until empty set
            Set<V> vertexSet = decompositionMap.get(leaf);
            Integer current = leaf;
            for (V forget : vertexSet) {
                current = addIntroduce(forget, current);
            }
        }
    }

    /**
     * Returns the tree of the decomposition as an unmodifiable, directed graph
     * 
     * @return the computed decomposition
     */
    public Graph<Integer, DefaultEdge> getDecomposition()
    {
        return new AsUnmodifiableGraph<>(decomposition);
    }

    /**
     * Returns the map from integer nodes of the tree decomposition {@code getDecomposition()} to
     * the intervals of the interval graph as an unmodifiable map
     * 
     * @return a nodes to interval map
     */
    public Map<Integer, Set<V>> getMap()
    {
        return Collections.unmodifiableMap(decompositionMap);
    }

    /**
     * Returns the root of the decomposition {@code getDecomposition()}
     * 
     * @return a set of roots
     */
    public Integer getRoot()
    {
        return root;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return getDecomposition() + "\n " + getMap();
    }
}
