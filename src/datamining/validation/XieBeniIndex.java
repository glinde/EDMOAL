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
import datamining.clustering.protoype.AbstractPrototypeClusteringAlgorithm;
import datamining.clustering.protoype.Prototype;
import datamining.resultProviders.CrispClusteringProvider;
import etc.MyMath;

/** 
 * Calculates the Xie - Beni index. The complexity depends on how the pairwise distances of clusters and the
 * diameter of one cluster is calculated.<br>
 * 
 * Complexity for pairwise prototype calculations: O(n*c + c^2). 
 * Complexity for pairwise data object calculations: O(n^2*c^2), 
 *  with n being the number of data objects and c being the number of clusters.<br> 
 * 
 * See paper: X.L. Xie and G.A. Beni. Validity Measure for Fuzzy Clustering.IEEE Transactions on Pattern Analysis and Machine Intel-ligence (PAMI) 3(8):841�846. IEEE Press, Piscataway, NJ, USA 1991. Reprinted in [Bezdek and Pal 1992], 219�226
 * 
 * @param fuzzyAlgorithm The fuzzy clustering algorithm containing the clustering result
 * @param vs The vector space used for the clustering process
 * @param dist The distance metric.
 * @return The Xie - Beni index for the specified clustering result.
 */
public class XieBeniIndex<T> extends ClusterValidation<T>
{
	protected AbstractPrototypeClusteringAlgorithm<T, ? extends Prototype<T>> clusterAlgo;
		
	protected double fuzzifier;
	
	protected boolean crisp;
	/**
	 * @param clusterInfo
	 */
	public XieBeniIndex(ClusteringInformation<T> clusterInfo, AbstractPrototypeClusteringAlgorithm<T, ? extends Prototype<T>> clusterAlgo)
	{
		super(clusterInfo);
		
		this.clusterAlgo = clusterAlgo;
		this.fuzzifier = 2.0d;
		this.crisp = clusterAlgo instanceof CrispClusteringProvider;
	}
	
	/**
	 * @param clusterInfo
	 */
	public XieBeniIndex(ClusteringInformation<T> clusterInfo, double fuzzifier, boolean crisp)
	{
		super(clusterInfo);
		
		this.clusterAlgo = null;
		this.fuzzifier = fuzzifier;
		this.crisp = crisp;
	}
	
	public double index()
	{
		return this.crisp? (this.clusterInfo.getNoiseDistance() >= 0.0? this.crispNoiseIndex() : this.crispIndex()):
						   (this.clusterInfo.getNoiseDistance() >= 0.0? this.fuzzyNoiseIndex() : this.fuzzyIndex());
	}
	
	public double crispIndex()
	{
		this.clusterInfo.checkClusterDistances();
		
		int i, j, k;
		double objectiveFunctionValue;
				
		if(this.clusterAlgo != null)
		{
			objectiveFunctionValue = clusterAlgo.getObjectiveFunctionValue();
		}
		else
		{
			this.clusterInfo.checkCrispClusteringResult();
			this.clusterInfo.checkPrototypes();
			this.clusterInfo.checkDataSet();
			this.clusterInfo.checkMetric();
			
			
			objectiveFunctionValue = 0.0d;
									
			for(j=0; j<this.clusterInfo.getDataSet().size(); j++)
			{
				IndexedDataObject<T> obj = this.clusterInfo.getDataSet().get(j);
				
				if(this.clusterInfo.getCrispClusteringResult()[j] >= 0) objectiveFunctionValue += this.clusterInfo.getMetric().distanceSq(obj.x, this.clusterInfo.getPrototypes().get(this.clusterInfo.getCrispClusteringResult()[j]).getPosition());
			}	
		}
		
		double minClusterDistance = Double.POSITIVE_INFINITY;
		for(i=0; i<this.clusterInfo.getClusterCount(); i++)
		{
			for(k=i+1; k<this.clusterInfo.getClusterCount(); k++)
			{
				if(this.clusterInfo.getClusterDistances()[i][k] < minClusterDistance) minClusterDistance = this.clusterInfo.getClusterDistances()[i][k];
			}
		}
			
		return objectiveFunctionValue/(this.clusterInfo.getDataSet().size()*minClusterDistance*minClusterDistance);
	}
	
	public double crispNoiseIndex()
	{
		this.clusterInfo.checkClusterDistances();
		
		int i, j, k;
		double objectiveFunctionValue;
		
		if(this.clusterAlgo != null)
		{
			objectiveFunctionValue = clusterAlgo.getObjectiveFunctionValue();
		}
		else
		{
			this.clusterInfo.checkCrispClusteringResult();
			this.clusterInfo.checkPrototypes();
			this.clusterInfo.checkDataSet();
			this.clusterInfo.checkMetric();
			
			objectiveFunctionValue = 0.0d;
									
			for(j=0; j<this.clusterInfo.getDataSet().size(); j++)
			{
				IndexedDataObject<T> obj = this.clusterInfo.getDataSet().get(j);
				
				if(this.clusterInfo.getCrispClusteringResult()[j] >= 0) objectiveFunctionValue += this.clusterInfo.getMetric().distanceSq(obj.x, this.clusterInfo.getPrototypes().get(this.clusterInfo.getCrispClusteringResult()[j]).getPosition());
				else objectiveFunctionValue += this.clusterInfo.getNoiseDistance()*this.clusterInfo.getNoiseDistance();
			}
		}
		
		double minClusterDistance = Double.POSITIVE_INFINITY;
		for(i=0; i<this.clusterInfo.getClusterCount(); i++)
		{
			for(k=i+1; k<this.clusterInfo.getClusterCount(); k++)
			{
				if(this.clusterInfo.getClusterDistances()[i][k] < minClusterDistance) minClusterDistance = this.clusterInfo.getClusterDistances()[i][k];
			}
		}
		if(2.0 * this.clusterInfo.getNoiseDistance() < minClusterDistance) minClusterDistance = 2.0 * this.clusterInfo.getNoiseDistance();
			
		return objectiveFunctionValue/(this.clusterInfo.getDataSet().size()*minClusterDistance*minClusterDistance);
	}

	public double fuzzyIndex()
	{
		this.clusterInfo.checkClusterDistances();
		
		int i, j, k;
		double objectiveFunctionValue;
		
		if(this.clusterAlgo != null)
		{
			objectiveFunctionValue = clusterAlgo.getObjectiveFunctionValue();
		}
		else
		{
			this.clusterInfo.checkFuzzyClusteringProvider_FuzzyClusteringResult();
			this.clusterInfo.checkPrototypes();
			this.clusterInfo.checkDataSet();
			this.clusterInfo.checkMetric();
			
			double[] membershipValues;
			objectiveFunctionValue = 0.0d;
									
			for(j=0; j<this.clusterInfo.getFuzzyClusteringProvider().getDataSet().size(); j++)
			{
				IndexedDataObject<T> obj = this.clusterInfo.getDataSet().get(j);
				
				membershipValues = (this.clusterInfo.getFuzzyClusteringResult() != null)?
					this.clusterInfo.getFuzzyClusteringResult().get(j) : this.clusterInfo.getFuzzyClusteringProvider().getFuzzyAssignmentsOf(obj);
				
				for(i=0; i<this.clusterInfo.getClusterCount(); i++)
				{
					objectiveFunctionValue += MyMath.pow(membershipValues[i], this.fuzzifier) * this.clusterInfo.getMetric().distanceSq(obj.x, this.clusterInfo.getPrototypes().get(i).getPosition());
				}
			}	
		}
		
		double minClusterDistance = Double.POSITIVE_INFINITY;
		for(i=0; i<this.clusterInfo.getClusterCount(); i++)
		{
			for(k=i+1; k<this.clusterInfo.getClusterCount(); k++)
			{
				if(this.clusterInfo.getClusterDistances()[i][k] < minClusterDistance) minClusterDistance = this.clusterInfo.getClusterDistances()[i][k];
			}
		}
			
		return objectiveFunctionValue/(this.clusterInfo.getFuzzyClusteringProvider().getDataSet().size()*minClusterDistance*minClusterDistance);
	}

	public double fuzzyNoiseIndex()
	{
		this.clusterInfo.checkClusterDistances();
		
		int i, j, k;
		double objectiveFunctionValue;
		
		if(this.clusterAlgo != null)
		{
			objectiveFunctionValue = clusterAlgo.getObjectiveFunctionValue();
		}
		else
		{
			this.clusterInfo.checkFuzzyClusteringProvider_FuzzyClusteringResult();
			this.clusterInfo.checkPrototypes();
			this.clusterInfo.checkDataSet();
			this.clusterInfo.checkMetric();
			this.clusterInfo.checkNoiseClusterMembershipValues();
			
			double[] membershipValues;
			objectiveFunctionValue = 0.0d;
									
			for(j=0; j<this.clusterInfo.getFuzzyClusteringProvider().getDataSet().size(); j++)
			{
				IndexedDataObject<T> obj = this.clusterInfo.getDataSet().get(j);
				
				membershipValues = (this.clusterInfo.getFuzzyClusteringResult() != null)?
					this.clusterInfo.getFuzzyClusteringResult().get(j) : this.clusterInfo.getFuzzyClusteringProvider().getFuzzyAssignmentsOf(obj);
				
				for(i=0; i<this.clusterInfo.getClusterCount(); i++)
				{
					objectiveFunctionValue += MyMath.pow(membershipValues[i], this.fuzzifier) * this.clusterInfo.getMetric().distanceSq(obj.x, this.clusterInfo.getPrototypes().get(i).getPosition());
				}
				
				objectiveFunctionValue += MyMath.pow(this.clusterInfo.getNoiseClusterMembershipValues()[j], this.fuzzifier) * this.clusterInfo.getNoiseDistance()*this.clusterInfo.getNoiseDistance();
			}	
		}
		
		double minClusterDistance = Double.POSITIVE_INFINITY;
		for(i=0; i<this.clusterInfo.getClusterCount(); i++)
		{
			for(k=i+1; k<this.clusterInfo.getClusterCount(); k++)
			{
				if(this.clusterInfo.getClusterDistances()[i][k] < minClusterDistance) minClusterDistance = this.clusterInfo.getClusterDistances()[i][k];
			}
		}
		if(2.0 * this.clusterInfo.getNoiseDistance() < minClusterDistance) minClusterDistance = 2.0 * this.clusterInfo.getNoiseDistance();
			
		return objectiveFunctionValue/(this.clusterInfo.getFuzzyClusteringProvider().getDataSet().size()*minClusterDistance*minClusterDistance);
	}
}
