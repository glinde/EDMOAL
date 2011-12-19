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

import java.util.ArrayList;
import java.util.Collection;
import java.util.PriorityQueue;

import data.algebra.Distance;
import data.algebra.VectorSpace;
import data.set.DataSetNotSealedException;
import data.set.IndexedDataObject;
import data.set.IndexedDataSet;
import data.set.structures.order.OrderedDataObject;
import data.set.structures.queries.KNNQueryProvider;
import data.set.structures.queries.SphereQueryProvider;

/**
 * TODO Class Description
 *
 * Paper: Omohundro, S. M. Five Balltree Construction Algorithms International Computer Science Institute, 1989
 * Paper: Uhlmann, J. K. Satisfying general proximity / similarity queries with metric trees Information Processing Letters, 1991, 40, 175 - 179
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
	protected final Distance<T> distanceFunction;
	
	/**
	 * 
	 * @param dataSet
	 * @param distance
	 */
	public CenteredBallTree(IndexedDataSet<T> dataSet, VectorSpace<T> vectorSpace, Distance<T> distance)
	{
		super(dataSet);
		
		this.vectorSpace = vectorSpace;
		this.distanceFunction = distance;
	}
	
	/* (non-Javadoc)
	 * @see data.set.structures.AbstractTree#build()
	 */
	@Override
	public void build()
	{
		if(!this.dataSet.isSealed()) throw new DataSetNotSealedException("The data set is not sealed.");
		
		this.buildNaive();
	}
	
	/**
	 * A naive way of building the tree, adding data objects one by one, no optimization.
	 * Seals the data set.
	 */
	public void buildNaive()
	{
		if(!this.dataSet.isSealed()) throw new DataSetNotSealedException("The data set is not sealed.");
		
		ArrayList<IndexedDataObject<T>> subelements = new ArrayList<IndexedDataObject<T>>(this.dataSet.size());
		for(int i=1; i<this.dataSet.size(); i++) subelements.add(this.dataSet.get(i));

		this.root = new CenteredBallTreeNode<T>(this, null, this.dataSet.get(0));
		this.root.addNaive(subelements);
		
		this.build = true;
	}
	

	/* (non-Javadoc)
	 * @see data.set.structures.queries.SphereQueryProvider#sphereQuery(java.util.Collection, java.lang.Object, double)
	 */
	public Collection<IndexedDataObject<T>> sphereQuery(Collection<IndexedDataObject<T>> result, T centre, double radius)
	{
		if(!this.build) throw new DataStructureNotBuildException("Data structure is not build.");
		
		if(result == null) result = new ArrayList<IndexedDataObject<T>>();
		
		this.root.sphereQuery(result, centre, radius);
		
		return result;
	}
	
	
	/* (non-Javadoc)
	 * @see data.set.structures.queries.KNNQueryProvider#knnQuery(java.util.Collection, java.lang.Object, int)
	 */
	public Collection<IndexedDataObject<T>> knnQuery(Collection<IndexedDataObject<T>> result, T centre, int k)
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
	 * @return the distanceFunction
	 */
	public Distance<T> getDistanceFunction()
	{
		return this.distanceFunction;
	}

	/**
	 * @return the radiusList
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
