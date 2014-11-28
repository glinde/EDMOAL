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


package data.structures;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import data.set.IndexedDataObject;
import data.structures.balltree.BallTree;
import data.structures.balltree.BallTreeNode;

/**
 * An abstract implementation of the tree node. All information and some basic recursive algorithms
 * for analysing the tree is implemented. <br>
 * 
 * The rather complicated construction of generic variables is done in order to allow a variable combination of
 * Tree classes and Node classes. This complicated generic structure should be masked for any non-abstract
 * implementation of the tree structure, as it is done for example in the {@link BallTree} and {@link BallTreeNode}.<br>
 * 
 * This class also provides a special property in dealing with data objects that are equivalent to others.
 * Usually, for each data object, a node is represented in the tree. This is possible with this
 * tree structure, but also an alternative is provided. If two data objects are indistinguishable using
 * their data property (i.e. they are equal to any data analysis algorithm), they can be stored in the same
 * node. In this case, the data objects are stored in a HashSet, with the index of the {@link IndexedDataObject} as
 * hash key. That way, it is possible to handle large amounts of equivalent data objects without any impact
 * on the trees performance.
 *
 * @author Roland Winkler
 * 
 * @see AbstractTree
 */
public class AbstractTreeNode<E, N extends AbstractTreeNode<E, N, T>, T extends AbstractTree<E, N, T>> implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= 6512063591779793509L;

	/** the data object of this node */
	protected final IndexedDataObject<E> obj;
	
	/** The associated tree */
	protected final T tree;
	
	/** The parent of the node. If it is null, this node is the root node */
	protected final N parent;
	
	/** A list of data object elements that are equivalent to this.obj.element.
	 * That is, all objects that have a distance of 0 to this.obj.
	 * The reference to this object will remain null until the first equivalent data object is added. */
	protected HashSet<IndexedDataObject<E>> equivalents;
	
	/** left child node */
	protected N leftChild;
	
	/** right child node */
	protected N rightChild;
	
	/** The number of data objects in this subtree (including this.obj) */
	protected int size;	
	
	/** The height of the current subtree */
	protected int height;
	
	/** The number of parents above to the root */
	protected int depth;
		
	/**
	 *  Constructs a new tree node with the specified tree, parent and data object. If the parent is
	 *  unspecified (i.e. if it is <code>null</code>), the node is the root of the tree.
	 * 
	 * @param tree The tree this node belongs to
	 * @param parent The parent of the node. If it is <code>null</code>, the node is the root of the tree.
	 * @param obj The data object, stored in the node
	 */
	@SuppressWarnings("unchecked")
	protected AbstractTreeNode(T tree, N parent, IndexedDataObject<E> obj)
	{
		this.obj = obj;
		this.tree = tree;
		this.parent = parent;
		
		this.equivalents = null;
		this.leftChild = null;
		this.rightChild = null;
		this.height = 0;

		this.depth = (this.parent == null)? 0 : (this.parent.getDepth()+1);
		this.size = (this.obj == null)? 0:1;
		if(this.obj != null)
		{
			this.tree.nodeList.set(this.obj.getID(), (N) this);
		}
	}
	
	/**
	 * Adds the specified object to the set of equivalents.
	 * 
	 * @param obj
	 */
	@SuppressWarnings("unchecked")
	public void addEquivalent(IndexedDataObject<E> obj)
	{
		if(obj == null) return;
		if(this.equivalents == null) this.equivalents = new HashSet<IndexedDataObject<E>>();
		
		this.size++;
		this.equivalents.add(obj);
		this.tree.nodeList.set(obj.getID(), (N) this);
	}
	
	
	/**
	 * Recursively collects all subtree elements of this node and adds them to the
	 * specified collection.<br>
	 * 
	 * Complexity: O(n), with n being the number of nodes of the local subtree.
	 * 
	 * @param subtreeCollection The collection to store the subtree data objects in.
	 */
	public void collectSubtreeElements(Collection<IndexedDataObject<E>> subtreeCollection)
	{
		// add this element and all its equivalents
		subtreeCollection.add(this.obj);
		if(this.equivalents != null) subtreeCollection.addAll(this.equivalents); 
		
		// recursively add all child data objects
		if(this.leftChild != null) this.leftChild.collectSubtreeElements(subtreeCollection);
		if(this.rightChild != null) this.rightChild.collectSubtreeElements(subtreeCollection);
	}

	/**
	 * Recursively counts the number of inner nodes in the subtree, specified by this node. <br>
	 * 
	 * Complexity: O(n), with n being the number of nodes of the local subtree.
	 * 
	 * @return the number of inner nodes of this subtree
	 */
	public int countInnerNodes()
	{
		if(this.isLeaf()) return 0;

		int innerCount = 1;
		
		if(this.leftChild != null) innerCount += this.leftChild.countInnerNodes();
		if(this.rightChild != null) innerCount += this.rightChild.countInnerNodes();
		
		return innerCount;
	}

	/**
	 * Recursively counts the number of leaf nodes in the subtree, specified by this node.<br>
	 * 
	 * Complexity: O(n), with n being the number of nodes of the local subtree. 
	 * 
	 * @return the number of leaf nodes of this subtree
	 */
	public int countLeafNodes()
	{
		if(this.isLeaf()) return 1;

		int leafCount = 0;
		
		if(this.leftChild != null) leafCount += this.leftChild.countLeafNodes();
		if(this.rightChild != null) leafCount += this.rightChild.countLeafNodes();
		
		return leafCount;
	}

	/**
	 * Recursively fills a list that contains the number of leaf nodes, depending on their respective depth.<br>
	 * 
	 * Complexity: O(n), with n being the number of nodes of the local subtree.
	 * 
	 * @param leafCounts The list of leafs w.r.t. their depth in the tree.
	 */
	public void countLeafDepth(int[] leafCounts)
	{
		if(this.isLeaf()) leafCounts[this.depth]++;
		
		if(this.leftChild != null) this.leftChild.countLeafDepth(leafCounts);
		if(this.rightChild != null) this.rightChild.countLeafDepth(leafCounts);	
	}


	/**
	 * Recursively collects all data objects of level <code>k</code> and below in a 2-dimensional {@link ArrayList}.
	 * If this node is above <code>k</code>, the function is recursively called for all child nodes.
	 * If this node is at depth <code>k</code>, all its subtree elements are collected in a {@link ArrayList} and
	 * the list is added to <code>list</code>. If its depth is larger than <code>k</code>, nothing happens.<br> 
	 *  
	 * Complexity: O(n), with n being the number of nodes in the current subtree. 
	 * 
	 * @param k the level for the subtree elements.
	 * @param list A 2-dimensional {@link ArrayList}, containing all elements of the subtrees with roots
	 * of depth <code>k</code>.
	 * 
	 * @see AbstractTree#subtreeElementsOfLevel(int)
	 */
	public void subtreeElementsOfLevel(int k, ArrayList<ArrayList<IndexedDataObject<E>>> list)
	{
		if(this.depth == k)
		{
			ArrayList<IndexedDataObject<E>> subtreeElements = new ArrayList<IndexedDataObject<E>>(this.size);
			this.collectSubtreeElements(subtreeElements);
			list.add(subtreeElements);
		}
		else if(this.depth < k)
		{
			if(this.leftChild != null) this.leftChild.subtreeElementsOfLevel(k, list);
			if(this.rightChild != null) this.rightChild.subtreeElementsOfLevel(k, list);
		}
		else return;
	}
	
	/**
	 * Recursively collects all nodes of level <code>k</code> and stores them in the specified list.<br>
	 * 
	 * Complexity: O(k), with k being the number of reported nodes from the current subtree. 
	 * 
	 * @param k the level of which the nodes are supposed to be stored.
	 * @param list the list of nodes.
	 */
	@SuppressWarnings("unchecked")
	public void nodesOfLevel(int k, ArrayList<N> list)
	{

		if(this.depth == k)
		{
			list.add((N) this); // how to avoid the cast?
		}
		else
		{
			if(this.leftChild != null) this.leftChild.nodesOfLevel(k, list);
			if(this.rightChild != null) this.rightChild.nodesOfLevel(k, list);
		}
	}

	/**
	 * Returns the data object.<br>
	 * 
	 * Complexity: O(1)
	 * 
	 * @return the obj
	 */
	public IndexedDataObject<E> getObj()
	{
		return this.obj;
	}

	/**
	 * Returns the parent node.<br>
	 * 
	 * Complexity: O(1)
	 * 
	 * @return the parent
	 */
	public N getParent()
	{
		return this.parent;
	}

	/**
	 * Returns the left child node.<br>
	 * 
	 * Complexity: O(1)
	 * 
	 * @return the leftChild
	 */
	public N getLeftChild()
	{
		return this.leftChild;
	}

	/**
	 * Returns the right child node.<br>
	 * 
	 * Complexity: O(1)
	 * 
	 * @return the rightChild
	 */
	public N getRightChild()
	{
		return this.rightChild;
	}
	
	/**
	 * Determines whether or not this node is the root of the tree. <br>
	 * 
	 * Complexity: O(1)
	 * 
	 * @return true if this node is the root of the tree.
	 */
	public boolean isRoot()
	{
		return this.depth==0;		
	}
	
	/**
	 * Determines whether or not this node is a leaf. <br>
	 * 
	 * Complexity: O(1)
	 * 
	 * @return true if this node is a leaf.
	 */
	public boolean isLeaf()
	{
		return this.height==0;
	}

	/**
	 * Returns the local height of this node. That is the maximal number steps that can be
	 * done until reaching a leaf node. if the node is a leaf, the height is 0.<br>
	 * 
	 * Complexity: O(1)   
	 * 
	 * @return the height
	 */
	public int getHeight()
	{
		return this.height;
	}

	/**
	 * Returns the depth of this node. That is the number of steps until the root is reached.
	 * If this node is the root, the depth is 0.<br>
	 * 
	 * Complexity: O(1)   
	 * 
	 * @return the depth
	 */
	public int getDepth()
	{
		return this.depth;
	}

	/**
	 * Returns the size of the local subtree.<br>
	 * 
	 * Complexity: O(1)
	 * 
	 * @return the size
	 */
	public int getSize()
	{
		return this.size;
	}


}
