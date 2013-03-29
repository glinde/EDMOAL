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
import datamining.clustering.protoype.AbstractPrototypeClusteringAlgorithm;
import datamining.clustering.protoype.AlgorithmNotInitializedException;
import datamining.clustering.protoype.Centroid;
import datamining.resultProviders.FuzzyNoiseClusteringProvider;

/**
 * This is an implementation of the fuzzy c-means clustering algorithm with additional noise cluster and with a polynomial fuzzifier function.
 * It replaces the fuzzifier function <code>u<sup>w</sup></code> with <code>(1-beta)/(1+beta)u<sup>2</sup> + (2*beta)/(1+beta)*u</code>.
 * The linear component in the fuzzifier function leads to a crisp area of clustering around each prototype. The
 * clustering becomes crisp (in the two-cluster case), if the relative distances to the prototypes is equal to <code>beta</code>.
 * See the papers for more information on the algorithm and the theory connected to it. <br> 
 * 
 * Paper: Klawonn, F. & Höppner, F. R. Berthold, M.; Lenz, H.-J.; Bradley, E.; Kruse, R. & Borgelt, C. (Eds.) What Is Fuzzy about Fuzzy Clustering? Understanding and Improving the Concept of the Fuzzifier Advances in Intelligent Data Analysis V, Springer Berlin / Heidelberg, 2003, 2810, 254-264<br>
 * 
 * In this particular implementation, the membership matrix is  not stored when the algorithm is applied. That is possible because the membership
 * values of one data object are independent of all other objects, given the position of the prototypes. However,
 * the linear component in the fuzzifier function leads to a crisp assignment of data objects, near prototypes.
 * To calculate it correctly, the prototypes must be sorted w.r.t. their distance to the data object.
 * This sorting increases the complexity of the algorithm, especially if many prototypes are present.<br>
 * 
 * Due to the niose cluster and for a finite noise distance, data objects that are fare away from the prototypes are
 * clustered crisply as noise. This effect is a direct result of fact that the noise cluster has a constant distance to
 * all data objects and the concept of the polynomial fuzzifier function. The runtime complexity of the algorithm
 * is not effected by the additional noise cluster.<br>
 * 
 * However, due to the finite range of the 'good' clusters, it is advisable to either initialize the prototypes
 * close to the clusters, or to set the noise distance not too far at the beginning. It might be better to
 * reduce the noise distance and apply the algorithm in 2 or 3 steps in order to minimize the chance that a prototype
 * does not find any data objects due to an unlucky initialization and the limited distance a prototype can be effected
 * by data objects.<br> 
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
public class PolynomFCMNoiseClusteringAlgorithm<T> extends PolynomFCMClusteringAlgorithm<T> implements FuzzyNoiseClusteringProvider<T>
{	
	/**  */
	private static final long	serialVersionUID	= -4173377426715965448L;
	
	
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
	 * Creates a new PolynomFCMNoiseClusteringAlgorithm with the specified data set, vector space and metric.
	 * The prototypes are not initialized by this method, it has to be done separately.
	 * The metric must be differentiable w.r.t. <code>y</code> in <code>dist(x, y)<sup>2</sup></code>, and
	 * the directed differential in direction of <code>y</code> must yield <code>d/dy dist(x, y)^2 = 2(y - x)</code>
	 * for the algorithm to be correct.
	 * 
	 * @param data The data set that should be clustered.
	 * @param vs The vector space that is used to calculate the prototype positions.
	 * @param parameterMetric The metric that is used to calculate the distance between data objects and prototypes.
	 */
	public PolynomFCMNoiseClusteringAlgorithm(IndexedDataSet<T> data, VectorSpace<T> vs, Metric<T> dist)
	{
		super(data, vs, dist);

		this.noiseDistance				= 0.1d*Math.sqrt(Double.MAX_VALUE);
		this.degradingNoiseDistance		= this.noiseDistance;
		this.noiseDegrationFactor		= 1.0d;
	}

	/**
	 * This constructor creates a new PolynomFCMNoiseClusteringAlgorithm, taking an existing prototype clustering algorithm.
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
	public PolynomFCMNoiseClusteringAlgorithm(AbstractPrototypeClusteringAlgorithm<T, Centroid<T>> c, boolean useOnlyActivePrototypes)
	{
		super(c, useOnlyActivePrototypes);

		this.noiseDistance				= 0.1d*Math.sqrt(Double.MAX_VALUE);
		this.degradingNoiseDistance		= this.noiseDistance;
		this.noiseDegrationFactor		= 1.0d;
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.AbstractDoubleArrayClusteringAlgorithm#algorithmName()
	 */
	@Override
	public String algorithmName()
	{
		return "Polynomial Fuzzy c-Means Noise Clustering Algorithm";
	}
	

	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.altopt.PolynomFCMClusteringAlgorithm#apply(int)
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
		
		int zeroDistanceCount				= 0;
		SortablePrototype sp;

		PriorityQueue<SortablePrototype> sortedPrototypes = new PriorityQueue<SortablePrototype>(this.getClusterCount());		
		ArrayList<SortablePrototype> unsortedPrototypes = new ArrayList<SortablePrototype>(this.getClusterCount());
		for(Centroid<T> p:this.prototypes) unsortedPrototypes.add(new SortablePrototype(p));
		
		SortablePrototype sortedNoise = new SortablePrototype(null);
		

		System.out.print(this.algorithmName());
		long timeStart = System.currentTimeMillis();
		
		for(t = 0; t < steps; t++)
		{
			System.out.print(".");
			doubleTMP = this.noiseDistance + (this.degradingNoiseDistance - this.noiseDistance) * Math.exp(-this.noiseDegrationFactor*t);
			sortedNoise.squareDistance = doubleTMP*doubleTMP;
			
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
				sortedNoise.included = false;
				
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
					sortedPrototypes.add(sortedNoise);
					
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
			
			if(maxPrototypeMovement < this.epsilon*this.epsilon) break;
		}

		System.out.println(" done. [" + (System.currentTimeMillis() - timeStart) + "]");
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.altopt.PolynomFCMClusteringAlgorithm#getObjectiveFunctionValue()
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

		SortablePrototype sortedNoise = new SortablePrototype(null);
		sortedNoise.squareDistance = this.noiseDistance*this.noiseDistance;
						
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
				sortedPrototypes.add(sortedNoise);

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
	 * @see datamining.clustering.protoype.altopt.PolynomFCMClusteringAlgorithm#getFuzzyAssignmentsOf(data.set.IndexedDataObject)
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

		SortablePrototype sortedNoise = new SortablePrototype(null);
		sortedNoise.squareDistance = this.noiseDistance*this.noiseDistance;
		
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
			sortedPrototypes.add(sortedNoise);

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
	 * @see datamining.clustering.protoype.altopt.PolynomFCMClusteringAlgorithm#getAllFuzzyClusterAssignments(java.util.List)
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

		SortablePrototype sortedNoise = new SortablePrototype(null);
		sortedNoise.squareDistance = this.noiseDistance*this.noiseDistance;
		
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
				sortedPrototypes.add(sortedNoise);

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
	 * @see datamining.clustering.protoype.altopt.PolynomFCMClusteringAlgorithm#getFuzzyAssignmentSums()
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

		SortablePrototype sortedNoise = new SortablePrototype(null);
		sortedNoise.squareDistance = this.noiseDistance*this.noiseDistance;
				
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
				sortedPrototypes.add(sortedNoise);

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
	 * @see datamining.clustering.protoype.altopt.PolynomFCMClusteringAlgorithm#isFuzzyAssigned(data.set.IndexedDataObject)
	 */
	@Override
	public boolean isFuzzyAssigned(IndexedDataObject<T> obj)
	{
		return this.initialized && this.getFuzzyNoiseAssignmentOf(obj) < 1.0d;
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.FuzzyNoiseClusteringProvider#getFuzzyNoiseAssignments()
	 */
	@Override
	public double[] getFuzzyNoiseAssignments()
	{
		if(!this.initialized) throw new AlgorithmNotInitializedException("Prototypes not initialized.");
		
		int i, j; 

		double testDouble= 1.0d/this.beta - 1.0d;
		
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l 
		double doubleTMP = 0.0d;								 	// a temporarly variable for multiple perpuses
		int hatC = 0;
		
		double[] noiseMembershipValues = new double[this.getDataCount()];		
		int zeroDistanceCount				= 0;
		SortablePrototype sp;

		PriorityQueue<SortablePrototype> sortedPrototypes = new PriorityQueue<SortablePrototype>(this.getClusterCount());		
		ArrayList<SortablePrototype> unsortedPrototypes = new ArrayList<SortablePrototype>(this.getClusterCount());
		for(Centroid<T> p:this.prototypes) unsortedPrototypes.add(new SortablePrototype(p));

		SortablePrototype sortedNoise = new SortablePrototype(null);
		sortedNoise.squareDistance = this.noiseDistance*this.noiseDistance;
		
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
				noiseMembershipValues[j] = 0.0d;
			}
			else
			{
				sortedPrototypes.clear();
				sortedPrototypes.addAll(unsortedPrototypes);
				sortedPrototypes.add(sortedNoise);

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

				
				if(sortedNoise.included)
				{
					doubleTMP = 1.0d + (hatC - 1.0d)*this.beta;
					doubleTMP /= sortedNoise.squareDistance * distanceSum;
					doubleTMP -= this.beta;
					doubleTMP *= 1.0d/(1.0d - this.beta);
					
					noiseMembershipValues[j] = doubleTMP;
				}
				else
				{
					noiseMembershipValues[j] = 0.0d;
				}
			}
		}
		
		return noiseMembershipValues;
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.FuzzyNoiseClusteringProvider#getFuzzyNoiseAssignmentOf(data.set.IndexedDataObject)
	 */
	@Override
	public double getFuzzyNoiseAssignmentOf(IndexedDataObject<T> obj)
	{
		if(!this.initialized) throw new AlgorithmNotInitializedException("Prototypes not initialized.");
		
		int i; 

		double testDouble= 1.0d/this.beta - 1.0d;
		
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l 
		double doubleTMP = 0.0d;								 	// a temporarly variable for multiple perpuses
		int hatC = 0;
		
		double noiseMembershipValue = 0.0d;		
		int zeroDistanceCount = 0;
		SortablePrototype sp;

		PriorityQueue<SortablePrototype> sortedPrototypes = new PriorityQueue<SortablePrototype>(this.getClusterCount());		
		ArrayList<SortablePrototype> unsortedPrototypes = new ArrayList<SortablePrototype>(this.getClusterCount());
		for(Centroid<T> p:this.prototypes) unsortedPrototypes.add(new SortablePrototype(p));

		SortablePrototype sortedNoise = new SortablePrototype(null);
		sortedNoise.squareDistance = this.noiseDistance*this.noiseDistance;
		
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
			noiseMembershipValue = 0.0d;
		}
		else
		{
			sortedPrototypes.clear();
			sortedPrototypes.addAll(unsortedPrototypes);
			sortedPrototypes.add(sortedNoise);

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

			
			if(sortedNoise.included)
			{
				doubleTMP = 1.0d + (hatC - 1.0d)*this.beta;
				doubleTMP /= sortedNoise.squareDistance * distanceSum;
				doubleTMP -= this.beta;
				doubleTMP *= 1.0d/(1.0d - this.beta);
				
				noiseMembershipValue = doubleTMP;
			}
			else
			{
				noiseMembershipValue = 0.0d;
			}
		}
		
		return noiseMembershipValue;
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
	
	
}
