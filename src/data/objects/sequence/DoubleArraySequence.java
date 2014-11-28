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
import java.util.ArrayList;
import java.util.Collection;

import data.algebra.Metric;


/**
 * As the name states, the class implements a double array sequence. This data type is useful for
 * comparing multidimensional time series or similar data.<br>
 * 
 * When using this class, take care that all elements of the sequence hold arrays of the same length as this is not checked by this class
 * due to performance savings. This class is just a wrapper class for a list of double arrays, it does not mask any information.
 * It is only providing additional functionality w.r.t. the data stored as double array sequence.
 *
 * @author Roland Winkler
 */
public class DoubleArraySequence implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= -2873026221012051790L;
	
	/** The sequence of double arrays, it is public because there is no need to hide the data. */
	public ArrayList<double[]> sq;
	
	
	/**
	 * The standard constructor, creating an empty sequence and using the Euclidean metric.
	 */
	public DoubleArraySequence()
	{
		this.sq = new ArrayList<double[]>();
	}

	/**
	 * A constructor that fills the sequence with the elements in the collection. Only the references
	 * are copied, not the arrays them selfs.
	 * 
	 * @param col The collection of double arrays that are used for the sequence
	 */
	public DoubleArraySequence(Collection<double[]> col)
	{
		this.sq = new ArrayList<double[]>(col);
	}
	
	/**
	 * The copy constructor. Again, only the references of double arrays are copied, not the arrays them selfs.
	 * 
	 * @param seq The sequence to be copied.
	 */
	public DoubleArraySequence(DoubleArraySequence seq)
	{
		this(seq.sq);
	}
	
	
	/**
	 * Calculates the length of the sequence, using the Euclidean distance.
	 * The length is calculated by calculating the sum of all segments of the sequence.
	 * So the length of the sequence is the length of the way in n dimensions that is defined by the sequence:
	 * length = sum_{i=1}^{n-1} eucl.dist(s_{i-1}, s_{i}) with s_i being the arrays of the sequence and n being
	 * the number of stored arrays.<br>
	 * 
	 * If the sequence contains less then 2 double arrays, the result is 0.
	 * 
	 * @return The length of the double array sequence
	 */
	public double length(Metric<double[]> metric)
	{
		double length = 0.0d;
		int i;
		
		for(i=1; i<this.sq.size(); i++)
		{
			length += metric.distance(this.sq.get(i-1), this.sq.get(i));
		}
		
		return length;
	}
	
	/**
	 * The dimension of the sequence, defined by the first array. If the sequence has no
	 * arrays stored, the result is 0. Note that the result is determined by the length of the first
	 * array of the sequence. If the dimensionality differs for different elements, the sequence is invalid.
	 * 
	 * @return the dimension of the sequence
	 */
	public int getDimension()
	{
		if(sq.size() > 0)
			return this.sq.get(0).length;
		return 0;
	}
	
	/** 
	 * Checks whether the sequence is valid or not. It is valid if all stored double arrays have
	 * the same number of elements.
	 * 
	 * @return True if all arrays of the sequence have the same length, false otherwise.
	 */
	public boolean checkSequenceValidity()
	{
		boolean result = true;
		
		for(int i=1; i<this.sq.size() && result; i++)
		{
			result &= this.sq.get(i-1) == this.sq.get(i);
		}
		
		return result;
	}
	
	/**
	 * A deep copy of the sequence. Here, the arrays are cloned.
	 */
	@Override
	public DoubleArraySequence clone()
	{
		DoubleArraySequence clone = new DoubleArraySequence(new ArrayList<double[]>(this.sq.size()));
		for(double[] p:this.sq)
		{
			clone.sq.add(p.clone());
		}
		return clone;
	}
}
