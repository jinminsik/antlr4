/*
 * [The "BSD license"]
 *  Copyright (c) 2012 Terence Parr
 *  Copyright (c) 2012 Sam Harwell
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 *  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.antlr.v4.tool;

import org.antlr.v4.analysis.LeftRecursiveRuleAltInfo;
import org.antlr.v4.misc.OrderedHashMap;
import org.antlr.v4.runtime.misc.Tuple;
import org.antlr.v4.runtime.misc.Tuple2;
import org.antlr.v4.runtime.misc.Tuple3;
import org.antlr.v4.tool.ast.AltAST;
import org.antlr.v4.tool.ast.GrammarAST;
import org.antlr.v4.tool.ast.RuleAST;

import java.util.ArrayList;
import java.util.List;

public class LeftRecursiveRule extends Rule {
	public List<LeftRecursiveRuleAltInfo> recPrimaryAlts;
	public OrderedHashMap<Integer, LeftRecursiveRuleAltInfo> recOpAlts;

	/** Did we delete any labels on direct left-recur refs? Points at ID of ^(= ID el) */
	public List<Tuple2<GrammarAST,String>> leftRecursiveRuleRefLabels =
		new ArrayList<Tuple2<GrammarAST,String>>();

	public LeftRecursiveRule(Grammar g, String name, RuleAST ast) {
		super(g, name, ast, 1);
		alt = new Alternative[numberOfAlts+1]; // always just one
		for (int i=1; i<=numberOfAlts; i++) alt[i] = new Alternative(this, i);
	}

	@Override
	public boolean hasAltSpecificContexts() {
		return super.hasAltSpecificContexts() || getAltLabels()!=null;
	}

	@Override
	public int getOriginalNumberOfAlts() {
		int n = 0;
		if ( recPrimaryAlts!=null ) n += recPrimaryAlts.size();
		if ( recOpAlts!=null ) n += recOpAlts.size();
		return n;
	}

	@Override
	public List<AltAST> getUnlabeledAltASTs() {
		List<AltAST> alts = new ArrayList<AltAST>();
		for (LeftRecursiveRuleAltInfo altInfo : recPrimaryAlts) {
			if (altInfo.altLabel == null) alts.add(altInfo.originalAltAST);
		}
		for (int i = 0; i < recOpAlts.size(); i++) {
			LeftRecursiveRuleAltInfo altInfo = recOpAlts.getElement(i);
			if ( altInfo.altLabel==null ) alts.add(altInfo.originalAltAST);
		}
		if ( alts.isEmpty() ) return null;
		return alts;
	}

	/** Get -> labels from those alts we deleted for left-recursive rules. */
	@Override
	public List<Tuple3<Integer,AltAST,String>> getAltLabels() {
		List<Tuple3<Integer,AltAST,String>> labels = new ArrayList<Tuple3<Integer,AltAST,String>>();
		List<Tuple3<Integer,AltAST,String>> normalAltLabels = super.getAltLabels();
		if ( normalAltLabels!=null ) labels.addAll(normalAltLabels);
		for (LeftRecursiveRuleAltInfo altInfo : recPrimaryAlts) {
			if (altInfo.altLabel != null) {
				labels.add(Tuple.create(altInfo.altNum,
															   altInfo.originalAltAST,
															   altInfo.altLabel));
			}
		}
		for (int i = 0; i < recOpAlts.size(); i++) {
			LeftRecursiveRuleAltInfo altInfo = recOpAlts.getElement(i);
			if ( altInfo.altLabel!=null ) {
				labels.add(Tuple.create(altInfo.altNum,
															 altInfo.originalAltAST,
															 altInfo.altLabel));
			}
		}
		if ( labels.isEmpty() ) return null;
		return labels;
	}
}
