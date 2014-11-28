/**
Copyright (c) 2012, The EDMOAL Project

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


package data.objects.doubleArray;

import java.io.Serializable;

import data.algebra.Metric;
import etc.MyMath;


/**
 * The standard Lp distance for double arrays. This class implements the standard distance calculations for double arrays as they
 * occur for n-dimensional real vector spaces with orthogonal dimensions.   
 *
 * @author Roland Winkler
 */
public class DALpMetric implements Metric<double[]>, Serializable
{
	/**  */
	private static final long	serialVersionUID	= 8929609989993670728L;

	
	protected double p;
	
	
	public DALpMetric(double p)
	{
		this.p = p;
	}

	/* (non-Javadoc)
	 * @see data.algebra.Metric#distance(java.lang.Object, java.lang.Object)
	 */
	@Override
	public double distance(double[] x, double[] y)
	{
		return MyMath.pow(this.distanceP(x, y), 1.0d/this.p);
	}

	/* (non-Javadoc)
	 * @see data.algebra.Metric#distanceSq(java.lang.Object, java.lang.Object)
	 */
	@Override
	public double distanceSq(double[] x, double[] y)
	{
		double dist = this.distance(x, y);		
		return dist*dist;
	}
	
	
	/**
	 * Calculates the Lp to the power of p distance between two double arrays.  
	 * 
	 * @param x The coordinates of position x
	 * @param y The coordinates of position y
	 * @return The Lp to the power of p distance between <code>x</code> and <code>y</code>
	 */
	public double distanceP(double[] x, double[] y)
	{
		double dist = 0.0d;
		
		for(int i=0; i<x.length && i<y.length; i++)
		{
			dist += MyMath.pow(Math.abs(x[i]-y[i]), p);
		}
		
		return dist;
	}	
	
	/**
	 * Calculates the Lp distance between two double arrays, taking only the first <code>dim</code> dimensions into account.  
	 * 
	 * @param x The coordinates of position x
	 * @param y The coordinates of position y
	 * @param dim The number of dimensions that should be used for calculation
	 * @return The distance between <code>x</code> and <code>y</code>, calculated in the first <code>dim</code> dimensions.
	 */
	public double distance(double[] x, double[] y, int dim)
	{
		return MyMath.pow(this.distanceP(x, y, dim), 1.0d/this.p);
	}

	/**
	 * Calculates the squared Lp distance between two double arrays, taking only the first <code>dim</code> dimensions into account.  
	 * 
	 * @param x The coordinates of position x
	 * @param y The coordinates of position y
	 * @param dim The number of dimensions that should be used for calculation
	 * @return The distance between <code>x</code> and <code>y</code>, calculated in the first <code>dim</code> dimensions.
	 */
	public double distanceSq(double[] x, double[] y, int dim)
	{
		double dist = this.distance(x, y, dim);		
		return dist*dist;
	}
		
	/**
	 * Calculates the Lp to the power of p distance between two double arrays.  
	 * 
	 * @param x The coordinates of position x
	 * @param y The coordinates of position y
	 * @return The Lp to the power of p distance between <code>x</code> and <code>y</code>
	 */
	public double distanceP(double[] x, double[] y, int dim)
	{
		double dist = 0.0d;
		
		if(x.length < dim || y.length < dim) throw new IllegalArgumentException("The number of elements in x and y must be at least dim.");
		
		
		for(int i=0; i<dim; i++)
		{
			dist += MyMath.pow(Math.abs(x[i]-y[i]), p);
		}
		
		return dist;
	}	
	
}
