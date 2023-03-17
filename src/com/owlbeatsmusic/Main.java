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
        ENDLINE,
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


    static ArrayList<Object[]> variables = new ArrayList<>(); // Object{class, name, value}

    enum ExpressionToken {
        IDENTIFIER,
        SEPARATOR,
        ExpressionOperator,
        LITERAL
    }
    enum ExpressionOperator {
        PLUS,
        MINUS,
        TIMES,
        DIVIDED
    }
    public static int solveIntExpression(char[] inputEquation) {

        // count all characters that aren't space
        int length = 0;
        for (int i = 0; i < inputEquation.length; i++) {
            if (inputEquation[i] != 32) length++;
        }

        // create the new equation without spaces
        char[] equation =  new char[length];
        int index = 0;
        for (int i = 0; i < inputEquation.length; i++) {
            if (inputEquation[i] != 32) {
                equation[index] = inputEquation[i];
                index++;
            }
        }
        // create steps before calculation
        ArrayList<Object[]> steps = new ArrayList<>();
        for (int i = 0; i < equation.length; i++) {

            // Variable (adds the value of the variable)
            if (Character.isAlphabetic(equation[i])) {
                int value = 0;
                StringBuilder variable = new StringBuilder(String.valueOf(equation[i]));

                int l = 1;
                while (i+l < equation.length) {
                    if (" ;+-/*".contains(String.valueOf(equation[i + l]))) break;
                    variable.append(equation[i + l]);
                    l++;
                }
                i+=l-1;

                for (Object[] var : variables) {
                    if (String.valueOf(var[1]).equals(variable.toString())) {
                        value = (int) var[2];
                    }
                }
                steps.add(new Object[] {ExpressionToken.IDENTIFIER ,String.valueOf(value)});
            }

            // Digit
            else if (Character.isDigit(equation[i])) {
                String value = "";
                value += equation[i];
                int l = 1;
                try {
                    while (Character.isDigit(equation[i+l])) {
                        value += equation[i+l];
                        l++;
                    }
                    i += l-1;
                } catch (ArrayIndexOutOfBoundsException ignored) {}
                steps.add(new Object[] {ExpressionToken.LITERAL , value});
            }

            // Operation or Separator
            else {
                ExpressionToken token = ExpressionToken.ExpressionOperator;
                if ("()".contains(Character.toString(equation[i]))) token = ExpressionToken.SEPARATOR;
                steps.add(new Object[] {token ,String.valueOf(equation[i])});

            }
            // System.out.println(Arrays.deepToString(steps.toArray()));
        }

        int output;
        int value1 = 0;
        ExpressionOperator operator = ExpressionOperator.PLUS;
        int value2;
        boolean nextValue = false;

        // replace parenthesis with value of expression inside with recursion
        ArrayList<Object[]> nonParenthesisSteps = new ArrayList<>();
        int i1 = 0;
        while (i1 < steps.size()) {
            if (steps.get(i1)[1].equals("(")) {
                String expressionString = "";
                int expressionLength = 0;
                int j = i1 + 1;
                int startParenthesisCounter = 0;
                while (j < steps.size()) {
                    if ("(".contains(steps.get(j)[1].toString())) startParenthesisCounter++;
                    if (")".contains(steps.get(j)[1].toString())) {
                        expressionString += steps.get(j)[1];
                        if (startParenthesisCounter == 0) {
                            expressionLength = j - i1;
                            j = steps.size();
                        }
                        startParenthesisCounter--;
                    }
                    else {
                        expressionString += steps.get(j)[1];
                    }
                    j++;
                }
                i1 += expressionLength;

                char[] parenthesisExpression = new char[expressionString.length()];
                for (int c = 0; c < expressionString.length(); c++) {
                    parenthesisExpression[c] = expressionString.charAt(c);
                }
                nonParenthesisSteps.add(new Object[]{ExpressionToken.LITERAL, solveIntExpression(parenthesisExpression)});
            }
            else {
                nonParenthesisSteps.add(steps.get(i1));
            }
            i1++;
        }

        // arithmetic calculation with all values converted to digits without any parenthesis
        for (int i = 0; i < nonParenthesisSteps.size(); i++) {
            if (nonParenthesisSteps.get(i)[0] == ExpressionToken.IDENTIFIER | nonParenthesisSteps.get(i)[0] == ExpressionToken.LITERAL ) {
                if (!nextValue) {
                    value1 = Integer.parseInt(nonParenthesisSteps.get(i)[1].toString());
                    nextValue = true;
                }
                else {
                    value2 = Integer.parseInt(nonParenthesisSteps.get(i)[1].toString());
                    switch (operator) {
                        case PLUS -> value1 = value1 + value2;
                        case MINUS -> value1 = value1 - value2;
                        case TIMES -> value1 = value1 * value2;
                        case DIVIDED -> value1 = value1 / value2;
                    }
                }
            }
            if (nonParenthesisSteps.get(i)[0] == ExpressionToken.SEPARATOR) {

            }
            if (nonParenthesisSteps.get(i)[0] == ExpressionToken.ExpressionOperator) {
                switch (nonParenthesisSteps.get(i)[1].toString()) {
                    case "+" -> operator = ExpressionOperator.PLUS;
                    case "-" -> operator = ExpressionOperator.MINUS;
                    case "*" -> operator = ExpressionOperator.TIMES;
                    case "/" -> operator = ExpressionOperator.DIVIDED;
                }
            }
        }

        output = value1;

        return output;
    }
    static String content = readFile(new File("src/com/owlbeatsmusic/test")) + " ";
    public static void tokenize() {
        ArrayList<Object[]> tokens = new ArrayList<>(); // {Token, String}
        for (int i = 0; i < 5; i++) { tokens.add(new Object[]{Token.NULL, null}); }
        char[] chars = content.toCharArray();
        StringBuilder tokenizedWithoutExpression = new StringBuilder();
        int index = -1;
        while (index < chars.length) {
            index++;
            try {
                // OPERATORS
                if ("+-*/".contains(String.valueOf(chars[index])) & "=-<>".contains(String.valueOf(chars[index+1]))) {
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
                else if ((Character.toString(chars[index]) + chars[index + 1]).equals("if") |
                         (Character.toString(chars[index]) + chars[index + 1] + chars[index + 2] + chars[index + 3]).equals("else") |
                         (Character.toString(chars[index]) + chars[index + 1] + chars[index + 2] + chars[index + 3] + chars[index + 4]).equals("while") |
                         (Character.toString(chars[index]) + chars[index + 1] + chars[index + 2] + chars[index + 3]).equals("func") |
                         (Character.toString(chars[index]) + chars[index + 1] + chars[index + 2]).equals("int") ) {
                    StringBuilder keyword = new StringBuilder();
                    int l = index;
                    while (l < chars.length) {
                        if (" (".contains(Character.toString(chars[l]))) break;
                        keyword.append(chars[l]);
                        l++;
                    }
                    tokens.add(new Object[]{Token.KEYWORD, keyword});
                    index += keyword.length();
                }


                // TYPE
                else if (chars[index] == 'i' & chars[index + 1] == 'n' & chars[index + 2] == 't' & chars[index + 3] == ' ') {
                    tokens.add(new Object[]{Token.TYPE, "int"});
                    index += 3;
                } else if (chars[index] == 's' & chars[index + 1] == 't' & chars[index + 2] == 'r' & chars[index + 3] == 'i' & chars[index + 4] == 'n' & chars[index + 5] == 'g' & chars[index + 6] == ' ') {
                    tokens.add(new Object[]{Token.TYPE, "string"});
                    index += 5;
                } else if (chars[index] == 's' & chars[index + 1] == 'e' & chars[index + 2] == 't' & chars[index + 3] == ' ') {
                    tokens.add(new Object[]{Token.TYPE, "set"});
                    index += 3;
                }
                /*
                else if (chars[index] == 'i' & chars[index + 1] == 'f') {
                    tokens.add(new Object[]{Token.KEYWORD, "if"});
                    index += 1;
                } else if (chars[index] == 'e' & chars[index + 1] == 'l' & chars[index + 2] == 's' & chars[index + 3] == 'e') {
                    tokens.add(new Object[]{Token.KEYWORD, "else"});
                    index += 2;
                } else if (chars[index] == 'w' & chars[index + 1] == 'h' & chars[index + 2] == 'i' & chars[index + 3] == 'l' & chars[index + 4] == 'e') {
                    tokens.add(new Object[]{Token.KEYWORD, "while"});
                    index += 3;
                } else if (chars[index] == 'f' & chars[index + 1] == 'u' & chars[index + 2] == 'n' & chars[index + 3] == 'c') {
                    tokens.add(new Object[]{Token.KEYWORD, "func"});
                    index += 2;
                }

                 */


                // BRACKETS
                else if (chars[index] == '{') tokens.add(new Object[]{Token.OPEN_CURLY_BRACKET,  "{"});
                else if (chars[index] == '}') tokens.add(new Object[]{Token.CLOSE_CURLY_BRACKET, "}"});
                else if (chars[index] == '[') tokens.add(new Object[]{Token.OPEN_BRACKET,        "["});
                else if (chars[index] == ']') tokens.add(new Object[]{Token.CLOSE_BRACKET,       "]"});
                else if (chars[index] == '(') tokens.add(new Object[]{Token.OPEN_PARENTHESIS,    "("});
                else if (chars[index] == ')') tokens.add(new Object[]{Token.CLOSE_PARENTHESIS,   ")"});


                // ENDLINE
                else if (chars[index] == ';') tokens.add(new Object[]{Token.ENDLINE, ";"});


                // EXPRESSION
                boolean isExpression = false;
                try {
                    if ((tokens.get(tokens.size() - 3)[0] == Token.TYPE & tokens.get(tokens.size() - 2)[0] == Token.IDENTIFIER & tokens.get(tokens.size() - 1)[0] == Token.EQUALS_OPERATOR) |
                        (tokens.get(tokens.size() - 2)[0] == Token.IDENTIFIER & tokens.get(tokens.size() - 1)[0] == Token.EQUALS_OPERATOR) |
                        (tokens.get(tokens.size() - 2)[0] == Token.KEYWORD & tokens.get(tokens.size() - 1)[0] == Token.OPEN_PARENTHESIS) |
                        (tokens.get(tokens.size() - 4)[0] == Token.KEYWORD & tokens.get(tokens.size() - 3)[0] == Token.OPEN_PARENTHESIS & tokens.get(tokens.size() - 2)[0] == Token.EXPRESSION & tokens.get(tokens.size() - 1)[0] == Token.BOOLEAN_OPERATOR)) {
                        System.out.println("expreession");
                        isExpression = true;
                    }
                } catch (IndexOutOfBoundsException ignored) {
                    System.out.println(ignored);
                }

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
        tokenize();
    }
}
