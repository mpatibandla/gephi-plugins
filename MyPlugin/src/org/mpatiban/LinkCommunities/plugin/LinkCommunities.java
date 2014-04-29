/*
Copyright 2008-2013 Gephi
Authors : Danilo Domenino <danilodomenino@yahoo.it>, Massimiliano Vella <vella.massi@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2012 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

 Portions Copyrighted 2011 Gephi Consortium.
 */

package org.mpatiban.LinkCommunities.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.graph.api.Node;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalUndirectedGraph;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;


public class LinkCommunities implements Statistics, LongTask {

    private String report = "";
    private boolean cancel = false;
    private ProgressTicket progressTicket; //progress ticket
    private double threshold; //threshold value
    private double density;
    //private double elapsedTime;
    private long startTime, endTime, duration;
    private partitionDensity pd;
    
    int cid1,cid2;
    double d_cid12;
    ArrayList<HashSet<Edge>> edgesComm = new ArrayList<HashSet<Edge>>();     //creating array list of edges
    ArrayList<HashSet<Node>> nodesComm = new ArrayList<HashSet<Node>>();     //creating array list of nodes
        
    @Override
    @SuppressWarnings("empty-statement")
    public void execute(GraphModel gm, AttributeModel am) {
        
        HierarchicalUndirectedGraph hdg = gm.getHierarchicalUndirectedGraph();   //graph
        List<EdgePair> heapList = new ArrayList<EdgePair>();    //lists for edge pairs
        AttributeTable edgeTable = am.getEdgeTable();           //edge attribute lists
        AttributeColumn attrCol;                                //attribute
        startTime = System.currentTimeMillis();         
        hdg.readLock();                                     //acquiring read lock on the graph
        if (edgeTable.getColumn("community") == null)//if there is no such column
        {
            attrCol = edgeTable.addColumn("community", "Community", AttributeType.INT, AttributeOrigin.COMPUTED, 0);
            //adding column to the table with id community, Name Community, INTEGER TYPE OF ATTRIBUTE COLUMN, Computed is the origin of the attribute, where the attribute is obtained as it is computed in the program,
            //value of the object by default is 0
        }

        Edge[] edges = hdg.getEdges().toArray();    //getedges() returns edges contained in the graph
        Node[] nodes = hdg.getNodes().toArray();    //getnodes() returns nodes contained in the graph

        LinkSet<Node> st = new LinkSet<Node>();         //creating set of nodes
        LinkSet<Edge> ste = new LinkSet<Edge>();        //creating set of edges


        int cid = 0;

        //one edge for each community
        for (int i = 0; i < edges.length; i++) {
            edges[i].getEdgeData().getAttributes().setValue("community", cid);//edge i get the data of edge and attributes in particular and set the value of attributes as placed in community and cid
            edgesComm.add(new HashSet<Edge>());//add hashset of the edge into edgesComm
            edgesComm.get(cid).add(edges[i]);//add edge with cid into edgesComm
            nodesComm.add(new HashSet<Node>());//add hashset of the node into nodesComm
            nodesComm.get(cid).add(edges[i].getSource());//add the node as source on one end of the edge
            nodesComm.get(cid).add(edges[i].getTarget());//add the node as target on other end of the edge
            cid++;//cid community id
        }
                           Node value1,value2;
        for (int i = 0; i < nodes.length; i++) { //for all the nodes
            Edge[] adj = hdg.getEdgesAndMetaEdges(nodes[i]).toArray(); //get all the involved edges and meta edges for each node and add to the node
            int len = adj.length; //adj-length of the array
//System.out.println("Adj.length"+len); test case of 5 nodes, test.gephi, adj.length = 4 for all nodes, clique with partition density=1.0
            if (len > 1) {
                //create the edges combinations
                //"[18,0]"-> nodes[i],adj[j]
              
                for (int j = 0; j < len - 1; j++) {
                   // Node value1;
                    if (adj[j].getSource().equals(nodes[i])) {
                        value1 = adj[j].getTarget();
                        //System.out.println("j = 0 to Length-2; GET TARGET ID1 \t"+value1.getId());
                    } else {
                        value1 = adj[j].getSource();
                        //System.out.println("j = 0 to length-2; GET SOURCE ID1 \t"+value1.getId());
                    }
                    for (int k = j + 1; k < len; k++) {
                     //   Node value2;
                        if (adj[k].getSource().equals(nodes[i])) {
                            value2 = adj[k].getTarget();
                            //System.out.println("k = j+1 to len-1; GET TARGET ID2 \t"+value2.getId());
                        } else {
                            value2 = adj[k].getSource();
                            //System.out.println("k = j+1 to len-1; GET SOURCE ID2 \t"+value2.getId());
                        }
                    
                        int z=0,y=0;
                        
                        Node[] ngbs1 = hdg.getNeighbors(value1).toArray();
                        Node[] ngbs2 = hdg.getNeighbors(value2).toArray();
//              for(; z < ngbs1.length && y < ngbs2.length ;z++,y++)
  //            {
                  //System.out.println("ngbs1\t"+ngbs1+"-------ngbs2\t"+ngbs2);
    //          }
                        Set<Node> a = new HashSet<Node>(Arrays.asList(ngbs1));
                        a.add(value1);
                        Set<Node> b = new HashSet<Node>(Arrays.asList(ngbs2));
                        b.add(value2);
                        Set<Node> u = new HashSet<Node>();
                        Set<Node> inter = new HashSet<Node>();
                        st.union(a, b, u);
                        st.intersection(a, b, inter);
                        double similarity = 1.0 * inter.size() / u.size();
                        EdgePair ep = new EdgePair(1 - similarity, adj[j], adj[k]);
                        heapList.add(ep);
        //System.out.println("value 1\t"+value1+"-----value 2\t"+value2);           
                    }
                }
            //System.out.println("value 1\t"+value1+"\nvalue 2\t"+ value2);   }
                } }
        
        
        //push in structure every EdgePair combinations  
        MinHeap<EdgePair> heap = new MinHeap<EdgePair>(heapList); // heap with edge pairs

        double sim_prev = -1.0;    // previous similarity
        double best_sim = 1.0;     //best similarity
        double best_dens = 0.0;    //best density
        Progress.start(progressTicket, hdg.getEdgeCount());    //Start the progress indication for a task with known number of steps
  //here the no. of steps is the edge count from the graph
        int heap_count = 0;
        int count = 0;


        while (!heap.isEmpty()) {
            EdgePair temp = heap.remove(); //remove each edge pair from the heap
            double sim = 1 - temp.getDistance();  //similarity variable = 1- distance of that edge pair

            if (sim < threshold) {
                break;
            }
            if (sim != sim_prev) {    //if similarity variable not equa to the prev one, then check for density
                if (density >= best_dens) {  //if density > existing best density, change
                    best_dens = density;
                    best_sim = sim;
                }
                sim_prev = sim; //current sim becomes previous sim for the next iteration
            }
             cid1 = (Integer) (temp.getEdge1().getAttributes().getValue("community")); //for edge 1
            cid2 = (Integer) (temp.getEdge2().getAttributes().getValue("community")); //for edge 2
//System.out.println("Cid1->"+cid1+"---\tcid2->"+cid2);

            if (cid1 != cid2) { //community id for edge1 != community id for edge 2
                count++; 
                int m1 = edgesComm.get(cid1).size();  //edge community for cid1
                int m2 = edgesComm.get(cid2).size();  //edge community for cid2
                int n1 = nodesComm.get(cid1).size();  //node community for cid1
                int n2 = nodesComm.get(cid2).size();  //node community for cid2

pd = new partitionDensity();
                //partition density for communities
                double d_cid1 = pd.partitionDensityCommunity(m1, n1); //edges nd nodes given into find partition density for community
                //null point exception
                double d_cid2 = pd.partitionDensityCommunity(m2, n2);
                d_cid12 = 0.0;   

                if (m1 >= m2) {   //if community size for m1>m2,
                    HashSet<Edge> edgesTemp = new HashSet<Edge>();  //temporary edges
                    ste.union(edgesComm.get(cid2), edgesComm.get(cid1), edgesTemp);  //set theory function, union of cid1, cid2 into temp hashset
                    HashSet<Node> nodesTemp = new HashSet<Node>();  //same for nodes
                    st.union(nodesComm.get(cid2), nodesComm.get(cid1), nodesTemp);
                    nodesComm.set(cid1, nodesTemp);  //at index cid1 for nodescomm, nodestemp is replaced
                    Iterator<Edge> it = edgesComm.get(cid2).iterator();  //?
                    while (it.hasNext()) {  //while iteration continues, add that to the community
                        it.next().getEdgeData().getAttributes().setValue("community", cid1);
                    }
                    edgesComm.get(cid2).clear();  //all at edgesTemp
                    edgesComm.get(cid1).clear();
                    nodesComm.get(cid2).clear(); //same

                    edgesComm.set(cid1, edgesTemp); //at index cid1 for edgescomm, edgestemp is replaced


                    int m = edgesComm.get(cid1).size();  //for edges as in cid1(obtained by merging)
                    int n = nodesComm.get(cid1).size();  //for nodes as in cid1(obtained by merging)
                    d_cid12 = pd.partitionDensityCommunity(m, n);   //obtaining partition density for the current structure
                    report= report + "Edge Pairs"+edgesComm.get(cid1)+"----"+"involved nodes"+nodesComm.get(cid1)+"\npartition density for the set "+d_cid12+"\n";
  
              } else {  //else
                    HashSet<Edge> edgesTemp = new HashSet<Edge>();
                    ste.union(edgesComm.get(cid2), edgesComm.get(cid1), edgesTemp);
                    HashSet<Node> nodesTemp = new HashSet<Node>();
                    st.union(nodesComm.get(cid2), nodesComm.get(cid1), nodesTemp);
                    nodesComm.set(cid2, nodesTemp);
                    Iterator<Edge> it = edgesComm.get(cid1).iterator();
                    while (it.hasNext()) {
                        it.next().getEdgeData().getAttributes().setValue("community", cid2);
                    }
                    edgesComm.get(cid2).clear();
                    edgesComm.get(cid1).clear();
                    nodesComm.get(cid1).clear();
                    edgesComm.set(cid2, edgesTemp); //do the same but now with cid2 as m2>m1
                    int m = edgesComm.get(cid2).size();
                    int n = nodesComm.get(cid2).size();
                    d_cid12 = pd.partitionDensityCommunity(m, n);
 
                   report= report +"Edge Pairs "+ edgesComm.get(cid2)+"----"+"involved nodes"+nodesComm.get(cid2)+"\npartition density for the set "+d_cid12+"\n";
                }
   
                report=report + "Density till now is"+density+"\n";
                density = density + (d_cid12 - d_cid1 - d_cid2) * (2.0 / edges.length); //after all that, calculating the density
                //d.display(density);
            }
            if (cancel) {
                break;
            }
            heap_count++;
            Progress.progress(progressTicket, heap_count);
        }
        endTime = System.currentTimeMillis();
        duration = endTime - startTime;
    
    }
    @Override
    //displaying
    public String getReport() {
        report = "<HTML> <BODY> <h1> Link Communities Report </h1> "
                + "<hr>"
                + "<br />" + "<h2> Parameters: </h2>"
                + "Network Interpretation: undirected <br />"
                + "Threshold: " + threshold + " <br />"
                + "<br>" + "<h2> Results: </h2>"
                + "Partition Density : " + density + "<br />"
                + "Time taken to calculate Partition Density " + duration + "ms <br />" 
                + report
                + "<br/> <h2>Reference: </h2> <br/> "
                + "\"Yong-Yeol Ahn, James P. Bagrow & Sune Lehmann\" \"Link communities reveal multiscale complexity in networks\" 2010"
                + "</BODY> </HTML>";
        return report;
    }
    
    @Override
    public boolean cancel() {
        cancel = true;
        return cancel;
    }

    @Override
    public void setProgressTicket(ProgressTicket pt) {
        this.progressTicket = pt;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

   }

