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
import java.util.Collection;
import java.util.Collections;
import java.util.Random;

/**
 * A class for manipulating data with quite general application.
 *
 * @author Roland Winkler
 */
public class DataManipulator implements Serializable
{	
	/**  */
	private static final long	serialVersionUID	= 8117435987949605896L;

	/**
	 * Returns the value of the normal function with <code>x</code> being the distance to
	 * the expectation value and <code>sigma</code> being the standard deviation.
	 * 
	 * @param x The distance to the expectation value of the normal distribution.
	 * @param sigma The standard deviation of the normal distribution.
	 * @return The value of the normal distribution
	 */
	public static double gaussFunction(double x, double sigma)
	{
		return 1/(Math.sqrt(2*Math.PI)*sigma)*Math.exp(-0.5*x*x/(sigma*sigma));
	}
	
	/**
	 * Returns the value of the unnormalised gauss function with <code>x</code> being the
	 * distance to the expectation value and <code>sigma</code> being the standard deviation.
	 * The normalisation to integral 1 is missing for this function because there are some
	 * application which do not require it.
	 * 
	 * @param x The distance to the expectation value of the normal distribution.
	 * @param sigma The standard deviation of the normal distribution.
	 * @return The value of the unnormalised gauss function.
	 */
	public static double gaussFunctionNotNormal(double x, double sigma)
	{
		return Math.exp(-0.5*x*x/(sigma*sigma));
	}
	
	/**
	 * Performs a gauss filter on the specified <code>list</code> of double values, with the specified
	 * range <code>delta</code>.
	 * 
	 * @param list The original values.
	 * @param delta The range of the gauss filter.
	 * @return The list of values of the gauss filter.
	 */
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
	
	/**
	 * Computes for a list of double values the local variation.
	 * For a point x[i], the derivation is computed by
	 * x'[i] = 0.5 * x[i+1] - x[i-1] with x'[0] = x'[1] and x'[length] = x'[length-1].
	 * 
	 * @param list The list of double values.
	 * @return The derived list of double values.
	 */
	public static double[] firstListDerivative(double[] list)
	{
		double[] derivative = new double[list.length];
		int i;
		
		for(i=1; i<list.length-1; i++) derivative[i] = 0.5*(list[i+1] - list[i-1]);
		
		derivative[0] = derivative[1];
		derivative[derivative.length-1] = derivative[derivative.length-2];
		
		return derivative;
	}


	/** 
	 * The function determines weather the sequence of 3 points a-b-c has a left turn (positive result),
	 * is a straight line (0) or a right turn (negative result).
	 * It is not quite a scalar product.
	 * 
	 * @return The direction value of a turn.
	 */
	public static double turn2D(double[] a, double[] b, double[] c)
	{
		return (b[0] - a[0])*(c[1] - a[1]) - (b[1] - a[1])*(c[0] - a[0]); 
	}
	
	/** 
	 * The monotone chain algorithm for getting the convex hull of a set of 2D points.
	 * 
	 * http://en.wikibooks.org/wiki/Algorithm_Implementation/Geometry/Convex_hull/Monotone_chain
	 * 
	 * @param points The list of points for calculating the convex hull.
	 * @return The convex hull in form of a list of points.
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
	 * Randomly selects a number of objects from a collection, defining a subset of the specified number of objects.
	 * If the specified <code>number</code> is larger than the size of the collection,
	 * all objects of the list are returned, but likely in a different order.
	 * 
	 * @param col The collection of original objects.
	 * @param number The number of objects that should be chosen
	 * @return A subset of the collection.
	 */
	public static <T> ArrayList<T> selectWithoutCopy(Collection<T> col, int number)
	{
		ArrayList<T> list = new ArrayList<T>(col);
		Collections.shuffle(list);
		
		return new ArrayList<T>(list.subList(0, Math.min(number, list.size())));
	}
	
	/**
	 * Randomly selects a <code>number</code> of objects out of a collection, but allowing
	 * that an object is selected multiple times. The <code>number</code> may be larger
	 * than the number of objects in the collections.
	 * 
	 * @param col The collection of objects.
	 * @param number The number of objects to be selected.
	 * @return The list of selected objects.
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
