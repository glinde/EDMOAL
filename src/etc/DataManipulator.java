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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class DataManipulator implements Serializable
{	
	/**  */
	private static final long	serialVersionUID	= 8117435987949605896L;

	public static double gaussFunction(double x, double sigma)
	{
		return 1/(Math.sqrt(2*Math.PI)*sigma)*Math.exp(-0.5*x*x/(sigma*sigma));
	}
	
	public static double gaussFunctionNotNormal(double x, double sigma)
	{
		return Math.exp(-0.5*x*x/(sigma*sigma));
	}
	
	public static double[] gaussFilter(double[] list, int delta)
	{
		double[] filteredList = new double[list.length];
		double[] gaussFactors = new double[2*delta+1];
		int i, j, k;
		double facorSum = 0.0d;
		double a=0.0d;
		double lowEndDeviation=0.0d, highEndDeviaion=0.0d;
		
		if(delta == 0)
		{
			return list.clone();
		}
		
		lowEndDeviation = (list[delta] - list[0])/((double)delta);
		highEndDeviaion = (list[list.length-1] - list[list.length-1-delta])/((double)delta);
		
		for(i=-delta; i<=delta; i++)
		{
			gaussFactors[delta+i] = gaussFunctionNotNormal(i, ((double)delta)/3.0d);
			facorSum += gaussFactors[delta+i];
		}

		facorSum = 1.0d/facorSum;
		
		for(i=0; i<gaussFactors.length; i++) gaussFactors[i] *= facorSum;
		
		for(k=0; k<list.length; k++)
		{
			filteredList[k] = 0.0d;
			for(i=-delta; i<=delta; i++)
			{
				j=i+k;
				if(j<0)
				{
					a = list[0]+j*lowEndDeviation;
				}
				else if(j>=list.length)
				{
					a = list[list.length-1]+(j-(list.length-1))*highEndDeviaion;
				}
				else
				{
					a = list[j];
				}
				filteredList[k] += gaussFactors[delta+i]*a;
			}
		}
		
		return filteredList;
	}
	
	public static double[] firstListDerivative(double[] list)
	{
		double[] derivative = new double[list.length];
		int i;
		
		for(i=1; i<list.length-1; i++) derivative[i] = 0.5*(list[i+1] - list[i-1]);
		
		derivative[0] = derivative[1];
		derivative[derivative.length-1] = derivative[derivative.length-2];
		
		return derivative;
	}

	
	public static double[] calculateDistancePlot(double[] result, Collection<double[]> list, double[] x, boolean sort)
	{
		if(result == null || result.length < list.size()) result = new double[list.size()];
		int i=0;
		
		for(double[] p:list)
		{
			result[i] = MyMath.euclideanDist(p, x);
			i++;
		}
		
		if(sort) Arrays.sort(result);
		
		return result;
	}

	/** 
	 * The function determines weather the sequence of 3 points has a left turn (positive result), is a straight line (0) or a right turn (negative result).
	 * 
	 * @return
	 */
	public static double turn2D(double[] a, double[] b, double[] c)
	{
		return (b[0] - a[0])*(c[1] - a[1]) - (b[1] - a[1])*(c[0] - a[0]); 
	}
	
	/**  The monotone chain algorithm for getting the convex hull of a set of 2D points.
	 * 
	 * http://en.wikibooks.org/wiki/Algorithm_Implementation/Geometry/Convex_hull/Monotone_chain
	 * 
	 * @param points
	 * @return
	 */
	public static ArrayList<double[]> convexHull2D(Collection<double[]> points)
	{
		int i=0, lastLowerHullPoints=0;
		
		ArrayIndexListComparator comp = new ArrayIndexListComparator(new int[]{0, 1});
		ArrayList<double[]> convexHull = new ArrayList<double[]>();
		ArrayList<double[]> sortedPoints = new ArrayList<double[]>(points);
		
		Collections.sort(sortedPoints, comp);
				
		// lower hull
		for(i=0; i<sortedPoints.size(); i++)
		{
			while(convexHull.size()>1 && turn2D(convexHull.get(convexHull.size()-2), convexHull.get(convexHull.size()-1), sortedPoints.get(i)) <= 0)
				convexHull.remove(convexHull.size()-1);
			
			convexHull.add(sortedPoints.get(i));
		}
		
		lastLowerHullPoints = convexHull.size();
		
		// upper hull
		for(i=sortedPoints.size()-2; i>=0; i--)
		{
			while(convexHull.size()>lastLowerHullPoints && turn2D(convexHull.get(convexHull.size()-2), convexHull.get(convexHull.size()-1), sortedPoints.get(i)) <= 0)
				convexHull.remove(convexHull.size()-1);
			
			convexHull.add(sortedPoints.get(i));
		}
		
		return convexHull;
	}
	
	
	/**
	 * @param col
	 * @param number
	 * @return
	 */
	public static <T> ArrayList<T> selectWithoutCopy(Collection<T> col, int number)
	{
		ArrayList<T> list = new ArrayList<T>(col);
		Collections.shuffle(list);
		
		return new ArrayList<T>(list.subList(0, Math.min(number, list.size())));
	}
	
	/**
	 * @param col
	 * @param number
	 * @return
	 */
	public static <T> ArrayList<T> selectWithCopy(Collection<T> col, int number)
	{
		ArrayList<T> list = new ArrayList<T>(col);
		ArrayList<T> selection = new ArrayList<T>(number);
		Random rand = new Random();
		for(int i=0; i<number; i++)
		{
			selection.add(list.get(rand.nextInt(list.size())));
		}
		
		return selection;
	}
}
