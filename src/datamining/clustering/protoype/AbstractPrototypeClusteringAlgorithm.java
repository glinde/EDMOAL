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

import data.algebra.Metric;
import data.algebra.VectorSpace;
import data.set.IndexedDataSet;
import datamining.IterativeObjectiveFunctionOptimization;
import datamining.clustering.AbstractClusteringAlgorithm;

/**
 * An abstract class for all prototype based clustering algorithms, without specifying the class of the prototype.
 * It provides functionalities to record the objective function and it also provides some functionalities regarding
 * the learning factor.<br>
 * 
 * The learning factor influences the movement speed of the prototypes. When setting a prototype
 * to a new position, the difference vector from the current to the new position is multiplied
 * by the learning factor. Thus, it is possible to slow down the clustering process or to speed
 * it up.
 * 
 * @author Roland Winkler
 */
public abstract class AbstractPrototypeClusteringAlgorithm<T, S extends Prototype<T>> extends AbstractClusteringAlgorithm<T> implements PrototypeClusteringAlgorithm<T, S>, IterativeObjectiveFunctionOptimization
{
	/**  */
	private static final long	serialVersionUID	= -7799213874962389902L;

	/**
	 * The learning factor influences the movement speed of the prototypes. When setting a prototype
	 * to a new position, the difference vector from the current to the new position is multiplied
	 * by the learning factor. Thus, it is possible to slow down the clustering process or to speed
	 * it up. 
	 */
	protected double learningFactor;
	
	/** The vector space that is used for prototype position calculations. */
	protected final VectorSpace<T> vs;
	
	/** 
	 *	The vector of prototypes used for the clustering. 
	 */
	protected ArrayList<S> prototypes;
	
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
		
	/**
	 * The initial constructor for clustering. The number of clusters can be changed after initialisation, but it
	 * is not recommended because some algorithms have to be reinitialised.
	 * 
	 * @param data The data set for clustering.
	 * @param vs The vector space of which the data objects are elements.
	 */
	public AbstractPrototypeClusteringAlgorithm(IndexedDataSet<T> data, VectorSpace<T> vs, Metric<T> metric)
	{
		super(data, metric);
		
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
	 * This constructor creates a new clustering algorithm, taking an existing one. It has the option to use only
	 * active prototypes from the old clustering algorithm. This constructor is especially useful if the clusteing is done
	 * in multiple steps. So the first clustering algorithm can for example calculate the initial positions of the 
	 * prototypes for the second clustering algorithm. An other option is, that the first clustering algorithm
	 * creates a set of deactivated prototypes and the second clustering algorithm is initialised with less clusters than the
	 * first.
	 * 
	 * @param c the elders clustering algorithm object
	 * @param useOnlyActivePrototypes States, that only prototypes that are active in the old clustering
	 * algorithm are used for the new clustering algorithm.
	 */
	@SuppressWarnings("unchecked")
	public AbstractPrototypeClusteringAlgorithm(AbstractPrototypeClusteringAlgorithm<T, S> c, boolean useOnlyActivePrototypes)
	{
		super(c.data, c.getMetric());
		
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
	 * @see datamining.clustering.protoype.PrototypeClusteringAlgorithm#initializeWithPrototypes(java.util.Collection)
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
