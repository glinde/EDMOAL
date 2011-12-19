/**
Copyright (c) 2011, The EDMOAL Project

	DLR Deutsches Zentrum fuer Luft- und Raumfahrt e.V.
	German Aerospace Center e.V.
	Institut fuer Flugfuehrung/Institute of Flight Guidance
	Tel. +49 531 295 2500, Fax: +49 531 295 2550
	WWW: http://www.dlr.de/fl/		
 
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
    	this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice,
    	this list of conditions and the following disclaimer in the documentation and/or
    	other materials provided with the distribution.
    * Neither the name of the DLR nor the names of its contributors
    	may be used to endorse or promote products derived from this software
    	without specific prior written permission.

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

import java.util.ArrayList;
import java.util.Collection;

import data.algebra.VectorSpace;
import data.set.IndexedDataSet;
import datamining.IterativeObjectiveFunctionOptimization;
import datamining.clustering.AbstractClusteringAlgorithm;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public abstract class AbstractPrototypeClusteringAlgorithm<T, S extends Prototype<T>> extends AbstractClusteringAlgorithm<T> implements PrototypeClusteringAlgorithm<T, S>, IterativeObjectiveFunctionOptimization
{
	/**  */
	private static final long	serialVersionUID	= -7799213874962389902L;

	/** The percentage of which the cluster centers shell be moved at an updating step */
	protected double learningFactor;
	
	/**  */
	protected final VectorSpace<T> vs;
	
	/** 
	 *	the vector of prototypes used for the clustering. 
	 */
	protected ArrayList<S> prototypes;
	
	/** 
	 *  true if the prototypes have been initialized after object construction or a reset.<br>
	 *  false if the prototypes have not been initialized yet or if they have been reseted.
	 */
	protected boolean initialized;
	
	/** counts the steps the clustering algorithm is performed */
	protected int iterationCount;
		
	/** If true, the objective function values are recorded. */
	protected boolean monitorObjectiveFunctionValues;
	
	/** The recorded objective function values */
	protected ArrayList<Double> objectiveFunctionValues;
	
	/**
	 *	If the square of prototype movement distance is smaller than <code>epsilon</code>, the calculation stops.<br>
	 *	To ensure the algorithm does not stop, set <code>epsilon</code> to 0 or negative.
	 */
	protected double epsilon;
		
	/**
	 *	The initial constructor for clustering. The number of clusters can be changed after initialization, but it
	 * is not recommended because some algorithms have to be reinitialized.
	 */
	public AbstractPrototypeClusteringAlgorithm(IndexedDataSet<T> data, VectorSpace<T> vs)
	{
		super(data);
		
		this.vs								= vs;
		this.initialized					= false;
		this.learningFactor					= 1.0d;
		this.iterationCount					= 0;
		this.monitorObjectiveFunctionValues	= true;
		this.objectiveFunctionValues		= new ArrayList<Double>(100);
		this.epsilon						= 0;
		
		this.prototypes						= new ArrayList<S>();
	}
	
	/**
	 * This constructor is meant to be used if the clustering algorithm should be changed. All data references
	 * stay the same, still the data containers are reinitialized. So it is possible to skip some clusters
	 * if they are not needed any more.
	 * 
	 * @param c the elders clustering algorithm object
	 * @param useCluster An array of length of the original number of clusters that contains the information if the cluster
	 * according to its index shell be used.
	 */
	@SuppressWarnings("unchecked")
	public AbstractPrototypeClusteringAlgorithm(AbstractPrototypeClusteringAlgorithm<T, S> c, boolean useOnlyActivePrototypes)
	{
		super(c.data);
		
		this.vs								= c.vs;
		this.prototypes						= new ArrayList<S>(c.getClusterCount());
		this.initialized					= c.initialized;
		this.learningFactor					= c.learningFactor;
		this.iterationCount					= 0;
		this.monitorObjectiveFunctionValues	= c.monitorObjectiveFunctionValues;
		this.objectiveFunctionValues		= new ArrayList<Double>(100);
		this.epsilon						= c.epsilon;
		
		if(useOnlyActivePrototypes)
		{
			for(int j=0; j<c.getClusterCount(); j++)
			{
				if(c.prototypes.get(j).isActivated())
				{
					this.prototypes.add((S)c.prototypes.get(j).clone());
				}
			}
		}
		else
		{
			for(int j=0; j<c.getClusterCount(); j++)
			{
				this.prototypes.add((S)c.prototypes.get(j).clone());
			}
		}
	}
		
	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.PrototypeClusteringAlgorithm#initialize(java.util.Collection)
	 */
	@Override
	public void initializeWithPrototypes(Collection<S> initialPrototypes)
	{
		this.prototypes.clear();		
		this.prototypes.addAll(initialPrototypes);
		for(int i=0; i<this.prototypes.size(); i++) this.prototypes.get(i).setClusterIndex(i);
		this.initialized = true;
	}
	
	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.PrototypeClusteringAlgorithm#initialize(java.lang.Object)
	 */
	@Override
	public abstract void initializeWithPositions(Collection<T> initialPrototypePositions);

	/* (non-Javadoc)
	 * @see datamining.DataMiningAlgorithm#algorithmName()
	 */
	@Override
	public abstract String algorithmName();

	/* (non-Javadoc)
	 * @see datamining.IterativeAlgorithm#apply(int)
	 */
	@Override
	public abstract void apply(int steps);
	
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
	public abstract double getObjectiveFunctionValue();
	
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
	 * @see datamining.clustering.protoype.PrototypeClusteringAlgorithm#getActivePrototypes()
	 */
	@Override
	public ArrayList<S> getActivePrototypes()
	{
		ArrayList<S> active = new ArrayList<S>(this.getClusterCount());
		
		for(S p:this.prototypes)
		{
			if(p.isActivated()) active.add(p);
		}
		
		return active;
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

	/* (non-Javadoc)
	 * @see datamining.clustering.ClusteringAlgorithm#getActiveClusterCount()
	 */
	@Override
	public int getActiveClusterCount()
	{
		int active=0;
		
		for(Prototype<T> p:this.prototypes)
		{
			if(p.isActivated()) active++;
		}
		
		return active;
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.ClusteringAlgorithm#getInactiveClusterCount()
	 */
	@Override
	public int getInactiveClusterCount()
	{
		int inactive=0;
		
		for(Prototype<T> p:this.prototypes)
		{
			if(!p.isActivated()) inactive++;
		}
		
		return inactive;
	}

	
	
	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.PrototypeClusteringAlgorithm#getVectorSpace()
	 */
	@Override
	public VectorSpace<T> getVectorSpace()
	{
		return this.vs;
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.PrototypeClusteringAlgorithm#getPrototypes()
	 */
	@Override
	public ArrayList<S> getPrototypes()
	{
		return new ArrayList<S>(this.prototypes);
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

	/* (non-Javadoc)
	 * @see datamining.clustering.ClusteringAlgorithm#getClusterCount()
	 */
	@Override
	public int getClusterCount()
	{
		return this.prototypes.size();
	}

	/**
	 * @param clone
	 */
	@SuppressWarnings("unchecked")
	public void clone(AbstractPrototypeClusteringAlgorithm<T, S> clone)
	{
		super.clone(clone);
		
		clone.initialized = this.initialized;
		clone.learningFactor = this.learningFactor;
		clone.prototypes = new ArrayList<S>(this.prototypes.size());
		clone.monitorObjectiveFunctionValues = this.monitorObjectiveFunctionValues;
		clone.objectiveFunctionValues = new ArrayList<Double>(this.objectiveFunctionValues.size());
		clone.objectiveFunctionValues.addAll(this.objectiveFunctionValues);
		clone.epsilon = this.epsilon;
		for(Prototype<T> p:this.prototypes) clone.prototypes.add((S)p.clone());
				
		clone.iterationCount = this.iterationCount;
	}
}
