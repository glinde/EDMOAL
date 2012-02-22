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

import data.algebra.Metric;
import data.algebra.ScalarProduct;
import data.algebra.VectorSpace;
import data.set.IndexedDataObject;
import data.set.IndexedDataSet;
import datamining.clustering.protoype.AbstractPrototypeClusteringAlgorithm;
import datamining.clustering.protoype.AlgorithmNotInitializedException;
import datamining.clustering.protoype.Centroid;
import etc.MyMath;

/**
 * The Voronoi partitioning fuzzy c-means clustering algorithm is an extension to the standard fuzzy c-means clustering algorithm.
 * Its special property is, that for each individual data object, only the prototypes are part of the membership value
 * calculation, that are not located 'behind' other prototypes. More specifically, if the prototypes are sorted by distance
 * w.r.t. a data object, and a prototype <code>y<sub>far</sub></code> is further away than an other prototype <code>y<sub>near</sub></code>
 * then the membership value to <code>y<sub>far</sub></code> is 0, if it lys behind the hyperplane that goes through
 * <code>y<sub>near</sub></code> and is perpendicular to the vector from <code>y<sub>near</sub></code> and the data object.
 * In other words, <code>y<sub>far</sub></code> lies behind <code>y<sub>near</sub></code> and is therefore excluded.
 * With this, a Voronoi cell is defined for each data object and the 'close' prototypes define the faces of this Voronoi cell.<br> 
 *
 * Paper: to appear<br>
 * 
 * The Voronoi property requires, that the prototypes are sorted w.r.t. their distance towards each data object. This increases the
 * computational costs of the algorithm by a factor of log(c). But even worse, once the prototypes are sorted, they have to
 * be tested for being hidden behind closer prototypes. That means, for each data object, each pair of prototypes
 * has to be tested if the closer one of them hides the other. That means, the runtime complexity for each data object
 * is c^2 time the runtime complexity of calculating the scalar product in the algebraic space of the data objects.<br>
 * 
 * In this particular implementation, the membership matrix is  not stored when the algorithm is applied. That is possible because the membership
 * values of one data object are independent of all other objects, given the position of the prototypes.<br> 
 * 
 * The runtime complexity of this algorithm is in O(t*n*c^2),
 * with t being the number of iterations, n being the number of data objects and c being the number of clusters.
 * This is, neglecting the runtime complexity of distance calculations and algebraic operations in the vector space.
 * The full complexity would be in O(t*n*c*(c*O(scal)+O(dist)+O(add)+O(mul))) where O(dist) is the complexity of
 * calculating the distance between a data object and a prototype, O(add) is the complexity of calculating the
 * vector addition of two types <code>T</code>, O(mul) is the complexity of scalar multiplication of type <code>T</code> and
 * O(scal) is the complexity of calculating the scalar product for two objects from type <code>T</code>. <br>
 *  
 * The memory consumption of this algorithm is in O(t+n+c).
 *
 * @author Roland Winkler
 */
public class VoronoiPartitionFCMClusteringAlgorithm<T> extends FuzzyCMeansClusteringAlgorithm<T>
{
	/**  */
	private static final long	serialVersionUID	= -143340496452088879L;


	/**
	 * A comparable wrapper class for prototypes to sort them w.r.t. their distance to one data object.
	 * It also contains an indicator if it is included in the clustering process. 
	 *
	 * @author Roland Winkler
	 */
	public class SortablePrototype implements Comparable<SortablePrototype>
	{
		public Centroid<T> prototype;
		public double squareDistance;
		public T relativeVecToDataObject; 
		public boolean included;
		
		public SortablePrototype(Centroid<T> proto)
		{
			this.prototype = proto;
			this.squareDistance = 0.0d;
			this.included = true;
			this.relativeVecToDataObject = VoronoiPartitionFCMClusteringAlgorithm.this.vs.getNewAddNeutralElement();
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(SortablePrototype o)
		{
			if(this.squareDistance == o.squareDistance) return 0;
			return (this.squareDistance < o.squareDistance)? -1: 1;
		}
	}
	
	/** The scalar product, used for determining if prototype is hidden by an other prototype. */
	protected final ScalarProduct<T> sp;

	/**
	 * This constructor creates a new VoronoiPartitionFCMClusteringAlgorithm, taking an existing prototype clustering algorithm.
	 * It has the option to use only active prototypes from the old clustering algorithm. This constructor is especially
	 * useful if the clustering is done in multiple steps. The first clustering algorithm can for example calculate the
	 * initial positions of the prototypes for the second clustering algorithm. An other option is, that the first clustering
	 * algorithm creates a set of deactivated prototypes and the second clustering algorithm is initialized with less
	 * clusters than the first.
	 * 
	 * @param c the elders clustering algorithm.
	 * @param sp The scalar product, used for determining if prototype is hidden by an other prototype.
	 * @param useOnlyActivePrototypes States, that only prototypes that are active in the old clustering
	 * algorithm are used for the new clustering algorithm.
	 */
	public VoronoiPartitionFCMClusteringAlgorithm(AbstractPrototypeClusteringAlgorithm<T, Centroid<T>> c, ScalarProduct<T> sp, boolean useOnlyActivePrototypes)
	{
		super(c, useOnlyActivePrototypes);
		
		this.sp = sp;
	}


	/**
	 * Creates a new VoronoiPartitionFCMClusteringAlgorithm with the specified data set, vector space and metric.
	 * The prototypes are not initialized by this method, it has to be done separately.
	 * The metric must be differentiable w.r.t. <code>y</code> in <code>dist(x, y)<sup>2</sup></code>, and
	 * the directed differential in direction of <code>y</code> must yield <code>d/dy dist(x, y)^2 = 2(y - x)</code>
	 * for the algorithm to be correct.
	 * 
	 * @param data The data set that should be clustered.
	 * @param vs The vector space that is used to calculate the prototype positions.
	 * @param metric The metric that is used to calculate the distance between data objects and prototypes.
	 * @param sp The scalar product, used for determining if prototype is hidden by an other prototype.
	 */
	public VoronoiPartitionFCMClusteringAlgorithm(IndexedDataSet<T> data, VectorSpace<T> vs, Metric<T> metric, ScalarProduct<T> sp)
	{
		super(data, vs, metric);

		this.sp = sp;		
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.altopt.FuzzyCMeansClusteringAlgorithm#algorithmName()
	 */
	@Override
	public String algorithmName()
	{
		return "Voronoi Partition Based Fuzzy c-Means Clustering Algorithm";
	}
	
	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.altopt.FuzzyCMeansClusteringAlgorithm#apply(int)
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
					this.vs.copy(unsortedPrototypes.get(i).relativeVecToDataObject, unsortedPrototypes.get(i).prototype.getPosition());
					this.vs.sub(unsortedPrototypes.get(i).relativeVecToDataObject, this.data.get(j).x);
					unsortedPrototypes.get(i).squareDistance = this.metric.distanceSq(unsortedPrototypes.get(i).prototype.getPosition(), this.data.get(j).x);

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
							doubleTMP = this.sp.scalarProduct(ip.relativeVecToDataObject, sp.relativeVecToDataObject); 
														
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
				
				doubleTMP = this.metric.distanceSq(this.prototypes.get(i).getPosition(), newPrototypePosition.get(i));
				
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
				this.vs.copy(unsortedPrototypes.get(i).relativeVecToDataObject, unsortedPrototypes.get(i).prototype.getPosition());
				this.vs.sub(unsortedPrototypes.get(i).relativeVecToDataObject, this.data.get(j).x);
				unsortedPrototypes.get(i).squareDistance = this.metric.distanceSq(unsortedPrototypes.get(i).prototype.getPosition(), this.data.get(j).x);

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
						doubleTMP = this.sp.scalarProduct(ip.relativeVecToDataObject, sp.relativeVecToDataObject); 
													
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
				this.vs.copy(unsortedPrototypes.get(i).relativeVecToDataObject, unsortedPrototypes.get(i).prototype.getPosition());
				this.vs.sub(unsortedPrototypes.get(i).relativeVecToDataObject, this.data.get(j).x);
				unsortedPrototypes.get(i).squareDistance = this.metric.distanceSq(unsortedPrototypes.get(i).prototype.getPosition(), this.data.get(j).x);

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
						doubleTMP = this.sp.scalarProduct(ip.relativeVecToDataObject, sp.relativeVecToDataObject); 
													
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
				this.vs.copy(unsortedPrototypes.get(i).relativeVecToDataObject, unsortedPrototypes.get(i).prototype.getPosition());
				this.vs.sub(unsortedPrototypes.get(i).relativeVecToDataObject, this.data.get(j).x);
				unsortedPrototypes.get(i).squareDistance = this.metric.distanceSq(unsortedPrototypes.get(i).prototype.getPosition(), this.data.get(j).x);

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
						doubleTMP = this.sp.scalarProduct(ip.relativeVecToDataObject, sp.relativeVecToDataObject); 
													
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
			this.vs.copy(unsortedPrototypes.get(i).relativeVecToDataObject, unsortedPrototypes.get(i).prototype.getPosition());
			this.vs.sub(unsortedPrototypes.get(i).relativeVecToDataObject, obj.x);
			unsortedPrototypes.get(i).squareDistance = this.metric.distanceSq(unsortedPrototypes.get(i).prototype.getPosition(), obj.x);

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
					doubleTMP = this.sp.scalarProduct(ip.relativeVecToDataObject, sp.relativeVecToDataObject); 
												
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


	/**
	 * Returns the scalar product, used for determining if prototype is hidden by an other prototype.
	 * 
	 * @return The scalar product, used for determining if prototype is hidden by an other prototype.
	 */
	public ScalarProduct<T> getSp()
	{
		return this.sp;
	}
}
