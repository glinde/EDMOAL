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


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.Serializable;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class ScreenViewer extends JFrame implements WindowListener, Serializable
{
	/**  */
	private static final long serialVersionUID = -3740999700903157431L;

	/** */
	public Screen screen;
		
	/** */
	private JScrollPane scroller;

	/**
	 * 
	 */
	public ScreenViewer(int xRes, int yRes)
	{
		// this
		this.screen = new Screen();
		
		this.addKeyListener(screen);
		
		// JFrame
		this.setSize(xRes, yRes);
		this.setLocation(0, 0);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setVisible(true);
		this.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(xRes, yRes));
				
		// components
		this.scroller = new JScrollPane(this.screen);
		this.scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		this.scroller.setAutoscrolls(true);
		this.getContentPane().add(this.scroller, BorderLayout.CENTER);
		this.scroller.setVisible(true);
						
		this.repaint();
		this.pack();
	}
	
	/**
	 * 
	 */
	public ScreenViewer()
	{
		this(400, 400);
	}

	@Override
	public void windowActivated(WindowEvent e)
	{}

	@Override
	public void windowClosed(WindowEvent e)
	{
		this.screen = null;
		System.gc();
		try
		{
			Thread.sleep(200);
		} catch(InterruptedException e1)
		{
			e1.printStackTrace();
		}
	}

	@Override
	public void windowClosing(WindowEvent e)
	{}

	@Override
	public void windowDeactivated(WindowEvent e)
	{}

	@Override
	public void windowDeiconified(WindowEvent e)
	{}

	@Override
	public void windowIconified(WindowEvent e)
	{}

	@Override
	public void windowOpened(WindowEvent e)
	{}

	/* (non-Javadoc)
	 * @see java.awt.Window#setSize(int, int)
	 */
	@Override
	public void setSize(int width, int height)
	{
		super.setSize(width, height);
		
		this.screen.setSize(width-50, height-50);
	}

	/* (non-Javadoc)
	 * @see java.awt.Component#setPreferredSize(java.awt.Dimension)
	 */
	@Override
	public void setPreferredSize(Dimension preferredSize)
	{
		super.setPreferredSize(preferredSize);
		
		this.screen.setPreferredSize(new Dimension(preferredSize.width-50, preferredSize.height-50));
	}
	
		
}
