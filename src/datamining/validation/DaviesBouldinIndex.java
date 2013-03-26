/**
Copyright (c) 2013, The EDMOAL Project

	Roland Winkler
	Richard-Wagner Str. 42
	10585 Berlin, Germany
	roland.winkler@gmail.com
 
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
    	this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice,
    	this list of conditions and the following disclaimer in the documentation and/or
    	other materials provided with the distribution.
    * The name of Roland Winkler may not be used to endorse or promote products
		derived from this software without specific prior written permission.

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
package datamining.validation;

import java.util.ArrayList;

import data.algebra.Metric;
import data.algebra.VectorSpace;
import datamining.clustering.ClusteringAlgorithm;
import datamining.clustering.protoype.Prototype;
import datamining.resultProviders.CrispNoiseClusteringProvider;
import datamining.resultProviders.FuzzyClusteringProvider;
import datamining.resultProviders.FuzzyNoiseClusteringProvider;


/**
 * Calculates the Davies - Bouldin index. The complexity depends on how the pairwise distances of clusters and the
 * diameter of one cluster is calculated.<br>
 * 
 * Complexity for pairwise prototype calculations: O(n*c + c^2).
 * Complexity for pairwise data object calculations: O(n^2*c^2),
 *  with n being the number of data objects and c being the number of clusters.<br>    
 * 
 * See paper: D.L. Davies and D.W. Bouldin. A Cluster Separation Measure.IEEE Trans. on Pattern Analysis and Machine Intelligence (PAMI) 1(4):224–227. IEEE Press, Piscataway, NJ, USA 1979
 * 
 * @param fuzzyAlgorithm The fuzzy clustering algorithm containing the clustering result
 * @param vs The vector space used for the clustering process
 * @param dist The distance metric.
 * @return The Davies - Bouldin index for the specified clustering result.
 */
public class DaviesBouldinIndex<T> extends ClusterValidation<T>
{
	/**
	 * @param clusterInfo
	 */
	public DaviesBouldinIndex(ClusteringInformation<T> clusterInfo)
	{
		super(clusterInfo);
	}

	public double index()
	{	
		this.clusterInfo.checkClusterDistances();
		this.clusterInfo.checkClusterDiameters();
		
		// maximal diameter of cluster		
		double maxRatio = 0.0d;
		double maxRatioSum = 0.0d;
		double doubleTMP;
		
		for(int i=0; i<this.clusterInfo.getClusterCount(); i++)
		{
			maxRatio = 0.0d;
			for(int k=i+1; k<this.clusterInfo.getClusterCount(); k++)
			{
				doubleTMP = (this.clusterInfo.getClusterDiameters()[i] + this.clusterInfo.getClusterDiameters()[k])/this.clusterInfo.getClusterDistances()[i][k];
				maxRatio = (maxRatio < doubleTMP)? doubleTMP : maxRatio;
			}
			maxRatioSum += maxRatio;
		}
		
		return maxRatioSum/this.clusterInfo.getClusterCount();
	}
}
