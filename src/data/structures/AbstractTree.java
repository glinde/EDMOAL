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

import data.set.DataSetNotSealedException;
import data.set.IndexedDataObject;
import data.set.IndexedDataSet;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public abstract class AbstractTree<T, Node extends AbstractTreeNode<T, Node, Tree>, Tree extends AbstractTree<T, Node, Tree>> implements Serializable, DataSetFunctionalityProvider<T>
{

	/**  */
	private static final long	serialVersionUID	= -1879919519833096654L;

	/**  */
	protected IndexedDataSet<T> dataSet;
	
	/** The root of the tree. If null, there are no data objects */
	protected Node root;
	
	/**  */
	protected ArrayList<Node> nodeList;
	
	/**  */
	protected boolean build;
	

	/**
	 * 
	 * @param dataSet
	 * @param distance
	 */
	public AbstractTree()
	{
		this.dataSet = null;
		
		this.root = null;
		this.build = false;
		this.nodeList = new ArrayList<Node>();
	}
	
	/**
	 * 
	 * @param dataSet
	 * @param distance
	 */
	public AbstractTree(IndexedDataSet<T> dataSet)
	{
		this.dataSet = dataSet;
		
		this.root = null;
		this.build = false;
		this.nodeList = new ArrayList<Node>(dataSet.size());
	}
	
	public abstract void build();

	/** returns the node of the object in question or null if the object is not contained
	 * 
	 * @param dataObj
	 * @return
	 */
	public Node nodeOfObj(IndexedDataObject<T> dataObj)
	{
		if(!this.build) throw new DataStructureNotBuildException("Data structure is not build.");
		if(!this.dataSet.contains(dataObj)) return null;
		
		return this.nodeList.get(dataObj.getID());
	}
	
	/**
	 *  True if the object is contained in the tree
	 * 
	 * @param dataObj
	 * @return
	 */
	public boolean contains(IndexedDataObject<T> dataObj)
	{
		if(!this.build) throw new DataStructureNotBuildException("Data structure is not build.");
		
		return this.dataSet.contains(dataObj);
	}

	/**
	 * @return
	 */
	public int numberOfInnerNodes()
	{
		if(!this.build) throw new DataStructureNotBuildException("Data structure is not build.");
		
		return this.root.countInnerNodes();
	}

	/**
	 * @return
	 */
	public int numberOfLeafNodes()
	{
		if(!this.build) throw new DataStructureNotBuildException("Data structure is not build.");
		
		return this.root.countLeafNodes();
	}
	
	/**
	 * @return
	 */
	public int[] leafDepthDistribution()
	{
		if(!this.build) throw new DataStructureNotBuildException("Data structure is not build.");
		
		int[] leafCounts = new int[this.height()+1];
		
		this.root.countLeafDepth(leafCounts);
		
		return leafCounts;
	}

	/**
	 * @param k
	 * @return
	 */
	public ArrayList<ArrayList<IndexedDataObject<T>>> subtreeElementsOfLevel(int k)
	{
		if(!this.build) throw new DataStructureNotBuildException("Data structure is not build.");
		
		ArrayList<ArrayList<IndexedDataObject<T>>> subtreeElements = new ArrayList<ArrayList<IndexedDataObject<T>>>(100);
		
		this.root.subtreeElementsOfLevel(k, subtreeElements);
		
		return subtreeElements;
	}
	
	/**
	 * @param k
	 * @return
	 */
	public ArrayList<Node> nodesOfLevel(int k)
	{
		ArrayList<Node> nodes = new ArrayList<Node>(100);
		
		this.root.nodesOfLevel(k, nodes);
		
		return nodes;
	}
	
	/**
	 * @param obj
	 * @return
	 */
	public int subtreeSizeOf(IndexedDataObject<T> obj)
	{
		if(!this.build) throw new DataStructureNotBuildException("Data structure is not build.");

		return this.nodeList.get(obj.getID()).getSize();
	}
		
		
	/**
	 * returns the number of data objects, stored in the tree
	 * 
	 * @return
	 */
	public int size()
	{
		if(!this.build) throw new DataStructureNotBuildException("Data structure is not build.");
		
		return this.root.getSize();
	}

	/**
	 * @return
	 * @see data.structures.balltree.BallTreeNode#getHeight()
	 */
	public int height()
	{
		if(!this.build) throw new DataStructureNotBuildException("Data structure is not build.");
		
		return root.getHeight();
	}
	
	/**
	 * @return the root
	 */
	public Node getRoot()
	{
		if(!this.build) throw new DataStructureNotBuildException("Data structure is not build.");
		
		return this.root;
	}


	/* (non-Javadoc)
	 * @see data.DataSetFunctionalityProvider#getDataSet()
	 */
	@Override
	public IndexedDataSet<T> getDataSet()
	{
		return this.dataSet;
	}

	/* (non-Javadoc)
	 * @see data.DataSetFunctionalityProvider#setDataSet(data.set.IndexedDataSet)
	 */
	@Override
	public void setDataSet(IndexedDataSet<T> dataSet)
	{
		if(!dataSet.isSealed()) throw new DataSetNotSealedException("The data set is not sealed.");
		this.clearBuild();
		this.dataSet = dataSet;
	}

	/**
	 * @return the subtreeSizeList
	 */
	public int[] subtreeSizeList()
	{
		if(!this.build) throw new DataStructureNotBuildException("Data structure is not build.");

		int[] sizeList = new int[this.dataSet.size()];
		for(Node node:this.nodeList)
		{
			sizeList[node.getObj().getID()] = node.size; 
		}
		
		return sizeList;
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
