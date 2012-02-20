/**
Copyright (c) 2011, The EDMOAL Project

	DLR Deutsches Zentrum fuer Luft- und Raumfahrt e.V.
	German Aerospace Center e.V.
	Institut fuer Flugfuehrung/Institute of Flight Guidance
	Tel. +49 531 295 2500, Fax: +49 531 295 2550
	WWW: http://www.dlr.de/fl/		
 
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
    	this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice,
    	this list of conditions and the following disclaimer in the documentation and/or
    	other materials provided with the distribution.
    * Neither the name of the DLR nor the names of its contributors
    	may be used to endorse or promote products derived from this software
    	without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
THE POSSIBILITY OF SUCH DAMAGE.
*/


package datamining.clustering;

import data.algebra.Metric;
import datamining.DataMiningAlgorithm;

/**
 * The basic interface for all clustering algorithms. Clustering, in the sense of unsupervised classification.
 *  
 * There are two kinds of clusters,  active clusters and inactive clusters. If an algorithm
 * provides a dynamic number of clusters, only active clusters are interesting. But if a clustering
 * algorithm requires the number of clusters be known in advance to starting the clustering process,
 * active and inactive clusters becomes more interesting.<br>
 * 
 * Inactive clusters are clusters that are available in principle, but which have no data objects assigned to.
 * So if the number of clusteres is fixed from the start, the concept of inactive clusters allows
 * to adjust the number of relevant (=active) clusters. Usually, inactive clusters do not participate 
 * in the clustering procedure.<br>
 * 
 * Very abstract speaking, clustering is an analysis of the distances among data objects. Naturally,
 * that distance needs to be measurable in order to do any analysis. Therefore, a metric is required for all clustering
 * algorithms, independent of their nature.
 *
 * @author Roland Winkler
 */
public interface ClusteringAlgorithm<T> extends DataMiningAlgorithm<T>
{
	/**
	 * Returns the total number of clusters.
	 * 
	 * @return the total number of clusters.
	 */
	public int getClusterCount();
	
	/**
	 * Returns the number of active clusters.
	 * 
	 * @return the number of active clusters.
	 */
	public int getActiveClusterCount();
	
	
	/**
	 * Returns the number of inactive clusters.
	 * 
	 * @return the number of inactive clusters.
	 */
	public int getInactiveClusterCount();
	
	
	/**
	 * Returns the metric that is required to measure the distance among data objects or arbitrary locations
	 * and data objects.
	 * 
	 * @return the metric.
	 */
	public Metric<T> getMetric();
}
