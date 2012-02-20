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
import datamining.clustering.CrispClusteringAlgorithm;
import datamining.clustering.protoype.AbstractCentroidClusteringAlgorithm;
import datamining.clustering.protoype.AbstractPrototypeClusteringAlgorithm;
import datamining.clustering.protoype.AlgorithmNotInitializedException;
import datamining.clustering.protoype.Centroid;

/**
 * The hard c-means clustering algorithm is a prototype based clustering algorithm and maybe one of the first clustering algorithms
 * that have been invented. Hard c-means is an iterative, objection function based, crisp clustering algorithm and therefore,
 * provides all functionalities or these properties. For calculating the prototype positions, a {@link VectorSpace} on type <code>T</code> is required, and for
 * calculating the distance between data objects and prototypes, a {@link Metric} necessary. See the paper for more information
 * on the algorithm and the theory connected to it. <br> 
 *
 * Paper: MacQueen, J. B. Some Methods for Classification and Analysis of MultiVariate Observations Proc. of the fifth Berkeley Symposium on Mathematical Statistics and Probability, University of California Press, 1967, 1, 281-297
 * 
 * @author Roland Winkler
 */
public class HardCMeansClusteringAlgorithm<T> extends AbstractCentroidClusteringAlgorithm<T> implements CrispClusteringAlgorithm<T>
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
	 * All data objects are set to {@link CrispClusteringAlgorithm#UNASSIGNED_INDEX}. No prototypes
	 * are initialized so far.
	 * 
	 * @param data The data set that should be clustered.
	 * @param vs The vector space that is used to calculate the prototype positions.
	 * @param metric The metric that is used to calculate the distance between data obejcts and prototypes.
	 */
	public HardCMeansClusteringAlgorithm(IndexedDataSet<T> data, VectorSpace<T> vs, Metric<T> metric)
	{
		super(data, vs, metric);

		this.clusteringResult = new int[this.data.size()];
		for(int j=0; j<this.getDataCount(); j++) this.clusteringResult[j] = CrispClusteringAlgorithm.UNASSIGNED_INDEX;
	}

	/**
	 *
	 * 
	 * @param data
	 * @param numberOfClusters
	 */
	public HardCMeansClusteringAlgorithm(AbstractPrototypeClusteringAlgorithm<T, Centroid<T>> c, boolean useOnlyActivePrototypes)
	{
		super(c, useOnlyActivePrototypes);
		
		this.clusteringResult = new int[this.data.size()];
		for(int j=0; j<this.getDataCount(); j++) this.clusteringResult[j] = CrispClusteringAlgorithm.UNASSIGNED_INDEX;
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.PrototypeClusteringAlgorithm#initialize(java.util.Collection)
	 */
	@Override
	public void initializeWithPrototypes(Collection<Centroid<T>> initialPrototypes)
	{
		super.initializeWithPrototypes(initialPrototypes);
		
		this.recalculateClusterAssignments();
	}
	
	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.PrototypeClusteringAlgorithm#initialize(java.lang.Object)
	 */
	@Override
	public void initializeWithPositions(Collection<T> initialPrototypePositions)
	{
		super.initializeWithPositions(initialPrototypePositions);
		
		this.recalculateClusterAssignments();
	}
	
	/**
	 * 
	 */
	private void recalculateClusterAssignments()
	{
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
	 * @see datamining.clustering.AbstractDoubleArrayClusteringAlgorithm#algorithmName()
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
				
		for(t = 0; t < steps; t++)
		{
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
	 * @see datamining.clustering.CrispClusterResultAlgorithm#getCrispClusterAssignment(data.set.IndexedDataObject)
	 */
	@Override
	public int getCrispClusterAssignmentOf(IndexedDataObject<T> obj)
	{
		if(!this.initialized) throw new AlgorithmNotInitializedException("Prototypes not initialized.");
		
		return this.clusteringResult[obj.getID()];
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.CrispClusterResultAlgorithm#getCrispClusterAssignments()
	 */
	@Override
	public int[] getAllCrispClusterAssignments()
	{
		if(!this.initialized) throw new AlgorithmNotInitializedException("Prototypes not initialized.");
		
		return this.clusteringResult.clone();
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.CrispClusterResultAlgorithm#isCrispClusterAssigned(data.set.IndexedDataObject)
	 */
	@Override
	public boolean isCrispAssigned(IndexedDataObject<T> obj)
	{
		return this.initialized;
	}
}
