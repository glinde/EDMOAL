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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import data.objects.doubleArray.DAEuclideanMetric;
import datamining.clustering.protoype.AbstractPrototypeClusteringAlgorithm;
import datamining.clustering.protoype.Centroid;
import datamining.clustering.protoype.Prototype;
import datamining.clustering.protoype.SphericalNormalDistributionPrototype;
import datamining.resultProviders.CrispClusteringProvider;
import datamining.resultProviders.FuzzyClusteringProvider;
import datamining.validation.BezdecSeperationIndex;
import datamining.validation.ClusterF1Measure;
import datamining.validation.ClusteringInformation;
import datamining.validation.DaviesBouldinIndex;
import datamining.validation.NonFuzzynessIndex;
import datamining.validation.XieBeniIndex;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class Experiment//<S extends Prototype<double[]>>
{
	protected AbstractPrototypeClusteringAlgorithm<double[], ? extends Centroid<double[]>> clusteringAlgo;
		
	protected ArrayList<double[]> initialPositions;
	
	protected ArrayList<double[]> centroidPositions;
	
	protected ArrayList<double[]> variances;
	
	protected int maxIterations;
	
	protected String resultDir;
	
	protected String algoName;
	
	protected int id;
	
	protected ClusteringInformation<double[]> info;
	
	protected int[] correctClustering;
	
	
	protected double[] indexValues;
	
	/**
	 * @param clusteringAlgo
	 * @param initialPositions
	 * @param maxIterations
	 * @param resultDir
	 */
	public Experiment(AbstractPrototypeClusteringAlgorithm<double[], ? extends Centroid<double[]>> clusteringAlgo, ArrayList<double[]> initialPositions, int maxIterations, String resultDir, String algoName, int id, int[] correctClustering)
	{
		this.clusteringAlgo = clusteringAlgo;
		this.initialPositions = initialPositions;
		this.maxIterations = maxIterations;
		this.resultDir = resultDir;
		this.algoName = algoName;
		this.id = id;
		this.info = null;
		this.centroidPositions = new ArrayList<double[]>();
		this.variances = new ArrayList<double[]>();
		this.correctClustering = correctClustering;

		File dir = new File(this.resultDir);		
		if(!dir.isDirectory()) throw new IllegalArgumentException("The path " + this.resultDir + " is not a directory.");
		if(!dir.exists()) dir.mkdirs();
		
		this.indexValues = new double[5]; 
	}
	
	public void applyAlgorithm()
	{
		// initialize
		this.clusteringAlgo.initializeWithPositions(this.initialPositions);
		
		// apply algorithm
		this.clusteringAlgo.apply(this.maxIterations);
		
		// store prototype positions
		for(Centroid<double[]> proto:this.clusteringAlgo.getPrototypes())
		{
			this.centroidPositions.add(proto.getPosition().clone());
			if(proto instanceof SphericalNormalDistributionPrototype)
			{
				this.variances.add(new double[]{((SphericalNormalDistributionPrototype)proto).getVariance()});
			}
		}
	}
	
	public void analyseResult()
	{
		this.info = new ClusteringInformation<double[]>(this.clusteringAlgo.getClusterCount());
		
		this.info.setDataSet(this.clusteringAlgo.getDataSet());
		this.info.setPrototypes(this.clusteringAlgo.getPrototypes());
		this.info.setMetric(new DAEuclideanMetric());
		this.info.setTrueClusteringResult(this.correctClustering);
		
		if(this.clusteringAlgo instanceof FuzzyClusteringProvider)
		{
			this.info.setFuzzyClusteringProvider((FuzzyClusteringProvider<double[]>)this.clusteringAlgo);
			this.info.calculateClusterDiameters_Fuzzy_Prototype();
			this.info.calculateClusterDistances_Prototype();

			this.indexValues[0] = (new ClusterF1Measure<double[]>(this.info, false)).index();
			this.indexValues[1] = (new BezdecSeperationIndex<double[]>(this.info)).index();
			this.indexValues[2] = (new DaviesBouldinIndex<double[]>(this.info)).index();
			this.indexValues[3] = (new XieBeniIndex<double[]>(this.info, 1.5d, false)).index();
			this.indexValues[4] = (new NonFuzzynessIndex<double[]>(this.info)).index();
		}
		else if(this.clusteringAlgo instanceof CrispClusteringProvider)
		{
			this.info.setCrispClusteringResult(((CrispClusteringProvider<double[]>)this.clusteringAlgo).getAllCrispClusterAssignments());
			this.info.calculateClusterDiameters_Crisp_Prototype();
			this.info.calculateClusterDistances_Prototype();

			this.indexValues[0] = (new ClusterF1Measure<double[]>(this.info, true)).index();
			this.indexValues[1] = (new BezdecSeperationIndex<double[]>(this.info)).index();
			this.indexValues[2] = (new DaviesBouldinIndex<double[]>(this.info)).index();
			this.indexValues[3] = (new XieBeniIndex<double[]>(this.info, 1.5d, true)).index();
			this.indexValues[4] = -1.0d;
		}
	}
	
	public void clean()
	{
		
		this.clusteringAlgo = null;
		this.info = null;
	}
	
	public void storeResult()
	{		
		CSVFileWriter csvWriter = new CSVFileWriter();
		csvWriter.setAddFirstAttributeAsID(true);
		csvWriter.setFirstLineAsAtributeNames(true);
		csvWriter.setDefaultAttributeName("");
				
		try
		{
			csvWriter.openFile(new File(this.resultDir + "/"+this.algoName+"_protoPos.csv"));
			csvWriter.writeDoubleDataTable(this.centroidPositions, null);
		}
		catch(IOException e)
		{
			csvWriter.closeFile();
		}
		csvWriter.closeFile();
		
		if(this.variances.size() > 0)
		{
			try
			{
				csvWriter.openFile(new File(this.resultDir + "/"+this.algoName+"_clusterVariance.csv"));
				csvWriter.writeDoubleDataTable(this.variances, null);
			}
			catch(IOException e)
			{
				csvWriter.closeFile();
			}
			csvWriter.closeFile();
		}
	}
}
