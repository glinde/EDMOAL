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
import java.util.Arrays;
import java.util.Collection;
import java.util.PriorityQueue;

import data.algebra.Metric;
import data.algebra.VectorSpace;
import data.set.DataSetNotSealedException;
import data.set.IndexedDataObject;
import data.set.IndexedDataSet;
import data.structures.AbstractTree;
import data.structures.DataStructureNotBuildException;
import data.structures.order.OrderedDataObject;
import data.structures.queries.KNNQueryProvider;
import data.structures.queries.SphereQueryProvider;

/**
 * The CenteredBallTree is similar to the {@link BallTree} in its structure and use. But it has one additional
 * property: It is also storing the mean of data objects of each local subtree. That of curse requires
 * a {@link VectorSpace} structure on the data set.<br>
 *
 * See the following papers for more information:
 * <ul>
 * <li> Paper: Omohundro, S. M. Five Balltree Construction Algorithms International Computer Science Institute, 1989
 * <li> Paper: Uhlmann, J. K. Satisfying general proximity / similarity queries with metric trees Information Processing Letters, 1991, 40, 175 - 179
 * <li> Paper: Winkler, R.; Klawonn, F. & Kruse, R. Problems of Fuzzy c-Means Clustering and Similar Algorithms with High Dimensional Data Sets Advances in Data Analysis, Data Handling and Business Intelligence, 2011
 * </ul>
 * 
 * @author Roland Winkler
 */
public class CenteredBallTree<T> extends AbstractTree<T, CenteredBallTreeNode<T>, CenteredBallTree<T>> implements KNNQueryProvider<T>, SphereQueryProvider<T>
{

	/**  */
	private static final long	serialVersionUID	= -1236444125166072711L;

	/**  */
	protected final VectorSpace<T> vectorSpace;
	
	/**  */
	protected final Metric<T> metric;

	/**
	 * Creates a new centered ball tree, with the specified data set and metric.
	 * 
	 * @param dataSet The data set underlying this tree
	 * @param distance The metric, used for this tree.
	 */
	public CenteredBallTree(IndexedDataSet<T> dataSet, VectorSpace<T> vectorSpace, Metric<T> distance)
	{
		super(dataSet);
		
		this.vectorSpace = vectorSpace;
		this.metric = distance;
	}

	/** 
	 * Build the tree. In this case, {@link #buildNaive()} is called. 
	 * 
	 * @see data.structures.AbstractTree#build()
	 * @see data.structures.balltree.CenteredBallTree#buildNaive()
	 */
	@Override
	public void build()
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

		ArrayList<IndexedDataObject<T>> subelements = new ArrayList<IndexedDataObject<T>>(this.dataSet.size());
		for(int i=1; i<this.dataSet.size(); i++) subelements.add(this.dataSet.get(i));

		this.root = new CenteredBallTreeNode<T>(this, null, this.dataSet.get(0));
		this.root.addNaive(subelements);
		
		this.build = true;
	}
	

	/**
	 * Performs a sphere query. For a tree structure, that is consistent with the neighbourhood structure of the data,
	 * the complexity is roughly O(k*log(n)) with k being the number or reported data objects.
	 * 
	 * @throws DataStructureNotBuildException if the data structure was not build before calling the function.
	 * 
	 * @see CenteredBallTreeNode#sphereQuery(Collection, Object, double)
	 */
	public Collection<IndexedDataObject<T>> sphereQuery(Collection<IndexedDataObject<T>> result, T centre, double radius)
	{
		if(!this.build) throw new DataStructureNotBuildException("Data structure is not build.");
		
		if(result == null) result = new ArrayList<IndexedDataObject<T>>();
		
		this.root.sphereQuery(result, centre, radius);
		
		return result;
	}
	

	/**
	 * Performs a k-NN query. For a tree structure, that is consistent with the neighbourhood structure of the data,
	 * the complexity is roughly O(k log(k) log(n)). The term k log(k) comes from the fact that a sorted list of k elements
	 * needs to be available all the time. This implementation might be slightly faster than the one from the BallTree.
	 * The recursion is here based on the information of the mean of a subtree instead of the location of the child
	 * node data objects. That might be a slight advantage. 
	 * 
	 * @throws DataStructureNotBuildException if the data structure was not build before calling the function.
	 * 
	 * @see CenteredBallTreeNode#kNNQuery(PriorityQueue, Object, int)
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
		
		this.root.kNNQuery(queue, centre, k);
		
		// reverse the order for returning the result so it is in ascending order w.r.t. to the distances.
		while(!queue.isEmpty()) reversedResult.add(queue.poll().dataObject);
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
	public double[] radiusList()
	{
		if(!this.build) throw new DataStructureNotBuildException("Data structure is not build.");
		
		double[] radiusList = new double[this.dataSet.size()];
		for(CenteredBallTreeNode<T> node:this.nodeList)
		{
			radiusList[node.getObj().getID()] = node.radius; 
		}
		
		return radiusList;
	}	
}
