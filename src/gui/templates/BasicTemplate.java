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


package gui.templates;

import gui.Scheme;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.io.Serializable;
import java.util.Collection;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public abstract class BasicTemplate implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= 6898722389987671147L;
	public final static int STD_INTERN_COLOR_INDEX = 0;
	public final static int STD_BORDER_COLOR_INDEX = 1;
	public final static int STD_STROKE_INDEX = 0; 
	
	protected Shape symbol;	
	protected double pixelSize;
	protected boolean reshapeNecessary;
		
	protected boolean drawBorder;
	protected boolean drawInternal;
	
	protected int borderColorIndex;
	protected int internalColorIndex;
	protected int strokeIndex;
	
	public BasicTemplate()
	{
		this(BasicTemplate.STD_INTERN_COLOR_INDEX, BasicTemplate.STD_BORDER_COLOR_INDEX, BasicTemplate.STD_STROKE_INDEX);
	}
	
	private BasicTemplate(int internalColorIndex, int borderColorIndex, int strokeIndex)
	{
		this.pixelSize = 8.0d;
		this.drawBorder = true;
		this.drawInternal = true;
		
		this.borderColorIndex = strokeIndex;
		this.internalColorIndex = internalColorIndex;
		this.strokeIndex = borderColorIndex;
				
		this.symbol = null;
		this.reshapeNecessary = true;
	}
	
	/**
	 * 
	 */
	public abstract void reshape();
		
	/**
	 * @param g2
	 * @param p
	 */
	public final void drawAt(Graphics2D g2, Scheme sc, double[] p)
	{
		this.drawAt(g2, sc, p[0], p[1]);
	}

	/**
	 * @param g2
	 * @param x
	 * @param y
	 */
	public final void drawAt(Graphics2D g2, Scheme sc, double x, double y)
	{
		Color c;
		Stroke s;
		
		if(!this.drawBorder && !this.drawInternal) return;
		
		if(this.reshapeNecessary) this.reshape();
		
		c = g2.getColor();
		s = g2.getStroke();
		g2.translate(x, y);
		
		if(this.drawInternal)
		{
			g2.setColor(sc.getColor(this.internalColorIndex));
			g2.setStroke(sc.getStroke(this.strokeIndex));
			g2.fill(this.symbol);
		}

		if(this.drawBorder)
		{
			g2.setColor(sc.getColor(this.borderColorIndex));
			g2.setStroke(sc.getStroke(this.strokeIndex));
			g2.draw(this.symbol);
		}

		g2.translate(-x, -y);
		g2.setStroke(s);
		g2.setColor(c);
	}
	
	public final void drawAtAll(Graphics2D g2, Scheme sc, Collection<double[]> pCol)
	{
		AffineTransform t;
		Color c;
		Stroke s;
		
		double[] last;

		if(!this.drawBorder && !this.drawInternal) return;
		if(pCol == null || pCol.size() == 0) return;
		
		if(this.reshapeNecessary) this.reshape();
		
		last = new double[]{0.0d, 0.0d};
		c = g2.getColor();
		s = g2.getStroke();
		t = g2.getTransform();

		g2.setStroke(sc.getStroke(this.strokeIndex));
		
		for(double[] p:pCol)
		{
			g2.translate(p[0] - last[0], p[1]-last[1]);
			
			if(this.drawInternal)
			{
				g2.setColor(sc.getColor(this.internalColorIndex));
				g2.fill(this.symbol);
			}
	
			if(this.drawBorder)
			{
				g2.setColor(sc.getColor(this.borderColorIndex));
				g2.draw(this.symbol);
			}
			
			last = p;
		}
		
		g2.setTransform(t);
		g2.setStroke(s);
		g2.setColor(c);
	}

	/**
	 * @return the pixelSize
	 */
	public double getPixelSize()
	{
		return this.pixelSize;
	}

	/**
	 * @param pixelSize the pixelSize to set
	 */
	public void setPixelSize(double pixelSize)
	{
		this.reshapeNecessary = this.pixelSize != pixelSize;
		this.pixelSize = pixelSize;
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