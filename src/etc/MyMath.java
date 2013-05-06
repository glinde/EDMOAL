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


package etc;

import java.io.Serializable;

/**
 * A collection of mathematical methods and constants, that are not included in {@link java.lang.Math}.
 * 
 * @author Roland Winkler
 */
public class MyMath implements Serializable
{	
	/**  */
	private static final long	serialVersionUID	= -4075285600273842347L;

	/** A multiplication constant for transforming radiant values into arc values. */
	public static double rad2arc = 360.0d/(2.0d*Math.PI);

	/** A multiplication constant for transforming arc values into radiant values. */
	public static double arc2rad = (2.0d*Math.PI)/360.0d;

	/** A multiplication constant for transforming arc second values into radiant values. */
	public static double rad2arcSec = (360.0d*60.0d*60.0d)/(2.0d*Math.PI);
	
	/** A multiplication constant for transforming radiant values into arc second values. */
	public static double arcSec2rad = (2.0d*Math.PI)/(360.0d*60.0d*60.0d);

		
	/**
	 * The power function <code>a<sup>n</sup></code> as implemented in {@link java.lang.Math}, is very time
	 * consuming in the general case. If it is likely, that <code>n</code> is small, this function
	 * provides some shortcut calculation. In this case, if <code>n</code> is smaller or equal to 20, simple
	 * multiplication operations are used instead of the more expensive function in {@link java.lang.Math}.
	 * However, if <code>n</code> is larger than 20, {@link java.lang.Math} is used.
	 * 
     * @param   a   the base.
     * @param   n   the exponent.
     * @return  the value <code>a<sup>n</sup></code>.
     * 
     * @see java.lang.Math#pow(double, double)
	 */	
	public static double pow(double a, int n)
	{
		if(n<0)
		{
			a = 1.0d/a;
			n = -n;
		}
		double res = 1.0d;
		
		while(n>0)
		{
			if(n%2 == 1) res *= a;
			
			a *= a;
			n >>= 1;
		}
		
		return res;
	}

	/**
	 * The power function <code>a<sup>n</sup></code> as implemented in {@link java.lang.Math}, is very time
	 * consuming in the general case. If it is likely, that <code>n</code> is small or can easily be expressed
	 * using multiplication and the {@link java.lang.Math#sqrt(double)} function, this function can provide a
	 * considerable advantage.
	 * However, if <code>n</code> does is not easily expressible by the above mentioned operations, the {@link java.lang.Math}
	 * version is used.
	 * 
     * @param   a   the base.
     * @param   n   the exponent.
     * @return  the value of <code>a<sup>n</sup></code>.
     * 
     * @see java.lang.Math#sqrt(double)
     * @see java.lang.Math#pow(double, double)
	 */
	public static double pow(double a, double n)
	{	
		if(n < 0.0d) return 1.0d/MyMath.pow(a, -n);
		if(Math.floor(n) == n) return MyMath.pow(a, (int)n); // optimization for double values that are equal to integers.
		
		double b=0.0d;
		int m = (int)(8.0d*n);
				
		if(n<6.0d && 8.0d*n == (double)m)
		{
			switch(m)
			{
				case 1:  return Math.sqrt(Math.sqrt(Math.sqrt(a)));
				case 2:  return Math.sqrt(Math.sqrt(a));
				case 4:  return Math.sqrt(a);
				case 10: return a*Math.sqrt(Math.sqrt(a));
				case 12: return a*Math.sqrt(a);
				case 20: return a*a*Math.sqrt(a);
				case 28: return a*a*a*Math.sqrt(a);	
				case 36: {b=a*a; return b*b*Math.sqrt(a);}	
				case 44: {b=a*a; return b*b*a*Math.sqrt(a);}
			}
		}
		
		return Math.pow(a, n);
	}
	
	
	public static double min(double[] list)
	{
		double min = list[0];
		for(int i=1; i<list.length; i++)
		{
			min = (min>list[i])? list[i]:min;
		}
		return min;
	}
	
	public static double max(double[] list)
	{
		double max = list[0];
		for(int i=1; i<list.length; i++)
		{
			max = (max<list[i])? list[i]:max;
		}
		return max;
	}
}
