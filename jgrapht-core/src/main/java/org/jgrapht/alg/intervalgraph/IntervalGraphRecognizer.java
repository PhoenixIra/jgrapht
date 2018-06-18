package org.jgrapht.alg.intervalgraph;

import static org.jgrapht.alg.intervalgraph.LexBreadthFirstSearch.*;

import java.lang.reflect.Array;
import java.util.*;

import org.jgrapht.*;
import org.jgrapht.intervalgraph.interval.*;

/**
 * A recognizer for interval graphs.
 *
 * An interval graph is a intersection graph of a set of intervals on the line, i.e. they contain a
 * vertex for each interval and two vertices are connected if the corresponding intervals have a
 * nonempty intersection.
 *
 * The recognizer uses the algorithm described in <a href=
 * "https://webdocs.cs.ualberta.ca/~stewart/Pubs/IntervalSIAM.pdf">https://webdocs.cs.ualberta.ca/~stewart/Pubs/IntervalSIAM.pdf</a>
 * (<i>The LBFS Structure and Recognition of Interval Graphs. SIAM J. Discrete Math.. 23. 1905-1953.
 * 10.1137/S0895480100373455.</i>) by Derek Corneil, Stephan Olariu and Lorna Stewart based on
 * multiple lexicographical breadth-first search (LBFS) sweeps.
 *
 * For this recognizer to work correctly the graph must not be modified during iteration.
 *
 *
 * @param <V> the graph vertex type.
 * @param <E> the graph edge type.
 * @author Team J
 * @since April 2018
 */
public final class IntervalGraphRecognizer<V, E>
{

    /**
     * Stores whether or not the graph is an interval graph.
     */
    private boolean isIntervalGraph;

    /**
     * Stores the computed interval graph representation (or <tt>null</tt> if no such representation
     * exists) of the graph.
     */
    private ArrayList<Interval<Integer>> intervalsSortedByStartingPoint,
        intervalsSortedByEndingPoint;
    private Map<Interval<Integer>, V> intervalToVertexMap;
    private Map<V, IntervalVertexInterface<V, Integer>> vertexToIntervalMap;

    /**
     * Creates (and runs) a new interval graph recognizer for the given graph.
     * 
     * @param graph the graph to be tested.
     */
    @SuppressWarnings({ "unchecked" })
    public IntervalGraphRecognizer(Graph<V, E> graph)
    {
        this.isIntervalGraph = isIntervalGraph(graph);
    }

    /**
     * check if the graph is an interval graph
     *
     * @return
     */
    private boolean isIntervalGraph(Graph<V, E> graph)
    {

        // An empty graph is an interval graph.
        if (graph.vertexSet().isEmpty()) {
            this.intervalsSortedByStartingPoint = new ArrayList<>();
            this.intervalsSortedByEndingPoint = new ArrayList<>();
            return true;
        }

        // Step 1 - LBFS from an arbitrary vertex
        // Input - random vertex r
        // Output - the result of current sweep alpha, further last vertex a visited by current
        // sweep
        HashMap<V, Integer> sweepAlpha =
            lexBreadthFirstSearch(graph, randomElementOf(graph.vertexSet()));

        // Step 2 - LBFS+ from the last vertex of the previous sweep
        // Input - the result of previous sweep alpha, vertex a
        // Output - the result of current sweep beta, further last vertex b visited by current sweep
        HashMap<V, Integer> sweepBeta = lexBreadthFirstSearchPlus(graph, sweepAlpha);

        // Step 3 - LBFS+ from the last vertex of the previous sweep
        // Input - the result of previous sweep beta, vertex b
        // Output - the result of current sweep gamma, further last vertex c visited by current
        // sweep
        HashMap<V, Integer> sweepGamma = lexBreadthFirstSearchPlus(graph, sweepBeta);

        // Step 4 - LBFS+ from the last vertex of the previous sweep
        // Input - the result of previous sweep gamma, vertex c
        // Output - the result of current sweep delta, further last vertex d visited by current
        // sweep
        HashMap<V, Integer> sweepDelta = lexBreadthFirstSearchPlus(graph, sweepGamma);

        // Step 5 - LBFS+ from the last vertex of the previous sweep
        // Input - the result of previous sweep delta, vertex d
        // Output - the result of current sweep epsilon, further last vertex e visited by current
        // sweep
        HashMap<V, Integer> sweepEpsilon = lexBreadthFirstSearchPlus(graph, sweepDelta);

        // Step 6 - LBFS* with the resulting sweeps
        // Input - the result of sweep gamma and sweep epsilon
        // Output - the result of current sweep zeta
        HashMap<V, Integer> sweepZeta = lexBreadthFirstSearchStar(graph, sweepDelta, sweepEpsilon);

        // if sweepZeta is umbrella-free, then the graph is interval.
        // otherwise, the graph is not interval

        if (isIOrdering(sweepZeta, graph)) {

            HashMap<V, Integer> neighborIndex = new HashMap<>();
            for (V vertex : graph.vertexSet()) {
                int maxNeighbor = 0;

                List<V> neighbors = Graphs.neighborListOf(graph, vertex);
                neighbors.add(vertex);

                for (V neighbor : neighbors) {
                    maxNeighbor = Math.max(maxNeighbor, sweepZeta.get(neighbor));
                }

                neighborIndex.put(vertex, maxNeighbor);
            }

            Interval<Integer>[] intervals =
                (Interval<Integer>[]) Array.newInstance(Interval.class, graph.vertexSet().size());
            this.intervalsSortedByStartingPoint = new ArrayList<>(graph.vertexSet().size());
            this.intervalsSortedByEndingPoint = new ArrayList<>(graph.vertexSet().size());

            // Initialize the vertex map. Because we know the number of vertices we can make sure
            // the hashmap does not need to rehash by setting the capacity to the number of vertices
            // divided by the default load factor of 0.75.
            this.intervalToVertexMap =
                new HashMap<>((int) Math.ceil(graph.vertexSet().size() / 0.75));
            this.vertexToIntervalMap =
                new HashMap<>((int) Math.ceil(graph.vertexSet().size() / 0.75));
            Map<Integer,Set<Interval<Integer>>> endpointToInterval = 
                new HashMap<>((int) Math.ceil(graph.vertexSet().size() / 0.75));
            

            // Compute intervals and store them associated by their starting point ...
            for (V vertex : graph.vertexSet()) {
                Interval<Integer> vertexInterval =
                    new Interval<>(sweepZeta.get(vertex), neighborIndex.get(vertex));

                intervals[sweepZeta.get(vertex)] = vertexInterval;

                //save the endpoints in a map to generate a list of intervals sorted by their ending point
                if(endpointToInterval.containsKey(vertexInterval.getEnd()))
                {
                    endpointToInterval.get(vertexInterval.getEnd()).add(vertexInterval);
                }
                else
                {
                    Set<Interval<Integer>> intervalSet = new HashSet<>();
                    intervalSet.add(vertexInterval);
                    endpointToInterval.put(vertexInterval.getEnd(),intervalSet);
                }
                this.intervalToVertexMap.put(vertexInterval, vertex);
                this.vertexToIntervalMap.put(vertex, IntervalVertex.of(vertex, vertexInterval));
            }

            // ... and produce a list sorted by the starting points for an efficient construction of
            // the graph
            for (int i = 0; i < graph.vertexSet().size(); i++) {
                this.intervalsSortedByStartingPoint.add(intervals[i]);
            }
            
            //and a list sorted by the ending points
            for(int i = 0; i <= Collections.max(endpointToInterval.keySet()); i++)
            {
                if(endpointToInterval.containsKey(i))
                {
                    for(Interval<Integer> interval : endpointToInterval.get(i))
                    {
                        this.intervalsSortedByEndingPoint.add(interval);
                    }
                }
            }

            return true;
        } else {
            return false;
        }
    }

    /**
     * Calculates if the given sweep is an I-Ordering (according to the Graph graph) in linear time.
     * 
     * @param <E>
     *
     * @param sweep the order we want to check if its an I-Order
     * @param graph the graph we want to check if its an I-Order
     * @return true, if sweep is an I-Order according to graph
     */
    @SuppressWarnings({ "unchecked" })
    private static <V, E> boolean isIOrdering(HashMap<V, Integer> sweep, Graph<V, E> graph)
    {
        // Compute inverse sweep map to quickly find vertices at given indices
        V[] inverseSweep = (V[]) new Object[graph.vertexSet().size()];

        for (V vertex : graph.vertexSet()) {
            int index = sweep.get(vertex);
            inverseSweep[index] = vertex;
        }
        // Compute maximal neighbors w.r.t. sweep ordering for every vertex
        HashMap<V, V> maxNeighbors = new HashMap<>(graph.vertexSet().size());

        for (V vertex : graph.vertexSet()) {
            List<V> neighbors = Graphs.neighborListOf(graph, vertex);
            V maxNeighbor = vertex;

            for (V neighbor : neighbors) {
                if (sweep.get(neighbor) > sweep.get(maxNeighbor)) {
                    maxNeighbor = neighbor;
                }
            }

            maxNeighbors.put(vertex, maxNeighbor);
        }

        // Check if every vertex is connected to all vertices between itself and its maximal neighbor
        for (V vertex : graph.vertexSet()) {
            int index = sweep.get(vertex);
            int maxIndex = sweep.get(maxNeighbors.get(vertex));

            for (int i = index; i < maxIndex; i++) {
                if (vertex != inverseSweep[i] && !graph.containsEdge(vertex, inverseSweep[i])) {
                    // Found missing edge
                    return false;
                }
            }
        }

        // No missing edge found
        return true;
    }

    /**
     * return a random element of the given set
     *
     * @param set
     * @param <V> the generic type representing vertices
     * @return
     */
    private static <V> V randomElementOf(Set<V> set)
    {
        if (set == null) {
            throw new IllegalArgumentException("Set parameter cannot be null.");
        }

        int index = new Random().nextInt(set.size());
        Iterator<V> iterator = set.iterator();
        for (int i = 0; i < index; i++) {
            iterator.next();
        }
        return iterator.next();
    }

    /**
     * Returns whether or not the graph is an interval graph.
     *
     * @return <tt>true</tt> if the graph is an interval graph, otherwise false.
     */
    public boolean isIntervalGraph()
    {
        return isIntervalGraph;
    }

    /**
     * Returns the list of all intervals sorted by starting point, or null, if the graph was not an
     * interval graph.
     *
     * @return The list of all intervals sorted by starting point, or null, if the graph was not an
     *         interval graph.
     */
    public ArrayList<Interval<Integer>> getIntervalsSortedByStartingPoint()
    {
        return this.intervalsSortedByStartingPoint;
    }

    /**
     * Implementation of radix Sort for integers on Intervals after the ending point
     * 
     * @param list list of intervals to sort
     * @return a new sorted list of the intervals sorted after the ending point
     */
    private ArrayList<Interval<Integer>> radixSortInteger(List<Interval<Integer>> list)
    {
        if(list == null)
            throw new IllegalArgumentException("List parameter cannot be null.");
        
        if(list.isEmpty())
            return new ArrayList<Interval<Integer>>();
        
        ArrayList<Interval<Integer>> positiveList = new ArrayList<Interval<Integer>>(list.size());
        ArrayList<Interval<Integer>> negativeList = new ArrayList<Interval<Integer>>(list.size());
        for (Interval<Integer> interval : list) {
            if (interval.getEnd() < 0)
                negativeList.add(interval);
            else
                positiveList.add(interval);
        }

        positiveList = radixSortNatural(positiveList);
        negativeList = radixSortNatural(negativeList);
        negativeList.addAll(positiveList);
        return negativeList;
    }

    /**
     * Implementation of radix Sort for natural numbers on Intervals after the ending point
     * 
     * @param list list of intervals to sort
     * @return a new sorted list of the intervals sorted after the ending point
     */
    private ArrayList<Interval<Integer>> radixSortNatural(List<Interval<Integer>> list)
    {
        if(list == null)
            throw new IllegalArgumentException("List parameter cannot be null.");
        
        if(list.isEmpty())
            return new ArrayList<Interval<Integer>>();
        
        ArrayList<Interval<Integer>> intervals = new ArrayList<Interval<Integer>>(list);
        ArrayList<Interval<Integer>> intervalsTmp =
            new ArrayList<Interval<Integer>>(intervals.size());

        // init
        for (int i = 0; i < intervals.size(); i++)
            intervalsTmp.add(null);

        Interval<Integer> max =
            Collections.max(intervals, Interval.<Integer> getEndingComparator());
        int power = 1;
        // every digit
        while (max.getEnd() / power > 0) {
            int[] buckets = new int[10];

            // count all numbers with digit at position exponent
            for (int i = 0; i < intervals.size(); i++) {
                int digit = Math.abs((intervals.get(i).getEnd() / power) % 10);
                buckets[digit]++;
            }

            // compute position of digits
            for (int i = 1; i < 10; i++) {
                buckets[i] += buckets[i - 1];
            }

            // sort after digit in intervalsTmp
            for (int i = intervals.size() - 1; i >= 0; i--) {
                int digit = Math.abs((intervals.get(i).getEnd() / power) % 10);
                int position = buckets[digit] - 1;
                buckets[digit] = position;
                intervalsTmp.set(position, intervals.get(i));
            }

            // swap both
            ArrayList<Interval<Integer>> tmp = intervals;
            intervals = intervalsTmp;
            intervalsTmp = tmp;

            power *= 10;
        }
        return intervals;
    }

    /**
     * Returns the list of all intervals sorted by ending point, or null, if the graph was not an
     * interval graph.
     * The List will be created with radix sort from getIntervalsSortedByStartingPoint as soon as
     * this method is called. 
     *
     * @return The list of all intervals sorted by ending point, or null, if the graph was not an
     *         interval graph.
     */
    public ArrayList<Interval<Integer>> getIntervalsSortedByEndingPoint()
    {
        //if (this.intervalsSortedByEndingPoint == null)
        //    this.intervalsSortedByEndingPoint =
        //        radixSortInteger(this.intervalsSortedByStartingPoint);
        return this.intervalsSortedByEndingPoint;
    }

    /**
     * Returns a mapping of the constructed intervals to the vertices of the original graph, or
     * null, if the graph was not an interval graph.
     *
     * @return A mapping of the constructed intervals to the vertices of the original graph, or
     *         null, if the graph was not an interval graph.
     */
    public Map<Interval<Integer>, V> getIntervalToVertexMap()
    {
        return this.intervalToVertexMap;
    }

    /**
     * Returns a mapping of the vertices of the original graph to the constructed intervals, or
     * null, if the graph was not an interval graph.
     *
     * @return A mapping of the vertices of the original graph to the constructed intervals, or
     *         null, if the graph was not an interval graph.
     */
    public Map<V, IntervalVertexInterface<V, Integer>> getVertexToIntervalMap()
    {
        return this.vertexToIntervalMap;
    }
}
