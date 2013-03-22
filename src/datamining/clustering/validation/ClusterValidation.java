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


package datamining.clustering.validation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import data.algebra.Metric;
import data.algebra.VectorSpace;
import data.set.IndexedDataObject;
import data.set.IndexedDataSet;
import datamining.clustering.ClusteringAlgorithm;
import datamining.clustering.protoype.Centroid;
import datamining.clustering.protoype.Prototype;
import datamining.resultProviders.CrispClusteringProvider;
import datamining.resultProviders.CrispNoiseClusteringProvider;
import datamining.resultProviders.FuzzyClusteringProvider;
import datamining.resultProviders.FuzzyNoiseClusteringProvider;
import datamining.resultProviders.PrototypeProvider;

/**
 * This class provides a collection of standard cluster validation indices.
 * Some of the indices compare data objects pairwise. That is quite some effort for large data
 * sets. Therefore, there are two options, <code>PAIRWISE_DATAOBJECTS</code> and <code>PAIRWISE_PROTOTYPES</code>.
 * The former (<code>PAIRWISE_DATAOBJECTS</code>) indicates, that the exact, but cost intensive method is used.
 * The latter calculates first the weighted mean in form of a virtual prototype of a cluster and calculates
 * the prototype positions for calculations.<br>
 * 
 * The second option, if the cluster size is calculated <code>LINEAR</code> or  <code>SQUARED</code> is not
 * a performance option but rather how the user likes it to be applied either one might work.<br>
 * 
 * For all algorithms in this class, a paper reference is provided. However, even though the original
 * papers are cited, the actual implementations are followed by the habilitation thesis of Christian Borgelt:<br>
 *
 * See: Borgelt, C. Prototype-based Classification and Clustering, Otto-von-Guericke-University of Magdeburg, Germany, 2005 http://www.borgelt.net/habil.html
 * 
 * @TODO: implement consistent cluster diameter calculations, especially some that do not require a vector space.
 *
 * @author Roland Winkler
 */
public abstract class ClusterValidation<T>  implements Serializable
{
	protected ClusteringInformation<T> clusterInfo;
	
	/**
	 * @param clusterInfo
	 */
	public ClusterValidation(ClusteringInformation<T> clusterInfo)
	{
		super();
		this.clusterInfo = clusterInfo;
	}

	public abstract double index();

	/**
	 * @return the clusterInfo
	 */
	public ClusteringInformation<T> getClusterInfo()
	{
		return this.clusterInfo;
	}

	/**
	 * @param clusterInfo the clusterInfo to set
	 */
	public void setClusterInfo(ClusteringInformation<T> clusterInfo)
	{
		this.clusterInfo = clusterInfo;
	}
}
