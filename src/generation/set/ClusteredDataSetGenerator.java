/**
Copyright (c) 2013, The EDMOAL Project

	Roland Winkler
	Richard-Wagner Str. 42
	10585 Berlin, Germany
	roland.winkler@gmail.com
 
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
    	this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice,
    	this list of conditions and the following disclaimer in the documentation and/or
    	other materials provided with the distribution.
    * The name of Roland Winkler may not be used to endorse or promote products
		derived from this software without specific prior written permission.

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
package generation.set;

import java.util.ArrayList;
import java.util.Collections;

import data.set.IndexedDataObject;

import generation.data.DataGenerator;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class ClusteredDataSetGenerator<T> extends AbstractDataSetGenerator<T>
{
	public class ClusterGenerator<S>
	{
		public DataGenerator<S> generator;
		public int clusterSize;
		
		public ClusterGenerator(DataGenerator<S> generator, int clusterSize)
		{
			this.generator = generator;
			this.clusterSize = clusterSize;
		}
	}
	public class DataIndexPair<S>
	{
		public S dataObject;
		public int clusterIndex;

		public DataIndexPair(S dataObject, int clusterIndex)
		{
			super();
			this.dataObject = dataObject;
			this.clusterIndex = clusterIndex;
		}
	}
	
	protected ArrayList<ClusterGenerator<T>> clusterGenerators;

	protected int[] clusterIndicesOfData;	
	protected boolean shuffle;
	
	public ClusteredDataSetGenerator()
	{
		this.clusterGenerators = new ArrayList<ClusterGenerator<T>>();
		this.shuffle = true;
		this.clusterIndicesOfData = null;
	}
	
	public void addClusterDataGenerator(DataGenerator<T> generator, int clusterSize)
	{
		this.clusterGenerators.add(new ClusterGenerator<T>(generator, clusterSize));
	}
	
	/**
	 * @return the clusterGenerators
	 */
	public ArrayList<ClusterGenerator<T>> getClusterGenerators()
	{
		return this.clusterGenerators;
	}

	public void generateData()
	{
		ArrayList<DataIndexPair<T>> dataList = new ArrayList<DataIndexPair<T>>();
		
		for(int i=0; i<this.clusterGenerators.size(); i++)
		{
			ClusterGenerator<T> cGen = this.clusterGenerators.get(i);
			
			ArrayList<T> cluster = cGen.generator.generateDataObjects(cGen.clusterSize);
			
			for(T data:cluster)
			{
				dataList.add(new DataIndexPair<T>(data, i));
			}
		}
		
		if(this.shuffle)
		{
			Collections.shuffle(dataList);
		}
		
		this.clusterIndicesOfData = new int[dataList.size()];
		for(int j=0; j<dataList.size(); j++)
		{
			DataIndexPair<T> pair = dataList.get(j);
			this.dataSet.add(new IndexedDataObject<T>(pair.dataObject));
			this.clusterIndicesOfData[j] = pair.clusterIndex;
		}
		
		this.dataSet.seal();
	}

	/**
	 * @return the shuffle
	 */
	public boolean isShuffle()
	{
		return this.shuffle;
	}

	/**
	 * @param shuffle the shuffle to set
	 */
	public void setShuffle(boolean shuffle)
	{
		this.shuffle = shuffle;
	}

	/**
	 * @return the clusterIndicesOfData
	 */
	public int[] getClusterIndicesOfData()
	{
		return this.clusterIndicesOfData;
	}
	
	
}
