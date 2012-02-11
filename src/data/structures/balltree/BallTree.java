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


package data.structures.balltree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.PriorityQueue;

import data.algebra.Metric;
import data.set.DataSetNotSealedException;
import data.set.IndexedDataObject;
import data.set.IndexedDataSet;
import data.structures.AbstractTree;
import data.structures.DataStructureNotBuildException;
import data.structures.order.OrderedDataObject;
import data.structures.queries.KNNQueryProvider;
import data.structures.queries.SphereQueryProvider;

/**
 * The ball Tree is a data structure to organize data, based on the distance between pairs of data objects.
 * Each node of the tree can be regarded as sphere with a data object as centre and a radius (therefore, the name"Ball-Tree").
 * The radius is determined by the elements of the subtree of a node: The radius of a node is the maximum of all distances
 * to the data objects of its subtree. So the sphere is a convex hull of all data objects. Furthermore, the sphere of a node is 
 * the minimal bounding sphere, with the nodes element as centre, containing all subtree elements.<br>
 * 
 * The first data object of a node is stored in the left child node, the second in the right child node. All subsequent
 * data objects are stored in the subtree that is closest of the two child node data objects. So if a data object is
 * added to the local subtree, it is added to the left child node if the distance to the left data object is smaller
 * or equal than the distance to the right data object.<br>
 * 
 * Note that for this kind of tree, no vector space structure is required, it is enough to define a distance function
 * that satisfies the metric conditions in order to use a ball tree.<br>
 *
 * This of course, does not uniquely defines the tree structure. There are several buidling strategies, either by keeping the
 * Tree as flat as possible or by minimizing the total sphere volume. Usually, the ball tree is used in order to
 * store neighbourhood information. Because of that, it is not advisable to create a tree that is as balanced as
 * possible because it is very likely that a balanced tree does not reflect he true data distribution.<br>
 * 
 * See the following papers for more information:
 * <ul>
 * <li> Paper: Omohundro, S. M. Five Balltree Construction Algorithms International Computer Science Institute, 1989</li>
 * <li> Paper: Uhlmann, J. K. Satisfying general proximity / similarity queries with metric trees Information Processing Letters, 1991, 40, 175 - 179</li>
 * <li> Paper: Ciaccia, P.; Patella, M. & Zezula, P. M-tree: An Efficient Access Method for Similarity Search in Metric Spaces Proceedings of the 23rd International Conference on Very Large Data Bases, Morgan Kaufmann Publishers Inc., 1997, 426-435</li>
 * </ul>
 *
 * At moment only a very naive tree building is implemented: it simply dertermines the next child node by the
 * order of data objects. The tree construction time is in O(n log(n)). If the tree structure
 * reflects the underlying data distribution, it is likely that sphere queries have a complexity in O(k log(n)).
 * and kNN queries have a complexity in O(k log(k) log(n)). <br>
 *  
 * @TODO implement more building algorithms for ball trees. See Omohundros paper.
 * 
 * @author Roland Winkler
 */
public class BallTree<T> extends AbstractTree<T, BallTreeNode<T>, BallTree<T>> implements KNNQueryProvider<T>, SphereQueryProvider<T>
{
	
	/**  */
	private static final long	serialVersionUID	= 7686113033242605201L;
	
	/** The metric that is used for distance calculations. */
	protected final Metric<T> metric;
	
	/**
	 * Creates a new ball tree, with the specified data set and metric.
	 * 
	 * @param dataSet The data set underlying this tree
	 * @param distance The metric, used for this tree.
	 */
	public BallTree(IndexedDataSet<T> dataSet, Metric<T> distance)
	{
		super(dataSet);
		
		this.metric = distance;
	}
	
	/** 
	 * Build the tree. In this case, {@link #buildNaive()} is called. 
	 * 
	 * @see data.structures.AbstractTree#build()
	 * @see data.structures.balltree.BallTree#buildNaive()
	 */
	@Override
	public void build() throws DataSetNotSealedException
	{
		this.buildNaive();
	}
	
	/**
	 * A naive way of building the tree, adding data objects one by one, no optimization. The spheres
	 * are therefore determined by the order of the data objects in the data set. Note that the data set must be
	 * sealed in order to call this function.
	 * 
	 * @throws DataSetNotSealedException if the indexed data set was not sealed before trying to build the tree structure.
	 * 
	 */
	public void buildNaive() throws DataSetNotSealedException
	{
		if(!this.dataSet.isSealed()) throw new DataSetNotSealedException("The data set is not sealed.");
		if(this.isBuild()) this.clearBuild();
				
		Iterator<IndexedDataObject<T>> dataSetIterator = this.dataSet.iterator();
		IndexedDataObject<T> next;
		
		// if there are no data objects in the data set, do nothing.
		if(!dataSetIterator.hasNext()) return;
		
		if(this.root == null)
		{
			this.root = new BallTreeNode<T>(this, null, dataSetIterator.next());
		}
		
		while(dataSetIterator.hasNext())
		{
			next = dataSetIterator.next();
			this.root.addNaive(next, this.metric.distance(this.root.getObj().x, next.x));
		}

		this.build = true;
	}
	

	/**
	 * Performs a sphere query. For a tree structure, that is consistent with the neighbourhood structure of the data,
	 * the complexity is roughly O(k*log(n)) with k being the number or reported data objects.
	 * 
	 * @throws DataStructureNotBuildException if the data structure was not build before calling the function.
	 * 
	 * @see BallTreeNode#sphereQuery(Collection, Object, double)
	 */
	public Collection<IndexedDataObject<T>> sphereQuery(Collection<IndexedDataObject<T>> result, T centre, double radius) throws DataStructureNotBuildException
	{
		if(!this.build) throw new DataStructureNotBuildException("Data structure is not build.");
		
		if(result == null) result = new ArrayList<IndexedDataObject<T>>();
		
		this.root.sphereQuery(result, centre, radius);
		
		return result;
	}
	

	/**
	 * Performs a k-NN query. For a tree structure, that is consistent with the neighbourhood structure of the data,
	 * the complexity is roughly O(k log(k) log(n)). The term k log(k) comes from the fact that a sorted list of k elements
	 * needs to be available all the time. 
	 * 
	 * @throws DataStructureNotBuildException if the data structure was not build before calling the function.
	 * 
	 * @see BallTreeNode#kNNQuery(PriorityQueue, Object, int)
	 */
	public Collection<IndexedDataObject<T>> knnQuery(Collection<IndexedDataObject<T>> result, T centre, int k) throws DataStructureNotBuildException
	{
		if(!this.build) throw new DataStructureNotBuildException("Data structure is not build.");
		
		if(result == null) result = new ArrayList<IndexedDataObject<T>>(k);
		PriorityQueue<OrderedDataObject<T>> queue = new PriorityQueue<OrderedDataObject<T>>(k);
		ArrayList<IndexedDataObject<T>> reversedResult = new ArrayList<IndexedDataObject<T>>(k);
		
		// use negative distance values because the priority queue uses an ascending order and it is more
		// important to select the farthest element than it is to select the closest element. 
		for(int i=0; i<k; i++)
		{
			queue.add(new OrderedDataObject<T>(null, Double.NEGATIVE_INFINITY));
		}
		
		this.root.kNNQuery(queue, centre, k, this.metric.distance(this.root.getObj().x, centre));
		
		// reverse the order for returning the result so it is in ascending order w.r.t. to the distances.
		while(!queue.isEmpty()) reversedResult.add(queue.poll().dataObject);
		for(int i=reversedResult.size()-1; i>=0; i--) result.add(reversedResult.get(i));
			
		return result;
	}

	/**
	 * Returns the metric.
	 * 
	 * @return the metric
	 */
	public Metric<T> getMetric()
	{
		return this.metric;
	}

	/**
	 * Gets the list of radius values that are present in the nodes of the tree.
	 * 
	 * @return the list of radius values
	 * @throws DataStructureNotBuildException if the tree structure was not build
	 */
	public double[] radiusList() throws DataStructureNotBuildException
	{
		if(!this.build) throw new DataStructureNotBuildException("Data structure is not build.");
		
		double[] radiusList = new double[this.dataSet.size()];
		for(BallTreeNode<T> node:this.nodeList)
		{
			radiusList[node.getObj().getID()] = node.radius; 
		}
		
		return radiusList;
	}
}
