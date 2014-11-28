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

import data.set.AbstractStaticDataSetContainer;
import data.set.IndexedDataObject;
import data.set.IndexedDataSet;
import datamining.clustering.ClusteringAlgorithm;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class DummyCrispClusteringAlgorithm<T> extends AbstractStaticDataSetContainer<T> implements ClusteringAlgorithm<T>, CrispClusteringProvider<T>, CrispNoiseClusteringProvider<T>
{
	protected int[] clusteringResult;
	
	protected int clusterCount;
	
	public DummyCrispClusteringAlgorithm(IndexedDataSet<T> dataSet, int[] clusteringResult, int clusterCount)
	{
		super(dataSet);
		this.clusteringResult = clusteringResult;
		this.clusterCount = clusterCount;
	}
	
	/* (non-Javadoc)
	 * @see datamining.resultProviders.CrispClusteringProvider#isCrispAssigned(data.set.IndexedDataObject)
	 */
	@Override
	public boolean isCrispAssigned(IndexedDataObject<T> obj)
	{
		return this.data.contains(obj);
	}

	/* (non-Javadoc)
	 * @see datamining.resultProviders.CrispClusteringProvider#getAllCrispClusterAssignments()
	 */
	@Override
	public int[] getAllCrispClusterAssignments()
	{
		return this.clusteringResult;
	}

	/* (non-Javadoc)
	 * @see datamining.resultProviders.CrispClusteringProvider#getCrispClusterAssignmentOf(data.set.IndexedDataObject)
	 */
	@Override
	public int getCrispClusterAssignmentOf(IndexedDataObject<T> obj)
	{
		return this.clusteringResult[obj.getID()];
	}

	/* (non-Javadoc)
	 * @see datamining.resultProviders.CrispNoiseClusteringProvider#getCrispNoiseAssignments()
	 */
	@Override
	public boolean[] getCrispNoiseAssignments()
	{
		boolean[] noise = new boolean[this.getDataCount()];
		for(int j=0; j<this.getDataCount(); j++) noise[j] = this.clusteringResult[j] < 0;
		return noise;
	}

	/* (non-Javadoc)
	 * @see datamining.resultProviders.CrispNoiseClusteringProvider#isCrispNoiseAssigned(data.set.IndexedDataObject)
	 */
	@Override
	public boolean isCrispNoiseAssigned(IndexedDataObject<T> obj)
	{
		return this.clusteringResult[obj.getID()] < 0;
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
		return "Dummy Crisp Clustering Algorithm";
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
