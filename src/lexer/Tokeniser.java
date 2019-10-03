package lexer;

import lexer.Token.TokenClass;

import java.io.EOFException;
import java.io.IOException;

/**
 * @author cdubach
 */
public class Tokeniser {

    private Scanner scanner;

    private int error = 0;
    public int getErrorCount() {
	return this.error;
    }

    public Tokeniser(Scanner scanner) {
        this.scanner = scanner;
    }

    private void error(char c, int line, int col) {
        System.out.println("Lexing error: unrecognised character ("+c+") at "+line+":"+col);
	error++;
    }


    public Token nextToken() {
        Token result;
        try {
             result = next();
        } catch (EOFException eof) {
            // end of file, nothing to worry about, just return EOF token
            return new Token(TokenClass.EOF, scanner.getLine(), scanner.getColumn());
        } catch (IOException ioe) {
            ioe.printStackTrace();
            // something went horribly wrong, abort
            System.exit(-1);
            return null;
        }
        return result;
    }

    /*
     * To be completed
     */
    private Token next() throws IOException {

        int line = scanner.getLine();
        int column = scanner.getColumn();

        // get the next character
        char c = scanner.next();

        // skip white spaces
        if (Character.isWhitespace(c))
            return next();

        // recognises the assign operator
        if (c == '=')
            return new Token(TokenClass.ASSIGN, line, column);

        // recognises the left bracket
        if (c == '{')
            return new Token(TokenClass.LBRA, line, column);
        // recognises the right bracket
        if (c == '}')
            return new Token(TokenClass.RBRA, line, column);
        // recognises the left parentheses
        if (c == '(')
            return new Token(TokenClass.LPAR, line, column);
        // recognises the right parentheses
        if (c == ')')
            return new Token(TokenClass.RPAR, line, column);
        // recognises the left square bracket
        if (c == '[')
            return new Token(TokenClass.LSBR, line, column);
        // recognises the right square bracket
        if (c == ']')
            return new Token(TokenClass.RSBR, line, column);
        // recognises the semicolon
        if (c == ';')
            return new Token(TokenClass.SC, line, column);
        // recognises the comma
        if (c == ',')
            return new Token(TokenClass.COMMA, line, column);

        // recognises the plus operator
        if (c == '+')
            return new Token(TokenClass.PLUS, line, column);
        // recognises the minus operator
        if (c == '-')
            return new Token(TokenClass.MINUS, line, column);
        // recognises the asterix character (multiplication or pointers)
        if (c == '*')
            return new Token(TokenClass.ASTERIX, line, column);
        // recognises the division operator
        if (c == '/')
            return new Token(TokenClass.DIV, line, column);
        // recognises the modulo operator
        if (c == '%')
            return new Token(TokenClass.REM, line, column);

        // recognises the dot operator
        if (c == '.')
            return new Token(TokenClass.DOT, line, column);

        // ... to be completed


        // if we reach this point, it means we did not recognise a valid token
        error(c, line, column);
        return new Token(TokenClass.INVALID, line, column);
    }


}
