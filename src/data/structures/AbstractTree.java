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

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import sun.awt.image.ImageWatched.Link;

import data.set.DataSetNotSealedException;
import data.set.IndexedDataObject;
import data.set.IndexedDataSet;
import data.set.Sealable;
import data.structures.balltree.BallTree;
import data.structures.balltree.BallTreeNode;

/** 
 * The {@link AbstractTree} class, together with the {@link AbstractTreeNode} class are designed to
 * represent a type-save binary tree structure without specifying the way how it is build.
 * The abstraction is used to mask the building process of the tree structure and queries.  Other than that,
 * several functions for testing a tree structure are implemented.<br>
 * 
 * The rather complicated construction of generic variables is done in order to allow a variable combination of
 * Tree classes and Node classes. This complicated generic structure should be masked for any non-abstract
 * implementation of the tree structure, as it is done for example in the {@link BallTree} and {@link BallTreeNode}. <br>
 * 
 * @TODO: resolve inconsistency issue with {@link Sealable} in {@link IndexedDataObject}.
 *
 * @author Roland Winkler
 */
public abstract class AbstractTree<E, N extends AbstractTreeNode<E, N, T>, T extends AbstractTree<E, N, T>> implements Serializable, DataSetFunctionalityProvider<E>
{
	/**  */
	private static final long	serialVersionUID	= -1879919519833096654L;

	/** The data set the tree structure is build on */
	protected IndexedDataSet<E> dataSet;
	
	/** The root of the tree. If null, the tree contains no data */
	protected N root;
	
	/** A list of all nodes for O(1) access of the nodes, having an {@link IndexedDataObject} */
	protected ArrayList<N> nodeList;
	
	/** Indicates if the tree structure is build */
	protected boolean build;
	

	/**
	 * Constructs a new, empty tree structure.
	 */
	public AbstractTree()
	{
		this.dataSet = null;
		
		this.root = null;
		this.build = false;
		this.nodeList = new ArrayList<N>();
	}
	
	/**
	 * Constructs a new tree structure for the specified data set.
	 * 
	 * @param dataSet The data set on which the tree structure should be build.
	 */
	public AbstractTree(IndexedDataSet<E> dataSet)
	{
		this.dataSet = dataSet;
		
		this.root = null;
		this.build = false;
		this.nodeList = new ArrayList<N>(dataSet.size());
		for(int i=0; i<dataSet.size(); i++) this.nodeList.add(null);
	}
	
	/* (non-Javadoc)
	 * @see data.structures.DataSetFunctionalityProvider#build()
	 */
	public abstract void build();

	/**
	 * Returns the node of the specified {@link IndexedDataObject} or null if the object is not contained.<br> 
	 *  
	 * Complexity: O(1)
	 * 
	 * @param dataObj The data object that is contained in a node of the tree.
	 * @return The node that contains the dataObj
	 * 
	 * @throws DataStructureNotBuildException if the data structure is not build before calling this function.
	 */
	public N nodeOfObj(IndexedDataObject<E> dataObj) throws DataStructureNotBuildException
	{
		if(!this.build) throw new DataStructureNotBuildException("Data structure is not build.");
		if(!this.dataSet.contains(dataObj)) return null;
		
		return this.nodeList.get(dataObj.getID());
	}
	
	/**
	 *  Determines whether or not the specified object is contained in the tree.<br> 
	 *  
	 * Complexity: O(1)
	 * 
	 * @param dataObj The data object for which the test should be applied
	 * @return true, if the data object is contained, false otherwise
	 * 
	 * @throws DataStructureNotBuildException if the data structure is not build before calling this function.
	 */
	public boolean contains(IndexedDataObject<E> dataObj) throws DataStructureNotBuildException
	{
		if(!this.build) throw new DataStructureNotBuildException("Data structure is not build.");
		
		return this.dataSet.contains(dataObj);
	}

	/**
	 * Counts the number of inner nodes of the tree. That are all nodes with at least one child node.<br> 
	 *  
	 * Complexity: O(n), with n being the number of data objects, stored in the tree.
	 * 
	 * @return The total number of inner nodes.
	 * 
	 * @throws DataStructureNotBuildException if the data structure is not build before calling this function.
	 */
	public int numberOfInnerNodes() throws DataStructureNotBuildException
	{
		if(!this.build) throw new DataStructureNotBuildException("Data structure is not build.");
		
		return this.root.countInnerNodes();
	}

	/**
	 * Counts the number of leaf nodes of the tree. That are all nodes that have no child nodes.
	 * 
	 * @return The total number of leaf nodes.
	 * 
	 * @throws DataStructureNotBuildException if the data structure is not build before calling this function.
	 */
	public int numberOfLeafNodes() throws DataStructureNotBuildException
	{
		if(!this.build) throw new DataStructureNotBuildException("Data structure is not build.");
		
		return this.root.countLeafNodes();
	}
	
	/**
	 * Counts the number of leafs, depending on their depth in the tree. That is, for each level of the tree,
	 * the number of leafs of that level is returned. A level of the tree are all nodes which have the same depth,
	 * that is the number of parent nodes until they reach the root. The root has depth 0, its child nodes have
	 * depth 1, etc.<br> 
	 *  
	 * Complexity: O(n), with n being the number of data objects, stored in the tree.
	 *  
	 * @return A list that contains for each level of the tree, the number of leaf nodes of this level.
	 * 
	 * @throws DataStructureNotBuildException if the data structure is not build before calling this function.
	 */
	public int[] leafDepthDistribution() throws DataStructureNotBuildException
	{
		if(!this.build) throw new DataStructureNotBuildException("Data structure is not build.");
		
		int[] leafCounts = new int[this.height()+1];
		
		this.root.countLeafDepth(leafCounts);
		
		return leafCounts;
	}

	/**
	 * Returns all data objects of level <code>k</code> and below in a 2-dimensional {@link ArrayList}.
	 * The structure is like this: For each node of depth <code>k</code>, the first level {@link ArrayList} contains
	 * an entry. In a second level {@link ArrayList}, all elements of the subtree of a node of depth
	 * <code>k</code> is contained. A level of the tree are all nodes which have the same depth,
	 * that is the number of parent nodes until they reach the root. The root has depth 0, its child nodes have
	 * depth 1, etc.<br>
	 * 
	 * In other words, all data objects of a subtree with its root node of depth <code>k</code> are stored in one
	 * {@link ArrayList}. Then, all the {@link ArrayList}s (one for each node of depth <code>k</code>) are 
	 * stored in an other {@link ArrayList} which is returned.<br> 
	 *  
	 * Complexity: O(n), with n being the number of data objects, stored in the tree. 
	 * 
	 * @param k the level for the subtree elements.
	 * @return A 2-dimensional {@link ArrayList}, containing all elements of the subtrees with roots
	 * of depth <code>k</code>.
	 * 
	 * @throws DataStructureNotBuildException if the data structure is not build before calling this function.
	 */
	public ArrayList<ArrayList<IndexedDataObject<E>>> subtreeElementsOfLevel(int k) throws DataStructureNotBuildException
	{
		if(!this.build) throw new DataStructureNotBuildException("Data structure is not build.");
		
		ArrayList<ArrayList<IndexedDataObject<E>>> subtreeElements = new ArrayList<ArrayList<IndexedDataObject<E>>>(100);
		
		this.root.subtreeElementsOfLevel(k, subtreeElements);
		
		return subtreeElements;
	}
	
	/**
	 * Returns all Nodes of level (with depth) <code>k</code>.<br> 
	 *  
	 * Complexity: O(n), with n being the number of reported nodes.
	 * 
	 * @param k The depth of the nodes.
	 * @return A list of nodes of level <code>k</code>. 
	 * 
	 * @throws DataStructureNotBuildException if the data structure is not build before calling this function.
	 */
	public ArrayList<N> nodesOfLevel(int k) throws DataStructureNotBuildException
	{
		if(!this.build) throw new DataStructureNotBuildException("Data structure is not build.");
		
		ArrayList<N> nodes = new ArrayList<N>(100);
		
		this.root.nodesOfLevel(k, nodes);
		
		return nodes;
	}
	
	/**
	 * Gets the size of the subtree that corresponds with the specified data object. If the data object is
	 * not contained in the tree, the result is -1.<br> 
	 *  
	 * Complexity: O(k + log(n)), with n being the number of nodes of the tree and k being the number of reportet data objects.
	 * 
	 * @param obj The object for which the subtree size should be determined.
	 * @return The subtree size of the object, or -1, if the data object is not contained. 
	 * 
	 * @throws DataStructureNotBuildException if the data structure is not build before calling this function.
	 */
	public int subtreeSizeOf(IndexedDataObject<E> obj) throws DataStructureNotBuildException
	{
		if(!this.build) throw new DataStructureNotBuildException("Data structure is not build.");
		if(!this.dataSet.contains(obj)) return -1;

		return this.nodeList.get(obj.getID()).getSize();
	}
		
		
	/**
	 * Returns the number of data objects, stored in the tree.<br> 
	 *  
	 * Complexity: O(1)
	 * 
	 * @return the number of data objects, stored in the tree.
	 * 
	 * @throws DataStructureNotBuildException if the data structure is not build before calling this function.
	 */
	public int size() throws DataStructureNotBuildException
	{
		if(!this.build) throw new DataStructureNotBuildException("Data structure is not build.");
		
		return this.root.getSize();
	}

	/**
	 * The height of the tree. That is the longest path that is possible, starting from the root.
	 * It is equal to the maximal depth of all leaf nodes.<br> 
	 *  
	 * Complexity: O(1)
	 * 
	 * @return The height of the tree.
	 * 
	 * @throws DataStructureNotBuildException if the data structure is not build before calling this function.
	 */
	public int height() throws DataStructureNotBuildException
	{
		if(!this.build) throw new DataStructureNotBuildException("Data structure is not build.");
		
		return root.getHeight();
	}
	
	/**
	 * Returns the root of the tree.<br> 
	 *  
	 * Complexity: O(1)
	 * 
	 * @return the root.
	 * 
	 * @throws DataStructureNotBuildException if the data structure is not build before calling this function.
	 */
	public N getRoot() throws DataStructureNotBuildException
	{
		if(!this.build) throw new DataStructureNotBuildException("Data structure is not build.");
		
		return this.root;
	}


	/* (non-Javadoc)
	 * @see data.structures.DataSetFunctionalityProvider#getDataSet()
	 */
	@Override
	public IndexedDataSet<E> getDataSet()
	{
		return this.dataSet;
	}

	/**
	 * Sets the data set for the tree. If the data set is not yet sealed, an exception is thrown.<br> 
	 *  
	 * Complexity: O(1)
	 * 
	 * @param dataSet The data set to set.
	 * 
	 * @throws DataSetNotSealedException If the specified data set is not yet sealed. 
	 * 
	 * @see data.structures.DataSetFunctionalityProvider#setDataSet(data.set.IndexedDataSet)
	 */
	@Override
	public void setDataSet(IndexedDataSet<E> dataSet) throws DataSetNotSealedException
	{
		if(!dataSet.isSealed()) throw new DataSetNotSealedException("The data set is not sealed.");
		this.clearBuild();
		this.dataSet = dataSet;
	}

	/* (non-Javadoc)
	 * @see data.DataSetFunctionalityProvider#clear()
	 */
	@Override
	public void clearBuild()
	{
		this.root = null;
		this.nodeList.clear();
		this.build = false;
		
		System.gc();
	}

	/* (non-Javadoc)
	 * @see data.DataSetFunctionalityProvider#ready()
	 */
	@Override
	public boolean isBuild()
	{
		return this.isBuild();
	}
}
