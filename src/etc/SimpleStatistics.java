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
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class SimpleStatistics implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= -4215266533598669192L;

	public static double mean(double[] data)
	{
		double mean =0;
		
		for(int i=0; i<data.length; i++) mean+=data[i];
		
		mean/=((double)data.length);
		
		return mean;
	}
	
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
	 * @param data
	 * @return double[mean, variance]
	 */
	public static double[] varianceMean(double[] data)
	{
		double variance = 0.0d;
		double mean = SimpleStatistics.mean(data);
		
		for(int i=0; i<data.length; i++)
		{
			variance += (data[i]-mean)*(data[i]-mean);
		}
		
		variance/=(double)data.length;
		
		return new double[]{mean, variance};
	}

	public static ArrayList<double[]> boundingBoxCornersIndexed(Collection<IndexedDataObject<double[]>> list)
	{
		ArrayList<double[]> corners = new ArrayList<double[]>();
		double[] upperLeft = null;
		double[] lowerRight = null;
		
		for(IndexedDataObject<double[]> x:list)
		{
			if(upperLeft == null)
			{
				upperLeft = new double[x.x.length];
				lowerRight = new double[x.x.length];
				
				for(int k=0; k<x.x.length; k++)
				{
					upperLeft[k] = -Double.MAX_VALUE;
					lowerRight[k] = Double.MAX_VALUE;
				}
			}
			
			for(int k=0; k<x.x.length; k++)
			{
				if(upperLeft[k] < x.x[k]) upperLeft[k] = x.x[k];
				if(lowerRight[k] > x.x[k]) lowerRight[k] = x.x[k];
			}
		}
		
		corners.add(upperLeft);
		corners.add(lowerRight);
		
		return corners;
	}
	
	public static ArrayList<double[]> boundingBoxCorners(Collection<double[]> list)
	{
		ArrayList<double[]> corners = new ArrayList<double[]>();
		double[] upperLeft = null;
		double[] lowerRight = null;
		
		for(double[] x:list)
		{
			if(upperLeft == null)
			{
				upperLeft = new double[x.length];
				lowerRight = new double[x.length];
				
				for(int k=0; k<x.length; k++)
				{
					upperLeft[k] = -Double.MAX_VALUE;
					lowerRight[k] = Double.MAX_VALUE;
				}
			}
			
			for(int k=0; k<x.length; k++)
			{
				if(upperLeft[k] < x[k]) upperLeft[k] = x[k];
				if(lowerRight[k] > x[k]) lowerRight[k] = x[k];
			}
		}
		
		corners.add(upperLeft);
		corners.add(lowerRight);
		
		return corners;
	}
}
