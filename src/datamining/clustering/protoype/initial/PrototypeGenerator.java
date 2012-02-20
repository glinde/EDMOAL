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


package datamining.clustering.protoype.initial;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import data.algebra.VectorSpace;
import data.set.IndexedDataObject;
import datamining.clustering.protoype.Centroid;

/**
 * A service class for generating prototypes at random locations providing a variety of generation
 * strategies.  
 *
 * @author Roland Winkler
 */
public class PrototypeGenerator<T> implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= -3289242112173663584L;

	/** The random object to generate pseudo-random values. */
	protected Random rand;
	
	/** the vector space of the data set. */
	protected VectorSpace<T> vs;
	
	/**
	 * @param dataGenerator
	 */
	public PrototypeGenerator(VectorSpace<T> vs)
	{
		this.rand = new Random();
		this.vs = vs;
	}

	/**
	 * @param dataGenerator
	 */
	public PrototypeGenerator(long seed, VectorSpace<T> vs)
	{
		this.rand = new Random(seed);
		this.vs = vs;
	}
	
		
	/**
	 * Selects the first <code>number</code> of possible positions to create centroids
	 * with initial positions at these locations.<br>
	 * 
	 * If the number of <code>possiblePositions</code> is smaller than the specified <code>number</code>,
	 * some of the positions are used multiple times.
	 * 
	 * @param possiblePositions A collection of possible positions.
	 * @param number The number of prototypes to be generated.
	 * @return A list of prototypes, initialized at the specified locations.
	 */
	public ArrayList<Centroid<T>> ascendingExcamples(Collection<T> possiblePositions, int number)
	{
		int i;
		ArrayList<Centroid<T>> prototypes = new ArrayList<Centroid<T>>();
		ArrayList<T> positions = new ArrayList<T>(possiblePositions.size());
		positions.addAll(possiblePositions);

		for(i=0; i<number; i++)
		{
			prototypes.add(new Centroid<T>(this.vs, positions.get(i%positions.size())));
		}
		
		return prototypes;
	}
		
	/**
	 * Sets the positions of the prototypes according to the given possible positions by randomly picking a position.
	 * If the number of possible positions is not much larger than the number of prototypes,
	 * it is likely that some positions are used multiple times.
	 * 
	 * @param possiblePositions A collection of possible positions.
	 * @param number The number of prototypes to be generated.
	 * @return A list of prototypes, initialized at the specified locations.
	 */
	public ArrayList<Centroid<T>> randomExcamplesSample(List<T> possiblePositions, int number)
	{
		int i;
		ArrayList<Centroid<T>> prototypes = new ArrayList<Centroid<T>>();

		for(i=0; i<number; i++)
		{
			prototypes.add(new Centroid<T>(this.vs, possiblePositions.get(this.rand.nextInt(possiblePositions.size()))));
		}
		
		return prototypes;
	}

	
	/**
	 * Uses a data set to specify the initial positions of the prototypes. The data set should be much larger than the
	 * number of prototypes that are generated. Still, it is possible that some prototypes are initialized at
	 * the same position.
	 * 
	 * @param possiblePositions A collection of possible positions.
	 * @param number The number of prototypes to be generated.
	 * @return A list of prototypes, initialized at the specified locations.
	 */
	public ArrayList<Centroid<T>> randomDataSetSample(List<IndexedDataObject<T>> possiblePositions, int number)
	{
		int i;
		ArrayList<Centroid<T>> prototypes = new ArrayList<Centroid<T>>();

		for(i=0; i<number; i++)
		{
			prototypes.add(new Centroid<T>(this.vs, possiblePositions.get(this.rand.nextInt(possiblePositions.size())).x));
		}
		
		return prototypes;
	}
	
}
