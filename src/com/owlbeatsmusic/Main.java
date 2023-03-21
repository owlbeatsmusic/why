package com.owlbeatsmusic;

import javax.management.MBeanRegistration;
import javax.management.MBeanRegistrationException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {

    /**
     * From :  https://www.github.com/owlbeatsmusic/olib
     * Original name : "fileToString".
     * @param inputFile File to be read.
     * @return String with contents of file using the default line separator.
     */
    public static String readFile(File inputFile) {
        StringBuilder outputString = new StringBuilder();
        ArrayList<String> outputList;
        try {
            outputList = (ArrayList<String>) Files.readAllLines(inputFile.toPath(), Charset.defaultCharset());
            for (String line : outputList) {
                outputString.append(line).append(System.lineSeparator());
            }
        } catch (IOException io) {
            io.printStackTrace();
        }
        return outputString.toString();
    }

    /**
     * From :  https://www.github.com/owlbeatsmusic/olib
     * Creates a arraylist<String> if all lines in a given file.
     * @param inputFile File to read.
     * @return Returns arraylist<String> of lines in file.
     */
    public static ArrayList<String> contentToLines(File inputFile) {
        ArrayList<String> outputList = new ArrayList<>();
        try {
            outputList = (ArrayList<String>) Files.readAllLines(inputFile.toPath(), Charset.defaultCharset());
        } catch (IOException io) {
            io.printStackTrace();
        }
        return outputList;
    }

    enum Token {
        NULL,
        IDENTIFIER,
        EQUALS_OPERATOR,
        BOOLEAN_OPERATOR,
        EXPRESSION,
        END_LINE,
        NEW_LINE,
        KEYWORD,
        CODE,
        OPEN_BRACKET,
        CLOSE_BRACKET,
        OPEN_CURLY_BRACKET,
        CLOSE_CURLY_BRACKET,
        OPEN_PARENTHESIS,
        CLOSE_PARENTHESIS,
        PARAMETERS,
        TYPE,

        END_FILE
    }

    enum ExpressionToken {
        IDENTIFIER,
        SEPARATOR,
        LITERAL,
        OPERATOR,
        PLUS,
        MINUS,
        TIMES,
        DIVIDED
    }

    static ArrayList<Object[]> variables = new ArrayList<>(); // Object{class, name, value}
    static ArrayList<Object[]> tokens = new ArrayList<>(); // {Token, String}
    static String content = readFile(new File("src/com/owlbeatsmusic/test"));
    static ArrayList<String> contentAsLines = contentToLines(new File("src/com/owlbeatsmusic/test"));

    public static int interpretIntExpression(String inputExpression) {

        char[] expression = inputExpression.strip().toCharArray();

        // tokenize equation
        ArrayList<Object[]> equationTokens = new ArrayList<>();
        for (int i = 0; i < expression.length; i++) {

            // IDENTIFIER
            if (Character.isAlphabetic(expression[i])) {
                StringBuilder variable = new StringBuilder(String.valueOf(expression[i]));

                int l = 1;
                while (i+l < expression.length) {
                    if (" ;+-/*".contains(String.valueOf(expression[i + l]))) break;
                    variable.append(expression[i + l]);
                    l++;
                }
                i+=l-1;

                int value = 0;
                for (Object[] var : variables) {
                    if (String.valueOf(var[1]).equals(variable.toString())) {
                        value = (int) var[2];
                    }
                }
                equationTokens.add(new Object[] {ExpressionToken.IDENTIFIER ,String.valueOf(value)});
            }

            // LITERAL
            else if (Character.isDigit(expression[i])) {
                StringBuilder value = new StringBuilder();
                value.append(expression[i]);
                int l = 1;
                try {
                    while (Character.isDigit(expression[i+l])) {
                        value.append(expression[i + l]);
                        l++;
                    }
                    i += l-1;
                } catch (ArrayIndexOutOfBoundsException ignored) {}
                equationTokens.add(new Object[] {ExpressionToken.LITERAL , value.toString()});
            }

            // OPERATION or SEPARATOR
            else {
                ExpressionToken token = ExpressionToken.OPERATOR;
                if ("()".contains(Character.toString(expression[i]))) token = ExpressionToken.SEPARATOR;
                equationTokens.add(new Object[] {token ,String.valueOf(expression[i])});
            }
        }


        // replace parenthesis with value of expression inside with recursion
        ArrayList<Object[]> nonParenthesisSteps = new ArrayList<>();
        int i1 = 0;
        while (i1 < equationTokens.size()) {
            if (equationTokens.get(i1)[1].equals("(")) {
                StringBuilder expressionString = new StringBuilder();
                int expressionLength = 0;
                int j = i1 + 1;
                int startParenthesisCounter = 0;
                while (j < equationTokens.size()) {
                    if ("(".contains(equationTokens.get(j)[1].toString())) startParenthesisCounter++;
                    if (")".contains(equationTokens.get(j)[1].toString())) {
                        expressionString.append(equationTokens.get(j)[1]);
                        if (startParenthesisCounter == 0) {
                            expressionLength = j - i1;
                            j = equationTokens.size();
                        }
                        startParenthesisCounter--;
                    }
                    else {
                        expressionString.append(equationTokens.get(j)[1]);
                    }
                    j++;
                }
                i1 += expressionLength;

                char[] parenthesisExpression = new char[expressionString.length()];
                for (int c = 0; c < expressionString.length(); c++) {
                    parenthesisExpression[c] = expressionString.charAt(c);
                }
                nonParenthesisSteps.add(new Object[]{ExpressionToken.LITERAL, interpretIntExpression(String.valueOf(parenthesisExpression))});
            }
            else {
                nonParenthesisSteps.add(equationTokens.get(i1));
            }
            i1++;
        }


        // arithmetic calculation with all values converted to digits without any parenthesis
        int output = 0;
        ExpressionToken operator = ExpressionToken.PLUS;
        int value2;
        boolean nextValue = false;
        for (int i = 0; i < nonParenthesisSteps.size(); i++) {
            if (nonParenthesisSteps.get(i)[0] == ExpressionToken.IDENTIFIER | nonParenthesisSteps.get(i)[0] == ExpressionToken.LITERAL ) {
                if (!nextValue) {
                    output = Integer.parseInt(nonParenthesisSteps.get(i)[1].toString());
                    nextValue = true;
                }
                else {
                    value2 = Integer.parseInt(nonParenthesisSteps.get(i)[1].toString());
                    switch (operator) {
                        case PLUS -> output = output + value2;
                        case MINUS -> output = output - value2;
                        case TIMES -> output = output * value2;
                        case DIVIDED -> output = output / value2;
                    }
                }
            }
            if (nonParenthesisSteps.get(i)[0] == ExpressionToken.OPERATOR) {
                switch (nonParenthesisSteps.get(i)[1].toString()) {
                    case "+" -> operator = ExpressionToken.PLUS;
                    case "-" -> operator = ExpressionToken.MINUS;
                    case "*" -> operator = ExpressionToken.TIMES;
                    case "/" -> operator = ExpressionToken.DIVIDED;
                }
            }
        }

        return output;
    }
    public static void tokenize(String input) {

        for (int i = 0; i < 2; i++) { tokens.add(new Object[]{Token.NULL, null}); }
        tokens.add(new Object[]{Token.NEW_LINE, ""});
        char[] chars = input.toCharArray();
        int index = -1;
        while (index < chars.length) {
            index++;
            try {

                // END_LINE
                if (chars[index] == ';') tokens.add(new Object[]{Token.END_LINE, ";"});


                // NEW_LINE
                else if (chars[index] == '\n') tokens.add(new Object[]{Token.NEW_LINE,  '\n'});


                // BRACKETS
                else if (chars[index] == '{') tokens.add(new Object[]{Token.OPEN_CURLY_BRACKET,  "{"});
                else if (chars[index] == '}') tokens.add(new Object[]{Token.CLOSE_CURLY_BRACKET, "}"});
                else if (chars[index] == '[') tokens.add(new Object[]{Token.OPEN_BRACKET,        "["});
                else if (chars[index] == ']') tokens.add(new Object[]{Token.CLOSE_BRACKET,       "]"});
                else if (chars[index] == '(') tokens.add(new Object[]{Token.OPEN_PARENTHESIS,    "("});
                else if (chars[index] == ')') tokens.add(new Object[]{Token.CLOSE_PARENTHESIS,   ")"});


                // OPERATORS
                else if ("+-*/".contains(String.valueOf(chars[index])) & "=-<>".contains(String.valueOf(chars[index+1]))) {
                    tokens.add(new Object[]{Token.EQUALS_OPERATOR, Character.toString(chars[index])+ chars[index + 1]});
                    index += 1;
                }
                else if ("<>=".contains(String.valueOf(chars[index])) & "=".contains(String.valueOf(chars[index+1]))) {
                    tokens.add(new Object[]{Token.BOOLEAN_OPERATOR, Character.toString(chars[index])+ chars[index + 1]});
                    index += 1;
                }
                else if (chars[index] == '=') {
                    tokens.add(new Object[]{Token.EQUALS_OPERATOR, "="});
                }
                else if (chars[index] == '<' & chars[index+1] == '-') {
                    tokens.add(new Object[]{Token.EQUALS_OPERATOR, Character.toString(chars[index])+ chars[index + 1]});
                    index += 1;
                }


                // KEYWORD
                else if ((Character.toString(chars[index]) + chars[index + 1]).matches("if") |
                         (Character.toString(chars[index]) + chars[index + 1] + chars[index + 2]).matches("int|set") |
                         (Character.toString(chars[index]) + chars[index + 1] + chars[index + 2] + chars[index + 3]).matches("else") |
                         (Character.toString(chars[index]) + chars[index + 1] + chars[index + 2] + chars[index + 3] + chars[index + 4]).matches("while") |
                         (Character.toString(chars[index]) + chars[index + 1] + chars[index + 2] + chars[index + 3] + chars[index + 4] + chars[index + 5]).matches("string")) {
                    StringBuilder word = new StringBuilder();
                    int l = index;
                    while (l < chars.length) {
                        if (" (".contains(Character.toString(chars[l]))) break;
                        word.append(chars[l]);
                        l++;
                    }
                    if ("if, else, while".contains(word)) tokens.add(new Object[]{Token.KEYWORD, word});
                    if ("int, string, set".contains(word))      tokens.add(new Object[]{Token.TYPE, word});
                    index += word.length();
                }


                // EXPRESSION - wedoinabitofparsin
                boolean isExpression = false;
                try {
                    /*
                        Check if the current token sequence is expecting an expression,
                        then add all characters to the expression waiting until it hits a ';'
                        or when the number of closed parenthesis is more than the number of
                        open ones. (if it's the parenthesis way, then remove the last parenthesis)
                     */
                    if ((tokens.get(tokens.size() - 3)[0] == Token.TYPE & tokens.get(tokens.size() - 2)[0]       == Token.IDENTIFIER & tokens.get(tokens.size() - 1)[0]       == Token.EQUALS_OPERATOR) |
                        (tokens.get(tokens.size() - 2)[0] == Token.IDENTIFIER & tokens.get(tokens.size() - 1)[0] == Token.EQUALS_OPERATOR) |
                        (tokens.get(tokens.size() - 2)[0] == Token.KEYWORD & tokens.get(tokens.size() - 1)[0]    == Token.OPEN_PARENTHESIS) |
                        (tokens.get(tokens.size() - 4)[0] == Token.KEYWORD & tokens.get(tokens.size() - 3)[0]    == Token.OPEN_PARENTHESIS & tokens.get(tokens.size() - 2)[0] == Token.EXPRESSION & tokens.get(tokens.size() - 1)[0] == Token.BOOLEAN_OPERATOR)) {
                        index++;
                        StringBuilder expression = new StringBuilder();
                        int openParenthesis = 0;
                        int closeParenthesis = 0;
                        int l = index;
                        while (l < chars.length) {
                            if (closeParenthesis > openParenthesis | ";=<>".contains(Character.toString(chars[l])))
                                break;
                            if (chars[l] == '(') openParenthesis++;
                            if (chars[l] == ')') closeParenthesis++;
                            expression.append(chars[l]);
                            l++;
                        }
                        if (closeParenthesis > openParenthesis) {
                            expression = new StringBuilder(expression.substring(0, expression.length() - 1));
                        }
                        index += expression.length() - 1;
                        tokens.add(new Object[]{Token.EXPRESSION, interpretIntExpression(String.valueOf(expression))});
                    }


                    //CODE
                    if (tokens.get(tokens.size() - 1)[0] == Token.OPEN_CURLY_BRACKET) {
                        index++;
                        StringBuilder code = new StringBuilder();
                        int openBrackets = 0;
                        int closeBrackets = 0;
                        int l = index;
                        while (l < chars.length) {
                            if (closeBrackets > openBrackets)
                                break;
                            if (chars[l] == '{') openBrackets++;
                            if (chars[l] == '}') closeBrackets++;
                            code.append(chars[l]);
                            l++;
                        }
                        index += code.length()-2;
                        tokens.add(new Object[]{Token.CODE, code.substring(0, code.length()-1)});
                    }

                } catch (IndexOutOfBoundsException ignored) {}


                // IDENTIFIER
                if (Character.isAlphabetic(chars[index]) & !isExpression) {
                    StringBuilder identifier = new StringBuilder();
                    int l = index;
                    while (l <= chars.length) {
                        if (" ,.;+/()[]{}".contains(String.valueOf(chars[l]))) break;
                        identifier.append(chars[l]);
                        l++;
                    }
                    index += identifier.length()-1;
                    tokens.add(new Object[]{Token.IDENTIFIER, identifier.toString()});
                }

            } catch (ArrayIndexOutOfBoundsException ignored) {}
        }
        tokens.add(new Object[]{Token.END_FILE, ""});

        System.out.println(Arrays.deepToString(tokens.toArray()));

    }

    public static void parse() {

        // Allowed grammar
        Token[] KEYWORD_BOOLEAN_CODE       = new Token[]{Token.KEYWORD, Token.OPEN_PARENTHESIS, Token.EXPRESSION, Token.BOOLEAN_OPERATOR, Token.EXPRESSION, Token.CLOSE_PARENTHESIS, Token.OPEN_CURLY_BRACKET, Token.CODE, Token.CLOSE_CURLY_BRACKET};
        Token[] KEYWORD_EXPRESSION         = new Token[]{Token.KEYWORD, Token.EXPRESSION, Token.END_LINE};
        Token[] IDENTIFIER_EQUALS          = new Token[]{Token.IDENTIFIER, Token.EQUALS_OPERATOR, Token.EXPRESSION, Token.END_LINE};
        Token[] IDENTIFIER_FUNCTION        = new Token[]{Token.IDENTIFIER, Token.OPEN_PARENTHESIS, Token.PARAMETERS, Token.CLOSE_PARENTHESIS};
        Token[] TYPE_IDENTIFIER_EXPRESSION = new Token[]{Token.TYPE, Token.IDENTIFIER, Token.OPEN_CURLY_BRACKET, Token.CODE, Token.CLOSE_CURLY_BRACKET};
        Token[] TYPE_IDENTIFIER_CODE       = new Token[]{Token.TYPE, Token.IDENTIFIER, Token.EQUALS_OPERATOR, Token.EXPRESSION, Token.END_LINE};
        Token[][] ALLOWED_GRAMMAR = new Token[][]{KEYWORD_BOOLEAN_CODE, KEYWORD_EXPRESSION, IDENTIFIER_EQUALS, IDENTIFIER_FUNCTION, TYPE_IDENTIFIER_EXPRESSION, TYPE_IDENTIFIER_CODE};

        int lineCounter = 0;
        for (int i = 2; i < tokens.size(); i++) {

            boolean correct = true;
            boolean lineComplete = false;

            for (Token[] allowedToken : ALLOWED_GRAMMAR) {
                if (lineComplete) break;

                try {

                    for (int t = 0; t < allowedToken.length; t++) {
                        if (tokens.get(i + t)[0] == Token.END_FILE) {
                            correct = true;
                            lineComplete = true;

                            break;
                        }
                        else if (tokens.get(i + t)[0] == Token.NEW_LINE) {
                            lineCounter++;
                            i++;
                        }
                        else if (tokens.get(i + t)[0] == allowedToken[t]) {
                            //System.out.println("yes : " + tokens.get(i + t)[1] + " ".repeat(20-tokens.get(i + t)[0].toString().length()) + Arrays.toString(allowedToken));
                            correct = true;
                        }
                        else {
                            correct = false;
                            //System.out.println("no  : " + tokens.get(i + t)[1] + " ".repeat(20-tokens.get(i + t)[0].toString().length()) + Arrays.toString(allowedToken));
                            break;
                        }
                    }

                    if (correct) {
                        System.out.println("grammar : " + Arrays.toString(allowedToken));
                        i += allowedToken.length;
                    }
                } catch (IndexOutOfBoundsException e) {
                    correct = false;
                    break;
                }
            }
            if (!correct) {
                System.out.println("\u001B[31m" + "error \u001B[36m: "+"\033[1;37m"+"unexpected token sequence." + "\u001B[0m");
                System.out.println("\u001B[36m" + "      |" + "\u001B[0m");
                System.out.println("\u001B[36m" + " ".repeat(5-String.valueOf(lineCounter).length()) + lineCounter + " | " + "\u001B[0m" + contentAsLines.get(lineCounter-1));
                System.out.println("\u001B[36m" + "      |" + "\u001B[0m");
                System.exit(-1);
            }
        }
    }

    public static void interpret() {

    }

    public static void main(String[] args) {
        tokenize(content);
        parse();
        interpret();
        //System.out.println(interpretIntExpression("(10+2)*4"));
    }
}
