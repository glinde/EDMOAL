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

import java.util.ArrayList;

import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.commons.math3.random.RandomDataGenerator;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class WeightedMixtureDataGenerator<T> extends AbstractDataGenerator<T>
{
	private ArrayList<DataGenerator<T>> generators;
	private UniformRealDistribution selsctionDistr;
	private ArrayList<Double> weights;					// ugh.. this is sooooo ugly!!! Java, go and get fixed!
	private double[] normedWeights;
	private double[] cumulatedNormedWeights;			// starts with a leading 0.0d
	private double weightSum;
	
	
	private boolean probabilisticWeights;
	
	private int[] assignements;
	
	public WeightedMixtureDataGenerator()
	{
		this.generators = new ArrayList<DataGenerator<T>>();
		this.selsctionDistr = new UniformRealDistribution(0.0d, 1.0d);
		this.weights = new ArrayList<Double>();
		this.normedWeights = new double[0];
		this.cumulatedNormedWeights = new double[1];
		this.weightSum = 0.0d;
		
		this.probabilisticWeights = true;
		this.assignements = null;
	}

	public void addDataGenerator(DataGenerator<T> gen, double weight)
	{
		this.generators.add(gen);
		this.weights.add(weight);
		this.weightSum += weight;
	}
	
	private void fixWeights()
	{
		if(this.generators.size() != normedWeights.length)
		{
			this.normedWeights = new double[this.generators.size()];
			this.cumulatedNormedWeights = new double[this.generators.size()+1];

			double w = 1.0d/this.weightSum;
			
			for(int i=0; i<this.generators.size(); i++)
			{
				this.normedWeights[i] = w*this.weights.get(i);
				this.cumulatedNormedWeights[i+1] = this.cumulatedNormedWeights[i]+this.normedWeights[i];
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see generation.data.DataGenerator#nextRandomObject()
	 */
	@Override
	public T nextRandomObject()
	{
		this.fixWeights();
				
		int index = binaryDistrSearch(this.cumulatedNormedWeights, this.selsctionDistr.sample());
			
		return this.generators.get(index).nextRandomObject();
	}
	
	/* (non-Javadoc)
	 * @see generation.data.DataGenerator#generateDataObjects(int)
	 */
	@Override
	public ArrayList<T> generateDataObjects(int number)
	{
		ArrayList<T> data;
		
		double weightSum = 0.0d;
		double[] normedWeights = new double[this.weights.size()];
		this.assignements = new int[number];
				
		for(Double d:this.weights)
		{
			weightSum += d;
		}
		weightSum = 1.0d/weightSum;
		for(int i=0; i<this.weights.size(); i++)
		{
			normedWeights[i] = weightSum * this.weights.get(i); 
		}
		
		if(this.probabilisticWeights)
		{
			data = generateDataObjectsProbabilistic(number);					
		}
		else
		{
			data = generateDataObjectsDeterministic(number);
		}
		
		return data;
	}
	
	
	private ArrayList<T> generateDataObjectsDeterministic(int number)
	{
		this.fixWeights();
		
		ArrayList<T> data = new ArrayList<T>(number);
		ArrayList<T> shuffeledData = new ArrayList<T>(number);
		int intTMP=0;
		int[] shuffle;
		int[] unshuffledAssignemnts = new int[number];

		// generate data
		for(int i=0; i<this.normedWeights.length; i++)
		{
			intTMP = (int)(number*this.normedWeights[i]);
			for(int k=0; k<intTMP; k++)
			{
				unshuffledAssignemnts[data.size() + k] = i;
			}
			data.addAll(this.generators.get(i).generateDataObjects(intTMP));
			
		}
		
		// if less data is generated than it should be.. add some data objects from uniformly selected buckets
		UniformIntegerDistribution uniformInt = new UniformIntegerDistribution(0, this.normedWeights.length-1);		
		while(data.size() < number)
		{
			intTMP = uniformInt.sample();
			unshuffledAssignemnts[data.size()] = intTMP;
			data.add(this.generators.get(intTMP).nextRandomObject());
		}
		
		// shuffle
		RandomDataGenerator random = new RandomDataGenerator();
		
		shuffle = random.nextPermutation(number, number);
		for(int i=0; i<shuffle.length; i++)
		{
			shuffeledData.add(data.get(shuffle[i]));
			this.assignements[i] = unshuffledAssignemnts[shuffle[i]];
		}
		
		return shuffeledData;
	}

	private ArrayList<T> generateDataObjectsProbabilistic(int number)
	{		
		this.fixWeights();	
		
		ArrayList<T> data = new ArrayList<T>(number);		
		int index=0;	
		double[] distrSelection = this.selsctionDistr.sample(number);		

		// select the distributions
		for(int i=0; i<number; i++)
		{
			index = binaryDistrSearch(this.cumulatedNormedWeights, distrSelection[i]);
			this.assignements[i] = index;
			data.add(this.generators.get(index).nextRandomObject());
		}
		
		return data;
	}
	
	private int binaryDistrSearch(double[] cumulativeDistr, double target)
	{
		int min=1, max=cumulativeDistr.length-1, mid=max/2;
		
		while (max >= min)
		{
			mid=(min+max)/2;
			
			if(cumulativeDistr[mid] >= target && cumulativeDistr[mid-1] < target)
				return mid-1;
			
			if(cumulativeDistr[mid] <= target)
				min = mid+1;
			else
				max = mid-1;			
		}
		 
		System.err.println("WARNING: Binary Search Wrong!");
		return mid-1;
	}

	/**
	 * @return the probabilisticWeights
	 */
	public boolean isProbabilisticWeights()
	{
		return this.probabilisticWeights;
	}

	/**
	 * @param probabilisticWeights the probabilisticWeights to set
	 */
	public void setProbabilisticWeights(boolean probabilisticWeights)
	{
		this.probabilisticWeights = probabilisticWeights;
	}

	/**
	 * @return the generators
	 */
	public ArrayList<DataGenerator<T>> getGenerators()
	{
		return this.generators;
	}

	/**
	 * @return the weights
	 */
	public ArrayList<Double> getWeights()
	{
		return this.weights;
	}

	/**
	 * @return the assignements
	 */
	public int[] assignementsOfLastGeneration()
	{
		return this.assignements;
	}
}
