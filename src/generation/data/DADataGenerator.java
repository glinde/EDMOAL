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
import java.util.Collection;
import java.util.Random;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.random.RandomDataGenerator;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class DADataGenerator extends AbstractDataGenerator<double[]>
{
	protected ArrayList<AbstractRealDistribution> distributionList;
	
	DADataGenerator()
	{
		this.distributionList = new ArrayList<AbstractRealDistribution>();
	}

	DADataGenerator(Collection<AbstractRealDistribution> distributions)
	{
		this.distributionList.addAll(distributions);
	}


	/* (non-Javadoc)
	 * @see generation.data.DataGenerator#generateDataObjects(int)
	 */
	@Override
	public ArrayList<double[]> generateDataObjects(int number)
	{
		ArrayList<double[]> data = new ArrayList<double[]>(number);
				
		for(int j=0; j<number; j++)
		{
			double[] x = new double[this.getDim()];
			
			for(int k=0; k<this.getDim(); k++)
			{
				x[k] = this.distributionList.get(k).sample();
			}
			
			data.add(x);
		}
		
		return data;
	}
	
	public int getDim()
	{
		return this.distributionList.size();		
	}
	
	
	/**
	 * @return the distributionList
	 */
	public ArrayList<AbstractRealDistribution> getDistributionList()
	{
		return this.distributionList;
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.ArrayList#get(int)
	 */
	public AbstractRealDistribution getDistribution(int index)
	{
		return distributionList.get(index);
	}

	/**
	 * @param e
	 * @return
	 * @see java.util.ArrayList#add(java.lang.Object)
	 */
	public boolean addDistribution(AbstractRealDistribution e)
	{
		return distributionList.add(e);
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.ArrayList#remove(int)
	 */
	public AbstractRealDistribution removeDistribution(int index)
	{
		return distributionList.remove(index);
	}

	/**
	 * 
	 * @see java.util.ArrayList#clear()
	 */
	public void clearDistributionList()
	{
		distributionList.clear();
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.ArrayList#addAll(java.util.Collection)
	 */
	public boolean addAllDistributions(Collection<? extends AbstractRealDistribution> c)
	{
		return distributionList.addAll(c);
	}
}
