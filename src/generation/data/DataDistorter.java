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
package generation.data;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import data.set.IndexedDataObject;
import data.set.IndexedDataSet;
import etc.MyMath;
import etc.Parallel;
import etc.SimpleStatistics;
import generation.data.DistortionLayer;
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
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class DataDistorter implements Serializable
{
	private int dim;
	
	private ArrayList<DistortionLayer> distortionLevels;
	
	/** */
	public DataDistorter(int dim)
	{
		super();
		
		this.dim = dim;
		
		this.distortionLevels = new ArrayList<DistortionLayer>();
	}	

	public void normalizeParallel(List<double[]> data)
	{
		class NormalizeData
		{
			public List<double[]> dataList;
			public int k;
			/**
			 * @param data
			 * @param k
			 */
			public NormalizeData(List<double[]> dataList, int k)
			{
				this.dataList = dataList;
				this.k = k;
			}
		};

		ArrayList<NormalizeData> normalizeDataList = new ArrayList<NormalizeData>(this.dim);
		for(int k=0; k<this.dim; k++)
		{
			normalizeDataList.add(new NormalizeData(data, k));
		}
		
		// normalize in parallel
		Parallel.ForFJ(normalizeDataList, 
			 new Parallel.Operation<NormalizeData>()
			 {
			    public void perform(NormalizeData norm)
			    {
			    	double min = Double.MAX_VALUE;
			    	double max = -Double.MAX_VALUE;
			    	double tmp;
			    	for(int i=0; i<norm.dataList.size(); i++)
					{
			    		tmp = norm.dataList.get(i)[norm.k];
			    		min = (min > tmp)? tmp : min;
			    		max = (max < tmp)? tmp : max;
					}
			    	tmp = 1.0d/(max - min);
			    	for(int i=0; i<norm.dataList.size(); i++)
					{
			    		norm.dataList.get(i)[norm.k] = tmp*(norm.dataList.get(i)[norm.k] - min);   
					}
			    };
			}
		);		
	}
	
	public void redistributeBoarderPointsParallel(List<double[]> data)
	{
		class DataStruct
		{
			public List<double[]> data;
			public int k;
			/**
			 * @param data
			 * @param k
			 */
			public DataStruct(List<double[]> data, int k)
			{
				this.data = data;
				this.k = k;
			}
		};


		ArrayList<DataStruct> list = new ArrayList<DataStruct>(this.dim);
		for(int k=0; k<this.dim; k++)
		{
			list.add(new DataStruct(data, k));
		}  

		// apply unary functions in parallel
		Parallel.ForFJ(list,
			 new Parallel.Operation<DataStruct>()
			 {
			    public void perform(DataStruct dStruct)
			    {
					double[] col = new double[dStruct.data.size()]; 
					double[] colQuantil;
					
			    	for(int i=0; i<dStruct.data.size(); i++)
					{
						col[i] = dStruct.data.get(i)[dStruct.k];
					}
					
					Arrays.sort(col);
					colQuantil = Arrays.copyOfRange(col, 5, col.length-5);

					double[] meanVariance = SimpleStatistics.mean_variance(colQuantil);
					
					NormalDistribution normal = new NormalDistribution(meanVariance[0], meanVariance[1]);
					
					for(int i=0; i<dStruct.data.size(); i++)
					{
						if(dStruct.data.get(i)[dStruct.k] == 0.0d || 1.0d == dStruct.data.get(i)[dStruct.k])
						{
							dStruct.data.get(i)[dStruct.k] = normal.sample();
							if(dStruct.data.get(i)[dStruct.k] <= 0.0d || 1.0d <= dStruct.data.get(i)[dStruct.k]) dStruct.data.get(i)[dStruct.k] = meanVariance[0];
						}
					}
			    };
			}
		);
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
		DistortionLayer distorter;
		
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
		
		distorter = new DistortionLayer(this.dim, unaryFunctions, binaryFunctions, unaryFreq, binaryFreq);
		this.distortionLevels.add(distorter);
	}
	

	public void distortData(List<double[]> data)
	{
		int i=1;
//		this.normalize();
		for(DistortionLayer distorter: this.distortionLevels)
		{
			System.out.print(".");
			if(i%20 == 0) System.out.println(" " + i);
			distorter.updateUnarySpreadFunctions(data);
			distorter.applyDistortionsParallel(data);
			this.normalizeParallel(data);
			this.redistributeBoarderPointsParallel(data);
			i++;
		}
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
	 * @return the dim
	 */
	public int getDim()
	{
		return this.dim;
	}
}
