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
package datamining.clustering.validation;

import java.util.ArrayList;
import java.util.List;

import data.algebra.Metric;
import data.algebra.VectorSpace;
import data.set.IndexedDataSet;
import datamining.clustering.ClusteringAlgorithm;
import datamining.clustering.protoype.Prototype;
import datamining.resultProviders.CrispClusteringProvider;
import datamining.resultProviders.CrispNoiseClusteringProvider;
import datamining.resultProviders.FuzzyClusteringProvider;
import datamining.resultProviders.FuzzyNoiseClusteringProvider;
import datamining.resultProviders.PrototypeProvider;


/**
 * Calculates the Bezdec separation index. It is actually a more robust form of Dunns separation index, but
 * since bezdec made the more importent alterations and Dunns index is not valid for fuzzy membership values,
 * it is called Bezdec separation index here. The complexity depends on how the pairwise distances of clusters and the
 * diameter of one cluster is calculated.<br>
 * 
 * Complexity for pairwise prototype calculations: O(n*c + c^2).
 * Complexity for pairwise data object calculations: O(n^2*c^2),
 *  with n being the number of data objects and c being the number of clusters. <br>   
 * 
 * See paper: J.C. Bezdek, W.Q. Li, Y. Attikiouzel, and M. Wind-ham. A Geometric Approach to Cluster Validity for Normal Mixtures. Soft Computing 1(4):166–179. Springer-Verlag, Heidelberg, Germany 1997<br>
 * See paper: J.C. Dunn. A Fuzzy Relative of the ISODATA Process and Its Use in Detecting Compact Well-Separated Clusters.Journal of Cyber-netics3(3):32–57. American Society for Cybernetics, Washington, DC, USA 1973 Reprinted in [Bezdek and Pal 1992], 82–101<br>
 * 
 * @param fuzzyAlgorithm The fuzzy clustering algorithm containing the clustering result
 * @param vs The vector space used for the clustering process
 * @param dist The distance metric.
 * @return The Bezdec separation index (fuzzy Dunn separation index) for the specified clustering result.
 */
public class BezdecSeperationIndex extends ClusterValidation
{
	
	/**
	 * @param clusterInfo
	 */
	public BezdecSeperationIndex(ClusteringInformation clusterInfo)
	{
		super(clusterInfo);
		
	}

	public double index()
	{
		this.clusterInfo.checkClusterDistances();
		this.clusterInfo.checkClusterDiameters();
		
		double minClusterDistance = Double.POSITIVE_INFINITY;
		for(int i=0; i<this.clusterInfo.getClusterCount(); i++)
		{
			for(int k=i+1; k<this.clusterInfo.getClusterCount(); k++)
			{
				if(this.clusterInfo.getClusterDistances()[i][k] < minClusterDistance) minClusterDistance = this.clusterInfo.getClusterDistances()[i][k];
			}
		}
		
		// maximal diameter of cluster	
		double maxDiameter = Double.NEGATIVE_INFINITY;
		for(int i=0; i<this.clusterInfo.getClusterCount(); i++)
		{
			if(this.clusterInfo.getClusterDiameters()[i] > maxDiameter) maxDiameter = this.clusterInfo.getClusterDiameters()[i];
		}
		
		return minClusterDistance/maxDiameter;
	}


}
