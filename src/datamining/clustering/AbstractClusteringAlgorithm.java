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
import data.set.IndexedDataSet;
import datamining.AbstractStaticDataMiningAlgorithm;
import datamining.resultProviders.ResultProvider;
 
/**
 * Extends the {@link AbstractStaticDataMiningAlgorithm} class by adding the basic functionality of a clustering algorithms.
 * Very abstract speaking, clustering is an analysis of the distances among data objects. Naturally,
 * that distance needs to be measurable in order to do any analysis. Therefore, a metric is required for all clustering
 * algorithms, independent of their nature.
 *
 * @author Roland Winkler
 */
public abstract class AbstractClusteringAlgorithm<T> extends AbstractStaticDataMiningAlgorithm<T> implements ClusteringAlgorithm<T>, ResultProvider<T>
{	
	/**  */
	private static final long	serialVersionUID	= 4356769460943616262L;

	
	/**
	 * A metric for measuring the distance between objects of type <code>T</code>, that can be data objects
	 * or other locations in the feature space.
	 */
	protected final Metric<T> metric;
	
	/**
	 *	The initial constructor for clustering. The number of clusters can be changed after initialization, but it
	 * is not recommended because some algorithms have to be reinitialized.
	 * 
	 * @param data The data set that is to be analyzed.
	 * @param metric The metric for calculating distances in the feature space.
	 */
	public AbstractClusteringAlgorithm(IndexedDataSet<T> data, Metric<T> metric)
	{
		super(data);
		
		this.metric = metric;
	}
	
	/**
	 * A copy constructor.
	 * 
	 * @param c the elders clustering algorithm object
	 */
	public AbstractClusteringAlgorithm(AbstractClusteringAlgorithm<T> c)
	{
		super(c);
		
		this.metric = c.metric;
	}

	/**
	 * Returns the metric that is required to measure the distance among data objects or arbitrary locations
	 * and data objects.
	 * 
	 * @return the metric.
	 */
	public Metric<T> getMetric()
	{
		return this.metric;
	}	
}
