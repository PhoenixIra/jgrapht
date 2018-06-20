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
import org.jgrapht.alg.intervalgraph.IntervalGraphRecognizer;
import org.jgrapht.intervalgraph.*;
import org.jgrapht.intervalgraph.interval.*;

/**
 * Class for calculating the nice tree decomposition for interval graphs. It iterates over the
 * intervals of the graph. For every starting interval, an introduce node is added and for every
 * ending interval, a forget node is added. The resulting decomposition has exactly one introduce
 * node and one forget node for every vertex and zero join node (thus it is a path decomposition).
 * The complexity of this class depends on the input.
 * <p>
 * For more information about interval graphs see {@link IntervalGraphRecognizer} and for more
 * information about nice tree decompositions see {@link NiceDecompositionBuilder}.
 * <p>
 * The time complexity of this algorithm is bounded by the number of generates nodes. We have one
 * root node, exactly one introduce node for every vertex and one forget node for every vertex. We
 * do not have any join nodes, thus we have in total exactly $2|V|+1$ nodes, which sets are bounded
 * by the set of the nearest ancestor forget node n with $N(n)$ vertices. Thus the time complexity
 * of this algorithm is in $\mathcal{O}(|V|+|E|)$.
 *
 * @param <T> the value type of the intervals of the interval graph
 * @param <V> the type of the nodes of the input graph
 *
 * @author Ira Justus Fesefeldt (PhoenixIra)
 */
public class IntervalGraphNiceDecompositionBuilder<T extends Comparable<T>, V>
    extends
    NiceDecompositionBuilder<V>
{
    // input to the algorithm, list of sorted intervals
    private List<Interval<T>> startSort, endSort;
    private Map<Interval<T>, V> intervalToVertexMap;
    private Map<V, Interval<T>> vertexToIntervalMap;

    // helper attributes
    private Integer currentVertex = null;

    /**
     * Private constructor for the factory methods, which changes the inputs
     * 
     * @param sortedByStartPoint the intervals sorted after the starting points
     * @param sortedByEndPoint the intervals sorted after the ending points
     * @param intervalToVertexMap maps intervals to the vertices of the graph (may be the same)
     * @param vertexToIntervalMap maps vertices of the graph to the intervals (may be the same)
     */
    private IntervalGraphNiceDecompositionBuilder(
        List<Interval<T>> sortedByStartPoint, List<Interval<T>> sortedByEndPoint,
        Map<Interval<T>, V> intervalToVertexMap, Map<V, Interval<T>> vertexToIntervalMap)
    {
        super();

        this.startSort = sortedByStartPoint;
        this.endSort = sortedByEndPoint;
        this.intervalToVertexMap = intervalToVertexMap;
        this.vertexToIntervalMap = vertexToIntervalMap;
    }

    /**
     * Factory method for creating a nice tree decomposition for interval graphs. This factory
     * method uses general graphs and the IntervalGraphRecognizer to generate a list of intervals
     * sorted by starting and ending points. The complexity of this method is in
     * $\mathcal{O}(|V|+|E|)$.
     * 
     * @param graph the graph which should transformed to an interval graph and then into a
     *        corresponding nice tree decomposition
     * @param <V> the vertex type of the graph
     * @param <E> the edge type of the graph
     * @return the algorithm for the computation of the nice tree decomposition, returns null if
     *         graph was no interval graph
     * @see IntervalGraphRecognizer
     */
    public static <V, E> IntervalGraphNiceDecompositionBuilder<Integer, V> create(Graph<V, E> graph)
    {
        IntervalGraphRecognizer<V, E> recog = new IntervalGraphRecognizer<>(graph);

        HashMap<V, Interval<Integer>> vertexToIntegerMap =
            new HashMap<>(recog.getVertexToIntervalMap().size());

        Map<V, IntervalVertexInterface<V, Integer>> vertexToIntervalVertexMap =
            recog.getVertexToIntervalMap();
        for (V key : vertexToIntervalVertexMap.keySet())
            vertexToIntegerMap.put(key, vertexToIntervalVertexMap.get(key).getInterval());

        if (!recog.isIntervalGraph())
            return null;
        IntervalGraphNiceDecompositionBuilder<Integer,
            V> builder = new IntervalGraphNiceDecompositionBuilder<Integer, V>(
                recog.getIntervalsSortedByStartingPoint(), recog.getIntervalsSortedByEndingPoint(),
                recog.getIntervalToVertexMap(), vertexToIntegerMap);
        builder.computeNiceDecomposition();
        return builder;
    }

    /**
     * Factory method for creating a nice tree decomposition for interval graphs. This factory
     * method extracts the intervals from the interval graph and uses them as an input for the
     * computation. The complexity of this method depends on the sorting algorithm of ArrayList:
     * $\mathcal{O}(|V| log(|V|)$.
     * 
     * @param intervalGraph the input for which a nice tree decomposition should be computed
     * @param <V> the IntervalVertex Type of the interval graph
     * @param <E> the edge type of the interval graph
     * @param <VertexType> the vertex type of the graph
     * @param <T> the value of the intervals
     * @return the algorithm for the computation of the nice tree decomposition
     * @see ArrayList#sort(Comparator)
     */
    public static <V extends IntervalVertexInterface<VertexType, T>, E, VertexType,
        T extends Comparable<T>> IntervalGraphNiceDecompositionBuilder<T, V> create(
            IntervalGraph<V, E, VertexType, T> intervalGraph)
    {
        Set<V> vertexSet = intervalGraph.vertexSet();
        List<Interval<T>> intervals = new ArrayList<Interval<T>>(vertexSet.size());
        Map<Interval<T>, V> intervalToVertexMap = new HashMap<>(vertexSet.size());
        Map<V, Interval<T>> vertexToIntervalMap = new HashMap<>(vertexSet.size());
        for (V iv : vertexSet) {
            intervals.add(iv.getInterval());
            intervalToVertexMap.put(iv.getInterval(), iv);
            vertexToIntervalMap.put(iv, iv.getInterval());
        }
        ArrayList<Interval<T>> startSort = new ArrayList<>(intervals);
        ArrayList<Interval<T>> endSort = new ArrayList<>(intervals);
        startSort.sort(Interval.<T> getStartingComparator());
        endSort.sort(Interval.<T> getEndingComparator());
        IntervalGraphNiceDecompositionBuilder<T, V> builder =
            new IntervalGraphNiceDecompositionBuilder<T, V>(
                startSort, endSort, intervalToVertexMap, vertexToIntervalMap);
        builder.computeNiceDecomposition();
        return builder;
    }

    /**
     * Factory method for creating a nice tree decomposition for interval graphs. This factory
     * method needs to lists of intervals, the first sorted after starting points, the second after
     * ending points. This method does not check if the two lists are sorted or if they have the
     * same intervals. If these conditions does not apply, this algorithm behaves arbitrary. The
     * complexity of this method is in $\mathcal{O}(|Intervals|)$.
     * 
     * @param sortedByStartPoint a list of all intervals sorted by the starting point
     * @param sortedByEndPoint a list of all intervals sorted by the ending point
     * @param <T> the value of the intervals
     * @return the algorithm for the computation of the nice tree decomposition or null if the
     *         listed are not sorted
     */
    public static <
        T extends Comparable<T>> IntervalGraphNiceDecompositionBuilder<T, Interval<T>> create(
            List<Interval<T>> sortedByStartPoint, List<Interval<T>> sortedByEndPoint)
    {
        HashMap<Interval<T>, Interval<T>> identity = new HashMap<>(sortedByStartPoint.size());
        for (Interval<T> interval : sortedByStartPoint) {
            identity.put(interval, interval);
        }
        return new IntervalGraphNiceDecompositionBuilder<T, Interval<T>>(
            new ArrayList<Interval<T>>(sortedByStartPoint),
            new ArrayList<Interval<T>>(sortedByEndPoint), identity, identity);
    }

    /**
     * Factory method for creating a nice tree decomposition for interval graphs. This factory
     * method needs to lists of intervals, which then is sorted by ArrayList.sort(). The complexity
     * of this method depends on the sorting Algorithm of ArrayList: $\mathcal{O}(|List|
     * log(|List|))$
     * 
     * @param intervals the (unsorted) list of all intervals
     * @param <T> the values of the intervals
     * @return the algorithm for the computation of the nice tree decomposition
     * @see ArrayList#sort(Comparator)
     */
    public static <
        T extends Comparable<T>> IntervalGraphNiceDecompositionBuilder<T, Interval<T>> create(
            List<Interval<T>> intervals)
    {
        ArrayList<Interval<T>> startSort = new ArrayList<>(intervals);
        ArrayList<Interval<T>> endSort = new ArrayList<>(intervals);
        startSort.sort(Interval.<T> getStartingComparator());
        endSort.sort(Interval.<T> getEndingComparator());
        return create(startSort, endSort);
    }

    /**
     * Main method for computing the nice tree decomposition. Iterates over the starting points and
     * ending points of the intervals from lowest to highest point. If a starting point is found, we
     * made a forget node and if an ending point is found, we made an introduce node. By this we
     * create the path decomposition top-down from the root to the leaf (although introduce/forget
     * is bottom-up defined)
     */
    private void computeNiceDecomposition()
    {

        // starting with the root of the tree decomposition
        currentVertex = getRoot();

        // current ending point to watch for
        int endIndex = 0;

        // iterate over the starting points from lowest to highest
        for (Interval<T> current : startSort) {
            // iterate over the ending points from lowest to highest until reaching current start
            // point
            while (endSort.get(endIndex).getEnd().compareTo(current.getStart()) < 0) {
                // add for every ending point an introduce node
                V introducedElement = intervalToVertexMap.get(endSort.get(endIndex));
                currentVertex = addIntroduce(introducedElement, currentVertex);

                // next ending point
                endIndex++;
            }
            // add for every starting point a forget node
            V forgottenElement = intervalToVertexMap.get(current);
            currentVertex = addForget(forgottenElement, currentVertex);
        }
        // add the last introduce nodes
        leafClosure();
    }

    /**
     * Returns the interval to vertex Map
     * 
     * @return a map that maps intervals to vertices
     */
    public Map<Interval<T>, V> getIntervalToVertexMap()
    {
        return intervalToVertexMap;
    }

    /**
     * Returns the vertex to interval map
     * 
     * @return a map that maps vertices to intervals
     */
    public Map<V, Interval<T>> getVertexToIntervalMap()
    {
        return vertexToIntervalMap;
    }
}
