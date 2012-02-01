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
public class BallTreeNode<T> extends AbstractTreeNode<T, BallTreeNode<T>, BallTree<T>>
{	
	/**  */
	private static final long	serialVersionUID	= 2221604637270850625L;
	/** The distance to the farthest child */
	protected double radius;
		
	/**
	 * 
	 */
	protected BallTreeNode(BallTree<T> tree, BallTreeNode<T> parent, IndexedDataObject<T> obj)
	{
		super(tree, parent, obj);
		
		this.radius = 0.0d;
	}
	
	/** adds new data objects recursively
	 * @param dataObj
	 * @param distToObj
	 */
	protected void addNaive(IndexedDataObject<T> dataObj, double distToObj)
	{
		// if the new data object is equivalent to this one
		if(distToObj == 0.0d)
		{
			// if this data object is this data object
			if(this.obj == dataObj)	throw new IllegalArgumentException("Data Object multiple times added! id: " + this.obj.getID());
			
			// if there are already equivalent data objects, check them all
			if(this.equivalents != null)
			{
				// if the data object is contained in equivalents, return
				if(this.equivalents.contains(dataObj)) throw new IllegalArgumentException("Data Object multiple times added! id: " + this.obj.getID());
			}
			else // no equivalents have been added so far, therefore create equivalents, add the data object and return
			{
				this.equivalents = new HashSet<IndexedDataObject<T>>();
			}

			// if the data object is not contained in equivalents, add it to equivalents and return
			this.size++;
			this.equivalents.add(dataObj);
			return;
		}
		
		
		// update subtree-information
		this.size++;
		double distanceA, distanceB;
		
		// update the radius
		this.radius = (distToObj>=this.radius)? distToObj:this.radius;
				
		// this node has no childs. Build a new child and stop recursion
		if(this.leftChild == null)
		{
			this.leftChild = new BallTreeNode<T>(this.tree, this, dataObj);
			this.height = this.leftChild.getHeight() + 1;
			return;
		}
		
		// this node has only one child, build the second child and stop recursion
		if(this.rightChild == null)
		{
			this.rightChild = new BallTreeNode<T>(this.tree, this, dataObj);
			this.height = this.rightChild.getHeight() + 1;
			return;
		}
		
		// this node has two childs and the data object needs to be put in the subtree with closest local root.
		distanceA = this.tree.getDistanceFunction().distance(this.leftChild.getObj().element, dataObj.element);
		distanceB = this.tree.getDistanceFunction().distance(this.rightChild.getObj().element, dataObj.element);
		
		if(distanceA <= distanceB) this.leftChild.addNaive(dataObj, distanceA);
		else					   this.rightChild.addNaive(dataObj, distanceB);
		
		this.height = Math.max(this.leftChild.getHeight(), this.rightChild.getHeight()) + 1;
	}
	
	/**
	 * Collects all data objects inside the hypersphere defined by queryRadius of centre.
	 * 
	 * @param result the result
	 * @param centqueryCenterre the centre of the query
	 * @param queryRadius the square radius of the query
	 */
	public void sphereQuery(Collection<IndexedDataObject<T>> result, T queryCenter, double queryRadius)
	{
		double distToCentre = this.tree.getDistanceFunction().distance(this.obj.element, queryCenter);
		
		// if the query ball does not intersects the local subset ball, do nothing
		if(distToCentre > queryRadius + this.getRadius()) return;
		
		// if the query covers the local subset ball completely, report all data objects of this subtree and return
		if(distToCentre + this.getRadius() < queryRadius)
		{
			this.collectSubtreeElements(result);
			return;
		}

		// if this data object is inside the query sphere
		if(distToCentre < queryRadius)
		{
			result.add(this.obj);
			if(this.equivalents != null) result.addAll(this.equivalents);
		}

		// the query sphere intersects with the local subset, use recursion
		if(this.leftChild != null) this.leftChild.sphereQuery(result, queryCenter, queryRadius);
		if(this.rightChild != null) this.rightChild.sphereQuery(result, queryCenter, queryRadius);
	}
	
	/**
	 * @param result
	 * @param centre
	 * @param k
	 */
	public void kNNQuery(PriorityQueue<OrderedDataObject<T>> queue, T query, int k, double distanceX)
	{
		OrderedDataObject<T> tmpOrderedObject;
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
			distanceA = this.tree.getDistanceFunction().distance(this.leftChild.getObj().element, query);
			
			// if the farthest so far observed data object is further away than the closest potential data object from the child, do recursion.
			if(distanceA - this.leftChild.radius < -queue.peek().compare) this.leftChild.kNNQuery(queue, query, k, distanceA);
			
			return;
		}

		// this node has two childs, recurse with the one closer to the data object first, then with the other.
		distanceA = this.tree.getDistanceFunction().distance(this.leftChild.getObj().element, query);
		distanceB = this.tree.getDistanceFunction().distance(this.rightChild.getObj().element, query);
		
		// start recursion with the closer child node but check for both if a recursion is really necessary
		if(distanceA <= distanceB)
		{
			if(distanceA - this.leftChild.radius < -queue.peek().compare) this.leftChild.kNNQuery(queue, query, k, distanceA);
			if(distanceB - this.rightChild.radius < -queue.peek().compare) this.rightChild.kNNQuery(queue, query, k, distanceB);
		}
		else
		{
			if(distanceB - this.rightChild.radius < -queue.peek().compare) this.rightChild.kNNQuery(queue, query, k, distanceB);
			if(distanceA - this.leftChild.radius < -queue.peek().compare) this.leftChild.kNNQuery(queue, query, k, distanceA);
		}
		
	}

	/**
	 * @return the radius
	 */
	public double getRadius()
	{
		return this.radius;
	}
}
