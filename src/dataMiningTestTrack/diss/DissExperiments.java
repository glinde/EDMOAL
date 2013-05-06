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

import io.CSVFileWriter;
import io.FileLineReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import etc.SimpleStatistics;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class DissExperiments
{
	public static final String progressFilePath = "experiments/progress.txt";
	
	public static final int[] dims = new int[]{2, 3, 5, 7, 10, 15, 20, 30, 50, 70, 100};
	public static final int[] analyseClusters = new int[]{2, 3, 4, 5, 6, 7, 8, 10, 12, 15, 17, 20, 25, 30, 35, 40, 50, 60, 70, 80, 100, 120, 150, 170, 200, 250};

	public static final int clusters = analyseClusters[analyseClusters.length-1];
	public static final int dataPerCluster = 1000;
	public static final int setsPerConfig = 5;
	public static final int repititions = 2;
	public static final int maxIterations = 30;
	public static final double noiseFreq = 1.0d/9.0d;
	
	
	private static void genMixOfGauss(int dim, int clusterCount, int dataPerCluster, int repititions)
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
	
	private static void genMixOfGaussNoise(int dim, int clusterCount, int dataPerCluster, int noiseCount, int repititions)
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
	
	private static void genDistorted(int dim, int clusterCount, int dataPerCluster, int noiseCount, int repititions)
	{
		DecimalFormat format = new DecimalFormat();
		format.setMinimumIntegerDigits(3);
		format.setGroupingUsed(false);
		ClusterDataSetsGenerator dissFiles;
		String path = "experiments/data/Distorted/";
		
		for(int i=0; i<repititions; i++)
		{
			dissFiles = new ClusterDataSetsGenerator(dim, clusterCount);
			dissFiles.generateDistortedData(dataPerCluster, true, noiseCount, true, 6);
			dissFiles.generateInitialPositionsRandomUniform(10*clusterCount);
			dissFiles.store(path + format.format(dim) + "D/" + format.format(i) + "/");
			
			dissFiles=null;
			System.gc();
			try{Thread.sleep(100);} catch(InterruptedException e){}
		}
	}
	
	private static void genCorner(int dim, int clusterCount, int dataPerCluster, int noiseCount, int repititions)
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
		for(int i=0; i<dims.length; i++) genMixOfGauss(dims[i], clusters, dataPerCluster, setsPerConfig);
		for(int i=0; i<dims.length; i++) genMixOfGaussNoise(dims[i], clusters, dataPerCluster, clusters*dataPerCluster/5, setsPerConfig);
		for(int i=0; i<dims.length; i++) genDistorted(dims[i], clusters, dataPerCluster, clusters*dataPerCluster/5, setsPerConfig);
		for(int i=0; i<dims.length; i++) if(dims[i] >= 10) genCorner(dims[i], clusters, dataPerCluster, clusters*dataPerCluster/5, setsPerConfig);
	}
	
	public static void showSomeDataSets()
	{
		DissDataViewer dataViewer = new DissDataViewer();
		try
		{
			dataViewer.viewDissDataFile("experiments/data/Corner/070D/009/cluster_211.csv");
			dataViewer.viewDissDataFile("experiments/data/Corner/070D/009/cluster_156.csv");
			dataViewer.viewDissDataFile("experiments/data/Corner/070D/009/cluster_006.csv");
			dataViewer.viewDissDataFile("experiments/data/Corner/070D/009/cluster_234.csv");
		}
		catch (FileNotFoundException e)	{e.printStackTrace();}
	}
	
	private static void performExperiments(String experimentSelectionPath, File progressFile, Set<String> progressSet)
	{		
		System.out.print("Perform Experiments in: " + experimentSelectionPath);
		
		if(progressSet.contains(experimentSelectionPath))
		{
			System.out.println(" .. skipped.");
			return;
		}
		else
		{
			System.out.println("");
		}
		
		long time;
		ExperimentPerformer expPerformer = new ExperimentPerformer("experiments/data/"+experimentSelectionPath,
				"experiments/clusterings/"+experimentSelectionPath, maxIterations, repititions, analyseClusters, noiseFreq);
		
		System.out.print("load data.. "); time = System.currentTimeMillis(); 
		expPerformer.loadExperimentData();
		System.out.println(" done. [" + (System.currentTimeMillis() - time) + "]");
		
		System.out.print("generate experiments.. "); time = System.currentTimeMillis();
		expPerformer.generateExperiments();
		System.out.println(" done. [" + (System.currentTimeMillis() - time) + "]");
		
		System.out.print("perform Experiments.. "); time = System.currentTimeMillis();
		expPerformer.performExperiments();
		System.out.println(" done. [" + (System.currentTimeMillis() - time) + "]");
		
		System.out.print("save results.. "); time = System.currentTimeMillis();
		expPerformer.storeResults();
		System.out.println(" done. [" + (System.currentTimeMillis() - time) + "]");
		
		try
		{
			FileWriter writer = new FileWriter(progressFile, true);
			writer.write(experimentSelectionPath + "\n");
			writer.flush();
			writer.close();
			progressSet.add(experimentSelectionPath);
		}
		catch(IOException e1)
		{
			e1.printStackTrace();
		}
		
		System.gc();
		try{Thread.sleep(1000);} catch(InterruptedException e){e.printStackTrace();}
	}
	
	public static void clusterDissDataSets()
	{
		String experimentSubSelectionPath;
		DecimalFormat format = new DecimalFormat();
		format.setMinimumIntegerDigits(3);
		format.setGroupingUsed(false);
		
		TreeSet<String> progressSet = new TreeSet<String>();
		File progressFile = new File(progressFilePath);
		
		try
		{
			if(progressFile.exists() && progressFile.isFile())
			{
				FileLineReader reader = new FileLineReader();
				reader.openFile(progressFile);
				while(reader.ready()) progressSet.add(reader.readStringLine());
				reader.closeFile();
			}
			else
			{
				if(!progressFile.isFile()) throw new IOException("progress file path is not a normal file. " + progressFilePath);
				progressFile.getParentFile().mkdirs();
				progressFile.createNewFile();
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

		for(int k=0; k<setsPerConfig; k++)
		{
			for(int i=0; i<dims.length; i++)
			{
				experimentSubSelectionPath = format.format(dims[i]) + "D/" + format.format(k) + "/";
				
				performExperiments("MixNormal/" + experimentSubSelectionPath, progressFile, progressSet);
				performExperiments("MixNormalNoise/" + experimentSubSelectionPath, progressFile, progressSet);
				performExperiments("Distorted/" + experimentSubSelectionPath, progressFile, progressSet);
				if(dims[i] >= 10) performExperiments("Corner/" + experimentSubSelectionPath, progressFile, progressSet);
			}
		}	
	}
	
	public static void calculateClusterProperties()
	{
		DecimalFormat format = new DecimalFormat();
		format.setMinimumIntegerDigits(3);
		format.setGroupingUsed(false);
				
		ArrayList<String> algorithmDirs = new ArrayList<String>();
		algorithmDirs.add("experiments/data/MixNormal/");
		algorithmDirs.add("experiments/data/MixNormalNoise/");
		algorithmDirs.add("experiments/data/Distorted/");
		algorithmDirs.add("experiments/data/Corner/");
		
		ArrayList<String> attrNames = new ArrayList<String>();
//		attrNames.add("id");
		attrNames.add("size");
		attrNames.add("RadiusMax");
		attrNames.add("RadiusMean");
		attrNames.add("RadiusDeviation");
		attrNames.add("CentreRV");
		attrNames.add("MaxRV");
		
		
		CSVFileWriter writer = new CSVFileWriter();
		writer.setAddFirstAttributeAsID(true);
		writer.setFirstLineAsAtributeNames(true);
		
		for(String s:algorithmDirs)
		{
			File algoDir = new File(s);
			if(!algoDir.exists() || !algoDir.isDirectory())
			{
				System.out.println("Warning: The path \""+s+"\" does not exist or is not a directory. Ignoring.");
				continue;
			}
			File[] dimDirs = algoDir.listFiles();
			Arrays.sort(dimDirs);
			
			for(int i=0; i<dimDirs.length; i++)
			{
				if(!dimDirs[i].isDirectory()) continue;
													
				ArrayList<double[]> valueList = ClusterPropertyTester.analyseClusters(dimDirs[i]);
				
				try
				{
					writer.openFile(new File(dimDirs[i].getPath()+"/clusterProperties.csv"));
					writer.writeDoubleDataTable(valueList, attrNames);
					writer.closeFile();
				}
				catch(IOException e)
				{
					writer.closeFile();
					e.printStackTrace();
				}
				
				double[][] valueMatrix = new double[valueList.get(0).length][valueList.size()];
				
				for(int j=0; j<valueList.size(); j++)
				{
					for(int k=0; k<valueMatrix.length; k++)
					{
						valueMatrix[k][j] = valueList.get(j)[k];
					}
				}
				
				try
				{
					writer.openFile(new File(dimDirs[i]+"/clusterStatistics.ini"));
					writer.writeStringLine("Interpretation=" + "[min, max, mean, sample standard deviation]");
					writer.writeStringLine("DataObjects=" + Arrays.toString(SimpleStatistics.min_max_mean_deviation(valueMatrix[0])));
					writer.writeStringLine("RadiusMax=" + Arrays.toString(SimpleStatistics.min_max_mean_deviation(valueMatrix[1])));
					writer.writeStringLine("RadiusMean=" + Arrays.toString(SimpleStatistics.min_max_mean_deviation(valueMatrix[2])));
					writer.writeStringLine("RadiusDeviation=" + Arrays.toString(SimpleStatistics.min_max_mean_deviation(valueMatrix[3])));
					writer.writeStringLine("CentreRV=" + Arrays.toString(SimpleStatistics.min_max_mean_deviation(valueMatrix[4])));
					writer.writeStringLine("MaxRV=" + Arrays.toString(SimpleStatistics.min_max_mean_deviation(valueMatrix[5])));
					writer.closeFile();
				}
				catch(IOException e)
				{
					writer.closeFile();
					e.printStackTrace();
				}
			}
		}
	}
}
