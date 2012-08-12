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

import data.algebra.Norm;
import etc.MyMath;


/**
 * The standard Lp norm for double arrays. This class implements the standard norm for double arrays as they
 * occur for n-dimensional real vector spaces with orthogonal dimensions.   
 *
 * @author Roland Winkler
 */
public class DALpNorm implements Norm<double[]>, Serializable
{
	protected final double p;	
	protected final int intP;

	/**
	 * Creates a new Lp norm object for <code>p</code> 
	 * 
	 * @param p the parameter in the Lp norm.
	 */
	public DALpNorm(double p)
	{
		if(p < 1.0d) throw new IllegalArgumentException("p must be larger or equal to 1, otherwise this would not be a Norm."); 

		this.p = p;
		
		if(p == Math.floor(p))
		{
			this.intP = (int)p;
		}
		else
		{
			this.intP = -1;
		}
	}
	
	/* (non-Javadoc)
	 * @see data.algebra.Norm#length(data.objects.DataObject)
	 */
	@Override
	public double length(double[] x)
	{
		return MyMath.pow(this.lengthPowP(x), 1.0d/this.p);
	}

	/* (non-Javadoc)
	 * @see data.algebra.Norm#lengthSq(data.objects.DataObject)
	 */
	@Override
	public double lengthSq(double[] x)
	{
		return MyMath.pow(this.lengthPowP(x), 2.0d/this.p);
	}

	/**
	 * Calculates the length^p of the object <code>x</code> if p is a double value.
	 * 
	 * @param x The object that should be measured.
	 * @return The length to the power of p of the object <code>x</code>.
	 */
	protected double lengthPowP_double(double[] x)
	{
		double length = 0.0d;
		
		for(int i=0; i<x.length; i++)
		{
			length += MyMath.pow(Math.abs(x[i]), this.p);
		}
		
		return length;
	}

	/**
	 * Calculates the length^p of the object <code>x</code> if p is an integer value. This is much faster than {@link DALpNorm#length(double[])}.
	 * 
	 * @param x The object that should be measured.
	 * @return The length to the power of p of the object <code>x</code>.
	 */
	protected double lengthPowP_int(double[] x)
	{
		double length = 0.0d;
		
		for(int i=0; i<x.length; i++)
		{
			length += MyMath.pow(Math.abs(x[i]), this.intP);
		}
		
		return length;
	}
	
	/**
	 * Calculates the length^p of the object <code>x</code>.
	 * 
	 * @param x The object that should be measured.
	 * @return The length to the power of p of the object <code>x</code>.
	 */
	protected double lengthPowP(double[] x)
	{
		return (this.intP > 0)? this.lengthPowP_int(x) : this.lengthPowP_double(x); 
	}

	/**
	 * Calculates the Lp length for double arrays, taking only the first <code>dim</code> dimensions into account.  
	 * 
	 * @param x The coordinates of position x
	 * @param dim The number of dimensions that should be used for calculation
	 * @return The length for <code>x</code>, calculated in the first <code>dim</code> dimensions.
	 */
	public double length(double[] x, int dim)
	{
		return MyMath.pow(this.lengthPowP(x, dim), 1.0d/this.p);
	}


	/**
	 * Calculates the squared Lp length for double arrays, taking only the first <code>dim</code> dimensions into account.  
	 * 
	 * @param x The coordinates of position x
	 * @param dim The number of dimensions that should be used for calculation
	 * @return The length for <code>x</code>, calculated in the first <code>dim</code> dimensions.
	 */
	public double lengthSq(double[] x, int dim)
	{
		return MyMath.pow(this.lengthPowP(x, dim), 2.0d/this.p);
	}


	/**
	 * Calculates the length^p of the object <code>x</code> if p is a double value, taking only the first <code>dim</code> dimensions into account.
	 * 
	 * @param x The object that should be measured.
	 * @param dim The number of dimensions that should be used for calculation
	 * @return The length to the power of p of the object <code>x</code>.
	 */
	protected double lengthPowP_double(double[] x, int dim)
	{
		double length = 0.0d;

		if(x.length < dim) throw new IllegalArgumentException("The number of elements in x must be at least dim.");
		
		for(int i=0; i<dim; i++)
		{
			length += MyMath.pow(Math.abs(x[i]), this.p);
		}
		
		return length;
	}

	/**
	 * Calculates the length^p of the object <code>x</code> if p is an integer value, taking only the first <code>dim</code> dimensions into account.
	 * This is much faster than {@link DALpNorm#length(double[], int)}.
	 * 
	 * @param x The object that should be measured.
	 * @param dim The number of dimensions that should be used for calculation
	 * @return The length to the power of p of the object <code>x</code>.
	 */
	protected double lengthPowP_int(double[] x, int dim)
	{
		double length = 0.0d;

		if(x.length < dim) throw new IllegalArgumentException("The number of elements in x must be at least dim.");
		
		for(int i=0; i<dim; i++)
		{
			length += MyMath.pow(Math.abs(x[i]), this.intP);
		}
		
		return length;
	}
	
	/**
	 * Calculates the length^p of the object <code>x</code>, taking only the first <code>dim</code> dimensions into account.
	 * 
	 * @param x The object that should be measured.
	 * @param dim The number of dimensions that should be used for calculation
	 * @return The length to the power of p of the object <code>x</code>.
	 */
	protected double lengthPowP(double[] x, int dim)
	{
		if(x.length < dim) throw new IllegalArgumentException("The number of elements in x must be at least dim.");
		
		return (this.intP > 0)? this.lengthPowP_int(x, dim) : this.lengthPowP_double(x, dim); 
	}
}
