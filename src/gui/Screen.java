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

import io.BatikExport;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JPanel;

import data.set.IndexedDataObject;
import etc.StringService;


/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class Screen extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, Serializable
{
	/**  */
	private static final long serialVersionUID = 6102714248547892821L;

	protected String fileName;

	protected Point lastMousePosition;
	protected boolean zoomToMouse;
	protected Translation translator;
	
	protected ArrayList<DrawableObject> drawList;
	
	/** */
	public Screen()
	{		
		this.setSize(new Dimension(1000, 1000));

		this.translator = new Translation();
		this.translator.setZoom(Math.min(this.getWidth(), this.getHeight()));
//		this.translator.moveScreen(-this.getWidth()>>1, -this.getHeight()>>1);
		
		
		this.drawList = new ArrayList<DrawableObject>();
		
		this.zoomToMouse = true;
		
		// layout
		this.setLayout(null);		
		this.setVisible(true);
				
		// paint area
		this.setBackground(Color.WHITE);
		
		this.setBorder(null);
		
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addMouseWheelListener(this);
		this.addKeyListener(this);
		
		this.lastMousePosition = new Point(0,0);
		this.fileName = "screen_" + StringService.getTimeFormatted();
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g)
	{
		super.paintComponent(g);
		
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		synchronized(this.drawList)
		{
			for(DrawableObject d:this.drawList) d.drawRecursive(g2, this.translator);
		}
		
		g2.setColor(Color.BLACK);
	}
	
	/**
	 * 
	 */
	public void centerScreenToWorldOrigin()
	{		
//		System.out.println("center = " + StringOperations.arrayToString(translator.inverseTranslate(new double[]{0.0d, 0.0d})));
//		this.translator.setWorldOffset(translator.inverseTranslate(new double[]{-this.getWidth()>>1, -this.getHeight()>>1}));
//		System.out.println("center = " + StringOperations.arrayToString(translator.inverseTranslate(new double[]{0.0d, 0.0d})));
		this.centerScreenTo(new double[]{0.0d, 0.0d});
	}

	/**
	 * @param p
	 */
	public void centerScreenTo(double[] p)
	{		
		this.translator.setWorldOffset(new double[]{0.0d, 0.0d});
		double[] screenCenter = translator.inverseTranslate(new double[]{-this.getWidth()>>1, -this.getHeight()>>1});
		screenCenter[0] += p[0];
		screenCenter[1] += p[1];
//		System.out.println("center = " + StringOperations.arrayToString(translator.inverseTranslate(new double[]{0.0d, 0.0d})));
		this.translator.setWorldOffset(screenCenter);
//		System.out.println("center = " + StringOperations.arrayToString(translator.inverseTranslate(new double[]{0.0d, 0.0d})));
	}
	
	/**
	 * @param p
	 */
	public void zoomToDisplay(double[] p)
	{
		double screenH = this.getHeight()*0.5d;
		double screenW = this.getWidth()*0.5d;
//		System.out.println("width x height = " + this.getWidth() + " x " + this.getHeight());
		double[] screenCenter = this.translator.inverseTranslate(new double[]{screenW, screenH});
		double newZoom = 0.0d;

//		System.out.println("screen center world coordinates = " + StringOperations.arrayToString(screenCenter));
//		System.out.println("screen p coordinates = " + StringOperations.arrayToString(this.translator.translate(p)));
		
		
		if(p[0]-this.translator.getWorldOffset()[0] == 0.0d && p[1]-this.translator.getWorldOffset()[1] == 0.0d)
		{
			return;
		}
		else if(p[0]-this.translator.getWorldOffset()[0] == 0.0d)
		{
			newZoom = screenH/(Math.abs(p[1]-screenCenter[1]));
		}
		else if(p[1]-this.translator.getWorldOffset()[1] == 0.0d)
		{
			newZoom = screenW/(Math.abs(p[0]-screenCenter[0]));
		}
		else
		{
			newZoom = Math.min(screenW/(Math.abs(p[0]-screenCenter[0])), screenH/(Math.abs(p[1]-screenCenter[1])));
		}
		
		newZoom *= 0.95d;
		
//		System.out.println("zoom = " + this.translator.getZoom());
		this.translator.absoluteZoom(newZoom, screenCenter);
//		System.out.println("zoom = " + this.translator.getZoom());
		
//		screenCenter = this.translator.inverseTranslate(new double[]{screenW, screenH});

//		System.out.println("screen center world coordinates = " + StringOperations.arrayToString(screenCenter));
//		System.out.println("screen p coordinates = " + StringOperations.arrayToString(this.translator.translate(p)));
	}
	
	/**
	 * @param pCol
	 */
	public void zoomToDisplay(Collection<double[]> pCol)
	{
		double screenH = this.getHeight()*0.5d;
		double screenW = this.getWidth()*0.5d;
		double[] screenCenter = this.translator.inverseTranslate(new double[]{screenW, screenH});
		
		double[] farestPoint = screenCenter.clone();
		
		for(double[] p:pCol)
		{
			if(Math.abs(p[0]-screenCenter[0]) > Math.abs(farestPoint[0]-screenCenter[0]))
			{
				farestPoint[0] = p[0];
			}
			if(Math.abs(p[1]-screenCenter[1]) > Math.abs(farestPoint[1]-screenCenter[1]))
			{
				farestPoint[1] = p[1];
			}
		}
		
		zoomToDisplay(farestPoint);
	}

	/**
	 * @param pCol
	 */
	public void setScreenToDisplayAll(Collection<double[]> pCol)
	{
//		double screenH = this.getHeight()*0.5d;
//		double screenW = this.getWidth()*0.5d;
//		double[] screenCenter = this.translator.inverseTranslate(new double[]{screenW, screenH});
		boolean first = true;
		
		double[] upperRight = null;
		double[] lowerLeft = null;
		double[] center = new double[]{0.0d, 0.0d};
		
		if(pCol.size() == 0) return;
		
		for(double[] p:pCol)
		{
			if(first)
			{
				upperRight = p.clone();
				lowerLeft = p.clone();
				if(pCol.size()==1)
				{
					this.centerScreenTo(p);
					return;
				}
				
				first = false;
				continue;
			}
			
			if(p[0] > upperRight[0]) upperRight[0] = p[0];
			if(p[1] > upperRight[1]) upperRight[1] = p[1];
			if(p[0] < lowerLeft[0]) lowerLeft[0] = p[0];
			if(p[1] < lowerLeft[1]) lowerLeft[1] = p[1];
		}
		
		center[0] = 0.5d*(upperRight[0]+lowerLeft[0]);
		center[1] = 0.5d*(upperRight[1]+lowerLeft[1]);
		
		this.centerScreenTo(center);		
//		this.zoomToDisplay(upperRight);	
		this.zoomToDisplay(lowerLeft);
	}

	/**
	 * @param pCol
	 */
	public void setScreenToDisplayAllIndexed(Collection<IndexedDataObject<double[]>> pCol)
	{
//		double screenH = this.getHeight()*0.5d;
//		double screenW = this.getWidth()*0.5d;
//		double[] screenCenter = this.translator.inverseTranslate(new double[]{screenW, screenH});
		boolean first = true;
		
		double[] upperRight = null;
		double[] lowerLeft = null;
		double[] center = new double[]{0.0d, 0.0d};
		
		if(pCol.size() == 0) return;
		
		for(IndexedDataObject<double[]> p:pCol)
		{
			if(first)
			{
				upperRight = p.element.clone();
				lowerLeft = p.element.clone();
				if(pCol.size()==1)
				{
					this.centerScreenTo(p.element);
					return;
				}
				
				first = false;
				continue;
			}
			
			if(p.element[0] > upperRight[0]) upperRight[0] = p.element[0];
			if(p.element[1] > upperRight[1]) upperRight[1] = p.element[1];
			if(p.element[0] < lowerLeft[0]) lowerLeft[0] = p.element[0];
			if(p.element[1] < lowerLeft[1]) lowerLeft[1] = p.element[1];
		}
		
		center[0] = 0.5d*(upperRight[0]+lowerLeft[0]);
		center[1] = 0.5d*(upperRight[1]+lowerLeft[1]);
		
		this.centerScreenTo(center);		
//		this.zoomToDisplay(upperRight);	
		this.zoomToDisplay(lowerLeft);
	}

	/**
	 * @param path
	 * @param name
	 * @param type
	 */
	public void screenshot(String name)
	{ 	 
		if(name == null) name = "screenshot_"+StringService.getTimeFormatted();

		System.out.println("screenshot: " + name);
		
		
		try
		{
			BatikExport.pngExport(this, fileName);
			BatikExport.jpgExport(this, fileName);
			BatikExport.svgpdfExport(this, fileName);
		}
		catch(HeadlessException e)
		{
			e.printStackTrace();
		}
		
		System.out.println("screenshot finished");
	}
		
	public void mouseDragged(MouseEvent e)
	{
		if((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0)
		{
			this.translator.moveScreen(this.lastMousePosition.x - e.getX(), this.lastMousePosition.y - e.getY());

			this.lastMousePosition = e.getPoint();
			this.repaint();
		}
	}

	public void mouseMoved(MouseEvent e)
	{
//		System.out.println(StringOperations.arrayToString(this.translator.inverseTranslate(new double[]{e.getPoint().x, e.getPoint().y})));
	}
	public void mouseClicked(MouseEvent e)	{}
	public void mouseEntered(MouseEvent e)	{}
	public void mouseExited(MouseEvent e)	{}
	public void mousePressed(MouseEvent e)	
	{
		if(e.getButton() == MouseEvent.BUTTON1)
		{
			this.lastMousePosition = e.getPoint();
		}
	}

	public void mouseReleased(MouseEvent e)	{}
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		double[] zoomTo = null;
		
		if(e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL)
		{
			if(this.zoomToMouse)
			{
				zoomTo = this.translator.inverseTranslate(new double[]{e.getX(), e.getY()});
			}
			
			if(e.getWheelRotation() > 0)
			{
				if((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0)
					this.translator.relativeZoom(0.5d, zoomTo);
				else this.translator.decreaseZoom(zoomTo);
			}
			else if(e.getWheelRotation() < 0)
			{
				if((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0)
					this.translator.relativeZoom(2.0d, zoomTo);
				this.translator.increaseZoom(zoomTo);
			}
			this.repaint();
		}
	}

	public String getFileName()
	{
		return this.fileName;
	}

	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}

	public void keyPressed(KeyEvent e)
	{
		System.out.println(e.getKeyChar());
		
		if(e.getKeyChar() == 'S' || e.getKeyChar() == 's')
		{
			this.screenshot(null);
		}
	}

	public void keyReleased(KeyEvent e)	{}
	public void keyTyped(KeyEvent e){}

	public boolean addDrawableObject(DrawableObject e)
	{
		return this.drawList.add(e);
	}

	public boolean addAllDrawableObjects(Collection<? extends DrawableObject> c)
	{
		return this.drawList.addAll(c);
	}

	public boolean removeDrawableObject(DrawableObject o)
	{
		return this.drawList.remove(o);
	}

	public Translation getTranslator()
	{
		return this.translator;
	}

	public void setTranslator(Translation translator)
	{
		this.translator = translator;
	}
}
