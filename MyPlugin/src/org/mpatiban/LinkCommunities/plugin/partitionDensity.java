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
public class partitionDensity {
    public double partitionDensityCommunity(int m, int n) {
        if (n <= 2) {
            return 0.0;
        }
        return m * (m - n + 1.0) / (n - 2.0) / (n - 1.0);
    }
    
}
