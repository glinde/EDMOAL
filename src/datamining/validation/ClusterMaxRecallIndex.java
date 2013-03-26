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

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class ClusterMaxRecallIndex<T> extends ClusterValidation<T>
{
	/**
	 * @param clusterInfo
	 */
	public ClusterMaxRecallIndex(ClusteringInformation<T> clusterInfo)
	{
		super(clusterInfo);
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.validation.ClusterValidation#index()
	 */
	@Override
	public double index()
	{
		this.clusterInfo.checkFuzzyClusteringProvider_FuzzyClusteringResult();
		this.clusterInfo.checkTrueClusteringResult();
		
		double[][] clusterRecall = new double[this.clusterInfo.getClusterCount()][this.clusterInfo.getClusterCount()];
		double[] membershipValues;
		int dataObjectCount;
		double max;
		double sum;
		int[] classSize = new int[this.clusterInfo.getClusterCount()];
		int clas;

		dataObjectCount = (this.clusterInfo.getFuzzyClusteringResult() != null) ?
			this.clusterInfo.getFuzzyClusteringResult().size() :
			this.clusterInfo.getFuzzyClusteringProvider().getDataSet().size();

		int i, j, k;
		for(j=0; j<dataObjectCount; j++)
		{
			membershipValues = (this.clusterInfo.getFuzzyClusteringResult() != null)?
				this.clusterInfo.getFuzzyClusteringResult().get(j) :
				this.clusterInfo.getFuzzyClusteringProvider().getFuzzyAssignmentsOf(this.clusterInfo.getFuzzyClusteringProvider().getDataSet().get(j));

			clas = this.clusterInfo.getTrueClusteringResult()[j];
			if(clas >= 0)
			{
				classSize[clas]++;			
				for(i=0; i<this.clusterInfo.getClusterCount(); i++)
					clusterRecall[i][clas] += membershipValues[i];
			}
		}
		
		sum = 0.0d;
		for(i=0; i<this.clusterInfo.getClusterCount(); i++)
		{
			max = 0.0d;
			for(k=0; k<this.clusterInfo.getClusterCount(); k++)
			{
				max = (max > clusterRecall[i][k]/classSize[k])? max : clusterRecall[i][k]/classSize[k];  
			}
//			System.out.println("Max = " + max);
			sum += max;
		}
		
		return (sum - 1.0d)/(this.clusterInfo.getClusterCount() - 1.0d);
	}

}
