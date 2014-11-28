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


package data.objects.sequence;

import java.io.Serializable;

import data.algebra.Metric;

/**
 * The point sampling metric provides a distance function for double array sequences. The two involved sequences A and B
 * do not need to have the same number of segments and they do not need to have the same length.
 * However, they are required to be valid (all arrays need to have the same number of elements) and both need to have the same
 * dimensionality.<br>
 * 
 * To ensure the independence of length and number of sequence elements, the calculation process is rather costly. First,
 * both sequences are interpolated at each, n locations which are equally distributed in each sequence individually.
 * For example, take the two sequences:<br>
 * 
 * A: (0, 0) - (1, 1) - (2, 0) and <br>
 * B: (1, 3) - (0, 1) <br>
 * 
 * and the number of interpolation points is 4. Then the interpolation points define two new sequences:<br>
 * 
 * A': (0, 0) - (0.667, 0.667) - (1.333, 0.667) - (2, 0) and<br>
 * B': (1, 3) - (0.667, 2.333) - (0.333, 1.667) - (0, 1) <br>
 * 
 * Than the distances between each pair of interpolationpoints is calculated and summed up and finally, divided by the number of
 * interpolation points: result = 1/n sum_{i=0}^{n-1} dist(A'_i, B'_i).<br>
 * 
 * To save memory and reduce dynamic allocation of memory, the interpolated sequences are not calculated all at once, but are rather
 * calculated piecewise only using heap memory. Distance calculation is in O(n + A.elem + B.elem).
 *
 * @author Roland Winkler
 */
public class PointSamplingDistance implements Metric<DoubleArraySequence>, Serializable
{
	/**  */
	private static final long	serialVersionUID	= -3808294293183147878L;
	/** The number of sample points for the interpolated sequences */
	protected int		numberOfSamplePoints;
	

	/** The metric that is used for inter-point distances in the algebraic space the sequences live in. */
	protected Metric<double[]> metric;
	
	/**
	 * Creates a new {@link PointSamplingDistance} object with the specified number of sampling points and
	 * for the specified metric. 
	 * 
	 * @param numberOfSamplePoints The number of sampling points
	 * @param metric The metric used for calculating distances in the algebraic space of the sequence points.
	 */
	public PointSamplingDistance(int numberOfSamplePoints, Metric<double[]> metric)
	{
		this.numberOfSamplePoints = numberOfSamplePoints;
		this.metric = metric;
	}

	/**
	 * Calculates the distance between two double array sequences based on two interpolations with the same number of
	 * interpolation points. The two involved sequences A and B
	 * do not need to have the same number of segments and they do not need to have the same length.
	 * However, they are required to be valid (all arrays need to have the same number of elements) and both need to have the same
	 * dimensionality.<br>
	 * 
	 * Distance calculation is in O(n + a.elem + B.elem).
	 * 
	 * @param a Sequence a
	 * @param b Sequence b
	 * @result The distance between both sequences.
	 */
	@Override
	public double distance(DoubleArraySequence a, DoubleArraySequence b)
	{
		int i, k, indexA, indexB;
		
		int dim = a.sq.get(0).length;
		double sampleLengthA, sampleLengthB;
		double coveredLengthA, coveredLengthB;
		double accumulatedSegmentLengthA, accumulatedSegmentLengthB;
		double segmentLengthA, segmentLengthB;
		double alphaA, alphaB;
		double distance, localDistance;
		double tmp;
				
		if(a.sq.size() < 2 || b.sq.size() < 2) return Double.MAX_VALUE;
		
		distance = 0.0d;
		sampleLengthA = a.length(this.metric)/((double)this.numberOfSamplePoints);
		sampleLengthB = b.length(this.metric)/((double)this.numberOfSamplePoints);
		coveredLengthA = 0.0d;
		coveredLengthB = 0.0d;
		accumulatedSegmentLengthA = 0.0;
		accumulatedSegmentLengthB = 0.0;
		indexA = 1;
		indexB = 1;
		segmentLengthA = this.metric.distance(a.sq.get(indexA-1), a.sq.get(indexA));
		segmentLengthB = this.metric.distance(b.sq.get(indexB-1), b.sq.get(indexB));
		for(i=0; i<this.numberOfSamplePoints-1; i++)
		{
			localDistance = 0.0d;
			alphaA = (coveredLengthA-accumulatedSegmentLengthA)/segmentLengthA;
			alphaB = (coveredLengthB-accumulatedSegmentLengthB)/segmentLengthB;				
			for(k=0; k<dim; k++)
			{
				tmp  =	  (1.0d-alphaA)*a.sq.get(indexA-1)[k] + alphaA*a.sq.get(indexA)[k]
						- (1.0d-alphaB)*b.sq.get(indexB-1)[k] + alphaB*b.sq.get(indexB)[k];
				localDistance += tmp*tmp;
			}
			
			distance += Math.sqrt(localDistance);
			
			coveredLengthA += sampleLengthA;
			coveredLengthB += sampleLengthB;
			
			while(coveredLengthA - accumulatedSegmentLengthA > segmentLengthA)
			{
				accumulatedSegmentLengthA += segmentLengthA;
				indexA++;
				segmentLengthA = this.metric.distance(a.sq.get(indexA-1), a.sq.get(indexA));
			}
			
			while(coveredLengthB - accumulatedSegmentLengthB > segmentLengthB)
			{
				accumulatedSegmentLengthB += segmentLengthB;
				indexB++;
				segmentLengthB = this.metric.distance(b.sq.get(indexB-1), b.sq.get(indexB));
			}
		}

		distance += this.metric.distance(a.sq.get(a.sq.size()-1), b.sq.get(b.sq.size()-1));
		
		return distance/((double)this.numberOfSamplePoints);
	}

	/**
	 * Calculates the squared value of the distance defined by the function <code>distance(DoubleArraySequence, DoubleArraySequence)</code>.
	 * 
	 * @param a Sequence a
	 * @param b Sequence b
	 * @result The squared distance between both sequences.
	 * 
	 * @see PointSamplingDistance#distance(DoubleArraySequence, DoubleArraySequence)
	 */
	@Override
	public double distanceSq(DoubleArraySequence a, DoubleArraySequence b)
	{
		double dist = this.distance(a, b);
		return dist*dist;
	}


	/**
	 * @return the numberOfSamplePoints
	 */
	public int getNumberOfSamplePoints()
	{
		return this.numberOfSamplePoints;
	}
	
	/**
	 * @param numberOfSamplePoints the numberOfSamplePoints to set
	 */
	public void setNumberOfSamplePoints(int numberOfSamplePoints)
	{
		this.numberOfSamplePoints = numberOfSamplePoints;
	}
}
