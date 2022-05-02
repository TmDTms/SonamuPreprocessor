package listener;

import generated.*;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

public class SonamuPreprocessor extends SolidityBaseListener {

    ParseTreeProperty<String> strTree = new ParseTreeProperty<>(); // String으로 tree를 만들어주는 객체
    int indent = 0;

    // 최상위 노드 SourceUnit
    // SourceUnit을 exit하면서 하위의 3가지 논터미널 노드가 갖는 문자열을 모두 합쳐준다.
    @Override
    public void exitSourceUnit(SolidityParser.SourceUnitContext ctx) {
        String sourceUnit = "";
        // PragmaDirective
        for (int i = 0 ; i < ctx.pragmaDirective().size() ; i++) {
            sourceUnit += ctx.pragmaDirective(i);
        }
        // ImportDirective
        for (int i = 0 ; i < ctx.importDirective().size() ; i++) {
            sourceUnit += ctx.importDirective(i);
        }
        // ContractDefinition
        for (int i = 0 ; i < ctx.contractDefinition().size() ; i++) {
            sourceUnit += ctx.contractDefinition(i);
        }
        // 완성된 프로그램 출력
        System.out.println(sourceUnit);
    }

    @Override
    public void exitPragmaDirective(SolidityParser.PragmaDirectiveContext ctx) {
        String result = "";
        String s1 = ctx.getChild(0).getText(); // "pragma"
        String s2 = strTree.get(ctx.pragmaName());
        String s3 = strTree.get(ctx.pragmaValue());
        String s4 = ctx.getChild(3).getText(); // ";"
        strTree.put(ctx, s1 + " " + s2 + " " + s3 + s4);
    }

    @Override
    public void exitPragmaName(SolidityParser.PragmaNameContext ctx) {
        strTree.put(ctx, strTree.get(ctx.identifier()));
    }

    @Override
    public void exitIdentifier(SolidityParser.IdentifierContext ctx) {
        strTree.put(ctx, ctx.getChild(0).getText());
    }

    @Override
    public void exitPragmaValue(SolidityParser.PragmaValueContext ctx) {
        if (ctx.version() != null) {
            strTree.put(ctx, strTree.get(ctx.version()));
        }
        if (ctx.expression() != null) {
            strTree.put(ctx, strTree.get(ctx.expression()));
        }
    }

    @Override
    public void exitVersion(SolidityParser.VersionContext ctx) {
        // 작성중
        // int count = ctx.version
    }

    @Override
    public void exitVersionConstraint(SolidityParser.VersionConstraintContext ctx) {
        // 작성중
        super.exitVersionConstraint(ctx);
    }

    @Override
    public void exitVersionOperator(SolidityParser.VersionOperatorContext ctx) {
        // 작성중
        super.exitVersionOperator(ctx);
    }

    @Override
    public void exitExpression(SolidityParser.ExpressionContext ctx) {
        // 작성중
        super.exitExpression(ctx);
    }
}
