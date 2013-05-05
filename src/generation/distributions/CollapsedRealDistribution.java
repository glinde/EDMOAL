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
package generation.distributions;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.random.Well19937c;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class CollapsedRealDistribution extends AbstractRealDistribution
{
	/**  */
	private static final long	serialVersionUID	= 6375350100561109042L;
	protected double collapsedValue;
	
	/**
	 * @param collapsedValue
	 */
	public CollapsedRealDistribution(double collapsedValue)
	{
		super(new  Well19937c());
		this.collapsedValue = collapsedValue;
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.math3.distribution.RealDistribution#density(double)
	 */
	@Override
	public double density(double x)
	{
		return this.collapsedValue==x?1.0d:0.0d;
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.math3.distribution.RealDistribution#cumulativeProbability(double)
	 */
	@Override
	public double cumulativeProbability(double x)
	{
		return x<this.collapsedValue?0.0d:1.0d;
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.math3.distribution.RealDistribution#getNumericalMean()
	 */
	@Override
	public double getNumericalMean()
	{
		return this.collapsedValue;
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.math3.distribution.RealDistribution#getNumericalVariance()
	 */
	@Override
	public double getNumericalVariance()
	{
		return 0.0d;
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.math3.distribution.RealDistribution#getSupportLowerBound()
	 */
	@Override
	public double getSupportLowerBound()
	{
		return this.collapsedValue;
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.math3.distribution.RealDistribution#getSupportUpperBound()
	 */
	@Override
	public double getSupportUpperBound()
	{
		return this.collapsedValue;
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.math3.distribution.RealDistribution#isSupportLowerBoundInclusive()
	 */
	@Override
	public boolean isSupportLowerBoundInclusive()
	{
		return true;
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.math3.distribution.RealDistribution#isSupportUpperBoundInclusive()
	 */
	@Override
	public boolean isSupportUpperBoundInclusive()
	{
		return true;
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.math3.distribution.RealDistribution#isSupportConnected()
	 */
	@Override
	public boolean isSupportConnected()
	{
		return true;
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.math3.distribution.AbstractRealDistribution#probability(double, double)
	 */
	@Override
	public double probability(double x0, double x1)
	{
		// TODO Auto-generated method stub
		return super.probability(x0, x1);
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.math3.distribution.AbstractRealDistribution#sample()
	 */
	@Override
	public double sample()
	{
		return this.collapsedValue;
	}


	/* (non-Javadoc)
	 * @see org.apache.commons.math3.distribution.AbstractRealDistribution#probability(double)
	 */
	@Override
	public double probability(double x)
	{
		return this.collapsedValue==x?1.0d:0.0d;
	}
}
