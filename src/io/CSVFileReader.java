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


package io;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class CSVFileReader extends FileLineReader implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= -7730060117448141601L;

	protected boolean ignoreFirstAttribute;
	
	protected boolean firstLineAsAtributeNames;
	
	protected ArrayList<String> lastAttributeList;
	
	public CSVFileReader()
	{
		this.firstLineAsAtributeNames = true;
		this.ignoreFirstAttribute = false;
		this.lastAttributeList = new ArrayList<String>();
	}
	
	
	public ArrayList<double[]> readDoubleDataTable() throws IOException
	{
		ArrayList<double[]> table = new ArrayList<double[]>(100);
		ArrayList<String> line;
		int attributeCount = -1;
		double[] doubleLine;
		int linecounter = 0;
		
		if(this.firstLineAsAtributeNames)
		{
			line = super.readStringListLine();
			this.lastAttributeList.clear();
			this.lastAttributeList.addAll(line);
			linecounter++;
		}
		
		while(super.ready())
		{
			line = super.readStringListLine();
			linecounter++;
			if(attributeCount < 0)
			{
				attributeCount = line.size();
			}
			else if(line.size() != attributeCount)
			{
				throw new IOException("File Format wrong: number of attributes are not constant. (line " + linecounter +")");
			}
			doubleLine = new double[attributeCount-(this.ignoreFirstAttribute?1:0)];
			
			for(int i=(this.ignoreFirstAttribute?1:0); i<attributeCount; i++)
			{
				doubleLine[i-(this.ignoreFirstAttribute?1:0)] = Double.parseDouble(line.get(i));
			}
			table.add(doubleLine);
		}
		
		return table;
	}


	/**
	 * @return the firstLineAsAtributeNames
	 */
	public boolean isFirstLineAsAtributeNames()
	{
		return this.firstLineAsAtributeNames;
	}


	/**
	 * @param firstLineAsAtributeNames the firstLineAsAtributeNames to set
	 */
	public void setFirstLineAsAtributeNames(boolean firstLineAsAtributeNames)
	{
		this.firstLineAsAtributeNames = firstLineAsAtributeNames;
	}


	/**
	 * @return the lastAttributeList
	 */
	public ArrayList<String> getLastAttributeList()
	{
		return this.lastAttributeList;
	}


	/**
	 * @return the ignoreFirstAttribute
	 */
	public boolean isIgnoreFirstAttribute()
	{
		return this.ignoreFirstAttribute;
	}


	/**
	 * @param ignoreFirstAttribute the ignoreFirstAttribute to set
	 */
	public void setIgnoreFirstAttribute(boolean ignoreFirstAttribute)
	{
		this.ignoreFirstAttribute = ignoreFirstAttribute;
	}


	
	
}
