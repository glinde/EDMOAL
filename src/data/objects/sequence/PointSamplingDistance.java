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

import data.algebra.Distance;
import etc.MyMath;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class PointSamplingDistance implements Distance<DoubleArraySequence>, Serializable
{
	/**  */
	private static final long	serialVersionUID	= -3808294293183147878L;
	protected int		numberOfSamplePoints;
	protected boolean	relativeVectorLength;
	protected double	relativeLinearFactor;
	
	
	public PointSamplingDistance(int numberOfSamplePoints)
	{
		this(numberOfSamplePoints, false, 0.0d);
	}
	

	public PointSamplingDistance(int numberOfSamplePoints, boolean relativeVectorLength, double relativeLinearFactor)
	{
		this.numberOfSamplePoints = numberOfSamplePoints;
		this.relativeVectorLength = relativeVectorLength;
		this.relativeLinearFactor = relativeLinearFactor;
		
	}

	/* (non-Javadoc)
	 * @see data.DistanceMeasure#distance(java.lang.Object, java.lang.Object)
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
		double tmp, length, accLength;
				
		if(a.sq.size() < 2 || b.sq.size() < 2) return Double.MAX_VALUE;
		
//		sampledDataA.add(a.sequence.firstElement());
		distance = 0.0d;
		accLength = 0.0d;
		sampleLengthA = a.length()/((double)this.numberOfSamplePoints);
		sampleLengthB = b.length()/((double)this.numberOfSamplePoints);
		coveredLengthA = 0.0d;
		coveredLengthB = 0.0d;
		accumulatedSegmentLengthA = 0.0;
		accumulatedSegmentLengthB = 0.0;
		indexA = 1;
		indexB = 1;
		segmentLengthA = MyMath.euclideanDist(a.sq.get(indexA-1), a.sq.get(indexA));
		segmentLengthB = MyMath.euclideanDist(b.sq.get(indexB-1), b.sq.get(indexB));
		for(i=0; i<this.numberOfSamplePoints-1; i++)
		{
			localDistance = 0.0d;
			length = 0.0d;
			alphaA = (coveredLengthA-accumulatedSegmentLengthA)/segmentLengthA;
			alphaB = (coveredLengthB-accumulatedSegmentLengthB)/segmentLengthB;				
			for(k=0; k<dim; k++)
			{
				tmp  = (1.0d-alphaA)*a.sq.get(indexA-1)[k] + alphaA*a.sq.get(indexA)[k];
				length += tmp*tmp;
				tmp -= (1.0d-alphaB)*b.sq.get(indexB-1)[k] + alphaB*b.sq.get(indexB)[k];
				localDistance += tmp*tmp;
			}
//			length = MyMath.euclideanLengthSquare(a.sequence.get(indexA-1));
			length = 1.0d/(Math.sqrt(length)+this.relativeLinearFactor);
			
			distance += Math.sqrt(localDistance)*((this.relativeVectorLength)?length:1.0d);
			accLength += length;
			
			coveredLengthA += sampleLengthA;
			coveredLengthB += sampleLengthB;
			
			while(coveredLengthA - accumulatedSegmentLengthA > segmentLengthA)
			{
				accumulatedSegmentLengthA += segmentLengthA;
				indexA++;
				segmentLengthA = MyMath.euclideanDist(a.sq.get(indexA-1), a.sq.get(indexA));
			}
			
			while(coveredLengthB - accumulatedSegmentLengthB > segmentLengthB)
			{
				accumulatedSegmentLengthB += segmentLengthB;
				indexB++;
				segmentLengthB = MyMath.euclideanDist(b.sq.get(indexB-1), b.sq.get(indexB));
			}
		}

		length = MyMath.euclideanLength(a.sq.get(a.sq.size()-1));
		length = 1.0d/(Math.sqrt(length)+this.relativeLinearFactor);
		distance += MyMath.euclideanDist(a.sq.get(a.sq.size()-1), b.sq.get(b.sq.size()-1))*((this.relativeVectorLength)?length:1.0d);
		accLength += length;
//		if(Double.isNaN(distance))
//			System.out.println(distance/((this.relativeVectorLength)?accLength:((double)this.numberOfSamplePoints)));
//		return distance*((this.relativeVectorLength)?1.0d/accLength:1.0d)/((double)this.numberOfSamplePoints);
		return distance/((this.relativeVectorLength)?accLength:((double)this.numberOfSamplePoints));
	}

	/* (non-Javadoc)
	 * @see data.DistanceMeasure#distanceSq(java.lang.Object, java.lang.Object)
	 */
	@Override
	public double distanceSq(DoubleArraySequence a, DoubleArraySequence b)
	{
		double dist = this.distance(a, b);
		return dist*dist;
	}

	/**
	 * @return the relativeVectorLength
	 */
	public boolean isRelativeVectorLength()
	{
		return this.relativeVectorLength;
	}

	/**
	 * @param relativeVectorLength the relativeVectorLength to set
	 */
	public void setRelativeVectorLength(boolean relativeVectorLength)
	{
		this.relativeVectorLength = relativeVectorLength;
	}


	/**
	 * @return the relativeLinearFactor
	 */
	public double getRelativeLinearFactor()
	{
		return this.relativeLinearFactor;
	}


	/**
	 * @param relativeLinearFactor the relativeLinearFactor to set
	 */
	public void setRelativeLinearFactor(double relativeLinearFactor)
	{
		this.relativeLinearFactor = relativeLinearFactor;
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
