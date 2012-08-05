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


package datamining;

import data.set.AbstractStaticDataSetContainer;
import data.set.DataSetNotSealedException;
import data.set.IndexedDataSet;

/**
 * This an abstract class realizes the {@link DataMiningAlgorithm} interface. It provedes almost no
 * functionality but it stores the data set as final which should be done for all static data mining algorithms.
 * Therefore, it is recommended that all data mining algorithms that require a static data set extend this class. 
 *
 * @author Roland Winkler
 */
public abstract class AbstractStaticDataMiningAlgorithm<T> extends AbstractStaticDataSetContainer<T> implements DataMiningAlgorithm<T>
{	
	/**  */
	private static final long	serialVersionUID	= 3546778582879302179L;

	/**
	 * The standard constructor, taking the data set that is supposed to be analysed. Because
	 * this is a static data mining algorithm, it requires the data set to be sealed.
	 * 
	 * @param data The data set that is to be analysed.
	 * 
	 * @throws DataSetNotSealedException if the data set is not sealed.
	 */
	public AbstractStaticDataMiningAlgorithm(IndexedDataSet<T> data) throws DataSetNotSealedException
	{
		super(data);
	}
	
	/**
	 * The copy constructor. 
	 * 
	 * @param c The <code>AbstractStaticDataMiningAlgorithm</code> to be copied.
	 */
	public AbstractStaticDataMiningAlgorithm(AbstractStaticDataMiningAlgorithm<T> c)
	{
		super(c);
	}
}
