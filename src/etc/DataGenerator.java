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
import java.util.Random;

/**
 * This class provides a collection of functions for creating data sets of various distributions.
 * For generating the pseudo-random data sets, the java class {@link Random} is used.
 * The generated pseudo-random numbers are only as good as the java generator delivers them. 
 * 
 * @author Roland Winkler
 */
public class DataGenerator implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= -7857284348841449918L;
	
	/**
	 * The random object, used for generating pseudo-random numbers.
	 */
	public final Random generatorRand;

	/**
	 * The standard constructor, using the internal standard seed generator
	 * of the java VM.
	 * 
	 * @see java.util.Random#Random()
	 */
	public DataGenerator()
	{
		super();
		this.generatorRand = new Random();
	}	

	/**
	 * Creates a new data generator, using the specified seed.
	 * 
	 * @param randomSeed The seed to initialise the {@link Random} object with.
	 */
	public DataGenerator(long randomSeed)
	{
		super();
		this.generatorRand = new Random(randomSeed);
	}

	/**
	 * Creates a sample of standard normal distributed points with the specified number of dimensions.
	 * 'standard' normal distribution means: expectation value 0 and variance 1.
	 * 
	 * @param dimemsions The number of dimensions.
	 * @param number The number of double arrays to be generated.
	 * @return A list of double arrays, with values following the standard normal distribution.
	 */
	public ArrayList<double[]> gaussStandardPoints(int dimemsions, int number)
	{
		return this.gaussPoints(new double[dimemsions], 1.0d, number);
	}

	/**
	 * Creates a sample of spherical normal distributed double arrays with the specified dimensionality.
	 * The normal distribution has the specified expectation value and standard deviation. <br>
	 * 
	 * The <code>expValue</code> double array may have more elements than the specified dimension, in this case
	 * only the first <code>dim</code> elements are used for the sampling.
	 * 
	 * @param expValue The expectation value of the normal distribution.
	 * @param standardDeviation The standard deviation of the normal distribution.
	 * @param dim The dimensionality of the double arrays.
	 * @param number The number of double arrays to be generated.
	 * @return A sample of normal distributed double values.
	 */
	public ArrayList<double[]> gaussPoints(double[] expValue, double standardDeviation, int dim, int number)
	{
		ArrayList<double[]> points = new ArrayList<double[]>(number);
		double[] x = new double[dim];
//		double standardDeviation = Math.sqrt(variance);
		
		for(int k=0; k<number; k++)
		{
			for(int i=0; i<dim; i++)
			{
				x[i] = standardDeviation * this.generatorRand.nextGaussian() + expValue[i];
			}
			
			points.add(x.clone());
		}
		
		return points;
	}

	/**
	 * Creates a sample of spherical normal distributed double arrays. The dimensionality
	 * is implicitly defined by the number of elements of the <code>expValue</code> attribute.
	 * The normal distribution has the specified expectation value and standard deviation. <br>
	 * 
	 * @param expValue The expectation value of the normal distribution.
	 * @param standardDeviation The standard deviation of the normal distribution.
	 * @param number The number of double arrays to be generated.
	 * @return A sample of normal distributed double values.
	 */
	public ArrayList<double[]> gaussPoints(double[] expValue, double standardDeviation, int number)
	{
		ArrayList<double[]> points = new ArrayList<double[]>(number);
		double[] x = new double[expValue.length];
//		double standardDeviation = Math.sqrt(variance);
//		radius /= Math.sqrt(center.length);
		
		for(int k=0; k<number; k++)
		{
			for(int i=0; i<expValue.length; i++)
			{
				x[i] = standardDeviation * this.generatorRand.nextGaussian() + expValue[i];
			}
			
			points.add(x.clone());
		}
		
		return points;
	}
	
	/**
	 * Creates a sample of on a hyper rectangle uniform distributed double arrays with the specified dimensionality.
	 * The hyper rectangle is specified by the two corners. The double arrays of the two
	 * corners may have more elements than the specified dimension, in this case
	 * only the first <code>dim</code> elements are used for defining it. <br>
	 * 
	 * @param corner1 The first corner of the hyper rectangle.
	 * @param corner2 The second corner of the hyper rectangle.
	 * @param dim The dimensionality of the generated double arrays.
	 * @param number The number of data objects that are sampled.
	 * @return a list of double arrays, with values sampled from the specified uniform distribution.
	 */
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

	/**
	 * Creates a sample of on a hyper rectangle uniform distributed double arrays.
	 * The hyper rectangle is specified by the two corners. The double arrays of the two
	 * corners define the dimensionality of the resulting double arrays. <br>
	 * 
	 * @param corner1 The first corner of the hyper rectangle.
	 * @param corner2 The second corner of the hyper rectangle.
	 * @param number The number of data objects that are sampled.
	 * @return a list of double arrays, with values sampled from the specified uniform distribution.
	 */
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

	/**
	 * Creates a sample of on a standard hyper cube uniform distributed double arrays.
	 * The hyper cube is standard in the sense that it is defined by [0, 1]^<code>dim</code>.
	 * 
	 * @param dim The dimensionality of the generated double arrays.
	 * @param number The number of data objects that are sampled.
	 * @return a list of double arrays, with values sampled from the specified uniform distribution.
	 */
	public ArrayList<double[]> uniformStandardPoints(int dim, int number)
	{
		double[] corner1 = new double[dim];
		double[] corner2 = new double[dim];

		int i;
		
		for(i=0; i<dim; i++) corner1[i] = 0.0d;	
		for(i=0; i<dim; i++) corner2[i] = 1.0d;	
		
		return this.uniformPoints(corner1, corner2, number);
	}
	
	/**
	 * Creates a single double array with standard (each dimension in [0, 1]) uniform distributed data objects. 
	 * 
	 * @param dim The dimensionality of the array.
	 * @return An array of uniform distributed double values.
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
	 * Creates a sample of on a hyper sphere surface uniform distributed double arrays.
	 * In other words, the cumulative probability of a vector being sampled in a specified
	 * area A is equal to A/S where S is the (<code>dim</code>-1 dimensional) surface area of the
	 * (<code>dim</code>-dimensional) hyper sphere. Thus, the double arrays have
	 * <code>dim</code> many elements, but the probability distribution is <code>dim</code>-1
	 * dimensional.
	 * 
	 * @param dim The dimension of the double arrays.
	 * @param radius The radius of the hyper sphere
	 * @param number The number of double arrays sampled.
	 * @return A list of double arrays, sampled from the specified hyper sphere surface.
	 */
	public ArrayList<double[]> uniformHypersphereSurfacePoints(int dim, double radius, int number)
	{
		ArrayList<double[]> points = new ArrayList<double[]>();
		int i, j;
		double[] x;
		double length;
		
		for(i=0; i<number;i++)
		{
			x = new double[dim];
			length = 0;
			for(j=0; j<dim; j++)
			{
				x[j] = this.generatorRand.nextGaussian();
				length += x[j]*x[j];
			}
			length = radius/Math.sqrt(length);
			for(j=0; j<dim; j++)
			{
				x[j] *= length;
			}
			
			points.add(x);
		}
		
		return points;
	}
}
