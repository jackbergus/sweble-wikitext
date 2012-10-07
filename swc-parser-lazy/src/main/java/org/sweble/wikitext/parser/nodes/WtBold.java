package org.sweble.wikitext.parser.nodes;

public class WtBold
		extends
			WtContentNodeImpl
{
	private static final long serialVersionUID = 1L;
	
	// =========================================================================
	
	public WtBold()
	{
	}
	
	public WtBold(WtNodeList content)
	{
		super(content);
	}
	
	@Override
	public int getNodeType()
	{
		return NT_BOLD;
	}
}
