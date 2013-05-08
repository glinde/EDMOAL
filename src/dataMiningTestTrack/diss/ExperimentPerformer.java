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
import io.FileLineReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;

import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.random.RandomDataGenerator;

import data.objects.doubleArray.DAEuclideanMetric;
import data.objects.doubleArray.DAEuclideanVectorSpace;
import data.objects.doubleArray.DAMaximumMetric;
import data.set.IndexedDataObject;
import data.set.IndexedDataSet;
import datamining.clustering.protoype.DummyCrispPrototypeClusteringAlgorithm;
import datamining.clustering.protoype.altopt.ExpectationMaximizationSGMMClusteringAlgorithm;
import datamining.clustering.protoype.altopt.FuzzyCMeansClusteringAlgorithm;
import datamining.clustering.protoype.altopt.FuzzyCMeansNoiseClusteringAlgorithm;
import datamining.clustering.protoype.altopt.HardCMeansClusteringAlgorithm;
import datamining.clustering.protoype.altopt.PolynomFCMClusteringAlgorithm;
import datamining.clustering.protoype.altopt.PolynomFCMNoiseClusteringAlgorithm;
import datamining.clustering.protoype.altopt.RewardingCrispFCMClusteringAlgorithm;
import datamining.clustering.protoype.altopt.RewardingCrispFCMNoiseClusteringAlgorithm;
import etc.Parallel;
import etc.SimpleStatistics;
import etc.StringService;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class ExperimentPerformer
{
	private String dataDirectory;
		
	private String experimentDirectory;
		
	
	private int dim;
	
	private int availableClusterCount;
	
	private int availableDataCount;
	
	private int availableNoiseCount;
	
	private int[] clusterSize;
	
	
	private double noiseDistance;
	
	
	private int maxIterations;
	
	private int dataSetRepitition;
		
	private int[] analyseClusterList;
	
	private double noiseFraction;
	
	
	private ArrayList<ArrayList<double[]>> clusteredData;
	
	private ArrayList<double[]> availableInitialPositions; 
	
	private ArrayList<double[]> noiseData;
	
	private ArrayList<Experiment> experiments;

	private ArrayList<ArrayList<Experiment>> perfectProtoExperiments;
	
	private ArrayList<ArrayList<ArrayList<Experiment>>> experimentsByDataSet;
	
	private int experimentID;
	
	private double conversionEpsilon;
	
	
	public ExperimentPerformer(String dataDirectory, String experimentDirectory, int maxIterations, int dataSetRepitition, int[] analyseClusterList, double noiseFraction)
	{
		super();
		
		this.dataDirectory = dataDirectory;
		this.experimentDirectory = experimentDirectory;


//		System.out.println("Load data from " + this.dataDirectory);
		File dir = new File(this.dataDirectory);		
		if(!dir.exists() || !dir.isDirectory()) throw new IllegalArgumentException("The path " + this.dataDirectory + " does not exist or is not a directory.");
		
		dir = new File(this.experimentDirectory);		
//		if(!dir.isDirectory()) throw new IllegalArgumentException("The path " + this.experimentDirectory + " is not a directory.");
		if(!dir.exists()) dir.mkdirs();
		
		// filled by meta file
		this.dim = 0;
		this.availableClusterCount = 0;
		this.availableDataCount = 0;
		this.availableNoiseCount = 0;
		this.clusterSize = null;
		
		// filled by cluster statistics
		this.noiseDistance = 0.0d;
		
		// filled by experiment setup file
		this.maxIterations = maxIterations;
		this.dataSetRepitition = dataSetRepitition;
		this.analyseClusterList = analyseClusterList;
		this.noiseFraction = noiseFraction;
		
		// filled by cluster data files
		this.clusteredData = new ArrayList<ArrayList<double[]>>();
		this.availableInitialPositions = new ArrayList<double[]>();
		this.noiseData = new ArrayList<double[]>();
		
		// filled by generate experiments
		this.experiments = new ArrayList<Experiment>();
		this.perfectProtoExperiments = new ArrayList<ArrayList<Experiment>>();
		this.experimentsByDataSet = new ArrayList<ArrayList<ArrayList<Experiment>>>(); // clustercount, repititions, experiments
		
		this.experimentID = 0;
		this.conversionEpsilon = 0.01d;
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
				if(lineList.size() == 2) metaDataMap.put(lineList.get(0).trim(), lineList.get(1));
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
	
	private void loadClusterProperties() throws IOException
	{
		TreeMap<String, String> iniMap = new TreeMap<String, String>();
		File file = new File(this.dataDirectory + "/statistics.ini");
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
				if(lineList.size() == 2) iniMap.put(lineList.get(0).trim(), lineList.get(1).trim());
			};
		}
		catch(IOException e)
		{
			flReader.closeFile();
			throw new IOException(e);
		}
		flReader.closeFile();
		
		double[] statistics = StringService.parseDoubleArray(iniMap.get("RadiusMax"));
		
		this.noiseDistance = 1.5d*statistics[1];
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
	
	public void loadExperimentData()
	{
		try
		{
			this.loadMetaData();
			this.loadClusterData();
			this.loadClusterProperties();
			this.loadInitials();
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
		for(int i=0; i<this.analyseClusterList.length; i++)
		{
			int clusterCount = this.analyseClusterList[i];
			this.experimentsByDataSet.add(new ArrayList<ArrayList<Experiment>>());
			int[] clusterPermutation = random.nextPermutation(this.availableClusterCount, clusterCount);
			// build data set for all algorithms
			IndexedDataSet<double[]> dataSet = new IndexedDataSet<double[]>();
			ArrayList<double[]> data = new ArrayList<double[]>(1100*clusterCount);

			// add data from clusters
			for(int cluster=0; cluster<clusterCount; cluster++)
			{
				for(double[] x : this.clusteredData.get(clusterPermutation[cluster]))
				{
					data.add(x);
				}
			}
			
			int noiseCount = 0;
			int[] noisePermutation=null;
			
			// add noise if available
			if(this.noiseData.size() > 0) 
			{
				noiseCount = (int)(data.size() * this.noiseFraction);
				noisePermutation = random.nextPermutation(this.noiseData.size(), noiseCount);
				
				for(int n=0; n<noiseCount; n++)
				{
					data.add(this.noiseData.get(noisePermutation[n]));
				}
			}			
			// remember correct clustering
			int[] correctClustering = new int[data.size()];
			int[] permuttedCorrectClustering = new int[data.size()];
			int dataObjIndex = 0;
			for(int cluster=0; cluster<clusterCount; cluster++)
			{
				for(int n=0; n<this.clusteredData.get(clusterPermutation[cluster]).size(); n++)
				{
					correctClustering[dataObjIndex] = cluster;
					dataObjIndex++;
				}
			}
			for(int n=0; n<noiseCount; n++)
			{
				correctClustering[dataObjIndex] = -1;
				dataObjIndex++;
			}
			
			// shuffle the data
			int[] dataPermutation = random.nextPermutation(data.size(), data.size());
			for(int n=0; n<data.size(); n++)
			{
				dataSet.add(new IndexedDataObject<double[]>(data.get(dataPermutation[n])));
				permuttedCorrectClustering[n] = correctClustering[dataPermutation[n]];
			}
			dataSet.seal();

			// calculate relative variance of data set
			double maxRV = this.calculateExperimentRV(dataSet, 10);
			
			// repeat any experiment several times, using different initial values each time
			for(int k=0; k<this.dataSetRepitition; k++)
			{
				// subdirectory for the results of this experiment batch
				String experimentSubdirectory = this.experimentDirectory + "C"+format.format(clusterCount) + "/R"+format.format(k) + "/";
					
				// select initial positions
				ArrayList<double[]> initialPositions = new ArrayList<double[]>();
				int[] initialPermutation = random.nextPermutation(this.availableInitialPositions.size(), clusterCount);
				for(int n=0; n<clusterCount; n++)
				{
					initialPositions.add(this.availableInitialPositions.get(initialPermutation[n]));
				}
				
				// store data setup information
				this.saveExperimentSetup(clusterPermutation, noisePermutation, initialPermutation, maxRV, experimentSubdirectory);
				
				// generate experiment objects
				ArrayList<Experiment> group = this.createExperiments(dataSet, permuttedCorrectClustering, initialPositions, experimentSubdirectory);

				// generate experiments for perfect clusterings results. 
				
				this.experiments.addAll(group);
				this.experimentsByDataSet.get(this.experimentsByDataSet.size()-1).add(group);
			}
			
			// generate experiments with perfect prototype locations
			ArrayList<double[]> clusterCenters = new ArrayList<double[]>();
			for(int cluster=0; cluster<clusterCount; cluster++)
			{
				clusterCenters.add(SimpleStatistics.mean(this.clusteredData.get(clusterPermutation[cluster])));
			}
			
			ArrayList<Experiment> group = this.createExperiments(dataSet, permuttedCorrectClustering, clusterCenters, this.experimentDirectory + "C"+format.format(clusterCount) + "/perfectProto/");
			
			// add an other algorithm that already contains the correct result. For testing the internal indices.
			DummyCrispPrototypeClusteringAlgorithm<double[]> dummy =  new DummyCrispPrototypeClusteringAlgorithm<double[]>(dataSet, permuttedCorrectClustering, new DAEuclideanVectorSpace(this.dim), new DAEuclideanMetric());
			group.add(new Experiment(dummy, clusterCenters, this.maxIterations, this.experimentDirectory + "C"+format.format(clusterCount) + "/perfectProto/", "Dummy", this.experimentID++, permuttedCorrectClustering));
			
			this.perfectProtoExperiments.add(group);
			
			
		}
	}
	
	private double calculateExperimentRV(IndexedDataSet<double[]> dataSet, int repetitions)
	{
		ArrayList<RVExperiment> rvExperimentList = new ArrayList<RVExperiment>();
		UniformIntegerDistribution randomValue = new UniformIntegerDistribution(0, this.availableInitialPositions.size()-1);
		double maxRV = 0.0d;
		
		for(int i=0; i<repetitions; i++)
		{
			rvExperimentList.add(new RVExperiment(this.dim, dataSet, this.availableInitialPositions.get(randomValue.sample()), 100));
		}
		
//		for(RVExperiment rvExp:rvExperimentList)
//		{
//			rvExp.applyAlgorithm();
//		}
		
		Parallel.ForFJ(rvExperimentList, new Parallel.Operation<RVExperiment>(){
			public void perform(RVExperiment exp)
			{
				exp.applyAlgorithm();
			}
		});

		for(int i=0; i<repetitions; i++)
		{
			if(rvExperimentList.get(i).getMaxRV() > maxRV) maxRV = rvExperimentList.get(i).getMaxRV();
		}
		
		return maxRV;
	}
	
	private ArrayList<Experiment> createExperiments(IndexedDataSet<double[]> dataSet, int[] correctClustering, ArrayList<double[]> initPos, String resultDir)
	{
		DAEuclideanVectorSpace vs = new DAEuclideanVectorSpace(this.dim);
		DAEuclideanMetric metric = new DAEuclideanMetric();
				
		ArrayList<Experiment> group = new ArrayList<Experiment>(10);
				
		// HCM
		{
			HardCMeansClusteringAlgorithm<double[]> algo = new HardCMeansClusteringAlgorithm<double[]>(dataSet, vs, metric);
			algo.setMinIterations(10);
			algo.setEpsilon(this.conversionEpsilon);
			algo.setConvergenceMetric(new DAMaximumMetric());
			Experiment exp = new Experiment(algo, initPos, this.maxIterations, resultDir, "HCM", this.experimentID++, correctClustering);
			group.add(exp);
		}
		
		// FCM 2
		{
			FuzzyCMeansClusteringAlgorithm<double[]> algo = new FuzzyCMeansClusteringAlgorithm<double[]>(dataSet, vs, metric);
			algo.setFuzzifier(2.0d);
			algo.setEpsilon(this.conversionEpsilon);
			algo.setConvergenceMetric(new DAMaximumMetric());
			Experiment exp = new Experiment(algo, initPos, this.maxIterations, resultDir, "FCM2", this.experimentID++, correctClustering);
			group.add(exp);
		}
		
		// NFCM 2
		{
			FuzzyCMeansNoiseClusteringAlgorithm<double[]> algo = new FuzzyCMeansNoiseClusteringAlgorithm<double[]>(dataSet, vs, metric);
			algo.setMinIterations(10);
			algo.setFuzzifier(2.0d);
			algo.setNoiseDistance(this.noiseDistance);
			algo.setDegradingNoiseDistance(20*algo.getNoiseDistance());
			algo.setNoiseDegrationFactor(0.3d);
			algo.setEpsilon(this.conversionEpsilon);
			algo.setConvergenceMetric(new DAMaximumMetric());
			Experiment exp = new Experiment(algo, initPos, this.maxIterations, resultDir, "NFCM2", this.experimentID++, correctClustering);
			group.add(exp);
		}
		
		// FCM 1+1/m
		{
			FuzzyCMeansClusteringAlgorithm<double[]> algo = new FuzzyCMeansClusteringAlgorithm<double[]>(dataSet, vs, metric);
			algo.setMinIterations(10);
			algo.setFuzzifier(1.0d+1.0d/this.dim);
			algo.setEpsilon(this.conversionEpsilon);
			algo.setConvergenceMetric(new DAMaximumMetric());
			Experiment exp = new Experiment(algo, initPos, this.maxIterations, resultDir, "FCMdim", this.experimentID++, correctClustering);
			group.add(exp);
		}
		
		// NFCM 1+1/m
		{
			FuzzyCMeansNoiseClusteringAlgorithm<double[]> algo = new FuzzyCMeansNoiseClusteringAlgorithm<double[]>(dataSet, vs, metric);
			algo.setMinIterations(10);
			algo.setFuzzifier(1.0d+1.0d/this.dim);
			algo.setNoiseDistance(this.noiseDistance);
			algo.setDegradingNoiseDistance(20*algo.getNoiseDistance());
			algo.setNoiseDegrationFactor(0.3d);
			algo.setEpsilon(this.conversionEpsilon);
			algo.setConvergenceMetric(new DAMaximumMetric());
			Experiment exp = new Experiment(algo, initPos, this.maxIterations, resultDir, "NFCMdim", this.experimentID++, correctClustering);
			group.add(exp);
		}
		
		// PFCM
		{
			PolynomFCMClusteringAlgorithm<double[]> algo = new PolynomFCMClusteringAlgorithm<double[]>(dataSet, vs, metric);
			algo.setMinIterations(10);
			algo.setBeta(0.5d);
			algo.setEpsilon(this.conversionEpsilon);
			algo.setConvergenceMetric(new DAMaximumMetric());
			Experiment exp = new Experiment(algo, initPos, this.maxIterations, resultDir, "PFCM", this.experimentID++, correctClustering);
			group.add(exp);
		}
		
		// PNFCM
		{
			PolynomFCMNoiseClusteringAlgorithm<double[]> algo = new PolynomFCMNoiseClusteringAlgorithm<double[]>(dataSet, vs, metric);
			algo.setMinIterations(10);
			algo.setBeta(0.5d);
			algo.setNoiseDistance(this.noiseDistance);
			algo.setDegradingNoiseDistance(20*algo.getNoiseDistance());
			algo.setNoiseDegrationFactor(0.3d);
			algo.setEpsilon(this.conversionEpsilon);
			algo.setConvergenceMetric(new DAMaximumMetric());
			Experiment exp = new Experiment(algo, initPos, this.maxIterations, resultDir, "PNFCM", this.experimentID++, correctClustering);
			group.add(exp);
		}
		
		// RCFCM
		{
			RewardingCrispFCMClusteringAlgorithm<double[]> algo = new RewardingCrispFCMClusteringAlgorithm<double[]>(dataSet, vs, metric);
			algo.setMinIterations(10);
			algo.setFuzzifier(2.0d);
			algo.setDistanceMultiplierConstant(1.0d-1.0d/this.dim);
			algo.setEpsilon(this.conversionEpsilon);
			algo.setConvergenceMetric(new DAMaximumMetric());
			Experiment exp = new Experiment(algo, initPos, this.maxIterations, resultDir, "RCFCM", this.experimentID++, correctClustering);
			group.add(exp);
		}
		
		// RCNFCM
		{
			RewardingCrispFCMNoiseClusteringAlgorithm<double[]> algo = new RewardingCrispFCMNoiseClusteringAlgorithm<double[]>(dataSet, vs, metric);
			algo.setMinIterations(10);
			algo.setFuzzifier(2.0d);
			algo.setDistanceMultiplierConstant(1.0d-1.0d/this.dim);
			algo.setNoiseDistance(this.noiseDistance);
			algo.setDegradingNoiseDistance(20*algo.getNoiseDistance());
			algo.setNoiseDegrationFactor(0.3d);
			algo.setEpsilon(this.conversionEpsilon);
			algo.setConvergenceMetric(new DAMaximumMetric());
			Experiment exp = new Experiment(algo, initPos, this.maxIterations, resultDir, "RCNFCM", this.experimentID++, correctClustering);
			group.add(exp);
		}
		
		// EM-GMM
		{
			ExpectationMaximizationSGMMClusteringAlgorithm algo = new ExpectationMaximizationSGMMClusteringAlgorithm(dataSet, vs, metric);
			algo.setMinIterations(10);
			algo.setVarianceBounded(true);
			algo.setVarianceLowerBound(0.001d);
			algo.setVarianceUpperBound(100.0d);
			algo.setEpsilon(this.conversionEpsilon);
			algo.setConvergenceMetric(new DAMaximumMetric());
			Experiment exp = new Experiment(algo, initPos, this.maxIterations, resultDir, "EMGMM", this.experimentID++, correctClustering);
			group.add(exp);
		}
		
		return group;
	}
	
	private void saveExperimentSetup(int[] clusters, int[] noiseIds, int[] initialPermutation, double maxRV, String experimentDirectory)
	{
		FileWriter writer;
		File dir = new File(experimentDirectory);
		if(!dir.exists()) dir.mkdirs();
		
		try
		{
			writer = new FileWriter(experimentDirectory + "/dataSetup.ini");
			writer.write("maxRelativeVariance="+maxRV+"\n");
			writer.write("noiseDisance="+this.noiseDistance+"\n");
			writer.write("clusterCount="+clusters.length+"\n");
			writer.write("clusterIDs="+Arrays.toString(clusters)+"\n");
			writer.write("initialIDs="+Arrays.toString(initialPermutation)+"\n");
			if(noiseIds != null)
			{
				writer.write("noiseCount="+noiseIds.length+"\n");
				writer.write("noiseIDs="+Arrays.toString(noiseIds)+"\n");
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
//		for(Experiment exp : this.experiments)
//		{
//			exp.initializeAlgorithm();
//			exp.applyAlgorithm();
//			exp.readAlgorithmDetails();
//			exp.analyseResult();
//			exp.clean();
//		}
		
		Parallel.ForFJ(this.experiments, new Parallel.Operation<Experiment>()
		{
			public void perform(Experiment exp)
			{
				exp.initializeAlgorithm();
				exp.applyAlgorithm();
				exp.readAlgorithmDetails();
				exp.analyseResult();
				exp.clean();
			}
		});
		
		ArrayList<Experiment> perfectExperiments = new ArrayList<Experiment>();
		for(ArrayList<Experiment> list : this.perfectProtoExperiments) perfectExperiments.addAll(list);

//		for(Experiment exp : perfectExperiments)
//		{
//			exp.initializeAlgorithm();
//			exp.inplaceOptimizeAlgorithm();
//			exp.readAlgorithmDetails();
//			exp.analyseResult();
//			exp.clean();
//		}
		
		Parallel.ForFJ(perfectExperiments, new Parallel.Operation<Experiment>()
		{
			public void perform(Experiment exp)
			{
				exp.initializeAlgorithm();
				exp.inplaceOptimizeAlgorithm();
				exp.readAlgorithmDetails();
				exp.analyseResult();
				exp.clean();
			}
		});
	}
	
	public void storeResults()
	{
		for(Experiment exp:this.experiments) exp.storeResult();
		for(ArrayList<Experiment> list : this.perfectProtoExperiments) for(Experiment exp:list) exp.storeResult();
				
		for(int i=0; i<this.analyseClusterList.length;i++)
		{
			for(int r=0; r<this.dataSetRepitition; r++)
			{
				File dir = new File(this.experimentsByDataSet.get(i).get(r).get(0).resultDir);
				if(!dir.exists()) dir.mkdirs();
				
				// safe scores
				try
				{
					FileWriter writer = new FileWriter(this.experimentsByDataSet.get(i).get(r).get(0).resultDir + "/score.ini");

					writer.write("Interpretation="+Arrays.toString(this.experimentsByDataSet.get(i).get(r).get(0).indexNames)+"\n");
					for(int e=0; e<this.experimentsByDataSet.get(i).get(r).size(); e++)
					{
						writer.write(this.experimentsByDataSet.get(i).get(r).get(e).algoName+"="+Arrays.toString(this.experimentsByDataSet.get(i).get(r).get(e).indexValues)+"\n");
					}					
					writer.flush();
					writer.close();
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
				
				// safe convergence history
				try
				{
					FileWriter writer = new FileWriter(this.experimentsByDataSet.get(i).get(r).get(0).resultDir + "/convergence.ini");

					for(int e=0; e<this.experimentsByDataSet.get(i).get(r).size(); e++)
					{
						writer.write(this.experimentsByDataSet.get(i).get(r).get(e).algoName+"="+Arrays.toString(this.experimentsByDataSet.get(i).get(r).get(e).convergenceHistory)+"\n");
					}					
					writer.flush();
					writer.close();
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		
		for(ArrayList<Experiment> list : this.perfectProtoExperiments)
		{
			File dir = new File(list.get(0).resultDir);
			if(!dir.exists()) dir.mkdirs();
			
			try
			{
				FileWriter writer = new FileWriter(list.get(0).resultDir + "/score.ini");

				writer.write("Interpretation="+Arrays.toString(list.get(0).indexNames)+"\n");
				for(Experiment exp:list)
				{
					writer.write(exp.algoName+"_score="+Arrays.toString(exp.indexValues)+"\n");
				}
				writer.flush();
				writer.close();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
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
