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

import etc.MyMath;


/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class DoubleArraySequence implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= -2873026221012051790L;
	/**  */
	public ArrayList<double[]> sq;
	
	/**
	 * 
	 */
	public DoubleArraySequence()
	{
		this.sq = new ArrayList<double[]>();
	}

	/**
	 * @param c
	 */
	public DoubleArraySequence(Collection<double[]> col)
	{
		this.sq = new ArrayList<double[]>(col.size());
		this.sq.addAll(col);
	}
	
	/**
	 * @param c
	 */
	public DoubleArraySequence(DoubleArraySequence seq)
	{
		this(seq.sq);
	}
	
	
	public double length()
	{
		double length = 0.0d;
		int i;
		
		for(i=1; i<this.sq.size(); i++)
		{
			length += MyMath.euclideanDist(this.sq.get(i-1), this.sq.get(i));
		}
		
		return length;
	}
	
	/**
	 * @return
	 */
	public int getDimension()
	{
		if(sq.size() > 0)
			return this.sq.get(0).length;
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
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
