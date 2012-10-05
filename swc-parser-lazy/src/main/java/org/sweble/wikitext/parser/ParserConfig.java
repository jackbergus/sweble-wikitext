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

package org.sweble.wikitext.parser;

import org.sweble.wikitext.parser.WikitextWarning.WarningSeverity;
import org.sweble.wikitext.parser.parser.LinkBuilder.LinkTargetType;
import org.sweble.wikitext.parser.postprocessor.ScopeType;
import org.sweble.wikitext.parser.utils.XmlEntityResolver;

public interface ParserConfig
		extends
			XmlEntityResolver
{
	// ==[ Parser features ]====================================================
	
	boolean isWarningsEnabled();
	
	boolean isWarningLevelEnabled(WarningSeverity severity);
	
	boolean isAutoCorrect();
	
	boolean isGatherRtData();
	
	// ==[ Link classification and parsing ]====================================
	
	boolean isUrlProtocol(String proto);
	
	String getInternalLinkPrefixPattern();
	
	String getInternalLinkPostfixPattern();
	
	LinkTargetType classifyTarget(String target);
	
	boolean isNamespace(String nsName);
	
	boolean isTalkNamespace(String nsName);
	
	boolean isInterwikiName(String iwPrefix);
	
	boolean isIwPrefixOfThisWiki(String iwPrefix);
	
	// ==[ Names ]==============================================================
	
	boolean isValidPageSwitchName(String name);
	
	boolean isValidExtensionTagName(String name);
	
	// ==[ Parsing XML elements ]===============================================
	
	boolean isXmlElementAllowed(String name);
	
	boolean isXmlElementEmptyOnly(String name);
	
	ScopeType getXmlElementType(String name);
	
	boolean isValidXmlEntityRef(String name);
}
