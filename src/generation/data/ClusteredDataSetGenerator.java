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
package generation.data;

import java.util.ArrayList;
import java.util.List;

import etc.SimpleStatistics;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class ClusteredDataSetGenerator
{
	private int dim;
	
	private ArrayList<double[]> data;
	
	private ArrayList<ArrayList<double[]>> clusters;
	
	private int[] clusterIndices;
	
	public ClusteredDataSetGenerator(int dim)
	{
		this.dim = dim;
		this.data = new ArrayList<double[]>();
		this.clusters = new ArrayList<ArrayList<double[]>>();
		this.clusterIndices = null;
	}
	
	
	public void generateClusteredDataSet(int dataObjectsPerClusterCount, int clusterCount, int noise, boolean scale, int shuffleLocation)
	{
		this.data.ensureCapacity(dataObjectsPerClusterCount*clusterCount + noise);
		this.clusterIndices = new int[dataObjectsPerClusterCount*clusterCount + noise];
				
		System.out.print("Generate data ... ");		
		for(int i=0; i<clusterCount; i++)
		{
			this.clusters.add(this.generateRandomData(new int[]{dataObjectsPerClusterCount/2, dataObjectsPerClusterCount/2 + dataObjectsPerClusterCount%2}));
		}
		System.out.println(" done.");

		int k=0;
				
		for(int i=0; i<clusterCount; i++)
		{
			System.out.println("======= Distort cluster " + i + " ======="); 		
			this.distortCluster(clusters.get(i), scale, shuffleLocation);
			this.data.addAll(clusters.get(i));
			for(int j=0; j<dataObjectsPerClusterCount; j++, k++) this.clusterIndices[k] = i; 
		}

		System.out.print("Add noise ... ");

		this.data.addAll((new HyperrectangleUniformGenerator(this.dim)).generateDataObjects(noise));

//		double[] mean = SimpleStatistics.mean(data);
//		double variance = SimpleStatistics.variance(data, mean);
//		ArrayList<double[]> tmpData;
//		DataDistorter distorter = new DataDistorter(this.dim);
//		tmpData = (new SphericalNormalGenerator(mean, variance)).generateDataObjects(normalNiose);
//		distorter.normalizeParallel(tmpData);		
//		this.data.addAll(tmpData);
		
		for(int j=0; j<noise; j++, k++) this.clusterIndices[k] = -1; 

		System.out.println(" done.");
	}
	
	/**
	 * Distributions are:<br>
	 * 0: Normal Spherical Distribution<br>
	 * 1: Uniform Distribution<br>
	 * <br>
	 * @param objectCounts
	 * @return
	 */
	public ArrayList<double[]> generateRandomData(int[] objectCounts)
	{		
		ArrayList<DADataGenerator> generators = new ArrayList<DADataGenerator>();
		generators.add(new SphericalNormalGenerator(this.dim));
		generators.add(new HyperrectangleUniformGenerator(this.dim));
		
		return this.generateRandomData(objectCounts, generators);
	}
	
	
	public ArrayList<double[]> generateRandomData(int[] objectCounts, List<DADataGenerator> dataGenerators)
	{		
		ArrayList<double[]> data = new ArrayList<double[]>();
		ArrayList<double[]> tmpData;
		DataDistorter distorter = new DataDistorter(this.dim);
		
		for(int i=0; i<objectCounts.length; i++)
		{
			tmpData = dataGenerators.get(i).generateDataObjects(objectCounts[i]);
			distorter.normalizeParallel(tmpData);
			data.addAll(tmpData);
		}
		
		return data;
	}
	
	public void distortCluster(ArrayList<double[]> clusterData, boolean scale, int shuffleLocation)
	{
		DataDistorter distorter = new DataDistorter(this.dim);

		System.out.print("generate distortions ... ");
		// distort data
		for(int i=0; i<2*dim; i++) distorter.addDistortionLayers(new double[]{1.0, 0.5d/dim, 20.0d/dim, 10.0d/dim, 10.0d/dim, 1.2d/dim},  new double[]{0.0d, 0.1, 0.1, 1.0d/dim, Math.log(dim)/dim});

		// scale to fill the unit hypercube
		if(scale) distorter.addDistortionLayers(new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0d},  new double[]{1.0d, 0.0, 0.0, 0.0, 0.0});

		// push the data randomly in the direction of the upper right corner 
		for(int i=0; i<shuffleLocation; i++) distorter.addDistortionLayers(new double[]{0.7, 0.0, 0.0, 0.3, 0.0, 0.0d},  new double[]{1.0d, 0.0, 0.0, 0.0, 0.0});

		// randomly reverse data to randomize the data in the feature space
		if(shuffleLocation > 0) distorter.addDistortionLayers(new double[]{0.5, 0.5, 0.0, 0.0, 0.0, 0.0d},  new double[]{1.0d, 0.0, 0.0, 0.0, 0.0});
		System.out.println(" done.");

		System.out.print("Distort data ");
		long start = System.currentTimeMillis();
		distorter.distortData(clusterData);
		System.out.println(" done. [" + ((System.currentTimeMillis() - start)/1000) + " s]");
	}


	/**
	 * @return the dim
	 */
	public int getDim()
	{
		return this.dim;
	}


	/**
	 * @return the data
	 */
	public ArrayList<double[]> getData()
	{
		return this.data;
	}


	/**
	 * @return the clusters
	 */
	public ArrayList<ArrayList<double[]>> getClusters()
	{
		return this.clusters;
	}


	/**
	 * @return the clusterIndices
	 */
	public int[] getClusterIndices()
	{
		return this.clusterIndices;
	}
	
	
	
}
