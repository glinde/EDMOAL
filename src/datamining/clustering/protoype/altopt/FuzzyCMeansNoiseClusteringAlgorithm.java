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
import data.algebra.EuclideanVectorSpace;
import data.algebra.VectorSpace;
import data.set.IndexedDataObject;
import data.set.IndexedDataSet;
import datamining.clustering.FuzzyNoiseClusteringAlgorithm;
import datamining.clustering.protoype.AbstractPrototypeClusteringAlgorithm;
import datamining.clustering.protoype.AlgorithmNotInitializedException;
import datamining.clustering.protoype.Centroid;
import etc.MyMath;

/**
 * TODO Class Description
 * 
 * Paper: Dave, R. N. Characterization and detection of noise in clustering Pattern Recogn. Lett., Elsevier Science Inc., 1991, 12, 657-664
 * 
 * @author Roland Winkler
 */
public class FuzzyCMeansNoiseClusteringAlgorithm<T> extends FuzzyCMeansClusteringAlgorithm<T> implements FuzzyNoiseClusteringAlgorithm<T>
{
	/**  */
	private static final long	serialVersionUID	= 7172180428079511424L;
	/**  */
	protected double noiseDistance;
			
	/** the default constructor for cloning */
	public FuzzyCMeansNoiseClusteringAlgorithm(IndexedDataSet<T> data, VectorSpace<T> vs, Metric<T> dist)
	{
		super(data, vs, dist);
		
		this.noiseDistance				= 0.1d*Math.sqrt(Double.MAX_VALUE);
	}
	
	/**
	 * @param c
	 * @param useCluster
	 */
	public FuzzyCMeansNoiseClusteringAlgorithm(AbstractPrototypeClusteringAlgorithm<T, Centroid<T>> c, boolean useOnlyActivePrototypes)
	{
		super(c, useOnlyActivePrototypes);

		this.noiseDistance				= 0.1d*Math.sqrt(Double.MAX_VALUE);
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.AbstractDoubleArrayClusteringAlgorithm#algorithmName()
	 */
	@Override
	public String algorithmName()
	{
		return "Fuzzy c-Means Noise Clustering Algorithm";
	}
	
	/* (non-Javadoc)
	 * @see datamining.ClusteringAlgorithm#performClustering(int)
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
		
		double distanceExponent = 1.0d / (1.0d - this.fuzzifier);	// to reduce the usage of divisions
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l 
		double doubleTMP = 0.0d;									// a temporarly variable for multiple perpuses
		double[] fuzzDistances				= new double[this.getClusterCount()];
		double[] distancesSq				= new double[this.getClusterCount()];
		
		double fuzzNoiseDist				= 0.0d;
			
						
		for(j=0; j < this.getDataCount(); j++)
		{	
			
			distanceSum = 0.0d;
			for(i=0; i<this.getClusterCount(); i++)
			{
				doubleTMP = this.metric.distanceSq(this.data.get(j).x, this.prototypes.get(i).getPosition());
				if(doubleTMP <= 0.0d)
				{
					doubleTMP = 0.0d;
					distancesSq[i] = doubleTMP;
					fuzzDistances[i] = 1.0d;
				}
				else
				{
					distancesSq[i] = doubleTMP;
					doubleTMP = MyMath.pow(doubleTMP, distanceExponent);
					fuzzDistances[i] = doubleTMP;
					distanceSum += doubleTMP;
				}
			}
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

	/**
	 * @return
	 */
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
	 * @see datamining.FuzzyClusterResultAlgorithm#getFuzzyResult()
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
	 * @see datamining.clustering.FuzzyClusteringAlgorithm#getFuzzyAssignments(data.set.IndexedDataObject)
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
	 * @see datamining.clustering.FuzzyClusteringAlgorithm#isAssigned(data.set.IndexedDataObject)
	 */
	@Override
	public boolean isFuzzyAssigned(IndexedDataObject<T> obj)
	{
		return this.initialized && this.getFuzzyNoiseAssignmentOf(obj) < 1.0d;
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.FuzzyNoiseClusteringAlgorithm#getFuzzyNoiseAssignment(data.set.IndexedDataObject)
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
	 * @see datamining.clustering.FuzzyNoiseClusteringAlgorithm#getFuzzyNoiseAssignments()
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

	/**
	 * @param clone
	 */
	public void clone(FuzzyCMeansNoiseClusteringAlgorithm<T> clone)
	{
		super.clone(clone);
		
		clone.noiseDistance = this.noiseDistance;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public FuzzyCMeansNoiseClusteringAlgorithm<T> clone()
	{
		FuzzyCMeansNoiseClusteringAlgorithm<T> clone = new FuzzyCMeansNoiseClusteringAlgorithm<T>(this.data, (EuclideanVectorSpace<T>)this.vs, this.metric);
		this.clone(clone);
		return clone;
	}

}
