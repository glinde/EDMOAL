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

import datamining.clustering.protoype.AbstractPrototypeClusteringAlgorithm;
import datamining.clustering.protoype.Centroid;
import datamining.clustering.protoype.Prototype;
import datamining.clustering.protoype.SphericalNormalDistributionPrototype;

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
	
	/**
	 * @param clusteringAlgo
	 * @param initialPositions
	 * @param maxIterations
	 * @param resultDir
	 */
	public Experiment(AbstractPrototypeClusteringAlgorithm<double[], ? extends Centroid<double[]>> clusteringAlgo, ArrayList<double[]> initialPositions, int maxIterations, String resultDir, String algoName, int id)
	{
		this.clusteringAlgo = clusteringAlgo;
		this.initialPositions = initialPositions;
		this.maxIterations = maxIterations;
		this.resultDir = resultDir;
		this.algoName = algoName;
		this.id = id;
		this.centroidPositions = new ArrayList<double[]>();
		this.variances = new ArrayList<double[]>();

		File dir = new File(this.resultDir);		
		if(!dir.isDirectory()) throw new IllegalArgumentException("The path " + this.resultDir + " is not a directory.");
		if(!dir.exists()) dir.mkdirs();
	}

	public void initialize()
	{
		this.clusteringAlgo.initializeWithPositions(this.initialPositions);
	}
	
	public void applyAlgorithm()
	{
//		System.out.println("### "+id+" ### " + this.algoName + "\t" + resultDir);
		this.clusteringAlgo.apply(this.maxIterations);		
		
		for(Centroid<double[]> proto:this.clusteringAlgo.getPrototypes())
		{
			this.centroidPositions.add(proto.getPosition());
			if(proto instanceof SphericalNormalDistributionPrototype)
			{
				this.variances.add(new double[]{((SphericalNormalDistributionPrototype)proto).getVariance()});
			}
		}
				
		this.clusteringAlgo = null;
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
