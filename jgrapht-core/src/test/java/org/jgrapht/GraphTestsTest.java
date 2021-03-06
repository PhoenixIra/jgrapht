/*
 * (C) Copyright 2016-2018, by Dimitrios Michail and Contributors.
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
package org.jgrapht;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.jgrapht.generate.CompleteGraphGenerator;
import org.jgrapht.generate.GnpRandomBipartiteGraphGenerator;
import org.jgrapht.generate.NamedGraphGenerator;
import org.jgrapht.generate.StarGraphGenerator;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedPseudograph;
import org.jgrapht.graph.Pseudograph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleGraph;
import org.junit.Assert;
import org.junit.Test;

import static junit.framework.TestCase.fail;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Test class GraphTests.
 *
 * @author Dimitrios Michail
 */
public class GraphTestsTest
{

    @Test public void testIsEmpty()
    {
        Graph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        assertTrue(GraphTests.isEmpty(g));
        g.addVertex(1);
        assertTrue(GraphTests.isEmpty(g));
        g.addVertex(2);
        assertTrue(GraphTests.isEmpty(g));
        DefaultEdge e = g.addEdge(1, 2);
        assertFalse(GraphTests.isEmpty(g));
        g.removeEdge(e);
        assertTrue(GraphTests.isEmpty(g));
    }

    @Test public void testIsSimple()
    {
        // test empty
        Graph<Integer, DefaultEdge> g1 = new DefaultDirectedGraph<>(DefaultEdge.class);
        assertTrue(GraphTests.isSimple(g1));

        Graph<Integer, DefaultEdge> g2 = new SimpleGraph<>(DefaultEdge.class);
        assertTrue(GraphTests.isSimple(g2));

        Graph<Integer, DefaultEdge> g3 = new DirectedPseudograph<>(DefaultEdge.class);
        Assert.assertTrue(GraphTests.isSimple(g3));

        Graphs.addAllVertices(g3, Arrays.asList(1, 2));
        g3.addEdge(1, 2);
        g3.addEdge(2, 1);
        assertTrue(GraphTests.isSimple(g3));
        DefaultEdge g3e11 = g3.addEdge(1, 1);
        assertFalse(GraphTests.isSimple(g3));
        g3.removeEdge(g3e11);
        assertTrue(GraphTests.isSimple(g3));
        g3.addEdge(2, 1);
        assertFalse(GraphTests.isSimple(g3));

        Graph<Integer, DefaultEdge> g4 = new Pseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(g4, Arrays.asList(1, 2));
        assertTrue(GraphTests.isSimple(g4));
        DefaultEdge g4e12 = g4.addEdge(1, 2);
        g4.addEdge(2, 1);
        assertFalse(GraphTests.isSimple(g4));
        g4.removeEdge(g4e12);
        assertTrue(GraphTests.isSimple(g4));
        g4.addEdge(1, 1);
        assertFalse(GraphTests.isSimple(g4));
    }

    @Test public void testHasSelfLoops()
    {
        Graph<Integer, DefaultEdge> g1 = new DefaultDirectedGraph<>(DefaultEdge.class);
        Assert.assertFalse(GraphTests.hasSelfLoops(g1));

        Graph<Integer, DefaultEdge> g2 = new SimpleGraph<>(DefaultEdge.class);
        Assert.assertFalse(GraphTests.hasSelfLoops(g2));

        Graph<Integer, DefaultEdge> g3 = new DirectedPseudograph<>(DefaultEdge.class);
        Assert.assertFalse(GraphTests.hasSelfLoops(g3));

        Graphs.addAllVertices(g3, Arrays.asList(1, 2));
        g3.addEdge(1, 2);
        g3.addEdge(2, 1);
        Assert.assertFalse(GraphTests.hasSelfLoops(g3));
        g3.addEdge(2, 2);
        Assert.assertTrue(GraphTests.hasSelfLoops(g3));

        Graph<Integer, DefaultEdge> g4 = new Pseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(g4, Arrays.asList(1, 2));
        g4.addEdge(1, 2);
        g4.addEdge(2, 1);
        Assert.assertFalse(GraphTests.hasSelfLoops(g4));
        g4.addEdge(2, 2);
        Assert.assertTrue(GraphTests.hasSelfLoops(g4));
    }

    @Test public void testHasMultipleEdges()
    {
        Graph<Integer, DefaultEdge> g1 = new DefaultDirectedGraph<>(DefaultEdge.class);
        Assert.assertFalse(GraphTests.hasMultipleEdges(g1));

        Graph<Integer, DefaultEdge> g2 = new SimpleGraph<>(DefaultEdge.class);
        Assert.assertFalse(GraphTests.hasMultipleEdges(g2));

        Graph<Integer, DefaultEdge> g3 = new DirectedPseudograph<>(DefaultEdge.class);
        Assert.assertFalse(GraphTests.hasMultipleEdges(g3));
        Graphs.addAllVertices(g3, Arrays.asList(1, 2));
        g3.addEdge(1, 2);
        g3.addEdge(2, 1);
        g3.addEdge(1, 1);
        Assert.assertFalse(GraphTests.hasMultipleEdges(g3));
        g3.addEdge(2, 2);
        Assert.assertFalse(GraphTests.hasMultipleEdges(g3));
        g3.addEdge(2, 1);
        Assert.assertTrue(GraphTests.hasMultipleEdges(g3));

        Graph<Integer, DefaultEdge> g4 = new Pseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(g4, Arrays.asList(1, 2));
        g4.addEdge(1, 2);
        g4.addEdge(1, 1);
        Assert.assertFalse(GraphTests.hasMultipleEdges(g4));
        g4.addEdge(2, 1);
        Assert.assertTrue(GraphTests.hasMultipleEdges(g4));

        Graph<Integer, DefaultEdge> g5 = new Pseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(g5, Arrays.asList(1, 2));
        g5.addEdge(1, 2);
        g5.addEdge(1, 1);
        Assert.assertFalse(GraphTests.hasMultipleEdges(g5));
        g5.addEdge(1, 1);
        Assert.assertTrue(GraphTests.hasMultipleEdges(g5));

    }

    @Test public void testIsCompleteDirected()
    {
        Graph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        assertTrue(GraphTests.isComplete(g));
        g.addVertex(1);
        assertTrue(GraphTests.isComplete(g));
        g.addVertex(2);
        assertFalse(GraphTests.isComplete(g));
        g.addEdge(1, 2);
        assertFalse(GraphTests.isComplete(g));
        g.addEdge(2, 1);
        assertTrue(GraphTests.isComplete(g));
        g.addVertex(3);
        assertFalse(GraphTests.isComplete(g));
        g.addEdge(1, 3);
        assertFalse(GraphTests.isComplete(g));
        g.addEdge(3, 1);
        assertFalse(GraphTests.isComplete(g));
        g.addEdge(2, 3);
        assertFalse(GraphTests.isComplete(g));
        g.addEdge(3, 2);
        assertTrue(GraphTests.isComplete(g));

        // check loops
        Graph<Integer, DefaultEdge> g1 = new DirectedPseudograph<>(DefaultEdge.class);
        assertTrue(GraphTests.isComplete(g1));
        g1.addVertex(1);
        assertTrue(GraphTests.isComplete(g1));
        g1.addVertex(2);
        assertFalse(GraphTests.isComplete(g1));
        g1.addEdge(1, 1);
        g1.addEdge(2, 2);
        assertFalse(GraphTests.isComplete(g1));

        // check multiple edges
        Graph<Integer, DefaultEdge> g2 = new DirectedPseudograph<>(DefaultEdge.class);
        assertTrue(GraphTests.isComplete(g2));
        Graphs.addAllVertices(g2, Arrays.asList(1, 2, 3));
        assertFalse(GraphTests.isComplete(g2));
        g2.addEdge(1, 2);
        g2.addEdge(1, 3);
        g2.addEdge(2, 3);
        g2.addEdge(1, 1);
        g2.addEdge(2, 2);
        g2.addEdge(3, 3);
        assertFalse(GraphTests.isComplete(g2));
    }

    @Test public void testIsCompleteUndirected()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        assertTrue(GraphTests.isComplete(g));
        g.addVertex(1);
        assertTrue(GraphTests.isComplete(g));
        g.addVertex(2);
        assertFalse(GraphTests.isComplete(g));
        g.addEdge(1, 2);
        assertTrue(GraphTests.isComplete(g));
        g.addVertex(3);
        assertFalse(GraphTests.isComplete(g));
        g.addEdge(1, 3);
        assertFalse(GraphTests.isComplete(g));
        g.addEdge(2, 3);
        assertTrue(GraphTests.isComplete(g));

        // check loops
        Graph<Integer, DefaultEdge> g1 = new Pseudograph<>(DefaultEdge.class);
        assertTrue(GraphTests.isComplete(g1));
        g1.addVertex(1);
        assertTrue(GraphTests.isComplete(g1));
        g1.addVertex(2);
        assertFalse(GraphTests.isComplete(g1));
        g1.addEdge(1, 1);
        assertFalse(GraphTests.isComplete(g1));

        // check multiple edges
        Graph<Integer, DefaultEdge> g2 = new Pseudograph<>(DefaultEdge.class);
        assertTrue(GraphTests.isComplete(g2));
        g2.addVertex(1);
        assertTrue(GraphTests.isComplete(g2));
        g2.addVertex(2);
        assertFalse(GraphTests.isComplete(g2));
        g2.addEdge(1, 2);
        assertTrue(GraphTests.isComplete(g2));
        g2.addEdge(1, 2);
        assertFalse(GraphTests.isComplete(g2));
        g2.addVertex(3);
        g2.addEdge(1, 3);
        assertFalse(GraphTests.isComplete(g2));
    }

    @Test public void testIsConnectedUndirected()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        assertFalse(GraphTests.isConnected(g));
        g.addVertex(1);
        assertTrue(GraphTests.isConnected(g));
        g.addVertex(2);
        assertFalse(GraphTests.isConnected(g));
        g.addEdge(1, 2);
        assertTrue(GraphTests.isConnected(g));
        g.addVertex(3);
        assertFalse(GraphTests.isConnected(g));
        g.addEdge(1, 3);
        assertTrue(GraphTests.isConnected(g));
    }

    @Test public void testIsConnectedDirected()
    {
        Graph<Integer, DefaultEdge> g = new SimpleDirectedGraph<>(DefaultEdge.class);
        assertFalse(GraphTests.isWeaklyConnected(g));
        assertFalse(GraphTests.isStronglyConnected(g));
        g.addVertex(1);
        assertTrue(GraphTests.isWeaklyConnected(g));
        assertTrue(GraphTests.isStronglyConnected(g));
        g.addVertex(2);
        assertFalse(GraphTests.isWeaklyConnected(g));
        assertFalse(GraphTests.isStronglyConnected(g));
        g.addEdge(1, 2);
        assertTrue(GraphTests.isWeaklyConnected(g));
        assertFalse(GraphTests.isStronglyConnected(g));
        g.addVertex(3);
        assertFalse(GraphTests.isWeaklyConnected(g));
        assertFalse(GraphTests.isStronglyConnected(g));
        g.addEdge(2, 3);
        assertTrue(GraphTests.isWeaklyConnected(g));
        assertFalse(GraphTests.isStronglyConnected(g));
        g.addEdge(3, 1);
        assertTrue(GraphTests.isWeaklyConnected(g));
        assertTrue(GraphTests.isStronglyConnected(g));
    }

    @Test public void testIsTree()
    {
        Graph<Integer, DefaultEdge> g = GraphTestsUtils.createPseudograph();
        assertFalse(GraphTests.isTree(g));
        g.addVertex(1);
        assertTrue(GraphTests.isTree(g));
        g.addVertex(2);
        assertFalse(GraphTests.isTree(g));
        g.addEdge(1, 2);
        assertTrue(GraphTests.isTree(g));
        g.addVertex(3);
        assertFalse(GraphTests.isTree(g));
        g.addEdge(1, 3);
        assertTrue(GraphTests.isTree(g));
        g.addEdge(2, 3);
        assertFalse(GraphTests.isTree(g));

        // disconnected but with correct number of edges
        Graph<Integer, DefaultEdge> g1 = GraphTestsUtils.createPseudograph();
        assertFalse(GraphTests.isTree(g1));
        g1.addVertex(1);
        g1.addVertex(2);
        g.addEdge(1, 1);
        assertFalse(GraphTests.isTree(g1));
    }

    @Test public void testIsForest1()
    {
        Graph<Integer, DefaultEdge> g = GraphTestsUtils.createPseudograph();
        assertFalse(GraphTests.isForest(g));
        g.addVertex(1);
        assertTrue(GraphTests.isForest(g));
        g.addVertex(2);
        assertTrue(GraphTests.isForest(g));
        g.addEdge(1, 2);
        assertTrue(GraphTests.isForest(g));
        g.addEdge(1, 2);
        assertFalse(GraphTests.isForest(g));
    }

    @Test public void testIsForest2()
    {
        Graph<Integer, DefaultEdge> g = GraphTestsUtils.createPseudograph();
        StarGraphGenerator<Integer, DefaultEdge> gen = new StarGraphGenerator<>(10);
        gen.generateGraph(g);
        gen.generateGraph(g);
        assertTrue(GraphTests.isForest(g));
    }

    @Test public void testIsOverfull()
    {
        assertFalse(GraphTests.isOverfull(NamedGraphGenerator.clawGraph()));
        assertTrue(GraphTests.isOverfull(NamedGraphGenerator.doyleGraph()));

        Graph<Integer, DefaultEdge> k6 = GraphTestsUtils.createPseudograph();
        CompleteGraphGenerator<Integer, DefaultEdge> gen = new CompleteGraphGenerator<>(6);
        gen.generateGraph(k6);
        assertFalse(GraphTests.isOverfull(k6));

        Graph<Integer, DefaultEdge> k7 = GraphTestsUtils.createPseudograph();
        gen = new CompleteGraphGenerator<>(7);
        gen.generateGraph(k7);
        assertTrue(GraphTests.isOverfull(k7));
    }

    @Test public void isSplit1()
    {
        assertFalse(GraphTests.isSplit(NamedGraphGenerator.petersenGraph()));
        Graph<Integer, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);
        assertFalse(GraphTests.isSplit(g));
        g.addVertex(0);
        assertTrue(GraphTests.isSplit(g));
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4));
        // clique
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(2, 0);
        // independent set
        g.addEdge(3, 1);
        g.addEdge(3, 2);
        g.addEdge(4, 1);
        assertTrue(GraphTests.isSplit(g));
        g.addEdge(3, 4);
        assertTrue(GraphTests.isSplit(g));
    }

    @Test public void isSplit2()
    {
        // Create some random split graphs.
        Random rand = new Random(0);
        CompleteGraphGenerator<Integer, DefaultEdge> gen = new CompleteGraphGenerator<>(6);

        for (int inst = 0; inst < 5; inst++) {
            // 1. create a clique
            Graph<Integer, DefaultEdge> g = GraphTestsUtils.createSimpleGraph();
            gen.generateGraph(g);

            // 2. add a number of vertices (the independent set) and connect some of these vertices
            // with vertices in the clique.
            for (int j = 6; j < 12; j++) {
                g.addVertex(j);
                for (int i = 0; i < 6; i++)
                    if (rand.nextBoolean())
                        g.addEdge(i, j);
            }
            assertTrue(GraphTests.isSplit(g));
        }
    }

    @Test public void testBipartite1()
    {
        Graph<Integer, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);
        assertTrue(GraphTests.isBipartite(g));
        g.addVertex(1);
        assertTrue(GraphTests.isBipartite(g));
        g.addVertex(2);
        assertTrue(GraphTests.isBipartite(g));
        g.addEdge(1, 2);
        assertTrue(GraphTests.isBipartite(g));
        g.addVertex(3);
        assertTrue(GraphTests.isBipartite(g));
        g.addEdge(2, 3);
        assertTrue(GraphTests.isBipartite(g));
        g.addEdge(3, 1);
        assertFalse(GraphTests.isBipartite(g));
    }

    @Test public void testBipartite2()
    {
        Graph<Integer, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);

        for (int i = 0; i < 100; i++) {
            g.addVertex(i);
            if (i > 0) {
                g.addEdge(i, i - 1);
            }
        }
        g.addEdge(99, 0);
        assertTrue(GraphTests.isBipartite(g));
    }

    @Test public void testBipartite3()
    {
        Graph<Integer, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);

        for (int i = 0; i < 101; i++) {
            g.addVertex(i);
            if (i > 0) {
                g.addEdge(i, i - 1);
            }
        }
        g.addEdge(100, 0);
        assertFalse(GraphTests.isBipartite(g));
    }

    @Test public void testBipartite4()
    {
        Graph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        for (int i = 0; i < 101; i++) {
            g.addVertex(i);
            if (i > 0) {
                g.addEdge(i, i - 1);
            }
        }
        g.addEdge(100, 0);
        assertFalse(GraphTests.isBipartite(g));
    }

    @Test public void testRandomBipartite()
    {
        GnpRandomBipartiteGraphGenerator<Integer, DefaultEdge> generator =
            new GnpRandomBipartiteGraphGenerator<>(10, 10, 0.8);
        for (int i = 0; i < 100; i++) {
            Graph<Integer, DefaultEdge> g = GraphTestsUtils.createPseudograph();
            generator.generateGraph(g);
            assertTrue(GraphTests.isBipartite(g));
        }
    }

    @Test public void testIsBipartitePartition()
    {
        List<Graph<Integer, DefaultEdge>> gList = new ArrayList<>();
        gList.add(new Pseudograph<>(DefaultEdge.class));
        gList.add(new DirectedPseudograph<>(DefaultEdge.class));

        for (Graph<Integer, DefaultEdge> g : gList) {
            Set<Integer> a = new HashSet<>();
            Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4));
            a.addAll(Arrays.asList(1, 2));
            Set<Integer> b = new HashSet<>();
            b.addAll(Arrays.asList(3, 4));
            assertTrue(GraphTests.isBipartitePartition(g, a, b));
            g.addEdge(1, 3);
            g.addEdge(1, 4);
            g.addEdge(1, 3);
            g.addEdge(2, 3);
            g.addEdge(2, 4);
            g.addEdge(4, 1);
            g.addEdge(3, 1);
            assertTrue(GraphTests.isBipartitePartition(g, a, b));
            a.remove(1);
            assertFalse(GraphTests.isBipartitePartition(g, a, b));
            a.add(1);
            assertTrue(GraphTests.isBipartitePartition(g, a, b));
            DefaultEdge e11 = g.addEdge(1, 1);
            assertFalse(GraphTests.isBipartitePartition(g, a, b));
            g.removeEdge(e11);
            assertTrue(GraphTests.isBipartitePartition(g, a, b));
            DefaultEdge e44 = g.addEdge(4, 4);
            assertFalse(GraphTests.isBipartitePartition(g, a, b));
            g.removeEdge(e44);
            assertTrue(GraphTests.isBipartitePartition(g, a, b));
            g.addEdge(4, 3);
            assertFalse(GraphTests.isBipartitePartition(g, a, b));
        }
    }

    @Test public void testIsCubic()
    {
        assertTrue(GraphTests.isCubic(NamedGraphGenerator.petersenGraph()));
        Graph<Integer, DefaultEdge> triangle = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addEdgeWithVertices(triangle, 1, 2);
        Graphs.addEdgeWithVertices(triangle, 2, 3);
        Graphs.addEdgeWithVertices(triangle, 3, 1);
        assertFalse(GraphTests.isCubic(triangle));
    }

    @Test public void testIsChordal()
    {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2);
        Graphs.addEdgeWithVertices(graph, 2, 3);
        Graphs.addEdgeWithVertices(graph, 3, 4);
        Graphs.addEdgeWithVertices(graph, 4, 5);
        Graphs.addEdgeWithVertices(graph, 5, 1);
        Graphs.addEdgeWithVertices(graph, 1, 3);
        assertFalse(GraphTests.isChordal(graph));
        Graphs.addEdgeWithVertices(graph, 1, 4);
        assertTrue(GraphTests.isChordal(graph));
    }

    @Test public void testIsWeaklyChordal()
    {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2);
        Graphs.addEdgeWithVertices(graph, 2, 3);
        Graphs.addEdgeWithVertices(graph, 3, 4);
        Graphs.addEdgeWithVertices(graph, 4, 5);
        Graphs.addEdgeWithVertices(graph, 5, 1);
        assertFalse(GraphTests.isWeaklyChordal(graph));
        Graphs.addEdgeWithVertices(graph, 1, 3);
        assertTrue(GraphTests.isWeaklyChordal(graph));
    }

    @Test public void failRequireIsWeightedOnUnweightedGraph()
    {
        try {
            Graph<String, DefaultWeightedEdge> graph =
                new DefaultDirectedGraph<>(DefaultWeightedEdge.class);
            GraphTests.requireWeighted(graph);
            fail("Expected an IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("Graph must be weighted"));
        }
    }

    @Test public void failRequireIsWeightedOnNull()
    {
        try {
            GraphTests.requireWeighted(null);
            fail("Expected an NullPointerException to be thrown");
        } catch (NullPointerException e) {
            assertThat(e.getMessage(), is("Graph cannot be null"));
        }
    }

    @Test public void testRequireIsWeighted()
    {
        Graph graph = new DefaultUndirectedWeightedGraph<>(DefaultEdge.class);
        assertEquals(graph, GraphTests.requireWeighted(graph));
    }
}

// End GraphTestsTest.java
