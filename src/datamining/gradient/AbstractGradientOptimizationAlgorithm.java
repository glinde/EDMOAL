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
package datamining.gradient;

import java.util.ArrayList;

import data.algebra.Metric;
import data.algebra.VectorSpace;
import data.set.DataSetNotSealedException;
import data.set.IndexedDataSet;
import datamining.AbstractStaticDataMiningAlgorithm;
import datamining.IterativeObjectiveFunctionOptimization;
import datamining.ParameterOptimization;
import datamining.gradient.functions.GradientFunction;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public abstract class AbstractGradientOptimizationAlgorithm<D, P> extends AbstractStaticDataMiningAlgorithm<D> implements GradientOptimization<D, P>
{
	/**
	 *  The parameter that gets constantly updated during the optimization cycles.
	 */
	protected P parameter;
	
	/**
	 * The learning factor influences the movement speed of the prototypes. When setting a prototype
	 * to a new position, the difference vector from the current to the new position is multiplied
	 * by the learning factor. Thus, it is possible to slow down the clustering process or to speed
	 * it up. 
	 */
	protected double learningFactor;

	/** The vector space that is used for prototype position calculations. */
	protected final VectorSpace<P> parameterVS;

	/** The metric that is used to detect a parameter convergence. */
	protected final Metric<P> parameterMetric;

	/** The objective function that is to be optimized. */
	protected final GradientFunction<D, P> objectiveFunction;

	/** 
	 *  True if the prototypes have been initialised after object construction or a reset.
	 *  False if the prototypes have not been initialised yet or if they have been reseted.
	 */
	protected boolean initialized;
	
	/** The number of iteration steps the algorithm has performed. */
	protected int iterationCount;
		
	/** If true, the objective function values are recorded. */
	protected boolean monitorObjectiveFunctionValues;
	
	/** The recorded objective function values. */
	protected ArrayList<Double> objectiveFunctionValues;

	/**
	 *	If the square of prototype movement distance is smaller than <code>epsilon</code>, the calculation stops.<br>
	 *	To ensure the algorithm does not stop, set <code>epsilon</code> to 0 or negative.
	 */
	protected double epsilon;
	
	/** If true: this is a gradient ascending algorithm. (parameter maximization) <br>
	 *  If false: this is a gradient descending algorithm. (parameter minimization)
	 */
	protected boolean ascOrDesc;
	
	/**
	 * The standard constructor, taking the data set that is supposed to be analyzed. Because
	 * this is a static data mining algorithm, it requires the data set to be sealed.
	 * 
	 * @param data The data set that is to be analyzed.
	 * @param vs The vector space for the parameter.
	 * @param oF The objective function to be optimized.
	 * 
	 * @throws DataSetNotSealedException if the data set is not sealed.
	 */
	public AbstractGradientOptimizationAlgorithm(IndexedDataSet<D> data, VectorSpace<P> vs, Metric<P> parameterMetric, GradientFunction<D,P> objectiveFunction) throws DataSetNotSealedException
	{
		super(data);
		this.parameter = vs.getNewAddNeutralElement();
		this.learningFactor = 1.0d;
		this.parameterVS = vs;
		this.parameterMetric = parameterMetric;
		this.objectiveFunction = objectiveFunction;
		this.initialized = false;
		this.iterationCount = 0;
		this.monitorObjectiveFunctionValues = false;
		this.objectiveFunctionValues = new ArrayList<Double>(100);
		this.epsilon = 0.0d;
		this.ascOrDesc = false;
	}
	
	/**
	 * The copy constructor. 
	 * 
	 * @param c The <code>AbstractGradientOptimizationAlgorithm</code> to be copied.
	 */
	public AbstractGradientOptimizationAlgorithm(AbstractGradientOptimizationAlgorithm<D, P> c)
	{
		super(c);
		this.parameter = c.parameterVS.copyNew(c.parameter);
		this.learningFactor = c.learningFactor;
		this.parameterVS = c.parameterVS;
		this.parameterMetric = c.parameterMetric;
		this.objectiveFunction = c.objectiveFunction;
		this.initialized = c.initialized;
		this.iterationCount = c.iterationCount;
		this.monitorObjectiveFunctionValues = c.monitorObjectiveFunctionValues;
		this.objectiveFunctionValues = new ArrayList<Double>(c.objectiveFunctionValues);
		this.epsilon = c.epsilon;
		this.ascOrDesc = c.ascOrDesc;
	}

	/* (non-Javadoc)
	 * @see datamining.IterativeAlgorithm#apply(int)
	 */
	@Override
	public void apply(int iterations)
	{
		P nextPara = this.parameterVS.getNewAddNeutralElement();
		double distSq = Double.MAX_VALUE;
		
		for(int t=0; t<iterations; t++)
		{
			// get the gradient, takes also care of having a new instance of the parameter.
			this.objectiveFunction.setParameter(this.parameter);
			this.objectiveFunction.gradient(nextPara);
			
			// if this is an ascending algorithm, the scaled gradient is added to the current position.
			// if this is a descending algorithm, the scaled gradient is substracted from the current position.
			// this operation takes care of both cases without the danger of having a more costly "sub" method than add
			// and without multplying with an extra -1 value.
			if(this.ascOrDesc) this.parameterVS.mul(nextPara, this.learningFactor);
			else this.parameterVS.mul(nextPara, -this.learningFactor);
			
			// adds the last parameter to the scaled gradient.
			this.parameterVS.add(nextPara, this.parameter);
			
			// calculate the difference between the old and new parameter
			distSq = this.parameterMetric.distanceSq(this.parameter, nextPara);

			// store the new parameter
			this.updateParameter(nextPara);
			
			// iteration complete
			this.iterationComplete();
			
			// break if algorithm converged
			if(distSq < this.epsilon*this.epsilon) break;
		}
	}

	/* (non-Javadoc)
	 * @see datamining.DataMiningAlgorithm#apply()
	 */
	@Override
	public void apply()
	{
		this.apply(Integer.MAX_VALUE);
	}
	
	/* (non-Javadoc)
	 * @see datamining.ObjectiveFunctionOptimization#getObjectiveFunctionValue()
	 */
	@Override
	public double getObjectiveFunctionValue()
	{
		return this.objectiveFunction.functionValue();
	}
	
	/* (non-Javadoc)
	 * @see datamining.IterativeObjectiveFunctionOptimization#recordCurrentObjectiveFunctionValue()
	 */
	@Override
	public void recordCurrentObjectiveFunctionValue()
	{
		this.objectiveFunctionValues.add(new Double(this.getObjectiveFunctionValue()));
	}
	
	/** 
	 * marks that an iteration is complete 
	 */
	protected void iterationComplete()
	{
		this.iterationCount++;
		if(this.monitorObjectiveFunctionValues) this.recordCurrentObjectiveFunctionValue();
	}
			
	/* (non-Javadoc)
	 * @see datamining.IterativeAlgorithm#getIterationCount()
	 */
	@Override
	public int getIterationCount()
	{
		return this.iterationCount;
	}


	/* (non-Javadoc)
	 * @see datamining.IterativeObjectiveFunctionOptimization#getObjectiveFunctionValueHistory()
	 */
	@Override
	public double[] getObjectiveFunctionValueHistory()
	{
		double[] objValues = new double[this.objectiveFunctionValues.size()];
		
		for(int i=0; i<this.objectiveFunctionValues.size(); i++) objValues[i] = this.objectiveFunctionValues.get(i);
		
		return objValues;
	}

	/* (non-Javadoc)
	 * @see datamining.IterativeObjectiveFunctionOptimization#isObjectiveFunctionMonitoring()
	 */
	@Override
	public boolean isObjectiveFunctionMonitoring()
	{
		return this.monitorObjectiveFunctionValues;
	}

	/* (non-Javadoc)
	 * @see datamining.IterativeObjectiveFunctionOptimization#setObjectiveFunctionMonitoring(boolean)
	 */
	@Override
	public void setObjectiveFunctionMonitoring(boolean monitor)
	{
		 this.monitorObjectiveFunctionValues = monitor;
	}

	/**
	 * @return
	 */
	public VectorSpace<P> getParameterVectorSpace()
	{
		return this.parameterVS;
	}
	
	
	/**
	 * @return the parameterMetric
	 */
	public Metric<P> getParameterMetric()
	{
		return this.parameterMetric;
	}

	/**
	 * @return the epsilon
	 */
	public double getEpsilon()
	{
		return this.epsilon;
	}

	/**
	 * @param epsilon the epsilon to set
	 */
	public void setEpsilon(double epsilon)
	{
		this.epsilon = epsilon;
	}
	
	/**
	 * @return the learningFactor
	 */
	public double getLearningFactor()
	{
		return this.learningFactor;
	}

	/**
	 * @param learningFactor the learningFactor to set
	 */
	public void setLearningFactor(double learningFactor)
	{
		this.learningFactor = learningFactor;
	}

	/**
	 * @return the objectiveFunction
	 */
	public GradientFunction<D, P> getObjectiveFunction()
	{
		return this.objectiveFunction;
	}

	/**
	 * @return the initialized
	 */
	public boolean isInitialized()
	{
		return this.initialized;
	}	

	/**
	 * Returns the parameter that specifies whether this is a gradient ascending or a gradient descending algorithm.<br>
	 * 
	/** If true: this is a gradient ascending algorithm. (parameter maximization) <br>
	 *  If false: this is a gradient descending algorithm. (parameter minimization)
	 * 
	 * @return the ascending/descending parameter.
	 */
	public boolean isAscOrDesc()
	{
		return this.ascOrDesc;
	}

	/**
	 * Sets the parameter that specifies whether this is a gradient ascending or a gradient descending algorithm.<br>
	 * 
	/** If true: this is a gradient ascending algorithm. (parameter maximization) <br>
	 *  If false: this is a gradient descending algorithm. (parameter minimization)
	 * 
	 * @param descOrAsc the ascending/descending parameter to set.
	 */
	public void setAscOrDesc(boolean ascOrDesc)
	{
		this.ascOrDesc = ascOrDesc;
	}

	/* (non-Javadoc)
	 * @see datamining.ParameterOptimization#getParameter()
	 */
	public P getParameter()
	{
		return this.parameter;
	}
	
	/**
	 * @TODO: remove.  
	 */
	public void clone(AbstractGradientOptimizationAlgorithm<D, P> clone)
	{
		super.clone(clone);
		
		clone.parameter = this.parameterVS.copyNew(this.parameter);
		clone.initialized = this.initialized;
		clone.learningFactor = this.learningFactor;
		clone.monitorObjectiveFunctionValues = this.monitorObjectiveFunctionValues;
		clone.objectiveFunctionValues = new ArrayList<Double>(this.objectiveFunctionValues.size());
		clone.objectiveFunctionValues.addAll(this.objectiveFunctionValues);
		clone.epsilon = this.epsilon;				
		clone.iterationCount = this.iterationCount;
		clone.ascOrDesc = this.ascOrDesc;
	}
}
