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

import data.algebra.Metric;
import data.algebra.VectorSpace;
import data.set.DataSetNotSealedException;
import data.set.IndexedDataSet;
import datamining.clustering.protoype.Centroid;
import datamining.gradient.AbstractGradientOptimizationAlgorithm;
import datamining.gradient.functions.GradientFunction;
import datamining.resultProviders.PrototypeProvider;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class SingleCentroidGradientOptimizationAlgorithm<D> extends AbstractGradientOptimizationAlgorithm<D, D> implements PrototypeProvider<D, Centroid<D>>
{
	/**
	 * Using the centroid class here to monitor the movement of the data object like parameter.
	 */
	protected Centroid<D> parameterCentroid; 

	/**
	 * The copy constructor. 
	 * 
	 * @param c The <code>AbstractSingleCentroidGOAlgorithm</code> to be copied.
	 */
	public SingleCentroidGradientOptimizationAlgorithm(SingleCentroidGradientOptimizationAlgorithm<D> c)
	{
		super(c);
		
		this.parameterCentroid = c.parameterCentroid.clone();
	}
	
	/**
	 * @param data
	 * @param vs
	 * @param oF
	 * @throws DataSetNotSealedException
	 */
	public SingleCentroidGradientOptimizationAlgorithm(IndexedDataSet<D> data, VectorSpace<D> vs, Metric<D> metric, GradientFunction<D, D> objectiveFunction) throws DataSetNotSealedException
	{
		super(data, vs, metric, objectiveFunction);
		
		this.parameterCentroid = new Centroid<D>(vs);
	}
	
	/* (non-Javadoc)
	 * @see datamining.ParameterOptimization#initializeWith(java.lang.Object)
	 */
	@Override
	public void initializeWithParameter(D initialParameter)
	{
		this.parameterCentroid.initializeWithPosition(initialParameter);
		this.parameterVS.copy(this.parameter, initialParameter); 
		this.initialized = true;
	}

	/* (non-Javadoc)
	 * @see datamining.ParameterOptimization#getParameter()
	 */
	public D getParameter()
	{
		return this.parameterCentroid.getPosition();
	}
	
	/* (non-Javadoc)
	 * @see datamining.ParameterOptimization#updateParameter(java.lang.Object)
	 */
	public void updateParameter(D newParameter)
	{
		this.parameterCentroid.moveTo(newParameter);
		this.parameterVS.copy(this.parameter, newParameter); 
	}

	
	/* (non-Javadoc)
	 * @see datamining.DataMiningAlgorithm#algorithmName()
	 */
	@Override
	public String algorithmName()
	{
		return "Single Centroid Optimization Algorithm with: " + this.getObjectiveFunction().getName();
	}
	
	/**
	 * @return
	 */
	public Centroid<D> getCentroid()
	{
		return this.parameterCentroid;
	}

	/* (non-Javadoc)
	 * @see datamining.resultProviders.PrototypeProvider#getPrototypes()
	 */
	@Override
	public ArrayList<Centroid<D>> getPrototypes()
	{
		ArrayList<Centroid<D>> list = new ArrayList<Centroid<D>>(1);
		list.add(this.parameterCentroid);
		return list;
	}

	/* (non-Javadoc)
	 * @see datamining.resultProviders.PrototypeProvider#getActivePrototypes()
	 */
	@Override
	public ArrayList<Centroid<D>> getActivePrototypes()
	{
		return this.getPrototypes();
	}

	/* (non-Javadoc)
	 * @see datamining.resultProviders.PrototypeProvider#getPrototypeCount()
	 */
	@Override
	public int getPrototypeCount()
	{
		return 1;
	}

	/* (non-Javadoc)
	 * @see datamining.resultProviders.PrototypeProvider#getActivePrototypeCount()
	 */
	@Override
	public int getActivePrototypesCount()
	{
		return 1;
	}
}
