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
import java.util.PriorityQueue;

import data.algebra.EuclideanVectorSpace;
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
 * Paper: to appear
 * 
 * @author Roland Winkler
 */
public class VoronoiPartitionFCMNoiseClusteringAlgorithm<T> extends VoronoiPartitionFCMClusteringAlgorithm<T> implements FuzzyNoiseClusteringAlgorithm<T>
{
	/**  */
	private static final long	serialVersionUID	= 2723685927200471389L;
	/**  */
	protected double noiseDistance;
	
	/**
	 * @param data
	 * @param evs
	 */
	public VoronoiPartitionFCMNoiseClusteringAlgorithm(IndexedDataSet<T> data, EuclideanVectorSpace<T> evs)
	{
		super(data, evs);

		this.noiseDistance				= 0.1d*Math.sqrt(Double.MAX_VALUE);
	}

	/**
	 * @param c
	 * @param useOnlyActivePrototypes
	 */
	public VoronoiPartitionFCMNoiseClusteringAlgorithm(AbstractPrototypeClusteringAlgorithm<T, Centroid<T>> c, EuclideanVectorSpace<T> evs, boolean useOnlyActivePrototypes)
	{
		super(c, evs, useOnlyActivePrototypes);

		this.noiseDistance				= 0.1d*Math.sqrt(Double.MAX_VALUE);
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.altopt.VoronoiPartitionFCMClusteringAlgorithm#algorithmName()
	 */
	@Override
	public String algorithmName()
	{
		return "Voronoi Partition Based Fuzzy c-Means Noise Clustering Algorithm";
	}

	/* (non-Javadoc)
	 * @see datamining.FuzzyCMeansClusteringAlgorithm#performClustering(int)
	 */
	@Override
	public void apply(int steps)
	{
		if(!this.initialized) throw new AlgorithmNotInitializedException("Prototypes not initialized.");	
		
		int i, j, t; 

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
		SortablePrototype sp;
		
		PriorityQueue<SortablePrototype> sortedPrototypes = new PriorityQueue<SortablePrototype>(this.getClusterCount());		
		ArrayList<SortablePrototype> unsortedPrototypes = new ArrayList<SortablePrototype>(this.getClusterCount());
		for(Centroid<T> p:this.prototypes) unsortedPrototypes.add(new SortablePrototype(p));
		ArrayList<SortablePrototype> includedPrototypes = new ArrayList<SortablePrototype>(this.getClusterCount());
		
		int			zeroDistanceCount;

		for(t=0; t<steps; t++)
		{
			// reset values
			maxPrototypeMovement = 0.0d;
			
			for(i=0; i<this.getClusterCount(); i++)
			{
				this.vs.resetToAddNeutralElement(newPrototypePosition.get(i));
				membershipSum[i] = 0.0d;
			}
			
			// update membership values
			for(j = 0; j < this.getDataCount(); j++)
			{				
				zeroDistanceCount = 0;
				distanceSum = 0.0d;
				
				// calculate distances and relative vectors from data object j to all prototypes
				// fill the priority queue
				for(i=0; i<this.getClusterCount(); i++)
				{
					this.evs.copy(unsortedPrototypes.get(i).relativeVecToDataObject, unsortedPrototypes.get(i).prototype.getPosition());
					this.evs.sub(unsortedPrototypes.get(i).relativeVecToDataObject, this.data.get(j).x);
					unsortedPrototypes.get(i).squareDistance = this.evs.lengthSq(unsortedPrototypes.get(i).relativeVecToDataObject);

					if(unsortedPrototypes.get(i).squareDistance <= 0.0d)	zeroDistanceCount++;
				}
				
				// if one or more prototypes sit on top of a data object, no sorting etc. is necessary			
				if(zeroDistanceCount > 0)
				{
					doubleTMP = 1.0d/((double)zeroDistanceCount);
					for(i=0; i<unsortedPrototypes.size(); i++)
					{
						if(unsortedPrototypes.get(i).squareDistance <= 0.0d)
						{
							membershipValues[unsortedPrototypes.get(i).prototype.getClusterIndex()] = doubleTMP;
						}
						else
						{
							membershipValues[unsortedPrototypes.get(i).prototype.getClusterIndex()] = 0.0d;
						}
					}
				}
				else // update membership values regularly
				{
					sortedPrototypes.clear();
					sortedPrototypes.addAll(unsortedPrototypes);
					
					includedPrototypes.clear();
					
					// test for prototypes in ascending order w.r.t. their distance to the data object
					while(!sortedPrototypes.isEmpty())
					{
						sp = sortedPrototypes.poll();
						sp.included = true;
							
						// test if prototype should be excluded due to closer, already included prototypes
						for(SortablePrototype ip:includedPrototypes)
						{
							doubleTMP = this.evs.scalarProduct(ip.relativeVecToDataObject, sp.relativeVecToDataObject); 
														
							if(doubleTMP > ip.squareDistance)
							{
								sp.included = false;
								break;
							}
						}
						
						// if the prototype should be included, calculate distances accordingly
						if(sp.included)
						{
							includedPrototypes.add(sp);
							doubleTMP = MyMath.pow(sp.squareDistance, distanceExponent);
							fuzzDistances[sp.prototype.getClusterIndex()] = doubleTMP;
							distanceSum += doubleTMP;
						}
					}
					// influence of the noise cluster
					distanceSum += MyMath.pow(this.noiseDistance*this.noiseDistance, distanceExponent);
										
					for(i=0; i<this.getClusterCount(); i++)
					{
						if(unsortedPrototypes.get(i).included)
						{
							doubleTMP = fuzzDistances[unsortedPrototypes.get(i).prototype.getClusterIndex()] / distanceSum;
							membershipValues[unsortedPrototypes.get(i).prototype.getClusterIndex()] = doubleTMP;
						}
						else
						{
							membershipValues[unsortedPrototypes.get(i).prototype.getClusterIndex()] = 0.0d;
						}
					}
				}
				

				for(i=0; i<this.getClusterCount(); i++)
				{
					doubleTMP = MyMath.pow(membershipValues[i], this.fuzzifier);
					membershipSum[i] += doubleTMP;

					this.vs.copy(tmpX, this.data.get(j).x);
					this.vs.mul(tmpX, doubleTMP);
					this.vs.add(newPrototypePosition.get(i), tmpX);
				}
			}

			// update prototypes
			for(i=0; i<this.getClusterCount(); i++) // do not update the noise cluster
			{
				if(membershipSum[i] > 0.0d)
				{
					doubleTMP = 1.0d/membershipSum[i];
					this.vs.mul(newPrototypePosition.get(i), doubleTMP);
				}
				else
				{
					this.vs.copy(newPrototypePosition.get(i), this.prototypes.get(i).getPosition());
				}
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
				
				doubleTMP = this.evs.distanceSq(this.prototypes.get(i).getPosition(), newPrototypePosition.get(i));
				
				maxPrototypeMovement = (doubleTMP > maxPrototypeMovement)? doubleTMP : maxPrototypeMovement;
				
				this.prototypes.get(i).moveTo(newPrototypePosition.get(i));
			}

			this.iterationComplete();
			
			if(maxPrototypeMovement < this.epsilon*this.epsilon) break;
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
	
		double distanceExponent = 1.0d / (1.0d - this.fuzzifier);	// to reduce the usage of divisions
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l 
		double doubleTMP = 0.0d;									// a temporarly variable for multiple perpuses 
		double[] fuzzDistances				= new double[this.getClusterCount()];
		double[] membershipValues			= new double[this.getClusterCount()];
		double objectiveFunctionValue		= 0.0d;
		double fuzzNoiseDist				= 0.0d;
		SortablePrototype sp;
		
		PriorityQueue<SortablePrototype> sortedPrototypes = new PriorityQueue<SortablePrototype>(this.getClusterCount());
		
		ArrayList<SortablePrototype> unsortedPrototypes = new ArrayList<SortablePrototype>(this.getClusterCount());
		for(Centroid<T> p:this.prototypes) unsortedPrototypes.add(new SortablePrototype(p));
		ArrayList<SortablePrototype> includedPrototypes = new ArrayList<SortablePrototype>(this.getClusterCount());
		
		int zeroDistanceCount;
			
		// update membership values
		for(j = 0; j < this.getDataCount(); j++)
		{		
			zeroDistanceCount = 0;
			distanceSum = 0.0d;
			
			// calculate distances and relative vectors from data object j to all prototypes
			// fill the priority queue
			for(i=0; i<this.getClusterCount(); i++)
			{
				this.evs.copy(unsortedPrototypes.get(i).relativeVecToDataObject, unsortedPrototypes.get(i).prototype.getPosition());
				this.evs.sub(unsortedPrototypes.get(i).relativeVecToDataObject, this.data.get(j).x);
				unsortedPrototypes.get(i).squareDistance = this.evs.lengthSq(unsortedPrototypes.get(i).relativeVecToDataObject);

				if(unsortedPrototypes.get(i).squareDistance <= 0.0d)
				{
					zeroDistanceCount++;
					break;
				}
			}
			
			// if one or more prototypes sit on top of a data object, no sorting etc. is necessary			
			if(zeroDistanceCount > 0)
			{
				
			}
			else // update membership values regularly
			{
				sortedPrototypes.clear();
				sortedPrototypes.addAll(unsortedPrototypes);
				
				includedPrototypes.clear();
				
				// test for prototypes in ascending order w.r.t. their distance to the data object
				while(!sortedPrototypes.isEmpty())
				{
					sp = sortedPrototypes.poll();
					sp.included = true;
						
					// test if prototype should be excluded due to closer, already included prototypes
					for(SortablePrototype ip:includedPrototypes)
					{
						doubleTMP = this.evs.scalarProduct(ip.relativeVecToDataObject, sp.relativeVecToDataObject); 
													
						if(doubleTMP > ip.squareDistance)
						{
							sp.included = false;
							break;
						}
					}
					
					// if the prototype should be included, calculate distances accordingly
					if(sp.included)
					{
						includedPrototypes.add(sp);
						doubleTMP = MyMath.pow(sp.squareDistance, distanceExponent);
						fuzzDistances[sp.prototype.getClusterIndex()] = doubleTMP;
						distanceSum += doubleTMP;
					}
				}
				
				fuzzNoiseDist = MyMath.pow(this.noiseDistance*this.noiseDistance, distanceExponent);
				distanceSum += fuzzNoiseDist;
					
				for(i=0; i<this.getClusterCount(); i++)
				{
					if(unsortedPrototypes.get(i).included)
					{
						doubleTMP = fuzzDistances[unsortedPrototypes.get(i).prototype.getClusterIndex()] / distanceSum;
						membershipValues[unsortedPrototypes.get(i).prototype.getClusterIndex()] = doubleTMP;
					}
					else
					{
						membershipValues[unsortedPrototypes.get(i).prototype.getClusterIndex()] = 0.0d;
					}
				}
				
				for(i=0; i<this.getClusterCount(); i++)
				{
					objectiveFunctionValue += MyMath.pow(membershipValues[i], this.fuzzifier) * unsortedPrototypes.get(i).squareDistance;
				}
				
				doubleTMP = fuzzNoiseDist / distanceSum;				
				objectiveFunctionValue += MyMath.pow(doubleTMP, this.fuzzifier) * this.noiseDistance*this.noiseDistance;
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
		
		int i, j; 

		double distanceExponent = 1.0d / (1.0d - this.fuzzifier);	// to reduce the usage of divisions
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l 
		double doubleTMP = 0.0d;									// a temporarly variable for multiple perpuses
		 
		double[] fuzzDistances				= new double[this.getClusterCount()];
		double[] membershipValues			= new double[this.getClusterCount()];
		double[] fuzzyAssignmentSums		= new double[this.getClusterCount()];
		SortablePrototype sp;
		
		PriorityQueue<SortablePrototype> sortedPrototypes = new PriorityQueue<SortablePrototype>(this.getClusterCount());		
		ArrayList<SortablePrototype> unsortedPrototypes = new ArrayList<SortablePrototype>(this.getClusterCount());
		for(Centroid<T> p:this.prototypes) unsortedPrototypes.add(new SortablePrototype(p));
		ArrayList<SortablePrototype> includedPrototypes = new ArrayList<SortablePrototype>(this.getClusterCount());
		
		int			zeroDistanceCount;
		
		// update membership values
		for(j = 0; j < this.getDataCount(); j++)
		{				
			zeroDistanceCount = 0;
			distanceSum = 0.0d;
			
			// calculate distances and relative vectors from data object j to all prototypes
			// fill the priority queue
			for(i=0; i<this.getClusterCount(); i++)
			{
				this.evs.copy(unsortedPrototypes.get(i).relativeVecToDataObject, unsortedPrototypes.get(i).prototype.getPosition());
				this.evs.sub(unsortedPrototypes.get(i).relativeVecToDataObject, this.data.get(j).x);
				unsortedPrototypes.get(i).squareDistance = this.evs.lengthSq(unsortedPrototypes.get(i).relativeVecToDataObject);

				if(unsortedPrototypes.get(i).squareDistance <= 0.0d)	zeroDistanceCount++;
			}
			
			// if one or more prototypes sit on top of a data object, no sorting etc. is necessary			
			if(zeroDistanceCount > 0)
			{
				doubleTMP = 1.0d/((double)zeroDistanceCount);
				for(i=0; i<unsortedPrototypes.size(); i++)
				{
					if(unsortedPrototypes.get(i).squareDistance <= 0.0d)
					{
						membershipValues[unsortedPrototypes.get(i).prototype.getClusterIndex()] = doubleTMP;
					}
					else
					{
						membershipValues[unsortedPrototypes.get(i).prototype.getClusterIndex()] = 0.0d;
					}
				}
			}
			else // update membership values regularly
			{
				sortedPrototypes.clear();
				sortedPrototypes.addAll(unsortedPrototypes);
				
				includedPrototypes.clear();
				
				// test for prototypes in ascending order w.r.t. their distance to the data object
				while(!sortedPrototypes.isEmpty())
				{
					sp = sortedPrototypes.poll();
					sp.included = true;
						
					// test if prototype should be excluded due to closer, already included prototypes
					for(SortablePrototype ip:includedPrototypes)
					{
						doubleTMP = this.evs.scalarProduct(ip.relativeVecToDataObject, sp.relativeVecToDataObject); 
													
						if(doubleTMP > ip.squareDistance)
						{
							sp.included = false;
							break;
						}
					}
					
					// if the prototype should be included, calculate distances accordingly
					if(sp.included)
					{
						includedPrototypes.add(sp);
						doubleTMP = MyMath.pow(sp.squareDistance, distanceExponent);
						fuzzDistances[sp.prototype.getClusterIndex()] = doubleTMP;
						distanceSum += doubleTMP;
					}
				}
				distanceSum += MyMath.pow(this.noiseDistance*this.noiseDistance, distanceExponent);
									
				for(i=0; i<this.getClusterCount(); i++)
				{
					if(unsortedPrototypes.get(i).included)
					{
						doubleTMP = fuzzDistances[unsortedPrototypes.get(i).prototype.getClusterIndex()] / distanceSum;
						membershipValues[unsortedPrototypes.get(i).prototype.getClusterIndex()] = doubleTMP;
					}
					else
					{
						membershipValues[unsortedPrototypes.get(i).prototype.getClusterIndex()] = 0.0d;
					}
				}
			}
			
			for(i=0; i<this.getClusterCount(); i++) fuzzyAssignmentSums[i] += membershipValues[i];
			
		}

		return fuzzyAssignmentSums;
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
		
		int i, j; 

		double distanceExponent = 1.0d / (1.0d - this.fuzzifier);	// to reduce the usage of divisions
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l 
		double doubleTMP = 0.0d;									// a temporarly variable for multiple perpuses
		 
		double[] fuzzDistances				= new double[this.getClusterCount()];
		double[] membershipValues			= new double[this.getClusterCount()];
		SortablePrototype sp;
		
		PriorityQueue<SortablePrototype> sortedPrototypes = new PriorityQueue<SortablePrototype>(this.getClusterCount());		
		ArrayList<SortablePrototype> unsortedPrototypes = new ArrayList<SortablePrototype>(this.getClusterCount());
		for(Centroid<T> p:this.prototypes) unsortedPrototypes.add(new SortablePrototype(p));
		ArrayList<SortablePrototype> includedPrototypes = new ArrayList<SortablePrototype>(this.getClusterCount());
		
		int			zeroDistanceCount;
		
		// update membership values
		for(j = 0; j < this.getDataCount(); j++)
		{				
			zeroDistanceCount = 0;
			distanceSum = 0.0d;
			
			// calculate distances and relative vectors from data object j to all prototypes
			// fill the priority queue
			for(i=0; i<this.getClusterCount(); i++)
			{
				this.evs.copy(unsortedPrototypes.get(i).relativeVecToDataObject, unsortedPrototypes.get(i).prototype.getPosition());
				this.evs.sub(unsortedPrototypes.get(i).relativeVecToDataObject, this.data.get(j).x);
				unsortedPrototypes.get(i).squareDistance = this.evs.lengthSq(unsortedPrototypes.get(i).relativeVecToDataObject);

				if(unsortedPrototypes.get(i).squareDistance <= 0.0d)	zeroDistanceCount++;
			}
			
			// if one or more prototypes sit on top of a data object, no sorting etc. is necessary			
			if(zeroDistanceCount > 0)
			{
				doubleTMP = 1.0d/((double)zeroDistanceCount);
				for(i=0; i<unsortedPrototypes.size(); i++)
				{
					if(unsortedPrototypes.get(i).squareDistance <= 0.0d)
					{
						membershipValues[unsortedPrototypes.get(i).prototype.getClusterIndex()] = doubleTMP;
					}
					else
					{
						membershipValues[unsortedPrototypes.get(i).prototype.getClusterIndex()] = 0.0d;
					}
				}
			}
			else // update membership values regularly
			{
				sortedPrototypes.clear();
				sortedPrototypes.addAll(unsortedPrototypes);
				
				includedPrototypes.clear();
				
				// test for prototypes in ascending order w.r.t. their distance to the data object
				while(!sortedPrototypes.isEmpty())
				{
					sp = sortedPrototypes.poll();
					sp.included = true;
						
					// test if prototype should be excluded due to closer, already included prototypes
					for(SortablePrototype ip:includedPrototypes)
					{
						doubleTMP = this.evs.scalarProduct(ip.relativeVecToDataObject, sp.relativeVecToDataObject); 
													
						if(doubleTMP > ip.squareDistance)
						{
							sp.included = false;
							break;
						}
					}
					
					// if the prototype should be included, calculate distances accordingly
					if(sp.included)
					{
						includedPrototypes.add(sp);
						doubleTMP = MyMath.pow(sp.squareDistance, distanceExponent);
						fuzzDistances[sp.prototype.getClusterIndex()] = doubleTMP;
						distanceSum += doubleTMP;
					}
				}
				distanceSum += MyMath.pow(this.noiseDistance*this.noiseDistance, distanceExponent);
									
				for(i=0; i<this.getClusterCount(); i++)
				{
					if(unsortedPrototypes.get(i).included)
					{
						doubleTMP = fuzzDistances[unsortedPrototypes.get(i).prototype.getClusterIndex()] / distanceSum;
						membershipValues[unsortedPrototypes.get(i).prototype.getClusterIndex()] = doubleTMP;
					}
					else
					{
						membershipValues[unsortedPrototypes.get(i).prototype.getClusterIndex()] = 0.0d;
					}
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
		
		int i; 

		double distanceExponent = 1.0d / (1.0d - this.fuzzifier);	// to reduce the usage of divisions
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l 
		double doubleTMP = 0.0d;									// a temporarly variable for multiple perpuses
		 
		double[] fuzzDistances				= new double[this.getClusterCount()];
		double[] membershipValues			= new double[this.getClusterCount()];
		SortablePrototype sp;
		
		PriorityQueue<SortablePrototype> sortedPrototypes = new PriorityQueue<SortablePrototype>(this.getClusterCount());		
		ArrayList<SortablePrototype> unsortedPrototypes = new ArrayList<SortablePrototype>(this.getClusterCount());
		for(Centroid<T> p:this.prototypes) unsortedPrototypes.add(new SortablePrototype(p));
		ArrayList<SortablePrototype> includedPrototypes = new ArrayList<SortablePrototype>(this.getClusterCount());
		
		int			zeroDistanceCount;
		
		zeroDistanceCount = 0;
		distanceSum = 0.0d;
		
		// calculate distances and relative vectors from data object j to all prototypes
		// fill the priority queue
		for(i=0; i<this.getClusterCount(); i++)
		{
			this.evs.copy(unsortedPrototypes.get(i).relativeVecToDataObject, unsortedPrototypes.get(i).prototype.getPosition());
			this.evs.sub(unsortedPrototypes.get(i).relativeVecToDataObject, obj.x);
			unsortedPrototypes.get(i).squareDistance = this.evs.lengthSq(unsortedPrototypes.get(i).relativeVecToDataObject);

			if(unsortedPrototypes.get(i).squareDistance <= 0.0d)	zeroDistanceCount++;
		}
		
		// if one or more prototypes sit on top of a data object, no sorting etc. is necessary			
		if(zeroDistanceCount > 0)
		{
			doubleTMP = 1.0d/((double)zeroDistanceCount);
			for(i=0; i<unsortedPrototypes.size(); i++)
			{
				if(unsortedPrototypes.get(i).squareDistance <= 0.0d)
				{
					membershipValues[unsortedPrototypes.get(i).prototype.getClusterIndex()] = doubleTMP;
				}
				else
				{
					membershipValues[unsortedPrototypes.get(i).prototype.getClusterIndex()] = 0.0d;
				}
			}
		}
		else // update membership values regularly
		{
			sortedPrototypes.clear();
			sortedPrototypes.addAll(unsortedPrototypes);
			
			includedPrototypes.clear();
			
			// test for prototypes in ascending order w.r.t. their distance to the data object
			while(!sortedPrototypes.isEmpty())
			{
				sp = sortedPrototypes.poll();
				sp.included = true;
					
				// test if prototype should be excluded due to closer, already included prototypes
				for(SortablePrototype ip:includedPrototypes)
				{
					doubleTMP = this.evs.scalarProduct(ip.relativeVecToDataObject, sp.relativeVecToDataObject); 
												
					if(doubleTMP > ip.squareDistance)
					{
						sp.included = false;
						break;
					}
				}
				
				// if the prototype should be included, calculate distances accordingly
				if(sp.included)
				{
					includedPrototypes.add(sp);
					doubleTMP = MyMath.pow(sp.squareDistance, distanceExponent);
					fuzzDistances[sp.prototype.getClusterIndex()] = doubleTMP;
					distanceSum += doubleTMP;
				}
			}
			distanceSum += MyMath.pow(this.noiseDistance*this.noiseDistance, distanceExponent);
								
			for(i=0; i<this.getClusterCount(); i++)
			{
				if(unsortedPrototypes.get(i).included)
				{
					doubleTMP = fuzzDistances[unsortedPrototypes.get(i).prototype.getClusterIndex()] / distanceSum;
					membershipValues[unsortedPrototypes.get(i).prototype.getClusterIndex()] = doubleTMP;
				}
				else
				{
					membershipValues[unsortedPrototypes.get(i).prototype.getClusterIndex()] = 0.0d;
				}
			}
		}

		return membershipValues;
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
		
		int i, j; 

		double distanceExponent = 1.0d / (1.0d - this.fuzzifier);	// to reduce the usage of divisions
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l 
		double doubleTMP = 0.0d;									// a temporarly variable for multiple perpuses

		double fuzzNoiseDist				= 0.0d;
		double[] noiseMemberships			= new double[this.getDataCount()];
		SortablePrototype sp;
		
		PriorityQueue<SortablePrototype> sortedPrototypes = new PriorityQueue<SortablePrototype>(this.getClusterCount());		
		ArrayList<SortablePrototype> unsortedPrototypes = new ArrayList<SortablePrototype>(this.getClusterCount());
		for(Centroid<T> p:this.prototypes) unsortedPrototypes.add(new SortablePrototype(p));
		ArrayList<SortablePrototype> includedPrototypes = new ArrayList<SortablePrototype>(this.getClusterCount());
		
		int			zeroDistanceCount;
		
		for(j=0; j<this.getDataCount(); j++)
		{
			zeroDistanceCount = 0;
			distanceSum = 0.0d;
			
			// calculate distances and relative vectors from data object j to all prototypes
			// fill the priority queue
			for(i=0; i<this.getClusterCount(); i++)
			{
				this.evs.copy(unsortedPrototypes.get(i).relativeVecToDataObject, unsortedPrototypes.get(i).prototype.getPosition());
				this.evs.sub(unsortedPrototypes.get(i).relativeVecToDataObject, this.data.get(j).x);
				unsortedPrototypes.get(i).squareDistance = this.evs.lengthSq(unsortedPrototypes.get(i).relativeVecToDataObject);
	
				if(unsortedPrototypes.get(i).squareDistance <= 0.0d)	zeroDistanceCount++;
			}
			
			// if one or more prototypes sit on top of a data object, no sorting etc. is necessary			
			if(zeroDistanceCount > 0)
			{
				noiseMemberships[j] = 0.0d;
			}
			else // update membership values regularly
			{
				sortedPrototypes.clear();
				sortedPrototypes.addAll(unsortedPrototypes);
				
				includedPrototypes.clear();
				
				// test for prototypes in ascending order w.r.t. their distance to the data object
				while(!sortedPrototypes.isEmpty())
				{
					sp = sortedPrototypes.poll();
					sp.included = true;
						
					// test if prototype should be excluded due to closer, already included prototypes
					for(SortablePrototype ip:includedPrototypes)
					{
						doubleTMP = this.evs.scalarProduct(ip.relativeVecToDataObject, sp.relativeVecToDataObject); 
													
						if(doubleTMP > ip.squareDistance)
						{
							sp.included = false;
							break;
						}
					}
					
					// if the prototype should be included, calculate distances accordingly
					if(sp.included)
					{
						includedPrototypes.add(sp);
						doubleTMP = MyMath.pow(sp.squareDistance, distanceExponent);
						distanceSum += doubleTMP;
					}
				}
				fuzzNoiseDist = MyMath.pow(this.noiseDistance*this.noiseDistance, distanceExponent); 
				distanceSum += fuzzNoiseDist;
				
				noiseMemberships[j] = fuzzNoiseDist/distanceSum; 
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

		double distanceExponent = 1.0d / (1.0d - this.fuzzifier);	// to reduce the usage of divisions
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l 
		double doubleTMP = 0.0d;									// a temporarly variable for multiple perpuses

		double fuzzNoiseDist				= 0.0d;
		SortablePrototype sp;
		
		PriorityQueue<SortablePrototype> sortedPrototypes = new PriorityQueue<SortablePrototype>(this.getClusterCount());		
		ArrayList<SortablePrototype> unsortedPrototypes = new ArrayList<SortablePrototype>(this.getClusterCount());
		for(Centroid<T> p:this.prototypes) unsortedPrototypes.add(new SortablePrototype(p));
		ArrayList<SortablePrototype> includedPrototypes = new ArrayList<SortablePrototype>(this.getClusterCount());
		
		int			zeroDistanceCount;
		
		zeroDistanceCount = 0;
		distanceSum = 0.0d;
		
		// calculate distances and relative vectors from data object j to all prototypes
		// fill the priority queue
		for(i=0; i<this.getClusterCount(); i++)
		{
			this.evs.copy(unsortedPrototypes.get(i).relativeVecToDataObject, unsortedPrototypes.get(i).prototype.getPosition());
			this.evs.sub(unsortedPrototypes.get(i).relativeVecToDataObject, obj.x);
			unsortedPrototypes.get(i).squareDistance = this.evs.lengthSq(unsortedPrototypes.get(i).relativeVecToDataObject);

			if(unsortedPrototypes.get(i).squareDistance <= 0.0d)	zeroDistanceCount++;
		}
		
		// if one or more prototypes sit on top of a data object, no sorting etc. is necessary			
		if(zeroDistanceCount > 0)
		{
			return 0.0d;
		}
		else // update membership values regularly
		{
			sortedPrototypes.clear();
			sortedPrototypes.addAll(unsortedPrototypes);
			
			includedPrototypes.clear();
			
			// test for prototypes in ascending order w.r.t. their distance to the data object
			while(!sortedPrototypes.isEmpty())
			{
				sp = sortedPrototypes.poll();
				sp.included = true;
					
				// test if prototype should be excluded due to closer, already included prototypes
				for(SortablePrototype ip:includedPrototypes)
				{
					doubleTMP = this.evs.scalarProduct(ip.relativeVecToDataObject, sp.relativeVecToDataObject); 
												
					if(doubleTMP > ip.squareDistance)
					{
						sp.included = false;
						break;
					}
				}
				
				// if the prototype should be included, calculate distances accordingly
				if(sp.included)
				{
					includedPrototypes.add(sp);
					doubleTMP = MyMath.pow(sp.squareDistance, distanceExponent);
					distanceSum += doubleTMP;
				}
			}
			fuzzNoiseDist = MyMath.pow(this.noiseDistance*this.noiseDistance, distanceExponent); 
			distanceSum += fuzzNoiseDist;
		}

		return fuzzNoiseDist/distanceSum;
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
