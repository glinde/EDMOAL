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

import javax.xml.crypto.Data;

import data.algebra.Metric;
import data.algebra.VectorSpace;
import data.set.IndexedDataSet;
import data.structures.balltree.CenteredBallTree;
import data.structures.balltree.CenteredBallTreeNode;
import data.structures.queries.SphereQueryProvider;
import datamining.clustering.protoype.AbstractPrototypeClusteringAlgorithm;
import datamining.clustering.protoype.AlgorithmNotInitializedException;
import datamining.clustering.protoype.Centroid;
import etc.MyMath;

/**
 * This is an implementation of the standard Fuzzy c-Means clustering algorithm, except it uses a ball tree as data structure
 * and the neighborhood information contained in the tree to accelerate the calculation process. The acceleration
 * is accomplished by approximating chunks of the data by their center of mass. An additional parameter defines by what amount the
 * membership values may be uncertain due to the approximation. The approximation is only used for the iteration of the
 * algorithm, that is only for the optimization. Other fuctions like {@link #getFuzzyAssignmentsOf(data.set.IndexedDataObject)} do not
 * use the approximation. During the iteration, the approximation is extremely efficient, and if the number of data objects is
 * high enough, the execution time of the algorithm becomes independent on the number of data objects. A precision of 0.1 for membership values
 * seems to be quite crude, but the results show that there is only little difference to the movement of the prototypes compared to
 * the exact fuzzy c-means. There is one drawback however. The algorithm is performed recursively on the tree nodes. And for each
 * recursion, it has to be checked if an additional recursion is necessary due to the fact that the membership value interval is small
 * enough for a particular prototype. Calculating the membership value interval length for one prototype is in O(c) as the maximal and
 * minimal theoretical membership value has to be calculated and this value is dindividual for each prototype. Therefore, to check whether
 * or not to perform a recursion is in O(c^2). Also building the data structure takes O(n*log(n)).
 * See the papers for more information on the algorithm and the theory connected to it. <br> 
 * 
 * Paper: H�ppner, F. Speeding up fuzzy c-means: using a hierarchical data organisation to control the precision of membership calculation Fuzzy Sets and Systems, 2002, 128, 365 - 376
 * Paper: Winkler, R.; Klawonn, F.; H�ppner, F. & Kruse, R. A. Laurent, M.-J. L. (Ed.) Scalable Fuzzy Algorithms for Data Management and Analysis: Methods and Design Fuzzy Cluster Analysis of Larger Data Sets IGI Global: Information Science Reference, 2010, 302-331
 *
 * Of course, the membership matrix is not stored as this would demilish the advantage this algorithm provides w.r.t. the
 * normal implementation of FCM. The effectiveness of this algorithm depends very strongly on the number of data objects
 * and the number of clusters. for a small data set it is less effective than the standard FCM. For a large to huge data set, it
 * is much much more effective.<br> 
 *  
 * The runtime complexity of this algorithm is divided into two steps, the runtime of building the data structure prior to the clustering 
 * process and the clustering process it self. Building the data structure is in O(n*log(n)). The runtime of the algorithm depends on
 * the number of data objects and whether or not the approximation can effectively kick in. For a small number of data objects,
 * the algorithm is in O(t*n*c^2) and for a large number of data objects, the complexity is in O(t*N*c^2).
 * With t being the number of iterations, n being the number of data objects, N being the number of nodes
 * that have to be examined before the recursion is stopped and c being the number of clusters. For large n, N is constant and N<<n (yes,
 * I know the notation of N<<n is strange, but since n is the number of data objects everywhere else, bear with me in this case.)
 * This is, neglecting the runtime complexity of distance calculations and algebraic operations in the vector space.
 * The full complexity would be in O(t*n*c^2*(O(dist)+O(add)+O(mul))) and O(t*N*c^2*(O(dist)+O(add)+O(mul))) where O(dist) is the complexity of
 * calculating the distance between a data object and a prototype, O(add) is the complexity of calculating the
 * vector addition of two types <code>T</code> and O(mul) is the complexity of scalar multiplication of type <code>T</code>. <br>
 *  
 * The memory consumption of this algorithm is in O(t+n+c).
 *
 * @author Roland Winkler
 * 
 * @see data.structures.balltree.CenteredBallTree
 */
public class BallTreeFuzzyCMeansClusteringAlgorithm<T> extends FuzzyCMeansClusteringAlgorithm<T>
{
	/**  */
	private static final long	serialVersionUID	= -1692163192270100227L;

	/** The ball tree structure of the data set. */
	protected CenteredBallTree<T> cBallTree;

	/**
	 * The length of the membership value interval that limits the imprecision of the calculation.
	 * 
	 *	Range of values: 0 <= <code>maximalMembershipIntervalLength</code> <= 1
	 */
	protected double maximalMembershipIntervalLength;
	
	
	/** Internal variable, here as member to simplify the recursion. */
	private double[] membershipValueSum;
	/** Internal variable, here as member to simplify the recursion. */
	private double[] membershipValues;
	/** Internal variable, here as member to simplify the recursion. */
	private double[] prototypeDistances;
	/** Internal variable, here as member to simplify the recursion. */
	private double[] prototypeDistancesPow;
	/** Internal variable, here as member to simplify the recursion. */
	private double[] intervalLength;
	/** Internal variable, here as member to simplify the recursion. */
	private int[]    calculationDepth;
	/** Internal variable, here as member to simplify the recursion. */
	private ArrayList<T> newPrototypePosition; 

	/**
	 * Creates a new BallTreeFuzzyCMeansClusteringAlgorithm with the specified data set, vector space and metric.
	 * The prototypes are not initialized by this method, it has to be done separately.
	 * The metric must be differentiable w.r.t. <code>y</code> in <code>dist(x, y)<sup>2</sup></code>, and
	 * the directed differential in direction of <code>y</code> must yield <code>d/dy dist(x, y)^2 = 2(y - x)</code>
	 * for the algorithm to be correct. The CenteredBallTree is build automatically.
	 * 
	 * @param data The data set that should be clustered.
	 * @param vs The vector space that is used to calculate the prototype positions.
	 * @param metric The metric that is used to calculate the distance between data objects and prototypes.
	 */
	public BallTreeFuzzyCMeansClusteringAlgorithm(IndexedDataSet<T> dataSet, VectorSpace<T> vs, Metric<T> metric)
	{
		super(dataSet, vs, metric);
				
		this.cBallTree = new CenteredBallTree<T>(this.getDataSet(), this.vs, this.metric);
		this.cBallTree.build();
		
		this.maximalMembershipIntervalLength = 0.1d;
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
	 * This constructor creates a new FuzzyCMeansClusteringAlgorithm, taking an existing prototype clustering algorithm.
	 * It has the option to use only active prototypes from the old clustering algorithm. This constructor is especially
	 * useful if the clustering is done in multiple steps. The first clustering algorithm can for example calculate the
	 * initial positions of the prototypes for the second clustering algorithm. An other option is, that the first clustering
	 * algorithm creates a set of deactivated prototypes and the second clustering algorithm is initialized with less
	 * clusters than the first. The CenteredBallTree is build automatically.
	 * 
	 * @param c the elders clustering algorithm.
	 * @param useOnlyActivePrototypes States, that only prototypes that are active in the old clustering
	 * algorithm are used for the new clustering algorithm.
	 */
	public BallTreeFuzzyCMeansClusteringAlgorithm(AbstractPrototypeClusteringAlgorithm<T, Centroid<T>> c, boolean useOnlyActivePrototypes)
	{
		super(c, useOnlyActivePrototypes);

		this.cBallTree = new CenteredBallTree<T>(c.getDataSet(), this.vs, this.metric);
		this.cBallTree.build();
		
		this.maximalMembershipIntervalLength = 0.1d;
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
	 * @see datamining.clustering.protoype.altopt.FuzzyCMeansClusteringAlgorithm#algorithmName()
	 */
	@Override
	public String algorithmName()
	{
		return "Fuzzy c-Means Clustering Algorithm with Ball Tree Data Organization";
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
	 * Invokes the clustering for the specified node of the CenteredBallTree. All calculations w.r.t. this one node are done
	 * and if any of the prototypes requires a more precise calculation, this function is invoced recursively using its
	 * children.
	 * 
	 * @param node The node for which the calculation is done.
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
				this.prototypeDistances[i] = this.metric.distance(node.getCenterOfGravity(), this.prototypes.get(i).getPosition());
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
		
//		if(node.getObj().getID() == 1234) System.out.println("interval length = " + Arrays.toString(intervalLength));
				
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
		
//		System.out.print(this.algorithmName());
		long timeStart = System.currentTimeMillis();
		
		for(t = 0; t < steps; t++)
		{
//			System.out.print(".");
			
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
				
				doubleTMP = this.metric.distanceSq(this.prototypes.get(i).getPosition(), this.newPrototypePosition.get(i));
				
				maxPrototypeMovement = (doubleTMP > maxPrototypeMovement)? doubleTMP : maxPrototypeMovement;
				
				this.prototypes.get(i).moveTo(this.newPrototypePosition.get(i));
			}

			this.iterationComplete();
			
			if(this.iterationCount >= this.minIterations && maxPrototypeMovement < this.epsilon*this.epsilon) break;
		}

//		System.out.println(" done. [" + (System.currentTimeMillis() - timeStart) + "]");
	}


	/**
	 * Returns the maximal membership interval length.
	 * 
	 * @return the maximal membership interval length.
	 */
	public double getMaximalMembershipIntervalLength()
	{
		return this.maximalMembershipIntervalLength;
	}


	/**
	 * @param maximalMembershipIntervalLength the maximalMembershipIntervalLength to set
	 */
	/**
	 * Sets the maximal membership interval length. The range of the parameter is <code>0 <= maximalMembershipIntervalLength <= 1</code>.
	 *  
	 * @param maximalMembershipIntervalLength the maximal membership interval length to set.
	 */
	public void setMaximalMembershipIntervalLength(double maximalMembershipIntervalLength)
	{
		if(maximalMembershipIntervalLength < 0.0d || 1.0d < maximalMembershipIntervalLength)
			throw new IllegalArgumentException("The maximal membership interval length must be larger than 0 and smaller than 1. Specified maximal membership interval length: " + maximalMembershipIntervalLength);
		
		this.maximalMembershipIntervalLength = maximalMembershipIntervalLength;
	}


	/**
	 * returns te CenteredBallTree that is used in this clustering algorithm.
	 * 
	 * @return the cBallTree
	 */
	public CenteredBallTree<T> getCBallTree()
	{
		return this.cBallTree;
	}
	
	
}
