package org.sweble.wikitext.parser.nodes;

import de.fau.cs.osr.ptk.common.ast.AstNodePropertyIterator;

public class WtUrl
		extends
			WtLeafNode
		implements
			WtLinkTarget
{
	private static final long serialVersionUID = 1L;
	
	// =========================================================================
	
	/**
	 * Only for use by de-serialization code.
	 */
	protected WtUrl()
	{
	}
	
	public WtUrl(String protocol, String path)
	{
		setProtocol(protocol);
		setPath(path);
	}
	
	@Override
	public int getNodeType()
	{
		return NT_URL;
	}
	
	@Override
	public LinkTargetType getTargetType()
	{
		return LinkTargetType.URL;
	}
	
	// =========================================================================
	// Properties
	
	private String protocol;
	
	public final String getProtocol()
	{
		return this.protocol;
	}
	
	public final String setProtocol(String protocol)
	{
		if (protocol == null)
			throw new NullPointerException();
		String old = this.protocol;
		this.protocol = protocol;
		return old;
	}
	
	private String path;
	
	public final String getPath()
	{
		return this.path;
	}
	
	public final String setPath(String path)
	{
		if (path == null)
			throw new NullPointerException();
		String old = this.path;
		this.path = path;
		return old;
	}
	
	@Override
	public final int getPropertyCount()
	{
		return 2 + getSuperPropertyCount();
	}
	
	public final int getSuperPropertyCount()
	{
		return super.getPropertyCount();
	}
	
	@Override
	public final AstNodePropertyIterator propertyIterator()
	{
		return new WtLeafNodePropertyIterator()
		{
			@Override
			protected int getPropertyCount()
			{
				return WtUrl.this.getPropertyCount();
			}
			
			@Override
			protected String getName(int index)
			{
				switch (index - getSuperPropertyCount())
				{
					case 0:
						return "protocol";
					case 1:
						return "path";
						
					default:
						return super.getName(index);
				}
			}
			
			@Override
			protected Object getValue(int index)
			{
				switch (index - getSuperPropertyCount())
				{
					case 0:
						return WtUrl.this.getProtocol();
					case 1:
						return WtUrl.this.getPath();
						
					default:
						return super.getValue(index);
				}
			}
			
			@Override
			protected Object setValue(int index, Object value)
			{
				switch (index - getSuperPropertyCount())
				{
					case 0:
						return WtUrl.this.setProtocol((String) value);
					case 1:
						return WtUrl.this.setPath((String) value);
						
					default:
						return super.setValue(index, value);
				}
			}
		};
	}
}
