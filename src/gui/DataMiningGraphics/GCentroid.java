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
import gui.generalGraphics.GPath;
import gui.templates.BasicTemplate;
import gui.templates.GeomTemplate;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.io.Serializable;

import datamining.clustering.protoype.Centroid;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class GCentroid extends DrawableObject implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= 6771629669274492280L;
	
	public final static int STD_FILL_COLOR_INDEX = 0;
	public final static int STD_BORDER_COLOR_INDEX = 1;
	public final static int STD_BODY_STROKE_INDEX = 0;
	public final static int STD_TAIL_STROKE_INDEX = 1;
	
	protected Centroid<double[]> prototype;
		
	protected BasicTemplate body;
	protected GPath tail;
	
	protected boolean drawBody;
	
	private int fillColorIndex;
	private int borderColorIndex;
	private int bodyStrokeIndex;
		
	public GCentroid()
	{
		this(null);
		this.scheme.addColor(ColorList.RED);
		this.scheme.addColor(ColorList.BLACK);
		this.scheme.addStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		this.scheme.addStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
	}

	/**
	 * @param parent
	 */
	public GCentroid(DrawableObject parent)
	{
		super(parent);
		this.prototype = null;
		
		this.resetSchemeIndices();
		
		// the 'head' of the prototype
		this.body = new GeomTemplate(GeomTemplate.FILL_CIRCLE);
		this.body.setPixelSize(30.0d);
		this.body.setInternalColorIndex(this.fillColorIndex);
		this.body.setBorderColorIndex(this.borderColorIndex);
		this.body.setStrokeIndex(this.bodyStrokeIndex);
		this.body.setDrawBorder(true);
		this.body.setDrawInternal(true);
		this.body.reshape();
		this.drawBody = true;
		
		this.tail = new GPath(this);
		this.addChild(this.tail);
	}
	
	/* (non-Javadoc)
	 * @see gui.DrawableObject#draw(java.awt.Graphics2D)
	 */
	@Override
	public void draw(Graphics2D g2, Translation translator)
	{
		if(this.prototype == null) return;
		
		if(!this.prototype.isActivated())
		{
			this.setVisible(false);
			return;
		}
		else this.setVisible(true);
			
		
		this.tail.setWay(this.prototype.getWay());
		
		synchronized(this.prototype)
		{
			if(this.drawBody) this.body.drawAt(g2, this.scheme, translator.translate(this.prototype.getPosition()));
		}
	}
	
	/* (non-Javadoc)
	 * @see gui.graphics.DrawableObject#resetSchemeIndices()
	 */
	@Override
	public void resetSchemeIndices()
	{
		this.fillColorIndex = GCentroid.STD_FILL_COLOR_INDEX;
		this.borderColorIndex = GCentroid.STD_BORDER_COLOR_INDEX;
		this.bodyStrokeIndex = GCentroid.STD_BODY_STROKE_INDEX;
	}

	public Centroid<double[]> getPrototype()
	{
		return this.prototype;
	}

	public void setPrototype(Centroid<double[]> prototype)
	{
		this.prototype = prototype;
	}

	/**
	 * @return the drawBody
	 */
	public boolean isDrawBody()
	{
		return this.drawBody;
	}

	/**
	 * @param drawBody the drawBody to set
	 */
	public void setDrawBody(boolean drawBody)
	{
		this.drawBody = drawBody;
	}


	/**
	 * @return the bodyTemp
	 */
	public BasicTemplate getBody()
	{
		return this.body;
	}

	/**
	 * @param bodyTemp the bodyTemp to set
	 */
	public void setBodyTemp(BasicTemplate body)
	{
		this.body = body;
	}


	/**
	 * @return the fillColorIndex
	 */
	public int getFillColorIndex()
	{
		return this.fillColorIndex;
	}

	/**
	 * @param fillColorIndex the fillColorIndex to set
	 */
	public void setFillColorIndex(int fillColorIndex)
	{
		this.fillColorIndex = fillColorIndex;
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
	 * @return the bodyStrokeIndex
	 */
	public int getBodyStrokeIndex()
	{
		return this.bodyStrokeIndex;
	}

	/**
	 * @param bodyStrokeIndex the bodyStrokeIndex to set
	 */
	public void setBodyStrokeIndex(int bodyStrokeIndex)
	{
		this.bodyStrokeIndex = bodyStrokeIndex;
	}
}
