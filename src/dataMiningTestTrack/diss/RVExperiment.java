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
package dataMiningTestTrack.diss;

import java.util.Collection;

import data.objects.doubleArray.DAEuclideanVectorSpace;
import data.set.IndexedDataSet;
import datamining.gradient.centroid.SingleCentroidGradientOptimizationAlgorithm;
import datamining.gradient.functions.RelativeVarianceOfDistancesObjectiveFunction;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class RVExperiment//<S extends Prototype<double[]>>
{
	protected SingleCentroidGradientOptimizationAlgorithm<double[]> rvAlgo;
		
	protected double[] initialPosition;
	
	protected int maxIterations;
		
	protected double maxRV;
	
	public RVExperiment(int dim, Collection<double[]> data, double[] initialPosition, int maxIterations)
	{
		DAEuclideanVectorSpace evs = new DAEuclideanVectorSpace(dim);
		
		IndexedDataSet<double[]> dataSet = new IndexedDataSet<double[]>(data);
		dataSet.seal();
		
		RelativeVarianceOfDistancesObjectiveFunction relVarFunction = new RelativeVarianceOfDistancesObjectiveFunction(dataSet);
		this.rvAlgo = new SingleCentroidGradientOptimizationAlgorithm<double[]>(dataSet, evs, evs, relVarFunction, null); 
		this.rvAlgo.setAscOrDesc(true);
		this.rvAlgo.setLearningFactor(0.1d);
				
		this.initialPosition = initialPosition;
		this.maxIterations = maxIterations; 
	}
	
	public RVExperiment(int dim, IndexedDataSet<double[]> dataSet, double[] initialPosition, int maxIterations)
	{
		DAEuclideanVectorSpace evs = new DAEuclideanVectorSpace(dim);
		
		RelativeVarianceOfDistancesObjectiveFunction relVarFunction = new RelativeVarianceOfDistancesObjectiveFunction(dataSet);
		this.rvAlgo = new SingleCentroidGradientOptimizationAlgorithm<double[]>(dataSet, evs, evs, relVarFunction, null); 
		this.rvAlgo.setAscOrDesc(true);
		this.rvAlgo.setLearningFactor(0.1d);
				
		this.initialPosition = initialPosition;
		this.maxIterations = maxIterations; 
	}
	
	public void applyAlgorithm()
	{
		this.rvAlgo.initializeWithParameter(this.initialPosition);
		this.rvAlgo.apply(this.maxIterations);
		this.maxRV = this.rvAlgo.getObjectiveFunctionValue();
	}
		
	public SingleCentroidGradientOptimizationAlgorithm<double[]> getRvAlgo() {
		return rvAlgo;
	}

	public double[] getInitialPosition() {
		return initialPosition;
	}

	public int getMaxIterations() {
		return maxIterations;
	}

	public double getMaxRV() {
		return maxRV;
	}
	
	
}
