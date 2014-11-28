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
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;

import data.set.IndexedDataObject;
import data.structures.AbstractTreeNode;
import data.structures.order.OrderedDataObject;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class CenteredBallTreeNode<T> extends AbstractTreeNode<T, CenteredBallTreeNode<T>, CenteredBallTree<T>>
{	
	/**  */
	private static final long	serialVersionUID	= 6319433855471374954L;

	/** the center of gravity of the current subtree */
	protected T centerOfGravity;
	
	/** The distance from the center to the farthest child */
	protected double radius;
		

	/**
	 *  Constructs a new centered ball tree node with the specified tree, parent and data object. If the parent is
	 *  unspecified (i.e. if it is <code>null</code>), the node is the root of the tree.
	 * 
	 * @param tree The tree this node belongs to
	 * @param parent The parent of the node. If it is <code>null</code>, the node is the root of the tree.
	 * @param obj The data object, stored in the node
	 */
	protected CenteredBallTreeNode(CenteredBallTree<T> tree, CenteredBallTreeNode<T> parent, IndexedDataObject<T> obj)
	{
		super(tree, parent, obj);

		this.centerOfGravity = this.tree.vectorSpace.copyNew(obj.x); 
		this.radius = 0.0d;
	}

	/**
	 * Adds a data set to build the tree.
	 * The first data object of <code>subtreeElements</code> is stored in the left child node.
	 * The second data object of <code>subtreeElements</code> is stored in the right child node.
	 * All other data objects are stored in the respective subtree with the closer centre.<br>
	 * 
	 * This implementation is significantly different to the one in {@link BallTreeNode}. This implementation
	 * requires more organizing time, but it provides a consistancy for the local centre of gravity.
	 * @TODO: make a faster implementation by copying the implementation of {@link BallTreeNode} and
	 * than recursivly build the centre of gravity for each node. It is possible using the combination
	 * of the subtree size and its local centre of gravity.
	 * 
	 * @param subtreeElements The elements of the local subtree
	 */
	protected void addNaive(Collection<IndexedDataObject<T>> subtreeElements)
	{
		// primarily because the radius calculation requires the knowledge of the complete subtree.
		// dynamic adding of elements is not allowed by the tree anyway, so therefore the time consuming
		// calculation of calculating of the radius is omitted for dynamic adding of elements.
		if(this.size != 1) throw new IllegalArgumentException("Dynamic adding of elements is not supported by this method. It should only be called once!");
		
		ArrayList<IndexedDataObject<T>> leftSubtreeElements = new ArrayList<IndexedDataObject<T>>(subtreeElements.size());
		ArrayList<IndexedDataObject<T>> rightSubtreeElements = new ArrayList<IndexedDataObject<T>>(subtreeElements.size());		
		IndexedDataObject<T> left = null, right = null;
		double distToLeft = 0.0d, distToRight = 0.0d, distToCenter = 0.0d;
		
		for(IndexedDataObject<T> d:subtreeElements)
		{
			// if the new data object is equivalent to this one
			if(this.tree.metric.distance(this.obj.x, d.x) == 0.0d)
			{
				// if this data object is this data object
				if(this.obj.equals(d)) throw new IllegalArgumentException("Data Object multiple times added! id: " + this.obj.getID());
				
				// if there are already equivalent data objects, check them all
				if(this.equivalents != null)
				{
					// if the data object is contained in equivalents, return
					if(this.equivalents.contains(d)) throw new IllegalArgumentException("Data Object multiple times added! id: " + d.getID());
				}
				else // no equivalents have been added so far, therefore create equivalents, add the data object and return
				{
					this.equivalents = new HashSet<IndexedDataObject<T>>();
				}
	
				// if the data object is not contained in equivalents, add it to equivalents and take t into account for the center calculation.
				this.addEquivalent(d);
				this.tree.vectorSpace.add(this.centerOfGravity, d.x);
			}
			else
			{
				this.size++;
				this.tree.vectorSpace.add(this.centerOfGravity, d.x);
			
				// no childs so far
				if(left==null)
				{
					left = d;
					continue;
				}
				else
				{
					distToLeft = this.tree.metric.distance(left.x, d.x);
				}
				
				// in case the element is equivalent to the left child, no right child should be generated
				if(distToLeft == 0.0d)
				{
					leftSubtreeElements.add(d);
					continue;
				}
				
				// only one child so far
				if(right==null) 
				{
					right = d;
					continue;
				}
				else
				{
					distToRight = this.tree.metric.distance(right.x, d.x);
				}
				
				// the node has two childs and the elements of the list must be put into the subtree according to their distance
				if(distToLeft <= distToRight) leftSubtreeElements.add(d);
				else  rightSubtreeElements.add(d);
			}
		}

		// all subtree elements are considered for the center of gravity
		this.tree.vectorSpace.mul(this.centerOfGravity, 1.0d/((double)this.size));
		
		// calculate the radius of the node
		// the radius is the distance from the this.center to the furthest away subtree element
		this.radius = this.tree.metric.distanceSq(this.centerOfGravity, this.obj.x);
		for(IndexedDataObject<T> d:subtreeElements)
		{
			distToCenter = this.tree.metric.distanceSq(this.centerOfGravity, d.x);
			if(this.radius < distToCenter) this.radius = distToCenter;
		}
		this.radius = Math.sqrt(this.radius);
		
		
		// information regarding this node have been processed. Proceed with recursion.
		if(left != null)
		{
			this.leftChild = new CenteredBallTreeNode<T>(this.tree, this, left);
			this.leftChild.addNaive(leftSubtreeElements);
			this.height = this.leftChild.height + 1;
		}
		// recursion with the right child
		if(right != null)
		{
			this.rightChild = new CenteredBallTreeNode<T>(this.tree, this, right);
			this.rightChild.addNaive(rightSubtreeElements);
			this.height = Math.max(this.leftChild.getHeight(), this.rightChild.getHeight()) + 1;
		}
	}

	/**
	 * Recursively performs a sphere query on the local subtree.<br>
	 * 
	 * Collects all data objects inside the hypersphere defined by <code>queryCentre</code> and <code>queryRadius</code> and stores
	 * the result in the specified collection.
	 * The function is called recursively for the child nodes if that is necessary.
	 * 
	 * @param result the result collection
	 * @param centqueryCenterre the centre of the query
	 * @param queryRadius the square radius of the query
	 */
	public void sphereQuery(Collection<IndexedDataObject<T>> result, T queryCenter, double queryRadius)
	{
		double distqueryToCoG = this.tree.getMetric().distance(this.centerOfGravity, queryCenter);
		
		// if the query ball does not intersects the local subset ball, do nothing
		if(distqueryToCoG > queryRadius + this.getRadius()) return;
		
		// if the query covers the local subset ball completely, report all data objects of this subtree and return
		if(distqueryToCoG + this.getRadius() < queryRadius)
		{
			this.collectSubtreeElements(result);
			return;
		}

		// if the local element is inside the query sphere
		if(this.tree.getMetric().distance(this.obj.x, queryCenter) < queryRadius)
		{
			result.add(this.obj);
			if(this.equivalents != null) result.addAll(this.equivalents);
		}

		// the query sphere intersects with the local subset, use recursion
		if(this.leftChild != null) this.leftChild.sphereQuery(result, queryCenter, queryRadius);
		if(this.rightChild != null) this.rightChild.sphereQuery(result, queryCenter, queryRadius);
	}

	/**
	 * Recursively performs a k-NN query on the local subtree.<br>
	 * 
	 * A {@link PriorityQueue} is used to find the <code>k</code> data objects that are closest the query.
	 * The centre and radius information of the nodes are used to prune the tree and only go deaper in the tree
	 * if that is necessary. That is, if the current k-furthest data object is further away than the distance
	 * to the child minus its radius. The traversation is performed with the child node that is closer to the
	 * query first to increase the chance of pruning the subtree of the right child node.
	 * 
	 * @param queue The {@link PriorityQueue} that contains the current k-closest data objects.
	 * @param query The query object.
	 * @param k The number of data objects to be reported.
	 * @param distanceX The distance of the data object of this node to the query data object.
	 */
	public void kNNQuery(PriorityQueue<OrderedDataObject<T>> queue, T query, int k)
	{
		OrderedDataObject<T> tmpOrderedObject;
		double distanceX = this.tree.metric.distance(this.obj.x, query);
		double distanceA, distanceB;
		
		// if this.obj is closer to the query than previously found data objects
		if(distanceX < -queue.peek().compare)
		{
			tmpOrderedObject = new OrderedDataObject<T>(this.obj, -distanceX);
			queue.poll();
			queue.add(tmpOrderedObject);
			
			// fill up with equivalents
			if(this.equivalents != null)
			{		
				Iterator<IndexedDataObject<T>> iter = this.equivalents.iterator();
				while(iter.hasNext() && distanceX < -queue.peek().compare)
				{
					tmpOrderedObject = new OrderedDataObject<T>(iter.next(), -distanceX);
					queue.poll();
					queue.add(tmpOrderedObject);
				}
			}
		}
		
		// invoke recursion in cases where it is necessary
		
		// the node is a leaf: stop
		if(this.leftChild == null) return;
		
		// if rightChild is null and leftChild is not, only test leftChild for recursion
		if(this.rightChild == null)
		{
			distanceA = this.tree.getMetric().distance(this.leftChild.centerOfGravity, query);
			
			// if the farthest so far observed data object is further away than the closest potential data object from the child, do recursion.
			if(distanceA - this.leftChild.radius < -queue.peek().compare) this.leftChild.kNNQuery(queue, query, k);
			
			return;
		}
		
		// this node has two childs, recurse with the one closer to the data object first, then with the other.
		distanceA = this.tree.getMetric().distance(this.leftChild.centerOfGravity, query);
		distanceB = this.tree.getMetric().distance(this.rightChild.centerOfGravity, query);
		
		// start recursion with the closer child node but check for both if a recursion is really necessary
		if(distanceA <= distanceB)
		{
			if(distanceA - this.leftChild.radius < -queue.peek().compare) this.leftChild.kNNQuery(queue, query, k);
			if(distanceB - this.rightChild.radius < -queue.peek().compare) this.rightChild.kNNQuery(queue, query, k);
		}
		else
		{
			if(distanceB - this.rightChild.radius < -queue.peek().compare) this.rightChild.kNNQuery(queue, query, k);
			if(distanceA - this.leftChild.radius < -queue.peek().compare) this.leftChild.kNNQuery(queue, query, k);
		}
	}

	/**
	 * Returns the radius of the node.
	 * 
	 * @return the radius
	 */
	public double getRadius()
	{
		return this.radius;
	}

	/**
	 * Returns the center of gravity of the local subtree.
	 * 
	 * @return the center of gravity of the local subtree.
	 */
	public T getCenterOfGravity()
	{
		return this.centerOfGravity;
	}
}
