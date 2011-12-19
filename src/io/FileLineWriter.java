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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class FileLineWriter implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= -4216720560868193641L;

	/** */
	protected File			file;
	
	/** */
	protected BufferedWriter	lineWriter;
	
	/** */
	protected ArrayList<Object> 	linePattern;
	
	/** */
	protected long			fileLength;
	
	/** */
	protected long			filePosition;
	
	/**  */
	protected char			seperatorChar;
	
	
	/** */
	public FileLineWriter()
	{	
		this.linePattern = new ArrayList<Object>(50);
		
		this.file=null;		
		this.lineWriter = null;
		this.fileLength = 0;
		this.filePosition = 0;
		this.seperatorChar = ';';
	}
	
	/**
	 * @param outputFile 
	 * @throws IOException 
	 */
	public void openFile(File outputFile) throws IOException
	{
		if(this.lineWriter != null)
		{
			this.closeFile();
		}
		
		if(outputFile == null) return;
		if(outputFile.isDirectory())
		{
			System.out.println("Filepath is a directory.");
			return;
		}
		if(outputFile.exists())
		{
			outputFile.delete();
		}
		
		this.file = outputFile;
		this.fileLength = 0;
		this.filePosition = 0;
		
		outputFile.createNewFile();
		this.lineWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.file)));
	}
	
	/**
	 */
	public void closeFile()
	{
		if(this.lineWriter == null) return;
		
		try
		{
			this.lineWriter.flush();
			this.lineWriter.close();
		}
		catch(IOException e)
		{
			System.err.println("File not accessible");		
			e.printStackTrace();
		}
		
		this.file = null;
		this.fileLength=0;
		this.filePosition=0;
		this.lineWriter = null;
	
	}
	
	/**
	 * @return a line of the current file
	 */
	public void writeStringLine(String line) throws IOException
	{	
		this.lineWriter.write(line);
		this.lineWriter.newLine();
		
		this.filePosition += line.length() + 1;
	}
	

	/**
	 * @return a line of the current file divided into fields
	 */
	public void writeStringListLine(List<String> stringLine) throws IOException
	{
		String line;
		StringBuffer sb = new StringBuffer();
		int i=0;
		
		sb.append(stringLine.get(0));
		for(i=1; i<stringLine.size(); i++)
		{
			sb.append(this.seperatorChar);
			sb.append(stringLine.get(i).trim());
		}
		line = sb.toString();
		this.writeStringLine(line);
		this.lineWriter.flush();
	}
	


	/**
	 * @return a line of the current file divided into fields
	 */
	public void writeStringListLines(ArrayList<ArrayList<String>> stringLines) throws IOException
	{
		String line;
		StringBuffer sb;
		int i=0;
		int flushSize=0;
		
		for(ArrayList<String> stringLine:stringLines)
		{
			sb = new StringBuffer();
			sb.append(stringLine.get(0));
			for(i=1; i<stringLine.size(); i++)
			{
				sb.append(this.seperatorChar);
				sb.append(stringLine.get(i).trim());
			}
			line = sb.toString();
			this.writeStringLine(line);
			flushSize+=line.length();
			
			// flush every 1 MB of written file size
			if(16*flushSize>1024*1024)
			{
				flushSize = 0;
				this.lineWriter.flush();
			}
		}	
			
		this.lineWriter.flush();
	}
	
	/**
	 * @return
	 */
	public File getFile()
	{
		return this.file;
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
}
