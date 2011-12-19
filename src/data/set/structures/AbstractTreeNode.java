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


package data.set.structures;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import data.set.IndexedDataObject;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class AbstractTreeNode<T, Node extends AbstractTreeNode<T, Node, Tree>, Tree extends AbstractTree<T, Node, Tree>> implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= 6512063591779793509L;

	/** the data object of this node */
	protected final IndexedDataObject<T> obj;
	
	/** The associated tree */
	protected final Tree tree;
	
	/** The parent of the node. If it is null, this node is the root node */
	protected final Node parent;
	
	/** A list of data object elements that are equivalent to this.obj.element.
	 * That is, all objects that have a distance of 0 to this.obj.
	 * The reference to this object will remain null until the first equivalent data object is added. */
	protected HashSet<IndexedDataObject<T>> equivalents;
	
	/** left child node */
	protected Node leftChild;
	
	/** right child node */
	protected Node rightChild;
	
	/** The number of data objects in this subtree (including this.obj) */
	protected int size;	
	
	/** The height of the current subtree */
	protected int height;
	
	/** The number of parents above to the root */
	protected int depth;
		
	/**
	 * 
	 */
	protected AbstractTreeNode(Tree tree, Node parent, IndexedDataObject<T> obj)
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
	}
	
	/** collects all data objects of the current subset and stores them in localDataSet
	 * @param localDataSet
	 */
	public void collectSubtreeElements(Collection<IndexedDataObject<T>> localDataSet)
	{
		// add this element and all its equivalents
		localDataSet.add(this.obj);
		if(this.equivalents != null) localDataSet.addAll(this.equivalents); 
		
		// recursively add all child data objects
		if(this.leftChild != null) this.leftChild.collectSubtreeElements(localDataSet);
		if(this.rightChild != null) this.rightChild.collectSubtreeElements(localDataSet);
	}

	/**
	 * @return
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
	 * @return
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
	 * @param leafCounts
	 */
	public void countLeafDepth(int[] leafCounts)
	{
		if(this.isLeaf()) leafCounts[this.depth]++;
		
		if(this.leftChild != null) this.leftChild.countLeafDepth(leafCounts);
		if(this.rightChild != null) this.rightChild.countLeafDepth(leafCounts);	
	}

	/**
	 * @param k
	 * @return
	 */
	public void subtreeElementsOfLevel(int k, ArrayList<ArrayList<IndexedDataObject<T>>> list)
	{
		if(this.depth == k)
		{
			ArrayList<IndexedDataObject<T>> subtreeElements = new ArrayList<IndexedDataObject<T>>(this.size);
			this.collectSubtreeElements(subtreeElements);
			list.add(subtreeElements);
		}
		else
		{
			if(this.leftChild != null) this.leftChild.subtreeElementsOfLevel(k, list);
			if(this.rightChild != null) this.rightChild.subtreeElementsOfLevel(k, list);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void nodesOfLevel(int k, ArrayList<Node> list)
	{

		if(this.depth == k)
		{
			list.add((Node) this); // how to avoid the cast?
		}
		else
		{
			if(this.leftChild != null) this.leftChild.nodesOfLevel(k, list);
			if(this.rightChild != null) this.rightChild.nodesOfLevel(k, list);
		}
	}

	/**
	 * @return the obj
	 */
	public IndexedDataObject<T> getObj()
	{
		return this.obj;
	}

	/**
	 * @return the parent
	 */
	public Node getParent()
	{
		return this.parent;
	}

	/**
	 * @return the leftChild
	 */
	public Node getLeftChild()
	{
		return this.leftChild;
	}

	/**
	 * @return the rightChild
	 */
	public Node getRightChild()
	{
		return this.rightChild;
	}
	
	/**
	 * @return
	 */
	public boolean isRoot()
	{
		return this.depth==0;		
	}
	
	/**
	 * @return
	 */
	public boolean isLeaf()
	{
		return this.height==0;
	}

	/**
	 * @return the height
	 */
	public int getHeight()
	{
		return this.height;
	}

	/**
	 * @return the depth
	 */
	public int getDepth()
	{
		return this.depth;
	}

	/**
	 * @return the size
	 */
	public int getSize()
	{
		return this.size;
	}


}
