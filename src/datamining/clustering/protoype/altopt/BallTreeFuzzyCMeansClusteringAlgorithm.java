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
import data.set.IndexedDataSet;
import data.structures.balltree.CenteredBallTree;
import data.structures.balltree.CenteredBallTreeNode;
import data.structures.queries.SphereQueryProvider;
import datamining.clustering.protoype.AlgorithmNotInitializedException;
import datamining.clustering.protoype.Centroid;
import etc.MyMath;

/**
 * TODO Class Description
 * 
 * Paper: Höppner, F. Speeding up fuzzy c-means: using a hierarchical data organisation to control the precision of membership calculation Fuzzy Sets and Systems, 2002, 128, 365 - 376
 * Paper: Winkler, R.; Klawonn, F.; Höppner, F. & Kruse, R. A. Laurent, M.-J. L. (Ed.) Scalable Fuzzy Algorithms for Data Management and Analysis: Methods and Design Fuzzy Cluster Analysis of Larger Data Sets IGI Global: Information Science Reference, 2010, 302-331
 *
 * @author Roland Winkler
 */
public class BallTreeFuzzyCMeansClusteringAlgorithm<T> extends FuzzyCMeansClusteringAlgorithm<T>
{
	/**  */
	private static final long	serialVersionUID	= -1692163192270100227L;

	protected CenteredBallTree<T> cBallTree;

	protected double maximalMembershipIntervalLength;
	
	
	private double[] membershipValueSum;
	private double[] membershipValues;
	private double[] prototypeDistances;
	private double[] prototypeDistancesPow;
	private double[] intervalLength;
	private int[]    calculationDepth;
	private ArrayList<T> newPrototypePosition; 
	
	/**
	 * 
	 */
	public BallTreeFuzzyCMeansClusteringAlgorithm(IndexedDataSet<T> dataSet, VectorSpace<T> vs, Metric<T> dist)
	{
		super(dataSet, vs, dist);
				
		this.cBallTree = new CenteredBallTree<T>(dataSet, this.vs, this.dist);
		this.cBallTree.build();
		
		this.maximalMembershipIntervalLength = 0.0d;
		this.membershipValueSum = new double[this.prototypes.size()];
		this.membershipValues =  new double[this.prototypes.size()];
		this.prototypeDistances = new double[this.prototypes.size()];
		this.prototypeDistancesPow = new double[this.prototypes.size()];
		this.intervalLength = new double[this.prototypes.size()];
		this.calculationDepth = new int[this.prototypes.size()];
		
		this.newPrototypePosition = new ArrayList<T>(this.getClusterCount());
		for(int i=0; i<this.getClusterCount(); i++) this.newPrototypePosition.add(this.vs.getNewAddNeutralElement()); 
	}

//	is inserted in recursiveClustering for variable sharing		
//	/**
//	 * @param node
//	 * @param intervalLength
//	 * @param calculationDepth
//	 */
//	protected void calculateMembershipIntervalLength(CenteredBallTreeNode<T> node)
//	{
//		double tmp = 0.0d;
//		double min = 1.0d, max = 1.0d;
//		int i=0, j=0;
//		double exponent = 2.0d/(1.0d-this.fuzzifier);
//		int distancesBelowRadius = 0;
//		int lastDistBelowRadiusIndex = -1;
//				
//		for(i=0; i<this.prototypes.size(); i++)
//		{
//			if(this.prototypeDistances[i] <= node.getRadius())
//			{
//				distancesBelowRadius++;
//				lastDistBelowRadiusIndex = i;
//			}
//		}
//
//		for(i=0; i<this.prototypes.size(); i++)
//		{
//			if(calculationDepth[i] < node.getDepth()) continue;
//
//			tmp = 1.0d/(this.prototypeDistances[i] + node.getRadius());
//
//			if (distancesBelowRadius > 1 || (distancesBelowRadius == 1 && lastDistBelowRadiusIndex != i))
//			{
//				min = 0.0d;
//			}
//			else
//			{
//				min = 1.0d;
//				for(j=0; j<this.prototypes.size(); j++)
//				{
//					if(j==i) continue;
//					min += MyMath.pow((this.prototypeDistances[j] - node.getRadius())*tmp, exponent);
//				}
//				
//				min = 1.0d/min;
//			}
//
//			if(this.prototypeDistances[i] <= node.getRadius())
//			{
//				max = 1.0d;
//			}
//			else
//			{
//				tmp = 1.0d/(this.prototypeDistances[i] - node.getRadius());
//				max = 1.0d;
//				for(j=0; j<this.prototypes.size(); j++)
//				{
//					if(j==i) continue;
//					max += MyMath.pow((this.prototypeDistances[j] + node.getRadius())*tmp, exponent);
//				}
//				
//				max = 1.0d/max;
//			}
//			
//			this.intervalLength[i] = max - min;
//		}
//	}
	
	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.altopt.FuzzyCMeansClusteringAlgorithm#algorithmName()
	 */
	@Override
	public String algorithmName()
	{
		return "";
	}



	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.AbstractCentroidClusteringAlgorithm#initializeWithPositions(java.util.Collection)
	 */
	@Override
	public void initializeWithPositions(Collection<T> initialPrototypePositions)
	{
		super.initializeWithPositions(initialPrototypePositions);

		this.membershipValueSum = new double[this.prototypes.size()];
		this.membershipValues =  new double[this.prototypes.size()];
		this.prototypeDistances = new double[this.prototypes.size()];
		this.prototypeDistancesPow = new double[this.prototypes.size()];
		this.intervalLength = new double[this.prototypes.size()];
		this.calculationDepth = new int[this.prototypes.size()];
		
		this.newPrototypePosition = new ArrayList<T>(this.getClusterCount());
		for(int i=0; i<this.getClusterCount(); i++) this.newPrototypePosition.add(this.vs.getNewAddNeutralElement()); 
	}



	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.AbstractPrototypeClusteringAlgorithm#initializeWithPrototypes(java.util.Collection)
	 */
	@Override
	public void initializeWithPrototypes(Collection<Centroid<T>> initialPrototypes)
	{
		super.initializeWithPrototypes(initialPrototypes);

		this.membershipValueSum = new double[this.prototypes.size()];
		this.membershipValues =  new double[this.prototypes.size()];
		this.prototypeDistances = new double[this.prototypes.size()];
		this.prototypeDistancesPow = new double[this.prototypes.size()];
		this.intervalLength = new double[this.prototypes.size()];
		this.calculationDepth = new int[this.prototypes.size()];
		
		this.newPrototypePosition = new ArrayList<T>(this.getClusterCount());
		for(int i=0; i<this.getClusterCount(); i++) this.newPrototypePosition.add(this.vs.getNewAddNeutralElement()); 
	}


	/**
	 * @param node
	 */
	protected void recursiveClustering(CenteredBallTreeNode<T> node)
	{
		int i, k;
		double exponent = 2.0d/(1.0d - this.fuzzifier);
		double distanceSum = 0.0d;
		boolean recurse = false;
		double min, max, tmp;
		int distancesBelowRadius, lastDistBelowRadiusIndex;
		T tmpX = this.vs.getNewAddNeutralElement();
		
		// calculate distances and powered distances from the prototypes to and the data object from node where necessary.
		// also calculate the membership values.
		for(i=0; i<this.getClusterCount(); i++)
		{
			if(this.calculationDepth[i] >= node.getDepth())
			{
				this.prototypeDistances[i] = this.dist.distance(node.getCenterOfGravity(), this.prototypes.get(i).getPosition());
				// TODO: insert proper 0-distances handling. the current implementation uses numeric errors for correct calculation.
				if(this.prototypeDistances[i] > 0.0d) 
				{
					this.prototypeDistancesPow[i] = MyMath.pow(this.prototypeDistances[i], exponent);
				}
				else
				{
					this.prototypeDistancesPow[i] = 0.001*Double.MAX_VALUE/this.getClusterCount();
				}
			}

			distanceSum += this.prototypeDistancesPow[i];
		}
		distanceSum = 1.0d / distanceSum;
		for(i=0; i<this.getClusterCount(); i++)
		{
			if(this.calculationDepth[i] == node.getDepth())	this.membershipValues[i] = MyMath.pow(this.prototypeDistancesPow[i] * distanceSum, this.fuzzifier);
		}
		
		// if the node is a leaf, no further recursion must occur, therefore, calculate everything regardless the membership intervall length
		if(node.isLeaf())
		{
			for(i=0; i<this.getClusterCount(); i++)
			{
				if(this.calculationDepth[i] == node.getDepth())
				{
					this.membershipValueSum[i] += this.membershipValues[i];

					this.vs.copy(tmpX, node.getObj().x);
					this.vs.mul(tmpX, this.membershipValues[i]);
					this.vs.add(this.newPrototypePosition.get(i), tmpX);
				}
			}
			
			return;
		}

		// calculate the interval length for those prototypes, that needed further precision at the last recursion		
		distancesBelowRadius = 0;
		lastDistBelowRadiusIndex = -1;
		for(i=0; i<this.getClusterCount(); i++)
		{
			if(this.prototypeDistances[i] <= node.getRadius())
			{
				distancesBelowRadius++;
				lastDistBelowRadiusIndex = i;
			}
		}

		for(i=0; i<this.getClusterCount(); i++)
		{
			if(this.calculationDepth[i] < node.getDepth()) continue;

			tmp = 1.0d/(this.prototypeDistances[i] + node.getRadius());

			if (distancesBelowRadius > 1 || (distancesBelowRadius == 1 && lastDistBelowRadiusIndex != i))
			{
				min = 0.0d;
			}
			else
			{
				min = 1.0d;
				for(k=0; k<this.getClusterCount(); k++)
				{
					if(k==i) continue;
					min += MyMath.pow((this.prototypeDistances[k] - node.getRadius())*tmp, exponent);
				}
				
				min = 1.0d/min;
			}

			if(this.prototypeDistances[i] <= node.getRadius())
			{
				max = 1.0d;
			}
			else
			{
				tmp = 1.0d/(this.prototypeDistances[i] - node.getRadius());
				max = 1.0d;
				for(k=0; k<this.getClusterCount(); k++)
				{
					if(k==i) continue;
					max += MyMath.pow((this.prototypeDistances[k] + node.getRadius())*tmp, exponent);
				}
				
				max = 1.0d/max;
			}
			
			this.intervalLength[i] = max - min;
		}
				
		// calculate the prototypes for which a recursion is necessary
		for(i=0; i<this.calculationDepth.length; i++)
		{
			// which prototypes even more precision? 
			if(this.calculationDepth[i] == node.getDepth())
			{
				if(intervalLength[i] > this.maximalMembershipIntervalLength)
				{
					this.calculationDepth[i]++;
					recurse = true;
				}
			}
			
			// calculate new prototype positions for those prototypes that needed more precision in this recursion, but not in the next
			if(this.calculationDepth[i] == node.getDepth())
			{
				this.membershipValueSum[i] += node.getSize() * this.membershipValues[i];

				this.vs.copy(tmpX, node.getCenterOfGravity());
				this.vs.mul(tmpX, this.membershipValues[i]* node.getSize());
				this.vs.add(this.newPrototypePosition.get(i), tmpX);
			}
			// for those prototypes that need more precision, calculate new prototype positions only for the data object of the current node
			else if(this.calculationDepth[i] > node.getDepth())
			{
				this.membershipValueSum[i] += this.membershipValues[i];

				this.vs.copy(tmpX, node.getObj().x);
				this.vs.mul(tmpX, this.membershipValues[i]);
				this.vs.add(this.newPrototypePosition.get(i), tmpX);
			}
		}
		
		
		// if at least one prototype needed more precision, invoke recursion
		if(recurse)
		{
			if(node.getLeftChild() != null) this.recursiveClustering(node.getLeftChild());
			if(node.getRightChild() != null) this.recursiveClustering(node.getRightChild());
			
			// re-adjust the calculation depth because it is a global parameter
			for(i=0; i<this.calculationDepth.length; i++)
			{
				if(this.calculationDepth[i] > node.getDepth())
				{
					this.calculationDepth[i]--;
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.altopt.FuzzyCMeansClusteringAlgorithm#apply(int)
	 */
	@Override
	public void apply(int steps)
	{
		if(!this.initialized) throw new AlgorithmNotInitializedException("Prototypes not initialized.");	
		
		int i, t;
 
		double doubleTMP = 0.0d;									// a temporarly variable for multiple perpuses
		double maxPrototypeMovement;
		
		for(t = 0; t < steps; t++)
		{
			maxPrototypeMovement = 0.0d;
			//System.out.println("");
			for(i=0; i<this.getClusterCount(); i++)
			{
				this.calculationDepth[i] = 0;
				this.membershipValueSum[i] = 0.0d;
				this.membershipValues[i] = 0.0d;
				this.vs.resetToAddNeutralElement(this.newPrototypePosition.get(i));				
			}
			
			this.recursiveClustering(this.cBallTree.getRoot());

			// update prototype positions
			for(i = 0; i < this.getClusterCount(); i++)
			{
				doubleTMP = 1.0d/this.membershipValueSum[i];
				this.vs.mul(this.newPrototypePosition.get(i), doubleTMP);
			}
			
			// copy new prototype values into prototypes wrt. learning factor
			for(i = 0; i < this.getClusterCount(); i++)
			{
				if(Math.abs(this.learningFactor - 1.0d) > 0.01d)
				{
					this.vs.sub(this.newPrototypePosition.get(i), this.prototypes.get(i).getPosition());
					this.vs.mul(this.newPrototypePosition.get(i), this.learningFactor);
					this.vs.add(this.newPrototypePosition.get(i), this.prototypes.get(i).getPosition());	
				}
				
				doubleTMP = this.dist.distanceSq(this.prototypes.get(i).getPosition(), this.newPrototypePosition.get(i));
				
				maxPrototypeMovement = (doubleTMP > maxPrototypeMovement)? doubleTMP : maxPrototypeMovement;
				
				this.prototypes.get(i).moveTo(this.newPrototypePosition.get(i));
			}

			this.iterationComplete();
			
			if(maxPrototypeMovement < this.epsilon*this.epsilon) break;
		}
	}


	// TODO: override at lest some functions from the original fuzzy clustering algorithm to reflect the faster calculation
	
	/**
	 * @return the maximalMembershipIntervalLength
	 */
	public double getMaximalMembershipIntervalLength()
	{
		return this.maximalMembershipIntervalLength;
	}


	/**
	 * @param maximalMembershipIntervalLength the maximalMembershipIntervalLength to set
	 */
	public void setMaximalMembershipIntervalLength(double maximalMembershipIntervalLength)
	{
		this.maximalMembershipIntervalLength = maximalMembershipIntervalLength;
	}


	/**
	 * @return the cBallTree
	 */
	public CenteredBallTree<T> getCBallTree()
	{
		return this.cBallTree;
	}
	
	
}
