package listener;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import generated.*;

public class Main {

    public static void main(String[] args) throws Exception {
        CharStream codeCharStream = CharStreams.fromFileName("./test/test.sol");
        SolidityLexer lexer = new SolidityLexer(codeCharStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SolidityParser parser = new SolidityParser(tokens);
        ParseTree tree = parser.sourceUnit(); // solidity 시작점은 sourceUnit

        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new SonamuPreprocessor(), tree);
    }

}
