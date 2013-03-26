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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import data.algebra.Metric;
import data.algebra.VectorSpace;
import data.set.IndexedDataObject;
import data.set.IndexedDataSet;
import data.structures.balltree.BallTree;
import datamining.clustering.protoype.AbstractPrototypeClusteringAlgorithm;
import datamining.clustering.protoype.AlgorithmNotInitializedException;
import datamining.clustering.protoype.Centroid;
import etc.MyMath;
import etc.SimpleStatistics;


/**
 * The fuzzy c-means clustering algorithm with adopted distence function is an extension of FCM.
 * The squared distance function is reduced by a constant value in order to counter the effect of distance concentration due to a
 * high number of dimensions. This algorithm is particularly designed to counter the effects of the curse of dimensionality.
 * Similar to {@link RewardingCrispFCMClusteringAlgorithm}, the objective function is added by an penalty term, but the
 * term is different. But it also removes a value from all distances that appear during membership value calculations.
 * A paper is soon to appear providing more insight into this algorithm. <br> 
 * 
 * Paper: to appear<br>
 * 
 * The curse of dimensionality effects the distances w.r.t. to a reference point. Take the position of a prototype, then
 * all data objects are roughly at the same distance. Let <code>d<sub>min</sub></code> be the distance to the
 * closest data object. Then removing this distance <code>d<sub>rem</sub> = d<sub>min</sub></code> from all distances
 * is not enough to counter the distance concentration effects. Therefore, in this algorithm, <code>d<sub>rem</sub></code>
 * is chosen to be larger than <code>d<sub>min</sub></code>. To maximize the effect countering the curse of dimensionality
 * and to minimize the effect of negative distances, <code>d<sub>rem</sub></code> depends on the mean of distances
 * w.r.t. a prototype and the variance of these distances. The <code>distanceCorrectionParameter</code> influences the
 * distance calculation in the following way: <code>d<sup>2</sup><sub>new</sub> = d<sup>2</sup> - d<sup>2</sup><sub>rem</sub></code> and
 * d<sub>rem</sub></code> = mean(d<sub>i</sub>) - distanceCorrectionParameter * sqrt(var(d<sub>i</sub>))</code>.
 * For membership value calculations, the Karush-Kuhn-Tucker conditions are met by setting negative distance values to
 * 0.<br>
 * 
 * The constant removal of distances leads to the situation that close prototypes tend to move into the same position. That
 * effect can be used to remove unnecessary prototypes be setting them inactive. If the number of clusters is not known,
 * it is advisable to start the algorithm with an overestimated number of prototypes and to activate prototype merging and
 * prototype removal for prototypes that ended up at some random noise data objects.<br>
 * 
 * The complexity of the algorithm is not increased due to the parameter calculation, still the calculation adds a small fraction of
 * additional computation time, so even if the complexity is not higher than for {@link FuzzyCMeansClusteringAlgorithm},
 * the calculation time is higher. Also the prototype merging ability increases the runtime complexity by O(c^2) for a small
 * number of prototypes, or with high overhead by O(c*log(c)), if the number of prototypes c is high enough.<br>
 * 
 * In this particular implementation, the membership matrix is not stored when the algorithm is applied. That is possible because the membership
 * values of one data object are independent of all other objects, given the position of the prototypes.<br> 
 * 
 * The runtime complexity of this algorithm is in O(t*n*c+t*c^2),
 * with t being the number of iterations, n being the number of data objects and c being the number of clusters.
 * This is, neglecting the runtime complexity of distance calculations and algebraic operations in the vector space.
 * The full complexity would be in O(t*n*c*(O(dist)+O(add)+O(mul))+t*c^2*O(dist)) where O(dist) is the complexity of
 * calculating the distance between a data object and a prototype, O(add) is the complexity of calculating the
 * vector addition of two types <code>T</code> and O(mul) is the complexity of scalar multiplication of type <code>T</code>. <br>
 *  
 * The memory consumption of this algorithm is in O(t+n+c).
 *
 * @author Roland Winkler
 */
public class DistAdaptedFCMClusteringAlgorithm<T> extends FuzzyCMeansClusteringAlgorithm<T>
{
	/**  */
	private static final long	serialVersionUID	= -3814440444229284948L;

	/** Indicates whether the prototypes are merged or not. */
	protected boolean mergePrototypes;
	
	/** If two or more prototypes are closer together than this distance, one of them is deactivated (they merged). */
	protected double mergingDistance;
	
	/** Indicates whether empty prototypes are removed (deactivated) at the end of the clustering process. */
	protected boolean removeEmptyPrototypes;

	/** If a cluster has a membership value sum that os less than this value, the prototype is removed (deactivated) at
	 * the end of the clustering process. */
	protected double minMemembershipValueSum;
		
	/** The distance correction parameter. */
	protected double distanceCorrectionParameter;

	/**
	 * Creates a new DistAdaptedFCMClusteringAlgorithm with the specified data set, vector space and metric.
	 * The prototypes are not initialized by this method, it has to be done separately.
	 * The metric must be differentiable w.r.t. <code>y</code> in <code>dist(x, y)<sup>2</sup></code>, and
	 * the directed differential in direction of <code>y</code> must yield <code>d/dy dist(x, y)^2 = 2(y - x)</code>
	 * for the algorithm to be correct.
	 * 
	 * @param data The data set that should be clustered.
	 * @param vs The vector space that is used to calculate the prototype positions.
	 * @param parameterMetric The metric that is used to calculate the distance between data objects and prototypes.
	 */
	public DistAdaptedFCMClusteringAlgorithm(IndexedDataSet<T> data, VectorSpace<T> vs, Metric<T> dist)
	{
		super(data, vs, dist);
		
		this.mergePrototypes = false;
		this.mergingDistance = 0.0d;
		
		this.removeEmptyPrototypes = false;
		this.minMemembershipValueSum = 0.0d;
		
		this.distanceCorrectionParameter = 3.0d;
	}

	/**
	 * This constructor creates a new DistAdaptedFCMClusteringAlgorithm, taking an existing prototype clustering algorithm.
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
	public DistAdaptedFCMClusteringAlgorithm(AbstractPrototypeClusteringAlgorithm<T, Centroid<T>> c, boolean useOnlyActivePrototypes)
	{
		super(c, useOnlyActivePrototypes);

		this.mergePrototypes = false;
		this.mergingDistance = 0.0d;
		
		this.removeEmptyPrototypes = false;
		this.minMemembershipValueSum = 0.0d;
		
		this.distanceCorrectionParameter = 3.0d;
	}


	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.altopt.FuzzyCMeansClusteringAlgorithm#algorithmName()
	 */
	@Override
	public String algorithmName()
	{
		return "Distance Adapted Fuzzy c-Means Clustering Algorithm";
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
		ArrayList<T> newPrototypePosition			= new ArrayList<T>(this.getClusterCount());
		for(i=0; i<this.getClusterCount(); i++) newPrototypePosition.add(this.vs.getNewAddNeutralElement()); 
		double[] fuzzDistances						= new double[this.getClusterCount()];
		double[] membershipValues					= new double[this.getClusterCount()];
		double[] membershipSum						= new double[this.getClusterCount()];
		T tmpX										= this.vs.getNewAddNeutralElement();
		double[] distancesToData					= new double[this.getDataCount()];
		double[] dynamicDistanceCorrectionValues 	= new double[this.getClusterCount()];
		
		boolean prototypesMerged					= false;
		
		int[] zeroDistanceIndexList					= new int[this.getClusterCount()];
		int zeroDistanceCount;

		System.out.print(this.algorithmName());
		long timeStart = System.currentTimeMillis();
		
		for(t = 0; t < steps; t++)
		{
			System.out.print(".");
			
			// reset values
			maxPrototypeMovement = 0.0d;
			prototypesMerged = false;
			
			for(i = 0; i < this.getClusterCount(); i++)
			{
				this.vs.resetToAddNeutralElement(newPrototypePosition.get(i));
				membershipSum[i] = 0.0d;
			}
			

			// calculate dynamic distance correction values 
			for(i = 0; i < this.getClusterCount(); i++)
			{
				if(!this.getPrototypes().get(i).isActivated()) continue;
				
				distancesToData = this.calculateDataObjectDistancePlot(distancesToData, this.data, this.getPrototypes().get(i).getPosition(), false);
				doubleTMP = SimpleStatistics.mean(distancesToData);
				dynamicDistanceCorrectionValues[i] = doubleTMP - this.distanceCorrectionParameter*Math.sqrt(SimpleStatistics.variance(distancesToData, doubleTMP));
				if(dynamicDistanceCorrectionValues[i] <= 0.0d) dynamicDistanceCorrectionValues[i] = 0.0d;
				dynamicDistanceCorrectionValues[i] *= dynamicDistanceCorrectionValues[i];
			}
			
			// update membership values
			for(j = 0; j < this.getDataCount(); j++)
			{				
				for(i=0; i<this.getClusterCount(); i++) zeroDistanceIndexList[i] = -1;
				zeroDistanceCount = 0;
				distanceSum = 0.0d;
				for(i = 0; i < this.getClusterCount(); i++)
				{
					if(!this.getPrototypes().get(i).isActivated()) continue;
					
					doubleTMP = this.metric.distanceSq(this.data.get(j).x, this.prototypes.get(i).getPosition()) - dynamicDistanceCorrectionValues[i];
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

			if(this.mergePrototypes)
			{
				prototypesMerged = this.mergePrototypes() > 0;
			}
			
			
			this.iterationComplete();
			
			if(!prototypesMerged && maxPrototypeMovement < this.epsilon*this.epsilon) break;
		}		

		if(this.removeEmptyPrototypes)
		{
			this.removePrototypes();
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
		
		int i, j, k; 
		// i: index for clusters
		// j: index for data objects
		// k: index for dimensions, others
		// t: index for iterations	
				
		double distanceExponent = 1.0d / (1.0d - this.fuzzifier);	// to reduce the usage of divisions
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l 
		double doubleTMP = 0.0d;									// a temporarly variable for multiple perpuses
		double[] fuzzDistances						= new double[this.getClusterCount()];
		double[] distancesSq						= new double[this.getClusterCount()];
		double[] distancesToData					= new double[this.getDataCount()];
		double[] dynamicDistanceCorrectionValues 	= new double[this.getClusterCount()];
		double objectiveFunctionValue				= 0.0d;
		double[] membershipValues					= new double[this.getClusterCount()];

		int[] zeroDistanceIndexList					= new int[this.getClusterCount()];
		int zeroDistanceCount;

		// calculate dynamic distance correction values 
		for(i = 0; i < this.getClusterCount(); i++)
		{
			if(!this.getPrototypes().get(i).isActivated()) continue;
			
			distancesToData = this.calculateDataObjectDistancePlot(distancesToData, this.data, this.getPrototypes().get(i).getPosition(), false);
			doubleTMP = SimpleStatistics.mean(distancesToData);
			dynamicDistanceCorrectionValues[i] = doubleTMP - this.distanceCorrectionParameter*Math.sqrt(SimpleStatistics.variance(distancesToData, doubleTMP));
			if(dynamicDistanceCorrectionValues[i] <= 0.0d) dynamicDistanceCorrectionValues[i] = 0.0d;
			dynamicDistanceCorrectionValues[i] *= dynamicDistanceCorrectionValues[i];
		}
		
		// update membership values
		for(j = 0; j < this.getDataCount(); j++)
		{				
			for(i=0; i<this.getClusterCount(); i++) zeroDistanceIndexList[i] = -1;
			zeroDistanceCount = 0;
			distanceSum = 0.0d;
			for(i = 0; i < this.getClusterCount(); i++)
			{
				if(!this.getPrototypes().get(i).isActivated()) continue;
				
				doubleTMP = this.metric.distanceSq(this.data.get(j).x, this.prototypes.get(i).getPosition()) - dynamicDistanceCorrectionValues[i];
				distancesSq[i] = doubleTMP;
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
				objectiveFunctionValue +=  MyMath.pow(membershipValues[i], this.fuzzifier) * distancesSq[i];
			}
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
		// i: index for clusters
		// j: index for data objects
		// k: index for dimensions, others
		// t: index for iterations	
				
		double distanceExponent = 1.0d / (1.0d - this.fuzzifier);	// to reduce the usage of divisions
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l 
		double doubleTMP = 0.0d;									// a temporarly variable for multiple perpuses
		double[] fuzzDistances						= new double[this.getClusterCount()];
		double[] distancesToData					= new double[this.getDataCount()];
		double[] dynamicDistanceCorrectionValues 	= new double[this.getClusterCount()];
		double[] membershipValueSums				= new double[this.getClusterCount()];
		
		int[] zeroDistanceIndexList					= new int[this.getClusterCount()];
		int zeroDistanceCount;
	
		// calculate dynamic distance correction values 
		for(i = 0; i < this.getClusterCount(); i++)
		{
			if(!this.getPrototypes().get(i).isActivated()) continue;
			
			distancesToData = this.calculateDataObjectDistancePlot(distancesToData, this.data, this.getPrototypes().get(i).getPosition(), false);
			doubleTMP = SimpleStatistics.mean(distancesToData);
			dynamicDistanceCorrectionValues[i] = doubleTMP - this.distanceCorrectionParameter*Math.sqrt(SimpleStatistics.variance(distancesToData, doubleTMP));
			if(dynamicDistanceCorrectionValues[i] <= 0.0d) dynamicDistanceCorrectionValues[i] = 0.0d;
			dynamicDistanceCorrectionValues[i] *= dynamicDistanceCorrectionValues[i];
		}

		for(j=0; j < this.getDataCount(); j++)
		{			
			for(i=0; i<this.getClusterCount(); i++) zeroDistanceIndexList[i] = -1;
			zeroDistanceCount = 0;
			distanceSum = 0.0d;
			for(i = 0; i < this.getClusterCount(); i++)
			{
				if(!this.getPrototypes().get(i).isActivated()) continue;
				
				doubleTMP = this.metric.distanceSq(this.data.get(j).x, this.prototypes.get(i).getPosition()) - dynamicDistanceCorrectionValues[i];
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
				for(i = 0; i < this.getClusterCount(); i++)
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
	public List<double[]> getAllFuzzyClusterAssignments(List<double[]> assignmentList)
	{
		if(!this.initialized) throw new AlgorithmNotInitializedException("Prototypes not initialized.");	

		if(assignmentList == null) assignmentList = new ArrayList<double[]>(this.getDataCount());
		assignmentList.clear();
				
		int i, j, k; 
		// i: index for clusters
		// j: index for data objects
		// k: index for dimensions, others
		// t: index for iterations	
				
		double distanceExponent = 1.0d / (1.0d - this.fuzzifier);	// to reduce the usage of divisions
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l 
		double doubleTMP = 0.0d;									// a temporarly variable for multiple perpuses
		double[] fuzzDistances						= new double[this.getClusterCount()];
		double[] distancesToData					= new double[this.getDataCount()];
		double[] dynamicDistanceCorrectionValues 	= new double[this.getClusterCount()];
		double[] membershipValues					= new double[this.getClusterCount()];
		
		int[] zeroDistanceIndexList					= new int[this.getClusterCount()];
		int zeroDistanceCount;
	
		// calculate dynamic distance correction values 
		for(i = 0; i < this.getClusterCount(); i++)
		{
			if(!this.getPrototypes().get(i).isActivated()) continue;
			
			distancesToData = this.calculateDataObjectDistancePlot(distancesToData, this.data, this.getPrototypes().get(i).getPosition(), false);
			doubleTMP = SimpleStatistics.mean(distancesToData);
			dynamicDistanceCorrectionValues[i] = doubleTMP - this.distanceCorrectionParameter*Math.sqrt(SimpleStatistics.variance(distancesToData, doubleTMP));
			if(dynamicDistanceCorrectionValues[i] <= 0.0d) dynamicDistanceCorrectionValues[i] = 0.0d;
			dynamicDistanceCorrectionValues[i] *= dynamicDistanceCorrectionValues[i];
		}

		for(j=0; j < this.getDataCount(); j++)
		{			
			for(i=0; i<this.getClusterCount(); i++) zeroDistanceIndexList[i] = -1;
			zeroDistanceCount = 0;
			distanceSum = 0.0d;
			for(i = 0; i < this.getClusterCount(); i++)
			{
				if(!this.getPrototypes().get(i).isActivated()) continue;
				
				doubleTMP = this.metric.distanceSq(this.data.get(j).x, this.prototypes.get(i).getPosition()) - dynamicDistanceCorrectionValues[i];
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
		// i: index for clusters
		// j: index for data objects
		// k: index for dimensions, others
		// t: index for iterations	
				
		double distanceExponent = 1.0d / (1.0d - this.fuzzifier);	// to reduce the usage of divisions
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l 
		double doubleTMP = 0.0d;									// a temporarly variable for multiple perpuses
		double[] fuzzDistances						= new double[this.getClusterCount()];
		double[] distancesToData					= new double[this.getDataCount()];
		double[] dynamicDistanceCorrectionValues 	= new double[this.getClusterCount()];
		double[] membershipValues					= new double[this.getClusterCount()];
		
		int[] zeroDistanceIndexList					= new int[this.getClusterCount()];
		int zeroDistanceCount;

		// calculate dynamic distance correction values 
		for(i = 0; i < this.getClusterCount(); i++)
		{
			if(!this.getPrototypes().get(i).isActivated()) continue;
			
			distancesToData = this.calculateDataObjectDistancePlot(distancesToData, this.data, this.getPrototypes().get(i).getPosition(), false);
			doubleTMP = SimpleStatistics.mean(distancesToData);
			dynamicDistanceCorrectionValues[i] = doubleTMP - this.distanceCorrectionParameter*Math.sqrt(SimpleStatistics.variance(distancesToData, doubleTMP));
			if(dynamicDistanceCorrectionValues[i] <= 0.0d) dynamicDistanceCorrectionValues[i] = 0.0d;
			dynamicDistanceCorrectionValues[i] *= dynamicDistanceCorrectionValues[i];
		}
				
		for(i=0; i<this.getClusterCount(); i++) zeroDistanceIndexList[i] = -1;
		zeroDistanceCount = 0;
		distanceSum = 0.0d;
		for(i = 0; i < this.getClusterCount(); i++)
		{
			if(!this.getPrototypes().get(i).isActivated()) continue;
			
			doubleTMP = this.metric.distanceSq(obj.x, this.prototypes.get(i).getPosition()) - dynamicDistanceCorrectionValues[i];
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
		
		return membershipValues;
	}
	

	/**
	 * Merges prototypes that are close together than {@link #mergingDistance} by deactivating one of them.
	 * 
	 * @return The number of prototypes that have been deactivated.
	 */
	protected int mergePrototypes()
	{
		int i=0, j=0;
		int mergedPrototypes = 0;
		IndexedDataSet<T> tmpPrototypeDataSet;
		BallTree<T> ballTree;
		ArrayList<IndexedDataObject<T>> closePrototypes;
		ArrayList<Centroid<T>> activePrototypes = this.getActivePrototypes();
		
		// TODO: optimize the number. At some point, memory allocation overhead is faster than
		// the quadratic pairwise comparison of all prototypes.
		// Where is that point? 100 prototypes is just a wild guess!
		
		if(activePrototypes.size() < 100) // O(c^2) but with almost no overhead
		{
			for(i=0; i<this.getClusterCount(); i++)
			{
				if(!this.getPrototypes().get(i).isActivated()) continue;
				
				for(j=i+1; j<this.getClusterCount(); j++)
				{
					if(!this.getPrototypes().get(j).isActivated()) continue;
					
					if(this.metric.distanceSq(this.getPrototypes().get(i).getPosition(), this.getPrototypes().get(j).getPosition()) < this.mergingDistance*this.mergingDistance)
					{
						this.getPrototypes().get(j).setActivated(false);
						mergedPrototypes++;
						break;
					}
				}
			}
		}
		else // O(c*log(c)) but with massive overhead! 
		{
			tmpPrototypeDataSet = new IndexedDataSet<T>();
			for(Centroid<T> p:this.prototypes) tmpPrototypeDataSet.add(new IndexedDataObject<T>(p.getPosition()));
			tmpPrototypeDataSet.seal();
			ballTree = new BallTree<T>(tmpPrototypeDataSet, this.metric);
			closePrototypes = new ArrayList<IndexedDataObject<T>>(activePrototypes.size());
			
			for(Centroid<T> p : activePrototypes)
			{
				if(!p.isActivated()) continue;
				
				closePrototypes.clear();
				ballTree.sphereQuery(closePrototypes, p.getPosition(), this.mergingDistance);
				for(IndexedDataObject<T> cp : closePrototypes)
				{
					if(!activePrototypes.get(cp.getID()).isActivated()) continue;
					
					activePrototypes.get(cp.getID()).setActivated(false);
					mergedPrototypes++;
				}
			}
		}
		
		return mergedPrototypes;
	}

	/**
	 * Removes (deactivates) all prototypes that have a membership value sum of less than the specified
	 * minimal value that is specified in {@link #minMemembershipValueSum}.
	 * 
	 * @return The number of prototypes that have been deactivated.
	 */
	protected int removePrototypes()
	{
		int i;
		int removedPrototypes = 0;
		double[] fuzzymembershipSums = this.getFuzzyAssignmentSums();
		
		// deactivate 'empty' prototypes
		for(i=0; i<this.getClusterCount(); i++)
		{
			if(!this.getPrototypes().get(i).isActivated()) continue;
						
			if(fuzzymembershipSums[i] < this.minMemembershipValueSum)
			{
				this.getPrototypes().get(i).setActivated(false);
				removedPrototypes++;
			}
		}
		
		return removedPrototypes;
	}

	/**
	 * @param result
	 * @param list
	 * @param reference
	 * @param sort
	 * @return
	 */
	public double[] calculateDataObjectDistancePlot(double[] result, Collection<IndexedDataObject<T>> list, T reference, boolean sort)
	{
		if(result == null || result.length < list.size()) result = new double[list.size()];
		int i=0;
		
		for(IndexedDataObject<T> p:list)
		{
			result[i] = this.metric.distance(p.x, reference);
			i++;
		}
		
		if(sort) Arrays.sort(result);
		
		return result;
	}

	/**
	 * Indicates if prototypes that are close together are merged.
	 * 
	 * @return Whether or not prototypes that are close together are merged.
	 */
	public boolean isMergePrototypes()
	{
		return this.mergePrototypes;
	}

	/**
	 * Sets the switch to merge prototypes.
	 * 
	 * @param Whether or not prototypes that are close together are merged.
	 */
	public void setMergePrototypes(boolean mergePrototypes)
	{
		this.mergePrototypes = mergePrototypes;
	}

	/**
	 * Returns the distance at which prototypes are merged.
	 * 
	 * @return the distance at which prototypes are merged.
	 */
	public double getMergingDistance()
	{
		return this.mergingDistance;
	}

	/**
	 * Sets the distance at which prototypes are merged. The value must be larger than 0.
	 * 
	 * @param mergingDistance the distance at which prototypes are merged.
	 */
	public void setMergingDistance(double mergingDistance)
	{
		if(mergingDistance < 0.0d) throw new IllegalArgumentException("The mergin distance must be larger than 0. Specified merging distance: " + mergingDistance);
		
		this.mergingDistance = mergingDistance;
	}

	/**
	 * Indicates whether or not empty prototypes (prototypes with small membership value sum) are deactivated.
	 * 
	 * @return whether or not empty prototypes (prototypes with small membership value sum) are deactivated.
	 */
	public boolean isRemoveEmptyPrototypes()
	{
		return this.removeEmptyPrototypes;
	}

	/**
	 * Sets the switch to deactivate empty prototypes.
	 * 
	 * @param removeEmptyPrototypes the switch to deactivate empty prototypes.
	 */
	public void setRemoveEmptyPrototypes(boolean removeEmptyPrototypes)
	{
		this.removeEmptyPrototypes = removeEmptyPrototypes;
	}

	/**
	 * Returns the value that defines whether or not a prototype is regarded as empty.
	 * 
	 * @return the value that defines whether or not a prototype is regarded as empty.
	 */
	public double getMinMemembershipValueSum()
	{
		return this.minMemembershipValueSum;
	}

	/**
	 * Sets the value that defines whether or not a prototype is regarded as empty. The value must be larger than 0.
	 * 
	 * @param minMemembershipValueSum the value that defines whether or not a prototype is regarded as empty.
	 */
	public void setMinMemembershipValueSum(double minMemembershipValueSum)
	{
		if(minMemembershipValueSum < 0.0d) throw new IllegalArgumentException("The minimal membership value sum must be larger than 0. Specified distance concentration parameter: " + minMemembershipValueSum);
		
		this.minMemembershipValueSum = minMemembershipValueSum;
	}

	/**
	 * Returns the distance concentration parameter. The <code>distanceCorrectionParameter</code> influences the
	 * distance calculation in the following way: <code>d<sup>2</sup><sub>new</sub> = d<sup>2</sup> - d<sup>2</sup><sub>rem</sub></code> and
	 * d<sub>rem</sub></code> = mean(d<sub>i</sub>) - distanceCorrectionParameter * sqrt(var(d<sub>i</sub>))</code>.
	 * 
	 * @return the distance concentration parameter.
	 */
	public double getDistanceCorrectionParameter()
	{
		return this.distanceCorrectionParameter;
	}

	/**
	 * Sets the distance concentration parameter. The <code>distanceCorrectionParameter</code> influences the
	 * distance calculation in the following way: <code>d<sup>2</sup><sub>new</sub> = d<sup>2</sup> - d<sup>2</sup><sub>rem</sub></code> and
	 * d<sub>rem</sub></code> = mean(d<sub>i</sub>) - distanceCorrectionParameter * sqrt(var(d<sub>i</sub>))</code>.<br>
	 * 
	 * The distance concentration parameter value must be larger than 0.
	 * 
	 * @param distanceCorrectionParameter the distance concentration parameter to set.
	 */
	public void setDistanceCorrectionParameter(double distanceCorrectionParameter)
	{
		if(distanceCorrectionParameter < 0.0d) throw new IllegalArgumentException("The distance concentration parameter must be larger than 0. Specified distance concentration parameter: " + distanceCorrectionParameter);
				
		this.distanceCorrectionParameter = distanceCorrectionParameter;
	}
}
