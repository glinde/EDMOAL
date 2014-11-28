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
import java.util.List;

import data.set.IndexedDataObject;

/**
 * Calculates the partition entropy of the fuzzy clustering result.<br>
 * 
 * The complexity of the function is in O(n*c) with n being the number of data objects and c being the number of clusters.<br> 
 * 
 * See paper: J.C. Bezdek.Pattern Recognition with Fuzzy Objective Function Algorithms. Plenum Press, New York, NY, USA 1981
 *
 * @param fuzzyAlgorithm  The fuzzy clustering algorithm containing the clustering result
 * @return The partition coefficient of the specified clustering result.
 */
public class PartitionEntropy<T> extends ClusterValidation<T>
{
	/**
	 * @param clusterInfo
	 */
	public PartitionEntropy(ClusteringInformation<T> clusterInfo)
	{
		super(clusterInfo);
	}
	
	public double index()
	{
		this.clusterInfo.checkFuzzyClusteringProvider_FuzzyClusteringResult();
		if(this.clusterInfo.getNoiseDistance() >= 0.0) return this.noiseIndex(); 
		
		double sum = 0.0d;
		int i;		
		
		if(this.clusterInfo.getFuzzyClusteringResult() != null)
		{
			for(double[] membershipValues : this.clusterInfo.getFuzzyClusteringResult())
			{
				for(i=0; i<this.clusterInfo.getClusterCount(); i++) sum += membershipValues[i] * Math.log(membershipValues[i])/Math.log(2.0d);
			}	
		}
		else
		{
			double[] membershipValues;
			for(IndexedDataObject<T> d : this.clusterInfo.getFuzzyClusteringProvider().getDataSet())
			{
				membershipValues = this.clusterInfo.getFuzzyClusteringProvider().getFuzzyAssignmentsOf(d);
				for(i=0; i<this.clusterInfo.getClusterCount(); i++) sum += membershipValues[i] * (membershipValues[i]>0.0? Math.log(membershipValues[i])/Math.log(2.0d) : 0.0d);
			}
		}

		return -sum / ((double)this.clusterInfo.getDataCount() * Math.log((double)this.clusterInfo.getClusterCount())/Math.log(2.0d));
	}

	public double noiseIndex()
	{
		this.clusterInfo.checkFuzzyClusteringProvider_FuzzyClusteringResult();
		this.clusterInfo.checkNoiseClusterMembershipValues();
		
		double sum = 0.0d;
		double[] noiseMemberships = this.clusterInfo.getNoiseClusterMembershipValues();
		
		if(this.clusterInfo.getFuzzyClusteringResult() != null)
		{
			for(double[] membershipValues : this.clusterInfo.getFuzzyClusteringResult())
			{
				for(int i=0; i<this.clusterInfo.getClusterCount(); i++) sum += membershipValues[i] * (membershipValues[i]>0.0? Math.log(membershipValues[i])/Math.log(2.0d) : 0.0d);
			}
		}
		else
		{
			double[] membershipValues;
			for(IndexedDataObject<T> d : this.clusterInfo.getFuzzyClusteringProvider().getDataSet())
			{
				membershipValues = this.clusterInfo.getFuzzyClusteringProvider().getFuzzyAssignmentsOf(d);
				for(int i=0; i<this.clusterInfo.getClusterCount(); i++) sum += membershipValues[i] * (membershipValues[i]>0.0? Math.log(membershipValues[i])/Math.log(2.0d) : 0.0d);
			}
		}
		for(int j=0; j<noiseMemberships.length; j++) sum += noiseMemberships[j] * (noiseMemberships[j]>0.0? Math.log(noiseMemberships[j])/Math.log(2.0d) : 0.0d);
		
		return -sum / ((double)this.clusterInfo.getDataCount() * Math.log((double)this.clusterInfo.getClusterCount()+1.0d)/Math.log(2.0d));
	}
}