/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mpatiban.LinkCommunities.plugin;

import org.gephi.graph.api.Node;

public abstract class NodePair implements Comparable<NodePair> {

    private double distance;
    private Node node1;
    private Node node2;

    public NodePair() {
    }

    public NodePair(double distance, Node edge1, Node edge2) {
        this.distance = distance;
        this.node1 = node1;
        this.node2 = node2;
    }

    public Node getNode1() {
        return node1;
    }

    public void setNode1(Node node1) {
        this.node1 = node1;
    }

    public Node getNode2() {
        return node2;
    }

    public void setNode2(Node node2) {
        this.node2 = node2;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    public int compareTo(NodePair o) {
        if (this.getDistance() < o.getDistance()) {
            return -1;
        } else if (this.getDistance() == o.getDistance()) {
            return 0;
        }
        return 1;
    }
}
