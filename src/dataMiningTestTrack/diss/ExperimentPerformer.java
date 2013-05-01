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

import io.CSVFileReader;
import io.CSVFileWriter;
import io.FileLineReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.TreeMap;

import org.apache.commons.math3.random.RandomDataGenerator;

import data.objects.doubleArray.DAEuclideanMetric;
import data.objects.doubleArray.DAEuclideanVectorSpace;
import data.set.IndexedDataObject;
import data.set.IndexedDataSet;
import datamining.clustering.protoype.AbstractPrototypeClusteringAlgorithm;
import datamining.clustering.protoype.Centroid;
import datamining.clustering.protoype.Prototype;
import datamining.clustering.protoype.PrototypeClusteringAlgorithm;
import datamining.clustering.protoype.SphericalNormalDistributionPrototype;
import datamining.clustering.protoype.altopt.ExpectationMaximizationSGMMClusteringAlgorithm;
import datamining.clustering.protoype.altopt.FuzzyCMeansClusteringAlgorithm;
import datamining.clustering.protoype.altopt.FuzzyCMeansNoiseClusteringAlgorithm;
import datamining.clustering.protoype.altopt.HardCMeansClusteringAlgorithm;
import datamining.clustering.protoype.altopt.PolynomFCMClusteringAlgorithm;
import datamining.clustering.protoype.altopt.PolynomFCMNoiseClusteringAlgorithm;
import datamining.clustering.protoype.altopt.RewardingCrispFCMClusteringAlgorithm;
import datamining.clustering.protoype.altopt.RewardingCrispFCMNoiseClusteringAlgorithm;
import etc.Parallel;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class ExperimentPerformer
{
	private String dataDirectory;
	
	private String setupDirectory;
	
	private String experimentDirectory;
		
	
	private int dim;
	
	private int availableClusterCount;
	
	private int availableDataCount;
	
	private int availableNoiseCount;
	
	private int[] clusterSize;
	
	
	private int maxIterations;
	
	private int dataSetRepitition;
	
	private int analyseClusterCount;
	
	private int[] analyseClusterList;
	
	private double noiseFraction;
	
	
	private ArrayList<ArrayList<double[]>> clusteredData;
	
	private ArrayList<double[]> availableInitialPositions; 
	
	private ArrayList<double[]> noiseData;
	
	private ArrayList<Experiment> experiments;
	
	private ArrayList<Experiment> EMGMM_Experiments;
	
	
	public ExperimentPerformer(String dataDirectory, String setupDirectory, String experimentDirectory)
	{
		super();
		
		this.dataDirectory = dataDirectory;
		this.setupDirectory = setupDirectory;
		this.experimentDirectory = experimentDirectory;


//		System.out.println("Load data from " + this.dataDirectory);
		File dir = new File(this.dataDirectory);		
		if(!dir.exists() || !dir.isDirectory()) throw new IllegalArgumentException("The path " + this.dataDirectory + " does not exist or is not a directory.");

//		System.out.println("Setup is loaded from " + this.setupDirectory);
		dir = new File(this.setupDirectory);		
		if(!dir.exists() || !dir.isDirectory()) throw new IllegalArgumentException("The path " + this.setupDirectory + " does not exist or is not a directory.");
		
		dir = new File(this.experimentDirectory);		
//		if(!dir.isDirectory()) throw new IllegalArgumentException("The path " + this.experimentDirectory + " is not a directory.");
		if(!dir.exists()) dir.mkdirs();
		
		// filled by meta file
		this.dim = 0;
		this.availableClusterCount = 0;
		this.availableDataCount = 0;
		this.availableNoiseCount = 0;
		this.clusterSize = null;
		
		// filled by experiment setup file
		this.maxIterations = 0;
		this.dataSetRepitition = 0;
		this.analyseClusterCount = 0;
		this.analyseClusterList = null;
		
		// filled by cluster data files
		this.clusteredData = new ArrayList<ArrayList<double[]>>();
		this.availableInitialPositions = new ArrayList<double[]>();
		this.noiseData = new ArrayList<double[]>();
		
		// filled by generate experiments
		this.experiments = new ArrayList<Experiment>();
		this.EMGMM_Experiments = new ArrayList<Experiment>();
	}

	private void loadMetaData() throws IOException
	{
		TreeMap<String, String> metaDataMap = new TreeMap<String, String>();
		File file = new File(this.dataDirectory + "/meta.ini");
		if(!file.exists()) throw new FileNotFoundException("Meta file not readable. " + file.getAbsolutePath() + " does not exists.");
		FileLineReader flReader = new FileLineReader();
		
		flReader.openFile(file);
		flReader.setSeperatorChar('=');
		ArrayList<String> lineList;
		try
		{
			while(flReader.ready())
			{	
				lineList = flReader.readStringListLine();
				if(lineList.size() == 2) metaDataMap.put(lineList.get(0), lineList.get(1));
			};
		}
		catch(IOException e)
		{
			flReader.closeFile();
			throw new IOException(e);
		}
		flReader.closeFile();
		
		this.dim = Integer.parseInt(metaDataMap.get("dim"));
		this.availableClusterCount = Integer.parseInt(metaDataMap.get("availableClusterCount"));
		this.availableDataCount = Integer.parseInt(metaDataMap.get("availableDataCount"));
		this.availableNoiseCount = Integer.parseInt(metaDataMap.get("availableNoiseCount"));
		this.clusterSize = new int[this.availableClusterCount];
		
		for(int i=0; i<this.availableClusterCount; i++)
		{
			this.clusterSize[i] = Integer.parseInt(metaDataMap.get("clusterSize_" + i));
		}
	}
	
	private void loadClusterData() throws IOException
	{
		File file;
		DecimalFormat format = new DecimalFormat();
		format.setMinimumIntegerDigits(3);
		format.setGroupingUsed(false);
		
		for(int i=0; i<this.availableClusterCount; i++)
		{
			file = new File(this.dataDirectory + "/cluster_"+format.format(i)+".csv");
			if(!file.exists()) throw new FileNotFoundException("Data file not readable. " + file.getAbsolutePath() + " does not exists.");
			CSVFileReader csvReader = new CSVFileReader();
			
			csvReader.openFile(file);
			csvReader.setSeperatorChar(';');
			csvReader.setIgnoreFirstAttribute(true);
			csvReader.setFirstLineAsAtributeNames(true);
			
			try
			{
				this.clusteredData.add(csvReader.readDoubleDataTable());
			}
			catch(IOException e)
			{
				csvReader.closeFile();
				throw new IOException(e);
			}
			
			csvReader.closeFile();
		}
		
		if(this.availableNoiseCount > 0)
		{
			file = new File(this.dataDirectory + "/noise.csv");
			if(!file.exists()) throw new FileNotFoundException("Data file not readable. " + file.getAbsolutePath() + " does not exists.");
			CSVFileReader csvReader = new CSVFileReader();
			
			csvReader.openFile(file);
			csvReader.setSeperatorChar(';');
			csvReader.setIgnoreFirstAttribute(true);
			csvReader.setFirstLineAsAtributeNames(true);
			try
			{
				this.noiseData = csvReader.readDoubleDataTable();
			}
			catch(IOException e)
			{
				csvReader.closeFile();
				throw new IOException(e);
			}
		}
	}
	
	private void loadInitials() throws IOException
	{
		File file = new File(this.dataDirectory + "/init.csv");
		if(!file.exists()) throw new FileNotFoundException("Data file not readable. " + file.getAbsolutePath() + " does not exists.");
		CSVFileReader csvReader = new CSVFileReader();
		
		csvReader.openFile(file);
		csvReader.setSeperatorChar(';');
		csvReader.setIgnoreFirstAttribute(true);
		csvReader.setFirstLineAsAtributeNames(true);
		try
		{
			this.availableInitialPositions = csvReader.readDoubleDataTable();
		}
		catch(IOException e)
		{
			csvReader.closeFile();
			throw new IOException(e);
		}
		csvReader.closeFile();
	}

	public void loadSetup() throws IOException
	{
		TreeMap<String, String> metaDataMap = new TreeMap<String, String>();
		File file = new File(this.setupDirectory + "/setup.ini");
		if(!file.exists()) throw new FileNotFoundException("Setup file not readable. " + file.getAbsolutePath() + " does not exists.");
		FileLineReader flReader = new FileLineReader();
		
		flReader.openFile(file);
		flReader.setSeperatorChar('=');
		ArrayList<String> lineList;
		try
		{
			while(flReader.ready())
			{	
				lineList = flReader.readStringListLine();
				if(lineList.size() == 2) metaDataMap.put(lineList.get(0), lineList.get(1));
			};
		}
		catch(IOException e)
		{
			flReader.closeFile();
			throw new IOException(e);
		}
		flReader.closeFile();
		
		this.maxIterations = Integer.parseInt(metaDataMap.get("maxIterations"));
		this.dataSetRepitition = Integer.parseInt(metaDataMap.get("dataSetRepitition"));
		this.analyseClusterCount = Integer.parseInt(metaDataMap.get("clusterConfig"));
		this.noiseFraction = Double.parseDouble(metaDataMap.get("noiseFraction"));
		this.analyseClusterList = new int[analyseClusterCount];
		
		for(int i=0; i<this.analyseClusterCount; i++)
		{
			this.analyseClusterList[i] = Integer.parseInt(metaDataMap.get("cluster" + i));
		}
	}
	
	public void loadExperimentData()
	{
		try
		{
			this.loadMetaData();
			this.loadClusterData();
			this.loadInitials();
			
			System.out.println("dim = " + this.dim);
			System.out.println("availableClusterCount = " + this.availableClusterCount);
			System.out.println("dataCount = " + this.availableDataCount);
			System.out.println("noiseCount = " + this.availableNoiseCount);
			System.out.println("clusterSize = " + Arrays.toString(this.clusterSize));
						
			System.out.println("clusteredData (cluster count) = " + this.clusteredData.size());
			System.out.println("initialPositions (initial count) = " + this.availableInitialPositions.size());
			System.out.println("noiseData (noise data count) = " + this.noiseData.size());			
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	public void loadExperimentSetup()
	{
		try
		{
			this.loadSetup();
						
			System.out.println("dataSetRepitition = " + this.dataSetRepitition);
			System.out.println("noiseFraction = " + this.noiseFraction);
			System.out.println("analyseClusterCount = " + this.analyseClusterCount);			
			System.out.println("analyseClusterList = " + Arrays.toString(this.analyseClusterList));
			
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void generateExperiments()
	{
		RandomDataGenerator random = new RandomDataGenerator();
		DecimalFormat format = new DecimalFormat();
		format.setMinimumIntegerDigits(3);
		format.setGroupingUsed(false);
		
		// start with large data sets, going down to small data sets
		for(int i=0; i<this.analyseClusterCount; i++)
		{
			int clusterCount = this.analyseClusterList[i];
			
			// repeat any experiment 5 times, using different initial values each time
			for(int k=0; k<this.dataSetRepitition; k++)
			{
				// subdirectory for the results of this experiment batch
				String experimentSubdirectory = this.experimentDirectory + "C"+format.format(clusterCount) + "/R"+format.format(k) + "/";
												
				// build data set for all algorithms
				int[] clusterPermutation = random.nextPermutation(this.availableClusterCount, clusterCount);
				IndexedDataSet<double[]> dataSet = new IndexedDataSet<double[]>();
				ArrayList<double[]> data = new ArrayList<double[]>(1100*clusterCount);

				for(int cluster=0; cluster<clusterCount; cluster++)
				{
					for(double[] x : this.clusteredData.get(clusterPermutation[cluster]))
					{
						data.add(x);
					}
				}
				
				int noiseCount = 0;
				int[] noisePermutation=null;
				
				if(this.noiseData.size() > 0) 
				{
					noiseCount = (int)(data.size() * this.noiseFraction);
					noisePermutation = random.nextPermutation(this.noiseData.size(), noiseCount);
					
					for(int n=0; n<noiseCount; n++)
					{
						data.add(this.noiseData.get(noisePermutation[n]));
					}
				}

				Collections.shuffle(data);				
				for(double[] x:data) dataSet.add(new IndexedDataObject<double[]>(x));
				dataSet.seal();

				// select initial positions
				ArrayList<double[]> initialPositions = new ArrayList<double[]>();
				int[] initialPermutation = random.nextPermutation(this.availableInitialPositions.size(), clusterCount);
				for(int n=0; n<clusterCount; n++)
				{
					initialPositions.add(this.availableInitialPositions.get(initialPermutation[n]));
				}

				// store data setup information
				this.saveExperimentSetup(clusterPermutation, noisePermutation, initialPermutation, experimentSubdirectory);
				
				// generate experiment objects
				this.addExperiments(dataSet, initialPositions, experimentSubdirectory);
			}
		}
	}
	
	private void addExperiments(IndexedDataSet<double[]> dataSet, ArrayList<double[]> initPos, String resultDir)
	{
		DAEuclideanVectorSpace vs = new DAEuclideanVectorSpace(this.dim);
		DAEuclideanMetric metric = new DAEuclideanMetric();
		
		// HCM
		{
			HardCMeansClusteringAlgorithm<double[]> algo = new HardCMeansClusteringAlgorithm<double[]>(dataSet, vs, metric);
			algo.setEpsilon(0.001d);
			this.experiments.add(new Experiment(algo, initPos, this.maxIterations, resultDir, "HCM", this.experiments.size()));
		}
		
		// FCM 2
		{
			FuzzyCMeansClusteringAlgorithm<double[]> algo = new FuzzyCMeansClusteringAlgorithm<double[]>(dataSet, vs, metric);
			algo.setFuzzifier(2.0d);
			algo.setEpsilon(0.001d);
			this.experiments.add(new Experiment(algo, initPos, this.maxIterations, resultDir, "FCM2", this.experiments.size()));
		}
		
		// NFCM 2
		{
			FuzzyCMeansNoiseClusteringAlgorithm<double[]> algo = new FuzzyCMeansNoiseClusteringAlgorithm<double[]>(dataSet, vs, metric);
			algo.setFuzzifier(2.0d);
			algo.setNoiseDistance(0.1d*Math.sqrt(this.dim));
			algo.setDegradingNoiseDistance(20*algo.getNoiseDistance());
			algo.setNoiseDegrationFactor(0.2d);
			algo.setEpsilon(0.001d);
			this.experiments.add(new Experiment(algo, initPos, this.maxIterations, resultDir, "NFCM2", this.experiments.size()));
		}
		
		// FCM 1+1/m
		{
			FuzzyCMeansClusteringAlgorithm<double[]> algo = new FuzzyCMeansClusteringAlgorithm<double[]>(dataSet, vs, metric);
			algo.setFuzzifier(1.0d+1.0d/this.dim);
			algo.setEpsilon(0.001d);
			this.experiments.add(new Experiment(algo, initPos, this.maxIterations, resultDir, "FCMdim", this.experiments.size()));
		}
		
		// NFCM 1+1/m
		{
			FuzzyCMeansNoiseClusteringAlgorithm<double[]> algo = new FuzzyCMeansNoiseClusteringAlgorithm<double[]>(dataSet, vs, metric);
			algo.setFuzzifier(1.0d+1.0d/this.dim);
			algo.setNoiseDistance(0.1d*Math.sqrt(this.dim));
			algo.setDegradingNoiseDistance(20*algo.getNoiseDistance());
			algo.setNoiseDegrationFactor(0.2d);
			algo.setEpsilon(0.001d);
			this.experiments.add(new Experiment(algo, initPos, this.maxIterations, resultDir, "NFCMdim", this.experiments.size()));
		}
		
		// PFCM
		{
			PolynomFCMClusteringAlgorithm<double[]> algo = new PolynomFCMClusteringAlgorithm<double[]>(dataSet, vs, metric);
			algo.setBeta(0.5d);
			algo.setEpsilon(0.001d);
			this.experiments.add(new Experiment(algo, initPos, this.maxIterations, resultDir, "PFCM", this.experiments.size()));
		}
		
		// PNFCM
		{
			PolynomFCMNoiseClusteringAlgorithm<double[]> algo = new PolynomFCMNoiseClusteringAlgorithm<double[]>(dataSet, vs, metric);
			algo.setBeta(0.5d);
			algo.setNoiseDistance(0.1d*Math.sqrt(this.dim));
			algo.setDegradingNoiseDistance(20*algo.getNoiseDistance());
			algo.setNoiseDegrationFactor(0.2d);
			algo.setEpsilon(0.001d);
			this.experiments.add(new Experiment(algo, initPos, this.maxIterations, resultDir, "PNFCM", this.experiments.size()));
		}
		
		// RCFCM
		{
			RewardingCrispFCMClusteringAlgorithm<double[]> algo = new RewardingCrispFCMClusteringAlgorithm<double[]>(dataSet, vs, metric);
			algo.setFuzzifier(2.0d);
			algo.setDistanceMultiplierConstant(1.0d-1.0d/this.dim);
			algo.setEpsilon(0.001d);
			this.experiments.add(new Experiment(algo, initPos, this.maxIterations, resultDir, "RCFCM", this.experiments.size()));
		}
		
		// RCNFCM
		{
			RewardingCrispFCMNoiseClusteringAlgorithm<double[]> algo = new RewardingCrispFCMNoiseClusteringAlgorithm<double[]>(dataSet, vs, metric);
			algo.setFuzzifier(2.0d);
			algo.setDistanceMultiplierConstant(1.0d-1.0d/this.dim);
			algo.setNoiseDistance(0.1d*Math.sqrt(this.dim));
			algo.setDegradingNoiseDistance(20*algo.getNoiseDistance());
			algo.setNoiseDegrationFactor(0.2d);
			algo.setEpsilon(0.001d);
			this.experiments.add(new Experiment(algo, initPos, this.maxIterations, resultDir, "RCNFCM", this.experiments.size()));
		}
		
		// EM-GMM
		{
			ExpectationMaximizationSGMMClusteringAlgorithm algo = new ExpectationMaximizationSGMMClusteringAlgorithm(dataSet, vs, metric);
			algo.setEpsilon(0.001d);
			this.experiments.add(new Experiment(algo, initPos, this.maxIterations, resultDir, "EMGMM", this.experiments.size()));
//			this.EMGMM_Experiments.add(new Experiment(algo, initPos, this.maxIterations, resultDir));
		}
	}
	
	private void saveExperimentSetup(int[] clusters, int[] noiseIds, int[] initialPermutation, String experimentDirectory)
	{
		FileWriter writer;
		File dir = new File(experimentDirectory);
		if(!dir.exists()) dir.mkdirs();
		
		try
		{
			writer = new FileWriter(experimentDirectory + "/dataSetup.ini");
			writer.write("clusterCount="+clusters.length+"\n");
			for(int i=0; i<clusters.length; i++)
			{
				writer.write("clusterID_"+i+"="+clusters[i]+"\n");
			}
			for(int i=0; i<clusters.length; i++)
			{
				writer.write("initialID_"+i+"="+initialPermutation[i]+"\n");
			}
			if(noiseIds != null)
			{
				writer.write("noiseCount="+noiseIds.length+"\n");
				for(int i=0; i<clusters.length; i++)
				{
					writer.write("noiseID"+i+"="+noiseIds[i]+"\n");
				}
			}
			writer.flush();
			writer.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void performExperiments()
	{		
		Parallel.ForFJ(this.experiments, new Parallel.Operation<Experiment>(){
			public void perform(Experiment exp)
			{
				exp.initialize();
				exp.applyAlgorithm();
//				exp.storeResult();
			}
		});
	}
	
	public void storeResults()
	{
		for(Experiment exp:this.experiments) exp.storeResult();
	}

	/**
	 * @return the analyseClusterList
	 */
	public int[] getAnalyseClusterList()
	{
		return this.analyseClusterList;
	}

	/**
	 * @param analyseClusterList the analyseClusterList to set
	 */
	public void setAnalyseClusterList(int[] analyseClusterList)
	{
		this.analyseClusterList = analyseClusterList;
	}

	/**
	 * @return the dim
	 */
	public int getDim()
	{
		return this.dim;
	}

	

	/**
	 * @return the availableClusterCount
	 */
	public int getAvailableClusterCount()
	{
		return this.availableClusterCount;
	}

	/**
	 * @return the analyseClusterCount
	 */
	public int getAnalyseClusterCount()
	{
		return this.analyseClusterCount;
	}

	/**
	 * @return the dataCount
	 */
	public int getDataCount()
	{
		return this.availableDataCount;
	}

	/**
	 * @return the noiseCount
	 */
	public int getNoiseCount()
	{
		return this.availableNoiseCount;
	}

	/**
	 * @return the clusterSize
	 */
	public int[] getClusterSize()
	{
		return this.clusterSize;
	}
	
	
	
}
