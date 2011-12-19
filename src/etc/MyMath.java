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
import java.util.Vector;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class MyMath implements Serializable
{	
	
	/**  */
	private static final long	serialVersionUID	= -4075285600273842347L;
	public static double rad2arc = 360.0d/(2.0d*Math.PI);
	public static double arc2rad = (2.0d*Math.PI)/360.0d;
	public static double arcSec2rad = (2.0d*Math.PI)/(360.0d*60.0d*60.0d);
	public static double rad2arcSec = (360.0d*60.0d*60.0d)/(2.0d*Math.PI);

	public static double scalarProduct(double[] a, double[] b)
	{
		double s = 0.0d;
		
		for(int i=0; i<a.length; i++) s+=a[i]*b[i];
		
		return s;
	}
	
	/**
	 * @param p1
	 * @param p2
	 * @return
	 */
	public static double euclideanDist(double[] p1, double[] p2)
	{
		if(p1.length != p2.length) return -1.0d;
		
		return Math.sqrt(MyMath.euclideanDistSquare(p1, p2));
	}

	/**
	 * @param p1
	 * @param p2
	 * @return
	 */
	public static double euclideanDist(double[] p1, double[] p2, int dimCount)
	{
		if(p1.length < dimCount || p2.length < dimCount) return -1.0d;
		
		return Math.sqrt(MyMath.euclideanDistSquare(p1, p2, dimCount));
	}
	/**
	 * @param p1
	 * @param p2
	 * @return
	 */
	public static double euclideanDistSquare(double[] p1, double[] p2)
	{
		double dist=0.0d;
		
		if(p1.length != p2.length) return -1.0d;
		
		for(int i=0; i<p1.length; i++)
		{
			dist += (p1[i]-p2[i])*(p1[i]-p2[i]);
		}
		
		return dist;
	}

	/**
	 * @param p1
	 * @param p2
	 * @return
	 */
	public static double euclideanDistSquare(double[] p1, double[] p2, int dimCount)
	{
		double dist=0.0d;
		
		if(p1.length < dimCount || p2.length < dimCount) return -1.0d;
		
		for(int i=0; i<dimCount; i++)
		{
			dist += (p1[i]-p2[i])*(p1[i]-p2[i]);
		}
		
		return dist;
	}
	
	/**
	 * @param p
	 * @return
	 */
	public static double euclideanLengthSquare(double[] p)
	{
		double length=0.0d;
		
		for(int i=0; i<p.length; i++)
		{
			length += (p[i])*(p[i]);
		}
		
		return length;
	}	

	/**
	 * @param p
	 * @return
	 */
	public static double euclideanLengthSquare(double[] p, int dimCount)
	{
		double length=0.0d;

		if(p.length < dimCount) return -1;		
		
		for(int i=0; i<dimCount; i++)
		{
			length += (p[i])*(p[i]);
		}
		
		return length;
	}	

	/**
	 * @param p
	 * @return
	 */
	public static double euclideanLength(double[] p)
	{
		double length=0.0d;
		
		for(int i=0; i<p.length; i++)
		{
			length += (p[i])*(p[i]);
		}
		
		return Math.sqrt(length);
	}	

	/**
	 * @param p
	 * @return
	 */
	public static double euclideanLength(double[] p, int dimCount)
	{
		double length=0.0d;
		
		if(p.length < dimCount) return -1;
		
		for(int i=0; i<dimCount; i++)
		{
			length += (p[i])*(p[i]);
		}
		
		return Math.sqrt(length);
	}	
	

	public static double similarityDistance(double[] x, double[] y)
	{
		double dist=0;
		
		for(int k=0; k<x.length; k++)
		{
			dist += Math.exp(-Math.abs(x[k] - y[k]));
		}
		
		return dist;
	}
	

	public static double dissimilarityDistance(double[] x, double[] y)
	{
		return -Math.log(similarityDistance(x, y)/x.length);
	}
	
	/**
	 * @param x
	 * @return
	 */
	public static int round(double x)
	{
		int n = (int)x;
		if(x > 0.0d && x - n >= 0.5d) return n+1;
		if(x < 0.0d && x + n <= -0.5d) return n-1;
		return n;
	}
	
	
	/** circle center is (0,0) and coordinates of a and b are relative to circlecenter.
	 * 
	 * calculates the t in the equasion: a + t*(b - a) = d
	 * 
	 * @param a point inside of circle
	 * @param b point outside of circle
	 * @param d radius of circle
	 * @return t, a value between 0.0d and 1.0d
	 */
	public static double vertexCircleLine(double[] a, double[] b, double d)
	{
		double alpha, betta, gamma;
		
		if(a[0] == b[0] && a[1] == b[1]) return 0.0d;
		
		alpha = 1.0d / ((b[0]-a[0])*(b[0]-a[0]) + (b[1]-a[1])*(b[1]-a[1]));
		betta = 2.0d*a[0]*(b[0]-a[0]) + 2.0d*a[1]*(b[1]-a[1]);
		gamma = a[0]*a[0] + a[1]*a[1] - d*d;

		return -0.5d*betta*alpha + Math.sqrt(0.25d*betta*betta*alpha*alpha - gamma*alpha);
	}
	
	/** calulate the arc in mathematical positive (counterclockwise) direction with max = 2*Pi
	 * @param a
	 * @param b
	 * @return
	 */
	public static double calcMathArc(double[] a, double[] b)
	{
		return (b[1] >= a[1])? Math.acos((b[0] - a[0])/Math.sqrt((a[0]-b[0])*(a[0]-b[0]) + (a[1]-b[1])*(a[1]-b[1]))) : 2.0d*Math.PI - Math.acos((b[0] - a[0])/Math.sqrt((a[0]-b[0])*(a[0]-b[0]) + (a[1]-b[1])*(a[1]-b[1])));
	}

	
	/** calulate the arc in mathematical positive (counterclockwise) direction with max = 2*Pi
	 * @param arc
	 * @return
	 */
	public static double mathToAvionicArc(double arc)
	{
		return MyMath.rad2arc * ((arc + 1.5d*Math.PI)%(2.0d*Math.PI));
	}
	

	/** decides if the given arc is in the defined borders
	 * @param arc
	 * @param centerArc
	 * @param deltaArc
	 * @return
	 */
	public static boolean isInArcRange(double arc, double centerArc, double deltaArc)
	{
		if(Math.abs(centerArc - arc) < deltaArc) return true;
		if(centerArc < deltaArc && (2.0d*Math.PI - arc) < deltaArc - centerArc) return true;
		if(centerArc + deltaArc > 2.0d*Math.PI && arc < centerArc + deltaArc - 2.0d*Math.PI) return true;
		
		return false;
	}

	
	/** caclulates the arc at beta of the defined triangle
	 * @param A
	 * @param B
	 * @param C
	 * @return
	 */
	public static double lawOfCosinus(double[] A, double[] B, double[] C)
	{
		double	a2 = MyMath.euclideanDistSquare(B, C, 2),
				b2 = MyMath.euclideanDistSquare(A, C, 2),
				c2 = MyMath.euclideanDistSquare(A, B, 2),
				x;
				
		x = (a2 + c2 - b2)/(2.0d*Math.sqrt(a2*c2));
		
		x = (x < -1.0d)? -1.0d:x;
		x = (x > 1.0d)? 1.0d:x;
		
	//	if(a2 == c2 && a2 == 4.0d*b2)
	//	if((a2 + c2 - b2)/(2.0d*Math.sqrt(a2*c2)) <= -1.0d || (a2 + c2 - b2)/(2.0d*Math.sqrt(a2*c2)) >= 1.0d) System.out.println("arc cos from: " + (a2 + c2 - b2)/(2.0d*Math.sqrt(a2*c2)));
		//System.out.println("a = " + Math.sqrt(a2) + " b = " + Math.sqrt(b2) + " c = " + Math.sqrt(c2) + "acos = " + Math.acos((a2 + c2 - b2)/(2.0d*Math.sqrt(a2*c2))));
		return Math.acos(x);
	}
	
	
	public static double pow(double a, int n)
	{	
		double b = 1.0d, c = 1.0d;
		
		if(n<0) return 1.0d/MyMath.pow(a, -n);
		
		if(n <= 20)
		{
			switch(n)
			{
				case 0: return 1.0d;
				case 1: return a;
				case 2: return a*a;
				case 3: return a*a*a;
				case 4: b=a*a; return b*b;
				case 5: b=a*a; return b*b*a;
				case 6: b=a*a; return b*b*b;
				case 7: b=a*a; return b*b*b*a;
				case 8: b=a*a; b=b*b; return b*b;
				case 9: b=a*a*a; return b*b*b;
				case 10: b=a*a; c=b*b; return c*c*b;
				case 11: b=a*a; c=b*b; return c*c*b*a;
				case 12: b=a*a; b=b*b*b; return b*b;
				case 13: b=a*a; b=b*b*b; return b*b*a;
				case 14: b=a*a; c=b*b*b; return c*c*b;
				case 15: b=a*a*a; c=b*b; return c*c*b;
				case 16: b=a*a; b=b*b; b=b*b; return b*b;
				case 17: b=a*a; b=b*b; b=b*b; return b*b*a;
				case 18: b=a*a*a; b=b*b; return b*b*b;
				case 19: b=a*a*a; b=b*b; return b*b*b*a;
				case 20: b=a*a; c=b*b; c=c*c*b; return c*c;
				default: ;
			}
		}
		
		return Math.pow(a, n);
	}
	
	public static double pow(double a, double n)
	{	
		double b=0.0d;
		int m = (int)(8.0d*n);
		if(n < 0.0d) return 1.0d/MyMath.pow(a, -n);
		
		if(n<6.0d && 8.0d*n == (double)m)
		{
			switch(m)
			{
				case 0:  return 1.0d; 
				case 1:  return Math.sqrt(Math.sqrt(Math.sqrt(a)));
				case 2:  return Math.sqrt(Math.sqrt(a));
				case 4:  return Math.sqrt(a);
				case 8:  return a;
				case 10: return a*Math.sqrt(Math.sqrt(a));
				case 12: return a*Math.sqrt(a);
				case 16: return a*a;
				case 20: return a*a*Math.sqrt(a);
				case 24: return a*a*a;
				case 28: return a*a*a*Math.sqrt(a);	
				case 32: {b=a*a; return b*b;}
				case 36: {b=a*a; return b*b*Math.sqrt(a);}	
				case 40: {b=a*a; return b*b*a;}
				case 44: {b=a*a; return b*b*a*Math.sqrt(a);}
				case 48: {b=a*a; return b*b*b;}		
			}
		}
		
		return Math.pow(a, n);
	}

	/**
	 *  returns the distance of x to the line from p1 to p2, or the distance to the nearest end point of the line if that is smaller
	 * 
	 * @param x
	 * @param p1
	 * @param p2
	 * @return
	 */
	public static double distanceToLine2D(double[] x, double[] p1, double[] p2)
	{
		double p12;
		double p1x;
		double p2x;
		double tmp=0;
				
		// outside of the central area in direction of p1
		tmp = (p2[0]-p1[0])*(x[0]-p1[0]) + (p2[1]-p1[1])*(x[1]-p1[1]);		
		if(tmp <= 0.0d) return Math.sqrt((p1[0]- x[0])*(p1[0]- x[0]) + (p1[1]- x[1])*(p1[1]- x[1]));

		// outside of the central area in direction of p2
		tmp = (p1[0]-p2[0])*(x[0]-p2[0]) + (p1[1]-p2[1])*(x[1]-p2[1]);		
		if(tmp <= 0.0d) return Math.sqrt((p2[0]- x[0])*(p2[0]- x[0]) + (p2[1]- x[1])*(p2[1]- x[1]));
		
		p12 = (p1[0]-p2[0])*(p1[0]-p2[0]) + (p1[1]-p2[1])*(p1[1]-p2[1]);
		p1x = (p1[0]- x[0])*(p1[0]- x[0]) + (p1[1]- x[1])*(p1[1]- x[1]);
		p2x = (p2[0]- x[0])*(p2[0]- x[0]) + (p2[1]- x[1])*(p2[1]- x[1]);
		
		// all arcs of the triangle are below 90°
		// simplest found way of calculating the hight of the triangle without using sin, cos or tan
		// formula in tex-formula format
		// a^2 = p1x, b^2 = p2x, c^2 = p12 
		// search for h_c, the hight of x above the connecting line between p1 and p2
		// 16 \cdot A^2 = (a^2 + b^2 + c^2)^2 - 2 \cdot (a^4 + b^4 + c^4)
		// A = 0.5 \cdot h_c \cdot c
		// from: http://de.wikipedia.org/wiki/Dreieck 27.11.2007 13:45
		tmp = p1x + p2x + p12;
		tmp *= tmp;
		tmp -= 2.0d*(p1x*p1x + p2x*p2x + p12*p12);
		tmp /= (4.0d*p12);
				
		return Math.sqrt(tmp);
	}

	/**
	 *  returns the distance of x to the route by checking the distance to each line
	 * 
	 * @param x
	 * @param route
	 * @return
	 */
	public static double distanceToRoute2D(double[] x, Vector<double[]> route)
	{
		double minimalDist = Double.MAX_VALUE;
		double tmp;
		int i;
		for(i=1; i<route.size(); i++)
		{
			tmp = distanceToLine2D(x, route.get(i), route.get(i-1));
			minimalDist = (tmp<minimalDist)?tmp:minimalDist;
		}
		
		return minimalDist;
	}
	
	/**
	 * @param x
	 * @param y
	 * @param alpha
	 * @return (1-alpha) * x + alpha * y
	 */
	public static double[] alphaCombination(double[] x, double[] y, double alpha)
	{
		double[] z = new double[x.length];
		
		for(int i=0; i<x.length; i++)
		{
			z[i] = (1.0d-alpha)*x[i] + alpha * y[i];
		}
		
		return z;
	}
	
	public static void rotate2D(double[] x, double arc)
	{
		double a, b;
		
		a = x[0]*Math.cos(arc) - x[1]*Math.sin(arc);
		b = x[0]*Math.sin(arc) + x[1]*Math.cos(arc);
		
		x[0] = a; x[1] = b;
	}
}
