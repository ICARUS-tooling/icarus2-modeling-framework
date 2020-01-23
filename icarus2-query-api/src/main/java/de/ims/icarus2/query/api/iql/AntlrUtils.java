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

	public static QueryException asSyntaxException(RecognitionException ex, String msg) {
		String message = Optional.ofNullable(msg)
				.orElse("Unspecified parsing error");
		RuleContext ctx = Optional.ofNullable(ex)
				.map(RecognitionException::getCtx)
				.orElse(null);
		QueryFragment fragment = Optional.ofNullable(ctx)
				.map(RuleContext::getSourceInterval)
				.map(iv -> new QueryFragment(ctx.getText(), iv.a, Math.max(iv.a, iv.b)))
				.orElse(null);
		return new QueryException(QueryErrorCode.SYNTAX_ERROR, message, fragment, ex);
	}

	public static String cleanNumberLiteral(String s) {
		return replaceAll(s, '_', "");
	}
}
