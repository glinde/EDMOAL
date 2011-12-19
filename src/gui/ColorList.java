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

import java.awt.Color;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public interface ColorList
{
	// general purpose colors		
	// grays
	public static Color WHITE 			= new Color(255, 255, 255);
	public static Color BRIGHT2_GRAY 	= new Color(220, 220, 220);
	public static Color BRIGHT_GRAY 	= new Color(200, 200, 200);
	public static Color LIGHT_GRAY 		= new Color(150, 150, 150);
	public static Color GRAY 			= new Color(128, 128, 128);
	public static Color STRONG_GRAY 	= new Color(100, 100, 100);
	public static Color DARK_GRAY 		= new Color(50, 50, 50);
	public static Color BLACK 			= new Color(0, 0, 0);

	// pure colors
	public static Color BRIGHT2_RED		= new Color(255, 204, 204);
	public static Color BRIGHT2_GREEN	= new Color(210, 255, 200);
	public static Color BRIGHT2_BLUE	= new Color(200, 204, 255);
	
	public static Color BRIGHT_RED		= new Color(255, 150, 150);
	public static Color BRIGHT_GREEN	= new Color(158, 255, 150);
	public static Color BRIGHT_BLUE		= new Color(140, 150, 255);
	
	public static Color LIGHT_RED		= new Color(255, 80, 80);
	public static Color LIGHT_GREEN		= new Color(84, 255, 80);
	public static Color LIGHT_BLUE		= new Color(88, 90, 255);
	
	public static Color RED				= new Color(255, 0, 0);
	public static Color GREEN			= new Color(0, 255, 0);
	public static Color BLUE			= new Color(0, 0, 255);
		
	public static Color STRONG_RED		= new Color(200, 0, 0);
	public static Color STRONG_GREEN	= new Color(0, 200, 0);
	public static Color STRONG_BLUE		= new Color(0, 0, 200);

	public static Color DARK_RED		= new Color(120, 0, 0);
	public static Color DARK_GREEN		= new Color(0, 120, 0);
	public static Color DARK_BLUE		= new Color(0, 0, 120);

	// mixed colors
	public static Color BRIGHT_CYAN		= new Color(150, 255, 255);
	public static Color BRIGHT_MAGENTA	= new Color(255, 150, 255);
	public static Color BRIGHT_YELLOW	= new Color(255, 235, 150);

	public static Color LIGHT_CYAN		= new Color(80, 255, 255);
	public static Color LIGHT_MAGENTA	= new Color(255, 80, 255);
	public static Color LIGHT_YELLOW	= new Color(255, 235, 80);
	
	public static Color CYAN			= new Color(0, 255, 255);
	public static Color MAGENTA			= new Color(255, 0, 255);
	public static Color YELLOW			= new Color(255, 235, 0);
	
	public static Color STRONG_CYAN		= new Color(0, 200, 200);
	public static Color STRONG_MAGENTA	= new Color(200, 0, 200);
	public static Color STRONG_YELLOW	= new Color(200, 185, 0);
	
	public static Color DARK_CYAN		= new Color(0, 120, 120);
	public static Color DARK_MAGENTA	= new Color(120, 0, 120);
	public static Color DARK_YELLOW		= new Color(120, 110, 0);

	public static Color DARK_ORANGE		= new Color(150, 100, 0);
	public static Color STRONG_ORANGE	= new Color(200, 150, 0);
	public static Color ORANGE			= new Color(255, 190, 0);
	public static Color LIGHT_ORANGE	= new Color(255, 220, 100);
	public static Color BRIGHT_ORANGE	= new Color(255, 240, 150);
	
	
	public static Color[] clusterColors = new Color[]{
		ColorList.RED,			ColorList.GREEN,			ColorList.BLUE,		
		ColorList.YELLOW,		ColorList.MAGENTA,			ColorList.CYAN,					
		ColorList.STRONG_RED,	ColorList.STRONG_GREEN,		ColorList.STRONG_BLUE,					
		ColorList.STRONG_CYAN,	ColorList.STRONG_MAGENTA,	ColorList.STRONG_YELLOW,							
		ColorList.DARK_RED,		ColorList.DARK_GREEN,		ColorList.DARK_BLUE,					
		ColorList.LIGHT_RED,	ColorList.LIGHT_GREEN,		ColorList.LIGHT_BLUE,					
		ColorList.LIGHT_CYAN,	ColorList.LIGHT_MAGENTA,	ColorList.LIGHT_YELLOW,					
		ColorList.BRIGHT_RED,	ColorList.BRIGHT_GREEN,		ColorList.BRIGHT_BLUE,							
		ColorList.BRIGHT_CYAN,	ColorList.BRIGHT_MAGENTA,	ColorList.BRIGHT_YELLOW,
	};
	
	public static final int RGB_COLOR_MODEL = 0;
	public static final int YUV_COLOR_MODEL = 1;
	public static final int HSB_COLOR_MODEL = 2;
}
