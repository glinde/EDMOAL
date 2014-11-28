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

import data.set.IndexedDataObject;


/**
 * Calculates the partition coefficient of the fuzzy clustering result.<br>
 * 
 * The complexity of the function is in O(n*c) with n being the number of data objects and c being the number of clusters.<br> 
 * 
 * See paper: J.C. Bezdek.Pattern Recognition with Fuzzy Objective Function Algorithms. Plenum Press, New York, NY, USA 1981
 *
 * @param fuzzyAlgorithm  The fuzzy clustering algorithm containing the clustering result
 * @return The partition coefficient of the specified clustering result.
 */
public class PartitionCoefficient<T> extends ClusterValidation<T>
{
	/**
	 * @param clusterInfo
	 */
	public PartitionCoefficient(ClusteringInformation<T> clusterInfo)
	{
		super(clusterInfo);
	}
	

	public double index()
	{
		this.clusterInfo.checkFuzzyClusteringProvider_FuzzyClusteringResult();
		if(this.clusterInfo.getNoiseDistance() >= 0.0) return this.noiseIndex(); 
		
		double sum = 0.0d;
		int dataCount;
		int i;		
		
		if(this.clusterInfo.getFuzzyClusteringResult() != null)
		{
			dataCount = this.clusterInfo.getFuzzyClusteringResult().size();
			for(double[] membershipValues : this.clusterInfo.getFuzzyClusteringResult())
			{
				for(i=0; i<this.clusterInfo.getClusterCount(); i++) sum += membershipValues[i] * membershipValues[i];
			}	
		}
		else
		{
			dataCount = this.clusterInfo.getFuzzyClusteringProvider().getDataSet().size();
			double[] membershipValues;
			for(IndexedDataObject<T> d : this.clusterInfo.getFuzzyClusteringProvider().getDataSet())
			{
				membershipValues = this.clusterInfo.getFuzzyClusteringProvider().getFuzzyAssignmentsOf(d);
				for(i=0; i<this.clusterInfo.getClusterCount(); i++) sum += membershipValues[i] * membershipValues[i];
			}
		}
				
		sum /= ((double)dataCount);
		
		return sum;
	}

	public double noiseIndex()
	{
		this.clusterInfo.checkFuzzyClusteringProvider_FuzzyClusteringResult();
		this.clusterInfo.checkNoiseClusterMembershipValues();
		
		double sum = 0.0d;
		int dataCount;
		int i;		
		double[] noiseMemberships = this.clusterInfo.getNoiseClusterMembershipValues();
		
		if(this.clusterInfo.getFuzzyClusteringResult() != null)
		{
			dataCount = this.clusterInfo.getFuzzyClusteringResult().size();
			for(double[] membershipValues : this.clusterInfo.getFuzzyClusteringResult())
			{
				for(i=0; i<this.clusterInfo.getClusterCount(); i++) sum += membershipValues[i] * membershipValues[i];
			}	
		}
		else
		{
			dataCount = this.clusterInfo.getFuzzyClusteringProvider().getDataSet().size();
			double[] membershipValues;
			for(IndexedDataObject<T> d : this.clusterInfo.getFuzzyClusteringProvider().getDataSet())
			{
				membershipValues = this.clusterInfo.getFuzzyClusteringProvider().getFuzzyAssignmentsOf(d);
				for(i=0; i<this.clusterInfo.getClusterCount(); i++) sum += membershipValues[i] * membershipValues[i];
			}
		}
		
		for(int j=0; j<noiseMemberships.length; j++)
		{
			sum += noiseMemberships[j] * noiseMemberships[j];
		}
		
		return sum / ((double)dataCount);
	}

}
