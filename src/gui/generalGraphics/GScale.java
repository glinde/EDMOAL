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

import gui.ColorList;
import gui.DrawableObject;
import gui.Scheme;
import gui.Translation;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class GScale extends DrawableObject implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= 8936486318751092361L;
	public final static int STD_LINE_COLOR_INDEX = 0;
	public final static int STD_BACKGROUND_COLOR_INDEX = 1;
	public final static int STD_LINE_STROKE_INDEX = 0;
	public final static int STD_BACKGROUND_STROKE_INDEX = 1;
	public final static int STD_FONT_INDEX = 0;
	
	protected int lineColorIndex;
	protected int backgroundColorIndex;
	protected int lineStrokeIndex;
	protected int backgroundStrokeIndex;
	protected int fontIndex;
	
	protected ArrayList<double[]> gridLengthLookup;
	protected int maxGridLength;
	protected double gridPixNM;
	protected double gridPixKM;
	protected double gridLengthNM;
	protected double gridLengthKM;
	protected double gridPartPixNM;
	protected double gridPartPixKM;
	protected int[] gridPosition;
	protected int gridThickness;

	
	protected boolean drawKM;
	protected boolean drawNM;
	

	public GScale()
	{		
		this(null);
	}
	
	public GScale(DrawableObject parent)
	{
		super(parent);		
		this.calculateGridLookup();
		
		this.maxGridLength = 500;
		this.gridPosition = new int[]{100, 900};
		
		this.scheme = new Scheme();
		this.scheme.addColor(ColorList.BLACK);
		this.scheme.addColor(ColorList.WHITE);
		this.scheme.addFont(new Font(Font.DIALOG, Font.CENTER_BASELINE, 16));
		this.scheme.addStroke(new BasicStroke(2, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
		this.scheme.addStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		
		this.resetSchemeIndices();
		this.scheme.setColorAlpha(this.backgroundColorIndex, 100);
		
		this.drawKM = true;
		this.drawNM = false;
	}
	
	/* (non-Javadoc)
	 * @see gui.DrawableObject#resetSchemeIndices()
	 */
	@Override
	public void resetSchemeIndices()
	{
		this.lineColorIndex = STD_LINE_COLOR_INDEX;
		this.backgroundColorIndex = STD_BACKGROUND_COLOR_INDEX;
		this.lineStrokeIndex = STD_LINE_STROKE_INDEX;
		this.backgroundStrokeIndex = STD_BACKGROUND_STROKE_INDEX;
		this.fontIndex = STD_FONT_INDEX;
	}

	
	protected void recalculateGrid(Translation translator)
	{
		int i;
				
		this.gridLengthNM = this.maxGridLength / (translator.getZoom() * 1.852d);
		this.gridLengthKM = this.maxGridLength / translator.getZoom();
		
		i=1;
		while(this.gridLengthNM > this.gridLengthLookup.get(i)[0]) i++; i--;
		this.gridPartPixNM = this.gridLengthLookup.get(i)[1];
		this.gridPixNM    = this.gridLengthLookup.get(i)[0];
		this.gridLengthNM  = this.gridLengthLookup.get(i)[0] / 1000;	

		i=1;
		while(this.gridLengthKM > this.gridLengthLookup.get(i)[0]) i++; i--;
		this.gridPartPixKM = this.gridLengthLookup.get(i)[1];
		this.gridPixKM = this.gridLengthLookup.get(i)[0];
		this.gridLengthKM = this.gridLengthLookup.get(i)[0];
				
		
		this.gridPixNM *= translator.getZoom() * 1.852d;
		this.gridPartPixNM *= translator.getZoom() * 1.852d;
		this.gridPixKM *= translator.getZoom();
		this.gridPartPixKM *= translator.getZoom();
	}
		
	protected void draw(Graphics2D g2, Translation translator)
	{
		double x;
		this.recalculateGrid(translator);
						
		Line2D.Double gridLineNM = new Line2D.Double(this.gridPosition[0], this.gridPosition[1]     , this.gridPosition[0] + this.gridPixNM, this.gridPosition[1]);
		Line2D.Double gridLineKM = new Line2D.Double(this.gridPosition[0], this.gridPosition[1] + 20, this.gridPosition[0] + this.gridPixKM, this.gridPosition[1] + 20);
		Rectangle2D.Double shadowRect = new Rectangle2D.Double(this.gridPosition[0]-10, this.gridPosition[1]-30, Math.max(this.gridPixNM, this.gridPixKM) + 80, 80);		
		
		g2.setColor(this.scheme.getColor(this.backgroundColorIndex));
		g2.setStroke(this.scheme.getStroke(this.backgroundStrokeIndex));	
		g2.fill(shadowRect);
		
		g2.setColor(this.scheme.getColor(this.lineColorIndex));
		g2.setStroke(this.scheme.getStroke(this.lineStrokeIndex));
		g2.setFont(this.scheme.getFont(this.fontIndex));
		
		if(this.drawNM)
		{
			g2.draw(gridLineNM);
			g2.drawString("0", (int)(this.gridPosition[0] - 2), (int)(this.gridPosition[1] - 10));
			for(x=0; x<=1.001d*this.gridPixNM; x+=this.gridPartPixNM)
			{
				g2.draw(new Line2D.Double(this.gridPosition[0] + x, this.gridPosition[1]- 3, this.gridPosition[0] + x, this.gridPosition[1] + 3));			
			}
			g2.drawString("" + this.gridLengthNM + " NM", (int)(this.gridPosition[0] + this.gridPixNM - 10), (int)(this.gridPosition[1] - 10));
		}
		
		if(this.drawKM)
		{		
			g2.draw(gridLineKM);
			g2.drawString("0", (int)(this.gridPosition[0] - 2), (int)(this.gridPosition[1] + 40));
			for(x=0.0d; x<=1.001d*gridPixKM; x+=gridPartPixKM)
			{
				g2.draw(new Line2D.Double(this.gridPosition[0] + x, this.gridPosition[1] + 17, this.gridPosition[0] + x, this.gridPosition[1] + 23));
			}
			g2.drawString("" + this.gridLengthKM + " ", (int)(this.gridPosition[0] + this.gridPixKM - 10), (int)(this.gridPosition[1] + 40));
		}
	}
	

	private void calculateGridLookup()
	{
		long i; 
		double x = 1E-8;
		
		this.gridLengthLookup = new ArrayList<double[]>();
		
		for(i=1; i<Long.MAX_VALUE/10;i*=10)
		{
			this.gridLengthLookup.add(new double[]{ 1*i*x, 0.2d*i*x});
			this.gridLengthLookup.add(new double[]{ 2*i*x, 0.5d*i*x});
			this.gridLengthLookup.add(new double[]{ 5*i*x, 1.0d*i*x});
		}
	}
}
