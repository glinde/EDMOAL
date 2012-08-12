/**
Copyright (c) 2012, The EDMOAL Project

	Roland Winkler
	Richard-Wagner Str. 42
	10585 Berlin, Germany
 
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
    	this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice,
    	this list of conditions and the following disclaimer in the documentation and/or
    	other materials provided with the distribution.
    * The name of Roland Winkler may not be used to endorse or promote products
		derived from this software without specific prior written permission.

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
package datamining.gradient.parameter;

import java.util.ArrayList;

import data.algebra.VectorSpace;
import datamining.clustering.protoype.Centroid;
import datamining.clustering.protoype.Prototype;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class PositionListParameter<D>
{
	protected ArrayList<D> positions;

	/**
	 * @param prototypes
	 */
	public PositionListParameter(ArrayList<D> positions)
	{
		this.positions = new ArrayList<D>(positions);
	}
	
	/**
	 * @param prototypes
	 */
	public PositionListParameter(int number, VectorSpace<D> vs)
	{
		this.positions = new ArrayList<D>(number);
		
		for(int i=0; i<number; i++)
		{
			this.positions.add(vs.getNewAddNeutralElement());
		}
	}
	
	public PositionListParameter<D> clone(VectorSpace<D> vs)
	{
		ArrayList<D> clonePositions = new ArrayList<D>(this.positions.size());
		for(D pos:this.positions)
		{
			clonePositions.add(vs.copyNew(pos));
		}
		
		return new PositionListParameter<D>(clonePositions);
	}

	/**
	 * @return the centroids
	 */
	public ArrayList<D> getPositions()
	{
		return this.positions;
	}

	/**
	 * @param i Index of the centroid to be returned
	 * @return The centroid with index i.
	 */
	public D getPosition(int i)
	{
		return this.positions.get(i);
	}
	
	/**
	 * @return
	 */
	public int getPositionCount()
	{
		return this.positions.size();
	}
}
