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


package datamining.clustering.protoype.initial;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import data.algebra.Metric;
import data.algebra.VectorSpace;
import data.set.IndexedDataObject;
import datamining.clustering.protoype.Centroid;
import etc.DataGenerator;

/**
 * Generates {@link Centroid}s specifically for double array data objects. 
 * Several distributions are available. This class is most useful for distributions that are based
 * on a specific data set. So that prototype initializations are bound by the data set at hand. 
 *
 * @author Roland Winkler
 */
public class DoubleArrayPrototypeGenerator extends PrototypeGenerator<double[]> implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= 2089339786778802373L;
	
	
	/** The data generator that is used for generating the random locations. */
	protected DataGenerator dataGenerator; 
	
	/**
	 * Creates a new DoubleArrayPrototypeGenerator with the specified seed and vector space.
	 * 
	 * @param seed The seed that should be used for the eneration of the pseudo-random locations of the prototypes.
	 * @param vs The vector space.
	 */
	public DoubleArrayPrototypeGenerator(long seed, VectorSpace<double[]> vs)
	{
		super(seed, vs);
		this.dataGenerator = new DataGenerator(seed);
	}

	/**
	 * Creates a new DoubleArrayPrototypeGenerator with the vector space. For the pseudo-random locations of the
	 * prototypes, the seed generation of the {@link Random} class is used.
	 * 
	 * @param vs The vector space.
	 */
	public DoubleArrayPrototypeGenerator(VectorSpace<double[]> vs)
	{
		super(vs);
		this.dataGenerator = new DataGenerator();
	}

	/**
	 * Uniform distributed prototypes on the hyper rectangle defined by two opposite corners x and y
	 * 
	 * @param x The first corner of the hyper rectangle
	 * @param y The second corner of the hyper rectangle
	 * @param number The number of centroids to be generated.
	 * @return The list of generated centroids.
	 */
	public ArrayList<Centroid<double[]>> randomUniformOnBounds(double[] x, double[] y, int number)
	{
		ArrayList<Centroid<double[]>> prototypes = new ArrayList<Centroid<double[]>>(number);
		Collection<double[]> positions;
		
		if(x.length < this.vs.getDimension() || y.length < this.vs.getDimension()) throw new IllegalArgumentException("The arrays x and y must be at least the size of the dimensionality of the vector space.");
				
		positions = this.dataGenerator.uniformPoints(x, y, this.vs.getDimension(), number);
		
		for(double[] p: positions)	prototypes.add(new Centroid<double[]>(this.vs, p));
		
		return prototypes;
	}
	
	/**
	 * Uniform distributed centroids on the unit hyper cube [0, 1]^dim, with the dimension specified in the vector space.
	 * 
	 * @param number The number of centroids to be generated.
	 * @return The list of generated centroids.
	 */
	public ArrayList<Centroid<double[]>> randomUniformOnUnitCube(int number)
	{
		int k;
		int dimension = this.vs.getDimension();
		
		double[] lowerCorner = new double[dimension];
		double[] upperCorner = new double[dimension];
		
		for(k=0; k<this.vs.getDimension(); k++) upperCorner[k] = 1.0d;
		
		return this.randomUniformOnBounds(lowerCorner, upperCorner, number);
	}
	
	/**
	 * Generates a set of centroids that are uniform distributed on a hyper rectangle, defined by the specified data set.
	 * The data set is used to define a minimal, axis parallel hyper rectangle, containing all data objects.
	 * Then, the centroids are generated using a uniform distribution on the hyper rectangle.   
	 * 
	 * @param data The data set for specifying the hyper rectangle.
	 * @param number The number of centroids to be generated.
	 * @return The list of generated centroids.
	 */
	public ArrayList<Centroid<double[]>> randomUniformOnDataBounds(Collection<IndexedDataObject<double[]>> data, int number)
	{
		int k;
		int dimension = this.vs.getDimension();
		
		double[] lowerCorner = new double[dimension];
		double[] upperCorner = new double[dimension];
						
		if(data == null || data.size() == 0)
		{
			return this.randomUniformOnUnitCube(number);
		}
		
		for(k=0; k<dimension; k++)
		{
			lowerCorner[k] = Double.POSITIVE_INFINITY;
			upperCorner[k] = Double.NEGATIVE_INFINITY;
		}
		
		for(IndexedDataObject<double[]> d:data)
		{				
			for(k=0; k<dimension; k++)
			{
				if(lowerCorner[k] > d.x[k]) lowerCorner[k] = d.x[k];
				if(upperCorner[k] < d.x[k]) upperCorner[k] = d.x[k];
			}
		}
	
		return this.randomUniformOnBounds(lowerCorner, upperCorner, number);
	}
	
	/**
	 * Generates a specified number of centroids in a circle around the specified center with the specified radius.
	 * The centroids are regularly located on the circle boarder with equal distance to their neighbors.
	 * 
	 * @param centre The center of the circle.
	 * @param radius The radius of the circle.
	 * @param number The number of centroids to be generated.
	 * @return The list of generated centroids.
	 */
	public ArrayList<Centroid<double[]>> circleRegular_2D(double[] centre, double radius, int number)
	{
		int i;
		double arc = 2.0d*Math.PI / ((double)number);
		double[] pos;
		ArrayList<Centroid<double[]>> prototypes = new ArrayList<Centroid<double[]>>(number);
				
		for(i=0; i<number; i++)
		{
			pos = new double[this.vs.getDimension()];
						
			pos[0] = 0.5d + radius * Math.sin(arc * ((double)i)) + centre[0];
			pos[1] = 0.5d + radius * Math.cos(arc * ((double)i)) + centre[1];
			
			prototypes.add(new Centroid<double[]>(this.vs, pos));
		}
		
		return prototypes;
	}

	/**
	 * Generates normal distributed prototypes with the normal distribution defined by the specified
	 * expectation value and variance. 
	 * 
	 * @param expectation The expectation value of the normal distribution.
	 * @param variance The variance of the normal distribution.
	 * @param number The number of centroids to be generated.
	 * @return The list of generated centroids.
	 */
	public ArrayList<Centroid<double[]>> randomSphericalNormal(double[] expectation, double variance, int number)
	{
		ArrayList<Centroid<double[]>> prototypes = new ArrayList<Centroid<double[]>>(number);
		Collection<double[]> positions;
		
		if(expectation.length < this.vs.getDimension()) throw new IllegalArgumentException("The arrays x and y must be at least the size of the dimensionality of the vector space.");
		
		positions = this.dataGenerator.gaussPoints(expectation, Math.sqrt(variance), this.vs.getDimension(), number);
		
		for(double[] p: positions)	prototypes.add(new Centroid<double[]>(this.vs, p));
		
		return prototypes;
	}

	/**
	 * 
	 * Generates normal distributed prototypes with a normal distribution that is estimated from the specified
	 * data set. In a first step, the mean and sample variance of the data set are calculated and than,
	 * the specified number of centroids are generated using the estimated normal distribution. 
	 * 
	 * @param data The data set that specifies the normal distribution.
	 * @param dist The metric, used for estimating the normal distribution.
	 * @param number The number of centroids to be generated.
	 * @return The list of generated centroids.
	 */
	public ArrayList<Centroid<double[]>> randomSphericalNormalLikeDataSet(Collection<IndexedDataObject<double[]>> data, Metric<double[]> dist, int number)
	{
		double[] exp = new double[this.vs.getDimension()];
		double variance = 0;
		double invDataCount = 1.0d/((double)data.size());
		
		for(IndexedDataObject<double[]> d:data)
		{
			this.vs.add(exp, d.x);
		}

		this.vs.mul(exp, invDataCount);
		
		for(IndexedDataObject<double[]> d:data)
		{
			variance += dist.distanceSq(exp, d.x);
		}
		
		variance /= ((double)(data.size()+1));
		
		return this.randomSphericalNormal(exp, variance, number);
	}
	
}
