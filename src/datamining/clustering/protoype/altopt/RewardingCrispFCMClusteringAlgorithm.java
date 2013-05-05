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

import java.util.ArrayList;
import java.util.List;

import data.algebra.Metric;
import data.algebra.VectorSpace;
import data.set.IndexedDataObject;
import data.set.IndexedDataSet;
import datamining.clustering.protoype.AbstractPrototypeClusteringAlgorithm;
import datamining.clustering.protoype.AlgorithmNotInitializedException;
import datamining.clustering.protoype.Centroid;
import etc.MyMath;

/**
 * The rewarding crisp memberships fuzzy c-means clustering algorithm an extension of FCM.
 * The objective function is added an penalty term, that is strong if the membership values are close to 0.5 and that is
 * small for membership values are close to 0 or 1. That way, crisp membership values are regarded more optimal in the
 * objective function, hence the tendency to produce membership values, that give a clear tendency to which cluster a
 * data object belongs. See the paper for more information.<br> 
 * 
 * Paper: H�ppner, F. & Klawonn, F. Improved fuzzy partitions for fuzzy regression models Int. J. Approx. Reasoning, 2003, 32, 85-102<br>
 * 
 * The additional term in the objective function leads to a value that is removed from all distances
 * when calculating the membership values w.r.t. one data object. Other than in the paper, in this implementation
 * that value is chosen to be <code>distanceMultiplierConstant</code>times the smallest distance to all prototypes.
 * So <code>distanceMultiplierConstant</code> should be chosen between 0 and 1. When chosen 0, this algorithm is identical
 * to {@link FuzzyCMeansClusteringAlgorithm} and when chosen 1, it turns into {@link HardCMeansClusteringAlgorithm}.  
 * 
 * In this particular implementation, the membership matrix is not stored when the algorithm is applied. That is possible because the membership
 * values of one data object are independent of all other objects, given the position of the prototypes. Also the additional
 * term in the objective function has no influence on the runtime complexity of the algorithm.<br> 
 * 
 * The runtime complexity of this algorithm is in O(t*n*c),
 * with t being the number of iterations, n being the number of data objects and c being the number of clusters.
 * This is, neglecting the runtime complexity of distance calculations and algebraic operations in the vector space.
 * The full complexity would be in O(t*n*c*(O(dist)+O(add)+O(mul))) where O(dist) is the complexity of
 * calculating the distance between a data object and a prototype, O(add) is the complexity of calculating the
 * vector addition of two types <code>T</code> and O(mul) is the complexity of scalar multiplication of type <code>T</code>. <br>
 *  
 * The memory consumption of this algorithm is in O(t+n+c).
 *
 * 
 * @author Roland Winkler
 */
public class RewardingCrispFCMClusteringAlgorithm<T> extends FuzzyCMeansClusteringAlgorithm<T>
{
	/**  */
	private static final long	serialVersionUID	= -5623960399726400874L;
	/**  */
	protected double distanceMultiplierConstant;
	
	/** If this value is false, the algorithm is performed as the class description text.
	 * If this valie is true, an alternative update algorithm is used.
	 * See subsection 4.1.4 Rewarding Crisp Memberships Fuzzy c-Means in my (Roland Winkler)
	 * phd thesis. */
	protected boolean useHalfSumOptimization;

	/**
	 * Creates a new RewardingCrispFCMClusteringAlgorithm with the specified data set, vector space and metric.
	 * The prototypes are not initialized by this method, it has to be done separately.
	 * The metric must be differentiable w.r.t. <code>y</code> in <code>dist(x, y)<sup>2</sup></code>, and
	 * the directed differential in direction of <code>y</code> must yield <code>d/dy dist(x, y)^2 = 2(y - x)</code>
	 * for the algorithm to be correct.
	 * 
	 * @param data The data set that should be clustered.
	 * @param vs The vector space that is used to calculate the prototype positions.
	 * @param metric The metric that is used to calculate the distance between data objects and prototypes.
	 */
	public RewardingCrispFCMClusteringAlgorithm(IndexedDataSet<T> data, VectorSpace<T> vs, Metric<T> metric)
	{
		super(data, vs, metric);
		
		this.distanceMultiplierConstant = 0.5d;
		this.useHalfSumOptimization = false;
	}


	/**
	 * This constructor creates a new RewardingCrispFCMClusteringAlgorithm, taking an existing prototype clustering algorithm.
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
	public RewardingCrispFCMClusteringAlgorithm(AbstractPrototypeClusteringAlgorithm<T, Centroid<T>> c, boolean useOnlyActivePrototypes)
	{
		super(c, useOnlyActivePrototypes);

		this.distanceMultiplierConstant = 0.5d;
		this.useHalfSumOptimization = false;
	}
	
	
	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.altopt.FuzzyCMeansClusteringAlgorithm#apply(int)
	 */
	@Override
	public void apply(int steps)
	{
		if(!this.initialized) throw new AlgorithmNotInitializedException("Prototypes not initialized.");	
		
		
		int i, j, k, t; 
		// i: index for clusters
		// j: index for data objects
		// k: index for dimensions, others
		// t: index for iterations	
				
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l 
		double doubleTMP = 0.0d;									// a temporarly variable for multiple perpuses
		double maxPrototypeMovement = 0.0d;
		ArrayList<T> newPrototypePosition	= new ArrayList<T>(this.getClusterCount());
		for(i=0; i<this.getClusterCount(); i++) newPrototypePosition.add(this.vs.getNewAddNeutralElement()); 
		double[] fuzzDistances				= new double[this.getClusterCount()];
		double[] membershipValues			= new double[this.getClusterCount()];
		double[] membershipSum				= new double[this.getClusterCount()];
		T tmpX								= this.vs.getNewAddNeutralElement();
		double minDistValue					= 0.0d;
		int minDistIndex					= 0;
		double membershipHalfSum			= 0.0d;
		
		int[] zeroDistanceIndexList			= new int[this.getClusterCount()];
		int zeroDistanceCount;

//		System.out.print(this.algorithmName());
		long timeStart = System.currentTimeMillis();
		
		for(t = 0; t < steps; t++)
		{
//			System.out.print(".");
			
			// reset values
			maxPrototypeMovement = 0.0d;
			
			for(i = 0; i < this.getClusterCount(); i++)
			{
				this.vs.resetToAddNeutralElement(newPrototypePosition.get(i));
				membershipSum[i] = 0.0d;
			}
			
			// update membership values
			for(j = 0; j < this.getDataCount(); j++)
			{				
				for(i=0; i<this.getClusterCount(); i++) zeroDistanceIndexList[i] = -1;
				zeroDistanceCount = 0;
				distanceSum = 0.0d;
				minDistValue = Double.MAX_VALUE;
				minDistIndex = 0;

				for(i=0; i<this.getClusterCount(); i++)
				{
					doubleTMP = this.metric.distanceSq(this.data.get(j).x, this.prototypes.get(i).getPosition());
					fuzzDistances[i] = doubleTMP;
					if(minDistValue > doubleTMP)
					{
						minDistValue = doubleTMP;
						minDistIndex = i;
					}
				}
				minDistValue *= this.distanceMultiplierConstant;
				
				for(i = 0; i < this.getClusterCount(); i++)
				{
					doubleTMP = fuzzDistances[i] - minDistValue;
					if(doubleTMP <= 0.0d)
					{
						doubleTMP = 0.0d;
						zeroDistanceIndexList[zeroDistanceCount] = i;
						zeroDistanceCount++;
					}
					else
					{
						doubleTMP = 1.0d/doubleTMP;

						if(Double.isInfinite(doubleTMP))
						{
							doubleTMP = 0.0d;
							zeroDistanceIndexList[zeroDistanceCount] = i;
							zeroDistanceCount++;
						}
						
						fuzzDistances[i] = doubleTMP;
						distanceSum += doubleTMP;
					}
				}

				// special case handling: if one (or more) prototype sits on top of a data object
				if(zeroDistanceCount>0)
				{
					for(i = 0; i < this.getClusterCount(); i++)
					{
						membershipValues[i] = 0.0d;
					}
					doubleTMP = 1.0d / ((double)zeroDistanceCount);
					for(k=0; k<zeroDistanceCount; k++)
					{
						membershipValues[zeroDistanceIndexList[k]] = doubleTMP;
					}
				}
				else
				{
					for(i = 0; i < this.getClusterCount(); i++)
					{
						doubleTMP = fuzzDistances[i] / distanceSum;
						membershipValues[i] = doubleTMP;
					}
				}

				membershipHalfSum = 0.0d;
				for(i = 0; i < this.getClusterCount(); i++)
				{

					doubleTMP = membershipValues[i]*membershipValues[i];
					if(this.useHalfSumOptimization) membershipHalfSum += (membershipValues[i] - 0.5d)*(membershipValues[i] - 0.5d);
					membershipSum[i] += doubleTMP;

					this.vs.copy(tmpX, this.data.get(j).x);
					this.vs.mul(tmpX, doubleTMP);
					this.vs.add(newPrototypePosition.get(i), tmpX);
					
				}
				
				// The additional term for prototype location calculation.
				if(this.useHalfSumOptimization)
				{
					membershipHalfSum *= this.distanceMultiplierConstant;
					membershipSum[minDistIndex] -= membershipHalfSum;
					this.vs.copy(tmpX, this.data.get(j).x);
					this.vs.mul(tmpX, -membershipHalfSum);
					this.vs.add(newPrototypePosition.get(minDistIndex), tmpX);
				}				
			}

			// update prototype positions
			for(i = 0; i < this.getClusterCount(); i++)
			{
				doubleTMP = 1.0d/membershipSum[i];
				this.vs.mul(newPrototypePosition.get(i), doubleTMP);
			}
			
			// copy new prototype values into prototypes wrt. learning factor
			for(i = 0; i < this.getClusterCount(); i++)
			{
				if(Math.abs(this.learningFactor - 1.0d) > 0.01d)
				{
					this.vs.sub(newPrototypePosition.get(i), this.prototypes.get(i).getPosition());
					this.vs.mul(newPrototypePosition.get(i), this.learningFactor);
					this.vs.add(newPrototypePosition.get(i), this.prototypes.get(i).getPosition());	
				}
				
				doubleTMP = this.metric.distanceSq(this.prototypes.get(i).getPosition(), newPrototypePosition.get(i));
				
				maxPrototypeMovement = (doubleTMP > maxPrototypeMovement)? doubleTMP : maxPrototypeMovement;
				
				this.prototypes.get(i).moveTo(newPrototypePosition.get(i));
			}

			this.iterationComplete();

			if(this.iterationCount >= this.minIterations && maxPrototypeMovement < this.epsilon*this.epsilon) break;
		}

//		System.out.println(" done. [" + (System.currentTimeMillis() - timeStart) + "]");
	}
	
	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.altopt.FuzzyCMeansClusteringAlgorithm#getObjectiveFunctionValue()
	 */
	@Override
	public double getObjectiveFunctionValue()
	{
		if(!this.initialized) throw new AlgorithmNotInitializedException("Prototypes not initialized.");
		
		int i, j, k; 
		// i: index for clusters
		// j: index for data objects
		// k: index for dimensions, others
		
		double objectiveFunctionValue = 0.0d;
		
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l 
		double doubleTMP = 0.0d;									// a temporarly variable for multiple perpuses
		double[] fuzzDistances					= new double[this.getClusterCount()];
		double[] distancesSq					= new double[this.getClusterCount()];
		double minDistValue = 0.0d;

						
		for(j=0; j < this.getDataCount(); j++)
		{				
			distanceSum = 0.0d;
			minDistValue = Double.MAX_VALUE;

			for(i=0; i<this.getClusterCount(); i++)
			{
				doubleTMP = this.metric.distanceSq(this.data.get(j).x, this.prototypes.get(i).getPosition());
				distancesSq[i] = doubleTMP;
				if(minDistValue > doubleTMP) minDistValue = doubleTMP;
			}
			if(minDistValue <= 0.0d) continue;
			
			minDistValue *= this.distanceMultiplierConstant;
			
			for(i=0; i<this.getClusterCount(); i++)
			{
				doubleTMP = distancesSq[i] - minDistValue;
				if(doubleTMP <= 0.0d)
				{
					for(k=0; k<this.getClusterCount(); k++) fuzzDistances[k] = 0.0d;
					fuzzDistances[i] = 1.0d;
					distanceSum = 1.0d;
					break;
				}
				else
				{
					doubleTMP = 1.0d/doubleTMP;

					if(Double.isInfinite(doubleTMP))
					{
						doubleTMP = 0.0d;
						for(k=0; k<this.getClusterCount(); k++) fuzzDistances[k] = 0.0d;
						fuzzDistances[i] = 1.0d;
						distanceSum = 1.0d;
						break;
					}
					
					fuzzDistances[i] = doubleTMP;
					distanceSum += doubleTMP;
				}
			}
			
			for(i=0; i<this.getClusterCount(); i++)
			{
				doubleTMP = fuzzDistances[i] / distanceSum;
								
				objectiveFunctionValue += doubleTMP*doubleTMP*distancesSq[i] - minDistValue*(doubleTMP - 0.5d)*(doubleTMP - 0.5d);
			}
		}
		
		return objectiveFunctionValue;
	}
	
	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.altopt.FuzzyCMeansClusteringAlgorithm#getAllFuzzyClusterAssignments(java.util.List)
	 */
	@Override
	public List<double[]> getAllFuzzyClusterAssignments(List<double[]>  assignmentList)
	{
		if(!this.initialized) throw new AlgorithmNotInitializedException("Prototypes not initialized.");
		
		if(assignmentList == null) assignmentList = new ArrayList<double[]>(this.getDataCount());
		assignmentList.clear();
				
		int i, j, k;
				
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l 
		double doubleTMP = 0.0d;									// a temporarly variable for multiple perpuses
		double[] fuzzDistances				= new double[this.getClusterCount()];
		double[] membershipValues			= new double[this.getClusterCount()];
		int[] zeroDistanceIndexList			= new int[this.getClusterCount()];
		int zeroDistanceCount;
		double minDistValue = 0.0d;
			
						
		for(j=0; j < this.getDataCount(); j++)
		{				
			for(i=0; i<this.getClusterCount(); i++) zeroDistanceIndexList[i] = -1;
			zeroDistanceCount = 0;
			distanceSum = 0.0d;
			minDistValue = Double.MAX_VALUE;

			for(i=0; i<this.getClusterCount(); i++)
			{
				doubleTMP = this.metric.distanceSq(this.data.get(j).x, this.prototypes.get(i).getPosition());
				fuzzDistances[i] = doubleTMP;
				if(minDistValue > doubleTMP) minDistValue = doubleTMP;
			}
			minDistValue *= this.distanceMultiplierConstant;
			
			for(i = 0; i < this.getClusterCount(); i++)
			{
				doubleTMP = fuzzDistances[i] - minDistValue;
				if(doubleTMP <= 0.0d)
				{
					doubleTMP = 0.0d;
					zeroDistanceIndexList[zeroDistanceCount] = i;
					zeroDistanceCount++;
				}
				else
				{
					doubleTMP = 1.0d/doubleTMP;

					if(Double.isInfinite(doubleTMP))
					{
						doubleTMP = 0.0d;
						zeroDistanceIndexList[zeroDistanceCount] = i;
						zeroDistanceCount++;
					}
					
					fuzzDistances[i] = doubleTMP;
					distanceSum += doubleTMP;
				}
			}
	
			// special case handling: if one (or more) prototype sits on top of a data object
			if(zeroDistanceCount>0)
			{
				for(i=0; i<this.getClusterCount(); i++)
				{
					membershipValues[i] = 0.0d;
				}
				doubleTMP = 1.0d / ((double)zeroDistanceCount);
				for(k=0; k<zeroDistanceCount; k++)
				{
					membershipValues[zeroDistanceIndexList[k]] = doubleTMP;
				}
			}
			else
			{
				for(i=0; i<this.getClusterCount(); i++)
				{
					doubleTMP = fuzzDistances[i] / distanceSum;
					membershipValues[i] = doubleTMP;
				}
			}
			
			assignmentList.add(membershipValues.clone());
		}
		
		return assignmentList;
	}
	
	

	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.altopt.FuzzyCMeansClusteringAlgorithm#getFuzzyAssignmentSums()
	 */
	@Override
	public double[] getFuzzyAssignmentSums()
	{
		if(!this.initialized) throw new AlgorithmNotInitializedException("Prototypes not initialized.");
						
		int i, j, k;
				
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l 
		double doubleTMP = 0.0d;									// a temporarly variable for multiple perpuses
		double[] fuzzDistances				= new double[this.getClusterCount()];
		double[] membershipValueSums		= new double[this.getClusterCount()];
		int[] zeroDistanceIndexList			= new int[this.getClusterCount()];
		int zeroDistanceCount;
		double minDistValue = 0.0d;
			
						
		for(j=0; j < this.getDataCount(); j++)
		{				
			for(i=0; i<this.getClusterCount(); i++) zeroDistanceIndexList[i] = -1;
			zeroDistanceCount = 0;
			distanceSum = 0.0d;
			minDistValue = Double.MAX_VALUE;

			for(i=0; i<this.getClusterCount(); i++)
			{
				doubleTMP = this.metric.distanceSq(this.data.get(j).x, this.prototypes.get(i).getPosition());
				fuzzDistances[i] = doubleTMP;
				if(minDistValue > doubleTMP) minDistValue = doubleTMP;
			}
			minDistValue *= this.distanceMultiplierConstant;
			
			for(i = 0; i < this.getClusterCount(); i++)
			{
				doubleTMP = fuzzDistances[i] - minDistValue;
				if(doubleTMP <= 0.0d)
				{
					doubleTMP = 0.0d;
					zeroDistanceIndexList[zeroDistanceCount] = i;
					zeroDistanceCount++;
				}
				else
				{
					doubleTMP = 1.0d/doubleTMP;

					if(Double.isInfinite(doubleTMP))
					{
						doubleTMP = 0.0d;
						zeroDistanceIndexList[zeroDistanceCount] = i;
						zeroDistanceCount++;
					}
					
					fuzzDistances[i] = doubleTMP;
					distanceSum += doubleTMP;
				}
			}
	
			// special case handling: if one (or more) prototype sits on top of a data object
			if(zeroDistanceCount>0)
			{
				doubleTMP = 1.0d / ((double)zeroDistanceCount);
				for(k=0; k<zeroDistanceCount; k++)
				{
					membershipValueSums[zeroDistanceIndexList[k]] += doubleTMP;
				}
			}
			else
			{
				for(i=0; i<this.getClusterCount(); i++)
				{
					doubleTMP = fuzzDistances[i] / distanceSum;
					membershipValueSums[i] += doubleTMP;
				}
			}
		}
		
		return membershipValueSums;
	}


	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.altopt.FuzzyCMeansClusteringAlgorithm#getFuzzyAssignmentsOf(data.set.IndexedDataObject)
	 */
	@Override
	public double[] getFuzzyAssignmentsOf(IndexedDataObject<T> obj)
	{
		if(!this.initialized) throw new AlgorithmNotInitializedException("Prototypes not initialized.");

		int i, k;
				
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l 
		double doubleTMP = 0.0d;									// a temporarly variable for multiple perpuses
		double[] fuzzDistances				= new double[this.getClusterCount()];
		double[] membershipValues			= new double[this.getClusterCount()];
		int[] zeroDistanceIndexList			= new int[this.getClusterCount()];
		int zeroDistanceCount;
		double minDistValue = 0.0d;
		
		for(i=0; i<this.getClusterCount(); i++) zeroDistanceIndexList[i] = -1;
		zeroDistanceCount = 0;
		distanceSum = 0.0d;
		minDistValue = Double.MAX_VALUE;

		for(i=0; i<this.getClusterCount(); i++)
		{
			doubleTMP = this.metric.distanceSq(obj.x, this.prototypes.get(i).getPosition());
			fuzzDistances[i] = doubleTMP;
			if(minDistValue > doubleTMP) minDistValue = doubleTMP;
		}
		minDistValue *= this.distanceMultiplierConstant;
		
		for(i = 0; i < this.getClusterCount(); i++)
		{
			doubleTMP = fuzzDistances[i] - minDistValue;
			if(doubleTMP <= 0.0d)
			{
				doubleTMP = 0.0d;
				zeroDistanceIndexList[zeroDistanceCount] = i;
				zeroDistanceCount++;
			}
			else
			{
				doubleTMP = 1.0d/doubleTMP;

				if(Double.isInfinite(doubleTMP))
				{
					doubleTMP = 0.0d;
					zeroDistanceIndexList[zeroDistanceCount] = i;
					zeroDistanceCount++;
				}
				
				fuzzDistances[i] = doubleTMP;
				distanceSum += doubleTMP;
			}
		}

		// special case handling: if one (or more) prototype sits on top of a data object
		if(zeroDistanceCount>0)
		{
			for(i=0; i<this.getClusterCount(); i++)
			{
				membershipValues[i] = 0.0d;
			}
			doubleTMP = 1.0d / ((double)zeroDistanceCount);
			for(k=0; k<zeroDistanceCount; k++)
			{
				membershipValues[zeroDistanceIndexList[k]] = doubleTMP;
			}
		}
		else
		{
			for(i=0; i<this.getClusterCount(); i++)
			{
				doubleTMP = fuzzDistances[i] / distanceSum;
				membershipValues[i] = doubleTMP;
			}
		}
		
		return membershipValues;
	}
		
	/**
	 * Returns the distance multiplier constant.
	 * 
	 * @return the distance multiplier constant.
	 */
	public double getDistanceMultiplierConstant()
	{
		return this.distanceMultiplierConstant;
	}

	/**
	 * Sets the distance multiplier constant. The value must be between 0 and 1.
	 * 
	 * @param distanceMultiplierConstant The distance multiplier constant to set.
	 */
	public void setDistanceMultiplierConstant(double distanceMultiplierConstant)
	{
		if(distanceMultiplierConstant < 0.0d || 1.0d < distanceMultiplierConstant) throw new IllegalArgumentException("The distance multiplier constant parameter must be larger than 0 and smaller than 1. Specified distance multiplier constant: " + distanceMultiplierConstant);
				
		this.distanceMultiplierConstant = distanceMultiplierConstant;
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.altopt.FuzzyCMeansClusteringAlgorithm#algorithmName()
	 */
	@Override
	public String algorithmName()
	{
		return "Rewarding Crisp Memberships FcM";
	}


	/**
	 * @return the useHalfSumOptimization
	 */
	public boolean isUseHalfSumOptimization()
	{
		return this.useHalfSumOptimization;
	}


	/**
	 * @param useHalfSumOptimization the useHalfSumOptimization to set
	 */
	public void setUseHalfSumOptimization(boolean useHalfSumOptimization)
	{
		this.useHalfSumOptimization = useHalfSumOptimization;
	}
	
	
}
