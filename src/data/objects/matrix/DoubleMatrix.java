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
package data.objects.matrix;

import java.io.Serializable;

import etc.DataManipulator;

/**
 * Dummy class for a 2-Dimensional array of double elements.
 * At moment it does not provide useful functionality, but that might change as it is needed.
 *
 * @author Roland Winkler
 */
public class DoubleMatrix implements Serializable
{
	protected double[][] elements;
	
	public DoubleMatrix(int sizeX, int sizeY)
	{
		this.elements = new double[sizeX][sizeY];
	}
	
	public void set(int i, int j, double x)
	{
		this.elements[i][j] = x;
	}
	
	public double get(int i, int j)
	{
		return this.elements[i][j];
	}
	

	public int sizeX()
	{
		return this.elements.length;
	}

	public int sizeY()
	{
		return this.elements[0].length;
	}
	

	public void gaussFilter(int radius)
	{
		double[] line = new double[this.elements[0].length];
		
		for(int i=0; i<this.elements.length; i++)
		{
			DataManipulator.gaussFilter(this.elements[i], radius);
		}
		
		for(int i=0; i<this.elements.length; i++)
		{
			for(int j=0; j<this.elements[i].length; j++)
			{
				line[j] = this.elements[i][j];
			}
			DataManipulator.gaussFilter(line, radius);
			for(int j=0; j<this.elements[i].length; j++)
			{
				this.elements[i][j] = line[j];
			}
		}
	}
}
