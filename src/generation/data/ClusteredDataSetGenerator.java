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

import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.distribution.HypergeometricDistribution;
import org.apache.commons.math3.distribution.IntegerDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.commons.math3.random.RandomData;
import org.apache.commons.math3.random.RandomDataGenerator;

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

	private ArrayList<double[]> noise;
	
	private int[] clusterIndices;
	
	public ClusteredDataSetGenerator(int dim)
	{
		this.dim = dim;
		this.data = new ArrayList<double[]>();
		this.clusters = new ArrayList<ArrayList<double[]>>();
		this.noise = new ArrayList<double[]>();
		this.clusterIndices = null;
	}

	public void generateCornerClusteredDataSet(int dataObjectsPerClusterCount, boolean randomDataObjectsCount, int clusterCount, int noiseCount, int cluster1Count, boolean randomCluster1Count, int cluster1FlipsCount, int cluster0FlipsCount)
	{
		System.out.print("Generate corner centric data ... ");
		
		this.data.clear();
		this.clusters.clear();
		this.noise.clear();

		UniformRealDistribution uniGen = new UniformRealDistribution(0.0d, 1.0d);
		RandomDataGenerator random = new RandomDataGenerator();
		PoissonDistribution poissonC = new PoissonDistribution(cluster1Count);
		int[] clusterPermutation; // = random.nextPermutation(this.data.size(), this.data.size());
		int[] flips1Permutation;
		int[] flips0Permutation;
		int totalDataObjects = 0;
		int clusterSize;
		int cluster1C;
		int cluster1FlipsC;
		int cluster0FlipsC;
		double[] x;
		
		for(int i=0; i<clusterCount; i++)
		{
			// basic cluster values
			clusterSize = randomDataObjectsCount? dataObjectsPerClusterCount/5+(int)(9.0d/5.0d*uniGen.sample()*dataObjectsPerClusterCount) : dataObjectsPerClusterCount;
			cluster1C = randomCluster1Count? poissonC.sample() : cluster1Count;
			if(cluster1C > this.dim / 2) cluster1C = this.dim / 2; 
			
			// generate permutation of all dimensions. The first cluster1C elements belong to the cluster
			clusterPermutation = random.nextPermutation(this.dim, this.dim);

			// new distributions for this clusters flips.
			PoissonDistribution poisson1 = new PoissonDistribution(cluster1FlipsCount+0.0001d);
			PoissonDistribution poisson0 = new PoissonDistribution(cluster0FlipsCount+0.0001d);
			
			ArrayList<double[]> cluster = new ArrayList<double[]>(clusterSize);
			
			for(int j=0; j<clusterSize; j++)
			{
				x = new double[this.dim];

				// number of flips in both directions
				cluster1FlipsC = poisson1.sample();
				cluster0FlipsC = poisson0.sample();
				
				// bound number of flips
				cluster1FlipsC = (cluster1FlipsC>cluster1C/2)? cluster1C/2:cluster1FlipsC;
				cluster0FlipsC = (cluster0FlipsC>(this.dim-cluster1C)/2)? (this.dim-cluster1C)/2:cluster0FlipsC;
								
				// generate flip-permutations.. 1-to-0 flips act on the first cluster1C elements of the clusterPermutation,
				// the 0-to-1 flips act on the remaining dim-cluster1C elements of the clusterPermutation.
				flips1Permutation = (cluster1FlipsC>0)? random.nextPermutation(cluster1C, cluster1FlipsC): new int[0];
				flips0Permutation = (cluster0FlipsC>0)? random.nextPermutation(this.dim-cluster1C, cluster0FlipsC): new int[0];
				
				// setup standard cluster
				for(int k=0; k<cluster1C; k++)
				{
					x[clusterPermutation[k]] = 1.0d;
				}
				// turn some of the 1's into 0's
				for(int k=0; k<cluster1FlipsC; k++)
				{
					x[clusterPermutation[flips1Permutation[k]]] = 0.0d;
				}
				// turn some of the initially remaining 0's into 1's
				for(int k=0; k<cluster0FlipsC; k++)
				{
					x[clusterPermutation[cluster1C+flips0Permutation[k]]] = 1.0d;
				}
				
				cluster.add(x);
//				for(int k=0; k<this.dim; k++)
//				{
//					System.out.print((int)x[k]);
//				}
//				System.out.println("");
			}
			
			this.clusters.add(cluster);
			totalDataObjects += clusterSize;
		}
		
//		for(int k=0; k<this.dim; k++) System.out.print("-");
//		System.out.println("");

		for(int i=0; i<noiseCount; i++)
		{
			x = new double[this.dim];
			
			cluster1C = randomCluster1Count? poissonC.sample() : cluster1Count;
			clusterPermutation = random.nextPermutation(this.dim, this.dim);
			if(cluster1C > this.dim) cluster1C = this.dim; 
			for(int k=0; k<cluster1C; k++)
			{
				x[clusterPermutation[k]] = 1.0d;
			}
			
			this.noise.add(x);
//			for(int k=0; k<this.dim; k++)
//			{
//				System.out.print((int)x[k]);
//			}
//			System.out.println("");
		}
		totalDataObjects += noiseCount;				
		
		this.clusterIndices = new int[totalDataObjects];		
		this.data.ensureCapacity(totalDataObjects);		
		int dataIndex=0;
		
		for(int i=0; i<clusterCount; i++)
		{
			for(int j=0; j<this.clusters.get(i).size(); j++)
			{
				this.data.add(this.clusters.get(i).get(j));
				this.clusterIndices[dataIndex] = i;
				dataIndex++;
			}
		}
		
		for(int j=0; j<this.noise.size(); j++)
		{
			this.data.add(this.noise.get(j));
			this.clusterIndices[dataIndex] = -1;
			dataIndex++;
		}
				
		System.out.println(" done.");
	}
	
	public void generateUniformNormalClusteredDataSet(int dataObjectsPerClusterCount, boolean randomDataObjectsCount, int clusterCount, int noiseCount, double clusterVariance, boolean randomVariance)
	{
		System.out.print("Generate Uniform Normal data ... ");
		
		this.data.clear();
		this.clusters.clear();
		this.noise.clear();
		
		ArrayList<double[]> seeds = new ArrayList<double[]>(clusterCount);
		UniformRealDistribution uniGen = new UniformRealDistribution(0.0d, 1.0d);
		NormalDistribution normGen = new NormalDistribution(0.0d, 1.0d);
		double variance;
		int clusterSize;
		int totalDataObjects = 0;
		
		for(int i=0; i<clusterCount; i++)
		{
			seeds.add(uniGen.sample(this.dim));			
		}
		
		for(int i=0; i<clusterCount; i++)
		{
			clusterSize = randomDataObjectsCount? dataObjectsPerClusterCount/5+(int)(9.0d/5.0d*uniGen.sample()*dataObjectsPerClusterCount) : dataObjectsPerClusterCount;
			variance = randomVariance? 2.0d*uniGen.sample()*clusterVariance : clusterVariance;
			ArrayList<double[]> cluster = new ArrayList<double[]>(clusterSize);
			
			for(int j=0; j<clusterSize; j++)
			{
				double[] x = new double[this.dim];
				
				for(int k=0; k<this.dim; k++)
				{
					x[k] = variance*normGen.sample() + seeds.get(i)[k];
				}
				
				cluster.add(x);
			}
			
			this.clusters.add(cluster);
			totalDataObjects += clusterSize;
		}

		for(int i=0; i<noiseCount; i++)
		{
			double[] x = uniGen.sample(this.dim);
			this.noise.add(x);
		}
		totalDataObjects += noiseCount;				
		
		this.clusterIndices = new int[totalDataObjects];		
		this.data.ensureCapacity(totalDataObjects);		
		int dataIndex=0;
		
		for(int i=0; i<clusterCount; i++)
		{
			for(int j=0; j<this.clusters.get(i).size(); j++)
			{
				this.data.add(this.clusters.get(i).get(j));
				this.clusterIndices[dataIndex] = i;
				dataIndex++;
			}
		}
		
		for(int j=0; j<this.noise.size(); j++)
		{
			this.data.add(this.noise.get(j));
			this.clusterIndices[dataIndex] = -1;
			dataIndex++;
		}
				
		System.out.println(" done.");
	}
	
	public void generateDistortedClusteredDataSet(int dataObjectsPerClusterCount, boolean randomDataObjectsCount, int clusterCount, int noiseCount, boolean scale, int shuffleLocation)
	{
//		System.out.print("Generate distorted data ... ");
		
		this.data.clear();
		this.clusters.clear();
		this.noise.clear();
		
		ArrayList<double[]> seeds = new ArrayList<double[]>(clusterCount);
		UniformRealDistribution uniGen = new UniformRealDistribution(0.0d, 1.0d);
		int clusterSize;
		int totalDataObjects = 0;
		
		for(int i=0; i<clusterCount; i++)
		{
			seeds.add(uniGen.sample(this.dim));			
		}
		
		for(int i=0; i<clusterCount; i++)
		{
			clusterSize = randomDataObjectsCount? dataObjectsPerClusterCount/5+(int)(9.0d/5.0d*uniGen.sample()*dataObjectsPerClusterCount) : dataObjectsPerClusterCount;
			ArrayList<double[]> cluster = this.generateRandomData(new int[]{clusterSize/2, clusterSize/2 + clusterSize%2});
			
//			System.out.println("======= Distort cluster " + i + " ======="); 		
			this.distortCluster(cluster, scale, shuffleLocation);
						
			this.clusters.add(cluster);
			totalDataObjects += clusterSize;
		}

		for(int i=0; i<noiseCount; i++)
		{
			double[] x = uniGen.sample(this.dim);
			this.noise.add(x);
		}
		totalDataObjects += noiseCount;				
		
		this.clusterIndices = new int[totalDataObjects];		
		this.data.ensureCapacity(totalDataObjects);		
		int dataIndex=0;
		
		for(int i=0; i<clusterCount; i++)
		{
			for(int j=0; j<this.clusters.get(i).size(); j++)
			{
				this.data.add(this.clusters.get(i).get(j));
				this.clusterIndices[dataIndex] = i;
				dataIndex++;
			}
		}
		
		for(int j=0; j<this.noise.size(); j++)
		{
			this.data.add(this.noise.get(j));
			this.clusterIndices[dataIndex] = -1;
			dataIndex++;
		}
				
//		System.out.println(" done.");
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

//		System.out.print("generate distortions ... ");
		// distort data
		for(int i=0; i<2*dim; i++) distorter.addDistortionLayers(new double[]{1.0, 0.5d/dim, 20.0d/dim, 10.0d/dim, 10.0d/dim, 1.2d/dim},  new double[]{0.0d, 0.1, 0.1, 1.0d/dim, Math.log(dim)/dim});

		// scale to fill the unit hypercube
		if(scale) distorter.addDistortionLayers(new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0d},  new double[]{1.0d, 0.0, 0.0, 0.0, 0.0});

		// push the data randomly in the direction of the upper right corner 
		for(int i=0; i<shuffleLocation; i++) distorter.addDistortionLayers(new double[]{0.7, 0.0, 0.0, 0.3, 0.0, 0.0d},  new double[]{1.0d, 0.0, 0.0, 0.0, 0.0});

		// randomly reverse data to randomize the data in the feature space
		if(shuffleLocation > 0) distorter.addDistortionLayers(new double[]{0.5, 0.5, 0.0, 0.0, 0.0, 0.0d},  new double[]{1.0d, 0.0, 0.0, 0.0, 0.0});
//		System.out.println(" done.");

//		System.out.print("Distort data ");
//		long start = System.currentTimeMillis();
		distorter.distortData(clusterData);
//		System.out.println(" done. [" + ((System.currentTimeMillis() - start)/1000) + " s]");
	}

	public void shuffle()
	{
		RandomDataGenerator random = new RandomDataGenerator();
		int[] permutation = random.nextPermutation(this.data.size(), this.data.size());
		
		ArrayList<double[]> shuffledData = new ArrayList<double[]>(this.data.size());
		int[] shuffledIndices = new int[this.data.size()];
		
		for(int i=0; i<this.data.size(); i++)
		{
			shuffledData.add(this.data.get(permutation[i]));
			shuffledIndices[i] = this.clusterIndices[permutation[i]];
		}
		
		this.data = shuffledData;
		this.clusterIndices = shuffledIndices;
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

	/**
	 * @return the noise
	 */
	public ArrayList<double[]> getNoise()
	{
		return this.noise;
	}
	
	
	
}
