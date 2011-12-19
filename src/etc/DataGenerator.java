/**
Copyright (c) 2011, The EDMOAL Project

	DLR Deutsches Zentrum fuer Luft- und Raumfahrt e.V.
	German Aerospace Center e.V.
	Institut fuer Flugfuehrung/Institute of Flight Guidance
	Tel. +49 531 295 2500, Fax: +49 531 295 2550
	WWW: http://www.dlr.de/fl/		
 
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
    	this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice,
    	this list of conditions and the following disclaimer in the documentation and/or
    	other materials provided with the distribution.
    * Neither the name of the DLR nor the names of its contributors
    	may be used to endorse or promote products derived from this software
    	without specific prior written permission.

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


package etc;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class DataGenerator implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= -7857284348841449918L;
	public final Random generatorRand;

	/**	 */
	public DataGenerator()
	{
		super();
		this.generatorRand = new Random();
	}	

	/**
	 * @param randomFeed
	 */
	public DataGenerator(long randomFeed)
	{
		super();
		this.generatorRand = new Random(randomFeed);
	}

	public ArrayList<double[]> gaussStandardPoints(int dimemsions, int number)
	{
		return this.gaussPoints(new double[dimemsions], 1.0d, number);
	}

	public ArrayList<double[]> gaussPoints(double[] center, double standardDeviation, int dim, int number)
	{
		ArrayList<double[]> points = new ArrayList<double[]>(number);
		double[] x = new double[dim];
//		double standardDeviation = Math.sqrt(variance);
		
		for(int k=0; k<number; k++)
		{
			for(int i=0; i<dim; i++)
			{
				x[i] = standardDeviation * this.generatorRand.nextGaussian() + center[i];
			}
			
			points.add(x.clone());
		}
		
		return points;
	}

	public ArrayList<double[]> gaussPoints(double[] center, double standardDeviation, int number)
	{
		ArrayList<double[]> points = new ArrayList<double[]>(number);
		double[] x = new double[center.length];
//		double standardDeviation = Math.sqrt(variance);
//		radius /= Math.sqrt(center.length);
		
		for(int k=0; k<number; k++)
		{
			for(int i=0; i<center.length; i++)
			{
				x[i] = standardDeviation * this.generatorRand.nextGaussian() + center[i];
			}
			
			points.add(x.clone());
		}
		
		return points;
	}
	
	public ArrayList<double[]> uniformPoints(double[] corner1, double[] corner2, int dim, int number)
	{
		ArrayList<double[]> points = new ArrayList<double[]>(number);
		double[] x = new double[dim];
		
		for(int k=0; k<number; k++)
		{
			for(int i=0; i<dim; i++)
			{
				x[i] = (corner2[i] - corner1[i])*this.generatorRand.nextDouble() + corner1[i];
			}
			
			points.add(x.clone());
		}
		
		return points;
	}
	
	public ArrayList<double[]> uniformPoints(double[] corner1, double[] corner2, int number)
	{
		ArrayList<double[]> points = new ArrayList<double[]>(number);
		double[] x = new double[corner1.length];
		
		for(int k=0; k<number; k++)
		{
			for(int i=0; i<corner1.length; i++)
			{
				x[i] = (corner2[i] - corner1[i])*this.generatorRand.nextDouble() + corner1[i];
			}
			
			points.add(x.clone());
		}
		
		return points;
	}

	public ArrayList<double[]> uniformStandardPoints(int dimensions, int number)
	{
		double[] corner1 = new double[dimensions];
		double[] corner2 = new double[dimensions];

		int i;
		
		for(i=0; i<dimensions; i++) corner1[i] = 0.0d;	
		for(i=0; i<dimensions; i++) corner2[i] = 1.0d;	
		
		return this.uniformPoints(corner1, corner2, number);
	}
	
	/**
	 * @param dim
	 * @return
	 */
	public double[] uniformArray(int dim)
	{
		double[] array = new double[dim];
		for(int i=0; i<dim; i++)
		{
			array[i] = this.generatorRand.nextDouble();
		}
				
		return array;
	}
	
	/**
	 * @param dimension
	 * @param radius
	 * @param number
	 * @return
	 */
	public ArrayList<double[]> uniformHypersphereSurfacePoints(int dimension, double radius, int number)
	{
		ArrayList<double[]> points = new ArrayList<double[]>();
		int i, j;
		double[] x;
		double length;
		
		for(i=0; i<number;i++)
		{
			x = new double[dimension];
			length = 0;
			for(j=0; j<dimension; j++)
			{
				x[j] = this.generatorRand.nextGaussian();
				length += x[j]*x[j];
			}
			length = radius/Math.sqrt(length);
			for(j=0; j<dimension; j++)
			{
				x[j] *= length;
			}
			
			points.add(x);
		}
		
		return points;
	}
	
	/**
	 * @param dimensions
	 * @param numberOfSeeds
	 * @param numberOfDataPerSeed
	 * @param gaussVariance
	 * @return the seeds and the data in separate vector objects, combined in a vector
	 */
	public ArrayList<ArrayList<double[]>> gaussianScatteredUniformSeededPoints(int dimensions, int numberOfSeeds, int numberOfDataPerSeed, double gaussVariance)
	{
		ArrayList<double[]> seeds = new ArrayList<double[]>();
		ArrayList<double[]> data = new ArrayList<double[]>();		
		ArrayList<ArrayList<double[]>> result = new ArrayList<ArrayList<double[]>>();
		
		int i;
		
		double[] corner1 = new double[dimensions];
		double[] corner2 = new double[dimensions];
		
		for(i=0; i<dimensions; i++) corner1[i] = 0.0d;	
		for(i=0; i<dimensions; i++) corner2[i] = 1.0d;		
		seeds.addAll(this.uniformPoints(corner1, corner2, numberOfSeeds));
		for(double[] seed:seeds) data.addAll(this.gaussPoints(seed, gaussVariance, numberOfDataPerSeed));
		
		result.add(seeds);
		result.add(data);
		
		return result;
	}

	/**
	 * @param dimensions
	 * @param numberOfSeeds
	 * @param numberOfDataPerSeed
	 * @param gaussVariance
	 * @param numberOfNoisePoints
	 * @param shuffle
	 * @return the seeds and the data in separate vector objects, combined in a vector
	 */
	public ArrayList<ArrayList<double[]>> gaussianScatteredUniformSeededPoints(int dimensions, int numberOfSeeds, int numberOfDataPerSeed, double gaussVariance, int numberOfNoisePoints, boolean shuffle)
	{
		ArrayList<double[]> seeds = new ArrayList<double[]>();
		ArrayList<double[]> data = new ArrayList<double[]>();		
		ArrayList<ArrayList<double[]>> result = new ArrayList<ArrayList<double[]>>();
		
		int i;
		
		double[] corner1 = new double[dimensions];
		double[] corner2 = new double[dimensions];
		
		for(i=0; i<dimensions; i++) corner1[i] = 0.0d;	
		for(i=0; i<dimensions; i++) corner2[i] = 1.0d;		
		seeds.addAll(this.uniformPoints(corner1, corner2, numberOfSeeds));
		for(double[] seed:seeds) data.addAll(this.gaussPoints(seed, gaussVariance, numberOfDataPerSeed));

		data.addAll(this.uniformPoints(corner1, corner2, numberOfNoisePoints));
		
		if(shuffle) Collections.shuffle(data);
		
		result.add(seeds);
		result.add(data);
				
		return result;
	}
}
