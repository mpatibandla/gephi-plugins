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

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
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
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.plugin.multilevel.MultiLevelLayout;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2Builder;
import org.gephi.layout.plugin.fruchterman.FruchtermanReingold;
import org.gephi.layout.plugin.fruchterman.FruchtermanReingoldBuilder;
import org.gephi.layout.plugin.multilevel.MultiLevelLayout;
import org.gephi.layout.plugin.multilevel.YifanHuMultiLevel;
import org.gephi.partition.api.PartitionController;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;


public class LinkCommunities implements Statistics, LongTask {

    private String report = "";
    private boolean cancel = false;
    private ProgressTicket progressTicket; //progress ticket
    private double threshold; //threshold value
    private double density;
    //private double elapsedTime;
    private long startTime=0, endTime=0, duration=0, LayoutEnd=0, LayoutStart=0, Layout_Duration1=0, Layout_Duration2=0, Layout_Duration=0;
    private partitionDensity pd;
    private ReportTopComponent r;
    int cid1,cid2;
    double d_cid12,dense;
    ArrayList<HashSet<Edge>> edgesComm = new ArrayList<HashSet<Edge>>();     //creating array list of edges
    ArrayList<HashSet<Node>> nodesComm = new ArrayList<HashSet<Node>>();     //creating array list of nodes
    
    @Override
    @SuppressWarnings("empty-statement")
    public void execute(GraphModel gm, AttributeModel am) {
        
        HierarchicalUndirectedGraph hdg = gm.getHierarchicalUndirectedGraph();   //graph
        List<EdgePair> heapList = new ArrayList<EdgePair>();    //lists for edge pairs
        AttributeTable edgeTable = am.getEdgeTable();           //edge attribute lists
        AttributeTable nodeTable = am.getNodeTable();
        AttributeColumn attrCol,attrColor;                                //attribute
        PartitionController partitionController = Lookup.getDefault().lookup(PartitionController.class);
        
        startTime = System.currentTimeMillis();         
        float color1,color2,color3;
            Random rn = new Random();
            
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
        for (int i = 0; i < nodes.length; i++)  { //for all the nodes
            Edge[] adj = hdg.getEdgesAndMetaEdges(nodes[i]).toArray(); //get all the involved edges and meta edges for each node and add to the node
            int len = adj.length; //adj-length of the array
            if (len > 1) {
                for (int j = 0; j < len - 1; j++) {
                    // Node value1;
                    if (adj[j].getSource().equals(nodes[i])) {
                        value1 = adj[j].getTarget();
                    } else {
                        value1 = adj[j].getSource();
                    }
                    for (int k = j + 1; k < len; k++) {
                        //   Node value2;
                        if (adj[k].getSource().equals(nodes[i])) {
                            value2 = adj[k].getTarget();
                        } else {
                            value2 = adj[k].getSource();
                        }
                        int z=0,y=0;
                        Node[] ngbs1 = hdg.getNeighbors(value1).toArray();
                        Node[] ngbs2 = hdg.getNeighbors(value2).toArray();
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
                        
                    }
                }
                } }
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
            
            if (cid1 != cid2) { //community id for edge1 != community id for edge 2
                count++; 
                color1 = rn.nextInt(255);
            color2 = rn.nextInt(255);
            color3 = rn.nextInt(255);
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
                    System.out.println("COLOR1:"+color1);
                    for(Edge edge : edgesComm.get(cid1)){
                        edge.getEdgeData().setColor(color1/255,color2/255,color3/255);
                       //edge.getEdgeData().setG((255*(100-color1))/100);
                       //edge.getEdgeData().setB(255-color2);
                      }
                    for (Node node: nodesComm.get(cid1)){
                       node.getNodeData().setColor(color1/255,color2/255,color3/255);
                       //node.getNodeData().setG(255*(100-color1)/100);
                       //node.getNodeData().setB(255-color2);
                       }
                } 
                else {  //else
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
                    System.out.println("COlor2:"+color2);
                    for(Edge edge : edgesComm.get(cid2)){
                       edge.getEdgeData().setColor(color2/255,color1/255,color3/255);
                       
                   }
                   for (Node node: nodesComm.get(cid2)){
                       node.getNodeData().setColor(color2/255,color1/255,color3/255);
                       
                   }
                   
                }
                density = density + (d_cid12-d_cid1-d_cid2)*(2.0/edges.length); //after all that, calculating the density
            }
            if (cancel) {
                break;
                }
            heap_count++;
            Progress.progress(progressTicket, heap_count);
        }
        LayoutStart = System.currentTimeMillis();
        if(edges.length>20){
        ForceAtlas2Builder fb = new ForceAtlas2Builder();
        fb.buildLayout();
        ForceAtlas2 fa = new ForceAtlas2(fb);
        fa.setGraphModel(gm);
        fa.initAlgo();
        int temp;
        if(edges.length>2000)
            temp=1000;
        else
            temp=edges.length;
        for(int i=0;i<temp;i++){
        if(fa.canAlgo())
            fa.goAlgo();
        else
            fa.endAlgo();
        System.out.println(i);
        }   
        LayoutEnd = System.currentTimeMillis();
        
        Layout_Duration1 = LayoutEnd - LayoutStart;
        System.out.println(Layout_Duration1);
             LayoutStart = System.currentTimeMillis();
             if(Layout_Duration1>10000&&Layout_Duration1<20000){
             Layout2(500,gm);
             }
             else if(Layout_Duration1>20000){
             Layout2(20,gm);
             }
             else{
              Layout2(1000,gm);
             }
        LayoutEnd = System.currentTimeMillis();
        }
        Layout_Duration2 = LayoutEnd - LayoutStart;
        endTime = System.currentTimeMillis();
        duration = endTime - startTime;
        Layout_Duration = Layout_Duration1 + Layout_Duration2;
    }
    
    @SuppressWarnings("override")
            public String getReport() {
        
        report = "<html> <body> <h1><center> Link Communities Report </h1> "
                + "<hr>"
                + "<br />" + "<h2><center><u> Parameters </h2><br/>"
                + "<b><center>Network Interpretation: </b><center>undirected <br />"
                + "<b><center>Threshold: </b><center>" + threshold + " <br />"
                +"---------------------------------------------------------------"
                + "<br />" + "<h2><center><u> Results </u></h2><br/>"
                + "<center><b>Partition Density : </b>" + density + "<br /></center>"
                + "<br /><table align=\"center\"><tr><td>"
                + "<b>Time taken for Layout </b></td><td>" + Layout_Duration +"ms <br /></td></tr>"
                + "<tr><td><b>Total time taken </b></td><td>" + duration + "ms <br /></td></tr></table>" 
                + "<hr>"
                + "<br /><center> <h2><u>Reference: </h2> <br/> "
                + "<b>Yong-Yeol Ahn, James P. Bagrow & Sune Lehmann --- 'Link communities reveal multiscale complexity in networks, 2010'</b>"
                + "</body> </html>";
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

    private void Layout2(int n, GraphModel gm) {
        FruchtermanReingoldBuilder frb = new FruchtermanReingoldBuilder();
             frb.buildLayout();
             FruchtermanReingold fr;
            fr = new FruchtermanReingold(frb);
             fr.setGraphModel(gm);
             fr.setArea(10000.0f);
             fr.setGravity(10.0);
             fr.setSpeed(1.0);
             fr.initAlgo();
                 for(int i=0;i<n;i++){
        if(fr.canAlgo())
            fr.goAlgo();
        else
            fr.endAlgo();
        System.out.println(i);
    }

   }
}

//What has changed since the last update?
    //Approaches Used:
        //Tried changing the view with similar approach as cid taking color
            //Did not work, since it was not unique and not possible
        //Partition API used
            //Not possible due to some data structures and use of Iterator
        //Add Column - color for edges and nodes
            //Did not work since colors have to be different for each edge/node, and each row/column could not be generated that way
        //Simply coloring the edges and nodes as per community formation
            //Sounded too simple
        //Started working with Layouts
            //Force Atlas 2 is the most commonly used Layout
            //Implemented Force Atlas2
            //Coloring as per the formation done for edges and nodes
            //Still the graph looked clustered
        //Including another Layout
            //Fruchterman Reingold and Yifan Hu Multilevel Layout -- both implemented
            //Multilevel Layout provides Coarsening Strategy that would either coursen or refine the graph
            //Shows very good results
            //But takes lot of time 
            //Fruchterman Reingold -- Time complexity varies with the number of iterations
            //For very large graphs, does not make much changes after some iterations.
            //Average iterations observed, and implemented in the similar way
            //Lot of trial and error for diffrent sizes of graphs was observed and the values were taken
            //Coloring was randomized by using a random function and RGB Values of Edges and Nodes
    //Report
        //More detail
        //Decorative change in the look
        //Time taken is reported