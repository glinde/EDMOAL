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

import data.algebra.Distance;
import data.algebra.VectorSpace;
import data.set.IndexedDataObject;
import data.set.IndexedDataSet;
import datamining.clustering.FuzzyClusteringAlgorithm;
import datamining.clustering.FuzzyNoiseClusteringAlgorithm;
import datamining.clustering.protoype.AlgorithmNotInitializedException;
import etc.MyMath;

/**
 * TODO Class Description
 *
 * Paper: H�ppner, F. & Klawonn, F. Improved fuzzy partitions for fuzzy regression models Int. J. Approx. Reasoning, 2003, 32, 85-102
 * 
 * @author Roland Winkler
 */
public class RewardingCrispFCMNoiseClusteringAlgorithm<T> extends RewardingCrispFCMClusteringAlgorithm<T> implements FuzzyNoiseClusteringAlgorithm<T>, FuzzyClusteringAlgorithm<T>
{	
	/**  */
	private static final long	serialVersionUID	= 6531335434133789423L;
	/**  */
	protected double noiseDistance;
	
	/**
	 * @param data
	 * @param evs
	 */
	public RewardingCrispFCMNoiseClusteringAlgorithm(IndexedDataSet<T> data, VectorSpace<T> vs, Distance<T> dist)
	{
		super(data, vs, dist);
		
		this.noiseDistance = 0.1d*Math.sqrt(Double.MAX_VALUE);
	}


	/**
	 * @param c
	 * @param useOnlyActivePrototypes
	 */
	public RewardingCrispFCMNoiseClusteringAlgorithm(RewardingCrispFCMNoiseClusteringAlgorithm<T> c, boolean useOnlyActivePrototypes)
	{
		super(c, useOnlyActivePrototypes);
		
		this.noiseDistance = c.noiseDistance;
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

				for(i=0; i<this.getClusterCount(); i++)
				{
					doubleTMP = this.dist.distanceSq(this.data.get(j).element, this.prototypes.get(i).getPosition());
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
				
				for(i = 0; i < this.getClusterCount(); i++)
				{

					doubleTMP = MyMath.pow(membershipValues[i], this.fuzzifier);
					membershipSum[i] += doubleTMP;

					this.vs.copy(tmpX, this.data.get(j).element);
					this.vs.mul(tmpX, doubleTMP);
					this.vs.add(newPrototypePosition.get(i), tmpX);
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
				
				doubleTMP = this.dist.distanceSq(this.prototypes.get(i).getPosition(), newPrototypePosition.get(i));
				
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
				doubleTMP = this.dist.distanceSq(this.data.get(j).element, this.prototypes.get(i).getPosition());
				distancesSq[i] = doubleTMP;
				if(minDistValue > doubleTMP) minDistValue = doubleTMP;
			}
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
								
				objectiveFunctionValue += MyMath.pow(doubleTMP, this.fuzzifier) * (distancesSq[i] - minDistValue);
			}
			doubleTMP = fuzzyNoiseDist / distanceSum;
			
			objectiveFunctionValue += MyMath.pow(doubleTMP, this.fuzzifier) * (this.noiseDistance*this.noiseDistance - minDistValue);
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
				doubleTMP = this.dist.distanceSq(this.data.get(j).element, this.prototypes.get(i).getPosition());
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
				doubleTMP = this.dist.distanceSq(this.data.get(j).element, this.prototypes.get(i).getPosition());
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
			doubleTMP = this.dist.distanceSq(obj.element, this.prototypes.get(i).getPosition());
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
	 * @see datamining.clustering.FuzzyNoiseClusteringAlgorithm#getFuzzyNoiseAssignments()
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
				doubleTMP = this.dist.distanceSq(obj.element, this.prototypes.get(i).getPosition());
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
	 * @see datamining.clustering.FuzzyNoiseClusteringAlgorithm#getFuzzyNoiseAssignmentOf(data.set.IndexedDataObject)
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
			doubleTMP = this.dist.distanceSq(obj.element, this.prototypes.get(i).getPosition());
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
	 * @return the noiseDistance
	 */
	public double getNoiseDistance()
	{
		return this.noiseDistance;
	}


	/**
	 * @param noiseDistance the noiseDistance to set
	 */
	public void setNoiseDistance(double noiseDistance)
	{
		this.noiseDistance = noiseDistance;
	}
}
