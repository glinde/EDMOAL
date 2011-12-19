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

import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class GeomTemplate extends BasicTemplate implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= 1350900769170190893L;
	public static final int CUSTOM				 = 0;
	public static final int FILL_CIRCLE			 = 1;
	public static final int FILL_SQUARE			 = 2;
	public static final int FILL_CROSS			 = 3;
	public static final int FILL_RHOMBUS		 = 4;
	public static final int LINE_CROSS			 = 5;
	public static final int LINE_VERTICAL		 = 6;
	public static final int LINE_HORIZONTAL		 = 7;
	public static final int LINE_SHLASH			 = 8;
	public static final int LINE_BACKSHLASH		 = 9;

	protected ArrayList<ArrayList<double[]>> customPath;
	
	protected int shapeID;
	
	/**
	 * 
	 */
	public GeomTemplate()
	{
		this(GeomTemplate.FILL_CIRCLE);
	}

	/**
	 * @param id
	 */
	public GeomTemplate(int id)
	{
		super();
		
		this.customPath = new ArrayList<ArrayList<double[]>>();		
		
		this.shapeID = id;
		this.reshape();
	}
	
	/**
	 * @param customPath
	 */
	public GeomTemplate(ArrayList<ArrayList<double[]>> customPath)
	{
		super();
		this.customize(customPath);
		this.reshape();
	}
	
	public int getShapeID()
	{
		return this.shapeID;
	}
	
	public void setShape(int id)
	{
		this.reshapeNecessary = this.shapeID != id;
		this.shapeID = id;
	}
	
	/**
	 * @param cPath
	 */
	public void customize(List<? extends List<double[]>> cPath)
	{
		ArrayList<double[]> line;
		this.customPath = new ArrayList<ArrayList<double[]>>();
		for(List<double[]> vec:cPath)
		{
			line = new ArrayList<double[]>();
			for(double[] p:vec)
			{
				line.add(p.clone());
			}
			this.customPath.add(line);
		}
		this.shapeID = GeomTemplate.CUSTOM;
		this.reshapeNecessary = true;
	}
	
	public void reshape()
	{
		Path2D.Double path;
		int i, j;
				
		switch(this.shapeID)
		{
			case CUSTOM:
				path = new Path2D.Double();
				if(this.customPath.size() == 1)
				{
					path.moveTo(this.pixelSize*this.customPath.get(0).get(0)[0], this.pixelSize*this.customPath.get(0).get(0)[1]);
					for(j=1; j<this.customPath.get(0).size(); j++)
					{
						path.lineTo(this.pixelSize*this.customPath.get(0).get(j)[0], this.pixelSize*this.customPath.get(0).get(j)[1]);
					}
					path.closePath();
				}
				else
				{
					for(i=0; i<this.customPath.size(); i++)
					{
						path.moveTo(this.pixelSize*this.customPath.get(i).get(0)[0], this.pixelSize*this.customPath.get(i).get(0)[1]);
						for(j=1; j<this.customPath.get(i).size(); j++)
						{
							path.lineTo(this.pixelSize*this.customPath.get(i).get(j)[0], this.pixelSize*this.customPath.get(i).get(j)[1]);
						}
					}
				}
			break;
			case FILL_CIRCLE:
				this.symbol = new Ellipse2D.Double(-0.5d*this.pixelSize, -0.5d*this.pixelSize, this.pixelSize, this.pixelSize);
			break;
			case FILL_SQUARE:
				this.symbol = new Rectangle2D.Double(-0.5d*this.pixelSize, -0.5d*this.pixelSize, this.pixelSize, this.pixelSize);
			break;
			case FILL_CROSS:
				path = new Path2D.Double();
//				path.moveTo(-0.1d*this.size, -0.5d*this.size);
//				path.lineTo(0.1d*this.size, -0.5d*this.size);
//				path.lineTo(0.1d*this.size, -0.1d*this.size);
//				path.lineTo(0.5d*this.size, -0.1d*this.size);
//				path.lineTo(0.5d*this.size, 0.1d*this.size);
//				path.lineTo(0.1d*this.size, 0.1d*this.size);
//				path.lineTo(0.1d*this.size, 0.5d*this.size);
//				path.lineTo(-0.1d*this.size, 0.5d*this.size);
//				path.lineTo(-0.1d*this.size, 0.1d*this.size);
//				path.lineTo(-0.5d*this.size, 0.1d*this.size);
//				path.lineTo(-0.5d*this.size, -0.1d*this.size);
//				path.lineTo(-0.1d*this.size, -0.1d*this.size);
//				path.moveTo(-0.2d*this.size, -0.5d*this.size);
//				path.lineTo(0.2d*this.size, -0.5d*this.size);
//				path.lineTo(0.2d*this.size, -0.2d*this.size);
//				path.lineTo(0.5d*this.size, -0.2d*this.size);
//				path.lineTo(0.5d*this.size, 0.2d*this.size);
//				path.lineTo(0.2d*this.size, 0.2d*this.size);
//				path.lineTo(0.2d*this.size, 0.5d*this.size);
//				path.lineTo(-0.2d*this.size, 0.5d*this.size);
//				path.lineTo(-0.2d*this.size, 0.2d*this.size);
//				path.lineTo(-0.5d*this.size, 0.2d*this.size);
//				path.lineTo(-0.5d*this.size, -0.2d*this.size);
//				path.lineTo(-0.2d*this.size, -0.2d*this.size);
				path.moveTo(-0.15d*this.pixelSize, -0.5d*this.pixelSize);
				path.lineTo(0.15d*this.pixelSize, -0.5d*this.pixelSize);
				path.lineTo(0.15d*this.pixelSize, -0.15d*this.pixelSize);
				path.lineTo(0.5d*this.pixelSize, -0.15d*this.pixelSize);
				path.lineTo(0.5d*this.pixelSize, 0.15d*this.pixelSize);
				path.lineTo(0.15d*this.pixelSize, 0.15d*this.pixelSize);
				path.lineTo(0.15d*this.pixelSize, 0.5d*this.pixelSize);
				path.lineTo(-0.15d*this.pixelSize, 0.5d*this.pixelSize);
				path.lineTo(-0.15d*this.pixelSize, 0.15d*this.pixelSize);
				path.lineTo(-0.5d*this.pixelSize, 0.15d*this.pixelSize);
				path.lineTo(-0.5d*this.pixelSize, -0.15d*this.pixelSize);
				path.lineTo(-0.15d*this.pixelSize, -0.15d*this.pixelSize);
				path.closePath();
				this.symbol = path;
			break;
			case LINE_CROSS:
				path = new Path2D.Double();
				path.moveTo(-0.5d*this.pixelSize,  0.0d);
				path.lineTo( 0.5d*this.pixelSize,  0.0d);
				path.moveTo( 0.0d, -0.5d*this.pixelSize);
				path.lineTo( 0.0d,  0.5d*this.pixelSize);
				this.symbol = path;
			break;
			default:
				this.symbol = new Ellipse2D.Double(-0.5d*this.pixelSize, -0.5d*this.pixelSize, this.pixelSize, this.pixelSize);
		}
		
		this.reshapeNecessary = false;
	}
	
	
}
