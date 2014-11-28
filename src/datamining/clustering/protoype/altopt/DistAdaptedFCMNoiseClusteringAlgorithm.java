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
import java.util.Collection;
import java.util.List;

import data.algebra.Metric;
import data.algebra.VectorSpace;
import data.set.IndexedDataObject;
import data.set.IndexedDataSet;
import datamining.clustering.protoype.AbstractPrototypeClusteringAlgorithm;
import datamining.clustering.protoype.AlgorithmNotInitializedException;
import datamining.clustering.protoype.Centroid;
import datamining.resultProviders.FuzzyNoiseClassificationProvider;
import datamining.resultProviders.FuzzyNoiseClusteringProvider;
import datamining.resultProviders.NoiseDistanceProvider;
import etc.MyMath;
import etc.SimpleStatistics;

/**
 * This is an implementation of the fuzzy c-means clustering algorithm with adopted distence function and additional noise cluster.
 * The squared distance function is reduced by a constant value in order to counter the effect of distance concentration due to a
 * high number of dimensions. This algorithm is particularly designed to counter the effects of the curse of dimensionality
 * with the presence of noise data objects. Similar to {@link RewardingCrispFCMNoiseClusteringAlgorithm}, the objective function
 * is added by an penalty term, but the term is different. But it also removes a value from all distances that appear
 * during membership value calculations. A paper is soon to appear providing more insight into this algorithm. <br> 
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
 * Similar to {@link RewardingCrispFCMNoiseClusteringAlgorithm} and {@link PolynomFCMNoiseClusteringAlgorithm}, it is possible,
 * that the noise cluster can prevent a well defined clustering result if the noise distance is chosen to be too small in the first
 * iterations of the algorithm. It is therefore again advised, to use the algorithm in several runs and to reduce the
 * noise distance from a first very high value to its intentioned value. As for the other algorithms, the presense of the
 * noise cluster does not influences the runtime complexity of the algorithm.<br>
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
public class DistAdaptedFCMNoiseClusteringAlgorithm<T> extends DistAdaptedFCMClusteringAlgorithm<T> implements FuzzyNoiseClusteringProvider<T>, FuzzyNoiseClassificationProvider<T>, NoiseDistanceProvider
{
	/**  */
	private static final long	serialVersionUID	= -993192042228012860L;
	

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
	public DistAdaptedFCMNoiseClusteringAlgorithm(IndexedDataSet<T> data, VectorSpace<T> vs, Metric<T> dist)
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
	public DistAdaptedFCMNoiseClusteringAlgorithm(AbstractPrototypeClusteringAlgorithm<T, Centroid<T>> c, boolean useOnlyActivePrototypes)
	{
		super(c, useOnlyActivePrototypes);

		this.noiseDistance				= 0.1d*Math.sqrt(Double.MAX_VALUE);
		this.degradingNoiseDistance		= this.noiseDistance;
		this.noiseDegrationFactor		= 1.0d;
	}


	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.altopt.DistAdaptedFCMClusteringAlgorithm#algorithmName()
	 */
	@Override
	public String algorithmName()
	{
		return "Distance Adapted Fuzzy c-Means Noise Clustering Algorithm";
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.altopt.DistAdaptedFCMClusteringAlgorithm#apply(int)
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

//		System.out.print(this.algorithmName());
		long timeStart = System.currentTimeMillis();
		
		for(t = 0; t < steps; t++)
		{
//			System.out.print(".");
			
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
				
				doubleTMP = ((this.convergenceMetric!=null)?this.convergenceMetric:this.metric).distanceSq(this.prototypes.get(i).getPosition(), newPrototypePosition.get(i));				
				maxPrototypeMovement = (doubleTMP > maxPrototypeMovement)? doubleTMP : maxPrototypeMovement;				
				
				this.prototypes.get(i).moveTo(newPrototypePosition.get(i));
			}

			if(this.mergePrototypes)
			{
				prototypesMerged = this.mergePrototypes() > 0;
			}
			
			this.iterationComplete();

			this.convergenceHistory.add(Math.sqrt(maxPrototypeMovement));
			if(!prototypesMerged && this.iterationCount >= this.minIterations && maxPrototypeMovement < this.epsilon*this.epsilon) break;
		}		

		if(this.removeEmptyPrototypes)
		{
			this.removePrototypes();
		}

//		System.out.println(" done. [" + (System.currentTimeMillis() - timeStart) + "]");
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.altopt.DistAdaptedFCMClusteringAlgorithm#getObjectiveFunctionValue()
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
		double fuzzNoiseDist						= 0.0d;

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
			fuzzNoiseDist = MyMath.pow(this.noiseDistance*this.noiseDistance, distanceExponent);
			distanceSum += fuzzNoiseDist;

			for(i = 0; i < this.getClusterCount(); i++)
			{
				objectiveFunctionValue +=  MyMath.pow(membershipValues[i], this.fuzzifier) * distancesSq[i];
			}
			doubleTMP = fuzzNoiseDist / distanceSum;			
			objectiveFunctionValue += MyMath.pow(doubleTMP, this.fuzzifier) * this.noiseDistance*this.noiseDistance;
		}

		
		return objectiveFunctionValue;
	}


	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.altopt.DistAdaptedFCMClusteringAlgorithm#getFuzzyAssignmentSums()
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
	 * @see datamining.clustering.protoype.altopt.DistAdaptedFCMClusteringAlgorithm#getAllFuzzyClusterAssignments(java.util.List)
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
			distanceSum += MyMath.pow(this.noiseDistance*this.noiseDistance, distanceExponent);
			
	
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
	 * @see datamining.clustering.protoype.altopt.DistAdaptedFCMClusteringAlgorithm#getFuzzyAssignmentsOf(data.set.IndexedDataObject)
	 */
	@Override
	public double[] getFuzzyAssignmentsOf(IndexedDataObject<T> obj)
	{
		return this.classify(obj.x);
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
		// t: index for iterations	
				
		double distanceExponent = 1.0d / (1.0d - this.fuzzifier);	// to reduce the usage of divisions
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l 
		double doubleTMP = 0.0d;									// a temporarly variable for multiple perpuses
		double[] distancesToData					= new double[this.getDataCount()];
		double[] dynamicDistanceCorrectionValues 	= new double[this.getClusterCount()];

		int zeroDistanceCount;
		
		double fuzzNoiseDist				= 0.0d;
		double[] noiseMembership			= new double[this.getDataCount()];

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
			zeroDistanceCount = 0;
			distanceSum = 0.0d;
			
			for(i = 0; i < this.getClusterCount(); i++)
			{
				if(!this.getPrototypes().get(i).isActivated()) continue;
				
				doubleTMP = this.metric.distanceSq(this.data.get(j).x, this.prototypes.get(i).getPosition()) - dynamicDistanceCorrectionValues[i];
				if(doubleTMP <= 0.0d)
				{
					doubleTMP = 0.0d;
					zeroDistanceCount++;
				}
				else
				{
					doubleTMP = MyMath.pow(doubleTMP, distanceExponent);

					if(Double.isInfinite(doubleTMP))
					{
						doubleTMP = 0.0d;
						zeroDistanceCount++;
					}
					
					distanceSum += doubleTMP;
				}
			}
			
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


	/* (non-Javadoc)
	 * @see datamining.clustering.FuzzyNoiseClusteringProvider#getFuzzyNoiseAssignmentOf(data.set.IndexedDataObject)
	 */
	@Override
	public double getFuzzyNoiseAssignmentOf(IndexedDataObject<T> obj)
	{
		return this.classifyNoise(obj.x);		
	}
	
	
	
	/* (non-Javadoc)
	 * @see datamining.resultProviders.FuzzyNoiseClassificationProvider#classifyNoise(java.lang.Object)
	 */
	@Override
	public double classifyNoise(T x)
	{
		if(!this.initialized) throw new AlgorithmNotInitializedException("Prototypes not initialized.");	
		
		int i; 
		// i: index for clusters
		// j: index for data objects
		// k: index for dimensions, others
		// t: index for iterations	
				
		double distanceExponent = 1.0d / (1.0d - this.fuzzifier);	// to reduce the usage of divisions
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l 
		double doubleTMP = 0.0d;									// a temporarly variable for multiple perpuses
		double[] distancesToData					= new double[this.getDataCount()];
		double[] dynamicDistanceCorrectionValues 	= new double[this.getClusterCount()];

		int zeroDistanceCount;
		
		double fuzzNoiseDist				= 0.0d;

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
			
		zeroDistanceCount = 0;
		distanceSum = 0.0d;
		
		for(i = 0; i < this.getClusterCount(); i++)
		{
			if(!this.getPrototypes().get(i).isActivated()) continue;
			
			doubleTMP = this.metric.distanceSq(x, this.prototypes.get(i).getPosition()) - dynamicDistanceCorrectionValues[i];
			if(doubleTMP <= 0.0d)
			{
				zeroDistanceCount++;
			}
			else
			{
				doubleTMP = MyMath.pow(doubleTMP, distanceExponent);

				if(Double.isInfinite(doubleTMP))
				{
					doubleTMP = 0.0d;
					zeroDistanceCount++;
				}
				
				distanceSum += doubleTMP;
			}
		}
		
		fuzzNoiseDist = MyMath.pow(this.noiseDistance*this.noiseDistance, distanceExponent);
		distanceSum += fuzzNoiseDist;


		// special case handling: if one (or more) prototype sits on top of a data object
		if(zeroDistanceCount>0)
		{
			return 0.0d;
		}
		else
		{
			return fuzzNoiseDist / distanceSum;
		}
	}


	/* (non-Javadoc)
	 * @see datamining.resultProviders.FuzzyNoiseClassificationProvider#classifyNoiseAll(java.util.Collection)
	 */
	@Override
	public double[] classifyNoiseAll(Collection<T> list)
	{
		if(!this.initialized) throw new AlgorithmNotInitializedException("Prototypes not initialized.");	
		
		int i, j; 
		// i: index for clusters
		// j: index for data objects
		// k: index for dimensions, others
		// t: index for iterations	
				
		double distanceExponent = 1.0d / (1.0d - this.fuzzifier);	// to reduce the usage of divisions
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l 
		double doubleTMP = 0.0d;									// a temporarly variable for multiple perpuses
		double[] distancesToData					= new double[this.getDataCount()];
		double[] dynamicDistanceCorrectionValues 	= new double[this.getClusterCount()];

		int zeroDistanceCount;
		
		double fuzzNoiseDist				= 0.0d;
		double[] noiseMembership			= new double[list.size()];

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
		j=0;
		for(T x:list)
		{				
			zeroDistanceCount = 0;
			distanceSum = 0.0d;
			
			for(i = 0; i < this.getClusterCount(); i++)
			{
				if(!this.getPrototypes().get(i).isActivated()) continue;
				
				doubleTMP = this.metric.distanceSq(x, this.prototypes.get(i).getPosition()) - dynamicDistanceCorrectionValues[i];
				if(doubleTMP <= 0.0d)
				{
					doubleTMP = 0.0d;
					zeroDistanceCount++;
				}
				else
				{
					doubleTMP = MyMath.pow(doubleTMP, distanceExponent);

					if(Double.isInfinite(doubleTMP))
					{
						doubleTMP = 0.0d;
						zeroDistanceCount++;
					}
					
					distanceSum += doubleTMP;
				}
			}
			
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
			
			j++;
		}
		
		return noiseMembership;
	}


	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.altopt.DistAdaptedFCMClusteringAlgorithm#classify(java.lang.Object)
	 */
	@Override
	public double[] classify(T x)
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
			
			doubleTMP = this.metric.distanceSq(x, this.prototypes.get(i).getPosition()) - dynamicDistanceCorrectionValues[i];
			if(doubleTMP <= 0.0d)
			{
				doubleTMP = 0.0d;
				zeroDistanceIndexList[zeroDistanceCount] = i;
				zeroDistanceCount++;
			}
			else
			{
				doubleTMP = MyMath.pow(doubleTMP, distanceExponent);

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
		distanceSum += MyMath.pow(this.noiseDistance*this.noiseDistance, distanceExponent);

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


	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.altopt.DistAdaptedFCMClusteringAlgorithm#classifyAll(java.util.Collection)
	 */
	@Override
	public ArrayList<double[]> classifyAll(Collection<T> list)
	{
		if(!this.initialized) throw new AlgorithmNotInitializedException("Prototypes not initialized.");	

		ArrayList<double[]> assignmentList = new ArrayList<double[]>(list.size());
				
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

		for(T x:list)
		{			
			for(i=0; i<this.getClusterCount(); i++) zeroDistanceIndexList[i] = -1;
			zeroDistanceCount = 0;
			distanceSum = 0.0d;
			for(i = 0; i < this.getClusterCount(); i++)
			{
				if(!this.getPrototypes().get(i).isActivated()) continue;
				
				doubleTMP = this.metric.distanceSq(x, this.prototypes.get(i).getPosition()) - dynamicDistanceCorrectionValues[i];
				if(doubleTMP <= 0.0d)
				{
					doubleTMP = 0.0d;
					zeroDistanceIndexList[zeroDistanceCount] = i;
					zeroDistanceCount++;
				}
				else
				{
					doubleTMP = MyMath.pow(doubleTMP, distanceExponent);

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
			distanceSum += MyMath.pow(this.noiseDistance*this.noiseDistance, distanceExponent);
			
	
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
	 * @see datamining.clustering.protoype.altopt.FuzzyCMeansClusteringAlgorithm#isFuzzyAssigned(data.set.IndexedDataObject)
	 */
	@Override
	public boolean isFuzzyAssigned(IndexedDataObject<T> obj)
	{
		return this.initialized && this.getFuzzyNoiseAssignmentOf(obj) < 1.0d;
	}


	/* (non-Javadoc)
	 * @see datamining.resultProviders.NoiseDistanceProvider#getNoiseDistance()
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
	
	
}
