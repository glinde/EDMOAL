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
package dataMiningTestTrack.diss;

import java.text.DecimalFormat;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class DissExperiments
{
	public static void genMixOfGauss(int dim, int clusterCount, int dataPerCluster, int repititions)
	{
		DecimalFormat format = new DecimalFormat();
		format.setMinimumIntegerDigits(3);
		format.setGroupingUsed(false);
		ClusterDataSetsGenerator dissFiles;
		String path = "experiments/data/MixNormal/";
		
		for(int i=0; i<repititions; i++)
		{
			dissFiles = new ClusterDataSetsGenerator(dim, clusterCount);
			dissFiles.generateUniformDistributedNormalClusteres(dataPerCluster, false, 0, 0.01d, false);
			dissFiles.generateInitialPositionsRandomUniform(10*clusterCount);
			dissFiles.store(path + format.format(dim) + "D/" + format.format(i) + "/");
			
			dissFiles=null;
			System.gc();
			try{Thread.sleep(100);} catch(InterruptedException e){}
		}
	}
	
	public static void genMixOfGaussNoise(int dim, int clusterCount, int dataPerCluster, int noiseCount, int repititions)
	{
		DecimalFormat format = new DecimalFormat();
		format.setMinimumIntegerDigits(3);
		format.setGroupingUsed(false);
		ClusterDataSetsGenerator dissFiles;
		String path = "experiments/data/MixNormalNoise/";
		
		for(int i=0; i<repititions; i++)
		{
			dissFiles = new ClusterDataSetsGenerator(dim, clusterCount);
			dissFiles.generateUniformDistributedNormalClusteres(dataPerCluster, true, noiseCount, 0.01d, true);
			dissFiles.generateInitialPositionsRandomUniform(10*clusterCount);
			dissFiles.store(path + format.format(dim) + "D/" + format.format(i) + "/");
			
			dissFiles=null;
			System.gc();
			try{Thread.sleep(100);} catch(InterruptedException e){}
		}
	}
	
	public static void genDistorted(int dim, int clusterCount, int dataPerCluster, int noiseCount, int repititions)
	{
		DecimalFormat format = new DecimalFormat();
		format.setMinimumIntegerDigits(3);
		format.setGroupingUsed(false);
		ClusterDataSetsGenerator dissFiles;
		String path = "experiments/data/Distorted/";
		
		for(int i=0; i<repititions; i++)
		{
			dissFiles = new ClusterDataSetsGenerator(dim, clusterCount);
			dissFiles.generateDistortedData(dataPerCluster, true, noiseCount, true, 4);
			dissFiles.generateInitialPositionsRandomUniform(10*clusterCount);
			dissFiles.store(path + format.format(dim) + "D/" + format.format(i) + "/");
			
			dissFiles=null;
			System.gc();
			try{Thread.sleep(100);} catch(InterruptedException e){}
		}
	}
	
	public static void genCorner(int dim, int clusterCount, int dataPerCluster, int noiseCount, int repititions)
	{
		DecimalFormat format = new DecimalFormat();
		format.setMinimumIntegerDigits(3);
		format.setGroupingUsed(false);
		ClusterDataSetsGenerator dissFiles;
		String path = "experiments/data/Corner/";
		int cluster1 = (int)(Math.sqrt(dim));
		int flip1 = (int)(Math.sqrt(cluster1));
		int flip0 = (int)(Math.sqrt(cluster1))/2;
		
		for(int i=0; i<repititions; i++)
		{
			dissFiles = new ClusterDataSetsGenerator(dim, clusterCount);			
			dissFiles.generateCornerCentricClusteres(dataPerCluster, true, clusterCount, noiseCount, cluster1, true, flip1, flip0);		
			dissFiles.generateInitialPositionsRandomUniform(10*clusterCount);
			dissFiles.store(path + format.format(dim) + "D/" + format.format(i) + "/");
			
			dissFiles=null;
			System.gc();
			try{Thread.sleep(dim);} catch(InterruptedException e){}
		}
	}
	
	public static void genDissDataSets()
	{
		int[] dims = new int[]{2, 3, 5, 7, 10, 15, 20/*, 30, 50, 70, 100*/};
		int clusters = 250;
		int dataPerCluster = 1000;
		int repetitions = 20;
		
		for(int i=0; i<dims.length; i++) genMixOfGauss(dims[i], clusters, dataPerCluster, repetitions);
//		for(int i=0; i<dims.length; i++) genMixOfGaussNoise(dims[i], clusters, dataPerCluster, clusters*dataPerCluster/5, repetitions);
//		for(int i=0; i<dims.length; i++) genDistorted(dims[i], clusters, dataPerCluster, clusters*dataPerCluster/5, repetitions);
//		for(int i=4; i<dims.length; i++) genCorner(dims[i], clusters, dataPerCluster, clusters*dataPerCluster/5, repetitions);
	}
	
	public static void clusterDissDataSets()
	{
		int[] dims = new int[]{2, 3, 5, 7, 10, 15, 20/*, 30, 50, 70, 100*/};
		ExperimentPerformer expPerformer;
		String experimentSelectionPath;
		DecimalFormat format = new DecimalFormat();
		format.setMinimumIntegerDigits(3);
		format.setGroupingUsed(false);
		
		
		for(int i=0; i<dims.length; i++)
		{
			for(int k=0; k<20; k++)
			{
				experimentSelectionPath = format.format(dims[i]) + "D/" + format.format(k) + "/";
				
				expPerformer = new ExperimentPerformer("experiments/data/MixNormal/"+experimentSelectionPath, "experiments/results/MixNormal/", "experiments/results/MixNormal/"+experimentSelectionPath);
				
				expPerformer.loadExperimentData();
				expPerformer.loadExperimentSetup();
				expPerformer.generateExperiments();
				expPerformer.performExperiments();
				expPerformer.storeResults();
				
				System.gc();
				try{Thread.sleep(1000);} catch(InterruptedException e){e.printStackTrace();}
			}
		}		
	}
}
