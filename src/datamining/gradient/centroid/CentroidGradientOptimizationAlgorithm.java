/**
 Copyright (c) 2012, The EDMOAL Project

	Roland Winkler
	Richard-Wagner Str. 42
	10585 Berlin, Germany
 
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
import data.set.IndexedDataSet;
import datamining.clustering.protoype.Centroid;
import datamining.gradient.AbstractGradientOptimizationAlgorithm;
import datamining.gradient.functions.GradientFunction;
import datamining.gradient.parameter.CentroidListParameter;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class CentroidGradientOptimizationAlgorithm<D> extends AbstractGradientOptimizationAlgorithm<D, CentroidListParameter<D>>
{
	protected CentroidListParameter<D> centroidList;
		
	/**
	 * @param c
	 */
	public CentroidGradientOptimizationAlgorithm(CentroidGradientOptimizationAlgorithm<D> c)
	{
		super(c);
		
		this.centroidList = c.centroidList.clone();
	}

	/**
	 * @param data
	 * @param vs
	 * @param parameterMetric
	 * @param objectiveFunction
	 * @throws DataSetNotSealedException
	 */
	public CentroidGradientOptimizationAlgorithm(IndexedDataSet<D> data, VectorSpace<CentroidListParameter<D>> vs, Metric<CentroidListParameter<D>> parameterMetric, GradientFunction<D, CentroidListParameter<D>> objectiveFunction) throws DataSetNotSealedException
	{
		super(data, vs, parameterMetric, objectiveFunction);
	}

	/* (non-Javadoc)
	 * @see datamining.DataMiningAlgorithm#algorithmName()
	 */
	@Override
	public String algorithmName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see datamining.ParameterOptimization#getParameter()
	 */
	@Override
	public CentroidListParameter<D> getParameter()
	{
		return this.centroidList;
	}

	/* (non-Javadoc)
	 * @see datamining.ParameterOptimization#updateParameter(java.lang.Object)
	 */
	@Override
	public void updateParameter(CentroidListParameter<D> parameter)
	{
		if(parameter.getCentroids().size() != this.centroidList.getCentroids().size()) throw new IllegalArgumentException("Number of Parameters does not match number of Centroids. Parametersize: " + parameter.getCentroids().size() + " Centroids: " + this.centroidList.getCentroids().size());
		
		for(int i=0; i<this.centroidList.getCentroids().size(); i++) this.centroidList.getCentroid(i).moveTo(parameter.getCentroid(i).getPosition());
	}

	/* (non-Javadoc)
	 * @see datamining.ParameterOptimization#initializeWith(java.lang.Object)
	 */
	@Override
	public void initializeWith(CentroidListParameter<D> initialParameter)
	{
		this.centroidList = initialParameter.clone();
	}

	/**
	 * @return the centroids
	 */
	public CentroidListParameter<D> getCentroidList()
	{
		return this.centroidList;
	}
}
