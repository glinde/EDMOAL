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
import data.algebra.VectorSpace;
import data.set.IndexedDataObject;
import data.set.IndexedDataSet;
import datamining.clustering.protoype.AbstractCentroidClusteringAlgorithm;
import datamining.clustering.protoype.AbstractPrototypeClusteringAlgorithm;
import datamining.clustering.protoype.AlgorithmNotInitializedException;
import datamining.clustering.protoype.Centroid;
import datamining.clustering.protoype.MembershipFunctionProvider;
import datamining.resultProviders.FuzzyClusteringProvider;

/**
 * The fuzzy c-means clustering algorithm with polynomial fuzzifier function is an extension of FCM.
 * It replaces the fuzzifier function <code>u<sup>w</sup></code> with <code>(1-beta)/(1+beta)u<sup>2</sup> + (2*beta)/(1+beta)*u</code>.
 * The linear component in the fuzzifier function leads to a crisp area of clustering around each prototype. The
 * clustering becomes crisp (in the two-cluster case), if the relative distances to the prototypes is equal to <code>beta</code>.
 * See the papers for more information on the algorithm and the theory connected to it. <br> 
 * 
 * Paper: Klawonn, F. & H�ppner, F. R. Berthold, M.; Lenz, H.-J.; Bradley, E.; Kruse, R. & Borgelt, C. (Eds.) What Is Fuzzy about Fuzzy Clustering? Understanding and Improving the Concept of the Fuzzifier Advances in Intelligent Data Analysis V, Springer Berlin / Heidelberg, 2003, 2810, 254-264<br>
 * 
 * In this particular implementation, the membership matrix is  not stored when the algorithm is applied. That is possible because the membership
 * values of one data object are independent of all other objects, given the position of the prototypes. However,
 * the linear component in the fuzzifier function leads to a crisp assignment of data objects, near prototypes.
 * To calculate it correctly, the prototypes must be sorted w.r.t. their distance to the data object.
 * This sorting increases the complexity of the algorithm, especially if many prototypes are present.<br> 
 * 
 * The runtime complexity of this algorithm is in O(t*n*c*log(c)),
 * with t being the number of iterations, n being the number of data objects and c being the number of clusters.
 * This is, neglecting the runtime complexity of distance calculations and algebraic operations in the vector space.
 * The full complexity would be in O(t*n*c*log(c)*(O(dist)+O(add)+O(mul))) where O(dist) is the complexity of
 * calculating the distance between a data object and a prototype, O(add) is the complexity of calculating the
 * vector addition of two types <code>T</code> and O(mul) is the complexity of scalar multiplication of type <code>T</code>. <br>
 *  
 * The memory consumption of this algorithm is in O(t+n+c).
 *
 * 
 * @author Roland Winkler
 */
public class PolynomFCMClusteringAlgorithm<T> extends AbstractCentroidClusteringAlgorithm<T> implements FuzzyClusteringProvider<T>, MembershipFunctionProvider
{
	/**  */
	private static final long	serialVersionUID	= 3347388178304679371L;


	/**
	 * A comparable wrapper class for prototypes to sort them w.r.t. their distance to one data object.
	 * It also contains an indicator if it is included in the clustering process. 
	 *
	 * @author Roland Winkler
	 */
	public class SortablePrototype implements Comparable<SortablePrototype>
	{
		/** The Prototype */
		public Centroid<T> prototype;
		/** The squared distance to the data object */
		public double squareDistance;
		/** Indicates whether it is included in the clustering process. */
		public boolean included;
		
		/**
		 * The standard constructor for this comparable wrapper. 
		 * 
		 * @param proto The prototype for this comparable wrapper.
		 */
		public SortablePrototype(Centroid<T> proto)
		{
			this.prototype = proto;
			this.squareDistance = 0.0d;
			this.included = false;
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
	
	/** The distance ratio at which data objects are clustered in hard clustering */
	protected double beta;
		

	/**
	 * Creates a new PolynomFCMClusteringAlgorithm with the specified data set, vector space and metric.
	 * The prototypes are not initialized by this method, it has to be done separately.
	 * The metric must be differentiable w.r.t. <code>y</code> in <code>dist(x, y)<sup>2</sup></code>, and
	 * the directed differential in direction of <code>y</code> must yield <code>d/dy dist(x, y)^2 = 2(y - x)</code>
	 * for the algorithm to be correct.
	 * 
	 * @param data The data set that should be clustered.
	 * @param vs The vector space that is used to calculate the prototype positions.
	 * @param metric The metric that is used to calculate the distance between data objects and prototypes.
	 */
	public PolynomFCMClusteringAlgorithm(IndexedDataSet<T> data, VectorSpace<T> vs, Metric<T> metric)
	{
		super(data, vs, metric);
		
		this.beta = 0.5d;
	}


	/**
	 * This constructor creates a new PolynomFCMClusteringAlgorithm, taking an existing prototype clustering algorithm.
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
	public PolynomFCMClusteringAlgorithm(AbstractPrototypeClusteringAlgorithm<T, Centroid<T>> c, boolean useOnlyActivePrototypes)
	{
		super(c, useOnlyActivePrototypes);
		
		this.beta = 0.5d;
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.AbstractPrototypeClusteringAlgorithm#algorithmName()
	 */
	@Override
	public String algorithmName()
	{
		return "Polynomial Fuzzy c-Means Clustering Algorithm";
	}
	
	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.AbstractPrototypeClusteringAlgorithm#apply(int)
	 */
	@Override
	public void apply(int steps)
	{
		if(!this.initialized) throw new AlgorithmNotInitializedException("Prototypes not initialized.");
		
		int i, j, t; 

		double testDouble= 1.0d/this.beta - 1.0d;
		double hFunctionBetaA = (1.0d - this.beta)/(1.0d + this.beta);
		double hFunctionBetaB = 2.0d * this.beta/(1.0d + this.beta);
		
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l 
		
		double doubleTMP = 0.0d;								 	// a temporarly variable for multiple perpuses
		int hatC = 0;
		
		double maxPrototypeMovement = 0.0d;
		ArrayList<T> newPrototypePosition	= new ArrayList<T>(this.getClusterCount());
		for(i=0; i<this.getClusterCount(); i++) newPrototypePosition.add(this.vs.getNewAddNeutralElement()); 
		double[] membershipValues			= new double[this.getClusterCount()];
		double[] membershipSum				= new double[this.getClusterCount()];
		T tmpX								= this.vs.getNewAddNeutralElement();
		SortablePrototype sp;
		
		int zeroDistanceCount				= 0;

		PriorityQueue<SortablePrototype> sortedPrototypes = new PriorityQueue<SortablePrototype>(this.getClusterCount());		
		ArrayList<SortablePrototype> unsortedPrototypes = new ArrayList<SortablePrototype>(this.getClusterCount());
		for(Centroid<T> p:this.prototypes) unsortedPrototypes.add(new SortablePrototype(p));

//		System.out.print(this.algorithmName());
		long timeStart = System.currentTimeMillis();
		
		for(t = 0; t < steps; t++)
		{
//			System.out.print(".");
			
			maxPrototypeMovement = 0.0d;			
			for(i=0; i<this.getClusterCount(); i++)
			{
				this.vs.resetToAddNeutralElement(newPrototypePosition.get(i));
				membershipSum[i] = 0.0d;
			}
			
			for(j = 0; j < this.getDataCount(); j++)
			{
				zeroDistanceCount = 0;
				distanceSum = 0.0d;
				
				
				for(i = 0; i < this.getClusterCount(); i++)
				{
					unsortedPrototypes.get(i).included = false;
					unsortedPrototypes.get(i).squareDistance = this.metric.distanceSq(this.data.get(j).x, this.prototypes.get(i).getPosition());
					if(unsortedPrototypes.get(i).squareDistance <= 0.0d)	zeroDistanceCount++;
				}
				
				if(zeroDistanceCount > 0)
				{
					doubleTMP = 1.0d/zeroDistanceCount;
					for(i = 0; i < this.getClusterCount(); i++)
					{
						if(unsortedPrototypes.get(i).squareDistance <= 0.0d)
						{
							membershipValues[i] = doubleTMP;
						}
						else
						{
							membershipValues[i] = 0.0d;
						}
					}
				}
				else
				{
					sortedPrototypes.clear();
					sortedPrototypes.addAll(unsortedPrototypes);
					
					// calculate \hat c by iteratively test if an other prototype can be added to the calculation.					
					hatC = 0;
					doubleTMP = 0.0d;
					while(!sortedPrototypes.isEmpty())
					{
						sp = sortedPrototypes.poll();						
						doubleTMP += 1.0d/sp.squareDistance;
						if(sp.squareDistance * doubleTMP - (hatC + 1) > testDouble) break;
						hatC++;
						distanceSum = doubleTMP;
						sp.included=true;
					}
					
					for(SortablePrototype usp:unsortedPrototypes)
					{
						if(usp.included)
						{
							doubleTMP = 1.0d + (hatC - 1.0d)*this.beta;
							doubleTMP /= usp.squareDistance * distanceSum;
							doubleTMP -= this.beta;
							doubleTMP *= 1.0d/(1.0d - this.beta);
							
							membershipValues[usp.prototype.getClusterIndex()] = doubleTMP;
						}
						else
						{
							membershipValues[usp.prototype.getClusterIndex()] = 0.0d;
						}
					}

					for(i = 0; i < this.getClusterCount(); i++)
					{
						doubleTMP = (hFunctionBetaA*membershipValues[i] + hFunctionBetaB)*membershipValues[i]; 
						membershipSum[i] += doubleTMP;

						this.vs.copy(tmpX, this.data.get(j).x);
						this.vs.mul(tmpX, doubleTMP);
						this.vs.add(newPrototypePosition.get(i), tmpX);
					}
				}
			}

			// update prototype positions
			// update prototypes
			for(i=0; i<this.getClusterCount(); i++)
			{
				if(membershipSum[i] > 0.0d)
				{
					this.vs.mul(newPrototypePosition.get(i), 1.0d/membershipSum[i]);
				}
				else
				{
					this.vs.copy(newPrototypePosition.get(i), this.prototypes.get(i).getPosition());
				}
			}

			// copy new prototype values into prototypes wrt. learning factor
			for(i=0; i < this.getClusterCount(); i++)
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

			if(this.iterationCount >= this.minIterations && maxPrototypeMovement < this.epsilon*this.epsilon) break;
		}

//		System.out.println(" done. [" + (System.currentTimeMillis() - timeStart) + "]");
	}


	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.AbstractPrototypeClusteringAlgorithm#getObjectiveFunctionValue()
	 */
	@Override
	public double getObjectiveFunctionValue()
	{
		if(!this.initialized) throw new AlgorithmNotInitializedException("Prototypes not initialized.");
		
		int i, j; 

		double testDouble= 1.0d/this.beta - 1.0d;
		double hFunctionBetaA = (1.0d - this.beta)/(1.0d + this.beta);
		double hFunctionBetaB = 2.0d * this.beta/(1.0d + this.beta);
		
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l
		double doubleTMP = 0.0d;								 	// a temporarly variable for multiple perpuses
		int hatC = 0;
		
		double objectiveFunctionValue = 0.0d;
		double[] membershipValues			= new double[this.getClusterCount()];		
		int zeroDistanceCount				= 0;
		SortablePrototype sp;

		PriorityQueue<SortablePrototype> sortedPrototypes = new PriorityQueue<SortablePrototype>(this.getClusterCount());		
		ArrayList<SortablePrototype> unsortedPrototypes = new ArrayList<SortablePrototype>(this.getClusterCount());
		for(Centroid<T> p:this.prototypes) unsortedPrototypes.add(new SortablePrototype(p));
						
		for(j=0; j < this.getDataCount(); j++)
		{
			zeroDistanceCount = 0;
			distanceSum = 0.0d;
			
			
			for(i = 0; i < this.getClusterCount(); i++)
			{
				unsortedPrototypes.get(i).included = false;
				unsortedPrototypes.get(i).squareDistance = this.metric.distanceSq(this.data.get(j).x, this.prototypes.get(i).getPosition());
				if(unsortedPrototypes.get(i).squareDistance <= 0.0d)	zeroDistanceCount++;
			}
			
			if(zeroDistanceCount > 0)
			{}
			else
			{
				sortedPrototypes.clear();
				sortedPrototypes.addAll(unsortedPrototypes);

				// calculate \hat c by iteratively test if an other prototype can be added to the calculation.					
				hatC = 0;
				doubleTMP = 0.0d;
				while(!sortedPrototypes.isEmpty())
				{
					sp = sortedPrototypes.poll();	
					doubleTMP += 1.0d/sp.squareDistance;
					if(sp.squareDistance * doubleTMP - (hatC + 1) > testDouble) break;
					hatC++;
					distanceSum = doubleTMP;
					sp.included = true;
				}

				
				for(SortablePrototype usp:unsortedPrototypes)
				{
					if(usp.included)
					{
						doubleTMP = 1.0d + (hatC - 1.0d)*this.beta;
						doubleTMP /= usp.squareDistance * distanceSum;
						doubleTMP -= this.beta;
						doubleTMP *= 1.0d/(1.0d - this.beta);
						
						membershipValues[usp.prototype.getClusterIndex()] = doubleTMP;
					}
					else
					{
						membershipValues[usp.prototype.getClusterIndex()] = 0.0d;
					}
				}

				
				for(i=0; i<this.getClusterCount(); i++)
				{
					objectiveFunctionValue += ((hFunctionBetaA*membershipValues[i] + hFunctionBetaB)*membershipValues[i]) * unsortedPrototypes.get(i).squareDistance;
				}
			}
		}
		
		return objectiveFunctionValue;
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.FuzzyClusteringProvider#getFuzzyAssignmentsOf(data.set.IndexedDataObject)
	 */
	@Override
	public double[] getFuzzyAssignmentsOf(IndexedDataObject<T> obj)
	{
		if(!this.initialized) throw new AlgorithmNotInitializedException("Prototypes not initialized.");
		
		int i; 

		double testDouble= 1.0d/this.beta - 1.0d;
		
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l 
		
		double doubleTMP = 0.0d;								 	// a temporarly variable for multiple perpuses
		int hatC = 0;
		
		double[] membershipValues			= new double[this.getClusterCount()];		
		int zeroDistanceCount				= 0;
		SortablePrototype sp;

		PriorityQueue<SortablePrototype> sortedPrototypes = new PriorityQueue<SortablePrototype>(this.getClusterCount());		
		ArrayList<SortablePrototype> unsortedPrototypes = new ArrayList<SortablePrototype>(this.getClusterCount());
		for(Centroid<T> p:this.prototypes) unsortedPrototypes.add(new SortablePrototype(p));
				
		zeroDistanceCount = 0;
		distanceSum = 0.0d;
		
		
		for(i = 0; i < this.getClusterCount(); i++)
		{
			unsortedPrototypes.get(i).included = false;
			unsortedPrototypes.get(i).squareDistance = this.metric.distanceSq(obj.x, this.prototypes.get(i).getPosition());
			if(unsortedPrototypes.get(i).squareDistance <= 0.0d)	zeroDistanceCount++;
		}
		
		if(zeroDistanceCount > 0)
		{
			doubleTMP = 1.0d/zeroDistanceCount;
			for(i = 0; i < this.getClusterCount(); i++)
			{
				if(unsortedPrototypes.get(i).squareDistance <= 0.0d)
				{
					membershipValues[i] = doubleTMP;
				}
				else
				{
					membershipValues[i] = 0.0d;
				}
			}
		}
		else
		{
			sortedPrototypes.clear();
			sortedPrototypes.addAll(unsortedPrototypes);

			// calculate \hat c by iteratively test if an other prototype can be added to the calculation.					
			hatC = 0;
			doubleTMP = 0.0d;
			while(!sortedPrototypes.isEmpty())
			{
				sp = sortedPrototypes.poll();	
				doubleTMP += 1.0d/sp.squareDistance;
				if(sp.squareDistance * doubleTMP - (hatC + 1) > testDouble) break;
				hatC++;
				distanceSum = doubleTMP;
				sp.included=true;
			}

			
			for(SortablePrototype usp:unsortedPrototypes)
			{
				if(usp.included)
				{
					doubleTMP = 1.0d + (hatC - 1.0d)*this.beta;
					doubleTMP /= usp.squareDistance * distanceSum;
					doubleTMP -= this.beta;
					doubleTMP *= 1.0d/(1.0d - this.beta);
					
					membershipValues[usp.prototype.getClusterIndex()] = doubleTMP;
				}
				else
				{
					membershipValues[usp.prototype.getClusterIndex()] = 0.0d;
				}
			}
		}
			
		
		return membershipValues;
	}


	/* (non-Javadoc)
	 * @see datamining.clustering.FuzzyClusteringProvider#getAllFuzzyClusterAssignments(java.util.List)
	 */
	@Override
	public List<double[]> getAllFuzzyClusterAssignments(List<double[]> assignmentList)
	{
		if(!this.initialized) throw new AlgorithmNotInitializedException("Prototypes not initialized.");
		if(assignmentList == null) assignmentList = new ArrayList<double[]>(this.getDataCount());
		
		int i, j; 

		double testDouble= 1.0d/this.beta - 1.0d;
		
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l 
		
		double doubleTMP = 0.0d;								 	// a temporarly variable for multiple perpuses
		int hatC = 0;
		
		double[] membershipValues			= new double[this.getClusterCount()];		
		int zeroDistanceCount				= 0;
		SortablePrototype sp;

		PriorityQueue<SortablePrototype> sortedPrototypes = new PriorityQueue<SortablePrototype>(this.getClusterCount());		
		ArrayList<SortablePrototype> unsortedPrototypes = new ArrayList<SortablePrototype>(this.getClusterCount());
		for(Centroid<T> p:this.prototypes) unsortedPrototypes.add(new SortablePrototype(p));
				
		for(j = 0; j < this.getDataCount(); j++)
		{
			zeroDistanceCount = 0;
			distanceSum = 0.0d;
			
			
			for(i = 0; i < this.getClusterCount(); i++)
			{
				unsortedPrototypes.get(i).included = false;
				unsortedPrototypes.get(i).squareDistance = this.metric.distanceSq(this.data.get(j).x, this.prototypes.get(i).getPosition());
				if(unsortedPrototypes.get(i).squareDistance <= 0.0d)	zeroDistanceCount++;
			}
			
			if(zeroDistanceCount > 0)
			{
				doubleTMP = 1.0d/zeroDistanceCount;
				for(i = 0; i < this.getClusterCount(); i++)
				{
					if(unsortedPrototypes.get(i).squareDistance <= 0.0d)
					{
						membershipValues[i] = doubleTMP;
					}
					else
					{
						membershipValues[i] = 0.0d;
					}
				}
			}
			else
			{
				sortedPrototypes.clear();
				sortedPrototypes.addAll(unsortedPrototypes);

				// calculate \hat c by iteratively test if an other prototype can be added to the calculation.					
				hatC = 0;
				doubleTMP = 0.0d;
				while(!sortedPrototypes.isEmpty())
				{
					sp = sortedPrototypes.poll();	
					doubleTMP += 1.0d/sp.squareDistance;
					if(sp.squareDistance * doubleTMP - (hatC + 1) > testDouble) break;
					hatC++;
					distanceSum = doubleTMP;
					sp.included=true;
				}

				
				for(SortablePrototype usp:unsortedPrototypes)
				{
					if(usp.included)
					{
						doubleTMP = 1.0d + (hatC - 1.0d)*this.beta;
						doubleTMP /= usp.squareDistance * distanceSum;
						doubleTMP -= this.beta;
						doubleTMP *= 1.0d/(1.0d - this.beta);
						
						membershipValues[usp.prototype.getClusterIndex()] = doubleTMP;
					}
					else
					{
						membershipValues[usp.prototype.getClusterIndex()] = 0.0d;
					}
				}
			}
			
			assignmentList.add(membershipValues.clone());
		}
		
		return assignmentList;
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.FuzzyClusteringProvider#getFuzzyAssignmentSums()
	 */
	@Override
	public double[] getFuzzyAssignmentSums()
	{

		if(!this.initialized) throw new AlgorithmNotInitializedException("Prototypes not initialized.");
		
		int i, j; 

		double testDouble= 1.0d/this.beta - 1.0d;
		
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l 
		
		double doubleTMP = 0.0d;								 	// a temporarly variable for multiple perpuses
		int hatC = 0;
		
		double[] membershipValues			= new double[this.getClusterCount()];
		double[] membershipValueSum			= new double[this.getClusterCount()];				
		int zeroDistanceCount				= 0;
		SortablePrototype sp;

		PriorityQueue<SortablePrototype> sortedPrototypes = new PriorityQueue<SortablePrototype>(this.getClusterCount());		
		ArrayList<SortablePrototype> unsortedPrototypes = new ArrayList<SortablePrototype>(this.getClusterCount());
		for(Centroid<T> p:this.prototypes) unsortedPrototypes.add(new SortablePrototype(p));
				
		for(j = 0; j < this.getDataCount(); j++)
		{
			zeroDistanceCount = 0;
			distanceSum = 0.0d;
			
			
			for(i = 0; i < this.getClusterCount(); i++)
			{
				unsortedPrototypes.get(i).included = false;
				unsortedPrototypes.get(i).squareDistance = this.metric.distanceSq(this.data.get(j).x, this.prototypes.get(i).getPosition());
				if(unsortedPrototypes.get(i).squareDistance <= 0.0d)	zeroDistanceCount++;
			}
			
			if(zeroDistanceCount > 0)
			{
				doubleTMP = 1.0d/zeroDistanceCount;
				for(i = 0; i < this.getClusterCount(); i++)
				{
					if(unsortedPrototypes.get(i).squareDistance <= 0.0d)
					{
						membershipValues[i] = doubleTMP;
					}
					else
					{
						membershipValues[i] = 0.0d;
					}
				}
			}
			else
			{
				sortedPrototypes.clear();
				sortedPrototypes.addAll(unsortedPrototypes);

				// calculate \hat c by iteratively test if an other prototype can be added to the calculation.					
				hatC = 0;
				doubleTMP = 0.0d;
				while(!sortedPrototypes.isEmpty())
				{
					sp = sortedPrototypes.poll();	
					doubleTMP += 1.0d/sp.squareDistance;
					if(sp.squareDistance * doubleTMP - (hatC + 1) > testDouble) break;
					hatC++;
					distanceSum = doubleTMP;
					sp.included=true;
				}

				
				for(SortablePrototype usp:unsortedPrototypes)
				{
					if(usp.included)
					{
						doubleTMP = 1.0d + (hatC - 1.0d)*this.beta;
						doubleTMP /= usp.squareDistance * distanceSum;
						doubleTMP -= this.beta;
						doubleTMP *= 1.0d/(1.0d - this.beta);
						
						membershipValues[usp.prototype.getClusterIndex()] = doubleTMP;
					}
					else
					{
						membershipValues[usp.prototype.getClusterIndex()] = 0.0d;
					}
				}
			}
			
			for(i=0; i<this.getClusterCount(); i++)
			{
				membershipValueSum[i] += membershipValues[i];
			}
		}
		
		return membershipValueSum;
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.FuzzyClusteringProvider#isFuzzyAssigned(data.set.IndexedDataObject)
	 */
	@Override
	public boolean isFuzzyAssigned(IndexedDataObject<T> obj)
	{
		return this.initialized;
	}


	/**
	 * Returns the beta parameter.
	 * 
	 * @return The beta parameter.
	 */
	public double getBeta()
	{
		return this.beta;
	}


	/**
	 * Sets the beta parameter. The range of the values is 0 to 1.
	 * 
	 * @param beta the beta to set
	 */
	public void setBeta(double beta)
	{
		if(beta < 0.0d || 1.0d < beta)  throw new IllegalArgumentException("The beta parameter must be larger than 0 and smaller than 1. Specified beta: " + beta);
		
		this.beta = beta;
	}


	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.MembershipFunctionProvider#applyMembershipFunction(double)
	 */
	@Override
	public double applyMembershipFunction(double membershipValue)
	{
		return (1.0d - this.beta)/(1.0d + this.beta) * membershipValue * membershipValue + 2.0d * this.beta/(1.0d + this.beta) * membershipValue;
	}
}
