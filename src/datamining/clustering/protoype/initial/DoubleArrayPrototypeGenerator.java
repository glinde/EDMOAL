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

import data.algebra.Metric;
import data.algebra.VectorSpace;
import data.set.IndexedDataObject;
import datamining.clustering.protoype.Centroid;
import etc.DataGenerator;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class DoubleArrayPrototypeGenerator extends PrototypeGenerator<double[]> implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= 2089339786778802373L;
	
	
	protected DataGenerator dataGenerator; 
	
	/**
	 * @param seed
	 * @param vs
	 */
	public DoubleArrayPrototypeGenerator(long seed, VectorSpace<double[]> vs)
	{
		super(seed, vs);
		this.dataGenerator = new DataGenerator(seed);
	}

	/**
	 * @param vs
	 */
	public DoubleArrayPrototypeGenerator(VectorSpace<double[]> vs)
	{
		super(vs);
		this.dataGenerator = new DataGenerator();
	}

	/**
	 * Uniform distributed prototypes on the hyper rectangle defined by x and y
	 * 
	 * @param x
	 * @param y
	 * @param number
	 * @return
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
	 * Uniform distributed prototypes on the unit hyper cube [0, 1]^dim 
	 * 
	 * @param number
	 * @return
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
	 * Uniform distributed prototypes on the hyper rectangle defined by the data set 
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
				if(lowerCorner[k] > d.element[k]) lowerCorner[k] = d.element[k];
				if(upperCorner[k] < d.element[k]) upperCorner[k] = d.element[k];
			}
		}
	
		return this.randomUniformOnBounds(lowerCorner, upperCorner, number);
	}
	
	/**
	 * initializes the prototypes in equal intervals on a circle of the given radius
	 * on the hyper plane of the first two dimensions
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
	 * Samples the initial prototype positions from a spherical normal distribution with the specified parameters
	 * 
	 * @param expectation
	 * @param variance
	 * @param number
	 * @return
	 */
	public ArrayList<Centroid<double[]>> randomSphericalNormal(double[] expectation, double variance, int number)
	{
		ArrayList<Centroid<double[]>> prototypes = new ArrayList<Centroid<double[]>>(number);
		Collection<double[]> positions;
		
		if(expectation.length < this.vs.getDimension()) throw new IllegalArgumentException("The arrays x and y must be at least the size of the dimensionality of the vector space.");
		
		positions = this.dataGenerator.gaussPoints(expectation, variance, this.vs.getDimension(), number);
		
		for(double[] p: positions)	prototypes.add(new Centroid<double[]>(this.vs, p));
		
		return prototypes;
	}

	/**
	 * Samples the initial prototype positions from a spherical normal distribution with parameters estimated from a data set
	 * 
	 * @param expectation
	 * @param variance
	 * @param number
	 * @return
	 */
	public ArrayList<Centroid<double[]>> randomSphericalNormalLikeDataSet(Collection<IndexedDataObject<double[]>> data, Metric<double[]> dist, int number)
	{
		double[] exp = new double[this.vs.getDimension()];
		double variance = 0;
		double invDataCount = 1.0d/((double)data.size());
		
		for(IndexedDataObject<double[]> d:data)
		{
			this.vs.add(exp, d.element);
		}

		this.vs.mul(exp, invDataCount);
		
		for(IndexedDataObject<double[]> d:data)
		{
			variance += dist.distanceSq(exp, d.element);
		}
		
		variance /= ((double)(data.size()+1));
		
		return this.randomSphericalNormal(exp, variance, number);
	}
	
}
