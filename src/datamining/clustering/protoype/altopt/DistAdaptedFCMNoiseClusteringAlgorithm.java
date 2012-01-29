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
import datamining.clustering.FuzzyNoiseClusteringAlgorithm;
import datamining.clustering.protoype.AlgorithmNotInitializedException;
import etc.MyMath;
import etc.SimpleStatistics;

/**
 * TODO Class Description
 *
 * Paper: to appear
 * 
 * @author Roland Winkler
 */
public class DistAdaptedFCMNoiseClusteringAlgorithm<T> extends DistAdaptedFCMClusteringAlgorithm<T> implements FuzzyNoiseClusteringAlgorithm<T>
{
	/**  */
	private static final long	serialVersionUID	= -993192042228012860L;
	
	
	protected double noiseDistance;
	
	/**
	 * @param data
	 * @param vs
	 * @param dist
	 */
	public DistAdaptedFCMNoiseClusteringAlgorithm(IndexedDataSet<T> data, VectorSpace<T> vs, Metric<T> dist)
	{
		super(data, vs, dist);
	}

		
	/**
	 * @param c
	 * @param useOnlyActivePrototypes
	 */
	public DistAdaptedFCMNoiseClusteringAlgorithm(DistAdaptedFCMNoiseClusteringAlgorithm<T> c, boolean useOnlyActivePrototypes)
	{
		super(c, useOnlyActivePrototypes);
		
		this.noiseDistance = c.noiseDistance;
	}


	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.altopt.DistAdaptedFcMClusteringAlgorithm#algorithmName()
	 */
	@Override
	public String algorithmName()
	{
		return "Distance Adapted Fuzzy c-Means Noise Clustering Algorithm";
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.altopt.DistAdaptedFcMClusteringAlgorithm#apply(int)
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
		
		for(t = 0; t < steps; t++)
		{
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
					
					doubleTMP = this.dist.distanceSq(this.data.get(j).element, this.prototypes.get(i).getPosition()) - dynamicDistanceCorrectionValues[i];
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
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.altopt.DistAdaptedFcMClusteringAlgorithm#getObjectiveFunctionValue()
	 */
	@Override
	public double getObjectiveFunctionValue()
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
		double[] fuzzDistances						= new double[this.getClusterCount()];
		double[] distancesSq						= new double[this.getClusterCount()];
		double[] distancesToData					= new double[this.getDataCount()];
		double[] dynamicDistanceCorrectionValues 	= new double[this.getClusterCount()];
		double objectiveFunctionValue				= 0.0d;

		double fuzzNoiseDist						= 0.0d;

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
			for(i=0; i<this.getClusterCount(); i++) distancesSq[i] = 0.0d;
			distanceSum = 0.0d;
			for(i = 0; i < this.getClusterCount(); i++)
			{
				if(!this.getPrototypes().get(i).isActivated()) continue;
				
				doubleTMP = this.dist.distanceSq(this.data.get(j).element, this.prototypes.get(i).getPosition()) - dynamicDistanceCorrectionValues[i];
				distancesSq[i] = doubleTMP;
				if(doubleTMP <= 0.0d)
				{
					fuzzDistances[i] = 0.0;
				}
				else
				{
					doubleTMP = MyMath.pow(doubleTMP, distanceExponent);
					fuzzDistances[i] = doubleTMP;
					distanceSum += doubleTMP;
				}
			}
			
			fuzzNoiseDist = MyMath.pow(this.noiseDistance*this.noiseDistance, distanceExponent);
			distanceSum += fuzzNoiseDist;

			for(i=0; i<this.getClusterCount(); i++)
			{
				doubleTMP = fuzzDistances[i] / distanceSum;
				objectiveFunctionValue +=  MyMath.pow(doubleTMP, this.fuzzifier) * distancesSq[i];
			}
			
			doubleTMP = fuzzNoiseDist / distanceSum;			
			objectiveFunctionValue += MyMath.pow(doubleTMP, this.fuzzifier) * this.noiseDistance*this.noiseDistance;
		}
		
		return objectiveFunctionValue;
	}


	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.altopt.DistAdaptedFcMClusteringAlgorithm#getFuzzyAssignmentSums()
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
				
				doubleTMP = this.dist.distanceSq(this.data.get(j).element, this.prototypes.get(i).getPosition()) - dynamicDistanceCorrectionValues[i];
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
	 * @see datamining.clustering.protoype.altopt.DistAdaptedFcMClusteringAlgorithm#getAllFuzzyClusterAssignments(java.util.List)
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
				
				doubleTMP = this.dist.distanceSq(this.data.get(j).element, this.prototypes.get(i).getPosition()) - dynamicDistanceCorrectionValues[i];
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
	 * @see datamining.clustering.protoype.altopt.DistAdaptedFcMClusteringAlgorithm#getFuzzyAssignmentsOf(data.set.IndexedDataObject)
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
			
			doubleTMP = this.dist.distanceSq(obj.element, this.prototypes.get(i).getPosition()) - dynamicDistanceCorrectionValues[i];
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
				
				doubleTMP = this.dist.distanceSq(this.data.get(j).element, this.prototypes.get(i).getPosition()) - dynamicDistanceCorrectionValues[i];
				if(doubleTMP <= 0.0d)
				{
					doubleTMP = 0.0d;
					zeroDistanceCount++;
				}
				else
				{
					doubleTMP = MyMath.pow(doubleTMP, distanceExponent);
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
	 * @see datamining.clustering.FuzzyNoiseClusteringAlgorithm#getFuzzyNoiseAssignmentOf(data.set.IndexedDataObject)
	 */
	@Override
	public double getFuzzyNoiseAssignmentOf(IndexedDataObject<T> obj)
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
			
			doubleTMP = this.dist.distanceSq(obj.element, this.prototypes.get(i).getPosition()) - dynamicDistanceCorrectionValues[i];
			if(doubleTMP <= 0.0d)
			{
				zeroDistanceCount++;
			}
			else
			{
				doubleTMP = MyMath.pow(doubleTMP, distanceExponent);
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
	 * @see datamining.clustering.FuzzyClusteringAlgorithm#isAssigned(data.set.IndexedDataObject)
	 */
	@Override
	public boolean isFuzzyAssigned(IndexedDataObject<T> obj)
	{
		return this.initialized && this.getFuzzyNoiseAssignmentOf(obj) < 1.0d;
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
