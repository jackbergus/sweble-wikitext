package org.sweble.wikitext.parser.nodes;

public class WtLinkOptionAltText
		extends
			WtContentNodeImpl
{
	private static final long serialVersionUID = 1L;
	
	// =========================================================================
	
	public WtLinkOptionAltText()
	{
	}
	
	public WtLinkOptionAltText(WtNodeList content)
	{
		super(content);
	}
	
	@Override
	public int getNodeType()
	{
		return NT_LINK_OPTION_ALT_TEXT;
	}
}
