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
import gui.templates.BasicTemplate;
import gui.templates.GeomTemplate;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class GPath extends DrawableObject implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= -5572636318280816890L;
	public final static int STD_TAIL_COLOR_INDEX = 0;
	public final static int STD_BORDER_COLOR_INDEX = 1;
	public final static int STD_TAIL_STROKE_INDEX = 0;
	public final static int STD_BORDER_STROKE_INDEX = 1;
	
	protected BasicTemplate waypointTemp;
	
	protected boolean drawWay;
	protected boolean drawWaypoints;
	
	protected ArrayList<double[]> way;
	
	protected int tailColorIndex;
	protected int waypointBorerColorIndex;
	protected int tailStrokeIndex;
	protected int borderStrokeIndex;
	protected boolean drawTailBackgroundGray;
	
	protected boolean closed;
	
	public GPath()
	{
		this(null);
	}
	
	public GPath(DrawableObject parent)
	{
		super(parent);
		this.drawWay = true;
		this.closed = false;
		this.drawTailBackgroundGray = true;

		this.resetSchemeIndices();
		
		this.way = new ArrayList<double[]>();
		
		// the tail elements of the protoype
		this.waypointTemp = new GeomTemplate(GeomTemplate.FILL_CIRCLE);
		this.waypointTemp.setPixelSize(9.0d);
		this.waypointTemp.setInternalColorIndex(this.tailColorIndex);
		this.waypointTemp.setBorderColorIndex(this.waypointBorerColorIndex);
		this.waypointTemp.setStrokeIndex(this.borderStrokeIndex);
		this.waypointTemp.setDrawBorder(true);
		this.waypointTemp.setDrawInternal(true);
		this.drawWaypoints = true;
	}
	

	/* (non-Javadoc)
	 * @see gui.DrawableObject#draw(java.awt.Graphics2D, gui.Translation)
	 */
	@Override
	protected void draw(Graphics2D g2, Translation translator)
	{
		Path2D.Double tail;
		ArrayList<double[]> screenWay;
//		double[] p;
		int i;
		
		if(this.way == null || this.way.size() == 0) return;
		
		screenWay = new ArrayList<double[]>(this.way.size());
		
		for(double[] p:this.way) screenWay.add(translator.translate(this.projection.project(p, null)));
		
		if(this.drawWay)
		{
			tail = new Path2D.Double();
			tail.moveTo(screenWay.get(0)[0], screenWay.get(0)[1]);
			for(i=1; i<screenWay.size();i++)
			{
				tail.lineTo(screenWay.get(i)[0], screenWay.get(i)[1]);
			}
			if(this.closed) tail.closePath();

			if(this.drawTailBackgroundGray)
			{
				g2.setColor(Color.GRAY);
				BasicStroke backgroundStroke = new BasicStroke(this.scheme.getStroke(this.tailStrokeIndex).getLineWidth()+1.0f, this.scheme.getStroke(this.tailStrokeIndex).getEndCap(), this.scheme.getStroke(this.tailStrokeIndex).getLineJoin());
				g2.setStroke(backgroundStroke);
				g2.draw(tail);
			}
			
			g2.setColor(this.scheme.getColor(this.tailColorIndex));
			g2.setStroke(this.scheme.getStroke(this.tailStrokeIndex));
			g2.draw(tail);
			
		}
		if(this.drawWaypoints) this.waypointTemp.drawAtAll(g2, this.scheme, screenWay);
	}

	/* (non-Javadoc)
	 * @see gui.DrawableObject#resetSchemeIndices()
	 */
	@Override
	public void resetSchemeIndices()
	{
		this.tailColorIndex = GPath.STD_TAIL_COLOR_INDEX;
		this.waypointBorerColorIndex = GPath.STD_BORDER_COLOR_INDEX;
		this.tailStrokeIndex = GPath.STD_TAIL_STROKE_INDEX;
		this.borderStrokeIndex = GPath.STD_BORDER_STROKE_INDEX;
	}

	/**
	 * @return the waypointTemp
	 */
	public BasicTemplate getWaypointTemp()
	{
		return this.waypointTemp;
	}

	/**
	 * @param waypointTemp the waypointTemp to set
	 */
	public void setWaypointTemp(BasicTemplate waypointTemp)
	{
		this.waypointTemp = waypointTemp;
	}

	/**
	 * @return the drawWay
	 */
	public boolean isDrawWay()
	{
		return this.drawWay;
	}

	/**
	 * @param drawWay the drawWay to set
	 */
	public void setDrawWay(boolean drawWay)
	{
		this.drawWay = drawWay;
	}

	/**
	 * @return the drawWaypoints
	 */
	public boolean isDrawWaypoints()
	{
		return this.drawWaypoints;
	}

	/**
	 * @param drawWaypoints the drawWaypoints to set
	 */
	public void setDrawWaypoints(boolean drawWaypoints)
	{
		this.drawWaypoints = drawWaypoints;
	}

	/**
	 * @return the tailStrokeIndex
	 */
	public int getTailStrokeIndex()
	{
		return this.tailStrokeIndex;
	}

	/**
	 * @param tailStrokeIndex the tailStrokeIndex to set
	 */
	public void setTailStrokeIndex(int tailStrokeIndex)
	{
		this.tailStrokeIndex = tailStrokeIndex;
	}

	/**
	 * @return the way
	 */
	public ArrayList<double[]> getWay()
	{
		return this.way;
	}

	/**
	 * @param way the way to set
	 */
	public void setWay(Collection<double[]> way)
	{
		this.way = new ArrayList<double[]>();
		this.way.addAll(way);
	}

	/**
	 * @return the tailColorIndex
	 */
	public int getTailColorIndex()
	{
		return this.tailColorIndex;
	}

	/**
	 * @param tailColorIndex the tailColorIndex to set
	 */
	public void setTailColorIndex(int tailColorIndex)
	{
		this.tailColorIndex = tailColorIndex;
	}



	/**
	 * @return the waypointBorerColorIndex
	 */
	public int getWaypointBorerColorIndex()
	{
		return this.waypointBorerColorIndex;
	}



	/**
	 * @param waypointBorerColorIndex the waypointBorerColorIndex to set
	 */
	public void setWaypointBorerColorIndex(int waypointBorerColorIndex)
	{
		this.waypointBorerColorIndex = waypointBorerColorIndex;
	}

	/**
	 * @return the closed
	 */
	public boolean isClosed()
	{
		return this.closed;
	}

	/**
	 * @param closed the closed to set
	 */
	public void setClosed(boolean closed)
	{
		this.closed = closed;
	}

	public boolean isDrawTailBackgroundGray() {
		return drawTailBackgroundGray;
	}

	public void setDrawTailBackgroundGray(boolean drawTailBackgroundGray) {
		this.drawTailBackgroundGray = drawTailBackgroundGray;
	}
	
	
}
