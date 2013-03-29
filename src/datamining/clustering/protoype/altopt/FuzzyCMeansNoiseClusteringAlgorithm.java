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
 * The fuzzy c-means clustering algorithm, extended by a noise cluster as introduced by Dave.<br>
 * 
 * Paper: Dave, R. N. Characterization and detection of noise in clustering Pattern Recogn. Lett., Elsevier Science Inc., 1991, 12, 657-664
 * 
 * The noise cluster is not added as a normal cluster to the clustering result. It is for the design principles of EDMOAL
 * not wanted. I have tried this before and it makes more problems than it is good for the code, even if it is
 * mathematically elegant. Therefore, the fuzzy result of the algorithm contains only the membership values
 * of the 'normal' clusters. Of course, they do not add to 1 any more, but are less or equal 1. The membership
 * to the noise cluster can be get by the dedicated functions.<br> 
 * 
 * In this particular implementation, the membership matrix is  not stored when the algorithm is applied. That is possible because the membership
 * values of one data object are independent of all other objects, given the position of the prototypes.<br> 
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
 * @author Roland Winkler
 */
public class FuzzyCMeansNoiseClusteringAlgorithm<T> extends FuzzyCMeansClusteringAlgorithm<T> implements FuzzyNoiseClusteringProvider<T>
{
	/**  */
	private static final long	serialVersionUID	= 7172180428079511424L;
	
	/** The noise distance. The noise cluster is equally distant to all data objects and that distance is
	 * specified as the noise distance. The value must be larger than 0. */
	protected double noiseDistance;


	/** It is often advantageous to start with a large noise distance in order to allow the prototypes to find all clouds of data objects.
	 * this distance defines how high the noise distance shall be at the beginning of the iteration process.
	 * The function of degration is dist = <code>noiseDistance</code> + (<code>degradingNoiseDistance</code>-<code>noiseDistance</code>)*e^(-<code>noiseDegrationFactor</code>*t)
	 */
	protected double degradingNoiseDistance;
	
	/** 
	 * Controlls the speed at which the noise distance is degrading from its initial value. 
	 *  */
	protected double noiseDegrationFactor;
	
	/**
	 * Creates a new FuzzyCMeansNoiseClusteringAlgorithm with the specified data set, vector space and metric.
	 * The prototypes are not initialized by this method, it has to be done separately.
	 * The metric must be differentiable w.r.t. <code>y</code> in <code>dist(x, y)<sup>2</sup></code>, and
	 * the directed differential in direction of <code>y</code> must yield <code>d/dy dist(x, y)^2 = 2(y - x)</code>
	 * for the algorithm to be correct.
	 * 
	 * @param data The data set that should be clustered.
	 * @param vs The vector space that is used to calculate the prototype positions.
	 * @param parameterMetric The metric that is used to calculate the distance between data objects and prototypes.
	 */
	public FuzzyCMeansNoiseClusteringAlgorithm(IndexedDataSet<T> data, VectorSpace<T> vs, Metric<T> dist)
	{
		super(data, vs, dist);
		
		this.noiseDistance				= 0.1d*Math.sqrt(Double.MAX_VALUE);
		this.degradingNoiseDistance		= this.noiseDistance;
		this.noiseDegrationFactor		= 1.0d;
	}

	/**
	 * This constructor creates a new FuzzyCMeansNoiseClusteringAlgorithm, taking an existing prototype clustering algorithm.
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
	public FuzzyCMeansNoiseClusteringAlgorithm(AbstractPrototypeClusteringAlgorithm<T, Centroid<T>> c, boolean useOnlyActivePrototypes)
	{
		super(c, useOnlyActivePrototypes);

		this.noiseDistance				= 0.1d*Math.sqrt(Double.MAX_VALUE);
		this.degradingNoiseDistance		= this.noiseDistance;
		this.noiseDegrationFactor		= 1.0d;
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.altopt.FuzzyCMeansClusteringAlgorithm#algorithmName()
	 */
	@Override
	public String algorithmName()
	{
		return "Fuzzy c-Means Noise Clustering Algorithm";
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

		double distanceExponent = 1.0d / (1.0d - this.fuzzifier);	// to reduce the usage of divisions
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l 
		double doubleTMP = 0.0d;									// a temporarly variable for multiple perpuses
		double maxPrototypeMovement = 0.0d;
		ArrayList<T> newPrototypePosition	= new ArrayList<T>(this.getClusterCount());
		for(i=0; i<this.getClusterCount(); i++) newPrototypePosition.add(this.vs.getNewAddNeutralElement()); 
		double[] fuzzDistances				= new double[this.getClusterCount()];
		double[] membershipValues			= new double[this.getClusterCount()];
		double[] membershipSum				= new double[this.getClusterCount()];
		T tmpX								= this.vs.getNewAddNeutralElement();

		int[] zeroDistanceIndexList			= new int[this.getClusterCount()];
		int zeroDistanceCount;

		System.out.print(this.algorithmName());
		long timeStart = System.currentTimeMillis();
		
		for(t = 0; t < steps; t++)
		{
			System.out.print(".");
			
			// reset values
			maxPrototypeMovement = 0.0d;
			
			for(i = 0; i < this.getClusterCount(); i++)
			{
				this.vs.resetToAddNeutralElement(newPrototypePosition.get(i));
				membershipSum[i] = 0.0d;
			}
			
			// update membership values
			for(j=0; j<this.getDataCount(); j++)
			{				
				for(i=0; i<this.getClusterCount(); i++) zeroDistanceIndexList[i] = -1;
				zeroDistanceCount = 0;
				distanceSum = 0.0d;
				for(i = 0; i < this.getClusterCount(); i++)
				{
					doubleTMP = this.metric.distanceSq(this.data.get(j).x, this.prototypes.get(i).getPosition());
					if(doubleTMP <= 0.0d)
					{
						doubleTMP = 0.0d;
						zeroDistanceIndexList[zeroDistanceCount] = i;
						zeroDistanceCount++;
					}
					else
					{
						doubleTMP = MyMath.pow(doubleTMP, distanceExponent);
						fuzzDistances[i] = doubleTMP;
						distanceSum += doubleTMP;
					}
				}
				
				// influence of the noise cluster
				doubleTMP = this.noiseDistance + (this.degradingNoiseDistance - this.noiseDistance) * Math.exp(-this.noiseDegrationFactor*t);
				distanceSum += MyMath.pow(doubleTMP*doubleTMP, distanceExponent);

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
						membershipValues[i] = fuzzDistances[i] / distanceSum;
					}
				}
				
				for(i = 0; i < this.getClusterCount(); i++)
				{

					doubleTMP = MyMath.pow(membershipValues[i], this.fuzzifier);
					membershipSum[i] += doubleTMP;

					this.vs.copy(tmpX, this.data.get(j).x);
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

				doubleTMP = this.metric.distanceSq(this.prototypes.get(i).getPosition(), newPrototypePosition.get(i));
				
				maxPrototypeMovement = (doubleTMP > maxPrototypeMovement)? doubleTMP : maxPrototypeMovement;

				this.prototypes.get(i).moveTo(newPrototypePosition.get(i));
			}

			this.iterationComplete();
			
			if(maxPrototypeMovement < this.epsilon*this.epsilon) break;
		}

		System.out.println(" done. [" + (System.currentTimeMillis() - timeStart) + "]");
	}
		
	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.altopt.FuzzyCMeansClusteringAlgorithm#getObjectiveFunctionValue()
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
		
		double distanceExponent = 1.0d / (1.0d - this.fuzzifier);	// to reduce the usage of divisions
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l 
		double doubleTMP = 0.0d;									// a temporarly variable for multiple perpuses
		double[] fuzzDistances				= new double[this.getClusterCount()];
		double[] distancesSq				= new double[this.getClusterCount()];
		
		double fuzzNoiseDist				= 0.0d;
		boolean zeroDistance = false;
		
						
		for(j=0; j < this.getDataCount(); j++)
		{				
			zeroDistance = false;
			distanceSum = 0.0d;
			for(i=0; i<this.getClusterCount(); i++)
			{
				doubleTMP = this.metric.distanceSq(this.data.get(j).x, this.prototypes.get(i).getPosition());
				if(doubleTMP <= 0.0d)
				{
				}
				else
				{
					distancesSq[i] = doubleTMP;
					doubleTMP = MyMath.pow(doubleTMP, distanceExponent);
					fuzzDistances[i] = doubleTMP;
					distanceSum += doubleTMP;
				}
			}
			if(zeroDistance) continue;
			fuzzNoiseDist = MyMath.pow(this.noiseDistance*this.noiseDistance, distanceExponent);
			distanceSum += fuzzNoiseDist;

			// don't check for distance sum to be zero.. that would just be rediculus!!

			for(i=0; i<this.getClusterCount(); i++)
			{
				doubleTMP = fuzzDistances[i] / distanceSum;
								
				objectiveFunctionValue += MyMath.pow(doubleTMP, this.fuzzifier) * distancesSq[i];
			}
			doubleTMP = fuzzNoiseDist / distanceSum;
			
			objectiveFunctionValue += MyMath.pow(doubleTMP, this.fuzzifier) * this.noiseDistance*this.noiseDistance;
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
		
		double distanceExponent = 1.0d / (1.0d - this.fuzzifier);	// to reduce the usage of divisions
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l 
		double doubleTMP = 0.0d;									// a temporarly variable for multiple perpuses
		double[] fuzzDistances				= new double[this.getClusterCount()];
		double[] membershipValueSums		= new double[this.getClusterCount()];
		int[] zeroDistanceIndexList			= new int[this.getClusterCount()];
		int zeroDistanceCount;
			
						
		for(j=0; j < this.getDataCount(); j++)
		{				
			for(i=0; i<this.getClusterCount(); i++) zeroDistanceIndexList[i] = -1;
			zeroDistanceCount = 0;
			distanceSum = 0.0d;
			for(i=0; i<this.getClusterCount(); i++)
			{
				doubleTMP = this.metric.distanceSq(this.data.get(j).x, this.prototypes.get(i).getPosition());
				if(doubleTMP <= 0.0d)
				{
					doubleTMP = 0.0d;
					zeroDistanceIndexList[zeroDistanceCount] = i;
					zeroDistanceCount++;
				}
				else
				{
					doubleTMP = MyMath.pow(doubleTMP, distanceExponent);
					fuzzDistances[i] = doubleTMP;
					distanceSum += doubleTMP;
				}
			}
			distanceSum += MyMath.pow(this.noiseDistance*this.noiseDistance, distanceExponent);
	
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
	 * @see datamining.clustering.protoype.altopt.FuzzyCMeansClusteringAlgorithm#getAllFuzzyClusterAssignments(java.util.List)
	 */
	@Override
	public List<double[]> getAllFuzzyClusterAssignments(List<double[]>  assignmentList)
	{
		if(!this.initialized) throw new AlgorithmNotInitializedException("Prototypes not initialized.");
		
		if(assignmentList == null) assignmentList = new ArrayList<double[]>(this.getDataCount());
		assignmentList.clear();
				
		int i, j, k;
				
		double distanceExponent = 1.0d / (1.0d - this.fuzzifier);	// to reduce the usage of divisions
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l 
		double doubleTMP = 0.0d;									// a temporarly variable for multiple perpuses
		double[] fuzzDistances				= new double[this.getClusterCount()];
		double[] membershipValues			= new double[this.getClusterCount()];
		int[] zeroDistanceIndexList			= new int[this.getClusterCount()];
		int zeroDistanceCount;
			
						
		for(j=0; j < this.getDataCount(); j++)
		{				
			for(i=0; i<this.getClusterCount(); i++) zeroDistanceIndexList[i] = -1;
			zeroDistanceCount = 0;
			distanceSum = 0.0d;
			for(i=0; i<this.getClusterCount(); i++)
			{
				doubleTMP = this.metric.distanceSq(this.data.get(j).x, this.prototypes.get(i).getPosition());
				if(doubleTMP <= 0.0d)
				{
					doubleTMP = 0.0d;
					zeroDistanceIndexList[zeroDistanceCount] = i;
					zeroDistanceCount++;
				}
				else
				{
					doubleTMP = MyMath.pow(doubleTMP, distanceExponent);
					fuzzDistances[i] = doubleTMP;
					distanceSum += doubleTMP;
				}
			}
			distanceSum += MyMath.pow(this.noiseDistance*this.noiseDistance, distanceExponent);
	
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
	 * @see datamining.clustering.protoype.altopt.FuzzyCMeansClusteringAlgorithm#getFuzzyAssignmentsOf(data.set.IndexedDataObject)
	 */
	@Override
	public double[] getFuzzyAssignmentsOf(IndexedDataObject<T> obj)
	{
		if(!this.initialized) throw new AlgorithmNotInitializedException("Prototypes not initialized.");

		int i, k;
				
		double distanceExponent = 1.0d / (1.0d - this.fuzzifier);	// to reduce the usage of divisions
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l 
		double doubleTMP = 0.0d;									// a temporarly variable for multiple perpuses
		double[] distances					= new double[this.getClusterCount()];
		double[] membershipValues			= new double[this.getClusterCount()];
		int[] zeroDistanceIndexList			= new int[this.getClusterCount()];
		int zeroDistanceCount;
		
		
		for(i=0; i<this.getClusterCount(); i++) zeroDistanceIndexList[i] = -1;
		zeroDistanceCount = 0;
		distanceSum = 0.0d;
		for(i=0; i<this.getClusterCount(); i++)
		{
			doubleTMP = this.metric.distanceSq(obj.x, this.prototypes.get(i).getPosition());
			if(doubleTMP <= 0.0d)
			{
				doubleTMP = 0.0d;
				zeroDistanceIndexList[zeroDistanceCount] = i;
				zeroDistanceCount++;
			}
			else
			{
				doubleTMP = MyMath.pow(doubleTMP, distanceExponent);
				distances[i] = doubleTMP;
				distanceSum += doubleTMP;
			}
		}
		distanceSum += MyMath.pow(this.noiseDistance*this.noiseDistance, distanceExponent);

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
				doubleTMP = distances[i] / distanceSum;
				membershipValues[i] = doubleTMP;
			}
		}
		
		return membershipValues;
	}


	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.altopt.FuzzyCMeansClusteringAlgorithm#isFuzzyAssigned(data.set.IndexedDataObject)
	 */
	@Override
	public boolean isFuzzyAssigned(IndexedDataObject<T> obj)
	{
		return this.initialized && this.getFuzzyNoiseAssignmentOf(obj) < 1.0d;
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.FuzzyNoiseClusteringProvider#getFuzzyNoiseAssignmentOf(data.set.IndexedDataObject)
	 */
	@Override
	public double getFuzzyNoiseAssignmentOf(IndexedDataObject<T> obj)
	{
		if(!this.initialized) throw new AlgorithmNotInitializedException("Prototypes not initialized.");
		
		int i; 
		// i: index for clusters
		// j: index for data objects
		// k: index for dimensions, others
		
		
		double distanceExponent = 1.0d / (1.0d - this.fuzzifier);	// to reduce the usage of divisions
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l 
		double doubleTMP = 0.0d;									// a temporarly variable for multiple perpuses
		
		double fuzzNoiseDist				= 0.0d;
		double noiseMembership				= 0.0d;
		
		int[] zeroDistanceIndexList			= new int[this.getClusterCount()];
		int zeroDistanceCount;
		
		for(i=0; i<this.getClusterCount(); i++) zeroDistanceIndexList[i] = -1;
		zeroDistanceCount = 0;
		distanceSum = 0.0d;
		for(i=0; i<this.getClusterCount(); i++)
		{
			doubleTMP = this.metric.distanceSq(obj.x, this.prototypes.get(i).getPosition());
			if(doubleTMP <= 0.0d)
			{
				doubleTMP = 0.0d;
				zeroDistanceIndexList[zeroDistanceCount] = i;
				zeroDistanceCount++;
			}
			else
			{
				doubleTMP = MyMath.pow(doubleTMP, distanceExponent);
				distanceSum += doubleTMP;
			}
		}
		// influence of the noise cluster
		fuzzNoiseDist = MyMath.pow(this.noiseDistance*this.noiseDistance, distanceExponent);
		distanceSum += fuzzNoiseDist;

		// special case handling: if one (or more) prototype sits on top of a data object
		if(zeroDistanceCount>0)
		{
			noiseMembership = 0.0d;
		}
		else
		{
			noiseMembership = fuzzNoiseDist / distanceSum;
		}
	
		return noiseMembership;
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.FuzzyNoiseClusteringProvider#getFuzzyNoiseAssignments()
	 */
	@Override
	public double[] getFuzzyNoiseAssignments()
	{
		if(!this.initialized) throw new AlgorithmNotInitializedException("Prototypes not initialized.");
		
		int i, j; 
		// i: index for clusters
		// j: index for data objects
		// k: index for dimensions, others
		
		
		double distanceExponent = 1.0d / (1.0d - this.fuzzifier);	// to reduce the usage of divisions
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l 
		double doubleTMP = 0.0d;									// a temporarly variable for multiple perpuses
		double[] fuzzDistances				= new double[this.getClusterCount()];
		
		double fuzzNoiseDist				= 0.0d;
		double[] noiseMembership			= new double[this.getDataCount()];
		
		int[] zeroDistanceIndexList			= new int[this.getClusterCount()];
		int zeroDistanceCount;
		
		for(j=0; j<this.getDataCount(); j++)
		{
			for(i=0; i<this.getClusterCount(); i++) zeroDistanceIndexList[i] = -1;
			zeroDistanceCount = 0;
			distanceSum = 0.0d;
			for(i=0; i<this.getClusterCount(); i++)
			{
				doubleTMP = this.metric.distanceSq(this.data.get(j).x, this.prototypes.get(i).getPosition());
				if(doubleTMP <= 0.0d)
				{
					doubleTMP = 0.0d;
					zeroDistanceIndexList[zeroDistanceCount] = i;
					zeroDistanceCount++;
				}
				else
				{
					doubleTMP = MyMath.pow(doubleTMP, distanceExponent);
					fuzzDistances[i] = doubleTMP;
					distanceSum += doubleTMP;
				}
			}
			// influence of the noise cluster
			fuzzNoiseDist = MyMath.pow(this.noiseDistance*this.noiseDistance, distanceExponent);
			distanceSum += fuzzNoiseDist;
	
			// special case handling: if one (or more) prototype sits on top of a data object
			if(zeroDistanceCount>0)
			{
				noiseMembership[j] = 0.0d;
			}
			else
			{
				noiseMembership[j] = fuzzNoiseDist / distanceSum;
			}
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
		
		if(this.degradingNoiseDistance < this.noiseDistance) this.degradingNoiseDistance = this.noiseDistance;
	}


	/**
	 * @return the degradingNoiseDistance
	 */
	public double getDegradingNoiseDistance()
	{
		return this.degradingNoiseDistance;
	}


	/**
	 * @param degradingNoiseDistance the degradingNoiseDistance to set
	 */
	public void setDegradingNoiseDistance(double degradingNoiseDistance)
	{
		if(degradingNoiseDistance < this.noiseDistance) this.degradingNoiseDistance = this.noiseDistance;
		else this.degradingNoiseDistance = degradingNoiseDistance;
	}


	/**
	 * @return the noiseDegrationFactor
	 */
	public double getNoiseDegrationFactor()
	{
		return this.noiseDegrationFactor;
	}


	/**
	 * @param noiseDegrationFactor the noiseDegrationFactor to set
	 */
	public void setNoiseDegrationFactor(double noiseDegrationFactor)
	{
		this.noiseDegrationFactor = (noiseDegrationFactor>=0.0d)? noiseDegrationFactor:0.0d;
	}
	
	

	/**
	 * @TODO: remove
	 */
	public void clone(FuzzyCMeansNoiseClusteringAlgorithm<T> clone)
	{
		super.clone(clone);
		
		clone.noiseDistance = this.noiseDistance;
	}
	
	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.altopt.FuzzyCMeansClusteringAlgorithm#clone()
	 */
	@Override
	public FuzzyCMeansNoiseClusteringAlgorithm<T> clone()
	{
		FuzzyCMeansNoiseClusteringAlgorithm<T> clone = new FuzzyCMeansNoiseClusteringAlgorithm<T>(this.data, this.vs, this.metric);
		this.clone(clone);
		return clone;
	}
}
