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
            sourceUnit += strTree.get(ctx.pragmaDirective(i));
        }
        // ImportDirective
        for (int i = 0 ; i < ctx.importDirective().size() ; i++) {
            sourceUnit += strTree.get(ctx.importDirective(i));
        }
        // ContractDefinition
        for (int i = 0 ; i < ctx.contractDefinition().size() ; i++) {
            sourceUnit += strTree.get(ctx.contractDefinition(i));
        }
        // 완성된 프로그램 출력
        System.out.println(sourceUnit);
    }

    // pragmaDirective

    @Override
    public void exitPragmaDirective(SolidityParser.PragmaDirectiveContext ctx) {
        String s1 = ctx.getChild(0).getText(); // "pragma"
        String s2 = strTree.get(ctx.pragmaName());
        String s3 = strTree.get(ctx.pragmaValue());
        String s4 = ctx.getChild(3).getText(); // ";"
        strTree.put(ctx, s1 + " " + s2 + " " + s3 + s4 + "\n");
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
        String result = "";
        for (int i = 0 ; i < ctx.getChildCount() ; i++) {
            result += strTree.get(ctx.getChild(i));
            if (i != ctx.getChildCount() - 1)
                result += " ";
        }
        strTree.put(ctx, result);
    }

    @Override
    public void exitVersionConstraint(SolidityParser.VersionConstraintContext ctx) {
        String result = "";
        if (ctx.versionOperator() != null) {
            result += strTree.get(ctx.versionOperator());
        }
        result += ctx.VersionLiteral().getText(); // VersionLiteral
        strTree.put(ctx, result);
    }

    @Override
    public void exitVersionOperator(SolidityParser.VersionOperatorContext ctx) {
        strTree.put(ctx, ctx.getChild(0).getText());
    }

    @Override
    public void exitExpression(SolidityParser.ExpressionContext ctx) {
        // Expression이 PrimaryExpression인 경우
        if (ctx.primaryExpression() != null) {
            strTree.put(ctx, strTree.get(ctx.primaryExpression()));
        }
        // PrimaryExpression외에는 nodeCount에 따라 분리
        int nodeCount = ctx.getChildCount();
        String expr1 = strTree.get(ctx.expression(0)); // 가장 처음 나타나는 Expression을 미리 받아놓음
        // 노드가 2개인 경우
        if (nodeCount == 2) {
            // 'new' typeName
            if (ctx.typeName() != null) {
                String s1 = ctx.getChild(0).getText(); // 'new'
                String s2 = strTree.get(ctx.typeName());
                strTree.put(ctx, s1 + " " + s2);
            }
            // unary operator
            // expression이 앞에 나오는 경우
            if (ctx.getChild(0) == ctx.expression()) {
                String s1 = ctx.getChild(1).getText();
                strTree.put(ctx, expr1 + s1);
            }
            // expression이 뒤에 나오는 경우
            if (ctx.getChild(1) == ctx.expression()) {
                String s1 = ctx.getChild(0).getText();
                // s1이 after, delete인 경우 공백 추가
                if (s1.equals("after") || s1.equals("delete")) {
                    strTree.put(ctx, s1 + " " + expr1);
                } else {
                    // 나머지 경우 공백 없이 사용
                    strTree.put(ctx,s1 + expr1);
                }
            }
        }
        // 노드가 3개인 경우
        if (nodeCount == 3) {
            // '(' expression ')'
            if (ctx.getChild(0).getText().equals("(")) {
                String s1 = ctx.getChild(0).getText(); // '('
                String s2 = ctx.getChild(0).getText(); // ')'
                strTree.put(ctx, s1 + expr1 + s2);
            }
            // binary operator
            else {
                String expr2 = strTree.get(ctx.expression(1));
                String s1 = ctx.getChild(1).getText(); // 연산자
                strTree.put(ctx, expr1 + " " + s1 + " " + expr2);
            }
        }
        // 노드가 4개인 경우
        if (nodeCount == 4) {
            if (ctx.functionCallArguments() != null) {
                String expr2 = strTree.get(ctx.expression(1));
                String s1 = ctx.getChild(1).getText();
                String s2 = ctx.getChild(3).getText();
                strTree.put(ctx, expr1 + s1 + expr2 + s2);
            }
            else {
                String funcallArgs = strTree.get(ctx.functionCallArguments());
                String s1 = ctx.getChild(1).getText();
                String s2 = ctx.getChild(3).getText();
                strTree.put(ctx, expr1 + s1 + funcallArgs + s2);
            }
        }
        // 노드가 5개인 경우
        // expression '?' expression ':' expression
        if (nodeCount == 5) {
            String expr2 = strTree.get(ctx.expression(1));
            String expr3 = strTree.get(ctx.expression(2));
            String s1 = ctx.getChild(1).getText();
            String s2 = ctx.getChild(3).getText();
            strTree.put(ctx, expr1 + " " + s1 + " " + expr2 + " " + s2 + " " + expr3);
        }
    }

    @Override
    public void exitTypeName(SolidityParser.TypeNameContext ctx) {
        if (ctx.elementaryTypeName() != null) {
            strTree.put(ctx, strTree.get(ctx.elementaryTypeName()));
        } else if (ctx.userDefinedTypeName() != null) {
            strTree.put(ctx, strTree.get(ctx.userDefinedTypeName()));
        } else if (ctx.mapping() != null) {
            strTree.put(ctx, strTree.get(ctx.mapping()));
        } else if (ctx.typeName() != null) {
            String typeName = strTree.get(ctx.typeName());
            String s1 = ctx.getChild(1).getText();
            String s2;
            if (ctx.expression() != null) {
                // expression이 존재하는 경우
                String expr = strTree.get(ctx.expression());
                s2 = ctx.getChild(3).getText();
                strTree.put(ctx, typeName + s1 + expr + s2);
            } else {
                // expression이 존재하지 않는 경우
                s2 = ctx.getChild(2).getText();
                strTree.put(ctx, typeName + s1 + s2);
            }
        } else if (ctx.functionTypeName() != null) {
            strTree.put(ctx, strTree.get(ctx.functionTypeName()));
        } else {
            String s1 = ctx.getChild(0).getText();
            String s2 = ctx.getChild(1).getText();
            strTree.put(ctx, s1 + " " + s2);
        }
    }

    @Override
    public void exitElementaryTypeName(SolidityParser.ElementaryTypeNameContext ctx) {
        strTree.put(ctx, ctx.getChild(0).getText());
    }

    @Override
    public void exitUserDefinedTypeName(SolidityParser.UserDefinedTypeNameContext ctx) {
        int numOfIdentifier = ctx.identifier().size();
        String result = strTree.get(ctx.identifier(0));
        for (int i = 1; i < numOfIdentifier; i++) {
            result += ctx.getChild(2 * i - 1).getText(); // '.'
            result += strTree.get(ctx.identifier(1));
        }
        strTree.put(ctx, result);
    }

    @Override
    public void exitMapping(SolidityParser.MappingContext ctx) {
        String s1 = ctx.getChild(0).getText(); // 'mapping'
        String s2 = ctx.getChild(1).getText(); // '('
        String s3 = ctx.getChild(3).getText(); // '=>'
        String s4 = ctx.getChild(5).getText(); // ')'
        String elementaryTypeName = strTree.get(ctx.elementaryTypeName());
        String typeName = strTree.get(ctx.typeName());
        strTree.put(ctx, s1 + s2 + elementaryTypeName + s3 + typeName + s4);
    }

    @Override
    public void exitFunctionTypeName(SolidityParser.FunctionTypeNameContext ctx) {
        /*
          : 'function' functionTypeParameterList
        ( InternalKeyword | ExternalKeyword | stateMutability )*
        ( 'returns' functionTypeParameterList )? ;
         */
        String s1 = ctx.getChild(0).getText();
        String functionTypeParameterList1 = strTree.get(ctx.functionTypeParameterList(0));

        String mid = "";

        String ret = "";
        String functionTypeParameterList2 = "";

        // internalKeyword || externalKeyword || stateMutability 의 총 개수 합을 구함
        int countMidNode = ctx.getChildCount() - 2;
        // 'returns' 구문이 있다면 -2 수행하고 값 채우기
        if ((ret = ctx.getChild(countMidNode).getText()).equals("returns")) {
            functionTypeParameterList2 = strTree.get(ctx.functionTypeParameterList(1));
            countMidNode = countMidNode - 2;
        }

        // 나머지 중간 노드 채우기
        for (int i = 0; i < countMidNode; i++) {
            if (ctx.getChild(i + 2) instanceof SolidityParser.StateMutabilityContext) {
                // StateMutability 노드인 경우 strTree에서 가져오기
                mid += strTree.get(ctx.getChild(i+2));
            } else {
                // 그외의 노드인 경우 바로 text 불러오기
                mid += ctx.getChild(i + 2).getText();
            }
        }

        strTree.put(ctx, s1 + functionTypeParameterList1 + mid + ret + functionTypeParameterList2);
    }

    @Override
    public void exitStateMutability(SolidityParser.StateMutabilityContext ctx) {
        strTree.put(ctx, ctx.getChild(0).getText());
    }

    @Override
    public void exitFunctionTypeParameterList(SolidityParser.FunctionTypeParameterListContext ctx) {
        // '(' ( functionTypeParameter (',' functionTypeParameter)* )? ')' ;
        int count = ctx.functionTypeParameter().size();
        String s1 = ctx.getChild(0).getText(); // '('
        String s2 = ctx.getChild(ctx.getChildCount() - 1).getText(); // ')'
        String mid = "";
        if (count >= 1) {
            mid = strTree.get(ctx.functionTypeParameter(0));
        }
        for (int i = 1; i < count; i++) {
            mid += ctx.getChild(2 * i).getText(); // ','
            mid += strTree.get(ctx.functionTypeParameter(i));
        }
        strTree.put(ctx, s1 + mid + s2);
    }

    @Override
    public void exitFunctionTypeParameter(SolidityParser.FunctionTypeParameterContext ctx) {
        String result = strTree.get(ctx.typeName());
        if (ctx.storageLocation() != null) {
            result += strTree.get(ctx.storageLocation());
        }
        strTree.put(ctx, result);
    }

    @Override
    public void exitStorageLocation(SolidityParser.StorageLocationContext ctx) {
        strTree.put(ctx, ctx.getChild(0).getText());
    }

    // contractDefinition
    // natSpec? ( 'contract' | 'interface' | 'library' ) identifier
    //    ( 'is' inheritanceSpecifier (',' inheritanceSpecifier )* )?
    //    '{' contractPart* '}' ;
    @Override
    public void exitContractDefinition(SolidityParser.ContractDefinitionContext ctx) {
        String natSpec = "";
        String kindOf = "";
        String identifier = "";
        String inheritanceSpecifierPart = "";
        String leftParentheses = "";
        String contractPart = "";
        String rightParentheses = "";

        int indexOfKindOf = 0;
        if (ctx.natSpec() != null) {
            natSpec = strTree.get(ctx.natSpec());
            indexOfKindOf = 1;
            kindOf = ctx.getChild(indexOfKindOf).getText();
        } else {
            kindOf = ctx.getChild(indexOfKindOf).getText();
        }

        identifier = strTree.get(ctx.identifier());

        int countInheritanceSpecifier;
        if ((countInheritanceSpecifier = ctx.inheritanceSpecifier().size()) != 0) {
            inheritanceSpecifierPart += ctx.getChild(indexOfKindOf + 2) + " "; // 'is '
            inheritanceSpecifierPart += strTree.get(ctx.inheritanceSpecifier(0));
            for (int i = 1; i < countInheritanceSpecifier; i++) {
                inheritanceSpecifierPart += ctx.getChild(indexOfKindOf + 2 + 2 * i); // ','
                inheritanceSpecifierPart += " " + strTree.get(ctx.inheritanceSpecifier(i));
            }
        }

        int countContractPart = ctx.contractPart().size();
        leftParentheses = ctx.getChild(ctx.getChildCount() - countContractPart - 2).getText(); // '{'
        rightParentheses = ctx.getChild(ctx.getChildCount() - 1).getText(); // '}'
        for (int i = 0; i < countContractPart; i++) {
            contractPart += strTree.get(ctx.contractPart(i));
        }

        strTree.put(ctx, natSpec + "\n" + kindOf + " " + identifier + " " + inheritanceSpecifierPart +
                " " + leftParentheses + "\n" + contractPart + "\n" + rightParentheses);
    }


    // todo
    // 1. ContractDefinition 구성하는 아래의 NatSpec, InheritanceSpecifier, ContractPart 완성하기
    // 2. block indentation 어떻게 처리할지 논의하기
        // 2-1. ContractDefinition -> ContractPart 들어갈 때 indent
        // 2-2. ContractPart 아래의 각 노드에서 'block' 노드로 들어갈 때 indent

    @Override
    public void exitNatSpec(SolidityParser.NatSpecContext ctx) {
        //todo
    }

    @Override
    public void exitInheritanceSpecifier(SolidityParser.InheritanceSpecifierContext ctx) {
        //todo
    }

    @Override
    public void exitContractPart(SolidityParser.ContractPartContext ctx) {
        //todo
    }

    @Override
    public void exitStatement(SolidityParser.StatementContext ctx) {
        strTree.put(ctx, strTree.get(ctx.getChild(0)));
    }

    //modifierList
    //  ( modifierInvocation | stateMutability | ExternalKeyword
    //  | PublicKeyword | InternalKeyword | PrivateKeyword )* ;

    @Override
    public void exitModifierList(SolidityParser.ModifierListContext ctx) {
        int count = ctx.getChildCount();
        String result = "";
        for (int i = 0 ; i < count ; i++) {
            // child가 Non-Terminal 인 경우
            if (ctx.getChild(i) instanceof SolidityParser.ModifierInvocationContext
                    || ctx.getChild(i) instanceof SolidityParser.StateMutabilityContext) {
                result += strTree.get(ctx.getChild(i));
            }
            else {
                // child가 Terminal인 경우
                result += ctx.getChild(i).getText();
            }
            // 키워드 다음 공백 추가
            if (i < count - 1) {
                result += " ";
            }
        }
        strTree.put(ctx, result);
    }

    //: identifier ( '(' expressionList? ')' )? ;
    @Override
    public void exitModifierInvocation(SolidityParser.ModifierInvocationContext ctx) {
        String identifier = strTree.get(ctx.identifier());
        String s1 = "";
        String s2 = "";
        String exprList = "";
        if (ctx.expressionList() != null) {
            s1 = ctx.getChild(1).getText(); // '('
            s2 = ctx.getChild(3).getText(); // ')'
            exprList = strTree.get(ctx.expressionList());
        }
        strTree.put(ctx, identifier + s1 + exprList + s2);
    }


}
