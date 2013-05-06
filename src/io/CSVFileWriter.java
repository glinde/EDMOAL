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
import java.util.List;

import data.set.IndexedDataObject;
import data.set.IndexedDataSet;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class CSVFileWriter extends FileLineWriter implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= 4768616987919910789L;

	protected boolean firstLineAsAtributeNames;

	protected boolean addFirstAttributeAsID;
	
	protected String defaultAttributeName;
	
	public CSVFileWriter()
	{
		super();
		
		this.firstLineAsAtributeNames = true;
		this.addFirstAttributeAsID = false;
		this.defaultAttributeName = "attribute ";
	}

	
	public void writeDoubleDataTableIndexed(IndexedDataSet<double[]> dataSet, List<String> attributeNames) throws IOException
	{
		ArrayList<double[]> data = new ArrayList<double[]>(dataSet.size());
		for(IndexedDataObject<double[]> d:dataSet) data.add(d.x);
		
		this.writeDoubleDataTable(data, attributeNames);
	}
	
	public void writeDoubleDataTable(List<double[]> data, List<String> attributeNames) throws IOException
	{
		int i, j;
		ArrayList<String> attributeNamesList;
		
		if(this.firstLineAsAtributeNames)
		{
			if(attributeNames == null) attributeNamesList = new ArrayList<String>(data.get(0).length+2);
			else attributeNamesList = new ArrayList<String>(attributeNames);
			
			if(data.get(0).length > attributeNamesList.size())
			{
				for(i=attributeNamesList.size();i<data.get(0).length;i++)
				{
					attributeNamesList.add(this.defaultAttributeName + i);
				}
			}
			if(this.addFirstAttributeAsID) attributeNamesList.add(0, "ID");
			
			//System.out.println("attributes: " + attributeNames.toString());
			this.writeStringListLine(attributeNamesList);
		}
		
		ArrayList<ArrayList<String>> dataStringTable = new ArrayList<ArrayList<String>>();
		ArrayList<String> line;
		for(i=0; i<data.size(); i++)
		{
			line = new ArrayList<String>();
			if(this.addFirstAttributeAsID) line.add("" + i);
			for(j=0;j<data.get(i).length; j++)
			{
				line.add("" + data.get(i)[j]);
			}
			dataStringTable.add(line);
			
			if(i>0 && i%1000 == 0)
			{
				this.writeStringListLines(dataStringTable);
				dataStringTable.clear();
				System.gc();
			}
		}

		this.writeStringListLines(dataStringTable);
		dataStringTable.clear();
		System.gc();
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
	 * @return the addFirstAttributeAsID
	 */
	public boolean isAddFirstAttributeAsID()
	{
		return this.addFirstAttributeAsID;
	}


	/**
	 * @param addFirstAttributeAsID the addFirstAttributeAsID to set
	 */
	public void setAddFirstAttributeAsID(boolean addFirstAttributeAsID)
	{
		this.addFirstAttributeAsID = addFirstAttributeAsID;
	}


	/**
	 * @return the defaultAttributeName
	 */
	public String getDefaultAttributeName()
	{
		return this.defaultAttributeName;
	}


	/**
	 * @param defaultAttributeName the defaultAttributeName to set
	 */
	public void setDefaultAttributeName(String defaultAttributeName)
	{
		this.defaultAttributeName = defaultAttributeName;
	}
	
	
	
}
