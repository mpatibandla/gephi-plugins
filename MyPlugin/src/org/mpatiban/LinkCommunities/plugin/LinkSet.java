/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mpatiban.LinkCommunities.plugin;

/**
 *
 * @author Manasa
 */
import java.util.Set;
public class LinkSet<T> {
    
    public void union(Set<T> S1,Set<T> S2,Set<T> S3)
    {
    S3.clear();
    S3.addAll(S1);
    S3.addAll(S2);
    }
    
    public void intersection(Set<T> S1,Set<T> S2,Set<T> S3)
    {
        S3.clear();
        S3.addAll(S1);
        S3.retainAll(S2);
    }
    
}
