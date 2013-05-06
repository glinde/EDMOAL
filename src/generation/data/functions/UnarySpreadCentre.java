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
public class UnarySpreadCentre extends AbstractFunction
{
	public UnarySpreadCentre()
	{
		super(1, 2);
		
		this.parameterBounds[0][0] = 0.0d;
		this.parameterBounds[0][1] = 1.0d;

		this.parameterBounds[1][0] = 0.0d;
		this.parameterBounds[0][1] = 1.0d;
	}

	/* (non-Javadoc)
	 * @see generation.data.functions.Function#apply(double[])
	 */
	@Override
	public double apply(double... x)
	{
		return 1.0d/(1.0d+Math.exp(-((x[0]-this.parameters[0])/(this.parameters[1]*1.0))));
	}

	public UnarySpreadCentre newInstance(double... parameters)
	{
		return new UnarySpreadCentre();
	}
	
	public String getName() {return "Funtion Spread Centre";}
}
