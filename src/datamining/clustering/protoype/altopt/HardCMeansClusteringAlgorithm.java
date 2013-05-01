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

import data.algebra.Metric;
import data.algebra.VectorSpace;
import data.set.IndexedDataObject;
import data.set.IndexedDataSet;
import datamining.clustering.protoype.AbstractCentroidClusteringAlgorithm;
import datamining.clustering.protoype.AbstractPrototypeClusteringAlgorithm;
import datamining.clustering.protoype.AlgorithmNotInitializedException;
import datamining.clustering.protoype.Centroid;
import datamining.clustering.protoype.MembershipFunctionProvider;
import datamining.resultProviders.CrispClusteringProvider;

/**
 * The hard c-means clustering algorithm is maybe one of the first clustering algorithms that have been invented.
 * See the paper for more information on the algorithm and the theory connected to it. <br> 
 *
 * Paper: MacQueen, J. B. Some Methods for Classification and Analysis of MultiVariate Observations Proc. of the fifth Berkeley Symposium on Mathematical Statistics and Probability, University of California Press, 1967, 1, 281-297<br>
 *
 * for this particular implementation, an array of integer values contain the clustering result.
 * Each element of the array corresponds to the data object with the same index and
 * the value of the element is the cluster the data object is associated to. Before initializing
 * the algorithm, all data objects remain unclustered (cluster index {@link CrispClusteringProvider#UNASSIGNED_INDEX}).<br>
 * 
 * The runtime complexity of this algorithm is in O(t*n*c),
 * with t being the number of iterations, n being the number of data objects and c being the number of clusters.
 * This is, neglecting the runtime complexity of distance calculations and algebraic operations in the vector space.
 * The full complexity would be in O(t*n*c*(O(dist)+O(add))+t*c*O(mul)) where O(dist) is the complexity of
 * calculating the distance between a data object and a prototype, O(add) is the complexity of calculating the
 * vector addition of two types <code>T</code> and O(mul) is the complexity of scalar multiplication. <br>
 * 
 * The memory consumption of this algorithm is in O(t+n+c).
 * 
 * @author Roland Winkler
 */
public class HardCMeansClusteringAlgorithm<T> extends AbstractCentroidClusteringAlgorithm<T> implements CrispClusteringProvider<T>, MembershipFunctionProvider
{		
	/**  */
	private static final long	serialVersionUID	= -2518725991257149820L;
	
	/**
	 *  A list of integer values containing the clustering result. Each
	 *  element of the array corresponds to the data object with the same index and
	 *  the value of the element is the cluster the data obiect is associated to.
	 */
	protected int[] clusteringResult;
	
	/**
	 * Creates a new HardCMeansClusteringAlgorithm with the specified data set, vector space and metric.
	 * All data objects are set to {@link CrispClusteringProvider#UNASSIGNED_INDEX}. No prototypes
	 * are initialized so far.
	 * 
	 * @param data The data set that should be clustered.
	 * @param vs The vector space that is used to calculate the prototype positions.
	 * @param metric The metric that is used to calculate the distance between data objects and prototypes.
	 */
	public HardCMeansClusteringAlgorithm(IndexedDataSet<T> data, VectorSpace<T> vs, Metric<T> metric)
	{
		super(data, vs, metric);

		this.clusteringResult = new int[this.data.size()];
		for(int j=0; j<this.getDataCount(); j++) this.clusteringResult[j] = CrispClusteringProvider.UNASSIGNED_INDEX;
	}

	/**
	 * This constructor creates a new HardCMeansClusteringAlgorithm, taking an existing prototype clustering algorithm.
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
	public HardCMeansClusteringAlgorithm(AbstractPrototypeClusteringAlgorithm<T, Centroid<T>> c, boolean useOnlyActivePrototypes)
	{
		super(c, useOnlyActivePrototypes);
		
		this.clusteringResult = new int[this.data.size()];
		for(int j=0; j<this.getDataCount(); j++) this.clusteringResult[j] = CrispClusteringProvider.UNASSIGNED_INDEX;
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.AbstractPrototypeClusteringAlgorithm#initializeWithPrototypes(java.util.Collection)
	 */
	@Override
	public void initializeWithPrototypes(Collection<Centroid<T>> initialPrototypes)
	{
		super.initializeWithPrototypes(initialPrototypes);
		
		this.recalculateClusterAssignments();
	}
	
	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.AbstractCentroidClusteringAlgorithm#initializeWithPositions(java.util.Collection)
	 */
	@Override
	public void initializeWithPositions(Collection<T> initialPrototypePositions)
	{
		super.initializeWithPositions(initialPrototypePositions);
		
		this.recalculateClusterAssignments();
	}
	
	/**
	 * Recalculate the cluster assignments. That means, it fills the {@link #clusteringResult} with
	 * correct clustering values, given the current position of the prototypes. Useful for example
	 * after initialisation.
	 */
	private void recalculateClusterAssignments()
	{
		if(!this.initialized) throw new AlgorithmNotInitializedException("Prototypes not initialized.");
		
		int i;
		double distMin, dist;
		int pMin = 0;
		
		// separate data according to closest prototype 
		for(IndexedDataObject<T> x : this.data)
		{
			pMin = 0;
			distMin = Double.MAX_VALUE;
			for(i=0; i<this.getClusterCount(); i++)
			{
				dist = this.metric.distanceSq(this.prototypes.get(i).getPosition(), x.x); 
				if(dist < distMin)
				{
					distMin = dist;
					pMin = i;
				}
			}
			
			this.clusteringResult[x.getID()] = pMin;
		}
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.AbstractPrototypeClusteringAlgorithm#algorithmName()
	 */
	@Override
	public String algorithmName()
	{
		return "Hard c-Means Clustering Algorithm";
	}
	
	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.AbstractPrototypeClusteringAlgorithm#apply(int)
	 */
	@Override
	public void apply(int steps)
	{
		if(!this.initialized) throw new AlgorithmNotInitializedException("Prototypes not initialized.");
		
		int i, t; 
		
		int pMin;
		double distMin, dist;
		double doubleTMP;
		boolean assignmentChanged;
		int[] clusterWeight = new int[this.getClusterCount()];
		
		ArrayList<T> newPrototypePosition = new ArrayList<T>(this.getClusterCount());
		for(i=0; i<this.getClusterCount(); i++) newPrototypePosition.add(this.vs.getNewAddNeutralElement()); 

		System.out.print(this.algorithmName());
		long timeStart = System.currentTimeMillis();
		
		for(t = 0; t < steps; t++)
		{
			System.out.print(".");
			
			// reset
			for(i = 0; i < this.getClusterCount(); i++)
			{
				this.vs.resetToAddNeutralElement(newPrototypePosition.get(i));
				clusterWeight[i] = 0;
			}
			assignmentChanged = false;
			
			// separate data according to closest prototype 
			for(IndexedDataObject<T> x : this.data)
			{
				pMin = -1;
				distMin = Double.MAX_VALUE;
				for(i=0; i<this.getClusterCount(); i++)
				{
					dist = this.metric.distanceSq(this.prototypes.get(i).getPosition(), x.x); 
					if(dist < distMin)
					{
						distMin = dist;
						pMin = i;
					}
				}
				
				assignmentChanged |= (this.clusteringResult[x.getID()] != pMin); 
				
				this.clusteringResult[x.getID()] = pMin;
				clusterWeight[pMin]++;
				this.vs.add(newPrototypePosition.get(pMin), x.x);
			}
			
			for(i=0; i<this.getClusterCount();i++)
			{
				if(clusterWeight[i] <= 0) continue;
				doubleTMP = 1.0d/((double)clusterWeight[i]);				
				this.vs.mul(newPrototypePosition.get(i), doubleTMP);

				if(Math.abs(this.learningFactor - 1.0d) > 0.01d)
				{
					this.vs.sub(newPrototypePosition.get(i), this.prototypes.get(i).getPosition());
					this.vs.mul(newPrototypePosition.get(i), this.learningFactor);
					this.vs.add(newPrototypePosition.get(i), this.prototypes.get(i).getPosition());	
				}
				
				this.getPrototypes().get(i).moveTo(newPrototypePosition.get(i));
			}

			this.iterationComplete();
			
//			System.out.println("assignmentChanged = " + assignmentChanged);
			if(t>0 && !assignmentChanged)	break;
		}

		System.out.println(" done. [" + (System.currentTimeMillis() - timeStart) + "]");
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.AbstractPrototypeClusteringAlgorithm#getObjectiveFunctionValue()
	 */
	@Override
	public double getObjectiveFunctionValue()
	{
		if(!this.initialized) throw new AlgorithmNotInitializedException("Prototypes not initialized.");
		
		int i; 		
		double distMin, dist;
		double objectiveFunctionValue = 0.0d;
		

		// separate data according to closest prototype 
		for(IndexedDataObject<T> x : this.data)
		{
			distMin = Double.MAX_VALUE;
			for(i=0; i<this.getClusterCount(); i++)
			{
				dist = this.metric.distanceSq(this.prototypes.get(i).getPosition(), x.x); 
				if(dist < distMin)
				{
					distMin = dist;
				}
			}
			 
			objectiveFunctionValue += distMin;
		}
		
		return objectiveFunctionValue;
		
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.CrispClusteringProvider#getCrispClusterAssignmentOf(data.set.IndexedDataObject)
	 */
	@Override
	public int getCrispClusterAssignmentOf(IndexedDataObject<T> obj)
	{
		if(!this.initialized) throw new AlgorithmNotInitializedException("Prototypes not initialized.");
		
		return this.clusteringResult[obj.getID()];
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.CrispClusteringProvider#getAllCrispClusterAssignments()
	 */
	@Override
	public int[] getAllCrispClusterAssignments()
	{
		if(!this.initialized) throw new AlgorithmNotInitializedException("Prototypes not initialized.");
		
		return this.clusteringResult.clone();
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.CrispClusteringProvider#isCrispAssigned(data.set.IndexedDataObject)
	 */
	@Override
	public boolean isCrispAssigned(IndexedDataObject<T> obj)
	{
		return this.initialized;
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.MembershipFunctionProvider#applyMembershipFunction(double)
	 */
	@Override
	public double applyMembershipFunction(double membershipValue)
	{
		return membershipValue;
	}
}
