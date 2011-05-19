/**
 * Copyright 2011 The Open Source Research Group,
 *                University of Erlangen-Nürnberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sweble.wikitext;

import java.util.LinkedList;
import java.util.regex.Pattern;

import org.sweble.wikitext.engine.Page;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.utils.EntityReferences;
import org.sweble.wikitext.engine.utils.SimpleWikiConfiguration;
import org.sweble.wikitext.lazy.LinkTargetException;
import org.sweble.wikitext.lazy.encval.IllegalCodePoint;
import org.sweble.wikitext.lazy.parser.Bold;
import org.sweble.wikitext.lazy.parser.ExternalLink;
import org.sweble.wikitext.lazy.parser.HorizontalRule;
import org.sweble.wikitext.lazy.parser.ImageLink;
import org.sweble.wikitext.lazy.parser.InternalLink;
import org.sweble.wikitext.lazy.parser.Italics;
import org.sweble.wikitext.lazy.parser.MagicWord;
import org.sweble.wikitext.lazy.parser.Paragraph;
import org.sweble.wikitext.lazy.parser.Section;
import org.sweble.wikitext.lazy.parser.Url;
import org.sweble.wikitext.lazy.parser.Whitespace;
import org.sweble.wikitext.lazy.parser.XmlElement;
import org.sweble.wikitext.lazy.preprocessor.TagExtension;
import org.sweble.wikitext.lazy.preprocessor.Template;
import org.sweble.wikitext.lazy.preprocessor.TemplateArgument;
import org.sweble.wikitext.lazy.preprocessor.TemplateParameter;
import org.sweble.wikitext.lazy.preprocessor.XmlComment;
import org.sweble.wikitext.lazy.utils.XmlCharRef;
import org.sweble.wikitext.lazy.utils.XmlEntityRef;

import de.fau.cs.osr.ptk.common.Visitor;
import de.fau.cs.osr.ptk.common.ast.AstNode;
import de.fau.cs.osr.ptk.common.ast.NodeList;
import de.fau.cs.osr.ptk.common.ast.Text;
import de.fau.cs.osr.utils.StringUtils;

/**
 * A visitor to convert an article AST into a pure text representation. To
 * better understand the visitor pattern as implemented by the Visitor class,
 * please take a look at the following resources:
 * <ul>
 * <li>{@link http://en.wikipedia.org/wiki/Visitor_pattern} (classic pattern)</li>
 * <li>{@link http://www.javaworld.com/javaworld/javatips/jw-javatip98.html}
 * (the version we use here)</li>
 * </ul>
 * 
 * The methods needed to descend into an AST and visit the children of a given
 * node <code>n</code> are
 * <ul>
 * <li><code>dispatch(n)</code> - visit node <code>n</code>,</li>
 * <li><code>iterate(n)</code> - visit the <b>children</b> of node
 * <code>n</code>,</li>
 * <li><code>map(n)</code> - visit the <b>children</b> of node <code>n</code>
 * and gather the return values of the <code>visit()</code> calls in a list,</li>
 * <li><code>mapInPlace(n)</code> - visit the <b>children</b> of node
 * <code>n</code> and replace each child node <code>c</code> with the return
 * value of the call to <code>visit(c)</code>.</li>
 * </ul>
 */
public class TextConverter
        extends
            Visitor
{
	private static final Pattern ws = Pattern.compile("\\s+");
	
	private final SimpleWikiConfiguration config;
	
	private final int wrapCol;
	
	private StringBuilder sb;
	
	private StringBuilder line;
	
	private int extLinkNum;
	
	private boolean pastBod;
	
	private int needNewlines;
	
	private boolean needSpace;
	
	private boolean noWrap;
	
	private LinkedList<Integer> sections;
	
	// =========================================================================
	
	public TextConverter(SimpleWikiConfiguration config, int wrapCol)
	{
		this.config = config;
		this.wrapCol = wrapCol;
	}
	
	@Override
	protected boolean before(AstNode node)
	{
		// This method is called by go() before visitation starts
		sb = new StringBuilder();
		line = new StringBuilder();
		extLinkNum = 1;
		pastBod = false;
		needNewlines = 0;
		needSpace = false;
		noWrap = false;
		sections = new LinkedList<Integer>();
		return super.before(node);
	}
	
	@Override
	protected Object after(AstNode node, Object result)
	{
		finishLine();
		
		// This method is called by go() after visitation has finished
		// The return value will be passed to go() which passes it to the caller
		return sb.toString();
	}
	
	// =========================================================================
	
	public void visit(AstNode n)
	{
		// Fallback for all nodes that are not explicitly handled below
		write("<");
		write(n.getNodeName());
		write(" />");
	}
	
	public void visit(NodeList n)
	{
		iterate(n);
	}
	
	public void visit(Page p)
	{
		iterate(p.getContent());
	}
	
	public void visit(Text text)
	{
		write(text.getContent());
	}
	
	public void visit(Whitespace w)
	{
		write(" ");
	}
	
	public void visit(Bold b)
	{
		write("**");
		iterate(b.getContent());
		write("**");
	}
	
	public void visit(Italics i)
	{
		write("//");
		iterate(i.getContent());
		write("//");
	}
	
	public void visit(XmlCharRef cr)
	{
		write(Character.toChars(cr.getCodePoint()));
	}
	
	public void visit(XmlEntityRef er)
	{
		String ch = EntityReferences.resolve(er.getName());
		if (ch == null)
		{
			write('&');
			write(er.getName());
			write(';');
		}
		else
		{
			write(ch);
		}
	}
	
	public void visit(Url url)
	{
		write(url.getProtocol());
		write(':');
		write(url.getPath());
	}
	
	public void visit(ExternalLink link)
	{
		write('[');
		write(extLinkNum++);
		write(']');
	}
	
	public void visit(InternalLink link)
	{
		try
		{
			PageTitle page = PageTitle.make(config, link.getTarget());
			if (page.getNamespace().equals(config.getNamespace("Category")))
				return;
		}
		catch (LinkTargetException e)
		{
		}
		
		write(link.getPrefix());
		if (link.getTitle().getContent() == null
		        || link.getTitle().getContent().isEmpty())
		{
			write(link.getTarget());
		}
		else
		{
			iterate(link.getTitle());
		}
		write(link.getPostfix());
	}
	
	public void visit(Section s)
	{
		finishLine();
		StringBuilder saveSb = sb;
		boolean saveNoWrap = noWrap;
		
		sb = new StringBuilder();
		noWrap = true;
		
		iterate(s.getTitle());
		finishLine();
		String title = sb.toString().trim();
		
		sb = saveSb;
		
		if (s.getLevel() >= 1)
		{
			while (sections.size() > s.getLevel())
				sections.removeLast();
			while (sections.size() < s.getLevel())
				sections.add(1);
			
			StringBuilder sb2 = new StringBuilder();
			for (int i = 0; i < sections.size(); ++i)
			{
				if (i < 1)
					continue;
				
				sb2.append(sections.get(i));
				sb2.append('.');
			}
			
			if (sb2.length() > 0)
				sb2.append(' ');
			sb2.append(title);
			title = sb2.toString();
		}
		
		newline(2);
		write(title);
		newline(1);
		write(StringUtils.strrep('-', title.length()));
		newline(2);
		
		noWrap = saveNoWrap;
		
		iterate(s.getBody());
		
		while (sections.size() > s.getLevel())
			sections.removeLast();
		sections.add(sections.removeLast() + 1);
	}
	
	public void visit(Paragraph p)
	{
		iterate(p.getContent());
		newline(2);
	}
	
	public void visit(HorizontalRule hr)
	{
		newline(1);
		write(StringUtils.strrep('-', wrapCol));
		newline(2);
	}
	
	public void visit(XmlElement e)
	{
		if (e.getName().equalsIgnoreCase("br"))
		{
			newline(1);
		}
		else
		{
			iterate(e.getBody());
		}
	}
	
	// =========================================================================
	// Stuff we want to hide
	
	public void visit(ImageLink n)
	{
	}
	
	public void visit(IllegalCodePoint n)
	{
	}
	
	public void visit(XmlComment n)
	{
	}
	
	public void visit(Template n)
	{
	}
	
	public void visit(TemplateArgument n)
	{
	}
	
	public void visit(TemplateParameter n)
	{
	}
	
	public void visit(TagExtension n)
	{
	}
	
	public void visit(MagicWord n)
	{
	}
	
	// =========================================================================
	
	private void newline(int num)
	{
		if (pastBod)
		{
			if (num > needNewlines)
				needNewlines = num;
		}
	}
	
	private void wantSpace()
	{
		if (pastBod)
			needSpace = true;
	}
	
	private void finishLine()
	{
		sb.append(line.toString());
		line.setLength(0);
	}
	
	private void writeNewlines(int num)
	{
		finishLine();
		sb.append(StringUtils.strrep('\n', num));
		needNewlines = 0;
		needSpace = false;
	}
	
	private void writeWord(String s)
	{
		int length = s.length();
		if (length == 0)
			return;
		
		if (!noWrap && needNewlines <= 0)
		{
			if (needSpace)
				length += 1;
			
			if (line.length() + length >= wrapCol && line.length() > 0)
				writeNewlines(1);
		}
		
		if (needSpace && needNewlines <= 0)
			line.append(' ');
		
		if (needNewlines > 0)
			writeNewlines(needNewlines);
		
		needSpace = false;
		pastBod = true;
		line.append(s);
	}
	
	private void write(String s)
	{
		if (s.isEmpty())
			return;
		
		if (Character.isSpaceChar(s.charAt(0)))
			wantSpace();
		
		String[] words = ws.split(s);
		for (int i = 0; i < words.length;)
		{
			writeWord(words[i]);
			if (++i < words.length)
				wantSpace();
		}
		
		if (Character.isSpaceChar(s.charAt(s.length() - 1)))
			wantSpace();
	}
	
	private void write(char[] cs)
	{
		write(String.valueOf(cs));
	}
	
	private void write(char ch)
	{
		writeWord(String.valueOf(ch));
	}
	
	private void write(int num)
	{
		writeWord(String.valueOf(num));
	}
}
