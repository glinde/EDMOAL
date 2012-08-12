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
import data.set.IndexedDataSet;
import datamining.clustering.protoype.Centroid;
import datamining.clustering.protoype.Prototype;
import datamining.gradient.AbstractGradientOptimizationAlgorithm;
import datamining.gradient.functions.GradientFunction;
import datamining.gradient.parameter.PositionListParameter;
import datamining.resultProviders.PrototypeProvider;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class PrototypeGradientOptimizationAlgorithm<D, PT extends Prototype<D>, PLP extends PositionListParameter<D>> extends AbstractGradientOptimizationAlgorithm<D, PLP> implements PrototypeProvider<D, PT>
{
	protected ArrayList<PT> prototypes;

	/**
	 * @param data
	 * @param vs
	 * @param parameterMetric
	 * @param objectiveFunction
	 * @throws DataSetNotSealedException
	 */
	public PrototypeGradientOptimizationAlgorithm(IndexedDataSet<D> data, VectorSpace<PLP> parameterVS, Metric<PLP> parameterMetric, GradientFunction<D, PLP> objectiveFunction, ArrayList<PT> prototypes) throws DataSetNotSealedException
	{
		super(data, parameterVS, parameterMetric, objectiveFunction);	
		this.prototypes = new ArrayList<PT>(prototypes);
	}

	/* (non-Javadoc)
	 * @see datamining.DataMiningAlgorithm#algorithmName()
	 */
	@Override
	public String algorithmName()
	{
		return "Gradient optimization with prototypes as parameters for: " + this.objectiveFunction.getName();
	}

	/* (non-Javadoc)
	 * @see datamining.ParameterOptimization#updateParameter(java.lang.Object)
	 */
	@Override
	public void updateParameter(PLP param)
	{
		if(param.getPositionCount() != this.prototypes.size()) throw new IllegalArgumentException("Number of Positions in Parameters does not match number of Prototypes. Parametersize: " + param.getPositionCount() + " Prototypes: " + this.prototypes.size());
		
		// replace the current parameter values with the new ones.
		this.parameterVS.copy(this.parameter, param);
		
		// update the prototype positions, keeping their history etc.
		for(int i=0; i<this.parameter.getPositionCount(); i++)
		{
			this.prototypes.get(i).moveTo(this.parameter.getPosition(i));
		}
	}

	/* (non-Javadoc)
	 * @see datamining.ParameterOptimization#initializeWith(java.lang.Object)
	 */
	@Override
	public void initializeWithParameter(PLP initialParameter)
	{
		this.parameter = this.parameterVS.copyNew(initialParameter);
		for(int i=0; i<this.parameter.getPositionCount(); i++) this.prototypes.get(i).initializeWithPosition(this.parameter.getPosition(i));
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.PrototypeProvider#getPrototypes()
	 */
	@Override
	public ArrayList<PT> getPrototypes()
	{
		return this.prototypes;
	}
	
	/**
	 * @return
	 */
	public int getActivePrototypesCount()
	{
		int counter = 0;
		
		for(int i=0; i<this.prototypes.size(); i++)
		{
			if(this.prototypes.get(i).isActivated()) counter++;
		}
		
		return counter;
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.PrototypeProvider#getActivePrototypes()
	 */
	@Override
	public ArrayList<PT> getActivePrototypes()
	{
		ArrayList<PT> activeProtos = new ArrayList<PT>(this.prototypes.size());
		
		for(PT proto:this.prototypes)
		{
			if(proto.isActivated()) activeProtos.add(proto);
		}
		
		return activeProtos;
	}
}
