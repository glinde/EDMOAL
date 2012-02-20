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


package datamining.clustering.density;

import java.util.ArrayList;
import java.util.TreeSet;

import data.algebra.Metric;
import data.set.IndexedDataObject;
import data.set.IndexedDataSet;
import data.structures.balltree.BallTree;
import data.structures.queries.SphereQueryProvider;
import datamining.clustering.AbstractClusteringAlgorithm;
import datamining.clustering.CrispClusteringAlgorithm;
import datamining.clustering.CrispNoiseClusteringAlgorithm;

/**
 * The DBScan algorithm, implemented using a ball tree. This is one of the best and fastest clustering algorithms
 * that have been invented. It finds any kind of cluster shape, provided the data objects are fairly dense inside
 * the cluster and sparse in between the clusters. The algorithm is able to detect noise data objects, based
 * on two parameters: A core distance and a minimal number of core data objects.<br>
 * 
 * <ul>The Algorithm in short goes as follows: Start at one random data object.
 * <li> 1. case: If in the _radius_ around the data object are at least
 * 		a _min_ number of data objects, this data object forms a new cluster.
 * 		Note that all data objects belong to this cluster.
 * 		Then perform the test recursively for all untested or noise data objects in range of the _radius_.</li>
 * <li> 2. case: If there are not enough data objects around and this data object is not noted to belong to
 * 		a cluster already, note it as being noise and contionue with the next untested, random data object.</li>
 * </ul>
 * 
 * Using a ball tree to perform a sphere query makes this algorithm be for well defined parameters be in O(n * log(n)).
 * For a large data set with many clusters, this is significantly faster most prototype based algorithm. <br>
 * 
 * Paper: Ester, M.; Kriegel, H.-P.; Jörg, S. & Xu, X. A density-based algorithm for discovering clusters in large spatial databases with noise 2nd International Conference on Knowledge Discovery and Data Mining, AAAI Press, 1996, 226-231
 * 
 * @see data.structures.balltree.BallTree
 * 
 * @author Roland Winkler
 */
public class DBScan<T> extends AbstractClusteringAlgorithm<T> implements CrispClusteringAlgorithm<T>, CrispNoiseClusteringAlgorithm<T>
{
	/**  */
	private static final long	serialVersionUID	= 8539759213273998996L;

	/** For the DBScan clustering algorithm, the constants for noise and unassigned are changed. This is masked to the outside to fit the crisp noise clustering interface.  */
	private static final int DBSCAN_UNASSIGNED_ID = -2;
	
	/** For the DBScan clustering algorithm, the constants for noise and unassigned are changed. This is masked to the outside to fit the crisp noise clustering interface.  */
	private static final int DBSCAN_NOISE_ID = -1;	
	
	/** The core distance */
	protected double coreDist;
	
	/** The minimal number of core data objects. */
	protected int coreNum;
	
	/** The number of clustered found so far. */
	protected int clusterCount;
			
	/** The sphere query provider, for example a ball tree. */
	protected SphereQueryProvider<T> sphereQueryProvider;
	
	/** A list to hold the cluster ID's for all data objects. */
	protected int[] clusterIDs;
		
	/**
	 *  The standard constructor.
	 * 
	 * @param dataSet The data set that is to be clustered.
	 * @param coreDist The core distance
	 * @param coreNum The minimal number of data object to become a core point
	 * @param metric The metric for calculating distances amoung data objects.
	 */
	public DBScan(IndexedDataSet<T> dataSet, double coreDist, int coreNum, Metric<T> metric)
	{
		super(dataSet, metric);
		
		this.coreDist = coreDist;
		this.coreNum = coreNum;
		
		this.clusterCount = 1;

		this.sphereQueryProvider = null;
		
		this.clusterIDs = new int[this.getDataCount()];
		for(int i=0; i<this.getDataCount(); i++) this.clusterIDs[i] = DBScan.DBSCAN_UNASSIGNED_ID;
	}
	
	
	/**
	 *	A constructor assuming <code>coreDist</code> = 1 and <code>coreNum</code> = 4.
	 *
	 * @TODO: make an algorithm to estimate the parameter.
	 * 
	 * @param dataSet The data set that is to be clustered.
	 * @param metric The metric for calculating distances amoung data objects.
	 */
	public DBScan(IndexedDataSet<T> dataSet, Metric<T> metric)
	{
		this(dataSet, 1.0d, 4, metric);
	}
		
	/**
	 * The copy constructor.
	 * 
	 * @param c The elder clustering algorithm object.
	 */
	public DBScan(DBScan<T> c)
	{
		super(c);
		
		this.coreDist				= c.coreDist;
		this.coreNum				= c.coreNum;
		
		this.sphereQueryProvider	= c.sphereQueryProvider;
		
		this.clusterIDs				= c.clusterIDs.clone();
	}
	
	/**
	 * Sets the specified {@link SphereQueryProvider}. If the data set of the provider is identical to this data set,
	 * it is used as is. If it contains a different data set, it is cleared and the data set of this
	 * algorithm is added to the SphereQueryProvider.
	 * 
	 * @param provider The SphereQueryProvider to be used for this clustering algorithm.
	 */
	public void registerSphereQueryProvider(SphereQueryProvider<T> provider)
	{
		if(provider.getDataSet() != this.getDataSet())
		{
			provider.clearBuild();
			provider.setDataSet(this.data);
		}

		this.sphereQueryProvider = provider;
	}
	
	/**
	 * If the {@link SphereQueryProvider} is not specified in ths instance, a new {@link BallTree} is created to perform the
	 * clustering. 
	 */
	private void autoBuildTree()
	{
		this.sphereQueryProvider = new BallTree<T>(this.data, this.metric);
		this.sphereQueryProvider.build();
	}
	
	
	/* (non-Javadoc)
	 * @see datamining.DataMiningAlgorithm#apply()
	 */
	@Override
	public void apply()
	{
		TreeSet<IndexedDataObject<T>> cluster = new TreeSet<IndexedDataObject<T>>();
		ArrayList<IndexedDataObject<T>> query = new ArrayList<IndexedDataObject<T>>();
		IndexedDataObject<T> current;
		int currentID;
		
		System.out.println("apply DBScan");
		
		if(this.sphereQueryProvider == null) this.autoBuildTree();
		if(!this.sphereQueryProvider.isBuild()) this.sphereQueryProvider.build();
		
		// reset cluster assignments in case the algorithm was applied before.
		for(int i=0; i<this.getDataCount(); i++) this.clusterIDs[i] = DBScan.DBSCAN_UNASSIGNED_ID;
		
		this.clusterCount = 0; // the noise cluster does not count!
				
		for(IndexedDataObject<T> d:this.data)
		{			
			// if the data object is already clustered, ignore it and continue with the next.
			if(this.clusterIDs[d.getID()] != DBScan.DBSCAN_UNASSIGNED_ID) continue;
			
			// get all nearby data objects 
			cluster.clear();
			this.sphereQueryProvider.sphereQuery(cluster, d.x, this.coreDist);
			
			// if the nearby data objects are not enough to form a cluster, d is noise and the algorithm continues with the next data object
			if(cluster.size() < this.coreNum)
			{
				this.clusterIDs[d.getID()] = DBScan.DBSCAN_NOISE_ID;
				continue;
			}			
			
			// increase cluster count and mark all query elements as in the new cluster
			currentID = this.clusterCount;
			this.clusterCount++;
			for(IndexedDataObject<T> d2: cluster) this.clusterIDs[d2.getID()] = currentID;
			
			// the data object was unclustered and is not noise.
			cluster.remove(d);
			while(!cluster.isEmpty())
			{
				current = cluster.pollFirst(); 
				query.clear();
				this.sphereQueryProvider.sphereQuery(query, current.x, this.coreDist);
				if(query.size() >= this.coreNum)
				{
					for(IndexedDataObject<T> q:query)
					{
						if(this.clusterIDs[q.getID()] == DBScan.DBSCAN_NOISE_ID || this.clusterIDs[q.getID()] == DBScan.DBSCAN_UNASSIGNED_ID) // q is either noise or unclustered
						{
							if(this.clusterIDs[q.getID()] == DBScan.DBSCAN_UNASSIGNED_ID) // if q is already known to be noise, it does not need to be checked again.
							{
								cluster.add(q);
							}
							
							this.clusterIDs[q.getID()] = currentID;
						}
					}
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see datamining.ClusterResultAlgorithm#getPrototypeCount()
	 */
	@Override
	public int getClusterCount()
	{
		return this.clusterCount;
	}

	/**
	 * @return the coreDist
	 */
	public double getCoreDist()
	{
		return this.coreDist;
	}

	/**
	 * @param coreDist the coreDist to set
	 */
	public void setCoreDist(double coreDist)
	{
		this.coreDist = coreDist;
	}

	/**
	 * @return the coreNum
	 */
	public int getCoreNum()
	{
		return this.coreNum;
	}

	/**
	 * @param coreNum the coreNum to set
	 */
	public void setCoreNum(int coreNum)
	{
		this.coreNum = coreNum;
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.AbstractDoubleArrayClusteringAlgorithm#algorithmName()
	 */
	@Override
	public String algorithmName()
	{
		return "DBScan Clustering Algorithm";
	}

	/* (non-Javadoc)
	 * @see datamining.CrispClusterResultAlgorithm#getCrispIndicesResult()
	 */
	@Override
	public int[] getAllCrispClusterAssignments()
	{
		int[] crispResult = new int[this.getDataCount()];
		
		for(int j=0; j<this.getDataCount(); j++)
		{
			crispResult[j] = this.clusterIDs[this.data.get(j).getID()];
			crispResult[j] = (crispResult[j] == DBScan.DBSCAN_NOISE_ID || crispResult[j] == DBScan.DBSCAN_UNASSIGNED_ID)? CrispClusteringAlgorithm.UNASSIGNED_INDEX : crispResult[j];
		}
		
		return crispResult;
	}


	/* (non-Javadoc)
	 * @see datamining.clustering.CrispClusterResultAlgorithm#getCrispAssignment(data.set.IndexedDataObject)
	 */
	@Override
	public int getCrispClusterAssignmentOf(IndexedDataObject<T> obj)
	{
		int assignment = this.clusterIDs[obj.getID()];
		
		// either the obj is unassigned or it is noise, in both cases, the assignment index is -1
		assignment = (assignment == DBScan.DBSCAN_NOISE_ID || assignment == DBScan.DBSCAN_UNASSIGNED_ID)? CrispClusteringAlgorithm.UNASSIGNED_INDEX : assignment;
		
		return assignment;
	}


	/* (non-Javadoc)
	 * @see datamining.clustering.CrispClusterResultAlgorithm#isCrispClusterAssigned(data.set.IndexedDataObject)
	 */
	@Override
	public boolean isCrispAssigned(IndexedDataObject<T> obj)
	{
		return this.clusterIDs[obj.getID()] >= 0;
	}


	/* (non-Javadoc)
	 * @see datamining.clustering.CrispNoiseClusteringAlgorithm#getCrispNoiseAssignments()
	 */
	@Override
	public boolean[] getCrispNoiseAssignments()
	{
		boolean[] crispNoise = new boolean[this.getDataCount()];
		
		for(int j=0; j<this.getDataCount(); j++)
		{
			crispNoise[j] = this.clusterIDs[this.data.get(j).getID()] == DBScan.DBSCAN_NOISE_ID;
		}
		
		return crispNoise;
	}


	/* (non-Javadoc)
	 * @see datamining.clustering.CrispNoiseClusteringAlgorithm#isCrispNoiseAssigned(data.set.IndexedDataObject)
	 */
	@Override
	public boolean isCrispNoiseAssigned(IndexedDataObject<T> obj)
	{
		return this.clusterIDs[obj.getID()] == DBScan.DBSCAN_NOISE_ID;
	}


	/* (non-Javadoc)
	 * @see datamining.clustering.ClusteringAlgorithm#getActiveClusterCount()
	 */
	@Override
	public int getActiveClusterCount()
	{
		return this.getClusterCount();
	}


	/* (non-Javadoc)
	 * @see datamining.clustering.ClusteringAlgorithm#getInactiveClusterCount()
	 */
	@Override
	public int getInactiveClusterCount()
	{
		return 0;
	}


	/**
	 * @return the sphereQueryProvider
	 */
	public SphereQueryProvider<T> getSphereQueryProvider()
	{
		return this.sphereQueryProvider;
	}
}
