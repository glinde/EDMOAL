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
package datamining.classification;

import java.util.ArrayList;

import data.objects.matrix.FeatureSpaceClusterSampling2D;
import datamining.resultProviders.FuzzyClassificationProvider;
import datamining.resultProviders.FuzzyNoiseClassificationProvider;



public class Featurespace2DClassificationMapper
{
	private int sizeX;

	private int sizeY;
	
	private double[] lowerLeftCorner;

	private double[] upperRightCorner;
		
	public Featurespace2DClassificationMapper(int sizeX, int sizeY, double[] lowerLeftCorner, double[] upperRightCorner)
	{
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.lowerLeftCorner = lowerLeftCorner;
		this.upperRightCorner = upperRightCorner;
	}


	public ArrayList<FeatureSpaceClusterSampling2D> readFromClassifier(FuzzyClassificationProvider<double[]> classifier)
	{
		double[] obj = new double[2];
		double[] fuzzyClassification;
		
		ArrayList<FeatureSpaceClusterSampling2D> featureSpaceList = new ArrayList<FeatureSpaceClusterSampling2D>();
		
		for(int i=0; i<classifier.getClusterCount(); i++)
			featureSpaceList.add(new FeatureSpaceClusterSampling2D(this.sizeX, this.sizeY, this.lowerLeftCorner, this.upperRightCorner, i));
		
		for(int x=0; x<this.sizeX; x++) for(int y=0; y<this.sizeY; y++)
		{			
			obj[0] = this.lowerLeftCorner[0]+(((double)x)+0.5d)*((this.upperRightCorner[0] - this.lowerLeftCorner[0])/(this.sizeX+1));
			obj[1] = this.lowerLeftCorner[1]+(((double)y)+0.5d)*((this.upperRightCorner[1] - this.lowerLeftCorner[1])/(this.sizeY+1));
		
			fuzzyClassification = classifier.classify(obj);
			
			for(int i=0; i<classifier.getClusterCount(); i++)
				featureSpaceList.get(i).set(x, y, fuzzyClassification[i]);
		}
		
		if(classifier instanceof FuzzyNoiseClassificationProvider) featureSpaceList.add(this.readNoiseFromClassifier((FuzzyNoiseClassificationProvider<double[]>)classifier));
		
		return featureSpaceList;
	}


	private FeatureSpaceClusterSampling2D readNoiseFromClassifier(FuzzyNoiseClassificationProvider<double[]> classifier)
	{
		double[] obj = new double[2];
		
		FeatureSpaceClusterSampling2D featureSpace = new FeatureSpaceClusterSampling2D(this.sizeX, this.sizeY, this.lowerLeftCorner, this.upperRightCorner, -1);
		
		for(int x=0; x<this.sizeX; x++) for(int y=0; y<this.sizeY; y++)
		{
			obj[0] = this.lowerLeftCorner[0]+(((double)x)+0.5d)*((this.upperRightCorner[0] - this.lowerLeftCorner[0])/(this.sizeX+1));
			obj[1] = this.lowerLeftCorner[1]+(((double)y)+0.5d)*((this.upperRightCorner[1] - this.lowerLeftCorner[1])/(this.sizeY+1));
		
			featureSpace.set(x, y, classifier.classifyNoise(obj));
		}
		
		return featureSpace;
	}
	
}
