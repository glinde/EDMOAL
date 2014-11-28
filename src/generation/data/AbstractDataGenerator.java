/**
Copyright (c) 2013, The EDMOAL Project

	Roland Winkler
	Richard-Wagner Str. 42
	10585 Berlin, Germany
	roland.winkler@gmail.com
 
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
package generation.data;

import java.util.ArrayList;
import java.util.Random;

import data.set.IndexedDataObject;
import data.set.IndexedDataSet;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public abstract class AbstractDataGenerator<T> implements DataGenerator<T>, DataSetGenerator<T>
{
	protected Random rand;
	
	public AbstractDataGenerator()
	{
		this.rand = GlobalRandomSource.rand;
	}
	
	/* (non-Javadoc)
	 * @see generation.data.DataGenerator#generateDataObjects(int)
	 */
	@Override
	public ArrayList<T> generateDataObjects(int number)
	{
		ArrayList<T> data = new ArrayList<T>(number);
		for(int i=0; i<number; i++)
		{
			data.add(this.nextRandomObject());
		}
		
		return data;
	}
	
	/* (non-Javadoc)
	 * @see generation.data.DataSetGenerator#generateDataSet(int)
	 */
	@Override
	public IndexedDataSet<T> generateDataSet(int number)
	{
		ArrayList<T> data = this.generateDataObjects(number);		
		IndexedDataSet<T> set = new IndexedDataSet<T>(number);
		
		for(T d:data)
		{
			set.add(new IndexedDataObject<T>(d));
		}
		
		set.seal();
		
		return set;
	}

	/**
	 * @return the rand
	 */
	public Random getRand()
	{
		return this.rand;
	}

	/**
	 * @param rand the rand to set
	 */
	public void setRand(Random rand)
	{
		this.rand = rand;
	}
}
