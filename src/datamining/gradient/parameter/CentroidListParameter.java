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
public class CentroidListParameter<D>
{
	protected ArrayList<Centroid<D>> centroids;

	/**
	 * @param prototypes
	 */
	public CentroidListParameter(ArrayList<Centroid<D>> centroids)
	{
		this.centroids = centroids;
	}
	
	/**
	 * @param prototypes
	 */
	public CentroidListParameter(int number, VectorSpace<D> vs)
	{
		this.centroids = new ArrayList<Centroid<D>>(number);
		
		for(int i=0; i<number; i++)
		{
			this.centroids.add(new Centroid<D>(vs, vs.getNewAddNeutralElement()));
		}
	}
	
	public CentroidListParameter<D> clone()
	{
		ArrayList<Centroid<D>> cloneCentroids = new ArrayList<Centroid<D>>(this.centroids.size());
		for(Centroid<D> centr:this.centroids) cloneCentroids.add(centr.clone());
		
		return new CentroidListParameter<>(cloneCentroids);
	}

	/**
	 * @return the centroids
	 */
	public ArrayList<Centroid<D>> getCentroids()
	{
		return this.centroids;
	}

	/**
	 * @param i Index of the centroid to be returned
	 * @return The centroid with index i.
	 */
	public Centroid<D> getCentroid(int i)
	{
		return this.centroids.get(i);
	}
}
