/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
/**
 *
 */
package de.ims.icarus2.query.api.iql;

import static de.ims.icarus2.util.strings.StringUtil.replaceAll;
import static java.util.Objects.requireNonNull;

import java.util.Optional;

import javax.annotation.Nullable;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.query.api.QueryFragment;
import de.ims.icarus2.query.api.iql.antlr.IQLLexer;
import de.ims.icarus2.query.api.iql.antlr.IQLParser;

/**
 * @author Markus Gärtner
 *
 */
public final class AntlrUtils {

	/** Helper class that always throws an exception on syntax errors */
	static class SyntaxErrorReporter extends BaseErrorListener {
		@Override
		public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
				int charPositionInLine, String msg, RecognitionException e) {

			// Wrap any serious error
			if(e!=null)
				throw asSyntaxException(e, msg);
		}
	}

	public static int start(Token start) {
		return start.getStartIndex();
	}

	public static int stop(Token start, Token stop) {
		Token tkn = stop==null ? start : stop;
		return tkn.getStopIndex();
	}

	public static String textOf(Token start, Token stop) {
		CharStream stream = start.getInputStream();
		int index0 = start(start);
		int index1 = stop(start, stop);
		return stream.getText(Interval.of(index0, index1));
	}

	public static String textOf(ParserRuleContext ctx) {
		return textOf(ctx.start, ctx.stop);
	}

	public static String textOf(TerminalNode node) {
		return textOf(node.getSymbol(), null);
	}

	public static String textOf(Token token) {
		return textOf(token, null);
	}

	public static IQLLexer createLexer(String text, @Nullable String sourceName) {
		requireNonNull(text);
		if(sourceName==null) {
			sourceName = "<unnamed>";
		}
		CharStream stream = CharStreams.fromString(text, sourceName);
		IQLLexer lexer = new IQLLexer(stream);

		lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);

		return lexer;
	}

	public static IQLParser createParser(IQLLexer lexer) {
		TokenStream stream = new BufferedTokenStream(lexer);
		IQLParser parser = new IQLParser(stream);

		parser.removeErrorListener(ConsoleErrorListener.INSTANCE);
		parser.addErrorListener(new SyntaxErrorReporter());
		parser.getInterpreter().setPredictionMode(PredictionMode.LL);
		parser.setProfile(false);
		parser.setTrace(false);

		return parser;
	}

	public static IQLParser createParser(String text, @Nullable String sourceName) {
		return createParser(createLexer(text, sourceName));
	}

	private static final String UNSPECIFIED_MSG = "Unspecified parsing error";

	public static QueryFragment asFragment(RuleContext ctx) {
		if(ctx==null) {
			return null;
		}
		if(ctx instanceof ParserRuleContext) {
			ParserRuleContext prc = (ParserRuleContext) ctx;
			Interval iv = prc.getSourceInterval();
			if(iv!=Interval.INVALID && iv.a>=0 && iv.b>=0) {
				CharStream input = prc.getStart().getInputStream();
				String text = input.getText(Interval.of(0, iv.b));
				return new QueryFragment(text, Math.min(iv.a, iv.b), Math.max(iv.a, iv.b));
			}
		}
		return null;
	}

	public static QueryException asSyntaxException(RecognitionException ex, String msg) {
		String message = Optional.ofNullable(msg)
				.orElse(UNSPECIFIED_MSG);
		RuleContext ctx = Optional.ofNullable(ex)
				.map(RecognitionException::getCtx)
				.orElse(null);
		QueryFragment fragment = asFragment(ctx);
		return new QueryException(QueryErrorCode.SYNTAX_ERROR, message, fragment, ex);
	}

	public static QueryException asFeatureException(ParserRuleContext ctx, String msg) {
		String message = Optional.ofNullable(msg)
				.map(m -> String.format("%s in '%s'", m, textOf(ctx)))
				.orElse(UNSPECIFIED_MSG);
		QueryFragment fragment = asFragment(ctx);
		return new QueryException(QueryErrorCode.UNSUPPORTED_FEATURE, message, fragment);
	}

	/** Removes all underscores from the number literal, as Java is more strict than IQL */
	public static String cleanNumberLiteral(String s) {
		return replaceAll(s, '_', "");
	}

	private static boolean isContinuous(Token t1, Token t2) {
		int t1Stop = t1.getStopIndex();
		int t2Start = t2.getStartIndex();

		if(t1Stop==-1 || t2Start==-1)
			throw new QueryException(QueryErrorCode.AST_ERROR, String.format(
					"Cannot check continuity of tokens %s and %s", t1, t2));

		return t2Start==t1Stop+1;
	}

	public static boolean isContinuous(ParserRuleContext ctx) {
		Token previous = null;
		for (int i = 0; i < ctx.getChildCount(); i++) {
			ParseTree child = ctx.getChild(i);
			Token start, stop;
			if(child instanceof TerminalNode) {
				start = stop = ((TerminalNode)child).getSymbol();
			} else if(child instanceof ParserRuleContext) {
				ParserRuleContext cctx = (ParserRuleContext)child;
				if(!isContinuous(cctx)) {
						return false;
				}
				start = cctx.start;
				stop = cctx.stop;
			} else
				throw new QueryException(QueryErrorCode.AST_ERROR,
						"Unexpected rule context: "+ctx.getClass());

			if(previous!=null && !isContinuous(previous, start))
				return false;

			previous = stop;
		}

		return true;
	}
}
