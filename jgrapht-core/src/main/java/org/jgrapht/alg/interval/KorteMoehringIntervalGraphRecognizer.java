package org.jgrapht.alg.interval;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.cycle.ChordalityInspector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.interval.*;

/**
 * TODO: better Javadoc
 * @author Ira Justus Fesefeldt (PhoenixIra)
 * @author Timofey Chudakov
 *
 * @param <V> the vertex type of the graph
 * @param <E> the edge type of the graph
 */
public class KorteMoehringIntervalGraphRecognizer<V, E> implements IntervalGraphRecognizerInterface<V>
{

    // The recognized graph
    private Graph<V, E> graph;
    
    ChordalityInspector<V, E> chorInspec;

    private PNode treeRoot;
    
    private HashMap<V,Set<MPQNode>> vertexToMPQNode;
    
    private boolean isIntervalGraph;
    private boolean isChordal;

    /**
     * Constructor for the algorithm
     * @param graph the graph which should be recognized
     */
    public KorteMoehringIntervalGraphRecognizer(Graph<V, E> graph)
    {
        this.graph = graph;
        chorInspec = new ChordalityInspector<>(graph,ChordalityInspector.IterationOrder.LEX_BFS);
        treeRoot = new PNode(null,null);
        vertexToMPQNode = new HashMap<V, Set<MPQNode>>();
    }

    /**
     * TODO: better Javadoc
     * 
     * the Korte-Moehring Algorithm, which tests the graphs with an MPQ tree for an interval representation.
     * If the algorithm returns true, we can computed an interval representation of the MPQ Tree
     * If the algorithm returns false, we can computed an counter example of the MPQ Tree
     */
    private void testIntervalGraph()
    {

        //check for chordality
        isChordal = chorInspec.isChordal();
        if(!isChordal) 
        {
            isIntervalGraph = false;
            return;
        }
        
        // init all relevant objects
        Map<V, Integer> vertexOrder = getVertexInOrder(chorInspec.getPerfectEliminationOrder());
        // iterate over the perfect elimination order
        for (V u : chorInspec.getPerfectEliminationOrder()) {
            // calculate Adj(u) - the predecessors of u
            Set<V> predecessors = getPredecessors(vertexOrder, u);

            // special case for predecessors is empty
            if (predecessors.isEmpty()) {
                addEmptyPredecessors(u);
                continue;
            }

            // labeling phase: 
            // 1 if one but not all vertices in a PQNode is a predecessor
            // 2/inf if all vertices in a PQNode is a predecessor
            Map<MPQNode,Integer> positiveLabels = labelTree(predecessors);
            
            // test phase:
            // check for path of positive labels
            if(!testPath(positiveLabels.keySet()) 
            //check if outer sections of Q nodes N contain predecessors intersection V(N)
                | !testOuterSectionsOfQNodes(positiveLabels.keySet(), predecessors))
            {
                //then this is not an interval graph
                isIntervalGraph = false;
                return;
            }
            
            // update phase:
            // generate the path
            List<MPQNode> path = getPath(positiveLabels.keySet());
            
            //get lowest positive node in path
            MPQNode Nsmall = getNSmall(path, positiveLabels);
            
            //get highest non-inf node in path
            MPQNode Nbig = getNBig(path, positiveLabels);
            
            //update MPQ Tree
            changedPathToTemplates(u,predecessors,path,Nsmall,Nbig);

        }
    }
    
    
    
    
    
    /**
     * Returns the predecessors of {@code vertex} in the order defined by {@code map}. More
     * precisely, returns those of {@code vertex}, whose mapped index in {@code map} is less then
     * the index of {@code vertex}.
     *
     * @param vertexInOrder defines the mapping of vertices in {@code graph} to their indices in
     *        order.
     * @param vertex the vertex whose predecessors in order are to be returned.
     * @return the predecessors of {@code vertex} in order defines by {@code map}.
     */
    private Set<V> getPredecessors(Map<V, Integer> vertexInOrder, V vertex)
    {
        Set<V> predecessors = new HashSet<>();
        Integer vertexPosition = vertexInOrder.get(vertex);
        Set<E> edges = graph.edgesOf(vertex);
        for (E edge : edges) {
            V oppositeVertex = Graphs.getOppositeVertex(graph, edge, vertex);
            Integer destPosition = vertexInOrder.get(oppositeVertex);
            if (destPosition < vertexPosition) {
                predecessors.add(oppositeVertex);
            }
        }
        return predecessors;
    }

    /**
     * Returns a map containing vertices from the {@code vertexOrder} mapped to their indices in
     * {@code vertexOrder}.
     *
     * @param vertexOrder a list with vertices.
     * @return a mapping of vertices from {@code vertexOrder} to their indices in
     *         {@code vertexOrder}.
     */
    private Map<V, Integer> getVertexInOrder(List<V> vertexOrder)
    {
        Map<V, Integer> vertexInOrder = new HashMap<>(vertexOrder.size());
        int i = 0;
        for (V vertex : vertexOrder) {
            vertexInOrder.put(vertex, i++);
        }
        return vertexInOrder;
    }
    
    
    /**
     * Changed the MPQ Tree if u has no predecessors.
     * Adds a new leaf node with the bag of this vertex to the root.
     * 
     * @param u the vertex to be added to the MPQ Tree
     */
    private void addEmptyPredecessors(V u)
    {
        Set<V> bag = new HashSet<V>();
        bag.add(u);
        MPQNode leaf = new PNode(null,bag);
        treeRoot.add(leaf);
            
    }
    
    /**
     * TODO: Better Javadoc
     * Label every positive vertex in the MPQ Tree
     * 
     * @param predecessors the predecessors which are used to label the vertices in the tree
     * @return the labeling of all positive labeled vertices
     */
    private Map<MPQNode,Integer> labelTree(Set<V> predecessors)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    /**
     * TODO: Better Javadoc
     * tests if positiveLabels form a path
     * 
     * @param positiveLabels the vertices which should form a path
     * @return true iff it forms a path
     */
    private boolean testPath(Set<MPQNode> positiveLabels)
    {
        // TODO Auto-generated method stub
        return false;
    }
    
    /**
     * TODO: Better Javadoc
     * tests if an outer section of every Q nodes N in positive labels contains predecessors intersection V(N)
     * 
     * @param positiveLabels the positive vertices
     * @param predecessors the predecessors of u
     * @return true iff it fulfills the condition
     */
    private boolean testOuterSectionsOfQNodes(Set<MPQNode> positiveLabels, Set<V> predecessors)
    {
        // TODO Auto-generated method stub
        return false;
    }
    
    /**
     * TODO: better Javadoc
     * computes a path from the root to a leaf, containing all positive vertices
     * 
     * @param positiveLabels the vertices which forms a path
     * @return the path from root to a leaf
     */
    private List<MPQNode> getPath(Set<MPQNode> positiveLabels)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    /**
     * TODO: better Javadoc
     * computes the smallest vertex N of the Tree which has a positive label
     * 
     * @param path the path from root to leaf
     * @param positiveLabels the map from nodes to positive labels
     * @return smalles vertex N with positive label
     */
    private MPQNode getNSmall(List<MPQNode> path, Map<MPQNode, Integer> positiveLabels)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    /**
     * TODO: better Javadoc
     * computes the highest vertex N of the tree which is non-empty and non-inf
     * 
     * @param path the path from root to leaf
     * @param positiveLabels the map from nodes to positive labels
     * @return highest non-empty, non-inf vertex N
     */
    private MPQNode getNBig(List<MPQNode> path, Map<MPQNode, Integer> positiveLabels)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    /**
     * TODO: better Javadoc
     * Adds the vertex u to the leaf of the path
     * 
     * @param u the vertex to be added
     * @param path the path of the leaf
     */
    private void addVertexToLeaf(V u, List<MPQNode> path)
    {
        // TODO Auto-generated method stub
        
    }

    /**
     * Checks the path for specifig patterns and changes every node accordingly.
     * We distinguish between the two cases:
     * - nSmall == nBig: then we just need to change one node in the tree and do this in the method
     *      templateForSmallEqBig. In this case a maximal clique can just be extended or split
     * - nSmall != nBig: now we have to generate some sort of ordering over the maximal cliques.
     *      The mpq tree is for the first element nSmall with as a special case changed in the
     *      templateForSmall method and afterwards the other nodes in the path until reaching
     *      the parent of nBig is changed with P3 or Q3 templates.
     * 
     * @param u the vertex to add to the tree
     * @param path the path of vertices to be changed
     * @param nSmall the smalles positive node in path
     * @param nBig the highest non-empty, non-inf node in path (else equal to nSmall)
     */
    private void changedPathToTemplates(V u, Set<V> adj, List<MPQNode> path, MPQNode nSmall, MPQNode nBig)
    {
        //search for position of nSmall in path
        int pos = 0;
        while(path.get(pos) != nSmall) {
            pos++;
        }
        
        //init
        MPQNode current = path.get(pos);
        Set<V> adjOfNode = new HashSet<V>(adj);
        adjOfNode.retainAll(current.getBag());
        
        //special case that nSmall == nBig
        if(nSmall == nBig) {
            templateForSmallEqBig(current, u, adjOfNode, path, pos);
            return; //no further iteration needed!
        }
        
        
        
        
        
        //do for nSmall != nBig
        templateForSmall(current,u,adjOfNode,path,pos);
        pos++;
        
        //and for the rest of the list
        while(path.get(pos-1) != nBig) {
            //is a P-Node, can not be a leaf
            if(current.getType() == NodeType.PNODE) {
                PNode pCurrent = current.asPNode();
                templateP3(pCurrent, u, adjOfNode);
                
            //is a Q-Node, now check whether the left or right outer section has to be changed
            } else if(current.getType() == NodeType.QSECTION) {
                QSectionNode qSecCurrent = current.asSection();
                pos++;
                current = path.get(pos);
                QNode qCurrent = current.asQNode();
                
                
                if(qCurrent.getLeftestSection() == qSecCurrent) {
                    templateQ3Left(qCurrent, u, adjOfNode);
                } else if(qCurrent.getRightestSection() == qSecCurrent) {
                    templateQ3Right(qCurrent, u, adjOfNode);
                }
            }
        }
    }
    
    /**
     * Handles the special case for nSmall == nBig. We distinguish between the case that nSmall is a PNode or a QNode.
     * - PNode: No we need to check the condition for L1 and for P1
     * - QNode: check whether we need to change the left or the righter outer section. In both cases we also need to
     *          check for the Q1/Q2 condition.
     *          
     *  After all checks are completed, we choose the correct template for this node.
     * 
     * 
     * @param current the node to be changed according to a template
     * @param u the vertex which needs to be added
     * @param adjOfNode the adjacent vertices in the bag of current
     * @param path the path which needs to be changed
     * @param pos the position of current in path
     */
    private void templateForSmallEqBig(MPQNode current, V u, Set<V> adjOfNode, List<MPQNode> path, int pos) {
        //templates for PNodes
        if (current.getType() == NodeType.PNODE) {
            PNode pCurrent = current.asPNode();

            // for leaves
            if (pCurrent.isLeaf()) {
                templateL1(pCurrent, u, adjOfNode);

                // for 'real' PNodes
            } else {
                templateP1(pCurrent, u, adjOfNode);
            }

            // templates for QNodes
        } else if (current.getType() == NodeType.QNODE) {
            QNode qCurrent = current.asQNode();
            MPQNode pathSection = path.get(pos - 1);

            // differentiate between left outer section and right outer section
            // left outer section
            if (qCurrent.getLeftestSection() == pathSection) {

                // check A subset V_m condition
                if (qCurrent.getRightestSection().getBag().containsAll(adjOfNode)) {
                    templateQ1LeftNBig(qCurrent, u, adjOfNode);
                } else {
                    templateQ2LeftNBig(qCurrent, u, adjOfNode);
                }

                // right outer section
            } else if (qCurrent.getRightestSection() == pathSection) {

                // check A subset V_m condition
                if (qCurrent.getRightestSection().getBag().containsAll(adjOfNode)) {
                    templateQ1RightNBig(qCurrent, u, adjOfNode);
                } else {
                    templateQ2RightNBig(qCurrent, u, adjOfNode);
                }
            }
        }
    }
    
    /**
     * Handles the special case for current == nSmall != nBig. We distinguish between the case that nSmall is a PNode or a QNode.
     * - PNode: No we need to check the condition for L2 and for P2
     * - QNode: check whether we need to change the left or the righter outer section. In both cases we also need to
     *          check for the Q1/Q2 condition.
     *          
     *  After all checks are completed, we choose the correct template for this node.
     * 
     * @param current the node to be changed according to a template
     * @param u the vertex which needs to be added
     * @param adjOfNode the adjacent vertices in the bag of current
     * @param path the path which needs to be changed
     * @param pos the position of current in path
     */
    private void templateForSmall(MPQNode current, V u, Set<V> adjOfNode, List<MPQNode> path, int pos) {
        //templates for PNodes
        if(current.getType() == NodeType.PNODE) {
            PNode pCurrent = current.asPNode();
            
            //template for leaves
            if(pCurrent.isLeaf()) {
                templateL2(pCurrent, u, adjOfNode);
                
            //template for 'real' PNodes
            } else {
                templateP2(pCurrent, u, adjOfNode);
            }
        } else if(current.getType() == NodeType.QNODE) {
            QNode qCurrent = current.asQNode();
            MPQNode pathSection = path.get(pos - 1);

            // differentiate between left outer section and right outer section
            // left outer section
            if (qCurrent.getLeftestSection() == pathSection) {

                // check A subset V_m condition
                if (qCurrent.getRightestSection().getBag().containsAll(adjOfNode)) {
                    templateQ1LeftNSmall(qCurrent, u, adjOfNode);
                } else {
                    templateQ2LeftNSmall(qCurrent, u, adjOfNode);
                }

                // right outer section
            } else if (qCurrent.getRightestSection() == pathSection) {

                // check A subset V_m condition
                if (qCurrent.getRightestSection().getBag().containsAll(adjOfNode)) {
                    templateQ1RightNSmall(qCurrent, u, adjOfNode);
                } else {
                    templateQ2RightNSmall(qCurrent, u, adjOfNode);
                }
            }
        }
    }
    
    private void templateL1(PNode current, V u, Set<V> adj) {
        
    }
    
    private void templateL2(PNode current, V u, Set<V> adj) {
        
    }
    
    private void templateP1(PNode current, V u, Set<V> adj) {
        
    }
    
    private void templateP2(PNode current, V u, Set<V> adj) {
        
    }
    
    private void templateP3(PNode current, V u, Set<V> adj) {
        
    }
    
    private void templateQ1LeftNBig(QNode current, V u, Set<V> adj) {
        
    }
    
    private void templateQ2LeftNBig(QNode current, V u, Set<V> adj) {
        
    }
    
    private void templateQ1RightNBig(QNode current, V u, Set<V> adj) {
        
    }
    
    private void templateQ2RightNBig(QNode current, V u, Set<V> adj) {
        
    }
    
    private void templateQ1LeftNSmall(QNode current, V u, Set<V> adj) {
        
    }
    
    private void templateQ2LeftNSmall(QNode current, V u, Set<V> adj) {
        
    }
    
    private void templateQ1RightNSmall(QNode current, V u, Set<V> adj) {
        
    }
    
    private void templateQ2RightNSmall(QNode current, V u, Set<V> adj) {
        
    }
    
    private void templateQ3Left(QNode current, V u, Set<V> adj) {
        
    }
    
    private void templateQ3Right(QNode current, V u, Set<V> adj) {
        
    }

    
    
    
    
    @Override
    public boolean isIntervalGraph()
    {
        return isIntervalGraph;
    }
    
    @Override
    public List<Interval<Integer>> getIntervalsSortedByStartingPoint()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<V, IntervalVertexPair<V, Integer>> getVertexToIntervalMap()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public Map<Interval<Integer>, V> getIntervalToVertexMap()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    /**
     * TODO: better javadoc
     * 
     * the hole in the graph as an counter example for chordality
     * @return a hole if the graph is not chordal, or null if the graph is chordal.
     */
    public GraphPath<V,E> getHole()
    {
        return chorInspec.getHole();
    }
    
    /**
     * TODO: better javadoc
     * 
     * the Umbrella sub graph in the graph iff the graph is chordal but not an interval graph
     * @return an umbrella if the graph is not an intervalgraph, or null if the graph is an intervalgraph.
     */
    public Graph<V,E> getUmbrellaSubGraph()
    {
        // TODO implement
        return null;
    }
    
    
    
    
    
    
    /**
     * To handel object casting elegant, we use an enum to detect the subclass of a MPQNode.
     * Possible objects in this enum are PNodes, QNodes and QSections
     * @author phoenix
     *
     */
    private enum NodeType {PNODE, QNODE, QSECTION};
    
    /**
     * An abstract class for all MPQNodes, which give the basic pointers and elements of a MPQNode
     * @author phoenix
     *
     */
    private abstract class MPQNode
    {
        MPQNode left;
        MPQNode right;
        MPQNode parent;
        
        NodeType type;
        
        Set<V> bag;
        
        MPQNode(Set<V> bag) {
            this.bag = bag;
        }
        
        MPQNode getParent() {
            return parent;
        }
        
        Set<V> getBag() {
            return bag;
        }

        NodeType getType() {
            return type;
        }
        
        abstract boolean isLeaf();
        abstract PNode asPNode();
        abstract QNode asQNode();
        abstract QSectionNode asSection();
        
    }
    
    private class PNode extends MPQNode
    {
        MPQNode children;
        
        PNode(MPQNode child, Set<V> bag) {
            super(bag);
            super.type = NodeType.PNODE;
            this.children = child;
        }
        
        void add(MPQNode child) {
            child.parent = this;
            if(this.children == null)
            {
                child.left = child;
                child.right = child;
                this.children = child;
            }else {
                child.left = this.children;
                child.right = this.children.left;
                this.children.left.right = child;
                this.children.left = child;
            }
        }
        
        boolean isLeaf() {
            return children == null;
        }
        

        PNode asPNode() {
            return this;
        }
        QNode asQNode() {
            return null;
        }
        QSectionNode asSection() {
            return null;
        }
        
    }
    
    private class QNode extends MPQNode
    {
        MPQNode leftestSection;
        MPQNode rightestSection;
        
        QNode(MPQNode section, Set<V> bag) {
            super(bag);
            super.type = NodeType.QNODE;
            this.leftestSection = section;
            this.rightestSection = section;
        }
        
        MPQNode getLeftestSection() {
            return leftestSection;
        }
        
        MPQNode getRightestSection() {
            return rightestSection;
        }
        
        boolean isLeaf() {
            return false;
        }
        
        PNode asPNode() {
            return null;
        }
        QNode asQNode() {
            return this;
        }
        QSectionNode asSection() {
            return null;
        }
    }
    
    private class QSectionNode extends MPQNode
    {
        MPQNode child;
        
        QSectionNode(MPQNode child, Set<V> bag) {
            super(bag);
            super.type = NodeType.QSECTION;
            
        }
        
        boolean isLeaf() {
            return false;
        }
        
        PNode asPNode() {
            return null;
        }
        QNode asQNode() {
            return null;
        }
        QSectionNode asSection() {
            return this;
        }
    }
}
