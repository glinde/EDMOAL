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


package io;

import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;

import javax.imageio.ImageIO;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.fop.svg.PDFTranscoder;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class BatikExport implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= -7953731640917591482L;

	public static String defaultFilePath = (new File("")).getAbsolutePath().replace('\\', '/') + "/";
	
	public static final String defaultSVGFilePath = BatikExport.defaultFilePath + "svg/";
	public static final String defaultPDFFilePath = BatikExport.defaultFilePath + "pdf/";
	public static final String defaultJPGFilePath = BatikExport.defaultFilePath + "jpg/";
	public static final String defaultPNGFilePath = BatikExport.defaultFilePath + "png/";
	
	private static final String svgNS = "http://www.w3.org/2000/svg";
	
	public static String svgFilePath = BatikExport.defaultSVGFilePath;
	public static String pdfFilePath = BatikExport.defaultPDFFilePath;
	public static String pngFilePath = BatikExport.defaultPNGFilePath;
	public static String jpgFilePath = BatikExport.defaultJPGFilePath;
	
	
	public static void createFolders()
	{
		File f;
		f = new File(BatikExport.defaultSVGFilePath); if(!f.exists()) f.mkdir();
		f = new File(BatikExport.defaultPDFFilePath); if(!f.exists()) f.mkdir();
		f = new File(BatikExport.defaultJPGFilePath); if(!f.exists()) f.mkdir();
		f = new File(BatikExport.defaultPNGFilePath); if(!f.exists()) f.mkdir();
	}
	
	public static boolean svgExport(Component scr, String filename)
	{		
		System.out.println("svg Export "+filename);
		BatikExport.createFolders();
		
		Writer wr;
		FileOutputStream fos;

		// create output stream
		DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
		Document document = domImpl.createDocument(BatikExport.svgNS, "svg", null);
		SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
		File outputFile = new File(BatikExport.svgFilePath + filename + ((filename.endsWith(".svg"))?"":".svg"));
		
		try
		{
			Thread.sleep(1000);
	        scr.paint(svgGenerator);
			Thread.sleep(1000);
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
			return false;
		}
       
		try
		{
			fos = new FileOutputStream(outputFile);
			wr = new OutputStreamWriter(fos, "UTF-8");
			svgGenerator.stream(wr, false);
		}
		catch(IOException  e)
		{
			e.printStackTrace();	
			return false;
		}

		System.out.println("svg Export finished");
        return true;
	}

	public static boolean pdfExport(Component scr, String filename)
	{		
		System.out.println("pdf Export "+filename);
		BatikExport.createFolders();
		
		ByteArrayInputStream bis;
		TranscoderInput inputT;

		Writer wr;

		ByteArrayOutputStream bos;	
		TranscoderOutput outputT;
		FileOutputStream fos;

		// create output stream
		DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
		Document document = domImpl.createDocument(BatikExport.svgNS, "svg", null);
		SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
		File outputFile = new File(BatikExport.pdfFilePath + filename + ((filename.endsWith(".pdf"))?"":".pdf"));
        PDFTranscoder pdfT = new PDFTranscoder();
		float dpi = 96.0f;

		try
		{
			Thread.sleep(1000);
	        scr.paint(svgGenerator);
			Thread.sleep(1000);
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
			return false;
		}
       
		try
		{
			bos = new ByteArrayOutputStream();
			wr = new OutputStreamWriter(bos, "UTF-8");
			svgGenerator.stream(wr, true);
												
			// Create the transcoder input.
			bis = new ByteArrayInputStream(bos.toByteArray());
			inputT = new TranscoderInput(bis);
			
			// Create the transcoder output.
			fos = new FileOutputStream(outputFile);
			outputT = new TranscoderOutput(fos);
			
			// Set the transcoding hints.
			pdfT.addTranscodingHint(PDFTranscoder.KEY_AOI, scr.getBounds());
			pdfT.addTranscodingHint(PDFTranscoder.KEY_WIDTH, (float)scr.getBounds().width);
			pdfT.addTranscodingHint(PDFTranscoder.KEY_HEIGHT, (float)scr.getBounds().height);
			pdfT.addTranscodingHint(PDFTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER, 25.4f/dpi);
			
			// Save the vector graphic.
			pdfT.transcode(inputT, outputT);
			
			// Flush and close the stream.
			fos.flush();
			fos.close();
		}
		catch(IOException  e)
		{
			e.printStackTrace();
			return false;
		}
		catch(TranscoderException e)
		{
			e.printStackTrace();
			return false;
		}

		System.out.println("pdf Export finished");
        return true;
	}

	public static boolean svgpdfExport(Component scr, String filename)
	{		
		System.out.println("svg-pdf Export "+filename);
		BatikExport.createFolders();
		
		ByteArrayInputStream bis;
		TranscoderInput inputT;

		Writer wr;

		ByteArrayOutputStream bos;	
		TranscoderOutput outputT;
		FileOutputStream pdffos;
		FileOutputStream svgfos;
		
		byte[] buffer;

		// create output stream
		DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
		Document document = domImpl.createDocument(BatikExport.svgNS, "svg", null);
		SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
		File pdfOutputFile = new File(BatikExport.pdfFilePath + filename + ((filename.endsWith(".pdf"))?"":".pdf"));
		File svgOutputFile = new File(BatikExport.svgFilePath + filename + ((filename.endsWith(".svg"))?"":".svg"));
        PDFTranscoder pdfT = new PDFTranscoder();
		float dpi = 96.0f;

		try
		{
			Thread.sleep(1000);
	        scr.paint(svgGenerator);
			Thread.sleep(1000);
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
			return false;
		}
       
		try
		{
			// generate svg stream
			bos = new ByteArrayOutputStream();
			wr = new OutputStreamWriter(bos, "UTF-8");
			svgGenerator.stream(wr, true);
			buffer = bos.toByteArray();
			
			// write svgFile
			svgfos = new FileOutputStream(svgOutputFile);
			svgfos.write(buffer);
			
			// write pdf file
			// Create the transcoder input.
			bis = new ByteArrayInputStream(buffer);
			inputT = new TranscoderInput(bis);
			
			// Create the transcoder output.
			pdffos = new FileOutputStream(pdfOutputFile);
			outputT = new TranscoderOutput(pdffos);
			
			// Set the transcoding hints.
			pdfT.addTranscodingHint(PDFTranscoder.KEY_AOI, scr.getBounds());
			pdfT.addTranscodingHint(PDFTranscoder.KEY_WIDTH, (float)scr.getBounds().width);
			pdfT.addTranscodingHint(PDFTranscoder.KEY_HEIGHT, (float)scr.getBounds().height);
			pdfT.addTranscodingHint(PDFTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER, 25.4f/dpi);
			
			// Save the vector graphic.
			pdfT.transcode(inputT, outputT);
			
			// Flush and close the stream.
			pdffos.flush();
			pdffos.close();
		}
		catch(IOException  e)
		{
			e.printStackTrace();
			return false;
		}
		catch(TranscoderException e)
		{
			e.printStackTrace();
			return false;
		}

		System.out.println("svg-pdf Export finished");
        return true;
	}
	
	public static boolean pngExport(Component src, String filename)
	{
		System.out.println("png Export "+filename);
		BatikExport.createFolders();
				
		BufferedImage bi = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
		
		try
		{
			Thread.sleep(1000);
			src.paint(bi.getGraphics());
			Thread.sleep(1000);
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
			return false;
		}
		
		try
		{
			ImageIO.write(bi, "png", new File(BatikExport.pngFilePath + filename + ((filename.endsWith(".png"))?"":".png")));
		}
		catch(HeadlessException e)
		{
			e.printStackTrace();
			return false;
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return false;
		}
		
		System.out.println("png Export finished");
		return true;
	}

	public static boolean jpgExport(Component src, String filename)
	{
		System.out.println("jpg Export "+filename);
		BatikExport.createFolders();
		
		BufferedImage bi = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);

		try
		{
			Thread.sleep(1000);
			src.paint(bi.getGraphics());
			Thread.sleep(1000);
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
			return false;
		}
		
		try
		{	
			ImageIO.write(bi, "jpg", new File(BatikExport.jpgFilePath + filename + ((filename.endsWith(".jpg"))?"":".jpg")));
		}
		catch(HeadlessException e)
		{
			e.printStackTrace();
			return false;
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return false;
		}
		
		System.out.println("jpg Export finished");
		return true;
	}
}
