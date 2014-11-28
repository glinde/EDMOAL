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
package datamining.resultProviders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import data.set.AbstractStaticDataSetContainer;
import data.set.IndexedDataObject;
import data.set.IndexedDataSet;
import datamining.clustering.ClusteringAlgorithm;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class DummyFuzzyClusteringAlgorithm<T> extends AbstractStaticDataSetContainer<T> implements ClusteringAlgorithm<T>, FuzzyClusteringProvider<T>, FuzzyNoiseClusteringProvider<T>
{
	protected ArrayList<double[]> clusteringResult;
	
	protected double[] noise;
	
	protected int clusterCount;
	
	public DummyFuzzyClusteringAlgorithm(IndexedDataSet<T> dataSet, Collection<double[]> clusteringResult, double[] noise)
	{
		super(dataSet);
		this.clusteringResult = new ArrayList<double[]>(clusteringResult);
		if(noise != null) this.noise = noise;
		else this.noise = new double[this.clusteringResult.size()];
		this.clusterCount = this.clusteringResult.get(0).length;
	}
	
	/* (non-Javadoc)
	 * @see datamining.resultProviders.CrispClusteringProvider#isCrispAssigned(data.set.IndexedDataObject)
	 */
	@Override
	public boolean isFuzzyAssigned(IndexedDataObject<T> obj)
	{
		return this.data.contains(obj);
	}

	/* (non-Javadoc)
	 * @see datamining.resultProviders.FuzzyNoiseClusteringProvider#getFuzzyNoiseAssignments()
	 */
	@Override
	public double[] getFuzzyNoiseAssignments()
	{
		return this.noise;
	}

	/* (non-Javadoc)
	 * @see datamining.resultProviders.FuzzyNoiseClusteringProvider#getFuzzyNoiseAssignmentOf(data.set.IndexedDataObject)
	 */
	@Override
	public double getFuzzyNoiseAssignmentOf(IndexedDataObject<T> obj)
	{
		return this.noise[obj.getID()];
	}

	/* (non-Javadoc)
	 * @see datamining.resultProviders.FuzzyClusteringProvider#getFuzzyAssignmentsOf(data.set.IndexedDataObject)
	 */
	@Override
	public double[] getFuzzyAssignmentsOf(IndexedDataObject<T> obj)
	{
		return this.clusteringResult.get(obj.getID());
	}

	/* (non-Javadoc)
	 * @see datamining.resultProviders.FuzzyClusteringProvider#getAllFuzzyClusterAssignments(java.util.List)
	 */
	@Override
	public List<double[]> getAllFuzzyClusterAssignments(List<double[]> assignmentList)
	{
		if(assignmentList == null) assignmentList = new ArrayList<double[]>(this.data.size());
		
		assignmentList.addAll(this.clusteringResult);
		
		return assignmentList;
	}

	/* (non-Javadoc)
	 * @see datamining.resultProviders.FuzzyClusteringProvider#getFuzzyAssignmentSums()
	 */
	@Override
	public double[] getFuzzyAssignmentSums()
	{
		double[] sums = new double[this.getClusterCount()];
		
		for(double[] memberV:this.clusteringResult)
		{
			for(int i=0; i<this.getClusterCount(); i++)
			{
				sums[i] += memberV[i];
			}
		}
		
		return sums;
	}

	/* (non-Javadoc)
	 * @see datamining.DataMiningAlgorithm#apply()
	 */
	@Override
	public void apply()
	{}

	/* (non-Javadoc)
	 * @see datamining.DataMiningAlgorithm#algorithmName()
	 */
	@Override
	public String algorithmName()
	{
		return "Dummy Fuzzy Clustering Algorithm";
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.ClusteringAlgorithm#getClusterCount()
	 */
	@Override
	public int getClusterCount()
	{
		return this.clusterCount;
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.ClusteringAlgorithm#getActiveClusterCount()
	 */
	@Override
	public int getActiveClusterCount()
	{
		return this.clusterCount;
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.ClusteringAlgorithm#getInactiveClusterCount()
	 */
	@Override
	public int getInactiveClusterCount()
	{
		return 0;
	}

}
