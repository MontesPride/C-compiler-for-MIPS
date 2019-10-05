package lexer;

import lexer.Token.TokenClass;

import java.io.EOFException;
import java.io.IOException;

/**
 * @author sfilipiak
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

        // recognises the #include
        if (c == '#') {
            StringBuilder data = new StringBuilder();
            String include = "include";
            for (int i = 0; i < include.length(); i++) {
                if (scanner.peek() == include.charAt(i)) {
                    data.append(include.charAt(i));
                    c = scanner.next();
                } else {
                    break;
                }
            }
            if (data.toString().equals(include)) {
                return new Token(TokenClass.INCLUDE, line, column);
            } else {
                error(c, line, column);
                return new Token(TokenClass.INVALID, line, column);
            }
        }

        //recognizes the single line comment
        if (c == '/' && scanner.peek() == '/') {
            while (scanner.peek() != 0 && scanner.peek() != '\n')
                scanner.next();
            scanner.next();
            return next();
        }

        //recognises the multi line comment
        if (c == '/' && scanner.peek() == '*') {
            while (!(c == '*' && scanner.peek() == '/'))
                c = scanner.next();
            scanner.next();
            return next();
        }

        // recognises the string literal
        if (c == '"') {
            StringBuilder data = new StringBuilder();
            while (scanner.peek() != '"') {
                if (scanner.peek() == '\\') {
                    scanner.next();
                    switch (scanner.peek()) {
                        case 't':
                            data.append('\t');
                            break;
                        case 'b':
                            data.append('\b');
                            break;
                        case 'n':
                            data.append('\n');
                            break;
                        case 'r':
                            data.append('\r');
                            break;
                        case 'f':
                            data.append('\f');
                            break;
                        case '0':
                            data.append('\0');
                            break;
                        default:
                            data.append(scanner.peek());
                            break;
                    }
                } else {
                    data.append(scanner.peek());
                }
                scanner.next();
            }
            scanner.next();
            return new Token(TokenClass.STRING_LITERAL, data.toString(), line, column);
        }

        // recognises the char literal
        if (c == '\'') {
            StringBuilder data = new StringBuilder();
            if (scanner.peek() == '\\') {
                scanner.next();
                switch (scanner.peek()) {
                    case 't':
                        data.append('\t');
                        break;
                    case 'b':
                        data.append('\b');
                        break;
                    case 'n':
                        data.append('\n');
                        break;
                    case 'r':
                        data.append('\r');
                        break;
                    case 'f':
                        data.append('\f');
                        break;
                    case '0':
                        data.append('\0');
                        break;
                    default:
                        data.append(scanner.peek());
                        break;
                }
            } else {
                data.append(scanner.peek());
            }

            c = scanner.next();
            if (scanner.peek() == '\'') {
                scanner.next();
                return new Token(TokenClass.CHAR_LITERAL, data.toString(), line, column);
            } else {
                error(c, line, column);
                return new Token(TokenClass.INVALID, line, column);
            }
        }

        // recognises the int literal
        if (Character.isDigit(c)) {
            StringBuilder data = new StringBuilder(String.valueOf(c));
            while (Character.isDigit(scanner.peek())) {
                data.append(scanner.peek());
                scanner.next();
            }
            return new Token(TokenClass.INT_LITERAL, data.toString(), line, column);
        }

        if (Character.isLetter(c) || c == '_') {
            StringBuilder data = new StringBuilder(String.valueOf(c));
            while (Character.isLetterOrDigit(scanner.peek()) || scanner.peek() == '_') {
                c = scanner.next();
                data.append(c);
            }
            // recognises the void type declaration
            if (data.toString().equals("void"))
                return new Token(TokenClass.VOID, line, column);
            // recognises the char type declaration
            if (data.toString().equals("char"))
                return new Token(TokenClass.CHAR, line, column);
            // recognises the int type declaration
            if (data.toString().equals("int"))
                return new Token(TokenClass.INT, line, column);
            if (data.toString().equals("if"))
                return new Token(TokenClass.IF, line, column);
            if (data.toString().equals("else"))
                return new Token(TokenClass.ELSE, line, column);
            if (data.toString().equals("while"))
                return new Token(TokenClass.WHILE, line, column);
            if (data.toString().equals("return"))
                return new Token(TokenClass.RETURN, line, column);
            if (data.toString().equals("struct"))
                return new Token(TokenClass.STRUCT, line, column);
            if (data.toString().equals("sizeof"))
                return new Token(TokenClass.SIZEOF, line, column);

            // recognizes the identifier
            return new Token(TokenClass.IDENTIFIER, data.toString(), line, column);
        }


        if (c == '=') {
            if (scanner.peek() == '=') {
                // recognises the equality operator
                scanner.next();
                return new Token(TokenClass.EQ, line, column);
            } else {
                // recognises the assign operator
                return new Token(TokenClass.ASSIGN, line, column);
            }
        }

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
        // recognises the less than operator
        if (c == '<' && scanner.peek() != '=') {
            return new Token(TokenClass.LT, line, column);
        }
        // recognises the greater than operator
        if (c == '>' && scanner.peek() != '=') {
            return new Token(TokenClass.GT, line, column);
        }
        // recognises the less or equal operator
        if (c == '<' && scanner.peek() == '=') {
            scanner.next();
            return new Token(TokenClass.LE, line, column);
        }
        // recognises the greater or equal operator
        if (c == '>' && scanner.peek() == '=') {
            scanner.next();
            return new Token(TokenClass.GE, line, column);
        }
        // recognises the inequality operator
        if (c == '!' && scanner.peek() == '=') {
            scanner.next();
            return new Token(TokenClass.NE, line, column);
        }
        // recognises the AND operator
        if (c == '&' && scanner.peek() == '&') {
            scanner.next();
            return new Token(TokenClass.AND, line, column);
        }
        // recognises the OR operator
        if (c == '|' && scanner.peek() == '|'){
            scanner.next();
            return new Token(TokenClass.OR, line, column);
        }

        // ... to be completed

        // if we reach this point, it means we did not recognise a valid token
        error(c, line, column);
        return new Token(TokenClass.INVALID, line, column);
    }


}
