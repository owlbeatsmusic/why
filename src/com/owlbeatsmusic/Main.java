package com.owlbeatsmusic;

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

    enum Token {
        NULL,
        IDENTIFIER,
        EQUALS_OPERATOR,
        BOOLEAN_OPERATOR,
        EXPRESSION,
        END_LINE,
        KEYWORD,
        CODE,
        OPEN_BRACKET,
        CLOSE_BRACKET,
        OPEN_CURLY_BRACKET,
        CLOSE_CURLY_BRACKET,
        OPEN_PARENTHESIS,
        CLOSE_PARENTHESIS,
        TYPE
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
    static String content = readFile(new File("src/com/owlbeatsmusic/test")).replaceAll(System.lineSeparator(), "") + " ";

    public static int solveIntEquation(String inputEquation) {

        char[] equation = inputEquation.strip().toCharArray();

        // tokenize equation
        ArrayList<Object[]> steps = new ArrayList<>();
        for (int i = 0; i < equation.length; i++) {

            // IDENTIFIER
            if (Character.isAlphabetic(equation[i])) {
                StringBuilder variable = new StringBuilder(String.valueOf(equation[i]));

                int l = 1;
                while (i+l < equation.length) {
                    if (" ;+-/*".contains(String.valueOf(equation[i + l]))) break;
                    variable.append(equation[i + l]);
                    l++;
                }
                i+=l-1;

                int value = 0;
                for (Object[] var : variables) {
                    if (String.valueOf(var[1]).equals(variable.toString())) {
                        value = (int) var[2];
                    }
                }
                steps.add(new Object[] {ExpressionToken.IDENTIFIER ,String.valueOf(value)});
            }

            // LITERAL
            else if (Character.isDigit(equation[i])) {
                StringBuilder value = new StringBuilder();
                value.append(equation[i]);
                int l = 1;
                try {
                    while (Character.isDigit(equation[i+l])) {
                        value.append(equation[i + l]);
                        l++;
                    }
                    i += l-1;
                } catch (ArrayIndexOutOfBoundsException ignored) {}
                steps.add(new Object[] {ExpressionToken.LITERAL , value.toString()});
            }

            // OPERATION or SEPARATOR
            else {
                ExpressionToken token = ExpressionToken.OPERATOR;
                if ("()".contains(Character.toString(equation[i]))) token = ExpressionToken.SEPARATOR;
                steps.add(new Object[] {token ,String.valueOf(equation[i])});
            }
        }


        // replace parenthesis with value of expression inside with recursion
        ArrayList<Object[]> nonParenthesisSteps = new ArrayList<>();
        int i1 = 0;
        while (i1 < steps.size()) {
            if (steps.get(i1)[1].equals("(")) {
                StringBuilder expressionString = new StringBuilder();
                int expressionLength = 0;
                int j = i1 + 1;
                int startParenthesisCounter = 0;
                while (j < steps.size()) {
                    if ("(".contains(steps.get(j)[1].toString())) startParenthesisCounter++;
                    if (")".contains(steps.get(j)[1].toString())) {
                        expressionString.append(steps.get(j)[1]);
                        if (startParenthesisCounter == 0) {
                            expressionLength = j - i1;
                            j = steps.size();
                        }
                        startParenthesisCounter--;
                    }
                    else {
                        expressionString.append(steps.get(j)[1]);
                    }
                    j++;
                }
                i1 += expressionLength;

                char[] parenthesisExpression = new char[expressionString.length()];
                for (int c = 0; c < expressionString.length(); c++) {
                    parenthesisExpression[c] = expressionString.charAt(c);
                }
                nonParenthesisSteps.add(new Object[]{ExpressionToken.LITERAL, solveIntEquation(String.valueOf(parenthesisExpression))});
            }
            else {
                nonParenthesisSteps.add(steps.get(i1));
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
        ArrayList<Object[]> tokens = new ArrayList<>(); // {Token, String}
        for (int i = 0; i < 2; i++) { tokens.add(new Object[]{Token.NULL, null}); }
        char[] chars = input.toCharArray();
        int index = -1;
        while (index < chars.length) {
            index++;
            try {

                // ENDLINE
                if (chars[index] == ';') tokens.add(new Object[]{Token.END_LINE, ";"});


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
                         (Character.toString(chars[index]) + chars[index + 1] + chars[index + 2] + chars[index + 3]).matches("else|func") |
                         (Character.toString(chars[index]) + chars[index + 1] + chars[index + 2] + chars[index + 3] + chars[index + 4]).matches("while") |
                         (Character.toString(chars[index]) + chars[index + 1] + chars[index + 2] + chars[index + 3] + chars[index + 4] + chars[index + 5]).matches("string")) {
                    StringBuilder word = new StringBuilder();
                    int l = index;
                    while (l < chars.length) {
                        if (" (".contains(Character.toString(chars[l]))) break;
                        word.append(chars[l]);
                        l++;
                    }
                    if ("if, else, while, func".contains(word)) tokens.add(new Object[]{Token.KEYWORD, word});
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
                        tokens.add(new Object[]{Token.EXPRESSION, expression});
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
                    index += identifier.length();
                    tokens.add(new Object[]{Token.IDENTIFIER, identifier.toString()});
                }

            } catch (ArrayIndexOutOfBoundsException ignored) {}
        }

        System.out.println(Arrays.deepToString(tokens.toArray()));

    }

    public static void main(String[] args) {
        //tokenize(content);
        System.out.println(solveIntEquation("(10+2)*4"));
    }
}
