package org.sweble.wikitext.parser.nodes;

import java.util.List;

import org.sweble.wikitext.parser.WtEntityMap;

import de.fau.cs.osr.ptk.common.Warning;

public class WtParsedWikitextPage
		extends
			WtPage
{
	private static final long serialVersionUID = 1L;
	
	// =========================================================================
	
	public WtParsedWikitextPage()
	{
	}
	
	public WtParsedWikitextPage(WtNodeList content, List<Warning> warnings)
	{
		super(content, warnings);
	}
	
	public WtParsedWikitextPage(
			WtNodeList content,
			List<Warning> warnings,
			WtEntityMap entityMap)
	{
		super(content, warnings, entityMap);
	}
}
