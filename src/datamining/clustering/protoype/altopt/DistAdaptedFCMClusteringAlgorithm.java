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

import data.algebra.Distance;
import data.algebra.VectorSpace;
import data.set.IndexedDataObject;
import data.set.IndexedDataSet;
import data.set.structures.BallTree;
import datamining.clustering.protoype.AlgorithmNotInitializedException;
import datamining.clustering.protoype.Centroid;
import etc.MyMath;
import etc.SimpleStatistics;

/**
 * TODO Class Description
 * 
 * Paper: to appear
 *
 * @author Roland Winkler
 */
public class DistAdaptedFCMClusteringAlgorithm<T> extends FuzzyCMeansClusteringAlgorithm<T>
{
	/**  */
	private static final long	serialVersionUID	= -3814440444229284948L;

	protected boolean mergePrototypes;
	
	protected double mergingDistance;
	
	protected boolean removeEmptyPrototypes;

	protected double minMemembershipValueSum;
		
	protected double distanceCorrectionParameter;

	/**
	 * @param data
	 * @param vs
	 * @param dist
	 */
	public DistAdaptedFCMClusteringAlgorithm(IndexedDataSet<T> data, VectorSpace<T> vs, Distance<T> dist)
	{
		super(data, vs, dist);
		
		this.mergePrototypes = false;
		this.mergingDistance = 0.0d;
		
		this.removeEmptyPrototypes = false;
		this.minMemembershipValueSum = 0.0d;
		
		this.distanceCorrectionParameter = 3.0d;
	}

	/**
	 * @param c
	 * @param useOnlyActivePrototypes
	 */
	public DistAdaptedFCMClusteringAlgorithm(DistAdaptedFCMClusteringAlgorithm<T> c, boolean useOnlyActivePrototypes)
	{
		super(c, useOnlyActivePrototypes);
		
		this.mergePrototypes = c.mergePrototypes ;
		this.mergingDistance = c.mergingDistance;
		
		this.removeEmptyPrototypes = c.removeEmptyPrototypes;
		this.minMemembershipValueSum = c.minMemembershipValueSum;
		
		this.distanceCorrectionParameter = c.distanceCorrectionParameter;
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
		// t: index for iterations	
				
		double distanceExponent = 1.0d / (1.0d - this.fuzzifier);	// to reduce the usage of divisions
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l 
		double doubleTMP = 0.0d;									// a temporarly variable for multiple perpuses
		double[] fuzzDistances						= new double[this.getClusterCount()];
		double[] distancesSq						= new double[this.getClusterCount()];
		double[] distancesToData					= new double[this.getDataCount()];
		double[] dynamicDistanceCorrectionValues 	= new double[this.getClusterCount()];
		double objectiveFunctionValue				= 0.0d;
			

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

			for(i=0; i<this.getClusterCount(); i++)
			{
				doubleTMP = fuzzDistances[i] / distanceSum;
				objectiveFunctionValue +=  MyMath.pow(doubleTMP, this.fuzzifier) * distancesSq[i];
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
					
					if(this.dist.distanceSq(this.getPrototypes().get(i).getPosition(), this.getPrototypes().get(j).getPosition()) < this.mergingDistance*this.mergingDistance)
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
			ballTree = new BallTree<T>(tmpPrototypeDataSet, this.dist);
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

	public double[] calculateDataObjectDistancePlot(double[] result, Collection<IndexedDataObject<T>> list, T reference, boolean sort)
	{
		if(result == null || result.length < list.size()) result = new double[list.size()];
		int i=0;
		
		for(IndexedDataObject<T> p:list)
		{
			result[i] = this.dist.distance(p.element, reference);
			i++;
		}
		
		if(sort) Arrays.sort(result);
		
		return result;
	}

	/**
	 * @return the mergePrototypes
	 */
	public boolean isMergePrototypes()
	{
		return this.mergePrototypes;
	}

	/**
	 * @param mergePrototypes the mergePrototypes to set
	 */
	public void setMergePrototypes(boolean mergePrototypes)
	{
		this.mergePrototypes = mergePrototypes;
	}

	/**
	 * @return the mergingDistance
	 */
	public double getMergingDistance()
	{
		return this.mergingDistance;
	}

	/**
	 * @param mergingDistance the mergingDistance to set
	 */
	public void setMergingDistance(double mergingDistance)
	{
		this.mergingDistance = mergingDistance;
	}

	/**
	 * @return the removeEmptyPrototypes
	 */
	public boolean isRemoveEmptyPrototypes()
	{
		return this.removeEmptyPrototypes;
	}

	/**
	 * @param removeEmptyPrototypes the removeEmptyPrototypes to set
	 */
	public void setRemoveEmptyPrototypes(boolean removeEmptyPrototypes)
	{
		this.removeEmptyPrototypes = removeEmptyPrototypes;
	}

	/**
	 * @return the minMemembershipValueSum
	 */
	public double getMinMemembershipValueSum()
	{
		return this.minMemembershipValueSum;
	}

	/**
	 * @param minMemembershipValueSum the minMemembershipValueSum to set
	 */
	public void setMinMemembershipValueSum(double minMemembershipValueSum)
	{
		this.minMemembershipValueSum = minMemembershipValueSum;
	}

	/**
	 * @return the distanceCorrectionParameter
	 */
	public double getDistanceCorrectionParameter()
	{
		return this.distanceCorrectionParameter;
	}

	/**
	 * @param distanceCorrectionParameter the distanceCorrectionParameter to set
	 */
	public void setDistanceCorrectionParameter(double distanceCorrectionParameter)
	{
		this.distanceCorrectionParameter = distanceCorrectionParameter;
	}
	
	
}
