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

import java.io.Serializable;

import java.util.ArrayList;

import data.set.IndexedDataObject;
import data.set.IndexedDataSet;
import etc.MyMath;
import generation.data.DataDistorter;
import generation.data.functions.BinaryAdd;
import generation.data.functions.BinaryExp;
import generation.data.functions.BinaryMul;
import generation.data.functions.BinarySqrt;
import generation.data.functions.Function;
import generation.data.functions.Identity;
import generation.data.functions.Reverse;
import generation.data.functions.UnaryConcentrateCentre;
import generation.data.functions.UnaryConcentrateLower;
import generation.data.functions.UnarySpreadLower;
import generation.data.functions.UnarySpreadCentre;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class DistortionTester extends TestVisualizer implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= 742963747828265375L;

	/**
	 * The data set to be clustered
	 */
	private ArrayList<double[]> data;
		
	private int dim;
	
	/**  */
	private ArrayList<double[]> clusterPositions;
		
	private ArrayList<DataDistorter> distortionLevels;
	
	/** */
	public DistortionTester(int dim)
	{
		super();
		
		this.dim = dim;
		this.data = new ArrayList<double[]>();
		this.clusterPositions = new ArrayList<double[]>();
		this.distortionLevels = new ArrayList<DataDistorter>();
	}
	
	/**
	 * Shows the generated data set.
	 */
	public void showDataSet(int x, int y)
	{
		this.xIndex = x;
		this.yIndex = y;
		IndexedDataSet<double[]> dataSet = new IndexedDataSet<double[]>(this.data.size());
		for(double[] d:this.data) dataSet.add(new IndexedDataObject<double[]>(d));
		this.showDataSet(dataSet, null);
	}
	
	
	public void generateStandardUniformData(int objectCount)
	{	
//		NormalDistribution normDistr = new NormalDistribution(0.0d, 1.0d);
		UniformRealDistribution normDistr = new UniformRealDistribution(0.0d, 1.0d);
		double[] d;
		int i, k;

//		this.data.clear();
		
		for(i=0; i<objectCount; i++)
		{
			d = new double[this.dim];
			for(k=0; k<this.dim; k++) d[k] = normDistr.sample();
						
			this.data.add(d);
		}
	}

	
	public void generateSphericalGaussianData(int objectCount)
	{
		NormalDistribution normDistr = new NormalDistribution(0.5d, 1.0d);
		double[] d;
		int i, k;

//		this.data.clear();
		
		for(i=0; i<objectCount; i++)
		{
			d = new double[this.dim];
			for(k=0; k<this.dim; k++) d[k] = normDistr.sample();
						
			this.data.add(d);
		}

		// scale data to [0, 1]^dim
		this.normalize();
	}
	
	public void normalize()
	{
		int k;
		double[] min = new double[dim];
		double[] max = new double[dim];
		
		for(k=0; k<dim; k++)
		{
			min[k] =  Double.MAX_VALUE;
			max[k] = -Double.MAX_VALUE;
		}
		
		for(double[] x:this.data)
		{
			for(k=0; k<this.dim; k++)
			{
				min[k] = (min[k] < x[k])? min[k] : x[k];
				max[k] = (max[k] > x[k])? max[k] : x[k];
			}
		}
		
		for(double[] x:this.data)
		{
			for(k=0; k<this.dim; k++)
			{
				x[k] -= min[k];
				x[k] /= max[k] - min[k];
			}
		}
	}
	
	/**
	 * Unary functions:<br>
	 * Identity<br>
	 * Reverse<br>
	 * UnaryConcentrateLower<br>
	 * UnarySpreadLower<br>
	 * UnaryConcentrateCentre<br>
	 * <br>
	 * Binary Functions: <br>
	 * Identity<br>
	 * BinaryAdd<br>
	 * BinaryMul<br>
	 * BinaryExp<br>
	 * BinarySqrt<br>
	 * 
	 * @param unaryWaights
	 * @param binaryWeights
	 */
	public void addDistortionLayers(double[] unaryWeights, double[] binaryWeights)
	{
		DataDistorter distorter;
		
		double[] unaryFreq = unaryWeights.clone();
		double[] binaryFreq = binaryWeights.clone();
		double tmp;
		
		
		tmp = 0.0d;
		for(int i=0; i<unaryWeights.length; i++)
		{
			tmp += unaryWeights[i];
		}
		for(int i=0; i<unaryWeights.length; i++)
		{
			unaryFreq[i] /= tmp;
		}
		
		tmp = 0.0d;
		for(int i=0; i<binaryWeights.length; i++)
		{
			tmp += binaryWeights[i];
		}
		for(int i=0; i<binaryWeights.length; i++)
		{
			binaryFreq[i] /= tmp;
		}
		
		ArrayList<Function> unaryFunctions = new ArrayList<Function>(6);
		ArrayList<Function> binaryFunctions = new ArrayList<Function>(5);
		
		unaryFunctions.add(new Identity(1));
		unaryFunctions.add(new Reverse());
		unaryFunctions.add(new UnaryConcentrateLower());
		unaryFunctions.add(new UnarySpreadLower());
		unaryFunctions.add(new UnaryConcentrateCentre());
		unaryFunctions.add(new UnarySpreadCentre());
		
		binaryFunctions.add(new Identity(2));
		binaryFunctions.add(new BinaryAdd());
		binaryFunctions.add(new BinaryMul());
		binaryFunctions.add(new BinaryExp());
		binaryFunctions.add(new BinarySqrt());
		
		distorter = new DataDistorter(this.dim, unaryFunctions, binaryFunctions, unaryFreq, binaryFreq);
		this.distortionLevels.add(distorter);
	}
	

	public void distortDataSet()
	{
		System.out.print("distort data ");
		int i=1;
		for(DataDistorter distorter: this.distortionLevels)
		{
			System.out.print(".");
			if(i%20 == 0) System.out.println(" " + i); 
			distorter.updateUnarySpreadFunctions(this.data);
			distorter.applyOnDataSet(this.data);
			this.normalize();
			i++;
		}
		System.out.println(" done. ");
	}
	
	/**
	 * @return the dataSet
	 */
	public ArrayList<double[]> getData()
	{
		return this.data;
	}
	
	public ArrayList<ArrayList<String>> distortionNames()
	{
		ArrayList<ArrayList<String>> list = new ArrayList<ArrayList<String>>(this.distortionLevels.size());
		for(int i=0; i<this.distortionLevels.size(); i++)
		{
			ArrayList<String> level = new ArrayList<String>(this.dim);
			for(int k=0; k<this.dim; k++)
			{
				level.add(this.distortionLevels.get(i).getUnaryFunctionList().get(k).getName());
			}
			for(int k=0; k<this.dim; k++)
			{
				level.add(this.distortionLevels.get(i).getBinaryFunctionList().get(k).getName() + "(" + this.distortionLevels.get(i).getBinarySecondAttributeIndex()[k] + ")");
			}
			
			list.add(level);
		}
		
		return list;
	}

	/**
	 * @return the clusterPositions
	 */
	public ArrayList<double[]> getClusterPositions()
	{
		return this.clusterPositions;
	}

	/**
	 * @return the dim
	 */
	public int getDim()
	{
		return this.dim;
	}
	
	
}
