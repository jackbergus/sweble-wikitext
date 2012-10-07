package org.sweble.wikitext.parser.nodes;

public class WtDefinitionListDef
		extends
			WtContentNodeImpl
{
	private static final long serialVersionUID = 1L;
	
	// =========================================================================
	
	public WtDefinitionListDef()
	{
	}
	
	public WtDefinitionListDef(WtNodeList content)
	{
		super(content);
	}
	
	@Override
	public int getNodeType()
	{
		return NT_DEFINITION_LIST_DEF;
	}
}
