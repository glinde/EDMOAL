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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class Translation implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= -6755615276746498999L;

	protected double[] worldOffset;
	
	protected double zoom;
	protected double zoomInv;
	protected double minzoom;
	protected double maxzoom;
	
	protected double relativeZoomStepSize;

	/**
	 */
	public Translation()
	{
		this.worldOffset = new double[]{0.0d, 0.0d};
		this.zoom = 1.0d;
		this.zoomInv = 1.0d;
		
		this.minzoom = 0.0d;
		this.maxzoom = Double.MAX_VALUE;
				
		this.relativeZoomStepSize = 1.1d;
	}
//	
//	/**
//	 * @param offsetX
//	 * @param offsetY
//	 * @param zoom
//	 */
//	public Translation(double offsetX, double offsetY, double zoom)
//	{
//		this();
//		
//		this.worldOffset = new double[]{offsetX, offsetY};
//		this.zoom = zoom;
//		this.zoomInv = 1.0d/this.zoom;
//	}

	/**
	 * @param offset
	 * @param zoom
	 */
	public Translation(double[] offset, double zoom)
	{
		this();
		
		this.worldOffset = offset.clone();
		this.zoom = zoom;
		this.zoomInv = 1.0d/this.zoom;
	}
	
	public double[] translate(double[] p)
	{
		return new double[]{(p[0]-this.worldOffset[0])*this.zoom, -(p[1]-this.worldOffset[1])*this.zoom};	
	}

	public double scaleLength(double l)
	{
		return this.zoom*l;
	}
	
	public double translateX(double x)
	{
		return (x-this.worldOffset[0])*this.zoom;	
	}

	public double translateY(double y)
	{
		return -(y-this.worldOffset[1])*this.zoom;	
	}
	
	public Collection<double[]> translate(Collection<double []> worldPoints, Collection<double []> screenPoints)
	{
		if(screenPoints == null) screenPoints = new ArrayList<double[]>();
		
		for(double[] p:worldPoints)
		{
			screenPoints.add(this.translate(p));
		}
		
		return screenPoints;
	}
	
	public double[] inverseTranslate(double[] p)
	{
		return new double[]{p[0]*this.zoomInv + this.worldOffset[0], -(p[1])*this.zoomInv + this.worldOffset[1]};
	}

	
//	public Collection<double[]> inverseTranslate(Collection<double []> worldPoints, Collection<double []> screenPoints)
//	{
//		if(worldPoints == null) worldPoints = new Vector<double[]>();
//		
//		for(double[] p:screenPoints)
//		{
//			worldPoints.add(this.inverseTranslate(p));
//		}
//		
//		return worldPoints;
//	}
	
	
	public void moveScreen(double[] delta)
	{
		this.worldOffset[0] += delta[0]*this.zoomInv;
		this.worldOffset[1] -= delta[1]*this.zoomInv;
	}
	
	public void moveScreen(double dx, double dy)
	{
		this.worldOffset[0] += dx*this.zoomInv;
		this.worldOffset[1] -= dy*this.zoomInv;
	}

	public void moveOffset(double[] delta)
	{
		this.worldOffset[0] += delta[0];
		this.worldOffset[1] += delta[1];
	}
	
	public void moveOffset(double dx, double dy)
	{
		this.worldOffset[0] += dx;
		this.worldOffset[1] += dy;
	}
	
//	public void centerOnWorld(double[] center, int width, int height)
//	{
//		this.worldOffset[0] = center[0];
//		this.worldOffset[1] = center[1];
//		
//		this.moveScreen(-width/2, height/2);
//	}
//	
	/**
	 * @return the worldOffset
	 */
	public double[] getWorldOffset()
	{
		return this.worldOffset;
	}

	/**
	 * @param worldOffset the worldOffset to set
	 */
	public void setWorldOffset(double[] worldOffset)
	{
		this.worldOffset = worldOffset;
	}

	public void setZoom(double z)
	{
		if(z < this.minzoom)
		{
			z = this.minzoom;
		}
		if(z > this.maxzoom)
		{
			z = this.maxzoom;
		}
		
		this.zoom = z;
		this.zoomInv = 1.0d/z;
	}

	public void absoluteZoom(double z, double[] zoomToWorld)
	{
		double zoomOld = this.zoom, zoomBy;
		
		this.setZoom(z);
		
		if(zoomToWorld != null)
		{
			zoomBy = zoomOld/this.zoom;
			this.worldOffset[0] = zoomBy*this.worldOffset[0] + (1.0d - zoomBy) * zoomToWorld[0];
			this.worldOffset[1] = zoomBy*this.worldOffset[1] + (1.0d - zoomBy) * zoomToWorld[1];
		}
	}

	public void relativeZoom(double z, double[] zoomToWorld)
	{
		this.absoluteZoom(z*this.zoom, zoomToWorld);
	}

	public double getZoom()
	{
		return this.zoom;
	}
	
	public void increaseZoom(double[] zoomToWorld)
	{
		this.relativeZoom(this.relativeZoomStepSize, zoomToWorld);
	}

	public void decreaseZoom(double[] zoomToWorld)
	{
		this.relativeZoom(1.0d/this.relativeZoomStepSize, zoomToWorld);
	}	
		
	public double getMaxzoom()
	{
		return this.maxzoom;
	}

	public void setMaxzoom(double maxzoom)
	{
		if(this.minzoom < maxzoom) this.maxzoom = maxzoom;
		this.setZoom(this.zoom);
	}

	public double getMinzoom()
	{
		return this.minzoom;
	}

	public void setMinzoom(double minzoom)
	{
		if(this.maxzoom > minzoom) this.minzoom = minzoom;
		this.setZoom(this.zoom);
	}

	public double getRelativeZoomStepSize()
	{
		return this.relativeZoomStepSize;
	}

	public void setRelativeZoomStepSize(double relativeZoomStepSize)
	{
		this.relativeZoomStepSize = relativeZoomStepSize;
	}
}
