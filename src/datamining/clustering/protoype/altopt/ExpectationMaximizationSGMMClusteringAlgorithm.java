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

package datamining.clustering.protoype.altopt;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import data.algebra.Distance;
import data.algebra.VectorSpace;
import data.set.IndexedDataObject;
import data.set.IndexedDataSet;
import datamining.clustering.FuzzyClusteringAlgorithm;
import datamining.clustering.protoype.AbstractPrototypeClusteringAlgorithm;
import datamining.clustering.protoype.AlgorithmNotInitializedException;
import datamining.clustering.protoype.SphericalNormalDistributionPrototype;

/**
 * TODO Class Description
 * 
 * I am not sure this algorithm can be implemented for anything else than a real vector space.
 * Until I am sure it can be done, this remains to be for double arrays only.
 * 
 * A version for arbitrary shaped normal distributions is going to be implemented once some matrix related structures and 
 * algorithms are implemented.
 * 
 * In this algorithm, the membership matrix (aka conditional probabilities that x_j belongs to cluster i, given x_j) needs to
 * be calculated completely because for updating the variances, both the new prototype locations as well as the new membership values
 * need to be known. Therefore, the complete membership matrix must be stored during the optimization.
 * Since the memory must be available anyway, one might as well store it as class object to simplify later the fuzzy clustering algorithm
 * queries from the interface.
 * 
 * Paper: see for example Borgelt, C. Prototype-based Classification and Clustering (Habilitationsschrift) Otto-von-Guericke-University of Magdeburg, Germany, 2005
 *
 * @author Roland Winkler
 */
public class ExpectationMaximizationSGMMClusteringAlgorithm extends AbstractPrototypeClusteringAlgorithm<double[], SphericalNormalDistributionPrototype> implements Serializable, FuzzyClusteringAlgorithm<double[]>
{	
	/**  */
	private static final long	serialVersionUID	= -8858858125481849303L;

	/**  */
	protected final Distance<double[]> dist;
	
	protected double[] clusterProbability;
	
	protected ArrayList<double[]> conditionalProbabilities;
	
	protected boolean varianceBounded;	
	protected double varianceLowerBound;
	protected double varianceUpperBound;
		
	/**
	 * @param c
	 * @param useOnlyActivePrototypes
	 */
	public ExpectationMaximizationSGMMClusteringAlgorithm(ExpectationMaximizationSGMMClusteringAlgorithm c, boolean useOnlyActivePrototypes)
	{
		super(c, useOnlyActivePrototypes);

		this.dist = c.dist;
		this.clusterProbability = c.clusterProbability.clone();
		this.conditionalProbabilities = new ArrayList<double[]>(c.getDataCount());
		for(int j=0; j<c.getDataCount(); j++) this.conditionalProbabilities.add(c.conditionalProbabilities.get(j).clone());
		
		this.varianceBounded = c.varianceBounded;
		this.varianceLowerBound = c.varianceLowerBound;
		this.varianceUpperBound = c.varianceUpperBound;
	}

	/**
	 * @param data
	 * @param vs
	 */
	public ExpectationMaximizationSGMMClusteringAlgorithm(IndexedDataSet<double[]> data, VectorSpace<double[]> vs, Distance<double[]> dist)
	{
		super(data, vs);
		
		this.dist = dist;
		this.clusterProbability = null;
		this.conditionalProbabilities = null;
		
		this.varianceBounded = false;
		this.varianceLowerBound = 0.0d;
		this.varianceUpperBound = Double.MAX_VALUE;
	}
	
	protected void recalculateProbabilities()
	{
		if(!this.initialized) throw new AlgorithmNotInitializedException("Prototypes not initialized.");	
		
		int i, j;
		double doubleTMP = 0.0d;
				
		for(j=0; j<this.getDataCount(); j++)
		{
			doubleTMP = 0.0d;
			for(i=0; i<this.getClusterCount(); i++)
			{
				this.conditionalProbabilities.get(j)[i] = this.clusterProbability[i];
				this.conditionalProbabilities.get(j)[i] *= this.prototypes.get(i).density(this.getDataSet().get(j).element);
				doubleTMP += this.conditionalProbabilities.get(j)[i];
			}
			doubleTMP = 1.0d/doubleTMP;
			for(i=0; i<this.getClusterCount(); i++)
			{
				this.conditionalProbabilities.get(j)[i] *= doubleTMP;
				this.clusterProbability[i] += this.conditionalProbabilities.get(j)[i];
			}
		}
		
		for(i=0; i<this.getClusterCount(); i++)
		{
			this.clusterProbability[i] = this.clusterProbability[i]/((double)this.getDataCount());
		}
	}
	
	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.AbstractPrototypeClusteringAlgorithm#initializeWithPrototypes(java.util.Collection)
	 */
	@Override
	public void initializeWithPrototypes(Collection<SphericalNormalDistributionPrototype> initialPrototypes)
	{
		super.initializeWithPrototypes(initialPrototypes);
		
		this.clusterProbability = new double[this.getClusterCount()];
		Arrays.fill(this.clusterProbability, 1.0d/this.clusterProbability.length);
		this.conditionalProbabilities = new ArrayList<double[]>(this.getDataCount());
		for(int j=0; j<this.getDataCount(); j++) this.conditionalProbabilities.add(new double[this.getClusterCount()]);
		
		this.recalculateProbabilities();
		this.initialized = true;
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.AbstractPrototypeClusteringAlgorithm#initializeWithPositions(java.util.Collection)
	 */
	@Override
	public void initializeWithPositions(Collection<double[]> initialPrototypePositions)
	{
		SphericalNormalDistributionPrototype centr;
		int i = 0;
		
		this.prototypes.clear();		
		for(double[] x: initialPrototypePositions) 
		{
			centr = new SphericalNormalDistributionPrototype(this.vs, this.dist, x);
			centr.setClusterIndex(i);
			centr.setVariance(0.1d);
			this.prototypes.add(centr);
			i++;
		}
		
		this.clusterProbability = new double[this.getClusterCount()];
		Arrays.fill(this.clusterProbability, 1.0d/this.clusterProbability.length);
		this.conditionalProbabilities = new ArrayList<double[]>(this.getDataCount());
		for(int j=0; j<this.getDataCount(); j++) this.conditionalProbabilities.add(new double[this.getClusterCount()]);

		this.initialized = true;
		this.recalculateProbabilities();
	}
	
	/* (non-Javadoc)
	 * @see datamining.clustering.AbstractDoubleArrayClusteringAlgorithm#algorithmName()
	 */
	@Override
	public String algorithmName()
	{
		return "Expectation Maximization with Spherical Gaussian Mixture Models Clustering Algorithm";
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.AbstractPrototypeClusteringAlgorithm#apply(int)
	 */
	@Override
	public void apply(int steps)
	{
		if(!this.initialized) throw new AlgorithmNotInitializedException("Prototypes not initialized.");	
		
		int i, j, t; 
		// i: index for clusters
		// j: index for data objects
		// k: index for dimensions
		// t: index for iterations
		 
		double doubleTMP = 0.0d;									// a temporal variable for multiple purposes
		double prototypeMovement = 0.0d;
		double[] invCondDOProbSum = new double[this.getClusterCount()];
		double[] tmpX = this.vs.getNewAddNeutralElement();

		ArrayList<double[]> newExpectationValues = new ArrayList<double[]>(this.getClusterCount());
		for(i=0; i<this.getClusterCount(); i++) newExpectationValues.add(this.vs.getNewAddNeutralElement());
		
		
		for(t = 0; t < steps; t++)
		{
			prototypeMovement = 0.0d;

			for(i = 0; i < this.getClusterCount(); i++)
			{
				this.vs.resetToAddNeutralElement(newExpectationValues.get(i));
				invCondDOProbSum[i] = 0.0d;
			}
			
			// the expectation step			
			for(j=0; j<this.getDataCount(); j++)
			{
				doubleTMP = 0.0d;
				for(i=0; i<this.getClusterCount(); i++)
				{
					this.conditionalProbabilities.get(j)[i] = this.clusterProbability[i];
					this.conditionalProbabilities.get(j)[i] *= this.prototypes.get(i).density(this.getDataSet().get(j).element);
					doubleTMP += this.conditionalProbabilities.get(j)[i];
					if(Double.isNaN(this.conditionalProbabilities.get(j)[i]))
						System.out.println("MOEOEOEP 1");
				}
				doubleTMP = 1.0d/doubleTMP;
				for(i=0; i<this.getClusterCount(); i++)
				{
					this.conditionalProbabilities.get(j)[i] *= doubleTMP;
					invCondDOProbSum[i] += this.conditionalProbabilities.get(j)[i];
					if(Double.isNaN(this.conditionalProbabilities.get(j)[i]))
						System.out.println("MOEOEOEP 2");
				}
			}
			
			// the maximization step
			// cluster weight probabilities
			for(i=0; i<this.getClusterCount(); i++)
			{
				this.clusterProbability[i] = invCondDOProbSum[i]/((double)this.getDataCount());
				invCondDOProbSum[i] = 1.0d/invCondDOProbSum[i];
				
				if(Double.isNaN(this.clusterProbability[i]))
					System.out.println("MOEOEOEP 3");
				
				if(Double.isNaN(invCondDOProbSum[i]))
					System.out.println("MOEOEOEP 4");
			}
			
			// new expectation values
			for(j=0; j<this.getDataCount(); j++)
			{
				for(i=0; i<this.getClusterCount(); i++) 
				{				
					this.vs.copy(tmpX, this.data.get(j).element);
					this.vs.mul(tmpX, this.conditionalProbabilities.get(j)[i]);
					this.vs.add(newExpectationValues.get(i), tmpX);
				}
			}

			// update prototype positions
			for(i=0; i<this.getClusterCount(); i++)
			{
				this.vs.mul(newExpectationValues.get(i), invCondDOProbSum[i]);
			}
			
			// copy new prototype values into prototypes wrt. learning factor
			for(i = 0; i < this.getClusterCount(); i++)
			{
				if(Math.abs(this.learningFactor - 1.0d) > 0.01d)
				{
					this.vs.sub(newExpectationValues.get(i), this.prototypes.get(i).getPosition());
					this.vs.mul(newExpectationValues.get(i), this.learningFactor);
					this.vs.add(newExpectationValues.get(i), this.prototypes.get(i).getPosition());	
				}
				
				doubleTMP = this.dist.distanceSq(this.prototypes.get(i).getPosition(), newExpectationValues.get(i));
				if(doubleTMP > prototypeMovement) prototypeMovement = doubleTMP;
				
				this.prototypes.get(i).moveTo(newExpectationValues.get(i));
			}
			
			// variances
			for(i=0; i<this.getClusterCount(); i++)
			{
				doubleTMP = 0.0d;				
				for(j=0; j<this.getDataCount(); j++)
				{
					doubleTMP += this.conditionalProbabilities.get(j)[i] *  this.dist.distanceSq(this.data.get(j).element, this.prototypes.get(i).getPosition());
				}
				doubleTMP *= invCondDOProbSum[i] / ((double)this.vs.getDimension());

				if(Double.isNaN(doubleTMP))
					System.out.println("MOEOEOEP 5");
				
				
				this.prototypes.get(i).setVariance(doubleTMP);
				if(this.varianceBounded)
				{
					if(this.prototypes.get(i).getVariance() < this.varianceLowerBound) 
						this.prototypes.get(i).setVariance(this.varianceLowerBound);
					if(this.prototypes.get(i).getVariance() > this.varianceUpperBound) 
						this.prototypes.get(i).setVariance(this.varianceUpperBound);
				}
			}

			this.iterationComplete();
			
			if(prototypeMovement < this.epsilon*this.epsilon) break;
		}
	}
	


	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.AbstractPrototypeClusteringAlgorithm#getObjectiveFunctionValue()
	 */
	@Override
	public double getObjectiveFunctionValue()
	{
		if(!this.initialized) throw new AlgorithmNotInitializedException("Prototypes not initialized.");	
		
		double objectiveFunctionValue = 0.0d;
		int i,j;
		double doubleTMP = 0.0d;
		double ln2Pi = 0.5d*((double)this.vs.getDimension())*Math.log(2.0d*Math.PI);

		for(i=0; i<this.getClusterCount(); i++)
		{
			doubleTMP = Math.log(this.clusterProbability[i]);
			doubleTMP -= ln2Pi;
			doubleTMP += 0.5d*((double)this.vs.getDimension())*Math.log(1.0d/this.prototypes.get(i).getVariance());

			for(j=0; j<this.getDataCount(); j++)	
			{
				objectiveFunctionValue += this.conditionalProbabilities.get(j)[i] * (doubleTMP - 0.5d*this.dist.distanceSq(this.prototypes.get(i).getPosition(), this.data.get(j).element)/this.prototypes.get(i).getVariance());
			}
		}
		
		return objectiveFunctionValue;
	}

	
	/* (non-Javadoc)
	 * @see datamining.clustering.FuzzyClusteringAlgorithm#getFuzzyAssignmentsOf(data.set.IndexedDataObject)
	 */
	@Override
	public double[] getFuzzyAssignmentsOf(IndexedDataObject<double[]> obj)
	{
		if(!this.initialized) throw new AlgorithmNotInitializedException("Prototypes not initialized.");	
		
		return this.conditionalProbabilities.get(obj.getID()).clone();
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.FuzzyClusteringAlgorithm#getAllFuzzyClusterAssignments(java.util.List)
	 */
	@Override
	public List<double[]> getAllFuzzyClusterAssignments(List<double[]> assignmentList)
	{
		if(!this.initialized) throw new AlgorithmNotInitializedException("Prototypes not initialized.");	
		
		if(assignmentList == null) assignmentList = new ArrayList<double[]>(this.getDataCount());
		
		assignmentList.addAll(this.conditionalProbabilities);
		return assignmentList;
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.FuzzyClusteringAlgorithm#isFuzzyAssigned(data.set.IndexedDataObject)
	 */
	@Override
	public boolean isFuzzyAssigned(IndexedDataObject<double[]> obj)
	{
		return this.initialized;
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.FuzzyClusteringAlgorithm#getFuzzyAssignmentSums()
	 */
	@Override
	public double[] getFuzzyAssignmentSums()
	{
		if(!this.initialized) throw new AlgorithmNotInitializedException("Prototypes not initialized.");	
		
		double[] sums = new double[this.getClusterCount()];
		int i,j;

		for(i=0; i<this.getClusterCount(); i++)
		{
			for(j=0; j<this.getDataCount(); j++)	
			{
				sums[i] += this.conditionalProbabilities.get(j)[i];
			}
		}
		
		return sums;
	}

	/**
	 * @return the varianceBounded
	 */
	public boolean isVarianceBounded()
	{
		return this.varianceBounded;
	}

	/**
	 * @param varianceBounded the varianceBounded to set
	 */
	public void setVarianceBounded(boolean varianceBounded)
	{
		this.varianceBounded = varianceBounded;
	}

	/**
	 * @return the varianceLowerBound
	 */
	public double getVarianceLowerBound()
	{
		return this.varianceLowerBound;
	}

	/**
	 * @param varianceLowerBound the varianceLowerBound to set
	 */
	public void setVarianceLowerBound(double varianceLowerBound)
	{
		this.varianceLowerBound = varianceLowerBound;
	}

	/**
	 * @return the varianceUpperBound
	 */
	public double getVarianceUpperBound()
	{
		return this.varianceUpperBound;
	}

	/**
	 * @param varianceUpperBound the varianceUpperBound to set
	 */
	public void setVarianceUpperBound(double varianceUpperBound)
	{
		this.varianceUpperBound = varianceUpperBound;
	}

	/**
	 * @return the dist
	 */
	public Distance<double[]> getDist()
	{
		return this.dist;
	}

	/**
	 * @return the clusterProbability
	 */
	public double[] getClusterProbability()
	{
		return this.clusterProbability.clone();
	}

	/**
	 * @return the conditionalProbabilities
	 */
	public ArrayList<double[]> getConditionalProbabilities()
	{
		return new ArrayList<double[]>(this.conditionalProbabilities);
	}
	
	
}
