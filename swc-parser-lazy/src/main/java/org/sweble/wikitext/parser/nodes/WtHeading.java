package org.sweble.wikitext.parser.nodes;

public class WtHeading
		extends
			WtContentNodeImpl
{
	private static final long serialVersionUID = 1L;
	
	// =========================================================================
	
	public WtHeading()
	{
	}
	
	public WtHeading(WtNodeList content)
	{
		super(content);
	}
	
	@Override
	public int getNodeType()
	{
		return NT_HEADING;
	}
}
