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
import gui.DrawableObject;
import gui.Translation;
import gui.templates.BasicTemplate;
import gui.templates.GeomTemplate;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;


//import org.apache.batik.ext.awt.geom.Polygon2D;

import data.set.IndexedDataObject;
import etc.DataManipulator;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class GDataSet extends DrawableObject implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= -10276043649999779L;
	public final static int STD_CLUSTER_COLOR_INDEX = 0;
	public final static int STD_INTERNAL_AREA_STROKE_INDEX = 0;
	
	protected ArrayList<IndexedDataObject<double[]>> dataObjects;
	protected BasicTemplate dataObjectsTemplate;
		
	protected ArrayList<double[]> convexHull;
	protected boolean drawInternalArea;
	protected int internalAreaAlpha;
	
	protected int colorIndex;
	protected int convexHullStrokeIndex;
		
	public GDataSet()
	{
		super();

		this.scheme.addColor(ColorList.RED);
		this.scheme.addColor(ColorList.BLACK);
		this.scheme.addStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		this.scheme.addStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		
		this.dataObjects = new ArrayList<IndexedDataObject<double[]>>();
		
		this.resetSchemeIndices();
		
		this.dataObjectsTemplate = new GeomTemplate(GeomTemplate.FILL_CIRCLE);
		this.dataObjectsTemplate.setPixelSize(6.0d);
		this.dataObjectsTemplate.setDrawBorder(false);
		this.dataObjectsTemplate.reshape();
				
		this.convexHull = new ArrayList<double[]>();	
		this.drawInternalArea = false;
	}
	
	/* (non-Javadoc)
	 * @see gui.graphics.DrawableObject#draw(java.awt.Graphics2D, gui.Translation)
	 */
	@Override
	public void draw(Graphics2D g2, Translation translator)
	{
		Color internalAreaColor;
		Polygon surroundingPoly;
		double[] tmp;
		double[] point;
		
		this.dataObjectsTemplate.drawAtAll(g2, this.scheme, translator.translate(this.projection.projectIndexed(this.dataObjects, null), null));
		
		if(this.drawInternalArea)
		{
			internalAreaColor = new Color(this.scheme.getColor(this.colorIndex).getRed(), this.scheme.getColor(this.colorIndex).getGreen(), this.scheme.getColor(this.colorIndex).getBlue(), this.internalAreaAlpha);
			tmp = new double[2];
			
			if(this.convexHull.size() > 1)
			{
				surroundingPoly = new Polygon();
				
				for(double[] p:this.convexHull)
				{
					point = translator.translate(this.projection.project(p, tmp));
					surroundingPoly.addPoint((int)point[0], (int)point[1]);
		//					System.out.println(Arrays.toString(point));
				}
				
				g2.setStroke(this.scheme.getStroke(this.convexHullStrokeIndex));			
				g2.setColor(internalAreaColor);
				g2.fill(surroundingPoly);
				g2.setColor(this.scheme.getColor(this.colorIndex));
				g2.draw(surroundingPoly);
			}
		}
	}
	
	public void calcCrispInternalSourrounding()
	{	
		ArrayList<double[]> dataPoints = new ArrayList<double[]>(this.dataObjects.size());
		for(IndexedDataObject<double[]> d:this.dataObjects)
			dataPoints.add(d.x);
		
		this.convexHull = DataManipulator.convexHull2D(dataPoints);
	}

	/* (non-Javadoc)
	 * @see gui.graphics.DrawableObject#resetSchemeIndices()
	 */
	@Override
	public void resetSchemeIndices()
	{
		this.colorIndex = GDataSet.STD_CLUSTER_COLOR_INDEX;
		this.convexHullStrokeIndex = GDataSet.STD_INTERNAL_AREA_STROKE_INDEX;
	}
	
	public BasicTemplate getDataObjectsTemplate()
	{
		return this.dataObjectsTemplate;
	}

	public void setDataObjectsTemplate(BasicTemplate dataObjectsTemplate)
	{
		this.dataObjectsTemplate = dataObjectsTemplate;
	}

	public ArrayList<IndexedDataObject<double[]>> getDataObjects()
	{
		return this.dataObjects;
	}

	public void setDataObjects(Collection<IndexedDataObject<double[]>> dataObjects)
	{
		this.dataObjects.clear();
		this.dataObjects.addAll(dataObjects);
	}

	public void addDataObjects(Collection<IndexedDataObject<double[]>> dataObjects)
	{
		this.dataObjects.addAll(dataObjects);
	}

	public boolean isDrawInternalArea()
	{
		return this.drawInternalArea;
	}

	public void setDrawInternalArea(boolean drawInternalArea)
	{
		this.drawInternalArea = drawInternalArea;
	}
	
	/**
	 * @return the convexHull
	 */
	public ArrayList<double[]> getConvexHull()
	{
		return new ArrayList<double[]>(this.convexHull);
	}

	/**
	 * @param convexHull the convexHull to set
	 */
	public void setConvexHull(Collection<double[]> convexHull)
	{
		this.convexHull = new ArrayList<double[]>(convexHull);
	}

	/**
	 * @return the internalAreaAlpha
	 */
	public int getInternalAreaAlpha()
	{
		return this.internalAreaAlpha;
	}

	/**
	 * @param internalAreaAlpha the internalAreaAlpha to set
	 */
	public void setInternalAreaAlpha(int internalAreaAlpha)
	{
		this.internalAreaAlpha = internalAreaAlpha;
	}

	/**
	 * @return the clusterColorIndex
	 */
	public int getColorIndex()
	{
		return this.colorIndex;
	}

	/**
	 * @param clusterColorIndex the clusterColorIndex to set
	 */
	public void setColorIndex(int colorIndex)
	{
		this.colorIndex = colorIndex;
	}

	/**
	 * @return the convexHullStrokeIndex
	 */
	public int getConvexHullStrokeIndex()
	{
		return this.convexHullStrokeIndex;
	}

	/**
	 * @param convexHullStrokeIndex the convexHullStrokeIndex to set
	 */
	public void setConvexHullStrokeIndex(int convexHullStrokeIndex)
	{
		this.convexHullStrokeIndex = convexHullStrokeIndex;
	}
}
