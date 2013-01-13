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

import data.set.IndexedDataObject;

/**
 * This class provides some basic statistical functions like calculating the mean or
 * the sample variance of a collection of double values.
 *
 * @author Roland Winkler
 */
public class SimpleStatistics implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= -4215266533598669192L;

	/**
	 * Calculates the mean of an array of double values.
	 * 
	 * @param data An array of double values.
	 * @return The mean of the values in the double array.
	 */
	public static double mean(double[] data)
	{
		double mean = 0.0d;
		
		for(int i=0; i<data.length; i++) mean+=data[i];
		
		mean/=((double)data.length);
		
		return mean;
	}

	/**
	 * Calculates the multidimensional mean of a collection of double arrays, embedded in an
	 * indexed data object.
	 * 
	 * @param data The collection of double arrays.
	 * @return The multidimensional mean.
	 */
	public static double[] meanIndexed(Collection<IndexedDataObject<double[]>> data)
	{
		double[] mean = null;
		
		for(IndexedDataObject<double[]> d : data)
		{
			if(mean==null) mean = d.x.clone();
			else
			{
				for(int k=0; k<mean.length; k++) mean[k] += d.x[k];
			}
		}
		for(int k=0; k<mean.length; k++) mean[k] /= data.size();
		
		return mean;
	}
	
	/**
	 * Calculates the multidimensional mean of a collection of double arrays.
	 * 
	 * @param data The collection of double arrays.
	 * @return The multidimensional mean.
	 */
	public static double[] mean(Collection<double[]> data)
	{
		double[] mean = null;
		
		for(double[] d : data)
		{
			if(mean==null) mean = d.clone();
			else
			{
				for(int k=0; k<mean.length; k++) mean[k] += d[k];
			}
		}
		for(int k=0; k<mean.length; k++) mean[k] /= data.size();
		
		return mean;
	}
	

	/** 
	 * Calculates the median of a collection of double arrays, embedded in an
	 * indexed data object. The median is the multidimensional median, which is
	 * composed of the individual median values for each dimension.
	 * 
	 * @param data The collection of double arrays.
	 * @return The multidimensional median.
	 */
	public static double[] medianIndexed(Collection<IndexedDataObject<double[]>> data)
	{
		int i, k, m=data.size()/2;
		double[][] list = new double[data.size()][];		
		i=0; for(IndexedDataObject<double[]> d: data)	{ list[i] = d.x; i++;	}
		double[] median = new double[list[0].length];
		ArrayIndexComparator aiComp = new ArrayIndexComparator(0);
				
		for(k=0; k<median.length; k++)
		{
			aiComp.setIndex(k);
			Arrays.sort(list, aiComp);
			
			if(list.length%2 == 0)
			{
				median[k] = 0.5d*(list[m][k] + list[m+1][k]);
			}
			else
			{
				median[k] = list[m][k];
			}
		}
		
		return median;
	}
	
	/** 
	 * Calculates the median of a collection of double arrays. The median
	 * is the multidimensional median, which is composed of the individual
	 * median values for each dimension.
	 * 
	 * @param data The collection of double arrays.
	 * @return The multidimensional median.
	 */
	public static double[] median(Collection<double[]> data)
	{
		int i, k, m=data.size()/2;
		double[][] list = new double[data.size()][];		
		i=0; for(double[] d: data)	{ list[i] = d; i++;	}
		double[] median = new double[list[0].length];
		ArrayIndexComparator aiComp = new ArrayIndexComparator(0);
				
		for(k=0; k<median.length; k++)
		{
			aiComp.setIndex(k);
			Arrays.sort(list, aiComp);
			
			if(list.length%2 == 0)
			{
				median[k] = 0.5d*(list[m][k] + list[m+1][k]);
			}
			else
			{
				median[k] = list[m][k];
			}
		}
		
		return median;
	}
	
	/**
	 * Calculates the sample variance of a list of double values.
	 * 
	 * @param data The double values
	 * @return The variance of a list of double value.
	 */
	public static double variance(double[] data)
	{
		double variance = 0.0d;
		double mean = SimpleStatistics.mean(data);
		
		for(int i=0; i<data.length; i++)
		{
			variance += (data[i]-mean)*(data[i]-mean);
		}
		
		variance /= (double) (data.length-1);
		
		return variance;
	}
	
	/**
	 * Calculates the sample variance of a list of double values, given the specified mean.
	 * 
	 * @param data The double values
	 * @param mean The mean of the double values (to save some calculation time, because it usually is known already, if the sample variance is supposed to be of interest).
	 * @return The variance of a list of double value.
	 */
	public static double variance(double[] data, double mean)
	{
		double variance = 0.0d;
		
		for(int i=0; i<data.length; i++)
		{
			variance += (data[i]-mean)*(data[i]-mean);
		}
		
		variance /= (double) (data.length-1);
		
		return variance;
	}

	
	/**
	 * Calculates the mean and the sample variance of a set of double values and
	 * returns it in form of an array where the first value holds the mean
	 * and the second holds the sample variance.
	 * 
	 * @param data The double values.
	 * @return The mean and sample variance as array: double[mean, variance].
	 */
	public static double[] mean_variance(double[] data)
	{
		double variance = 0.0d;
		double mean = SimpleStatistics.mean(data);
		
		for(int i=0; i<data.length; i++)
		{
			variance += (data[i]-mean)*(data[i]-mean);
		}
		
		variance/=(double)(data.length-1);
		
		return new double[]{mean, variance};
	}

	/**
	 * Calculates an axis parallel hyper rectangle as bounding box of the specified
	 * list of double arrays that are stored as indexed data objects.
	 * 
	 * @param list The list of double arrays.
	 * @return The axis parallel bounding box.
	 */
	public static ArrayList<double[]> boundingBoxCornersIndexed(Collection<IndexedDataObject<double[]>> list)
	{
		ArrayList<double[]> corners = new ArrayList<double[]>();
		double[] lowerLeft = null;
		double[] upperRight = null;
		
		for(IndexedDataObject<double[]> x:list)
		{
			if(lowerLeft == null)
			{
				lowerLeft = new double[x.x.length];
				upperRight = new double[x.x.length];
				
				for(int k=0; k<x.x.length; k++)
				{
					lowerLeft[k]  =  Double.MAX_VALUE;
					upperRight[k] = -Double.MAX_VALUE;
				}
			}
			
			for(int k=0; k<x.x.length; k++)
			{
				if(lowerLeft[k] > x.x[k]) lowerLeft[k] = x.x[k];
				if(upperRight[k] < x.x[k]) upperRight[k] = x.x[k];
			}
		}
		
		corners.add(lowerLeft);
		corners.add(upperRight);
		
		return corners;
	}
	
	/**
	 * Calculates an axis parallel hyper rectangle as bounding box of the specified
	 * list of double arrays.
	 * 
	 * @param list The list of double arrays.
	 * @return The axis parallel bounding box.
	 */
	public static ArrayList<double[]> boundingBoxCorners(Collection<double[]> list)
	{
		ArrayList<double[]> corners = new ArrayList<double[]>();
		double[] lowerLeft = null;
		double[] upperRight = null;
		
		for(double[] x:list)
		{
			if(lowerLeft == null)
			{
				lowerLeft = new double[x.length];
				upperRight = new double[x.length];
				
				for(int k=0; k<x.length; k++)
				{
					lowerLeft[k]  =  Double.MAX_VALUE;
					upperRight[k] = -Double.MAX_VALUE;
				}
			}
			
			for(int k=0; k<x.length; k++)
			{
				if(lowerLeft[k] > x[k]) lowerLeft[k] = x[k];
				if(upperRight[k] < x[k]) upperRight[k] = x[k];
			}
		}
		
		corners.add(lowerLeft);
		corners.add(upperRight);
		
		return corners;
	}
}
