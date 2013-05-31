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


package datamining.clustering.protoype;

import data.algebra.Metric;
import data.algebra.VectorSpace;
import etc.MyMath;

/**
 * A prototype that represents not only a cluster, but also a spherical normal distribution.
 * Therefore, it inherits more information than its super class {@link Centroid}.
 *
 * @author Roland Winkler
 */
public class SphericalNormalDistributionPrototype extends Centroid<double[]>
{
	/**  */
	private static final long	serialVersionUID	= -508521471124914355L;

	/** For a distribution it is not enough to have a vector space, it is also necessary to calculate the
	 * distance of a data object to the mean of the distribution. Therefor, a metric has to be specified.
	 */
	protected Metric<double[]> dist;
	
	/** The variance of the spherical distribution. */
	protected double variance;
	
	/** The variance, this prototype is initialised with. */
	protected double initialVariance;
	
	/** Creates a new normal distribution prototype with the specified vector space and metric. 
	 * 
	 * @param vs The vector space.
	 * @param dist The metric.
	 */
	public SphericalNormalDistributionPrototype(VectorSpace<double[]> vs, Metric<double[]> dist)
	{
		super(vs);
		
		this.variance = 1.0d;
		this.initialVariance = 1.0d;
		this.dist = dist;
	}

	/** Creates a new normal distribution prototype with the specified vector space, metric and initial conditions. 
	 * 
	 * @param vs The vector space.
	 * @param dist The metric.
	 * @param initialPos The initial mean.
	 * @param initialVar The initial variance.
	 */
	public SphericalNormalDistributionPrototype(VectorSpace<double[]> vs, Metric<double[]> dist, double[] initialPos, double initialVar)
	{
		super(vs, initialPos);

		this.variance = initialVar;
		this.initialVariance = initialVar;
		this.dist = dist;
	}
	
	/**
	 * The copy constructor.
	 * 
	 * @param nd The normal distribution prototype that is copied.
	 */
	public SphericalNormalDistributionPrototype(SphericalNormalDistributionPrototype nd)
	{
		super(nd);
		
		this.variance = nd.variance;
		this.initialVariance = nd.variance;
		this.dist = nd.dist;
	}
	
	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.Centroid#resetToInitialPosition()
	 */
	@Override
	public void resetToInitialPosition()
	{
		super.resetToInitialPosition();
		this.variance = this.initialVariance;
	}
	
	/**
	 * Initialises the prototype with the specified position (= mean) and variance.
	 * 
	 * @param pos The initail mean of the normal distribution prototype.
	 * @param variance initail variance of the normal distribution prototype.
	 */
	public void initializeWithPosition(double[] pos, double variance)
	{
		super.initializeWithPosition(pos);
		
		this.variance = variance;
		this.initialVariance = variance;
	}
	

	/**
	 * Returns the density of the normal distribution at point <code>x</code>
	 * 
	 * @param x The location for which the density should be calculated.
	 * @return The density at position <code>x</code>.
	 */
	public double density(double[] x)
	{
		double tmp=0.0d;
		
		tmp = this.dist.distanceSq(x, this.position);
		tmp /= this.variance;
		tmp *= -0.5d;
		tmp = Math.exp(tmp);
		tmp *= MyMath.pow(2.0d*Math.PI*this.variance, -0.5d*((double)this.position.length));
		
		return tmp;
	}


	/**
	 * Returns the variance.
	 * 
	 * @return The variance.
	 */
	public double getVariance()
	{
		return this.variance;
	}


	/**
	 * Sets the variance.
	 * 
	 * @param variance The variance to set.
	 */
	public void setVariance(double variance)
	{
		this.variance = variance;
	}


	/**
	 * Returns the initial variance.
	 * 
	 * @return The initial variance.
	 */
	public double getInitialVariance()
	{
		return this.initialVariance;
	}
	
	
}