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


package gui.DataMiningGraphics;

import gui.ColorList;
import gui.Scheme;

import java.awt.BasicStroke;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import datamining.clustering.protoype.Prototype;
import datamining.resultProviders.PrototypeProvider;
import datamining.resultProviders.ResultProvider;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class GCentroidClusteringAlgorithm extends GClusteredDataSet implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= 1265531814270253408L;

	protected ArrayList<GCentroid> gCentroids;
		
	protected PrototypeProvider<double[], ? extends Prototype<double[]>> prototypeProvider;

	public GCentroidClusteringAlgorithm(Collection<Scheme> clusterSchemes)
	{
		super(clusterSchemes);
		
		GCentroid gCentroid;
		
		this.prototypeProvider = null;
		this.gCentroids = new ArrayList<GCentroid>(clusterSchemes.size());
		
		for(int i=0; i<clusterSchemes.size(); i++)
		{
			gCentroid = new GCentroid();
			this.gCentroids.add(gCentroid);
			this.addChild(gCentroid);
			gCentroid.setSchemePropergated(this.clusterSchemes.get(i));
		}
	}
	
	
	protected static ArrayList<Scheme> makePrototypeScemes(int number)
	{
		Scheme sc;
		ArrayList<Scheme> schemes = new ArrayList<Scheme>();
		for(int i=0; i<number; i++)
		{
			sc = new Scheme();
			sc.addColor(ColorList.clusterColors[i%ColorList.clusterColors.length]);
			sc.addColor(ColorList.BLACK);
			sc.addStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			sc.addStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			schemes.add(sc);
		}
		
		return schemes;
	}
	
	public GCentroidClusteringAlgorithm(int clusterCount)
	{
		this(GCentroidClusteringAlgorithm.makePrototypeScemes(clusterCount));
	}
	

	public  GCentroidClusteringAlgorithm(PrototypeProvider<double[], ? extends Prototype<double[]>> prototypeProvider, ResultProvider<double[]> resultProvider, int[] dataSubsetList)
	{
		this(prototypeProvider.getPrototypeCount());

		this.dataSubsetPresentation = dataSubsetList != null;
		this.dataSubsetList = dataSubsetList;

		this.prototypeProvider = prototypeProvider;

		this.dataSet.addAll(this.prototypeProvider.getDataSet());	

		for(int i=0; i<this.clusterCount; i++)
		{
			this.gCentroids.get(i).setPrototype(this.prototypeProvider.getPrototypes().get(i));
		}
		
		
		this.updateClusterAssignments(resultProvider);
	}
}
