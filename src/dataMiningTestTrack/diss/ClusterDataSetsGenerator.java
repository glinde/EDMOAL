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


package dataMiningTestTrack.diss; 

import generation.data.ClusteredDataSetGenerator;
import generation.data.HyperrectangleUniformGenerator;
import io.CSVFileWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;

import dataMiningTestTrack.tests.TestVisualizer;


/**
 * This class provides some functionality to test cluster algorithms and to verify the result visually.
 * The data for testing is generated artificially, which is also done by this class.<br>
 * 
 * The data set is generated in the following way: The number of clusters determines the
 * number of seeds that are uniformly distributed in the hypercube with cornes at each
 * dimension at 0.1 and 0.9 (the hypercube is specified by [0.1, 0.9]^dim).
 * For each seed, a set of dim-dimensional normal distributed data objects is generated.
 * The variance of the (dim-dimensional) normal distribution is randomly picked between 0 and 0.1.
 * Finally, 10% of noise data objects is added to the data set.
 * The noise data objects are generated uniformly on the unit-hypercube ([0, 1]^dim) 
 * 
 *
 * @author Roland Winkler
 */
public class ClusterDataSetsGenerator extends TestVisualizer implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= -4791280640176444354L;

	
	private ClusteredDataSetGenerator clusterGen;
	
	/**
	 * The data set to be clustered
	 */
//	private IndexedDataSet<double[]> dataSet;
	private ArrayList<ArrayList<double[]>> clusteredData;
	private ArrayList<double[]> noiseData;
	
	/**  */
	private int dim;
	/**
	 * The number of clusters
	 */
	private int clusterCount;
	
	private int dataSetSize;
			
	/**
	 * The correct clustering (partitioning) according to the data generation process.
	 */
//	private int[] correctClustering;
	
	/**  */
//	private ArrayList<double[]> lastFuzzyClusteringResult;

	/**  */
//	private int[] lastCrispClusteringResult;
	
	/**
	 * The initial positions of prototypes.
	 */
	private ArrayList<double[]> initialPositons;
	
	
	public ClusterDataSetsGenerator(int dim, int clusterCount)
	{
		this.dim = dim;
		this.clusterCount = clusterCount;
		this.dataSetSize = 0;
		this.clusterGen = new ClusteredDataSetGenerator(this.dim);
//		this.dataSet = new IndexedDataSet<double[]>();
//		this.correctClustering = new int[0];
		this.initialPositons = new ArrayList<double[]>();
		this.clusteredData = null;
		this.noiseData = null;
//		this.lastFuzzyClusteringResult = new ArrayList<double[]>();
//		this.lastCrispClusteringResult = new int[0];
	}
//
//	/**
//	 * Shows the generated data set.
//	 */
//	public void showDataSet(int x, int y)
//	{
//		this.xIndex = x;
//		this.yIndex = y;
//		this.showDataSet(this.dataSet, null);
//	}
//
//	/**
//	 * Shows the generated data set.
//	 */
//	public void showDataSetClustered(int x, int y, int[] clustering, String filename)
//	{
//		this.xIndex = x;
//		this.yIndex = y;
//		this.showCrispDataSetClustering(this.dataSet, this.clusterCount, clustering, filename);
//	}
//	
//	/**
//	 * Shows the generated data set.
//	 */
//	public void showDataSetClustered(int x, int y, Collection<double[]> clustering, String filename)
//	{
//		this.xIndex = x;
//		this.yIndex = y;
//		this.showFuzzyDataSetClustering(this.dataSet, clustering, filename);
//	}
//	
//	public void showDataSetClustered2DProjections(double addA, double addB, String filename)
//	{
//		if(filename == null) filename = "Clustered Data Set";
//		
//		for(int i=0; i<this.dim; i+=Math.max(addA, 1.0d))
//		{
//			for(int j=i+1; j<this.dim; j+=Math.max(addB, 1.0d))
//			{
//				this.showDataSetClustered(i, j, this.correctClustering, filename + "_proj_" + i + "-" + j);
//			}
//		}
//	}
//
//	public void showCrispClusteringResult2DProjections(double addA, double addB, String filename)
//	{
//		if(filename == null) filename = "Crisp Clustering Result";
//		
//		for(int i=0; i<this.dim; i+=Math.max(addA, 1.0d))
//		{
//			for(int j=i+1; j<this.dim; j+=Math.max(addB, 1.0d))
//			{
//				this.showDataSetClustered(i, j, this.lastCrispClusteringResult, filename + "_proj_" + i + "-" + j);
//			}
//		}
//	}
//
//	public void showFuzzyClusteringResult2DProjections(double addA, double addB, String filename)
//	{
//		if(filename == null) filename = "Fuzzy Clustering Result";
//		
//		for(int i=0; i<this.dim; i+=Math.max(addA, 1.0d))
//		{
//			for(int j=i+1; j<this.dim; j+=Math.max(addB, 1.0d))
//			{
//				this.showDataSetClustered(i, j, this.lastFuzzyClusteringResult, filename + "_proj_" + i + "-" + j);
//			}
//		}
//	}
	
	public void generateCornerCentricClusteres(int dataObjectsPerClusterCount, boolean randomDataObjectsCount, int clusterCount, int noiseCount, int cluster1Count, boolean randomCluster1Count, int cluster1FlipsFreq, int cluster0FlipsFreq)
	{		
		this.clusterGen.generateCornerClusteredDataSet(dataObjectsPerClusterCount, randomDataObjectsCount, clusterCount, noiseCount, cluster1Count, randomCluster1Count, cluster1FlipsFreq, cluster0FlipsFreq);
//		this.clusterGen.shuffle();
//		
//		this.dataSet = new IndexedDataSet<double[]>(this.clusterGen.getData().size());
//		for(double[] x:this.clusterGen.getData()) this.dataSet.add(new IndexedDataObject<double[]>(x));
//		
//		this.dataSet.seal();
//		
//		this.correctClustering = this.clusterGen.getClusterIndices();
		
		this.clusteredData = this.clusterGen.getClusters();
		this.noiseData = this.clusterGen.getNoise();
		this.dataSetSize = this.clusterGen.getData().size();
	}
	
	public void generateDistortedData(int dataPerClusterCount, boolean randomClusterSize, int noise, boolean scale, int shuffleLocation)
	{		
		this.clusterGen.generateDistortedClusteredDataSet(dataPerClusterCount, randomClusterSize, this.clusterCount, noise, scale, shuffleLocation);
//		this.clusterGen.shuffle();
//		
//		this.dataSet = new IndexedDataSet<double[]>(clusterGen.getData().size());
//		for(double[] x:clusterGen.getData()) this.dataSet.add(new IndexedDataObject<double[]>(x));
//		
//		this.dataSet.seal();
//		
//		this.correctClustering = clusterGen.getClusterIndices();

		this.clusteredData = this.clusterGen.getClusters();
		this.noiseData = this.clusterGen.getNoise();
		this.dataSetSize = this.clusterGen.getData().size();
	}
	
	public void generateUniformDistributedNormalClusteres(int dataPerClusterCount, boolean randomClusterSize, int noise, double clusterVariance, boolean randomVariance)
	{		
		this.clusterGen.generateUniformNormalClusteredDataSet(dataPerClusterCount, randomClusterSize, this.clusterCount, noise, clusterVariance, randomVariance);
//		this.clusterGen.shuffle();
//		
//		this.dataSet = new IndexedDataSet<double[]>(clusterGen.getData().size());
//		for(double[] x:clusterGen.getData()) this.dataSet.add(new IndexedDataObject<double[]>(x));
//		
//		this.dataSet.seal();
//		
//		this.correctClustering = clusterGen.getClusterIndices();
		
		this.clusteredData = this.clusterGen.getClusters();
		this.noiseData = this.clusterGen.getNoise();
		this.dataSetSize = this.clusterGen.getData().size();
	}
	
	public void generateInitialPositionsRandomUniform(int initPosCount)
	{
		this.initialPositons = new ArrayList<double[]>();
				
		HyperrectangleUniformGenerator gen = new HyperrectangleUniformGenerator(this.dim);
		
		this.initialPositons.addAll(gen.generateDataObjects(initPosCount));
	}

	public void store(String foldername)
	{
		System.out.println("Store in " + foldername);
		File folder = new File(foldername);		
		if(!folder.exists()) folder.mkdirs();
		DecimalFormat format = new DecimalFormat();
		format.setMinimumIntegerDigits(3);
		format.setGroupingUsed(false);
		
		// store meta information
		try
		{
			FileWriter writer;
			writer = new FileWriter(foldername + "/meta.ini");
			writer.write("dim="+this.dim+"\n");
			writer.write("availableClusterCount="+this.clusterCount+"\n");
			writer.write("availableDataCount="+this.dataSetSize+"\n");
			for(int i=0; i<this.clusterCount; i++)
			{
				writer.write("clusterSize_"+i+"="+this.clusteredData.get(i).size()+"\n");
			}
			writer.write("availableNoiseCount="+this.clusterGen.getNoise().size()+"\n");
			writer.flush();
			writer.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		// store data information
		try
		{
			CSVFileWriter csvWriter = new CSVFileWriter();
			csvWriter.setAddFirstAttributeAsID(true);
			csvWriter.setFirstLineAsAtributeNames(true);
			csvWriter.setDefaultAttributeName("");
			for(int i=0; i<this.clusterCount; i++)
			{
				csvWriter.openFile(new File(foldername + "/cluster_"+format.format(i)+".csv"));
				csvWriter.writeDoubleDataTable(this.clusteredData.get(i), null);
				csvWriter.closeFile();
			}
			if(this.noiseData != null && this.noiseData.size() > 0)
			{
				csvWriter.openFile(new File(foldername + "/noise.csv"));
				csvWriter.writeDoubleDataTable(this.noiseData, null);
				csvWriter.closeFile();
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

		// store initial positions
		try
		{
			CSVFileWriter csvWriter = new CSVFileWriter();
			csvWriter.openFile(new File(foldername + "/init.csv"));
			csvWriter.setAddFirstAttributeAsID(true);
			csvWriter.setFirstLineAsAtributeNames(true);
			csvWriter.setDefaultAttributeName("");
			csvWriter.writeDoubleDataTable(this.initialPositons, null);
			csvWriter.closeFile();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

		// store cluster result
//		try
//		{
//			FileWriter writer;
//			writer = new FileWriter(foldername + "/res.csv");
//			writer.write("ID;Cluster\n");
//			for(int j=0; j<this.correctClustering.length; j++)
//			{
//				writer.write(j+";"+this.correctClustering[j]+"\n");
//				if((j+1) % 1000 == 0) writer.flush();
//			}
//			writer.flush();
//			writer.close();
//		}
//		catch(IOException e)
//		{
//			e.printStackTrace();
//		}
	}
	
	public void printDataStatistics()
	{
		System.out.println("========== Data Statistics ==========");
		System.out.println("Dimensionality: " + this.dim);
		System.out.println("Number of data Objects: " + this.dataSetSize);
		for(int i=0; i<this.clusterCount; i++)
		{
			System.out.println("\tSize of cluster "+i+": " + this.clusterGen.getClusters().get(i).size());
		}
			
		System.out.println("Number of noise data Objects: " + this.clusterGen.getNoise().size());
	}
}
