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

import data.algebra.Metric;
import data.algebra.VectorSpace;
import data.set.IndexedDataObject;
import data.set.IndexedDataSet;
import datamining.clustering.protoype.AbstractPrototypeClusteringAlgorithm;
import datamining.clustering.protoype.AlgorithmNotInitializedException;
import datamining.clustering.protoype.SphericalNormalDistributionPrototype;
import datamining.resultProviders.FuzzyClusteringProvider;

/**
 * This is an implementation of the expectation maximization algorithm for a mixture of (hyper-) spherical normal distributions.
 * A version for arbitrary shaped normal distributions is going to be implemented once some matrix related structures and 
 * algorithms like matrix inversion etc. are implemented. The theory regarding the EM algorithm is quite complex and
 * can not be fully described here. Please see the literature for further information, for example:<br> 
 * 
 * Paper: Borgelt, C. Prototype-based Classification and Clustering (Habilitationsschrift) Otto-von-Guericke-University of Magdeburg, Germany, 2005<br>
 * 
 * I am not sure this algorithm can be implemented for anything else than a real vector space. Therefore, this algorithm
 * does not provide the same algebraic range as the other clustering algorithms in this package.<br>
 *  
 * In this implementation, the membership matrix (aka conditional probabilities that x_j belongs to cluster i, given x_j) needs to
 * be held in memory because for updating the variances of the normal distributions, both the new prototype locations as well as
 * the new membership values need to be known. Therefore, the complete membership matrix must be stored during the optimization.
 * Since the memory must be available anyway, it is stored as class object to simplify the fuzzy clustering algorithm
 * queries from the respective interface.<br>
 * 
 * In many cases, the variances of the normal distributions should be bounded because for very small variances and very large variances,
 * the algorithm produces numerically unstable values. This is especially very likely, if teh data set contains data objects that are
 * identical. A variance in the range of 0.01 to 100 for a coordinate values in rage 0 to 1 is suitable and in most cases numerically stable.<br>
 * 
 * The runtime complexity of this algorithm is in O(t*n*c),
 * with t being the number of iterations, n being the number of data objects and c being the number of clusters.
 * This is, neglecting the runtime complexity of distance calculations and algebraic operations in the vector space.
 * The full complexity would be in O(t*n*c*d)) where d is the dimension (aka number of attributes or number of elements
 * in the data object double arrays) of the feature space. <br>
 *  
 * The memory consumption of this algorithm is in O(t+n*c), or with taking the number of dimensions d into account: O(t+n*(c+d)).
 * 
 * @author Roland Winkler
 */
public class ExpectationMaximizationSGMMClusteringAlgorithm extends AbstractPrototypeClusteringAlgorithm<double[], SphericalNormalDistributionPrototype> implements Serializable, FuzzyClusteringProvider<double[]>
{	
	/**  */
	private static final long	serialVersionUID	= -8858858125481849303L;
	
	/** The marginal probabilities that a data object belongs to a cluster. */
	protected double[] clusterProbability;
	
	/** The membership matrix in fuzzy terms, or the matrix of conditional probabilities
	 * with entries at i, j: The conditional probability that x_j belongs to cluster i, given x_j */
	protected ArrayList<double[]> conditionalProbabilities;
	
	/** State, that the variance is bounded and that the bounds are specified by 
	 * {@link #varianceLowerBound} and {@link #varianceUpperBound}. */
	protected boolean varianceBounded;	
	
	/** The lower bound of variances. */
	protected double varianceLowerBound;
	
	/** The upper bound of variances. */
	protected double varianceUpperBound;

	/**
	 * This constructor creates a new ExpectationMaximizationSGMMClusteringAlgorithm, taking an existing prototype clustering algorithm.
	 * It has the option to use only active prototypes from the old clustering algorithm. This constructor is especially
	 * useful if the clustering is done in multiple steps. The first clustering algorithm can for example calculate the
	 * initial positions of the prototypes for the second clustering algorithm. An other option is, that the first clustering
	 * algorithm creates a set of deactivated prototypes and the second clustering algorithm is initialized with less
	 * clusters than the first.
	 * 
	 * @param c the elders clustering algorithm.
	 * @param useOnlyActivePrototypes States, that only prototypes that are active in the old clustering
	 * algorithm are used for the new clustering algorithm.
	 */
	public ExpectationMaximizationSGMMClusteringAlgorithm(AbstractPrototypeClusteringAlgorithm<double[], SphericalNormalDistributionPrototype> c, boolean useOnlyActivePrototypes)
	{
		super(c, useOnlyActivePrototypes);

		this.clusterProbability = null;
		this.conditionalProbabilities = null;
		
		this.varianceBounded = false;
		this.varianceLowerBound = 0.0d;
		this.varianceUpperBound = Double.MAX_VALUE;
	}

	/**
	 * Creates a new ExpectationMaximizationSGMMClusteringAlgorithm with the specified data set, vector space and metric.
	 * The prototypes are not initialized by this method, it has to be done separately.
	 * The metric must be differentiable w.r.t. <code>y</code> in <code>dist(x, y)<sup>2</sup></code>, and
	 * the directed differential in direction of <code>y</code> must yield <code>d/dy dist(x, y)^2 = 2(y - x)</code>
	 * for the algorithm to be correct.
	 * 
	 * @param data The data set that should be clustered.
	 * @param vs The vector space that is used to calculate the prototype positions.
	 * @param metric The metric that is used to calculate the distance between data objects and prototypes.
	 */
	public ExpectationMaximizationSGMMClusteringAlgorithm(IndexedDataSet<double[]> data, VectorSpace<double[]> vs, Metric<double[]> metric)
	{
		super(data, vs, metric);
		
		this.clusterProbability = null;
		this.conditionalProbabilities = null;
		
		this.varianceBounded = false;
		this.varianceLowerBound = 0.0d;
		this.varianceUpperBound = Double.MAX_VALUE;
	}
	
	/**
	 * Recalculate the conditional probabilities and marginal probabilities of the clusters.
	 * They are stored in the class fields {@link #conditionalProbabilities} and {@link #clusterProbability}.<br>
	 * 
	 * The runtime complexity of this function is in O(n*c)  
	 */
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
				this.conditionalProbabilities.get(j)[i] *= this.prototypes.get(i).density(this.getDataSet().get(j).x);
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

	/**
	 * Recalculate the variances of the clusters.
	 * 
	 * The runtime complexity of this function is in O(n*c)  
	 */
	protected void recalculateVariances()
	{
		double[] invCondDOProbSum = new double[this.getClusterCount()];
		double doubleTMP;
		
		// the maximization step
		// cluster weight probabilities
		for(int i=0; i<this.getClusterCount(); i++)
		{
			invCondDOProbSum[i] = 1.0d/(this.clusterProbability[i]*((double)this.getDataCount()));
		}
		
		for(int i=0; i<this.getClusterCount(); i++)
		{
			doubleTMP = 0.0d;				
			for(int j=0; j<this.getDataCount(); j++)
			{
				doubleTMP += this.conditionalProbabilities.get(j)[i] *  this.metric.distanceSq(this.data.get(j).x, this.prototypes.get(i).getPosition());
			}
			doubleTMP *= invCondDOProbSum[i] / ((double)this.vs.getDimension());

			this.prototypes.get(i).setVariance(doubleTMP);
			if(this.varianceBounded)
			{
				if(this.prototypes.get(i).getVariance() < this.varianceLowerBound) 
					this.prototypes.get(i).setVariance(this.varianceLowerBound);
				if(this.prototypes.get(i).getVariance() > this.varianceUpperBound) 
					this.prototypes.get(i).setVariance(this.varianceUpperBound);
			}
		}
	}
		
	/**
	 * Recalculate the conditional probabilities, marginal probabilities and variances of the clusters.
	 * They are stored in the class fields {@link #conditionalProbabilities} and {@link #clusterProbability}.<br>
	 * 
	 * The runtime complexity of this function is in O(n*c*iterations)
	 * 
	 * @param iterations The number of iterations this should be done. Usually 5 should be sufficient.
	 */
	public void optimizeProbabilities(int iterations)
	{
		for(int t=0; t<iterations; t++)
		{
			this.recalculateProbabilities();
			this.recalculateVariances();
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
			centr = new SphericalNormalDistributionPrototype(this.vs, this.metric, x, 1.0d);
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
	 * @see datamining.clustering.protoype.AbstractPrototypeClusteringAlgorithm#algorithmName()
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
		double maxPrototypeMovement = 0.0d;
		double[] invCondDOProbSum = new double[this.getClusterCount()];
		double[] tmpX = this.vs.getNewAddNeutralElement();

		ArrayList<double[]> newExpectationValues = new ArrayList<double[]>(this.getClusterCount());
		for(i=0; i<this.getClusterCount(); i++) newExpectationValues.add(this.vs.getNewAddNeutralElement());
		

//		System.out.print(this.algorithmName());
		long timeStart = System.currentTimeMillis();
		
		for(t = 0; t < steps; t++)
		{
//			System.out.print(".");
			
			maxPrototypeMovement = 0.0d;

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
					this.conditionalProbabilities.get(j)[i] *= this.prototypes.get(i).density(this.getDataSet().get(j).x);
					doubleTMP += this.conditionalProbabilities.get(j)[i];
//					if(Double.isNaN(this.conditionalProbabilities.get(j)[i]))
//						System.out.println("MOEOEOEP 1");
				}
				doubleTMP = 1.0d/doubleTMP;
				for(i=0; i<this.getClusterCount(); i++)
				{
					this.conditionalProbabilities.get(j)[i] *= doubleTMP;
					invCondDOProbSum[i] += this.conditionalProbabilities.get(j)[i];
//					if(Double.isNaN(this.conditionalProbabilities.get(j)[i]))
//						System.out.println("MOEOEOEP 2");
				}
			}
			
			// the maximization step
			// cluster weight probabilities
			for(i=0; i<this.getClusterCount(); i++)
			{
				this.clusterProbability[i] = invCondDOProbSum[i]/((double)this.getDataCount());
				invCondDOProbSum[i] = 1.0d/invCondDOProbSum[i];
				
//				if(Double.isNaN(this.clusterProbability[i]))
//					System.out.println("MOEOEOEP 3");
				
//				if(Double.isNaN(invCondDOProbSum[i]))
//					System.out.println("MOEOEOEP 4");
			}
			
			// new expectation values
			for(j=0; j<this.getDataCount(); j++)
			{
				for(i=0; i<this.getClusterCount(); i++) 
				{				
					this.vs.copy(tmpX, this.data.get(j).x);
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
				
				doubleTMP = ((this.convergenceMetric!=null)?this.convergenceMetric:this.metric).distanceSq(this.prototypes.get(i).getPosition(), newExpectationValues.get(i));
				if(doubleTMP > maxPrototypeMovement) maxPrototypeMovement = doubleTMP;
				
				this.prototypes.get(i).moveTo(newExpectationValues.get(i));
			}
			
			// variances
			for(i=0; i<this.getClusterCount(); i++)
			{
				doubleTMP = 0.0d;				
				for(j=0; j<this.getDataCount(); j++)
				{
					doubleTMP += this.conditionalProbabilities.get(j)[i] *  this.metric.distanceSq(this.data.get(j).x, this.prototypes.get(i).getPosition());
				}
				doubleTMP *= invCondDOProbSum[i] / ((double)this.vs.getDimension());

//				if(Double.isNaN(doubleTMP))
//					System.out.println("MOEOEOEP 5");
				
				
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

			this.convergenceHistory.add(Math.sqrt(maxPrototypeMovement));
			if(this.iterationCount >= this.minIterations && maxPrototypeMovement < this.epsilon*this.epsilon) break;
		}

//		System.out.println(" done. [" + (System.currentTimeMillis() - timeStart) + "]");
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
			doubleTMP -= 0.5d*((double)this.vs.getDimension())*Math.log(this.prototypes.get(i).getVariance());

			for(j=0; j<this.getDataCount(); j++)	
			{
				objectiveFunctionValue += this.conditionalProbabilities.get(j)[i] * (doubleTMP - 0.5d*this.metric.distanceSq(this.prototypes.get(i).getPosition(), this.data.get(j).x)/this.prototypes.get(i).getVariance());
			}
		}
		
//		if(objectiveFunctionValue < 0.0d)
//		{
//			double[] marginalProbabilities = new double[this.getDataCount()];
//			for(j=0; j<this.getDataCount(); j++)
//			{
//				for(i=0; i<this.getClusterCount(); i++)
//				{
//					marginalProbabilities[j] += this.conditionalProbabilities.get(j)[i];
//				}
//			}
//
//			double clusterProbSum=0.0d;
//			for(i=0; i<this.getClusterCount(); i++)
//			{
//				clusterProbSum += this.clusterProbability[i];
//			}
//			
//			System.out.println("Warning: negative OFV");
//		}
		
		
		
		return objectiveFunctionValue;
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.FuzzyClusteringProvider#getFuzzyAssignmentsOf(data.set.IndexedDataObject)
	 */
	@Override
	public double[] getFuzzyAssignmentsOf(IndexedDataObject<double[]> obj)
	{
		if(!this.initialized) throw new AlgorithmNotInitializedException("Prototypes not initialized.");	
		
		return this.conditionalProbabilities.get(obj.getID());
	}


	/* (non-Javadoc)
	 * @see datamining.clustering.FuzzyClusteringProvider#getAllFuzzyClusterAssignments(java.util.List)
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
	 * @see datamining.clustering.FuzzyClusteringProvider#isFuzzyAssigned(data.set.IndexedDataObject)
	 */
	@Override
	public boolean isFuzzyAssigned(IndexedDataObject<double[]> obj)
	{
		return this.initialized;
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.FuzzyClusteringProvider#getFuzzyAssignmentSums()
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
	 * Indicates whether or not the variances are bounded.
	 * 
	 * @return whether or not the variances are bounded.
	 */
	public boolean isVarianceBounded()
	{
		return this.varianceBounded;
	}

	/**
	 * Sets whether or not the variances are bounded.
	 * 
	 * @param varianceBounded whether or not the variances are bounded.
	 */
	public void setVarianceBounded(boolean varianceBounded)
	{
		this.varianceBounded = varianceBounded;
	}

	/**
	 * Returns the lower bound of the variances.
	 * 
	 * @return The lower bound of the variances.
	 */
	public double getVarianceLowerBound()
	{
		return this.varianceLowerBound;
	}

	/**
	 * Sets the lower bound of the variances. The value must be larger than 0.
	 * 
	 * @param varianceLowerBound The new lower bound of the variances.
	 */
	public void setVarianceLowerBound(double varianceLowerBound)
	{
		if(varianceLowerBound < 0.0d) throw new IllegalArgumentException("The lower bound of the variances must be larger than 1. Specified  lower bound of the variances: " + varianceLowerBound);
		
		this.varianceLowerBound = varianceLowerBound;
	}

	/**
	 * Returns the upper bound of the variances.
	 * 
	 * @return The upper bound of the variances.
	 */
	public double getVarianceUpperBound()
	{
		return this.varianceUpperBound;
	}

	/**
	 * Sets the upper bound of the variances. The value must be larger than 0.
	 * 
	 * @param varianceUpperBound The new lower bound of the variances.
	 */
	public void setVarianceUpperBound(double varianceUpperBound)
	{

		if(varianceUpperBound < 0.0d) throw new IllegalArgumentException("The upper bound of the variances must be larger than 1. Specified upper bound of the variances: " + varianceUpperBound);
		
		this.varianceUpperBound = varianceUpperBound;
	}

	/**
	 * Returns the marginal probabilities that a (unspecified) data object belongs to a cluster. It can also be regarded
	 * as the relative size of the cluster. 
	 * 
	 * @return The marginal probabilities that a data object belongs to a cluster.
	 */
	public double[] getClusterProbability()
	{
		return this.clusterProbability.clone();
	}

	/**
	 * Returns essentially the membership matrix in fuzzy terms, or the matrix of conditional probabilities
	 * with entries at i, j: The conditional probability that x_j belongs to cluster i, given x_j
	 * 
	 * @return The matrix of conditionalProbabilities.
	 */
	public ArrayList<double[]> getConditionalProbabilities()
	{
		return new ArrayList<double[]>(this.conditionalProbabilities);
	}
	
	
}
