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

import generation.data.DADataGenerator;
import generation.data.HyperrectangleUniformGenerator;
import generation.data.SphericalNormalGenerator;
import generation.data.WeightedMixtureDataGenerator;

import java.util.ArrayList;

import data.objects.matrix.FeatureSpaceSampling2D;
import data.set.IndexedDataSet;
import datamining.gradient.functions.RelativeVarianceOfDistancesObjectiveFunction;
import etc.SimpleStatistics;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class DataGenerationAlgorithmTest extends TestVisualizer
{
	private int dim;
	private int number;
	private int clusters;
	private double noiseFraction;
	
	public DataGenerationAlgorithmTest(int dim, int number, int clusters, double noise)
	{
		this.dim = dim;
		this.number = number;
		this.clusters = clusters;
		this.noiseFraction = noise;
	}

	public void mixtureOfGaussiansTest()
	{
		IndexedDataSet<double[]> dataSet;
		int[] clustering;
		
		HyperrectangleUniformGenerator seedGen = new HyperrectangleUniformGenerator(this.dim);		
		WeightedMixtureDataGenerator<double[]> clusterGenerator = new WeightedMixtureDataGenerator<double[]>();
		
		// generate seeds
		ArrayList<double[]> seeds = seedGen.generateDataObjects(this.clusters);
		
		// generate noise
		clusterGenerator.addDataGenerator(new HyperrectangleUniformGenerator(this.dim), noiseFraction);
		
		// generate clusters
		for(double[] seed:seeds)
		{
			clusterGenerator.addDataGenerator(new SphericalNormalGenerator(seed, 0.05d), (1.0d-noiseFraction)/clusters);
		}
		
		// create data set
		
		dataSet = clusterGenerator.generateDataSet(number);
		clustering = clusterGenerator.assignementsOfLastGeneration();

		this.showCrispDataSetClustering(dataSet, this.clusters+1, clustering, null);
		
		RelativeVarianceOfDistancesObjectiveFunction relVarFunction = new RelativeVarianceOfDistancesObjectiveFunction(dataSet);
				
		// image overlay calculation
		
		double scale = 0.05d;
		double[] parameter = new double[2];
		double value;
		double min = Double.MAX_VALUE, max = 0.0d;
		double[] llC = new double[2];
		double[] urC = new double[2];
		
		ArrayList<double[]> boundingBox = SimpleStatistics.boundingBoxCornersIndexed(dataSet);
		llC[0] = (boundingBox.get(0)[0]-(boundingBox.get(1)[0] - boundingBox.get(0)[0])*0.5d);  
		llC[1] = (boundingBox.get(0)[1]-(boundingBox.get(1)[1] - boundingBox.get(0)[1])*0.5d);  
		urC[0] = (boundingBox.get(1)[0]+(boundingBox.get(1)[0] - boundingBox.get(0)[0])*0.5d);  
		urC[1] = (boundingBox.get(1)[1]+(boundingBox.get(1)[0] - boundingBox.get(0)[0])*0.5d);  
		
		FeatureSpaceSampling2D featureSampling = new FeatureSpaceSampling2D((int)((urC[0] - llC[0])/scale), (int)((urC[1] - llC[1])/scale));
		for(int x=0; x<featureSampling.sizeX(); x++)
		{
			for(int y=0; y<featureSampling.sizeY(); y++)
			{
				parameter[0] = ((double)x)*scale + llC[0] + 0.5d * scale;
				parameter[1] = ((double)y)*scale + llC[1] + 0.5d * scale;
				relVarFunction.setParameter(parameter);
				value = relVarFunction.functionValue();
				min = (value < min)? value : min;
				max = (value > max)? value : max;
				featureSampling.set(x, y, value);
			}	
		}
		
		for(int x=0; x<featureSampling.sizeX(); x++)
		{
			for(int y=0; y<featureSampling.sizeY(); y++)
			{
				value = featureSampling.get(x, y);
				featureSampling.set(x, y, (value - min)/(max - min));
			}	
		}
		
		featureSampling.setLowerLeftCorner(llC);
		featureSampling.setUpperRightCorner(urC);
		
		this.showDataSetImaged(dataSet, featureSampling, "Data Set Test", "DataSetTest");
	}
	

	public void variousDistributionTest()
	{
		DADataGenerator dataGen = new DADataGenerator();
		dataGen.addDistribution(new BetaDistribution(0.01d, 0.01d));
		dataGen.addDistribution(new UniformRealDistribution(0.0d, 1.0d));
		
		
	}
}
