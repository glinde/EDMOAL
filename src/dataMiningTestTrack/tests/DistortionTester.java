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
package dataMiningTestTrack.tests;

import generation.data.ClusteredDataSetGenerator;

import java.io.Serializable;
import java.util.List;

import data.set.IndexedDataObject;
import data.set.IndexedDataSet;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class DistortionTester extends TestVisualizer implements Serializable
{
	/** */
	public DistortionTester()
	{
		super();
	}
	
	/**
	 * Shows the generated data set.
	 */
	public void showDataSet(int x, int y, List<double[]> data)
	{
		this.xIndex = x;
		this.yIndex = y;
		IndexedDataSet<double[]> dataSet = new IndexedDataSet<double[]>(data.size());
		for(double[] d:data) dataSet.add(new IndexedDataObject<double[]>(d));
		this.showDataSet(dataSet, null);
	}

	/**
	 * Shows the generated data set.
	 */
	public void showDataSetClustered(int x, int y, List<double[]> data, int clusterCount, int[] clusterInformation)
	{
		this.xIndex = x;
		this.yIndex = y;
		IndexedDataSet<double[]> dataSet = new IndexedDataSet<double[]>(data.size());
		for(double[] d:data) dataSet.add(new IndexedDataObject<double[]>(d));
		this.showCrispDataSetClustering(dataSet, clusterCount, clusterInformation, null);
	}
	
	public void testDistortedClusters(int dim, int dataPerClusterCount, boolean randomClusterSize, int clusterCount, int noise, boolean scale, int shuffleLocation)
	{
		ClusteredDataSetGenerator clusterGen = new ClusteredDataSetGenerator(dim);
		
		clusterGen.generateDistortedClusteredDataSet(dataPerClusterCount, randomClusterSize, clusterCount, noise, scale, shuffleLocation);
		
		for(int i=0; i<dim; i+=Math.max(dim/4, 1))
		{
			for(int j=i+1; j<dim; j+=Math.max(dim/5, 1))
			{
				this.showDataSetClustered(i, j, clusterGen.getData(), clusterCount+1, clusterGen.getClusterIndices());
			}
		}
	}
	
}
