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
package datamining.clustering.protoype;

import java.util.Collection;

import data.algebra.Metric;
import data.algebra.VectorSpace;
import data.set.IndexedDataObject;
import data.set.IndexedDataSet;
import datamining.resultProviders.CrispClusteringProvider;

/**
 * A dummy class, holding the result of a clustering algorithm. It is used to impersonate real algorithms for generic result analysis.
 *
 * @author Roland Winkler
 */
public class DummyCrispPrototypeClusteringAlgorithm<T> extends AbstractCentroidClusteringAlgorithm<T> implements CrispClusteringProvider<T>, MembershipFunctionProvider
{
	/** */
	private static final long serialVersionUID = 1147970237323085082L;
	/**
	 *  A list of integer values containing the clustering result. Each
	 *  element of the array corresponds to the data object with the same index and
	 *  the value of the element is the cluster the data object is associated to.
	 */
	protected int[] clusteringResult;
	
	/**
	 * Creates a new HardCMeansClusteringAlgorithm with the specified data set, vector space and metric.
	 * All data objects are set to {@link CrispClusteringProvider#UNASSIGNED_INDEX}. No prototypes
	 * are initialized so far.
	 * 
	 * @param data The data set that should be clustered.
	 * @param vs The vector space that is used to calculate the prototype positions.
	 * @param metric The metric that is used to calculate the distance between data objects and prototypes.
	 */
	public DummyCrispPrototypeClusteringAlgorithm(IndexedDataSet<T> data, int[] clusteringResult, VectorSpace<T> vs, Metric<T> metric)
	{
		super(data, vs, metric);
	
		this.clusteringResult = clusteringResult.clone();
		this.initialized = true;
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.AbstractPrototypeClusteringAlgorithm#initializeWithPrototypes(java.util.Collection)
	 */
	@Override
	public void initializeWithPrototypes(Collection<Centroid<T>> initialPrototypes)
	{
		super.initializeWithPrototypes(initialPrototypes);
	}
	
	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.AbstractCentroidClusteringAlgorithm#initializeWithPositions(java.util.Collection)
	 */
	@Override
	public void initializeWithPositions(Collection<T> initialPrototypePositions)
	{
		super.initializeWithPositions(initialPrototypePositions);
	}
	

	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.AbstractPrototypeClusteringAlgorithm#algorithmName()
	 */
	@Override
	public String algorithmName()
	{
		return "Dummy Prototype Clustering Algorithm";
	}
	
	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.AbstractPrototypeClusteringAlgorithm#apply(int)
	 */
	@Override
	public void apply(int steps)
	{}

	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.AbstractPrototypeClusteringAlgorithm#getObjectiveFunctionValue()
	 */
	@Override
	public double getObjectiveFunctionValue()
	{
		return 0.0d;
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.CrispClusteringProvider#getCrispClusterAssignmentOf(data.set.IndexedDataObject)
	 */
	@Override
	public int getCrispClusterAssignmentOf(IndexedDataObject<T> obj)
	{
		return this.clusteringResult[obj.getID()];
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.CrispClusteringProvider#getAllCrispClusterAssignments()
	 */
	@Override
	public int[] getAllCrispClusterAssignments()
	{
		return this.clusteringResult.clone();
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.CrispClusteringProvider#isCrispAssigned(data.set.IndexedDataObject)
	 */
	@Override
	public boolean isCrispAssigned(IndexedDataObject<T> obj)
	{
		return true;
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.MembershipFunctionProvider#applyMembershipFunction(double)
	 */
	@Override
	public double applyMembershipFunction(double membershipValue)
	{
		return membershipValue;
	}
}
