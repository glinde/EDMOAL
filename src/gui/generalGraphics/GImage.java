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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;

import data.objects.matrix.DoubleMatrix;
import data.objects.matrix.FeatureSpaceSampling2D;
import etc.DataManipulator;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class GImage extends DrawableObject implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= -3049018252109079727L;

	protected BufferedImage img;
	
	protected double[] lowerLeftCorner;
	protected double[] upperRightCorner;
	
	/**
	 * @param parent
	 */
	public GImage(DrawableObject parent)
	{
		this(null, new double[]{0.0d, 0.0d}, new double[]{1.0d, 1.0d}, parent);
	}
	
	/**
	 * @param parent
	 */
	public GImage(DrawableObject parent, FeatureSpaceSampling2D featureSampling)
	{
		this(null, featureSampling.getLowerLeftCorner(), featureSampling.getUpperRightCorner(), parent);
		
		this.setImageData(featureSampling, 0x00FF00, 0xFF0000);
	}

	/**
	 * @param img
	 * @param upperLeftCorner
	 * @param lowerRightCorner
	 */
	public GImage(BufferedImage img, double[] upperLeftCorner, double[] lowerRightCorner)
	{
		this(img, upperLeftCorner, lowerRightCorner, null);
	}
	
	/**
	 * @param img
	 * @param lowerLeftCorner
	 * @param upperRightCorner
	 */
	public GImage(BufferedImage img, double[] lowerLeftCorner, double[] upperRightCorner, DrawableObject parent)
	{
		super(parent);
		
		this.img = img;
		this.lowerLeftCorner = lowerLeftCorner;
		this.upperRightCorner = upperRightCorner;
	}

	/* (non-Javadoc)
	 * @see gui.DrawableObject#draw(java.awt.Graphics2D, gui.Translation)
	 */
	@Override
	protected void draw(Graphics2D g2, Translation translator)
	{
		double[] ulC, lrC;
		
		ulC = translator.translate(this.projection.project(this.lowerLeftCorner, null));
		lrC = translator.translate(this.projection.project(this.upperRightCorner, null));
		
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
	public BufferedImage getImg()
	{
		return this.img;
	}

	/**
	 * @param img the img to set
	 */
	public void setImg(BufferedImage img)
	{
		this.img = img;
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
		
		this.lowerLeftCorner = new double[]{x, y};
		this.upperRightCorner = new double[]{x+width/pixPerUnit, y+height/pixPerUnit};
		this.img = bfImage;
	}
	
	public void setImageData(DoubleMatrix matrix, int rgbMin, int rgbMax)
	{
		BufferedImage bfImage = new BufferedImage(matrix.sizeX(), matrix.sizeY(), BufferedImage.TYPE_INT_RGB);
		
		double value;
		int red=0, green=0, blue=0;
		
		for(int x=0; x<matrix.sizeX(); x++)
		{
			for(int y=0; y<matrix.sizeY(); y++)
			{
				value = matrix.get(x, y);
				
				red   = (int)(value*((rgbMax & 0x00FF0000)>>>16) + (1.0d - value)*((rgbMin & 0x00FF0000)>>>16))<<16 & 0xFF0000;
				green = (int)(value*((rgbMax & 0x0000FF00)>>>8)	 + (1.0d - value)*((rgbMin & 0x0000FF00)>>>8))<<8 & 0x00FF00;
				blue  = (int)(value*((rgbMax & 0x000000FF))		 + (1.0d - value)*((rgbMin & 0x000000FF))) & 0x0000FF;
				
				bfImage.setRGB(x, y, red+green+blue);
			}
		}
		
		this.img = bfImage;
	}
	
	public void addHeightLines(DoubleMatrix matrix, double mapHeight, int lineWidth, Color col)
	{
		if(this.img == null || this.img.getWidth() < matrix.sizeX() || this.img.getHeight() < matrix.sizeY())
		{
			this.img = new BufferedImage(matrix.sizeX(), matrix.sizeY(), BufferedImage.TYPE_INT_RGB);
			for(int x=0; x<matrix.sizeX(); x++) for(int y=0; y<matrix.sizeY(); y++) this.img.setRGB(x, y, 0xFFFFFF);
		}
		
		for(int x=1; x<matrix.sizeX()-1; x++)
		{
			for(int y=1; y<matrix.sizeY()-1; y++)
			{
				if(matrix.get(x, y) >= mapHeight)
				{
					if(	matrix.get(x-1,	y+1)	< mapHeight ||	matrix.get(x,	y+1)	< mapHeight ||	matrix.get(x+1, y+1)	< mapHeight || 
						matrix.get(x-1,	y)		< mapHeight ||											matrix.get(x+1, y)		< mapHeight ||
						matrix.get(x-1,	y-1)	< mapHeight ||	matrix.get(x,	y-1)	< mapHeight ||	matrix.get(x+1, y-1)	< mapHeight)
//					if(											matrix.get(x,	y+1)	< mapHeight ||
//						matrix.get(x-1,	y)		< mapHeight ||											matrix.get(x+1, y)		< mapHeight ||
//																matrix.get(x,	y-1)	< mapHeight)
					{
						for(int r=Math.max(x-lineWidth+1, 0); r<x+lineWidth && r<matrix.sizeX(); r++)
						{
							for(int s=Math.max(y-lineWidth+1, 0); s<y+lineWidth && s<matrix.sizeY(); s++)
							{
								if((x-r)*(x-r)+(y-s)*(y-s) <= (lineWidth-1)*(lineWidth-1) && matrix.get(r, s) >= mapHeight) this.img.setRGB(r, s, col.getRGB());	
							}
						}
					}
				}
			}
		}
	}

	public void fillAboveHeight(DoubleMatrix matrix, double mapHeight, Color col)
	{
		if(this.img == null || this.img.getWidth() < matrix.sizeX() || this.img.getHeight() < matrix.sizeY())
		{
			this.img = new BufferedImage(matrix.sizeX(), matrix.sizeY(), BufferedImage.TYPE_INT_RGB);
			for(int x=0; x<matrix.sizeX(); x++) for(int y=0; y<matrix.sizeY(); y++) this.img.setRGB(x, y, 0xFFFFFF);
		}
		
		int rgb = col.getRGB();
		double value = col.getAlpha()/255.0d;
		int red   = (int)(value*255 + (1.0d - value)*((rgb & 0x00FF0000)>>>16))<<16 & 0xFF0000;
		int green = (int)(value*255	+ (1.0d - value)*((rgb & 0x0000FF00)>>>8))<<8 & 0x00FF00;
		int blue  = (int)(value*255	+ (1.0d - value)*((rgb & 0x000000FF))) & 0x0000FF;
		rgb = red + green + blue;
		
		for(int x=1; x<matrix.sizeX()-1; x++)
		{
			for(int y=1; y<matrix.sizeY()-1; y++)
			{
				if(matrix.get(x, y) >= mapHeight)
				{
					this.img.setRGB(x, y, rgb);
				}
			}
		}
	}
	
	private ArrayList<double[][]> extractColorChannelsRGB(BufferedImage image)
	{
		ArrayList<double[][]> channels = new ArrayList<double[][]>();
		
		double[][] red = new double[image.getWidth()][image.getHeight()];	
		double[][] green = new double[image.getWidth()][image.getHeight()];	
		double[][] blue = new double[image.getWidth()][image.getHeight()];
		
		for(int x=0; x<image.getWidth(); x++)
		{
			for(int y=0; y<image.getHeight(); y++)
			{
				red[x][y] = ((double)((image.getRGB(x, y)&0x00FF0000)>>16))/255.0d;
				green[x][y] = ((double)((image.getRGB(x, y)&0x0000FF00)>>8))/255.0d;
				blue[x][y] = ((double)(image.getRGB(x, y)&0x000000FF))/255.0d;
			}
		}
		
		channels.add(red);
		channels.add(green);
		channels.add(blue);
				
		return channels;
	}

	private BufferedImage combineColorChannelsRGB(double[][] red, double[][] green, double[][] blue)
	{		
		BufferedImage image = new BufferedImage(red.length, red[0].length, BufferedImage.TYPE_INT_RGB);
		
		int rgb = 0;
		
		for(int x=0; x<image.getWidth(); x++)
		{
			for(int y=0; y<image.getHeight(); y++)
			{
				rgb = 0;
				rgb |= (((int)(255.0d*red[x][y]))&0x000000FF)<<16;
				rgb |= (((int)(255.0d*green[x][y]))&0x000000FF)<<8;
				rgb |= (((int)(255.0d*blue[x][y]))&0x000000FF);
				
				image.setRGB(x, y, rgb);
			}
		}
		
		return image;
	}
	
	
	public void gaussFilterImage(int radius)
	{
		ArrayList<double[][]> channels = this.extractColorChannelsRGB(this.img);

		double[][] red = channels.get(0);	
		double[][] green = channels.get(1);	
		double[][] blue = channels.get(2);
		
		DataManipulator.gaussFilter(red, radius);
		DataManipulator.gaussFilter(green, radius);
		DataManipulator.gaussFilter(blue, radius);
		
		this.img = this.combineColorChannelsRGB(red, green, blue);
	}
	
	
	/**
	 * @return the lowerLeftCorner
	 */
	public double[] getLowerLeftCorner()
	{
		return this.lowerLeftCorner;
	}

	/**
	 * @param lowerLeftCorner the lowerLeftCorner to set
	 */
	public void setLowerLeftCorner(double[] lowerLeftCorner)
	{
		this.lowerLeftCorner = lowerLeftCorner;
	}

	/**
	 * @return the upperRightCorner
	 */
	public double[] getUpperRightCorner()
	{
		return this.upperRightCorner;
	}

	/**
	 * @param upperRightCorner the upperRightCorner to set
	 */
	public void setUpperRightCorner(double[] upperRightCorner)
	{
		this.upperRightCorner = upperRightCorner;
	}
	
	
}
