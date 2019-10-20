package parser;

import ast.*;

import lexer.Token;
import lexer.Tokeniser;
import lexer.Token.TokenClass;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


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
        System.out.println("Parsing error: expected ("+sb+") found ("+token+") at "+token.position);

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

        int cnt=1;
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
<<<<<<< HEAD
        parseStructDecls();
        parseVarAndFunDecls();
=======
>>>>>>> a946d269f5a4a74e8124de992ea0c9133fb548b5
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
        // to be completed ...
<<<<<<< HEAD
        if (accept(TokenClass.STRUCT)) {
            nextToken();
            expect(TokenClass.IDENTIFIER);
            expect(TokenClass.LBRA);
            parseStructValDecls();
            expect(TokenClass.RBRA);
            expect(TokenClass.SC);
            parseStructDecls();
        }
    }

    private void parseStructValDecls() {
        if (accept(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID, TokenClass.STRUCT))
            parseVarDeclsOnly();
        else
            expect(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID, TokenClass.STRUCT);
    }

    private void parseVarAndFunDecls() {
        if (accept(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID, TokenClass.STRUCT)) {
            parseType();
            expect(TokenClass.IDENTIFIER);
            if (accept(TokenClass.SC, TokenClass.LSBR)) {
                parseVarDecls();
            } else if (accept(TokenClass.LPAR)) {
                parseFunDecls();
            } else {
                expect(TokenClass.SC, TokenClass.LSBR, TokenClass.LPAR);
            }
        }
=======
>>>>>>> a946d269f5a4a74e8124de992ea0c9133fb548b5
    }

    private List<VarDecl> parseVarDecls() {
        // to be completed ...
<<<<<<< HEAD
        if (accept(TokenClass.SC,TokenClass.LSBR)) {
            Token varDeclToken = expect(TokenClass.SC, TokenClass.LSBR);
            if (varDeclToken != null && varDeclToken.tokenClass.equals(TokenClass.LSBR)) {
                expect(TokenClass.INT_LITERAL);
                expect(TokenClass.RSBR);
                expect(TokenClass.SC);
            }
            parseVarAndFunDecls();
        }
    }

    private void parseVarDeclsOnly() {
        if (accept(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID, TokenClass.STRUCT)) {
            parseType();
            expect(TokenClass.IDENTIFIER);
            if (accept(TokenClass.SC,TokenClass.LSBR)) {
                Token varDeclOnlyToken = expect(TokenClass.SC, TokenClass.LSBR);
                if (varDeclOnlyToken != null && varDeclOnlyToken.tokenClass.equals(TokenClass.LSBR)) {
                    expect(TokenClass.INT_LITERAL);
                    expect(TokenClass.RSBR);
                    expect(TokenClass.SC);
                }
                parseVarDeclsOnly();
            } else {
                expect(TokenClass.SC,TokenClass.LSBR);
            }
        }
=======
>>>>>>> a946d269f5a4a74e8124de992ea0c9133fb548b5
    }

    private List<FunDecl> parseFunDecls() {
        // to be completed ...
<<<<<<< HEAD
        if (accept(TokenClass.LPAR)) {
            nextToken();
            parseParamsList();
            expect(TokenClass.RPAR);
            parseBlock();
            parseFunDeclsOnly();
        }
    }

    private void parseFunDeclsOnly() {
        if (accept(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID, TokenClass.STRUCT)) {
            parseType();
            expect(TokenClass.IDENTIFIER);
            if (accept(TokenClass.LPAR)) {
                nextToken();
                parseParamsList();
                expect(TokenClass.RPAR);
                parseBlock();
                parseFunDeclsOnly();
            } else {
                expect(TokenClass.LPAR);
            }
        }
    }

    private void parseType() {
        if (accept(TokenClass.STRUCT))
            parseStructType();
        else
            expect(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID);
        if (accept(TokenClass.ASTERIX))
            nextToken();
    }

    private void parseStructType() {
        expect(TokenClass.STRUCT);
        expect(TokenClass.IDENTIFIER);
    }

    private void parseParamsList() {
        if (!accept(TokenClass.RPAR)) {
            parseType();
            expect(TokenClass.IDENTIFIER);
            parseParamsRepetitions();
        }
    }

    private void parseParamsRepetitions() {
        if (!accept(TokenClass.RPAR)) {
            expect(TokenClass.COMMA);
            parseType();
            expect(TokenClass.IDENTIFIER);
            parseParamsRepetitions();
        }
=======
>>>>>>> a946d269f5a4a74e8124de992ea0c9133fb548b5
    }

    private void parseStatement() {
        if (accept(TokenClass.LBRA, TokenClass.WHILE, TokenClass.IF, TokenClass.RETURN, TokenClass.LPAR, TokenClass.MINUS, TokenClass.ASTERIX, TokenClass.SIZEOF, TokenClass.IDENTIFIER, TokenClass.INT_LITERAL, TokenClass.CHAR_LITERAL, TokenClass.STRING_LITERAL)) {
            switch (token.tokenClass) {
                case LBRA:
                    parseBlock();
                    break;
                case WHILE:
                    nextToken();
                    expect(TokenClass.LPAR);
                    parseExpression();
                    expect(TokenClass.RPAR);
                    parseStatementOnly();
                    break;
                case IF:
                    nextToken();
                    expect(TokenClass.LPAR);
                    parseExpression();
                    expect(TokenClass.RPAR);
                    parseStatementOnly();
                    if (accept(TokenClass.ELSE)) {
                        nextToken();
                        parseStatementOnly();
                    }
                    break;
                case RETURN:
                    nextToken();
                    if (!accept(TokenClass.SC)) {
                        parseExpression();
                    }
                    expect(TokenClass.SC);
                    break;
                case LPAR:
                case MINUS:
                case ASTERIX:
                case SIZEOF:
                case IDENTIFIER:
                case INT_LITERAL:
                case CHAR_LITERAL:
                case STRING_LITERAL:
                    parseExpression();
                    Token statementToken = expect(TokenClass.ASSIGN, TokenClass.SC);
                    if (statementToken != null && statementToken.tokenClass.equals(TokenClass.ASSIGN)) {
                        parseExpression();
                        expect(TokenClass.SC);
                    }
                    break;
                default:
                    expect(TokenClass.INVALID);
                    break;
            }
            parseStatement();
        }
    }

    private void parseStatementOnly() {
        if (accept(TokenClass.LBRA, TokenClass.WHILE, TokenClass.IF, TokenClass.RETURN, TokenClass.LPAR, TokenClass.MINUS, TokenClass.ASTERIX, TokenClass.SIZEOF, TokenClass.IDENTIFIER, TokenClass.INT_LITERAL, TokenClass.CHAR_LITERAL, TokenClass.STRING_LITERAL)) {
            switch (token.tokenClass) {
                case LBRA:
                    parseBlock();
                    break;
                case WHILE:
                    nextToken();
                    expect(TokenClass.LPAR);
                    parseExpression();
                    expect(TokenClass.RPAR);
                    parseStatementOnly();
                    break;
                case IF:
                    nextToken();
                    expect(TokenClass.LPAR);
                    parseExpression();
                    expect(TokenClass.RPAR);
                    parseStatementOnly();
                    if (accept(TokenClass.ELSE)) {
                        nextToken();
                        parseStatementOnly();
                    }
                    break;
                case RETURN:
                    nextToken();
                    if (!accept(TokenClass.SC)) {
                        parseExpression();
                    }
                    expect(TokenClass.SC);
                    break;
                case LPAR:
                case MINUS:
                case ASTERIX:
                case SIZEOF:
                case IDENTIFIER:
                case INT_LITERAL:
                case CHAR_LITERAL:
                case STRING_LITERAL:
                    parseExpression();
                    Token statementToken = expect(TokenClass.ASSIGN, TokenClass.SC);
                    if (statementToken != null && statementToken.tokenClass.equals(TokenClass.ASSIGN)) {
                        parseExpression();
                        expect(TokenClass.SC);
                    }
                    break;
                default:
                    expect(TokenClass.INVALID);
                    break;
            }
        } else {
            expect(TokenClass.LBRA, TokenClass.WHILE, TokenClass.IF, TokenClass.RETURN, TokenClass.LPAR, TokenClass.MINUS, TokenClass.ASTERIX, TokenClass.SIZEOF, TokenClass.IDENTIFIER, TokenClass.INT_LITERAL, TokenClass.CHAR_LITERAL, TokenClass.STRING_LITERAL);
        }
    }

    private void parseBlock() {
        expect(TokenClass.LBRA);
        parseVarDeclsOnly();
        parseStatement();
        expect(TokenClass.RBRA);
    }

    private void parseExpression() {
        if (accept(TokenClass.LPAR, TokenClass.MINUS, TokenClass.ASTERIX, TokenClass.SIZEOF, TokenClass.IDENTIFIER, TokenClass.INT_LITERAL, TokenClass.CHAR_LITERAL, TokenClass.STRING_LITERAL)) {
            switch (token.tokenClass) {
                case LPAR:
                    Token expressionLPARToken = lookAhead(1);
                    if (expressionLPARToken != null && (expressionLPARToken.tokenClass.equals(TokenClass.INT) || expressionLPARToken.tokenClass.equals(TokenClass.CHAR) || expressionLPARToken.tokenClass.equals(TokenClass.VOID) || expressionLPARToken.tokenClass.equals(TokenClass.STRUCT)))
                        parseTypeCast();
                    else {
                        nextToken();
                        parseExpression();
                        expect(TokenClass.RPAR);
                    }
                    break;
                case MINUS:
                    nextToken();
                    parseExpression();
                    break;
                case ASTERIX:
                    parseValueAt();
                    break;
                case SIZEOF:
                    parseSizeOf();
                    break;
                case IDENTIFIER:
                    Token identifierToken = lookAhead(1);
                    if (identifierToken != null && identifierToken.tokenClass.equals(TokenClass.LPAR))
                        parseFuncCall();
                    else
                        nextToken();
                    break;
                default:
                    expect(TokenClass.INT_LITERAL, TokenClass.CHAR_LITERAL, TokenClass.STRING_LITERAL);
                    break;
            }
            parseExpressionWithoutLeftRecursion();
        } else {
            expect(TokenClass.LPAR, TokenClass.MINUS, TokenClass.ASTERIX, TokenClass.SIZEOF, TokenClass.IDENTIFIER, TokenClass.INT_LITERAL, TokenClass.CHAR_LITERAL, TokenClass.STRING_LITERAL);
        }
    }

    private void parseExpressionWithoutLeftRecursion() {
        if (accept(TokenClass.GT, TokenClass.LT, TokenClass.GE, TokenClass.LE, TokenClass.NE, TokenClass.EQ, TokenClass.PLUS, TokenClass.MINUS, TokenClass.DIV, TokenClass.ASTERIX, TokenClass.REM, TokenClass.OR, TokenClass.AND, TokenClass.LSBR, TokenClass.DOT)) {
            switch (token.tokenClass) {
                case GT:
                case LT:
                case GE:
                case LE:
                case NE:
                case EQ:
                case PLUS:
                case MINUS:
                case DIV:
                case ASTERIX:
                case REM:
                case OR:
                case AND:
                    nextToken();
                    parseExpression();
                    parseExpressionWithoutLeftRecursion();
                    break;
                case LSBR:
                    parseArrayAccess();
                    break;
                case DOT:
                    parseFieldAccess();
                    break;
                default:
                    expect(TokenClass.INVALID);
                    break;
            }
        }
    }

    private void parseFuncCall() {
        expect(TokenClass.IDENTIFIER);
        expect(TokenClass.LPAR);
        parseFuncCallParamsList();
        expect(TokenClass.RPAR);
    }

    private void parseFuncCallParamsList() {
        if (!accept(TokenClass.RPAR)) {
            parseExpression();
            parseFuncCallParamsRepetitions();
        }
    }

    private void parseFuncCallParamsRepetitions() {
        if (!accept(TokenClass.RPAR)) {
            expect(TokenClass.COMMA);
            parseExpression();
            parseFuncCallParamsRepetitions();
        }
    }

    private void parseArrayAccess() {
        expect(TokenClass.LSBR);
        parseExpression();
        expect(TokenClass.RSBR);
        parseExpressionWithoutLeftRecursion();
    }

    private void parseFieldAccess() {
        expect(TokenClass.DOT);
        expect(TokenClass.IDENTIFIER);
        parseExpressionWithoutLeftRecursion();
    }

    private void parseValueAt() {
        expect(TokenClass.ASTERIX);
        parseExpression();
    }

    private void parseSizeOf() {
        expect(TokenClass.SIZEOF);
        expect(TokenClass.LPAR);
        parseType();
        expect(TokenClass.RPAR);
    }

    private void parseTypeCast() {
        expect(TokenClass.LPAR);
        parseType();
        expect(TokenClass.RPAR);
        parseExpression();
    }
}
