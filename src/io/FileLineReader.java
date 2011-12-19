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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class FileLineReader implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= -6215004042434817031L;

	/** */
	protected File			file;
	
	/** */
	protected BufferedReader	lineReader;
	
	/** */
	protected ArrayList<Object> 	linePattern;
	
	/** */
	protected long			fileLength;
	
	/** */
	protected long			filePosition;
	
	/**  */
	protected char			seperatorChar;
	
	/**  */
	protected String		stringIdentifier;
	
	
	/** */
	public FileLineReader()
	{	
		this.linePattern = new ArrayList<Object>(50);
		
		this.file=null;		
		this.lineReader = null;
		this.fileLength = 0;
		this.filePosition = 0;
		this.seperatorChar = ';';
		this.stringIdentifier = "\"";
	}
	
	/**
	 * @param inputFile 
	 * @throws FileNotFoundException If the specified file can not be opened.
	 */
	public void openFile(File inputFile) throws FileNotFoundException
	{
		if(this.lineReader != null)
		{
			this.closeFile();
		}
		
		if(inputFile == null) return;
		if(!inputFile.canRead()) throw new FileNotFoundException("File \"" + inputFile.toString() + "\" is not readable");
		
		this.file = inputFile;
		this.fileLength = this.file.length();
		this.filePosition = 0;
		
		this.lineReader = new BufferedReader(new InputStreamReader(new FileInputStream(this.file))); 
	}
	
	/**
	 */
	public void closeFile()
	{
		this.file = null;
		
		this.fileLength=0;
		this.filePosition=0;
		
		if(this.lineReader == null) return;
		
		try
		{
			this.lineReader.close();
		}
		catch(IOException e)
		{
			System.err.println("File not accessible");		
			e.printStackTrace();
		}
		
		this.lineReader = null;
	}
	
	/**
	 * @return a line of the current file
	 */
	public String readStringLine() throws IOException
	{
		String line = ""; 
		
		if(this.lineReader.ready())	line = this.lineReader.readLine();
		
		this.filePosition += line.length() + 1;	

		return line;
	}
	

	/**
	 * @return a line of the current file divided into fields
	 */
	public ArrayList<String> readStringListLine() throws IOException
	{
		ArrayList<String> stringLine = new ArrayList<String>();
		String line;
		int i=0, j=0;
		
		line = this.readStringLine();
		
		if(line.length() > 0)
		{
			while(i<line.length())
			{
				if(line.charAt(i) == this.seperatorChar)
				{
					stringLine.add(line.substring(j, i));
					j=i+1;
				}
				
				i++;
			}
			
			stringLine.add(line.substring(j, i).replace(stringIdentifier, "").trim());
						
//			return stringLine;
		}

//		stringLine.clear();
		return stringLine;
	}
	
		
	/** */
	public void resetLine()
	{
		this.filePosition = 0;
		
		if(this.lineReader != null)
		try
		{
			this.lineReader.reset();
		}
		catch(IOException e)
		{
			System.err.println("File not accessible");
			e.printStackTrace();
		}
	}
	
	/**
	 * @return
	 */
	public File getFile()
	{
		return this.file;
	}
	
	/**
	 * @return true if ready or false if EOF or if an error occured
	 */
	public boolean ready()
	{
		if(this.lineReader == null) return false;
		
		try
		{
			return this.lineReader.ready();
		}
		catch(IOException e)
		{
			System.err.println("File not accessible");			
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * @return
	 */
	public double getProgress()
	{
		if(this.fileLength > 0)	return ((double)this.filePosition) / ((double)this.fileLength);
		return 0.0d;
	}

	/**
	 * @return the seperatorChar
	 */
	public char getSeperatorChar()
	{
		return this.seperatorChar;
	}

	/**
	 * @param seperatorChar the seperatorChar to set
	 */
	public void setSeperatorChar(char seperatorChar)
	{
		this.seperatorChar = seperatorChar;
	}

	/**
	 * @return the stringIdentifier
	 */
	public String getStringIdentifier()
	{
		return this.stringIdentifier;
	}

	/**
	 * @param stringIdentifier the stringIdentifier to set
	 */
	public void setStringIdentifier(String stringIdentifier)
	{
		this.stringIdentifier = stringIdentifier;
	}
	
	
}

