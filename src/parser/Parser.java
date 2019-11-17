package parser;

import ast.*;

import lexer.Token;
import lexer.Tokeniser;
import lexer.Token.TokenClass;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author sfilipiak
 */

public class Parser {

    private Token token;

    // use for backtracking (useful for distinguishing decls from procs when parsing a program for instance)
    private Queue<Token> buffer = new LinkedList<>();

    private final Tokeniser tokeniser;


    public Parser(Tokeniser tokeniser) {
        this.tokeniser = tokeniser;
    }

    public Program parse() {
        // get the first token
        nextToken();

        return parseProgram();
    }

    public int getErrorCount() {
        return error;
    }

    private int error = 0;
    private Token lastErrorToken;

    private void error(TokenClass... expected) {

        if (lastErrorToken == token) {
            // skip this error, same token causing trouble
            return;
        }

        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (TokenClass e : expected) {
            sb.append(sep);
            sb.append(e);
            sep = "|";
        }
        System.out.println("Parsing error: expected (" + sb + ") found (" + token + ") at " + token.position);

        error++;
        lastErrorToken = token;
    }

    /*
     * Look ahead the i^th element from the stream of token.
     * i should be >= 1
     */
    private Token lookAhead(int i) {
        // ensures the buffer has the element we want to look ahead
        while (buffer.size() < i)
            buffer.add(tokeniser.nextToken());
        assert buffer.size() >= i;

        int cnt = 1;
        for (Token t : buffer) {
            if (cnt == i)
                return t;
            cnt++;
        }

        assert false; // should never reach this
        return null;
    }


    /*
     * Consumes the next token from the tokeniser or the buffer if not empty.
     */
    private void nextToken() {
        if (!buffer.isEmpty())
            token = buffer.remove();
        else
            token = tokeniser.nextToken();
    }

    /*
     * If the current token is equals to the expected one, then skip it, otherwise report an error.
     * Returns the expected token or null if an error occurred.
     */
    private Token expect(TokenClass... expected) {
        for (TokenClass e : expected) {
            if (e == token.tokenClass) {
                Token cur = token;
                nextToken();
                return cur;
            }
        }

        error(expected);
        return null;
    }

    /*
     * Returns true if the current token is equals to any of the expected ones.
     */
    private boolean accept(TokenClass... expected) {
        boolean result = false;
        for (TokenClass e : expected)
            result |= (e == token.tokenClass);
        return result;
    }


    private Program parseProgram() {
        parseIncludes();

        List<StructTypeDecl> stds = parseStructDecls();
        List<VarDecl> vds = parseVarDecls();
        List<FunDecl> fds = parseFunDecls();

        expect(TokenClass.EOF);
        return new Program(stds, vds, fds);
    }

    // includes are ignored, so does not need to return an AST node
    private void parseIncludes() {
        if (accept(TokenClass.INCLUDE)) {
            nextToken();
            expect(TokenClass.STRING_LITERAL);
            parseIncludes();
        }
    }

    private List<StructTypeDecl> parseStructDecls() {
        if (accept(TokenClass.STRUCT) && lookAhead(2).tokenClass.equals(TokenClass.LBRA)) {
            List<StructTypeDecl> structTypeDecls = new ArrayList<>();

            nextToken();
            String name = token.data;
            expect(TokenClass.IDENTIFIER);
            expect(TokenClass.LBRA);
            List<VarDecl> structVarDecls = parseStructVarDecls();
            expect(TokenClass.RBRA);
            expect(TokenClass.SC);

            structTypeDecls.add(new StructTypeDecl(new StructType(name), structVarDecls));
            List<StructTypeDecl> parseStructDecls = parseStructDecls();
            if (!parseStructDecls.isEmpty()) {
                structTypeDecls = Stream.concat(structTypeDecls.stream(), parseStructDecls.stream()).collect(Collectors.toList());
            }
            return structTypeDecls;
        }
        return new ArrayList<>();
    }

    private List<VarDecl> parseStructVarDecls() {
        if (accept(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID, TokenClass.STRUCT)) {
            return parseVarDecls();
        } else {
            expect(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID, TokenClass.STRUCT);
            return null;
        }
    }

    private List<VarDecl> parseVarDecls() {
        if ((accept(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID) && !lookAhead(2).tokenClass.equals(TokenClass.LPAR)) || (accept(TokenClass.STRUCT) && !lookAhead(3).tokenClass.equals(TokenClass.LPAR))) {
            List<VarDecl> varDecls = new ArrayList<>();

            Type type = parseType();
            String name = null;
            Token identifier = expect(TokenClass.IDENTIFIER);
            if (identifier != null)
                name = identifier.data;

            if (accept(TokenClass.SC, TokenClass.LSBR)) {
                Token varDeclToken = expect(TokenClass.SC, TokenClass.LSBR);
                if (varDeclToken != null && varDeclToken.tokenClass.equals(TokenClass.LSBR)) {
                    Token arraySizeToken = expect(TokenClass.INT_LITERAL);
                    if (arraySizeToken != null)
                        type = new ArrayType(type, Integer.parseInt(arraySizeToken.data));
                    expect(TokenClass.RSBR);
                    expect(TokenClass.SC);
                }

                VarDecl variable = new VarDecl(type, name);
                varDecls.add(variable);
                List<VarDecl> parseVarDecls = parseVarDecls();
                if (parseVarDecls != null) {
                    varDecls = Stream.concat(varDecls.stream(), parseVarDecls.stream()).collect(Collectors.toList());
                }
                return varDecls;
            } else
                expect(TokenClass.SC, TokenClass.LSBR);
        }
        return new ArrayList<>();
    }

    private List<FunDecl> parseFunDecls() {
        if ((accept(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID) && lookAhead(2).tokenClass.equals(TokenClass.LPAR)) || (accept(TokenClass.STRUCT) && lookAhead(3).tokenClass.equals(TokenClass.LPAR))) {
            List<FunDecl> funDecls = new ArrayList<>();

            Type type = parseType();
            String name = null;
            Token identifier = expect(TokenClass.IDENTIFIER);
            if (identifier != null)
                name = identifier.data;

            if (accept(TokenClass.LPAR)) {
                nextToken();
                List<VarDecl> params = parseParamsList();
                expect(TokenClass.RPAR);
                Block block = parseBlock();

                FunDecl function = new FunDecl(type, name, params, block);
                funDecls.add(function);
                List<FunDecl> parseFunDecls = parseFunDecls();
                if (parseFunDecls != null) {
                    funDecls = Stream.concat(funDecls.stream(), parseFunDecls.stream()).collect(Collectors.toList());
                }
                return funDecls;
            } else
                expect(TokenClass.LPAR);
        }
        return new ArrayList<>();
    }

    private Type parseType() {
        Type type = null;
        if (accept(TokenClass.STRUCT)) {
            type = parseStructType();
        } else {
            switch (token.tokenClass) {
                case INT:
                    type = BaseType.INT;
                    nextToken();
                    break;
                case CHAR:
                    type = BaseType.CHAR;
                    nextToken();
                    break;
                case VOID:
                    type = BaseType.VOID;
                    nextToken();
                    break;
                default:
                    expect(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID);
                    break;
            }
        }

        if (accept(TokenClass.ASTERIX)) {
            nextToken();
            type = new PointerType(type);
        }
        return type;
    }

    private Type parseStructType() {
        Type type;
        expect(TokenClass.STRUCT);
        if (accept(TokenClass.IDENTIFIER))
            type = new StructType(token.data);
        else
            type = new StructType(null);
        expect(TokenClass.IDENTIFIER);
        return type;
    }

    private List<VarDecl> parseParamsList() {
        if (!accept(TokenClass.RPAR)) {
            List<VarDecl> params = new ArrayList<>();

            Type type = parseType();
            String name = null;
            Token identifier = expect(TokenClass.IDENTIFIER);
            if (identifier != null)
                name = identifier.data;
            VarDecl param = new VarDecl(type, name);

            params.add(param);
            List<VarDecl> paramsRepetitions = parseParamsRepetitions();
            if (!paramsRepetitions.isEmpty()) {
                params = Stream.concat(params.stream(), paramsRepetitions.stream()).collect(Collectors.toList());
            }
            return params;
        }
        return new ArrayList<>();
    }

    private List<VarDecl> parseParamsRepetitions() {
        if (!accept(TokenClass.RPAR)) {
            List<VarDecl> params = new ArrayList<>();

            expect(TokenClass.COMMA);

            Type type = parseType();
            String name = null;
            Token identifier = expect(TokenClass.IDENTIFIER);
            if (identifier != null)
                name = identifier.data;
            VarDecl param = new VarDecl(type, name);

            params.add(param);
            List<VarDecl> paramsRepetitions = parseParamsRepetitions();
            if (!paramsRepetitions.isEmpty()) {
                params = Stream.concat(params.stream(), paramsRepetitions.stream()).collect(Collectors.toList());
            }
            return params;
        }
        return new ArrayList<>();
    }

    private List<Stmt> parseStatement() {
        if (accept(TokenClass.LBRA, TokenClass.WHILE, TokenClass.IF, TokenClass.RETURN, TokenClass.LPAR, TokenClass.MINUS, TokenClass.ASTERIX, TokenClass.SIZEOF, TokenClass.IDENTIFIER, TokenClass.INT_LITERAL, TokenClass.CHAR_LITERAL, TokenClass.STRING_LITERAL)) {
            List<Stmt> statements = new ArrayList<>();
            switch (token.tokenClass) {
                case LBRA: {
                    statements.add(parseBlock());
                    break;
                }
                case WHILE: {
                    nextToken();
                    expect(TokenClass.LPAR);
                    Expr expression = parseExpression();
                    expect(TokenClass.RPAR);
                    Stmt statement = parseStatementOnly();
                    statements.add(new While(expression, statement));
                    break;
                }
                case IF: {
                    nextToken();
                    expect(TokenClass.LPAR);
                    Expr expression = parseExpression();
                    expect(TokenClass.RPAR);
                    Stmt ifStatement = parseStatementOnly();
                    Stmt elseStatement;
                    if (accept(TokenClass.ELSE)) {
                        nextToken();
                        elseStatement = parseStatementOnly();
                    } else
                        elseStatement = null;
                    statements.add(new If(expression, ifStatement, elseStatement));
                    break;
                }
                case RETURN: {
                    nextToken();
                    Expr expression;
                    if (!accept(TokenClass.SC)) {
                        expression = parseExpression();
                    } else
                        expression = null;
                    expect(TokenClass.SC);
                    statements.add(new Return(expression));
                    break;
                }
                case LPAR:
                case MINUS:
                case ASTERIX:
                case SIZEOF:
                case IDENTIFIER:
                case INT_LITERAL:
                case CHAR_LITERAL:
                case STRING_LITERAL: {
                    Expr expression = parseExpression();
                    Token statementToken = expect(TokenClass.ASSIGN, TokenClass.SC);
                    if (statementToken != null && statementToken.tokenClass.equals(TokenClass.ASSIGN)) {
                        Expr assignExpression = parseExpression();
                        expect(TokenClass.SC);
                        statements.add(new Assign(expression, assignExpression));
                    } else {
                        ExprStmt expressionStatement = new ExprStmt(expression);
                        statements.add(expressionStatement);
                    }
                    break;
                }
                default:
                    expect(TokenClass.INVALID);
                    break;
            }
            List<Stmt> parseStatement = parseStatement();
            if (!parseStatement.isEmpty()) {
                statements = Stream.concat(statements.stream(), parseStatement.stream()).collect(Collectors.toList());
            }
            return statements;
        }
        return new ArrayList<>();
    }

    private Stmt parseStatementOnly() {
        if (accept(TokenClass.LBRA, TokenClass.WHILE, TokenClass.IF, TokenClass.RETURN, TokenClass.LPAR, TokenClass.MINUS, TokenClass.ASTERIX, TokenClass.SIZEOF, TokenClass.IDENTIFIER, TokenClass.INT_LITERAL, TokenClass.CHAR_LITERAL, TokenClass.STRING_LITERAL)) {
            switch (token.tokenClass) {
                case LBRA: {
                    return parseBlock();
                }
                case WHILE: {
                    nextToken();
                    expect(TokenClass.LPAR);
                    Expr expression = parseExpression();
                    expect(TokenClass.RPAR);
                    Stmt statement = parseStatementOnly();
                    return new While(expression, statement);
                }
                case IF: {
                    nextToken();
                    expect(TokenClass.LPAR);
                    Expr expression = parseExpression();
                    expect(TokenClass.RPAR);
                    Stmt ifStatement = parseStatementOnly();
                    Stmt elseStatement;
                    if (accept(TokenClass.ELSE)) {
                        nextToken();
                        elseStatement = parseStatementOnly();
                    } else
                        elseStatement = null;
                    return new If(expression, ifStatement, elseStatement);
                }
                case RETURN: {
                    nextToken();
                    Expr expression;
                    if (!accept(TokenClass.SC)) {
                        expression = parseExpression();
                    } else
                        expression = null;
                    expect(TokenClass.SC);
                    return new Return(expression);
                }
                case LPAR:
                case MINUS:
                case ASTERIX:
                case SIZEOF:
                case IDENTIFIER:
                case INT_LITERAL:
                case CHAR_LITERAL:
                case STRING_LITERAL: {
                    Expr expression = parseExpression();
                    Token statementToken = expect(TokenClass.ASSIGN, TokenClass.SC);
                    if (statementToken != null && statementToken.tokenClass.equals(TokenClass.ASSIGN)) {
                        Expr assignExpression = parseExpression();
                        expect(TokenClass.SC);
                        return new Assign(expression, assignExpression);
                    } else {
                        return new ExprStmt(expression);
                    }
                }
                default:
                    expect(TokenClass.INVALID);
                    break;
            }
        } else {
            expect(TokenClass.LBRA, TokenClass.WHILE, TokenClass.IF, TokenClass.RETURN, TokenClass.LPAR, TokenClass.MINUS, TokenClass.ASTERIX, TokenClass.SIZEOF, TokenClass.IDENTIFIER, TokenClass.INT_LITERAL, TokenClass.CHAR_LITERAL, TokenClass.STRING_LITERAL);
        }
        return null;
    }

    private Block parseBlock() {
        expect(TokenClass.LBRA);
        List<VarDecl> varDecls = parseVarDecls();
        List<Stmt> statements = parseStatement();
        expect(TokenClass.RBRA);
        return new Block(varDecls, statements);
    }

    private Expr parseExpression() {
        if (accept(TokenClass.LPAR, TokenClass.MINUS, TokenClass.ASTERIX, TokenClass.SIZEOF, TokenClass.IDENTIFIER, TokenClass.INT_LITERAL, TokenClass.CHAR_LITERAL, TokenClass.STRING_LITERAL)) {
            return parseExpression_8();
        } else {
            expect(TokenClass.LPAR, TokenClass.MINUS, TokenClass.ASTERIX, TokenClass.SIZEOF, TokenClass.IDENTIFIER, TokenClass.INT_LITERAL, TokenClass.CHAR_LITERAL, TokenClass.STRING_LITERAL);
        }
        return null;
    }

    private Expr parseExpression_8() {
        Expr lhs = parseExpression_7();
        while (accept(TokenClass.OR)) {
            Op op = Op.OR;
            nextToken();
            Expr rhs = parseExpression_7();
            lhs = new BinOp(lhs, op, rhs);
        }
        return lhs;
    }

    private Expr parseExpression_7() {
        Expr lhs = parseExpression_6();
        while (accept(TokenClass.AND)) {
            Op op = Op.AND;
            nextToken();
            Expr rhs = parseExpression_6();
            lhs = new BinOp(lhs, op, rhs);
        }
        return lhs;
    }

    private Expr parseExpression_6() {
        Expr lhs = parseExpression_5();
        while (accept(TokenClass.EQ, TokenClass.NE)) {
            Op op;
            switch (token.tokenClass) {
                case EQ: {
                    op = Op.EQ;
                    break;
                }
                case NE: {
                    op = Op.NE;
                    break;
                }
                default:
                    op = null;
            }
            nextToken();
            Expr rhs = parseExpression_5();
            lhs = new BinOp(lhs, op, rhs);
        }
        return lhs;
    }

    private Expr parseExpression_5() {
        Expr lhs = parseExpression_4();
        while (accept(TokenClass.GT, TokenClass.LT, TokenClass.GE, TokenClass.LE)) {
            Op op;
            switch (token.tokenClass) {
                case GT: {
                    op = Op.GT;
                    break;
                }
                case LT: {
                    op = Op.LT;
                    break;
                }
                case GE: {
                    op = Op.GE;
                    break;
                }
                case LE: {
                    op = Op.LE;
                    break;
                }
                default:
                    op = null;
            }
            nextToken();
            Expr rhs = parseExpression_4();
            lhs = new BinOp(lhs, op, rhs);
        }
        return lhs;
    }

    private Expr parseExpression_4() {
        Expr lhs = parseExpression_3();
        while (accept(TokenClass.PLUS, TokenClass.MINUS)) {
            Op op;
            switch (token.tokenClass) {
                case PLUS: {
                    op = Op.ADD;
                    break;
                }
                case MINUS: {
                    op = Op.SUB;
                    break;
                }
                default:
                    op = null;
            }
            nextToken();
            Expr rhs = parseExpression_3();
            lhs = new BinOp(lhs, op, rhs);
        }
        return lhs;
    }

    private Expr parseExpression_3() {
        Expr lhs = parseExpression_2();
        while (accept(TokenClass.ASTERIX, TokenClass.DIV, TokenClass.REM)) {
            Op op;
            switch (token.tokenClass) {
                case ASTERIX: {
                    op = Op.MUL;
                    break;
                }
                case DIV: {
                    op = Op.DIV;
                    break;
                }
                case REM: {
                    op = Op.MOD;
                    break;
                }
                default:
                    op = null;
            }
            nextToken();
            Expr rhs = parseExpression_2();
            lhs = new BinOp(lhs, op, rhs);
        }
        return lhs;
    }

    private Expr parseExpression_2() {
        if (accept(TokenClass.SIZEOF)) {
            return parseSizeOf();
        } else if (accept(TokenClass.ASTERIX)) {
            return parseValueAt();
        } else if (accept(TokenClass.LPAR)) {
            if (lookAhead(1).tokenClass.equals(TokenClass.INT) || lookAhead(1).tokenClass.equals(TokenClass.VOID) || lookAhead(1).tokenClass.equals(TokenClass.CHAR) || lookAhead(1).tokenClass.equals(TokenClass.STRUCT))
                return parseTypeCast();
            else
                return parseExpression_1();
        } else if (accept(TokenClass.MINUS)) {
            expect(TokenClass.MINUS);
            return new BinOp(new IntLiteral(0), Op.SUB, parseExpression_2());
        } else {
            return parseExpression_1();
        }
    }

    private Expr parseExpression_1() {
        Expr lhs = parseExpression_0();

        while (accept(TokenClass.DOT, TokenClass.LSBR)) {
            if (accept(TokenClass.DOT)) {
                return parseFieldAccess(lhs);
            } else if (accept(TokenClass.LSBR)) {
                return parseArrayAccess(lhs);
            }
        }
        return lhs;
    }

    private Expr parseExpression_0() {
        String tokenData = token.data;

        if (accept(TokenClass.LPAR)) {
            expect(TokenClass.LPAR);
            Expr expression = parseExpression();
            expect(TokenClass.RPAR);
            return expression;
        } else if (accept(TokenClass.IDENTIFIER)) {
            if (lookAhead(1).tokenClass.equals(TokenClass.LPAR))
                return parseFuncCall();
            expect(TokenClass.IDENTIFIER);
            return new VarExpr(tokenData);
        }

        if (accept(TokenClass.INT_LITERAL)) {
            expect(TokenClass.INT_LITERAL);
            return new IntLiteral(Integer.parseInt(tokenData));
        }
        if (accept(TokenClass.CHAR_LITERAL)) {
            expect(TokenClass.CHAR_LITERAL);
            return new ChrLiteral(tokenData.charAt(0));
        }
        if (accept(TokenClass.STRING_LITERAL)) {
            expect(TokenClass.STRING_LITERAL);
            return new StrLiteral(tokenData);
        }

        // It should never reach here
        expect(TokenClass.INT_LITERAL, TokenClass.CHAR_LITERAL, TokenClass.STRING_LITERAL);
        // return dummy StrLiteral
        return new StrLiteral("");
    }

    private FunCallExpr parseFuncCall() {
        String name = null;
        Token function = expect(TokenClass.IDENTIFIER);
        if (function != null)
            name = function.data;
        expect(TokenClass.LPAR);
        List<Expr> params = parseFuncCallParamsList();
        expect(TokenClass.RPAR);
        return new FunCallExpr(name, params);
    }

    private List<Expr> parseFuncCallParamsList() {
        if (!accept(TokenClass.RPAR)) {
            List<Expr> params = new ArrayList<>();

            Expr expression = parseExpression();

            params.add(expression);
            List<Expr> paramsRepetitions = parseFuncCallParamsRepetitions();
            if (!paramsRepetitions.isEmpty()) {
                params = Stream.concat(params.stream(), paramsRepetitions.stream()).collect(Collectors.toList());
            }
            return params;
        }
        return new ArrayList<>();
    }

    private List<Expr> parseFuncCallParamsRepetitions() {
        if (!accept(TokenClass.RPAR)) {
            List<Expr> params = new ArrayList<>();
            expect(TokenClass.COMMA);

            Expr expression = parseExpression();

            params.add(expression);
            List<Expr> paramsRepetitions = parseFuncCallParamsRepetitions();
            if (!paramsRepetitions.isEmpty()) {
                params = Stream.concat(params.stream(), paramsRepetitions.stream()).collect(Collectors.toList());
            }
            return params;
        }
        return new ArrayList<>();
    }

    private ArrayAccessExpr parseArrayAccess(Expr inputExpression) {
        expect(TokenClass.LSBR);
        Expr index = parseExpression();
        expect(TokenClass.RSBR);
        return new ArrayAccessExpr(inputExpression, index);
    }

    private FieldAccessExpr parseFieldAccess(Expr inputExpression) {
        String fieldName = null;
        expect(TokenClass.DOT);
        Token field = expect(TokenClass.IDENTIFIER);
        if (field != null)
            fieldName = field.data;
        return new FieldAccessExpr(inputExpression, fieldName);
    }

    private ValueAtExpr parseValueAt() {
        expect(TokenClass.ASTERIX);
        Expr expression = parseExpression();
        return new ValueAtExpr(expression);
    }

    private SizeOfExpr parseSizeOf() {
        expect(TokenClass.SIZEOF);
        expect(TokenClass.LPAR);
        Type type = parseType();
        expect(TokenClass.RPAR);
        return new SizeOfExpr(type);
    }

    private TypecastExpr parseTypeCast() {
        expect(TokenClass.LPAR);
        Type type = parseType();
        expect(TokenClass.RPAR);
        Expr expression = parseExpression();
        return new TypecastExpr(type, expression);
    }
}
