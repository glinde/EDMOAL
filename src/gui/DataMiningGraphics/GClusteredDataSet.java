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
import gui.Scheme;
import gui.Translation;
import gui.templates.BasicTemplate;
import gui.templates.GeomTemplate;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Point2D;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import data.set.IndexedDataObject;
import datamining.clustering.ClusteringAlgorithm;
import datamining.clustering.CrispClusteringAlgorithm;
import datamining.clustering.FuzzyClusteringAlgorithm;
import etc.DataManipulator;
import etc.MyMath;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class GClusteredDataSet extends DrawableObject implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= -4317959415358092458L;

	protected int clusterCount;
	
	protected BasicTemplate dataObjectsTemplate;
	protected ArrayList<IndexedDataObject<double[]>> dataSet;
	protected ArrayList<Scheme> clusterSchemes;

	protected boolean fuzzyColoring;
	
	protected boolean fuzzyAssignmentsAvailable;
	protected int mixingColorModel;
	protected List<double[]> fuzzyMemberships;
	
	protected boolean drawMembershipLevels;
	protected double[] membershipLevels;
	protected ArrayList<ArrayList<ArrayList<double[]>>> convexHulls;
	protected int convexHullAreaAlpha;
	
	protected boolean crispAssignmentsAvailable;
	protected int[] crispClusterAssignments;


	public GClusteredDataSet(Collection<Scheme> clusterSchemes)
	{
		super();
		this.clusterCount = clusterSchemes.size();
		this.drawMembershipLevels = false;

		this.dataObjectsTemplate = new GeomTemplate(GeomTemplate.FILL_CIRCLE);
		this.dataObjectsTemplate.setPixelSize(3.0d);
		this.dataObjectsTemplate.setDrawBorder(false);
		this.dataObjectsTemplate.reshape();

		this.mixingColorModel = ColorList.HSB_COLOR_MODEL;

		this.scheme.addColor(ColorList.BLACK);
		this.scheme.addStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		
		this.membershipLevels = new double[]{1.0d};
		this.convexHulls = new ArrayList<ArrayList<ArrayList<double[]>>>(this.clusterCount);
		for(int i=0; i<this.clusterCount; i++) this.convexHulls.add(new ArrayList<ArrayList<double[]>>(5));
		this.convexHullAreaAlpha = 25; 
				
		this.clusterSchemes = new ArrayList<Scheme>(clusterSchemes);
		
		this.dataSet = new ArrayList<IndexedDataObject<double[]>>();

		this.fuzzyColoring = true;
		
		
		this.fuzzyMemberships = new ArrayList<double[]>();
		this.crispClusterAssignments = new int[this.clusterSchemes.size()];

	}
	
	protected static ArrayList<Scheme> makeClusterScemes(int number)
	{
		Scheme sc;
		ArrayList<Scheme> schemes = new ArrayList<Scheme>();
		for(int i=0; i<number; i++)
		{
			sc = new Scheme();
			sc.addColor(ColorList.clusterColors[i%ColorList.clusterColors.length]);
			sc.addStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			schemes.add(sc);
		}
		
		return schemes;
	}
	
	public GClusteredDataSet(int clusterCount)
	{
		this(GClusteredDataSet.makeClusterScemes(clusterCount));
	}
	
	
	public GClusteredDataSet(ClusteringAlgorithm<double[]> ca)
	{
		this(ca.getClusterCount());

		this.dataSet.addAll(ca.getDataSet());
		
		this.updateClusterAssignments(ca);
	}
	
	
	protected void recalculateConvexHulls()
	{
		int i, j, k;
		
		ArrayList<double[]> points = new ArrayList<double[]>();
		ArrayList<ArrayList<double[]>> crispClusteredPoints = new ArrayList<ArrayList<double[]>>(this.clusterCount);
		

		for(i=0; i<this.clusterCount; i++)
		{
			this.convexHulls.get(i).clear();
		}
		
		if(this.drawMembershipLevels)
		{			
			if(this.fuzzyAssignmentsAvailable)
			{
				for(i=0; i<this.clusterCount; i++)
				{
					for(k=0; k<this.membershipLevels.length; k++)
					{
						points.clear();
						
						for(j=0; j<this.dataSet.size(); j++)
						{
							if(this.fuzzyMemberships.get(j)[i] >= this.membershipLevels[k])
							{
								points.add(this.dataSet.get(j).element);
							}
						}
						
						if(points.size() > 1) this.convexHulls.get(i).add(DataManipulator.convexHull2D(points));
					}
				}
			}
			
			if(this.crispAssignmentsAvailable)
			{
				
				for(i=0; i<this.clusterCount; i++)
				{
					crispClusteredPoints.add(new ArrayList<double[]>(1000));
				}
				
				for(j=0; j<this.dataSet.size(); j++)
				{
					if(this.crispClusterAssignments[j] >= 0) crispClusteredPoints.get(this.crispClusterAssignments[j]).add(this.dataSet.get(j).element);
				}
				
				for(i=0; i<this.clusterCount; i++)
				{
					if(crispClusteredPoints.get(i).size() > 1) this.convexHulls.get(i).add(DataManipulator.convexHull2D(crispClusteredPoints.get(i)));
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see gui.graphics.DrawableObject#draw(java.awt.Graphics2D, gui.Translation)
	 */
	@Override
	public void draw(Graphics2D g2, Translation translator)
	{
		int j, i, k;
		
		double r, g, b, y, u, v;
		double yS, uS, vS;
		double[] colorSum = new double[6];
		Color color, internalAreaColor;
		Polygon surroundingPoly;
		double[] point;
		
		double maxMSV;
		int maxMSVI;
		float[] hsb = null;
		double[] tmp;
				
		Scheme sc = new Scheme();
		sc.addColor(this.scheme.getColor(0));
		sc.addColor(this.scheme.getColor(0));
		sc.addStroke(this.scheme.getStroke(0));
		sc.addStroke(this.scheme.getStroke(0));
		
//		this.resetClusterColors();
		for(j=0; j<this.dataSet.size(); j++)
		{
			for(i=0; i<colorSum.length; i++) colorSum[i] = 0.0d;
			
			if(this.fuzzyAssignmentsAvailable)
			{
				if(this.fuzzyColoring)
				{
					switch(this.mixingColorModel)
					{
						case ColorList.RGB_COLOR_MODEL:
						{
							for(i=0; i<this.clusterCount; i++)
							{
								r = this.clusterSchemes.get(i).getColor(0).getRed();
								g = this.clusterSchemes.get(i).getColor(0).getGreen();
								b = this.clusterSchemes.get(i).getColor(0).getBlue();
								
								colorSum[0]+=this.fuzzyMemberships.get(j)[i]*r;
								colorSum[1]+=this.fuzzyMemberships.get(j)[i]*g;
								colorSum[2]+=this.fuzzyMemberships.get(j)[i]*b;
							}
							
						}	break;
						
						case ColorList.YUV_COLOR_MODEL:
						{
							yS = 0.0d; uS = 0.0d; vS = 0.0d;
		
							for(i=0; i<this.clusterCount; i++)
							{
								r = this.clusterSchemes.get(i).getColor(0).getRed();
								g = this.clusterSchemes.get(i).getColor(0).getGreen();
								b = this.clusterSchemes.get(i).getColor(0).getBlue();
								
								y = 0.299d*r + 0.587d*g + 0.114d*b;
								u = 0.493d*(b-y);
								v = 0.877d*(r-y);
								
								yS+=MyMath.pow(this.fuzzyMemberships.get(j)[i], 0.75)*y;
								uS+=this.fuzzyMemberships.get(j)[i]*u;
								vS+=this.fuzzyMemberships.get(j)[i]*v;
							}
							
							colorSum[0] = yS + vS/0.877d;
							colorSum[2] = yS + uS/0.493d;
							colorSum[1] = 1.7d*yS - 0.509*colorSum[0] - 0.194d*colorSum[2];
												
						}	break;
						
						case ColorList.HSB_COLOR_MODEL:
						{
							for(i=0; i<this.clusterCount; i++)
							{
								r = this.clusterSchemes.get(i).getColor(0).getRed();
								g = this.clusterSchemes.get(i).getColor(0).getGreen();
								b = this.clusterSchemes.get(i).getColor(0).getBlue();
								
								colorSum[0]+=this.fuzzyMemberships.get(j)[i]*r;
								colorSum[1]+=this.fuzzyMemberships.get(j)[i]*g;
								colorSum[2]+=this.fuzzyMemberships.get(j)[i]*b;
							}
							
							hsb = Color.RGBtoHSB((int)colorSum[0], (int)colorSum[1], (int)colorSum[2], hsb);
							color = new Color(Color.HSBtoRGB(hsb[0], (float)MyMath.pow(hsb[1], 0.7), (float)MyMath.pow(hsb[2], 0.75)));
							
							colorSum[0] = color.getRed();
							colorSum[1] = color.getGreen();
							colorSum[2] = color.getBlue();
							
						}	break;
					}
					
		
					if(colorSum[0] > 255.0d) colorSum[0] = 255.0d;
					if(colorSum[1] > 255.0d) colorSum[1] = 255.0d;
					if(colorSum[2] > 255.0d) colorSum[2] = 255.0d;
					if(colorSum[0] < 0.0d) colorSum[0] = 0.0d;
					if(colorSum[1] < 0.0d) colorSum[1] = 0.0d;
					if(colorSum[2] < 0.0d) colorSum[2] = 0.0d;
					
					sc.setColor(0, new Color((int)(colorSum[0]), (int)(colorSum[1]), (int)(colorSum[2])));
				}
				else
				{
					maxMSV = 0.0d;
					maxMSVI = 0;
					for(i=0; i<this.clusterCount; i++)
					{
						if(this.fuzzyMemberships.get(j)[i] > maxMSV)
						{
							maxMSV = this.fuzzyMemberships.get(j)[i];
							maxMSVI = i;
						}
					}
		
					sc.setColor(0, this.clusterSchemes.get(maxMSVI).getColor(0));
				}
			}
			else if(this.crispAssignmentsAvailable)
			{
				if(this.crispClusterAssignments[j] >= 0) sc.setColor(0, this.clusterSchemes.get(this.crispClusterAssignments[j]).getColor(0));
				else sc.setColor(0, ColorList.BLACK);
			}
			
			
			this.dataObjectsTemplate.drawAt(g2, sc, translator.translate(this.projection.project(this.dataSet.get(j).element, null)));
		}
		

		if(this.drawMembershipLevels)
		{
			for(i=0; i<this.clusterCount; i++)
			{
				internalAreaColor = new Color(this.clusterSchemes.get(i).getColor(0).getRed(), this.clusterSchemes.get(i).getColor(0).getGreen(), this.clusterSchemes.get(i).getColor(0).getBlue(), this.convexHullAreaAlpha);
				tmp = new double[2];
				
				if(this.convexHulls.get(i).size() > 0)
				{
					for(k=0; k<this.convexHulls.get(i).size(); k++)
					{
//						if(this.convexHulls.get(i).get(k).size() < 2) continue;
						surroundingPoly = new Polygon();
						
						for(double[] p:this.convexHulls.get(i).get(k))
						{
							point = translator.translate(this.projection.project(p, tmp));
							surroundingPoly.addPoint((int)point[0], (int)point[1]);
						}
						
						g2.setStroke(this.clusterSchemes.get(i).getStroke(0));			
						g2.setColor(internalAreaColor);
						g2.fill(surroundingPoly);
						g2.setColor(this.clusterSchemes.get(i).getColor(0));
						g2.draw(surroundingPoly);
					}
				}
			}
		}
	}
	
	/**
	 * @param ca
	 */
	public void updateClusterAssignments(ClusteringAlgorithm<?> ca)
	{

		this.fuzzyAssignmentsAvailable = false;
		this.crispAssignmentsAvailable = false;
		this.fuzzyMemberships = new ArrayList<double[]>();
		this.crispClusterAssignments = new int[this.dataSet.size()];
		
		if(ca instanceof FuzzyClusteringAlgorithm)
		{
			this.fuzzyAssignmentsAvailable = true;
			((FuzzyClusteringAlgorithm<?>)ca).getAllFuzzyClusterAssignments(this.fuzzyMemberships);
		}
		
		if(ca instanceof CrispClusteringAlgorithm)
		{
			this.crispAssignmentsAvailable = true;
			this.crispClusterAssignments = ((CrispClusteringAlgorithm<?>)ca).getAllCrispClusterAssignments();
		}

		this.recalculateConvexHulls();
	}

	
	public boolean isDrawMembershipLevels()
	{
		return this.drawMembershipLevels;
	}

	public void setDrawMembershipLevels(boolean drawMembershipLevels)
	{
		this.drawMembershipLevels = drawMembershipLevels;
		this.recalculateConvexHulls();
	}

	public double[] getMembershipLevels() {
		return this.membershipLevels.clone();
	}

	public void setMembershipLevels(double[] membershipLevels) {
		this.membershipLevels = membershipLevels.clone();
		this.recalculateConvexHulls();
	}
	
	/**
	 * @return the fuzzyColoring
	 */
	public boolean isFuzzyColoring()
	{
		return this.fuzzyColoring;
	}

	/**
	 * @param fuzzyColoring the fuzzyColoring to set
	 */
	public void setFuzzyColoring(boolean fuzzyColoring)
	{
		this.fuzzyColoring = fuzzyColoring;
	}

	/* (non-Javadoc)
	 * @see gui.graphics.DrawableObject#resetSchemeIndices()
	 */
	@Override
	public void resetSchemeIndices()
	{}

	/**
	 * @return the mixingColorModel
	 */
	public int getMixingColorModel()
	{
		return this.mixingColorModel;
	}

	/**
	 * @param mixingColorModel the mixingColorModel to set
	 */
	public void setMixingColorModel(int mixingColorModel)
	{
		this.mixingColorModel = mixingColorModel;
	}

	/**
	 * @return the dataObjectsTemplate
	 */
	public BasicTemplate getDataObjectsTemplate()
	{
		return this.dataObjectsTemplate;
	}

	/**
	 * @param dataObjectsTemplate the dataObjectsTemplate to set
	 */
	public void setDataObjectsTemplate(BasicTemplate dataObjectsTemplate)
	{
		this.dataObjectsTemplate = dataObjectsTemplate;
	}

	/**
	 * @return the data
	 */
	public ArrayList<IndexedDataObject<double[]>> getDataSet()
	{
		return this.dataSet;
	}

	/**
	 * @param data the data to set
	 */
	public void setDataSet(Collection<IndexedDataObject<double[]>> data)
	{
		this.dataSet = new ArrayList<IndexedDataObject<double[]>>(data);
	}

	/**
	 * @return the fuzzyMemberships
	 */
	public ArrayList<double[]> getFuzzyMemberships()
	{
		return new ArrayList<double[]>(this.fuzzyMemberships);
	}

	/**
	 * @param fuzzyMemberships the fuzzyMemberships to set
	 */
	public void setFuzzyMemberships(Collection<double[]> fuzzyMemberships)
	{
		this.fuzzyMemberships = new ArrayList<double[]>(fuzzyMemberships);
	}

	/**
	 * @return the clusterSchemes
	 */
	public ArrayList<Scheme> getClusterSchemes()
	{
		return new ArrayList<Scheme>(this.clusterSchemes);
	}

	/**
	 * @param clusterSchemes the clusterSchemes to set
	 */
	public void setClusterSchemes(Collection<Scheme> clusterSchemes)
	{
		this.clusterSchemes = new ArrayList<Scheme>(clusterSchemes);
	}

	/**
	 * @return the convexHullAreaAlpha
	 */
	public int getConvexHullAreaAlpha()
	{
		return this.convexHullAreaAlpha;
	}

	/**
	 * @param convexHullAreaAlpha the convexHullAreaAlpha to set
	 */
	public void setConvexHullAreaAlpha(int convexHullAreaAlpha)
	{
		this.convexHullAreaAlpha = convexHullAreaAlpha;
	}

	/**
	 * @return the crispClusterAssignments
	 */
	public int[] getCrispClusterAssignments()
	{
		return this.crispClusterAssignments;
	}

	/**
	 * @param crispClusterAssignments the crispClusterAssignments to set
	 */
	public void setCrispClusterAssignments(int[] crispClusterAssignments)
	{
		this.crispClusterAssignments = crispClusterAssignments;
		this.crispAssignmentsAvailable = crispClusterAssignments != null;
		this.recalculateConvexHulls();
	}

	/**
	 * @return the clusterCount
	 */
	public int getClusterCount()
	{
		return this.clusterCount;
	}

	/**
	 * @param clusterSchemes the clusterSchemes to set
	 */
	public void setClusterSchemes(ArrayList<Scheme> clusterSchemes)
	{
		this.clusterSchemes = clusterSchemes;
	}

	/**
	 * @param fuzzyMemberships the fuzzyMemberships to set
	 */
	public void setFuzzyMemberships(List<double[]> fuzzyMemberships)
	{
		this.fuzzyMemberships = fuzzyMemberships;
		this.fuzzyAssignmentsAvailable = fuzzyMemberships != null;
		this.recalculateConvexHulls();
	}
	
	
	
}
