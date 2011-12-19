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


package gui;


import gui.projections.Orthogonal2DProjection;
import gui.projections.Projection;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public abstract class DrawableObject implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= 5762018076011063388L;
	protected boolean visible;	
	protected Scheme scheme;	
	protected ArrayList<DrawableObject> childElements;

	/**  */
	protected Projection projection;
	
	public DrawableObject()
	{
		this(null);
	}

	public DrawableObject(DrawableObject parent)
	{
		this.visible = true;
		if(parent != null)
		{
			this.scheme = parent.scheme;
			this.projection = parent.projection;
		}
		else
		{
			this.scheme = new Scheme();
			this.projection = new Orthogonal2DProjection();
			
			this.scheme.addColor(ColorList.RED);
			this.scheme.addColor(ColorList.BLACK);
			this.scheme.addStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			this.scheme.addStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		}
		this.childElements = new ArrayList<DrawableObject>();
	}

	protected abstract void draw(Graphics2D g2, Translation translator);
	
	public abstract void resetSchemeIndices();

	public void resetSchemeIndicesRecursive()
	{
		this.resetSchemeIndices();
		for(DrawableObject drawO:this.childElements)
			drawO.resetSchemeIndicesRecursive();
	}

	public final void drawRecursive(Graphics2D g2, Translation translator)
	{
		if(!this.visible) return;
		this.draw(g2, translator);
		for(DrawableObject drawO:this.childElements)
		{
			drawO.drawRecursive(g2, translator);
		}
	}
	
	public boolean isVisible()
	{
		return this.visible;
	}

	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}

	/**
	 * @return the scheme
	 */
	public Scheme getScheme()
	{
		return this.scheme;
	}

	/**
	 * @param scheme the scheme to set
	 */
	public void setScheme(Scheme scheme)
	{
		this.scheme = scheme;
	}

	/**
	 * @param scheme the scheme to set
	 */
	public void setSchemePropergated(Scheme scheme)
	{
		this.scheme = scheme;
		for(DrawableObject drawO:this.childElements)
		{
			drawO.setSchemePropergated(scheme);
		}
	}

	/**
	 * @param e
	 * @return
	 * @see java.util.Vector#add(java.lang.Object)
	 */
	public boolean addChild(DrawableObject e)
	{
		return this.childElements.add(e);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.Vector#addAll(java.util.Collection)
	 */
	public boolean addAllChildred(Collection<? extends DrawableObject> c)
	{
		return this.childElements.addAll(c);
	}

	/**
	 * 
	 * @see java.util.Vector#clear()
	 */
	public void clearChildred()
	{
		this.childElements.clear();
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.Vector#get(int)
	 */
	public DrawableObject getChild(int index)
	{
		return this.childElements.get(index);
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.Vector#remove(int)
	 */
	public DrawableObject removeChild(int index)
	{
		return this.childElements.remove(index);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.Vector#remove(java.lang.Object)
	 */
	public boolean removeChild(DrawableObject o)
	{
		return this.childElements.remove(o);
	}

	/**
	 * @return
	 * @see java.util.Vector#size()
	 */
	public int getChildCount()
	{
		return this.childElements.size();
	}

	/**
	 * @return the projection
	 */
	public Projection getProjection()
	{
		return this.projection;
	}

	/**
	 * @param projection the projection to set
	 */
	public void setProjection(Projection projection)
	{
		this.projection = projection;
	}
	
	
}
