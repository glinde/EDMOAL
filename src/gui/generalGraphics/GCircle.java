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


package gui.generalGraphics;

import gui.DrawableObject;
import gui.Translation;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.io.Serializable;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class GCircle extends DrawableObject implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= -7904179861762901159L;
	public final static int STD_INTERN_COLOR_INDEX = 0;
	public final static int STD_BORDER_COLOR_INDEX = 1;
	public final static int STD_STROKE_INDEX = 0; 
	
	protected double[] center;
	protected double radius;	
	protected boolean drawBorder;
	protected boolean drawInternal;
	

	protected int borderColorIndex;
	protected int internalColorIndex;
	protected int strokeIndex;

	public GCircle()
	{
		this(null);
	}
	
	public GCircle(DrawableObject parent)
	{
		super(parent);
		
		this.radius = 1.0d;
		this.drawBorder = true;
		this.drawInternal = true;
		
		this.center = new double[]{0.0d, 0.0d};
		
		this.resetSchemeIndices();
	}
	
	/* (non-Javadoc)
	 * @see gui.DrawableObject#draw(java.awt.Graphics2D, gui.Translation)
	 */
	@Override
	protected void draw(Graphics2D g2, Translation translator)
	{
		Shape s;
		double[] lowerLeftCorner = new double[]{this.center[0]-this.radius, this.center[1]+this.radius};
		
		lowerLeftCorner = translator.translate(this.projection.project(lowerLeftCorner, null));
		
		s = new Ellipse2D.Double(lowerLeftCorner[0], lowerLeftCorner[1], translator.scaleLength(2.0d*this.radius), translator.scaleLength(2.0d*this.radius));
		
		if(this.drawInternal)
		{
			g2.setColor(this.scheme.getColor(this.internalColorIndex));
			g2.setStroke(this.scheme.getStroke(this.strokeIndex));
			g2.fill(s);
		}
		if(this.drawBorder)
		{
			g2.setColor(this.scheme.getColor(this.borderColorIndex));
			g2.setStroke(this.scheme.getStroke(this.strokeIndex));
			g2.draw(s);			
		}
	}

	/* (non-Javadoc)
	 * @see gui.DrawableObject#resetSchemeIndices()
	 */
	@Override
	public void resetSchemeIndices()
	{

		borderColorIndex = GCircle.STD_BORDER_COLOR_INDEX;
		internalColorIndex = GCircle.STD_INTERN_COLOR_INDEX;
		strokeIndex = GCircle.STD_STROKE_INDEX;
	}

	/**
	 * @return the center
	 */
	public double[] getCenter()
	{
		return this.center;
	}

	/**
	 * @param center the center to set
	 */
	public void setCenter(double[] center)
	{
		this.center = center;
	}

	/**
	 * @return the radius
	 */
	public double getRadius()
	{
		return this.radius;
	}

	/**
	 * @param radius the radius to set
	 */
	public void setRadius(double radius)
	{
		this.radius = radius;
	}

	/**
	 * @return the drawBorder
	 */
	public boolean isDrawBorder()
	{
		return this.drawBorder;
	}

	/**
	 * @param drawBorder the drawBorder to set
	 */
	public void setDrawBorder(boolean drawBorder)
	{
		this.drawBorder = drawBorder;
	}

	/**
	 * @return the drawInternal
	 */
	public boolean isDrawInternal()
	{
		return this.drawInternal;
	}

	/**
	 * @param drawInternal the drawInternal to set
	 */
	public void setDrawInternal(boolean drawInternal)
	{
		this.drawInternal = drawInternal;
	}

	/**
	 * @return the borderColorIndex
	 */
	public int getBorderColorIndex()
	{
		return this.borderColorIndex;
	}

	/**
	 * @param borderColorIndex the borderColorIndex to set
	 */
	public void setBorderColorIndex(int borderColorIndex)
	{
		this.borderColorIndex = borderColorIndex;
	}

	/**
	 * @return the internalColorIndex
	 */
	public int getInternalColorIndex()
	{
		return this.internalColorIndex;
	}

	/**
	 * @param internalColorIndex the internalColorIndex to set
	 */
	public void setInternalColorIndex(int internalColorIndex)
	{
		this.internalColorIndex = internalColorIndex;
	}

	/**
	 * @return the strokeIndex
	 */
	public int getStrokeIndex()
	{
		return this.strokeIndex;
	}

	/**
	 * @param strokeIndex the strokeIndex to set
	 */
	public void setStrokeIndex(int strokeIndex)
	{
		this.strokeIndex = strokeIndex;
	}

	
}
