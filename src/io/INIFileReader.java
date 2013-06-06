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
package io;

import java.io.IOException;
import java.util.TreeMap;

/**
 * @author rwinkler
 *
 */
public class INIFileReader extends FileLineReader
{
	private TreeMap<String, String> map;
	
	public INIFileReader()
	{
		super();
		
		this.map = new TreeMap<String, String>();
	}

	public void loadInformaion() throws IOException
	{
		String[] lineList;
		String line;
		
		try
		{
			while(this.ready())
			{	
				line = this.readStringLine();
				if(line.startsWith("#")) continue;
				lineList = line.split("=");				
				if(lineList.length == 2 && lineList[0].trim().length() > 0) this.map.put(lineList[0].trim(), lineList[1].trim());
			};
		}
		catch(IOException e)
		{
			this.closeFile();
			throw new IOException(e);
		}
	}
	
	public String getElement(String key)
	{
		return this.map.get(key);
	}

	public short parseShort(String key)
	{
		return Short.parseShort(this.map.get(key));
	}

	public int parseInt(String key)
	{
		return Integer.parseInt(this.map.get(key));
	}

	public long parseLong(String key)
	{
		return Long.parseLong(this.map.get(key));
	}

	public float parseFloat(String key)
	{
		return Float.parseFloat(this.map.get(key));
	}

	public double parseDouble(String key)
	{
		return Double.parseDouble(this.map.get(key));
	}

	public short[] parseShortArray(String key)
	{
		short[] array;
		String element = this.map.get(key);		
		if(element == null) return null;
		
		String[] numbers = element.substring(1, element.length()-1).split(","); 
		array = new short[numbers.length];
		
		for(int k=0; k<array.length; k++)
		{
			array[k] = Short.parseShort(numbers[k].trim());
		}
				
		return array;
	}

	public int[] parseIntArray(String key)
	{
		int[] array;
		String element = this.map.get(key);		
		if(element == null) return null;
		
		String[] numbers = element.substring(1, element.length()-1).split(","); 
		array = new int[numbers.length];
		
		for(int k=0; k<array.length; k++)
		{
			array[k] = Integer.parseInt(numbers[k].trim());
		}
				
		return array;
	}

	public long[] parseLongArray(String key)
	{
		long[] array;
		String element = this.map.get(key);		
		if(element == null) return null;
		
		String[] numbers = element.substring(1, element.length()-1).split(","); 
		array = new long[numbers.length];
		
		for(int k=0; k<array.length; k++)
		{
			array[k] = Long.parseLong(numbers[k].trim());
		}
				
		return array;
	}

	public float[] parseFloatArray(String key)
	{
		float[] array;
		String element = this.map.get(key);		
		if(element == null) return null;
		
		String[] numbers = element.substring(1, element.length()-1).split(","); 
		array = new float[numbers.length];
		
		for(int k=0; k<array.length; k++)
		{
			array[k] = Float.parseFloat(numbers[k].trim());
		}
				
		return array;
	}

	public double[] parseDoubleArray(String key)
	{
		double[] array;
		String element = this.map.get(key);		
		if(element == null) return null;
		
		String[] numbers = element.substring(1, element.length()-1).split(","); 
		array = new double[numbers.length];
		
		for(int k=0; k<array.length; k++)
		{
			array[k] = Double.parseDouble(numbers[k].trim());
		}
				
		return array;
	}
}
