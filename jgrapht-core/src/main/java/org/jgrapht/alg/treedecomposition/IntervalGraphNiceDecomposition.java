package org.jgrapht.alg.treedecomposition;

import java.util.*;
import org.jgrapht.*;
import org.jgrapht.alg.intervalgraph.IntervalGraphRecognizer;
import org.jgrapht.intervalgraph.*;
import org.jgrapht.intervalgraph.interval.*;

/**
 * Class for calculating the nice Tree Decomposition for interval graphs
 *
 * @param <T> the value type of the intervals of the interval graph
 * @param <V> the type of the nodes of the input graph
 *
 * @author Ira Justus Fesefeldt (PhoenixIra)
 * @since Mai 14, 2018
 */
public class IntervalGraphNiceDecomposition<T extends Comparable<T>, V> extends NiceDecompositionBuilder<V>
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
     */
    private IntervalGraphNiceDecomposition(
        List<Interval<T>> sortedByStartPoint, List<Interval<T>> sortedByEndPoint,
        Map<Interval<T>, V> intervalToVertexMap, Map<V, Interval<T>> vertexToIntervalMap)
    {
        super();
        
        this.startSort = sortedByStartPoint;
        this.endSort = sortedByEndPoint;
        this.intervalToVertexMap = intervalToVertexMap;
        this.vertexToIntervalMap = vertexToIntervalMap;
        
        computeNiceDecomposition();
    }

    /**
     * Factory method for creating a nice tree decomposition for interval graphs. This factory
     * method uses general graphs and the IntervalGraphRecognizer to generate a list of intervals
     * sorted by starting and ending points. The complexity of this method depends on the sorting
     * algorithm in IntervalGraphRecognizer (probably quasi linear in the number of intervals)
     * 
     * @param graph the graph which should transformed to an interval graph and then into a
     *        corresponding nice tree decomposition
     * @param <V> the vertex type of the graph
     * @param <E> the edge type of the graph
     * @return the algorithm for the computation of the nice tree decomposition, returns null if
     *         graph was no interval graph
     * @see IntervalGraphRecognizer
     */
    public static <V, E> IntervalGraphNiceDecomposition<Integer, V> create(Graph<V, E> graph)
    {
        IntervalGraphRecognizer<V, E> recog = new IntervalGraphRecognizer<>(graph);
        
        HashMap<V, Interval<Integer>> vertexToIntegerMap =
            new HashMap<>(recog.getVertexToIntervalMap().size());
        
        Map<V,IntervalVertexInterface<V, Integer>> vertexToIntervalVertexMap = recog.getVertexToIntervalMap();
        for (V key : vertexToIntervalVertexMap.keySet())
            vertexToIntegerMap.put(key, vertexToIntervalVertexMap.get(key).getInterval());
        
        if (recog.isIntervalGraph())
            return new IntervalGraphNiceDecomposition<Integer, V>(
                recog.getIntervalsSortedByStartingPoint(),
                recog.getIntervalsSortedByEndingPoint(), recog.getIntervalToVertexMap(),
                vertexToIntegerMap);
        else
            return null;
    }

    /**
     * Factory method for creating a nice tree decomposition for interval graphs. This factory
     * method extracts the intervals from the interval graph and uses them as an input for the
     * computation. The complexity of this method depends on the sorting algorithm of ArrayList
     * (probably O(|V| log(|V|))
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
        T extends Comparable<T>> IntervalGraphNiceDecomposition<T, V> create(
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
        return new IntervalGraphNiceDecomposition<T, V>(
            startSort, endSort, intervalToVertexMap, vertexToIntervalMap);
    }

    /**
     * Factory method for creating a nice tree decomposition for interval graphs. This factory
     * method needs to lists of intervals, the first sorted after starting points, the second after
     * ending points The complexity of this method is linear in the number of intervals
     * 
     * @param sortedByStartPoint a list of all intervals sorted by the starting point
     * @param sortedByEndPoint a list of all intervals sorted by the ending point
     * @param <T> the value of the intervals
     * @return the algorithm for the computation of the nice tree decomposition
     */
    public static <T extends Comparable<T>> IntervalGraphNiceDecomposition<T, Interval<T>> create(
        List<Interval<T>> sortedByStartPoint, List<Interval<T>> sortedByEndPoint)
    {
        HashMap<Interval<T>, Interval<T>> identity = new HashMap<>(sortedByStartPoint.size());
        for (Interval<T> interval : sortedByStartPoint)
            identity.put(interval, interval);
        return new IntervalGraphNiceDecomposition<T, Interval<T>>(
            new ArrayList<Interval<T>>(sortedByStartPoint),
            new ArrayList<Interval<T>>(sortedByEndPoint), identity, identity);
    }

    /**
     * Factory method for creating a nice tree decomposition for interval graphs. This factory
     * method needs to lists of intervals, which then is sorted by ArrayList.sort(). The complexity
     * of this method depends on the sorting Algorithm of ArrayList (probably O(|List| log(|List|))
     * 
     * @param intervals the (unsorted) list of all intervals
     * @param <T> the values of the intervals
     * @return the algorithm for the computation of the nice tree decomposition
     * @see ArrayList#sort(Comparator)
     */
    public static <T extends Comparable<T>> IntervalGraphNiceDecomposition<T, Interval<T>> create(
        List<Interval<T>> intervals)
    {
        ArrayList<Interval<T>> startSort = new ArrayList<>(intervals);
        ArrayList<Interval<T>> endSort = new ArrayList<>(intervals);
        startSort.sort(Interval.<T> getStartingComparator());
        endSort.sort(Interval.<T> getEndingComparator());
        return create(startSort, endSort);
    }

    /**
     * Main method for computing the nice tree decomposition
     */
    private void computeNiceDecomposition()
    {

        // create all objects and set new root
        currentVertex = getRoot();

        int endIndex = 0;

        // as long as intervals remain
        for (Interval<T> current : startSort) {
            // first forget until you need to introduce new nodes
            while (endSort.get(endIndex).getEnd().compareTo(current.getStart()) < 0) {
                V forgetElement = intervalToVertexMap.get(endSort.get(endIndex));
                currentVertex = addForget(forgetElement, currentVertex);
                endIndex++;
            }            
            V introduceElement = intervalToVertexMap.get(current);
            currentVertex = addIntroduce(introduceElement, currentVertex);
        }
        // add the last forget nodes
        while (endIndex < endSort.size()) {
            V forgetElement = intervalToVertexMap.get(endSort.get(endIndex++));
            currentVertex = addForget(forgetElement, currentVertex);
        }
    }

    /**
     * getter for interval to vertex Map
     * 
     * @return a map that maps intervals to vertices
     */
    public Map<Interval<T>, V> getIntervalToVertexMap()
    {
        return intervalToVertexMap;
    }

    /**
     * getter for vertex to interval map
     * 
     * @return a map that maps vertices to intervals
     */
    public Map<V, Interval<T>> getVertexToIntervalMap()
    {
        return vertexToIntervalMap;
    }
}
