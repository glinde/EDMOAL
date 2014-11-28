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
package generation.data.functions;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public abstract class AbstractFunction implements Function
{
	/**  */
	protected int attributesCount;
	
	/**  */
	protected double[] parameters;
	
	/**  */
	protected double[][] parameterBounds;
		
	public AbstractFunction(int attributesCount, int parameters)
	{
		this.attributesCount = attributesCount;
		this.parameters = new double[parameters];
		this.parameterBounds = new double[parameters][2];
	}
	
	/* (non-Javadoc)
	 * @see generation.data.functions.Function#getParameterCount()
	 */
	@Override
	public int getParameterCount()
	{
		return this.parameters.length;
	}

	/* (non-Javadoc)
	 * @see generation.data.functions.Function#setParameter(double[])
	 */
	@Override
	public void setParameter(double parameters, int parameterId)
	{
		this.parameters[parameterId] = parameters;
	}

	/* (non-Javadoc)
	 * @see generation.data.functions.Function#getParameters()
	 */
	@Override
	public double[] getParameters()
	{
		return this.parameters;
	}

	/* (non-Javadoc)
	 * @see generation.data.functions.Function#getParameters()
	 */
	@Override
	public double getParameter(int parameterId)
	{
		return this.parameters[parameterId];
	}
	
	/* (non-Javadoc)
	 * @see generation.data.functions.Function#setParameterBounds(double[], int)
	 */
	@Override
	public void setParameterBounds(double[] bounds, int parameterId)
	{
		this.parameterBounds[parameterId][0] = bounds[0];
		this.parameterBounds[parameterId][1] = bounds[1];
	}

	/* (non-Javadoc)
	 * @see generation.data.functions.Function#getParameterBounds()
	 */
	@Override
	public double[][] getParameterBounds()
	{
		return this.parameterBounds;
	}

	/* (non-Javadoc)
	 * @see generation.data.functions.Function#getParameterBounds(int)
	 */
	public double[] getParameterBounds(int parameterId)
	{
		return this.parameterBounds[parameterId];
	}
	
	/* (non-Javadoc)
	 * @see generation.data.functions.Function#getAttributesCount()
	 */
	@Override
	public int getAttributesCount()
	{
		return this.attributesCount;
	}
	
	public String toString()
	{
		return this.getName();
	}
}
