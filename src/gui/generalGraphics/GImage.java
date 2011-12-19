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
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.Serializable;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class GImage extends DrawableObject implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= -3049018252109079727L;

	protected Image img;
	
	protected double[] upperLeftCorner;
	protected double[] lowerRightCorner;
	
	/**
	 * @param parent
	 */
	public GImage(DrawableObject parent)
	{
		this(null, new double[]{0.0d, 1.0d}, new double[]{1.0d, 0.0d}, parent);
	}

	/**
	 * @param img
	 * @param upperLeftCorner
	 * @param lowerRightCorner
	 */
	public GImage(Image img, double[] upperLeftCorner, double[] lowerRightCorner)
	{
		this(img, upperLeftCorner, lowerRightCorner, null);
	}
	
	/**
	 * @param img
	 * @param upperLeftCorner
	 * @param lowerRightCorner
	 */
	public GImage(Image img, double[] upperLeftCorner, double[] lowerRightCorner, DrawableObject parent)
	{
		super(parent);
		
		this.img = img;
		this.upperLeftCorner = upperLeftCorner;
		this.lowerRightCorner = lowerRightCorner;
	}

	/* (non-Javadoc)
	 * @see gui.DrawableObject#draw(java.awt.Graphics2D, gui.Translation)
	 */
	@Override
	protected void draw(Graphics2D g2, Translation translator)
	{
		double[] ulC, lrC;
		
		ulC = translator.translate(this.projection.project(this.upperLeftCorner, null));
		lrC = translator.translate(this.projection.project(this.lowerRightCorner, null));
		
		g2.drawImage(this.img, (int)ulC[0], (int)ulC[1], (int)(lrC[0]-ulC[0]), (int)(lrC[1]-ulC[1]), null);
	}

	/* (non-Javadoc)
	 * @see gui.DrawableObject#resetSchemeIndices()
	 */
	@Override
	public void resetSchemeIndices()
	{}

	/**
	 * @return the img
	 */
	public Image getImg()
	{
		return this.img;
	}

	/**
	 * @param img the img to set
	 */
	public void setImg(Image img)
	{
		this.img = img;
	}

	/**
	 * @return the upperLeftCorner
	 */
	public double[] getUpperLeftCorner()
	{
		return this.upperLeftCorner;
	}

	/**
	 * @param upperLeftCorner the upperLeftCorner to set
	 */
	public void setUpperLeftCorner(double[] upperLeftCorner)
	{
		this.upperLeftCorner = upperLeftCorner;
	}

	/**
	 * @return the lowerRightCorner
	 */
	public double[] getLowerRightCorner()
	{
		return this.lowerRightCorner;
	}

	/**
	 * @param lowerRightCorner the lowerRightCorner to set
	 */
	public void setLowerRightCorner(double[] lowerRightCorner)
	{
		this.lowerRightCorner = lowerRightCorner;
	}
	
	public void generateTestImage(double x, double y, double pixPerUnit, int width, int height)
	{
		BufferedImage bfImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		int i,j;
		
		for(i=0; i<width;i++)
		{
			for(j=0; j<height;j++)
			{
				if((i+j)%2==0)	bfImage.setRGB(i, j, 0xFFFFFFFF);
				else    	bfImage.setRGB(i, j, 0x00000000);
			}
		}
		
		this.upperLeftCorner = new double[]{x, y};
		this.lowerRightCorner = new double[]{x+width/pixPerUnit, y+height/pixPerUnit};
		this.img = bfImage;
	}
}
