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


package data.objects.doubleArray;

import java.io.Serializable;

import data.algebra.AbstractEuclideanVectorSpace;
import data.algebra.EuclideanVectorSpace;

/**
 * To avoid too much method-ping-pong, all functions are called directly rather than calling
 * the methods of the abstract superclass.
 * 
 * TODO Class Description
 *
 * @author Roland Winkler
 *
 */
public class DAEuclideanVectorSpace extends AbstractEuclideanVectorSpace<double[]> implements EuclideanVectorSpace<double[]>, Serializable
{
	/**  */
	private static final long	serialVersionUID	= 3177664211525072147L;
	private DAStandardVectorSpace vectorSpace;
	private DAEuclideanDistance distance;
	private DAEuclideanNorm norm;
	private DAEuclideanScalarProduct scalarProduct;
	
	/**
	 * @param dim
	 */
	public DAEuclideanVectorSpace(int dim)
	{
		this.vectorSpace = new DAStandardVectorSpace(dim);
		this.distance = new DAEuclideanDistance();
		this.norm = new DAEuclideanNorm();
		this.scalarProduct = new DAEuclideanScalarProduct();
	}

	/**
	 * @param x
	 * @param y
	 * @return
	 * @see data.objects.doubleArray.DAEuclideanDistance#distance(double[], double[])
	 */
	public double distance(double[] x, double[] y)
	{
		return distance.distance(x, y, this.vectorSpace.dim);
	}

	/**
	 * @param x
	 * @param y
	 * @return
	 * @see data.objects.doubleArray.DAEuclideanDistance#distanceSq(double[], double[])
	 */
	public double distanceSq(double[] x, double[] y)
	{
		return distance.distanceSq(x, y, this.vectorSpace.dim);
	}

	/**
	 * @param x
	 * @return
	 * @see data.objects.doubleArray.DAEuclideanNorm#length(double[])
	 */
	public double length(double[] x)
	{
		return norm.length(x, this.vectorSpace.dim);
	}

	/**
	 * @param x
	 * @return
	 * @see data.objects.doubleArray.DAEuclideanNorm#lengthSq(double[])
	 */
	public double lengthSq(double[] x)
	{
		return norm.lengthSq(x, this.vectorSpace.dim);
	}

	/**
	 * @param x
	 * @param y
	 * @return
	 * @see data.objects.doubleArray.DAEuclideanScalarProduct#scalarProduct(double[], double[])
	 */
	public double scalarProduct(double[] x, double[] y)
	{
		return scalarProduct.scalarProduct(x, y, this.vectorSpace.dim);
	}

	/**
	 * @param x
	 * @param y
	 * @see data.objects.doubleArray.DAStandardVectorSpace#add(double[], double[])
	 */
	public void add(double[] x, double[] y)
	{
		vectorSpace.add(x, y);
	}

	/**
	 * @param x
	 * @param y
	 * @return
	 * @see data.objects.doubleArray.DAStandardVectorSpace#addNew(double[], double[])
	 */
	public double[] addNew(double[] x, double[] y)
	{
		return vectorSpace.addNew(x, y);
	}

	/**
	 * @return
	 * @see data.objects.doubleArray.DAStandardVectorSpace#newAddNeutralElement()
	 */
	public double[] getNewAddNeutralElement()
	{
		return vectorSpace.getNewAddNeutralElement();
	}	

	/**
	 * @param x
	 * @see data.objects.doubleArray.DAStandardVectorSpace#resetToAddNeutralElement(double[])
	 */
	public void resetToAddNeutralElement(double[] x)
	{
		vectorSpace.resetToAddNeutralElement(x);
	}

	/**
	 * @return
	 * @see data.objects.doubleArray.DAStandardVectorSpace#getDimension()
	 */
	public int getDimension()
	{
		return vectorSpace.getDimension();
	}

	/**
	 * @param x
	 * @see data.objects.doubleArray.DAStandardVectorSpace#inv(double[])
	 */
	public void inv(double[] x)
	{
		vectorSpace.inv(x);
	}

	/**
	 * @param x
	 * @return
	 * @see data.objects.doubleArray.DAStandardVectorSpace#invNew(double[])
	 */
	public double[] invNew(double[] x)
	{
		return vectorSpace.invNew(x);
	}

	

	/**
	 * @param x
	 * @param a
	 * @see data.objects.doubleArray.DAStandardVectorSpace#mul(double[], double)
	 */
	public void mul(double[] x, double a)
	{
		vectorSpace.mul(x, a);
	}

	/**
	 * @param x
	 * @param a
	 * @return
	 * @see data.objects.doubleArray.DAStandardVectorSpace#mulNew(double[], double)
	 */
	public double[] mulNew(double[] x, double a)
	{
		return vectorSpace.mulNew(x, a);
	}

	/**
	 * @param x
	 * @param y
	 * @see data.objects.doubleArray.DAStandardVectorSpace#sub(double[], double[])
	 */
	public void sub(double[] x, double[] y)
	{
		vectorSpace.sub(x, y);
	}

	/**
	 * @param x
	 * @param y
	 * @return
	 * @see data.objects.doubleArray.DAStandardVectorSpace#subNew(double[], double[])
	 */
	public double[] subNew(double[] x, double[] y)
	{
		return vectorSpace.subNew(x, y);
	}

	/**
	 * @param x
	 * @param y
	 * @see data.objects.doubleArray.DAStandardVectorSpace#copy(double[], double[])
	 */
	public void copy(double[] x, double[] y)
	{
		vectorSpace.copy(x, y);
	}

	/**
	 * @param x
	 * @return
	 * @see data.objects.doubleArray.DAStandardVectorSpace#copyNew(double[])
	 */
	public double[] copyNew(double[] x)
	{
		return vectorSpace.copyNew(x);
	}
	
}
