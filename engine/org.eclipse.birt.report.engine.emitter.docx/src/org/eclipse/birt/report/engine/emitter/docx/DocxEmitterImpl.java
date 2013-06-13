/*******************************************************************************
 * Copyright (c) 2013 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.report.engine.emitter.docx;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.report.engine.api.CompressionMode;
import org.eclipse.birt.report.engine.api.DocxRenderOption;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.eclipse.birt.report.engine.content.ICellContent;
import org.eclipse.birt.report.engine.content.IContent;
import org.eclipse.birt.report.engine.content.IElement;
import org.eclipse.birt.report.engine.content.IForeignContent;
import org.eclipse.birt.report.engine.content.IStyle;
import org.eclipse.birt.report.engine.content.ITableContent;
import org.eclipse.birt.report.engine.emitter.IEmitterServices;
import org.eclipse.birt.report.engine.emitter.docx.writer.DocxWriter;
import org.eclipse.birt.report.engine.emitter.wpml.AbstractEmitterImpl;
import org.eclipse.birt.report.engine.emitter.wpml.WordUtil;
import org.eclipse.birt.report.engine.layout.pdf.util.HTML2Content;
import org.eclipse.birt.report.engine.presentation.ContentEmitterVisitor;

public class DocxEmitterImpl extends AbstractEmitterImpl
{

	private static final String OUTPUT_FORMAT = "docx";
	
	private boolean embedHtml = true;

	public DocxEmitterImpl( ContentEmitterVisitor contentVisitor )
	{
		this.contentVisitor = contentVisitor;
	}

	public void initialize( IEmitterServices service ) throws EngineException
	{
		super.initialize( service );
		String tempFileDir = service.getReportEngine( ).getConfig( )
				.getTempDir( );
		wordWriter = new DocxWriter( out, tempFileDir, getCompressionMode( service )
				.getValue( ) );
		IRenderOption renderOption = service.getRenderOption( );
		Object value = renderOption.getOption( DocxRenderOption.OPTION_EMBED_HTML );
		if ( value instanceof Boolean )
		{
			this.embedHtml = (Boolean) value;
		}
	}

	private CompressionMode getCompressionMode( IEmitterServices service )
	{
		RenderOption renderOption = (RenderOption) service.getRenderOption( );
		CompressionMode compressionMode = CompressionMode.BEST_COMPRESSION;
		Object mode = renderOption
				.getOption( DocxRenderOption.OPTION_COMPRESSION_MODE );
		if ( mode instanceof CompressionMode )
		{
			compressionMode = (CompressionMode) mode;
		}
		return compressionMode;
	}

	public String getOutputFormat( )
	{
		return OUTPUT_FORMAT;
	}

	public void endTable( ITableContent table )
	{
		endTable( );
		decreaseTOCLevel( table );
	}

	public void startForeign( IForeignContent foreign ) throws BirtException
	{
		if ( IForeignContent.HTML_TYPE.equalsIgnoreCase( foreign.getRawType( ) ) )
		{
			if ( embedHtml )
			{
<<<<<<< HEAD
				 int width = WordUtil.convertTo( foreign.getWidth( ),
				 context.getCurrentWidth( ), reportDpi );
				 width = Math.min( width, context.getCurrentWidth( ) );
				 wordWriter.startTable( foreign.getComputedStyle( ), width, true
				 );
				 wordWriter.startTableRow( -1 );
				 wordWriter
				 .startTableCell( width, foreign.getComputedStyle( ), null );
				 writeBookmark( foreign );
				 // TODO:need text paser for foreign raw value
				 wordWriter.writeForeign( foreign );
				 wordWriter.endTableCell( true ,true);
				 wordWriter.endTableRow( );
				 wordWriter.endTable( );
				 context.setIsAfterTable( true );
				 context.addContainer( true );
=======
				int width = WordUtil.convertTo( foreign.getWidth( ),
						context.getCurrentWidth( ), reportDpi );
				width = Math.min( width, context.getCurrentWidth( ) );
				wordWriter
						.startTable( foreign.getComputedStyle( ), width, true );
				wordWriter.startTableRow( -1 );
				wordWriter.startTableCell( width, foreign.getComputedStyle( ),
						null );
				// TODO:need text paser for foreign raw value
				wordWriter.writeForeign( foreign );
				if ( isInSpannedCell( foreign ) )
				{
					//insert empty line after embed html 
					wordWriter.endTableCell( true, true );
				}
				else
				{
					//no empty line after embed html
					wordWriter.endTableCell( true, false );
				}
				wordWriter.endTableRow( );
				wordWriter.endTable( );
				context.setIsAfterTable( true );
				context.addContainer( true );
>>>>>>> refs/remotes/origin/master
			}
			else
			{
				writeBookmark( foreign );
				writeToc( foreign );
				HTML2Content.html2Content( foreign );
				contentVisitor.visitChildren( foreign, null );
				adjustInline( );
			}

		}
		else
		{
			Object rawValue = foreign.getRawValue( );
			String foreignText = rawValue == null ? "" : rawValue.toString( );
			writeContent( AbstractEmitterImpl.NORMAL, foreignText, foreign );
		}
	}

	private boolean isInSpannedCell( IForeignContent foreign )
	{
		IElement content = foreign.getParent( );
		if ( content instanceof ICellContent )
		{
			ICellContent cell = (ICellContent) content;
			if ( cell != null )
			{
				if ( cell.getColSpan( ) > 1 )
					return true;
			}
		}
		return false;
	}

	protected void writeContent( int type, String txt, IContent content )
	{
		context.addContainer( false );
		IStyle computedStyle = content.getComputedStyle( );
        IStyle inlineStyle = null;
		InlineFlag inlineFlag = InlineFlag.BLOCK;
		String textAlign = null;
		if ( "inline".equalsIgnoreCase( content.getComputedStyle( )
				.getDisplay( ) ) )
		{
			if ( context.isFirstInline( ) )
			{
				context.startInline( );
				inlineFlag = InlineFlag.FIRST_INLINE;
				computedStyle = computeStyle( computedStyle );
			}
			else
				inlineFlag = InlineFlag.MIDDLE_INLINE;
			IContent parent = (IContent) content.getParent( );
			if ( parent != null && parent.getComputedStyle( ) != null )
				textAlign = parent.getComputedStyle( ).getTextAlign( );
		}
		else
		{
			adjustInline( );
		}

		writeBookmark( content );
		writeToc( content, inlineFlag == InlineFlag.MIDDLE_INLINE ); // element with Toc contains bookmark
		writeText( type, txt, content, inlineFlag, computedStyle, inlineStyle,
				textAlign );
		context.setIsAfterTable( false );
	}
}
