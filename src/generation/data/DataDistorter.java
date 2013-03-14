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

import generation.data.functions.Function;
import generation.data.functions.UnarySpreadCentre;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.distribution.UniformRealDistribution;

import data.set.IndexedDataObject;
import data.set.IndexedDataSet;
import etc.SimpleStatistics;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class DataDistorter
{
	private int	dim;
	
	private ArrayList<Function> unaryFunctionDummies;
	private ArrayList<Function> binaryFunctionDummies;
	
	private double[] unaryDistortionFrequencies;
	private double[] binaryDistortionFrequencies;
	
	private ArrayList<Function> unaryFunctionList;
	private ArrayList<Function> binaryFunctionList;
	
	private int[] binarySecondAttributeIndex;
	
	/**
	 * @TODO: Check validity of the parameters!!
	 * 
	 * @param dim
	 * @param unaryFunctionDummies
	 * @param binaryFunctionDummies
	 */
	public DataDistorter(int dim, List<Function> unaryFunctionDummies, List<Function> binaryFunctionDummies)
	{
		this.dim = dim;
		
		this.unaryFunctionDummies = new ArrayList<Function>();
		this.binaryFunctionDummies = new ArrayList<Function>();

		this.unaryFunctionDummies.addAll(unaryFunctionDummies);
		this.binaryFunctionDummies.addAll(binaryFunctionDummies);

		this.unaryDistortionFrequencies = new double[this.unaryFunctionDummies.size()];
		this.binaryDistortionFrequencies = new double[this.binaryFunctionDummies.size()];
		
		this.unaryFunctionList = new ArrayList<Function>(this.dim);
		this.binaryFunctionList = new ArrayList<Function>(this.dim);
		
		this.binarySecondAttributeIndex = new int[this.dim];

		for(int i=0; i<this.unaryDistortionFrequencies.length; i++)
		{
			this.unaryDistortionFrequencies[i] = 1.0d/this.unaryDistortionFrequencies.length;
		}

		for(int i=0; i<this.binaryDistortionFrequencies.length; i++)
		{
			this.binaryDistortionFrequencies[i] = 1.0d/this.binaryDistortionFrequencies.length;
		}

		this.genUnaryFunctionMap();
		this.genBinaryFunctionMap();
	}
	

	/**
	 * @TODO: Check validity of the parameters!!
	 * 
	 * @param dim
	 * @param unaryFunctionDummies
	 * @param binaryFunctionDummies
	 * @param unaryFrequencies
	 * @param binaryFrequencies
	 */
	public DataDistorter(int dim, List<Function> unaryFunctionDummies, List<Function> binaryFunctionDummies, double[] unaryFrequencies, double[] binaryFrequencies)
	{
		this.dim = dim;

		this.unaryFunctionDummies = new ArrayList<Function>();
		this.binaryFunctionDummies = new ArrayList<Function>();

		this.unaryFunctionDummies.addAll(unaryFunctionDummies);
		this.binaryFunctionDummies.addAll(binaryFunctionDummies);

		this.unaryDistortionFrequencies = unaryFrequencies;
		this.binaryDistortionFrequencies = binaryFrequencies;

		this.unaryFunctionList = new ArrayList<Function>(this.dim);
		this.binaryFunctionList = new ArrayList<Function>(this.dim);

		this.binarySecondAttributeIndex = new int[this.dim];
		
		this.genUnaryFunctionMap();
		this.genBinaryFunctionMap();
	}
	

	public void normalize(double[] data)
	{
		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;
		
		for(int i=0; i<data.length; i++)
		{
			min = (data[i] < min)? data[i]:min;
			max = (data[i] > max)? data[i]:max;
		}
		
		for(int i=0; i<data.length; i++)
		{
			data[i] = (data[i] - min)/(max - min);
		}
	}
	
	public void genUnaryFunctionMap()
	{
		double[] distortionFrequenciesSums = new double[this.unaryDistortionFrequencies.length];
		UniformRealDistribution uniDistr = new UniformRealDistribution(0.0d, 1.0d);
		double select = 0.0d;
		int selctedIndex = 0;
		Function f;
		double[] functionParameter;
		
		this.unaryFunctionList.clear();

		distortionFrequenciesSums[0] = this.unaryDistortionFrequencies[0];
		for(int i=1; i<this.unaryDistortionFrequencies.length; i++)
		{
			distortionFrequenciesSums[i] = distortionFrequenciesSums[i-1] + this.unaryDistortionFrequencies[i];
		}
	
		for(int k=0; k<this.dim; k++)
		{
			select = uniDistr.sample();
			
			for(int i=0; i<this.unaryDistortionFrequencies.length; i++)
			{
				if(select <= distortionFrequenciesSums[i])
				{
					selctedIndex = i;
					break;
				}
			}
			
			f = this.unaryFunctionDummies.get(selctedIndex);
			functionParameter = new double[f.getParameterCount()];
			
			for(int i=0; i<f.getParameterCount(); i++)
			{
				functionParameter[i] = uniDistr.sample() * (f.getParameterBounds(i)[1] - f.getParameterBounds(i)[0]) + f.getParameterBounds(i)[0];
			}
			
			this.unaryFunctionList.add(f.newInstance(functionParameter));
		}
	}
	
	public void genBinaryFunctionMap()
	{
		double[] distortionFrequenciesSums = new double[this.binaryDistortionFrequencies.length];
		UniformRealDistribution uniDistr = new UniformRealDistribution(0.0d, 1.0d);
		double select = 0.0d;
		int selctedIndex = 0;
		int partnerIndex;
		Function f;
		double[] functionParameter;
		
		this.binaryFunctionList.clear();

		distortionFrequenciesSums[0] = this.binaryDistortionFrequencies[0];
		for(int i=1; i<this.binaryDistortionFrequencies.length; i++)
		{
			distortionFrequenciesSums[i] = distortionFrequenciesSums[i-1] + this.binaryDistortionFrequencies[i];
		}
	
		for(int k=0; k<this.dim; k++)
		{
			select = uniDistr.sample();
			
			for(int i=0; i<this.binaryDistortionFrequencies.length; i++)
			{
				if(select <= distortionFrequenciesSums[i])
				{
					selctedIndex = i;
					break;
				}
			}
			
			f = this.binaryFunctionDummies.get(selctedIndex);
			functionParameter = new double[f.getParameterCount()];
			
			for(int i=0; i<f.getParameterCount(); i++)
			{
				functionParameter[i] = uniDistr.sample() * (f.getParameterBounds(i)[1] - f.getParameterBounds(i)[0]) + f.getParameterBounds(i)[0];
			}
			
			this.binaryFunctionList.add(f.newInstance(functionParameter));

			do
			{
				partnerIndex = (int) (uniDistr.sample() * this.dim);
				if(partnerIndex == this.dim) partnerIndex = 0;
			}while(k == partnerIndex);
			
			this.binarySecondAttributeIndex[k] = partnerIndex;
		}
	}
	
	public void regenFunctionMap()
	{
		this.genUnaryFunctionMap();
		this.genBinaryFunctionMap();
	}
	
	public void updateUnarySpreadFunctions(List<double[]> data)
	{		
		for(int k=0; k<this.dim; k++)
		{
			if(this.unaryFunctionList.get(k) instanceof UnarySpreadCentre)
			{
				double[] list = new double[data.size()];
				
				for(int i=0; i<data.size(); i++)
				{
					list[i] = data.get(i)[k];
				}
				
				double[] stats = SimpleStatistics.mean_variance(list);
				
				this.unaryFunctionList.get(k).setParameter(stats[0], 0);
				this.unaryFunctionList.get(k).setParameter(Math.sqrt(stats[1]), 1);
			}
		}
	}
	
	
	public void updateUnarySpreadFunctionsIndexed(IndexedDataSet<double[]> data)
	{		
		for(int k=0; k<this.dim; k++)
		{
			if(this.unaryFunctionList.get(k) instanceof UnarySpreadCentre)
			{
				double[] list = new double[data.size()];
				
				for(int i=0; i<data.size(); i++)
				{
					list[i] = data.get(i).x[k];
				}
				
				double[] stats = SimpleStatistics.mean_variance(list);
				
				this.unaryFunctionList.get(k).setParameter(stats[0], 0);
				this.unaryFunctionList.get(k).setParameter(Math.sqrt(stats[1]), 1);
			}
		}
	}
	
	public void applyOnDataSet(List<double[]> data)
	{
		int k;
		double[] col = new double[data.size()];

		// apply unary functions
		for(k=0; k<this.dim; k++)
		{
			for(int i=0; i<data.size(); i++)
			{
				col[i] = this.unaryFunctionList.get(k).apply(data.get(i), k); 
			}
			this.normalize(col);
			for(int i=0; i<data.size(); i++)
			{
				data.get(i)[k] = col[i]; 
			}
		}

		// apply binary functions
		for(k=0; k<this.dim; k++)
		{			
			for(int i=0; i<data.size(); i++)
			{				
				col[i] = this.binaryFunctionList.get(k).apply(data.get(i), k, this.binarySecondAttributeIndex[k]);
			}
			this.normalize(col);
			for(int i=0; i<data.size(); i++)
			{
				data.get(i)[k] = col[i]; 
			}
		}
	}
	
	public void applyOnIndexedDataSet(IndexedDataSet<double[]> data)
	{
		int k;
		double[] col = new double[data.size()];

		// apply unary functions
		for(k=0; k<this.dim; k++)
		{
			for(int i=0; i<data.size(); i++)
			{
				col[i] = this.unaryFunctionList.get(k).apply(data.get(i).x, k); 
			}
			this.normalize(col);
			for(int i=0; i<data.size(); i++)
			{
				data.get(i).x[k] = col[i]; 
			}
		}

		// apply binary functions
		for(k=0; k<this.dim; k++)
		{			
			for(int i=0; i<data.size(); i++)
			{				
				col[i] = this.binaryFunctionList.get(k).apply(data.get(i).x, k, this.binarySecondAttributeIndex[k]);
			}
			this.normalize(col);
			for(int i=0; i<data.size(); i++)
			{
				data.get(i).x[k] = col[i]; 
			}
		}
	}


	/**
	 * @return the dim
	 */
	public int getDim()
	{
		return this.dim;
	}


	/**
	 * @return the unaryFunctionList
	 */
	public ArrayList<Function> getUnaryFunctionList()
	{
		return this.unaryFunctionList;
	}


	/**
	 * @return the binaryFunctionList
	 */
	public ArrayList<Function> getBinaryFunctionList()
	{
		return this.binaryFunctionList;
	}


	/**
	 * @return the binarySecondAttributeIndex
	 */
	public int[] getBinarySecondAttributeIndex()
	{
		return this.binarySecondAttributeIndex;
	}


	/**
	 * @return the unaryFunctionDummies
	 */
	public ArrayList<Function> getUnaryFunctionDummies()
	{
		return this.unaryFunctionDummies;
	}

	/**
	 * @return the binaryFunctionDummies
	 */
	public ArrayList<Function> getBinaryFunctionDummies()
	{
		return this.binaryFunctionDummies;
	}

	/**
	 * @return the unaryDistortionFrequencies
	 */
	public double[] getUnaryDistortionFrequencies()
	{
		return this.unaryDistortionFrequencies;
	}

	/**
	 * @return the binaryDistortionFrequencies
	 */
	public double[] getBinaryDistortionFrequencies()
	{
		return this.binaryDistortionFrequencies;
	}

	
}
