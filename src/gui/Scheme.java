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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class Scheme implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= -388466152216184413L;
	protected ArrayList<Color> colors;
	protected ArrayList<BasicStroke> strokes;
	protected ArrayList<Font> fonts;

	
	/**
	 * 
	 */
	public Scheme()
	{
		this.colors = new ArrayList<Color>();
		this.strokes = new ArrayList<BasicStroke>();
		this.fonts = new ArrayList<Font>();
	}

	/**
	 * @param colorColl
	 * @param s
	 */
	public Scheme(Collection<Color> colorColl, BasicStroke s)
	{
		this.colors = new ArrayList<Color>();
		this.colors.addAll(colorColl);
		this.strokes = new ArrayList<BasicStroke>();
		this.strokes.add(s);
		this.fonts = new ArrayList<Font>();
	}
	
	/**
	 * @param index
	 * @param alpha
	 */
	public void setColorAlpha(int index, int alpha)
	{
		Color c = this.colors.get(index);
		this.colors.set(index, new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha));
	}

	/**
	 * @param index
	 * @return
	 */
	public double getStrokeThickness(int index)
	{
		return this.strokes.get(index).getLineWidth();
	}

	/**
	 * @param index
	 * @param strokeThickness
	 * @return 
	 */
	public void setStrokeThickness(int index, double strokeThickness)
	{
		this.setStroke(index, new BasicStroke((float) strokeThickness, this.strokes.get(index).getEndCap(), this.strokes.get(index).getLineJoin()));
	}
	
	/**
	 * @param e
	 * @return
	 * @see java.util.ArrayList#add(java.lang.Object)
	 */
	public boolean addColor(Color e)
	{
		return this.colors.add(e);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.ArrayList#addAll(java.util.Collection)
	 */
	public boolean addAllColors(Collection<? extends Color> c)
	{
		return this.colors.addAll(c);
	}

	/**
	 * 
	 * @see java.util.ArrayList#clear()
	 */
	public void clearColors()
	{
		this.colors.clear();
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.ArrayList#get(int)
	 */
	public Color getColor(int index)
	{
		return this.colors.get(index);
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.ArrayList#remove(int)
	 */
	public Color removeColor(int index)
	{
		return this.colors.remove(index);
	}

	/**
	 * @param index
	 * @param element
	 * @return
	 * @see java.util.ArrayList#set(int, java.lang.Object)
	 */
	public Color setColor(int index, Color element)
	{
		return this.colors.set(index, element);
	}

	/**
	 * @param e
	 * @return
	 * @see java.util.ArrayList#add(java.lang.Object)
	 */
	public boolean addStroke(BasicStroke e)
	{
		return strokes.add(e);
	}
	

	/**
	 * @param c
	 * @return
	 * @see java.util.ArrayList#addAll(java.util.Collection)
	 */
	public boolean addAllStrokes(Collection<? extends BasicStroke> c)
	{
		return this.strokes.addAll(c);
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.ArrayList#get(int)
	 */
	public BasicStroke getStroke(int index)
	{
		return this.strokes.get(index);
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.ArrayList#remove(int)
	 */
	public BasicStroke removeStroke(int index)
	{
		return this.strokes.remove(index);
	}

	/**
	 * @param index
	 * @param element
	 * @return
	 * @see java.util.ArrayList#set(int, java.lang.Object)
	 */
	public BasicStroke setStroke(int index, BasicStroke element)
	{
		return this.strokes.set(index, element);
	}

	/**
	 * 
	 * @see java.util.ArrayList#clear()
	 */
	public void clearStrokes()
	{
		this.strokes.clear();
	}

	/**
	 * @param e
	 * @return
	 * @see java.util.ArrayList#add(java.lang.Object)
	 */
	public boolean addFont(Font e)
	{
		return this.fonts.add(e);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.ArrayList#addAll(java.util.Collection)
	 */
	public boolean addAllFonts(Collection<? extends Font> c)
	{
		return this.fonts.addAll(c);
	}

	/**
	 * 
	 * @see java.util.ArrayList#clear()
	 */
	public void clearFonts()
	{
		this.fonts.clear();
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.ArrayList#get(int)
	 */
	public Font getFont(int index)
	{
		return this.fonts.get(index);
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.ArrayList#remove(int)
	 */
	public Font removeFont(int index)
	{
		return this.fonts.remove(index);
	}

	/**
	 * @param index
	 * @param element
	 * @return
	 * @see java.util.ArrayList#set(int, java.lang.Object)
	 */
	public Font setFont(int index, Font element)
	{
		return this.fonts.set(index, element);
	}

	
}
