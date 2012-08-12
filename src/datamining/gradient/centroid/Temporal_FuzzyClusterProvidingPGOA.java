/**
Copyright (c) 2012, The EDMOAL Project

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
package datamining.gradient.centroid;

import java.util.ArrayList;
import java.util.List;

import data.algebra.Metric;
import data.algebra.VectorSpace;
import data.set.DataSetNotSealedException;
import data.set.IndexedDataObject;
import data.set.IndexedDataSet;
import datamining.clustering.FuzzyClusteringAlgorithm;
import datamining.clustering.protoype.Centroid;
import datamining.clustering.protoype.PrototypeClusteringAlgorithm;
import datamining.gradient.functions.GradientFunction;
import datamining.gradient.functions.clustering.FuzzyCMeansObjectiveFunction;
import datamining.gradient.parameter.PositionListParameter;

/**
 * Visualizer is not yet capable of handling objective functions. Use this instead. TODO: delete.
 *
 * @author Roland Winkler
 */
public class Temporal_FuzzyClusterProvidingPGOA<T> extends PrototypeGradientOptimizationAlgorithm<T, Centroid<T>, PositionListParameter<T>> implements FuzzyClusteringAlgorithm<T>, PrototypeClusteringAlgorithm<T, Centroid<T>>
{

	protected FuzzyCMeansObjectiveFunction<T> fcmFunc;
	
	/**
	 * @param data
	 * @param parameterVS
	 * @param parameterMetric
	 * @param objectiveFunction
	 * @throws DataSetNotSealedException
	 */
	public Temporal_FuzzyClusterProvidingPGOA(IndexedDataSet<T> data, VectorSpace<PositionListParameter<T>> parameterVS, Metric<PositionListParameter<T>> parameterMetric, FuzzyCMeansObjectiveFunction<T> fcmFunc, ArrayList<Centroid<T>> prototypes) throws DataSetNotSealedException
	{
		super(data, parameterVS, parameterMetric, fcmFunc, prototypes);
		this.fcmFunc = fcmFunc;
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.ClusteringAlgorithm#getClusterCount()
	 */
	@Override
	public int getClusterCount()
	{
		return this.parameter.getPositionCount();
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.ClusteringAlgorithm#getActiveClusterCount()
	 */
	@Override
	public int getActiveClusterCount()
	{
		return this.getActivePrototypesCount();
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.ClusteringAlgorithm#getInactiveClusterCount()
	 */
	@Override
	public int getInactiveClusterCount()
	{
		return this.getClusterCount() - this.getActiveClusterCount();
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.FuzzyClusteringAlgorithm#getFuzzyAssignmentsOf(data.set.IndexedDataObject)
	 */
	@Override
	public double[] getFuzzyAssignmentsOf(IndexedDataObject<T> obj)
	{
		return this.fcmFunc.getFuzzyAssignmentsOf(obj);
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.FuzzyClusteringAlgorithm#getAllFuzzyClusterAssignments(java.util.List)
	 */
	@Override
	public List<double[]> getAllFuzzyClusterAssignments(List<double[]> assignmentList)
	{
		return fcmFunc.getAllFuzzyClusterAssignments(assignmentList);
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.FuzzyClusteringAlgorithm#isFuzzyAssigned(data.set.IndexedDataObject)
	 */
	@Override
	public boolean isFuzzyAssigned(IndexedDataObject<T> obj)
	{
		return this.fcmFunc.isFuzzyAssigned(obj);
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.FuzzyClusteringAlgorithm#getFuzzyAssignmentSums()
	 */
	@Override
	public double[] getFuzzyAssignmentSums()
	{
		return this.fcmFunc.getFuzzyAssignmentSums();
	}


}
