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


package gui.projections;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import data.set.IndexedDataObject;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class OrthogonalProjection implements Projection, Invertible, Serializable
{
	/**  */
	private static final long	serialVersionUID	= 1387471742461631144L;
	protected int inputDimCount;
	protected int outputDimCount;
	
	protected ArrayList<int[]> mapping;

	/**
	 * @param inputDimCount
	 * @param outputDimCount
	 */
	public OrthogonalProjection(int inputDimCount, int outputDimCount)
	{
		this.inputDimCount  = inputDimCount;
		this.outputDimCount = outputDimCount;
		
		this.mapping = new ArrayList<int[]>();
	}
	
	/**
	 * @param mapping
	 * @param inputDimCount
	 * @param outputDimCount
	 */
	public OrthogonalProjection(Collection<int[]> mapping, int inputDimCount, int outputDimCount)
	{
		this(inputDimCount, outputDimCount);
		this.mapping.addAll(mapping);
	}

	/* (non-Javadoc)
	 * @see gui.projections.Projection#project(double[], double[])
	 */
	@Override
	public double[] project(double[] inputPoint, double[] outputPoint)
	{
		int i;
		if(outputPoint == null) outputPoint = new double[this.outputDimCount];
		
		for(i=0; i<this.mapping.size();i++)
		{
			outputPoint[this.mapping.get(i)[1]] = inputPoint[this.mapping.get(i)[0]];
		}

		return outputPoint;
	}

	
	/* (non-Javadoc)
	 * @see gui.projections.Projection#projectIndexed(java.util.Collection, java.util.Collection)
	 */
	@Override
	public Collection<double[]> projectIndexed(Collection<IndexedDataObject<double[]>> inputPoints, Collection<double[]> outputPoints)
	{
		int i;
		double[] outP;
		if(outputPoints == null) outputPoints = new ArrayList<double[]>();
		
		for(IndexedDataObject<double[]> inP:inputPoints)
		{
			outP = new double[this.outputDimCount];
			
			for(i=0; i<this.mapping.size();i++)
			{
				outP[this.mapping.get(i)[1]] = inP.x[this.mapping.get(i)[0]];
			}
			
			outputPoints.add(outP);
		}
		
		return outputPoints;
	}

	/* (non-Javadoc)
	 * @see gui.projections.Projection#project(java.util.Collection, java.util.Collection)
	 */
	@Override
	public Collection<double[]> project(Collection<double[]> inputPoints, Collection<double[]> outputPoints)
	{
		int i;
		double[] outP;
		if(outputPoints == null) outputPoints = new ArrayList<double[]>();
		
		for(double[] inP:inputPoints)
		{
			outP = new double[this.outputDimCount];
			
			for(i=0; i<this.mapping.size();i++)
			{
				outP[this.mapping.get(i)[1]] = inP[this.mapping.get(i)[0]];
			}
			
			outputPoints.add(outP);
		}
		
		return outputPoints;
	}

	/* (non-Javadoc)
	 * @see gui.projections.Invertible#invProject(double[], double[])
	 */
	@Override
	public double[] invProject(double[] inputPoint, double[] outputPoint)
	{

		int i;
		if(inputPoint == null) inputPoint = new double[this.inputDimCount];
		
		for(i=0; i<this.mapping.size();i++)
		{
			inputPoint[this.mapping.get(i)[0]] = outputPoint[this.mapping.get(i)[1]];
		}

		return inputPoint;
	}

	/* (non-Javadoc)
	 * @see gui.projections.Invertible#invProject(java.util.Collection, java.util.Collection)
	 */
	@Override
	public Collection<double[]> invProject(Collection<double[]> inputPoints, Collection<double[]> outputPoints)
	{
		int i;
		double[] inP;
		if(inputPoints == null) inputPoints = new ArrayList<double[]>();
		
		for(double[] outP:inputPoints)
		{
			inP = new double[this.inputDimCount];
			
			for(i=0; i<this.mapping.size();i++)
			{
				inP[this.mapping.get(i)[0]] = outP[this.mapping.get(i)[1]];
			}
			
			inputPoints.add(inP);
		}
		
		return inputPoints;
	}

	/**
	 * @return the inputDimCount
	 */
	public int getInputDimCount()
	{
		return this.inputDimCount;
	}

	/**
	 * @param inputDimCount the inputDimCount to set
	 */
	public void setInputDimCount(int inputDimCount)
	{
		this.inputDimCount = inputDimCount;
	}

	/**
	 * @return the outputDimCount
	 */
	public int getOutputDimCount()
	{
		return this.outputDimCount;
	}

	/**
	 * @param outputDimCount the outputDimCount to set
	 */
	public void setOutputDimCount(int outputDimCount)
	{
		this.outputDimCount = outputDimCount;
	}

	/**
	 * @param e
	 * @return
	 * @see java.util.Vector#add(java.lang.Object)
	 */
	public boolean addMapping(int[] e)
	{
		return this.mapping.add(e);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.Vector#addAll(java.util.Collection)
	 */
	public boolean addAllMappings(Collection<? extends int[]> c)
	{
		return this.mapping.addAll(c);
	}

	/**
	 * 
	 * @see java.util.Vector#clear()
	 */
	public void clearMappings()
	{
		this.mapping.clear();
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.Vector#get(int)
	 */
	public int[] getMapping(int index)
	{
		return this.mapping.get(index);
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.Vector#remove(int)
	 */
	public int[] removeMapping(int index)
	{
		return this.mapping.remove(index);
	}

	/**
	 * @param map
	 * @return
	 * @see java.util.Vector#remove(java.lang.Object)
	 */
	public boolean removeMapping(int[] map)
	{
		return this.mapping.remove(map);
	}

	/**
	 * @param mappings
	 * @return
	 * @see java.util.Vector#removeAll(java.util.Collection)
	 */
	public boolean removeAllMappings(Collection<int[]> mappings)
	{
		return this.mapping.removeAll(mappings);
	}

	/**
	 * @param index
	 * @param map
	 * @return
	 * @see java.util.Vector#set(int, java.lang.Object)
	 */
	public int[] setMapping(int index, int[] map)
	{
		return this.mapping.set(index, map);
	}

	/**
	 * @return
	 * @see java.util.Vector#size()
	 */
	public int countMappings()
	{
		return this.mapping.size();
	}
	
	
}
