package org.sweble.wikitext.parser.nodes;

import de.fau.cs.osr.ptk.common.ast.AstNodePropertyIterator;

/**
 * <h1>Xml Entity Reference</h1> <h2>Grammar</h2>
 * <ul>
 * <li>
 * <p>
 * '&' XmlName ';'
 * </p>
 * </li>
 * </ul>
 */
public class XmlEntityRef
		extends
			WtLeafNode
{
	private static final long serialVersionUID = 1L;
	
	// =========================================================================
	
	public XmlEntityRef()
	{
		super();
		
	}
	
	public XmlEntityRef(String name, String resolved)
	{
		super();
		setName(name);
		setResolved(resolved);
		
	}
	
	@Override
	public int getNodeType()
	{
		return org.sweble.wikitext.parser.AstNodeTypes.NT_XML_ENTITY_REF;
	}
	
	// =========================================================================
	// Properties
	
	private String name;
	
	public final String getName()
	{
		return this.name;
	}
	
	public final String setName(String name)
	{
		String old = this.name;
		this.name = name;
		return old;
	}
	
	private String resolved;
	
	public final String getResolved()
	{
		return this.resolved;
	}
	
	public final String setResolved(String resolved)
	{
		String old = this.resolved;
		this.resolved = resolved;
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
				return XmlEntityRef.this.getPropertyCount();
			}
			
			@Override
			protected String getName(int index)
			{
				switch (index - getSuperPropertyCount())
				{
					case 0:
						return "name";
					case 1:
						return "resolved";
						
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
						return XmlEntityRef.this.getName();
					case 1:
						return XmlEntityRef.this.getResolved();
						
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
						return XmlEntityRef.this.setName((String) value);
					case 1:
						return XmlEntityRef.this.setResolved((String) value);
						
					default:
						return super.setValue(index, value);
				}
			}
		};
	}
}