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
package generation.data;

import java.util.Arrays;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class HyperrectangleUniformGenerator extends DADataGenerator
{
	/**
	 * @param dim
	 */
	public HyperrectangleUniformGenerator(double[] lower, double[] upper)
	{
		super();
		
		if(lower.length != upper.length) throw new IllegalArgumentException("The arrays upper and lower must have the same length: " + lower.length + " != " + upper.length);
		
		for(int k=0; k<lower.length; k++)
		{
			this.addDistribution(new UniformRealDistribution(lower[k], upper[k]));
		}
	}
	
	/**
	 * @param dim
	 */
	public HyperrectangleUniformGenerator(int dim)
	{
		this(new double[dim], HyperrectangleUniformGenerator.filledArray(dim, 1.0d));
	}
	
	public static double[] filledArray(int dim, double value)
	{
		double[] array = new double[dim];
		
		Arrays.fill(array, value);
		
		return array;
	}
}
