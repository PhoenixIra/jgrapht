/*
 * (C) Copyright 2018-2018, by Timofey Chudakov and Contributors.
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
package org.jgrapht.traverse;

import org.jgrapht.Graph;
import org.jgrapht.GraphTests;
import org.jgrapht.Graphs;

import java.util.*;

/**
 * A lexicographical breadth-first iterator for an undirected graph.
 * <p>
 * Every vertex has an implicit label (they aren't used explicitly in order to reduce time and
 * memory complexity). When some vertex is returned by this iterator, its index is the number of
 * vertices in this graph minus number of already returned vertices. For a given vertex v its label
 * is a concatenation of indices of already returned vertices, that were also its neighbours, with
 * some separator between them. For example, 7#4#3 is a valid vertex label.
 * <p>
 * Iterator chooses vertex with lexicographically largest label and returns it. It breaks ties
 * arbitrarily. For more information on lexicographical BFS see the following article:
 * Corneil D.G. (2004) <a href="https://pdfs.semanticscholar.org/d4b5/a492f781f23a30773841ec79c46d2ec2eb9c.pdf">
 * <i>Lexicographic Breadth First Search – A Survey</i></a>. In: Hromkovič J., Nagl M.,
 * Westfechtel B. (eds) Graph-Theoretic Concepts in Computer Science. WG 2004. Lecture Notes in
 * Computer Science, vol 3353. Springer, Berlin, Heidelberg; and the following paper:<a
 * href="http://www.cse.iitd.ac.in/~naveen/courses/CSL851/uwaterloo.pdf"><i>CS 762: Graph-theoretic
 * algorithms. Lecture notes of a graduate course. University of Waterloo</i></a>.
 * <p>
 * For this iterator to work correctly the graph must not be modified during iteration. Currently
 * there are no means to ensure that, nor to fail-fast. The results of such modifications are
 * undefined.
 * <p>
 * Note: only vertex events are fired by this iterator.
 *
 * @param <V> the graph vertex type.
 * @param <E> the graph edge type.
 * @author Timofey Chudakov
 * @since March 2018
 */
public class LexBreadthFirstIterator<V, E> extends AbstractGraphIterator<V, E> {

    /**
     * Reference to the {@code BucketList} that contains unvisited vertices.
     */
    private BucketList bucketList;

    /**
     * Contains current vertex of the {@code graph}.
     */
    private V current;

    /**
     * Creates new lexicographical breadth-first iterator for {@code graph}.
     *
     * @param graph the graph to be iterated.
     */
    public LexBreadthFirstIterator(Graph<V, E> graph) {
        super(graph);
        GraphTests.requireUndirected(graph);
        bucketList = new BucketList(graph.vertexSet());
    }
    
    /**
     * Creates new lexicographical breadth-first iterator for {@code graph}.
     *
     * @param graph the graph to be iterated.
     * @param startingVertex the initial vertex.
     */
    public LexBreadthFirstIterator(Graph<V, E> graph, V startingVertex) {
        super(graph);
        GraphTests.requireUndirected(graph);

        Set<V> copyOfSet = new HashSet<>();

        copyOfSet.addAll(graph.vertexSet());
        bucketList = new BucketList(copyOfSet, startingVertex);
    }
    
    
    /**
     * Creates new lexicographical breadth-first iterator with a static priority list for {@code graph}.
     *
     * @param graph the graph to be iterated.
     * @param priority the vertex array sorted by their priorities.
     */
    public LexBreadthFirstIterator(Graph<V, E> graph, HashMap<V, Integer> priority) {
        super(graph);
        GraphTests.requireUndirected(graph);

        Set<V> copyOfSet = new HashSet<>();

        copyOfSet.addAll(graph.vertexSet());
        bucketList = new BucketList(copyOfSet, new PriorityComparator(priority));
    }
    
    /**
     * Creates new lexicographical breadth-first iterator with a static priority list for {@code graph}.
     *
     * @param graph the graph to be iterated.
     */
    public LexBreadthFirstIterator(Graph<V, E> graph,
                                   HashMap<V, Integer> priorityA,
                                   HashMap<V, Integer> priorityB,
                                   HashMap<V, Integer> neighborIndexA,
                                   HashMap<V, Integer> neighborIndexB,
                                   HashMap<V, Set<V>> ASets,
                                   HashMap<V, Set<V>> BSets) {
        super(graph);
        GraphTests.requireUndirected(graph);

        Set<V> copyOfSet = new HashSet<>();

        copyOfSet.addAll(graph.vertexSet());
        bucketList = new BucketList(copyOfSet, priorityA, priorityB, neighborIndexA, neighborIndexB, ASets, BSets);
    }

    /**
     * Checks whether there exist unvisited vertices.
     *
     * @return true if there exist unvisited vertices.
     */
    @Override
    public boolean hasNext() {
        if (current != null) {
            return true;
        }
        current = advance();
        if (current != null && nListeners != 0) {
            fireVertexTraversed(createVertexTraversalEvent(current));
        }
        return current != null;
    }

    /**
     * Returns the next vertex in the ordering.
     *
     * @return the next vertex in the ordering.
     */
    @Override
    public V next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        V result = current;
        current = null;
        if (nListeners != 0) {
            fireVertexFinished(createVertexTraversalEvent(result));
        }
        return result;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Always returns true since this iterator doesn't care about connected components.
     */
    @Override
    public boolean isCrossComponentTraversal() {
        return true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Trying to disable the cross components nature of this iterator will result into throwing a
     * {@link IllegalArgumentException}.
     */
    @Override
    public void setCrossComponentTraversal(boolean crossComponentTraversal) {
        if (!crossComponentTraversal) {
            throw new IllegalArgumentException("Iterator is always cross-component");
        }
    }

    /**
     * Retrieves vertex from the {@code bucketList} and returns it.
     *
     * @return the vertex retrieved from the {@code bucketList}.
     */
    private V advance() {
        V vertex = bucketList.poll();
        if (vertex != null) {
            bucketList.updateBuckets(getUnvisitedNeighbours(vertex));
        }
        return vertex;
    }

    /**
     * Computes and returns neighbours of {@code vertex} which haven't been visited by this iterator.
     *
     * @param vertex the vertex, whose neighbours are being explored.
     * @return neighbours of {@code vertex} which have yet to be visited by this iterator.
     */
    private Set<V> getUnvisitedNeighbours(V vertex) {
        Set<V> unmapped = new HashSet<>();
        Set<E> edges = graph.edgesOf(vertex);
        for (E edge : edges) {
            V oppositeVertex = Graphs.getOppositeVertex(graph, edge, vertex);
            if (bucketList.containsBucketWith(oppositeVertex)) {
                unmapped.add(oppositeVertex);
            }
        }
        return unmapped;
    }

    /**
     * Data structure for performing lexicographical breadth-first search. Allows to add and retrieve
     * vertices from buckets, update their buckets after a new vertex has been added to the LexBFS
     * order. Labels aren't used explicitly, which results in time and space optimization.
     *
     * @author Timofey Chudakov
     * @since March 2018
     */
    class BucketList {
        /**
         * Bucket with the vertices that have lexicographically largest label.
         */
        private Bucket head;
        /**
         * Map for mapping vertices to buckets they are currently in. Is used for finding the bucket of
         * the vertex in constant time.
         */
        private Map<V, Bucket> bucketMap;
        
        /**
         * Comparator used for tiebreaking when multiple vertices have the same label
         */
        private Comparator<V> priorityComparator = null;
        
        /**
         * LBFS* static parameters
         */
        private HashMap<V, Integer> neighborIndexA = null;
        private HashMap<V, Integer> neighborIndexB = null;
        
        private HashMap<V, Set<V>> ASets = null;
        private HashMap<V, Set<V>> BSets = null;
        
        private HashMap<V, Integer> priorityA = null;
        private HashMap<V, Integer> priorityB = null;
        
        /**
         * Creates a {@code BucketList} with a single bucket and all specified {@code vertices} in it.
         *
         * @param vertices the vertices of the graph, that should be stored in the {@code head} bucket.
         */
        BucketList(Collection<V> vertices) {
            head = new Bucket(vertices, priorityComparator); // we do not need a comparator
            bucketMap = new HashMap<>(vertices.size());
            for (V vertex : vertices) {
                bucketMap.put(vertex, head);
            }
        }
        
        /**
         * Creates a {@code BucketList} with a single bucket and all specified {@code vertices} in it.
         *
         * @param vertices the vertices of the graph, that should be stored in the {@code head} bucket.
         * @param startingVertex the initial vertex.
         */
        BucketList(Collection<V> vertices, V startingVertex) {
            bucketMap = new HashMap<>(vertices.size());
            
            // Split off starting vertex into its own bucket
            vertices.remove(startingVertex);
            head = new Bucket(startingVertex, priorityComparator);
            head.insertBefore(new Bucket(vertices, priorityComparator));

            bucketMap.put(startingVertex, head);
            for (V vertex : vertices) {
                bucketMap.put(vertex, head.next);
            }
        }
        
        /**
         * Creates a {@code BucketList} with a single bucket and all specified {@code vertices} in it.
         *
         * @param vertices the vertices of the graph, that should be stored in the {@code head} bucket.
         * @param priorityComparator a comparator which defines a priority for tiebreaking.
         * @param startingVertex the initial vertex.
         */
        BucketList(Collection<V> vertices, Comparator<V> priorityComparator) {
            bucketMap = new HashMap<>(vertices.size());
            
            // Split off starting vertex into its own bucket
            head = new Bucket(vertices, priorityComparator);

            for (V vertex : vertices) {
                bucketMap.put(vertex, head);
            }
        }
        
        /**
         * Creates a {@code BucketList} with a single bucket and all specified {@code vertices} in it.
         *
         * @param vertices the vertices of the graph, that should be stored in the {@code head} bucket.
         */
        BucketList(Collection<V> vertices, HashMap<V, Integer> priorityA, HashMap<V, Integer> priorityB, HashMap<V, Integer> neighborIndexA, HashMap<V, Integer> neighborIndexB, HashMap<V, Set<V>> ASets, HashMap<V, Set<V>> BSets) {
            this.neighborIndexA = neighborIndexA;
            this.neighborIndexB = neighborIndexB;
            this.ASets = ASets;
            this.BSets = BSets;
            this.priorityA = priorityA;
            this.priorityB = priorityB;
            
            bucketMap = new HashMap<>(vertices.size());

            head = new Bucket(vertices, new PriorityComparator(priorityA), new PriorityComparator(priorityB));

            for (V vertex : vertices) {
                bucketMap.put(vertex, head);
            }
        }

        /**
         * Checks whether there exists a bucket with the specified {@code vertex}.
         *
         * @param vertex the vertex whose presence in some {@code Bucket} in this {@code BucketList} is
         *               checked.
         * @return <tt>true</tt> if there exists a bucket with {@code vertex} in it, otherwise
         * <tt>false</tt>.
         */
        boolean containsBucketWith(V vertex) {
            return bucketMap.containsKey(vertex);
        }

        /**
         * Retrieves element from the head bucket by invoking {@link Bucket#poll()} or
         * null if this {@code BucketList} is empty.
         * <p>
         * Removes the head bucket if it becomes empty after the operation.
         *
         * @return vertex returned by {@link Bucket#poll()} invoked on head bucket or
         * null if this {@code BucketList} is empty.
         */
        V poll() {
            if (bucketMap.size() > 0) {
                V res = null;
                
                if(neighborIndexA == null) {
                    res = head.poll();
                } else {
                    res = head.poll(neighborIndexA, neighborIndexB, ASets, BSets, priorityA, priorityB);
                }
                
                bucketMap.remove(res);
                if (head.isEmpty()) {
                    head = head.next;
                    if (head != null) {
                        head.prev = null;
                    }
                }
                return res;
            } else {
                return null;
            }
        }

        /**
         * For every bucket B in this {@code BucketList}, which contains vertices from the set {@code
         * vertices}, creates a new {@code Bucket} B' and moves vertices from B to B' according to the
         * following rule: $B' = B\cap vertices$ and $B = B\backslash B'$. For every such {@code Bucket}
         * B only one {@code Bucket} B' is created. If some bucket B becomes empty after this operation,
         * it is removed from the data structure.
         *
         * @param vertices the vertices, that should be moved to new buckets.
         */
        void updateBuckets(Set<V> vertices) {
            Set<Bucket> visitedBuckets = new HashSet<>();
            for (V vertex : vertices) {
                Bucket bucket = bucketMap.get(vertex);
                if (visitedBuckets.contains(bucket)) {
                    bucket.prev.addVertex(vertex);
                    bucketMap.put(vertex, bucket.prev);
                } else {
                    visitedBuckets.add(bucket);
                    Bucket newBucket;
                    if (priorityB != null) {
                        newBucket = new Bucket(vertex, new PriorityComparator(priorityA), new PriorityComparator(priorityB));
                    }
                    else{
                        newBucket = new Bucket(vertex, priorityComparator);
                    }
                    newBucket.insertBefore(bucket);
                    bucketMap.put(vertex, newBucket);
                    if (head == bucket) {
                        head = newBucket;
                    }
                }
                bucket.removeVertex(vertex);
                if (bucket.isEmpty()) {
                    visitedBuckets.remove(bucket);
                    bucket.removeSelf();
                }
            }
        }

        /**
         * Plays the role of the container of vertices. All vertices stored in a bucket have identical
         * label. Labels aren't used explicitly.
         * <p>
         * Encapsulates operations of addition and removal of vertices from the bucket and removal of a
         * bucket from the data structure.
         */
        private class Bucket {
            /**
             * Reference of the bucket with lexicographically smaller label.
             */
            private Bucket next;
            /**
             * Reference of the bucket with lexicographically larger label.
             */
            private Bucket prev;
            /**
             * Set of vertices currently stored in this bucket.
             */
            private Queue<V> vertices;
            /**
             * Set of vertices currently stored in this bucket (sorted by other order).
             */
            private Queue<V> verticesB = null;

            /**
             * Creates a new bucket with all {@code vertices} stored in it.
             *
             * @param vertices vertices to store in this bucket.
             */
            Bucket(Collection<V> vertices, Comparator<V> c) {
                if(c == null) {
                    this.vertices = new PriorityQueue<>();
                } else {
                    this.vertices = new PriorityQueue<>(c);
                }
                this.vertices.addAll(vertices);
            }

            /**
             * Creates a new Bucket with a single {@code vertex} in it.
             *
             * @param vertex the vertex to store in this bucket.
             */
            Bucket(V vertex, Comparator<V> c) {
                if(c == null) {
                    this.vertices = new PriorityQueue<>();
                } else {
                    this.vertices = new PriorityQueue<>(c);
                }
                vertices.add(vertex);
            }
            
            /**
             * LBFS*-variants
             */
            
            /**
             * Creates a new bucket with all {@code vertices} stored in it.
             *
             * @param vertices vertices to store in this bucket.
             */
            Bucket(Collection<V> vertices, Comparator<V> compA, Comparator<V> compB) {
                this.vertices = new PriorityQueue<>(compA);
                this.verticesB = new PriorityQueue<>(compB);
                
                this.vertices.addAll(vertices);
                this.verticesB.addAll(vertices);
            }

            /**
             * Creates a new Bucket with a single {@code vertex} in it.
             *
             * @param vertex the vertex to store in this bucket.
             */
            Bucket(V vertex, Comparator<V> compA, Comparator<V> compB) {
                this.vertices = new PriorityQueue<>(compA);
                this.verticesB = new PriorityQueue<>(compB);
                
                vertices.add(vertex);
                verticesB.add(vertex);
            }

            /**
             * Removes the {@code vertex} from this bucket.
             *
             * @param vertex the vertex to remove.
             */
            void removeVertex(V vertex) {
                vertices.remove(vertex);
                
                if(verticesB != null) {
                    verticesB.remove(vertex);
                }
            }

            /**
             * Removes this bucket from the data structure.
             */
            void removeSelf() {
                if (next != null) {
                    next.prev = prev;
                }
                if (prev != null) {
                    prev.next = next;
                }
            }

            /**
             * Inserts this bucket in the data structure before the {@code bucket}.
             *
             * @param bucket the bucket, that will be the next to this bucket.
             */
            void insertBefore(Bucket bucket) {
                this.next = bucket;
                if (bucket != null) {
                    this.prev = bucket.prev;
                    if (bucket.prev != null) {
                        bucket.prev.next = this;
                    }
                    bucket.prev = this;
                } else {
                    this.prev = null;
                }
            }

            /**
             * Adds the {@code vertex} to this bucket.
             *
             * @param vertex the vertex to add.
             */
            void addVertex(V vertex) {
                vertices.add(vertex);
                
                if(verticesB != null) {
                    verticesB.add(vertex);
                }
            }

            /**
             * Retrieves one vertex from this bucket.
             *
             * @return vertex, that was removed from this bucket, null if the bucket was empty.
             */
            V poll() {
                if (vertices.isEmpty()) {
                    return null;
                } else {
                    V vertex = vertices.poll();
                    return vertex;
                }
            }
            
            /**
             * Retrieves one vertex from this bucket (according to LBFS*).
             *
             * @return vertex, that was removed from this bucket, null if the bucket was empty.
             */
            V poll(HashMap<V, Integer> neighborIndexA, HashMap<V, Integer> neighborIndexB, HashMap<V, Set<V>> ASets, HashMap<V, Set<V>> BSets, HashMap<V, Integer> priorityA, HashMap<V, Integer> priorityB) {
                if (vertices.isEmpty()) {
                    return null;
                } else {
                    V alpha = vertices.peek();
                    V beta = verticesB.peek();
                    
                    if(neighborIndexA.get(alpha) > priorityA.get(alpha)) {
                        vertices.remove(beta);
                        return verticesB.poll(); // return Beta
                    } else if(neighborIndexB.get(beta) > priorityB.get(beta)) {
                        verticesB.remove(beta);
                        return vertices.poll(); // return Alpha
                    } else if(BSets.get(beta).isEmpty() || !ASets.get(alpha).isEmpty()) {
                        vertices.remove(beta);
                        return verticesB.poll(); // return Beta
                    } else if(neighborIndexA.get(BSets.get(beta).iterator().next()) == priorityA.get(alpha)) {
                        vertices.remove(beta);
                        return verticesB.poll(); // return Beta
                    } else {
                        verticesB.remove(beta);
                        return vertices.poll(); // return Alpha
                    }
                }
            }

            /**
             * Checks whether this bucket is empty.
             *
             * @return <tt>true</tt> if this bucket doesn't contain any elements, otherwise false.
             */
            boolean isEmpty() {
                return vertices.size() == 0;
            }
        }
    }
    
    class PriorityComparator implements Comparator<V>
    {
        /**
         * Contains the priorities of the vertices.
         */
        private final HashMap<V, Integer> priority;
        
        /**
         * Creates a new priority comparator for the vertex set with given priorities.
         * 
         * @param priority the (integer-valued) priorities of the vertices.
         * @throws IllegalArgumentException if the priorities are <tt>null</tt>.
         */
        public PriorityComparator(HashMap<V, Integer> priority) throws IllegalArgumentException {
            if(priority == null) {
                throw new IllegalArgumentException("Priority map must not be null");
            }
            this.priority = priority;
        }
        
        @Override
        /**
         * Compares the priorities of the given vertices.
         * 
         * @param vertex1 the first vertex to be compared.
         * @param vertex2 the second vertex to be compared.
         * 
         * @return Returns a positive integer (zero/a negative integer) if the priority of <tt>vertex1</tt> is smaller (equal to/higher) than the one of <tt>vertex2</tt>.
         */
        public int compare(V vertex1, V vertex2)
        {
            return priority.get(vertex2) - priority.get(vertex1);
        }
    }
}
