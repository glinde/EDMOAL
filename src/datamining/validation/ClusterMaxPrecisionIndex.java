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

import datamining.resultProviders.FuzzyNoiseClusteringProvider;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class ClusterMaxPrecisionIndex<T> extends ClusterValidation<T>
{
	protected boolean crisp;
	
	/**
	 * @param clusterInfo
	 */
	public ClusterMaxPrecisionIndex(ClusteringInformation<T> clusterInfo, boolean crisp)
	{
		super(clusterInfo);
		
		this.crisp = crisp;
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.validation.ClusterValidation#index()
	 */
	@Override
	public double index()
	{
		return this.crisp? this.crispIndex(): this.fuzzyIndex();
	}
	
	public double crispIndex()
	{
		this.clusterInfo.checkCrispClusteringResult();
		this.clusterInfo.checkTrueClusteringResult();

		int clusterCount = this.clusterInfo.getClusterCount();
		int[][] clusterPrecision = new int[this.clusterInfo.getClusterCount()][this.clusterInfo.getClusterCount()];
		int dataObjectCount;
		double max;
		double sum;
		int[] clusterSize = new int[this.clusterInfo.getClusterCount()];
		int clas, clus;
		int noiseRec = 0;
		int noiseSum = 0;

		dataObjectCount = this.clusterInfo.getCrispClusteringResult().length;

		int i, j, k;
		for(j=0; j<dataObjectCount; j++)
		{
			clus = this.clusterInfo.getCrispClusteringResult()[j];
			clas = this.clusterInfo.getTrueClusteringResult()[j];
			if(clas >= 0 && clus >= 0)
			{
				clusterSize[clus]++;
				clusterPrecision[clus][clas]++;
			}
			else 
			{
				if(clus < 0)
				{
					noiseSum++;
					if(clas < 0) noiseRec++;
				}
			}
		}
		
		sum = 0.0d;
		for(k=0; k<this.clusterInfo.getClusterCount(); k++)
		{
			max = 0.0d;
			for(i=0; i<this.clusterInfo.getClusterCount(); i++)
			{
				if(clusterSize[i] == 0) continue;
				max = (max > ((double)clusterPrecision[i][k])/((double)clusterSize[i]))? max : ((double)clusterPrecision[i][k])/((double)clusterSize[i]);  
			}
			sum += max;
		}

		// noise handling	
		
		if(noiseSum > 0)
		{
			sum += ((double)noiseRec) / ((double)noiseSum);
			clusterCount++;
		}

		return (sum - 1.0d)/(clusterCount - 1.0d);
	}
	
	public double fuzzyIndex()
	{
		this.clusterInfo.checkFuzzyClusteringProvider_FuzzyClusteringResult();
		this.clusterInfo.checkTrueClusteringResult();
		
		int clusterCount = this.clusterInfo.getClusterCount();
		double[][] clusterPrecision = new double[this.clusterInfo.getClusterCount()][this.clusterInfo.getClusterCount()];
		double[] membershipValues;
		int dataObjectCount;
		double max;
		double sum;
		double[] clusterSize = new double[this.clusterInfo.getClusterCount()];
		int clas;
		double noiseRec;
		double noiseSum;
		double[] noiseAssignments = null;

		dataObjectCount = (this.clusterInfo.getFuzzyClusteringResult() != null) ?
			this.clusterInfo.getFuzzyClusteringResult().size() :
			this.clusterInfo.getFuzzyClusteringProvider().getDataSet().size();

		int i, j, k;
		for(j=0; j<dataObjectCount; j++)
		{
			membershipValues = (this.clusterInfo.getFuzzyClusteringResult() != null)?
				this.clusterInfo.getFuzzyClusteringResult().get(j) :
				this.clusterInfo.getFuzzyClusteringProvider().getFuzzyAssignmentsOf(this.clusterInfo.getFuzzyClusteringProvider().getDataSet().get(j));

			for(i=0; i<this.clusterInfo.getClusterCount(); i++) clusterSize[i] += membershipValues[i];
				
			clas = this.clusterInfo.getTrueClusteringResult()[j];
			if(clas >= 0)
			{
				for(i=0; i<this.clusterInfo.getClusterCount(); i++)
					clusterPrecision[i][clas] += membershipValues[i];
			}
		}
		
		sum = 0.0d;
		for(k=0; k<this.clusterInfo.getClusterCount(); k++)
		{
			max = 0.0d;
			for(i=0; i<this.clusterInfo.getClusterCount(); i++)
			{
				if(clusterSize[i] == 0) continue;
				max = (max > clusterPrecision[i][k]/clusterSize[i])? max : clusterPrecision[i][k]/clusterSize[i];  
			}
			sum += max;
		}
		
		
		// noise handling
		noiseRec = 0.0d;
		noiseSum = 0.0d;		
		if(this.clusterInfo.getNoiseClusterMembershipValues() != null)
		{
			noiseAssignments = this.clusterInfo.getNoiseClusterMembershipValues();
		}
		else if(this.clusterInfo.getFuzzyClusteringProvider() instanceof FuzzyNoiseClusteringProvider)
		{
			noiseAssignments = ((FuzzyNoiseClusteringProvider<T>)this.clusterInfo.getFuzzyClusteringProvider()).getFuzzyNoiseAssignments();
		}
		if(noiseAssignments != null)
		{
			for(j=0; j<dataObjectCount; j++)
			{
				noiseSum += noiseAssignments[j];
				if(this.clusterInfo.getTrueClusteringResult()[j] < 0) noiseRec += noiseAssignments[j];
			}
			
			noiseRec /= noiseSum;
			
			sum += noiseRec;
			clusterCount++;
		}
		
		return (sum - 1.0d)/(clusterCount - 1.0d);
	}

}
