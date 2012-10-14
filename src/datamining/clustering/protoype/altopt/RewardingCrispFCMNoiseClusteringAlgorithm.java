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
import datamining.resultProviders.FuzzyNoiseClusteringProvider;
import etc.MyMath;

/**
 * This is an implementation of the rewarding crisp memberships fuzzy c-means clustering algorithm with additional noise cluster.
 * The objective function is added an penalty term, that is strong if the membership values are close to 0.5 and that is
 * small for membership values are close to 0 or 1. That way, crisp membership values are regarded more optimal in the
 * objective function, hence the tendency to produce membership values, that give a clear tendency to which cluster a
 * data object belongs. See the paper for more information.<br> 
 * 
 * Paper: Höppner, F. & Klawonn, F. Improved fuzzy partitions for fuzzy regression models Int. J. Approx. Reasoning, 2003, 32, 85-102<br>
 * 
 * The additional term in the objective function leads to a value that is removed from all distances
 * when calculating the membership values w.r.t. one data object. Other than in the paper, in this implementation
 * that value is chosen to be <code>distanceMultiplierConstant</code>times the smallest distance to all prototypes.
 * So <code>distanceMultiplierConstant</code> should be chosen between 0 and 1. When chosen 0, this algorithm is identical
 * to {@link FuzzyCMeansClusteringAlgorithm} and when chosen 1, it turns into {@link HardCMeansClusteringAlgorithm}.  
 * 
 * The noise cluster has, similar to {@link FuzzyCMeansNoiseClusteringAlgorithm}, a constant distance to all data objects.
 * Due to the more crisp membership values, data objects that are far away from all prototypes have a membership value
 * of almost 1 to the noise cluster. Therefore, if all prototypes are initialized far away from a cluster that is present
 * in the data, it is likely that this cluster is never found. Therefore, similar to {@link PolynomFCMNoiseClusteringAlgorithm},
 * it is advisable to make several runs with this clustering algorithm. The first run with a large noise distance
 * and than iteratively reducing the noise distance for each run.<br>   
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
public class RewardingCrispFCMNoiseClusteringAlgorithm<T> extends RewardingCrispFCMClusteringAlgorithm<T> implements FuzzyNoiseClusteringProvider<T>
{	
	/**  */
	private static final long	serialVersionUID	= 6531335434133789423L;
	/**  */
	protected double noiseDistance;
	
	/**
	 * @param data
	 * @param evs
	 */
	public RewardingCrispFCMNoiseClusteringAlgorithm(IndexedDataSet<T> data, VectorSpace<T> vs, Metric<T> dist)
	{
		super(data, vs, dist);
		
		this.noiseDistance = 0.1d*Math.sqrt(Double.MAX_VALUE);
	}


	/**
	 * @param c
	 * @param useOnlyActivePrototypes
	 */
	public RewardingCrispFCMNoiseClusteringAlgorithm(AbstractPrototypeClusteringAlgorithm<T, Centroid<T>> c, boolean useOnlyActivePrototypes)
	{
		super(c, useOnlyActivePrototypes);

		this.noiseDistance = 0.1d*Math.sqrt(Double.MAX_VALUE);
	}
	
	
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
		double membershipHalfSum			= 0.0d;
		int minDistIndex					= 0;
		double noiseMembershipValue			= 0.0d;
		
		
		int[] zeroDistanceIndexList			= new int[this.getClusterCount()];
		int zeroDistanceCount;
		
		for(t = 0; t < steps; t++)
		{
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
				if(minDistValue > this.noiseDistance*this.noiseDistance) minDistValue = this.noiseDistance*this.noiseDistance;
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
						fuzzDistances[i] = doubleTMP;
						distanceSum += doubleTMP;
					}
				}				
				distanceSum += 1.0d/(this.noiseDistance*this.noiseDistance - minDistValue);

				// special case handling: if one (or more) prototype sits on top of a data object
				if(zeroDistanceCount>0)
				{
					noiseMembershipValue = 0.0d;
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
					noiseMembershipValue = (this.noiseDistance*this.noiseDistance - minDistValue)/distanceSum;
				}
				
				membershipHalfSum = 0.0d;
				for(i = 0; i < this.getClusterCount(); i++)
				{

					doubleTMP = membershipValues[i]*membershipValues[i];
					membershipHalfSum += (membershipValues[i] - 0.5d)*(membershipValues[i] - 0.5d);
					membershipSum[i] += doubleTMP;

					this.vs.copy(tmpX, this.data.get(j).x);
					this.vs.mul(tmpX, doubleTMP);
					this.vs.add(newPrototypePosition.get(i), tmpX);
				}				
				membershipHalfSum += (noiseMembershipValue - 0.5d)*(noiseMembershipValue - 0.5d);
				
				// The additional term for prototype location calculation.
				membershipHalfSum *= this.distanceMultiplierConstant;
//				membershipSum[minDistIndex] -= membershipHalfSum;
				this.vs.copy(tmpX, this.data.get(j).x);
				this.vs.mul(tmpX, -membershipHalfSum);
//				this.vs.add(newPrototypePosition.get(minDistIndex), tmpX);
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
			
			if(maxPrototypeMovement < this.epsilon*this.epsilon) break;
		}
	}
	
	
	
	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.AbstractPrototypeClusteringAlgorithm#getObjectiveFunctionValue()
	 */
	@Override
	public double getObjectiveFunctionValue()
	{
		if(!this.initialized) throw new AlgorithmNotInitializedException("Prototypes not initialized.");
		
		int i, j; 
		// i: index for clusters
		// j: index for data objects
		// k: index for dimensions, others
		
		double objectiveFunctionValue = 0.0d;
		
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l 
		double doubleTMP = 0.0d;									// a temporarly variable for multiple perpuses
		double[] fuzzDistances					= new double[this.getClusterCount()];
		double[] distancesSq					= new double[this.getClusterCount()];
		double minDistValue = 0.0d;
		double fuzzyNoiseDist = 0.0d;
		
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
			if(minDistValue > this.noiseDistance*this.noiseDistance) minDistValue = this.noiseDistance*this.noiseDistance;
			minDistValue *= this.distanceMultiplierConstant;
			
			for(i=0; i<this.getClusterCount(); i++)
			{
				doubleTMP = distancesSq[i] - minDistValue;
				if(doubleTMP <= 0.0d)
				{
					fuzzDistances[i] = 1.0d;
				}
				else
				{
					doubleTMP = 1.0d/doubleTMP;
					fuzzDistances[i] = doubleTMP;
					distanceSum += doubleTMP;
				}
			}
			fuzzyNoiseDist = 1.0d/(this.noiseDistance*this.noiseDistance - minDistValue);
			distanceSum += fuzzyNoiseDist;
		
			// don't check for distance sum to be zero.. that would just be rediculus!!
		
			for(i=0; i<this.getClusterCount(); i++)
			{
				doubleTMP = fuzzDistances[i] / distanceSum;
								
				objectiveFunctionValue += MyMath.pow(doubleTMP, this.fuzzifier) * distancesSq[i]  - minDistValue*(doubleTMP - 0.5d)*(doubleTMP - 0.5d);
			}
			doubleTMP = fuzzyNoiseDist / distanceSum;
			
			objectiveFunctionValue += MyMath.pow(doubleTMP, this.fuzzifier) * (this.noiseDistance*this.noiseDistance) - minDistValue*(doubleTMP - 0.5d)*(doubleTMP - 0.5d);
		}
		
		return objectiveFunctionValue;
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
			if(minDistValue > this.noiseDistance*this.noiseDistance) minDistValue = this.noiseDistance*this.noiseDistance;
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
					fuzzDistances[i] = doubleTMP;
					distanceSum += doubleTMP;
				}
			}
			distanceSum += 1.0d/(this.noiseDistance*this.noiseDistance - minDistValue);
	
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
	 * @see datamining.clustering.protoype.FuzzyCMeansClusteringAlgorithm#getAllFuzzyClusterAssignments(java.util.List)
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
			if(minDistValue > this.noiseDistance*this.noiseDistance) minDistValue = this.noiseDistance*this.noiseDistance;
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
					fuzzDistances[i] = doubleTMP;
					distanceSum += doubleTMP;
				}
			}
			distanceSum += 1.0d/(this.noiseDistance*this.noiseDistance - minDistValue);
	
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
	 * @see datamining.clustering.protoype.FuzzyCMeansClusteringAlgorithm#getFuzzyAssignmentsOf(data.set.IndexedDataObject)
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
		if(minDistValue > this.noiseDistance*this.noiseDistance) minDistValue = this.noiseDistance*this.noiseDistance;
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
				doubleTMP = 1.0/doubleTMP;
				fuzzDistances[i] = doubleTMP;
				distanceSum += doubleTMP;
			}
		}
		distanceSum += 1.0d/(this.noiseDistance*this.noiseDistance - minDistValue);

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
		

	/* (non-Javadoc)
	 * @see datamining.clustering.AbstractDoubleArrayClusteringAlgorithm#algorithmName()
	 */
	@Override
	public String algorithmName()
	{
		return "Rewarding Crisp Memberships FcM";
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.FuzzyClusteringAlgorithm#isFuzzyAssigned(data.set.IndexedDataObject)
	 */
	@Override
	public boolean isFuzzyAssigned(IndexedDataObject<T> obj)
	{
		return this.initialized && this.getFuzzyNoiseAssignmentOf(obj) < 1.0d;
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.FuzzyNoiseClusteringProvider#getFuzzyNoiseAssignments()
	 */
	@Override
	public double[] getFuzzyNoiseAssignments()
	{
		if(!this.initialized) throw new AlgorithmNotInitializedException("Prototypes not initialized.");

		int i;
				
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l 
		double doubleTMP = 0.0d;									// a temporarly variable for multiple perpuses
		double[] fuzzDistances				= new double[this.getClusterCount()];
		int zeroDistanceCount;
		double minDistValue = 0.0d;
		double noiseDistance = 0.0d;
		double[] noiseMemberships = new double[this.getDataCount()];
		
		zeroDistanceCount = 0;
		distanceSum = 0.0d;
		minDistValue = Double.MAX_VALUE;

		for(IndexedDataObject<T> obj:this.data)
		{
			zeroDistanceCount = 0;
			distanceSum = 0.0d;
			minDistValue = Double.MAX_VALUE;
			
			for(i=0; i<this.getClusterCount(); i++)
			{
				doubleTMP = this.metric.distanceSq(obj.x, this.prototypes.get(i).getPosition());
				fuzzDistances[i] = doubleTMP;
				if(minDistValue > doubleTMP) minDistValue = doubleTMP;
			}
			if(minDistValue > this.noiseDistance*this.noiseDistance) minDistValue = this.noiseDistance*this.noiseDistance;
			minDistValue *= this.distanceMultiplierConstant;
			
			for(i=0; i<this.getClusterCount(); i++)
			{
				doubleTMP = fuzzDistances[i] - minDistValue;
				if(doubleTMP <= 0.0d)
				{
					zeroDistanceCount++;
				}
				else
				{
					distanceSum += 1.0d/doubleTMP;
				}
			}
			noiseDistance = 1.0d/(this.noiseDistance*this.noiseDistance - minDistValue);
			distanceSum += noiseDistance; 
	
			// special case handling: if one (or more) prototype sits on top of a data object
			if(zeroDistanceCount>0)
			{
				noiseMemberships[obj.getID()] = 0.0d;
			}
			else
			{
				noiseMemberships[obj.getID()] = noiseDistance/distanceSum;
			}
		}
		return noiseMemberships;
	}


	/* (non-Javadoc)
	 * @see datamining.clustering.FuzzyNoiseClusteringProvider#getFuzzyNoiseAssignmentOf(data.set.IndexedDataObject)
	 */
	@Override
	public double getFuzzyNoiseAssignmentOf(IndexedDataObject<T> obj)
	{
		if(!this.initialized) throw new AlgorithmNotInitializedException("Prototypes not initialized.");

		int i;
				
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l 
		double doubleTMP = 0.0d;									// a temporarly variable for multiple perpuses
		double[] fuzzDistances				= new double[this.getClusterCount()];
		int zeroDistanceCount;
		double minDistValue = 0.0d;
		double noiseDistance = 0.0d;
		double noiseMembership = 0.0d;
		
		zeroDistanceCount = 0;
		distanceSum = 0.0d;
		minDistValue = Double.MAX_VALUE;

		for(i=0; i<this.getClusterCount(); i++)
		{
			doubleTMP = this.metric.distanceSq(obj.x, this.prototypes.get(i).getPosition());
			fuzzDistances[i] = doubleTMP;
			if(minDistValue > doubleTMP) minDistValue = doubleTMP;
		}
		if(minDistValue > this.noiseDistance*this.noiseDistance) minDistValue = this.noiseDistance*this.noiseDistance;
		minDistValue *= this.distanceMultiplierConstant;
		
		for(i=0; i<this.getClusterCount(); i++)
		{
			doubleTMP = fuzzDistances[i] - minDistValue;
			if(doubleTMP <= 0.0d)
			{
				zeroDistanceCount++;
			}
			else
			{
				distanceSum += 1.0d/doubleTMP;
			}
		}
		noiseDistance = 1.0d/(this.noiseDistance*this.noiseDistance - minDistValue);
		distanceSum += noiseDistance; 

		// special case handling: if one (or more) prototype sits on top of a data object
		if(zeroDistanceCount>0)
		{
			noiseMembership = 0.0d;
		}
		else
		{
			noiseMembership = noiseDistance/distanceSum;
		}
		
		return noiseMembership;
	}

	/**
	 * Returns the noise distance.
	 * 
	 * @return The noise distance.
	 */
	public double getNoiseDistance()
	{
		return this.noiseDistance;
	}

	/**
	 * Sets the noise distance.  The range of the parameter is <code>noiseDistance > 0</code>.
	 * 
	 * @param noiseDistance the noiseDistance to set
	 */
	public void setNoiseDistance(double noiseDistance)
	{
		if(noiseDistance <= 0.0d) throw new IllegalArgumentException("The noise distance must be larger than 0. Specified noise distance: " + noiseDistance);
		
		this.noiseDistance = noiseDistance;
	}
}
