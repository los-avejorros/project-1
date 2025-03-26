package com.avejorros.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.avejorros.bean.LispList;
import com.avejorros.bean.LispNumber;
import com.avejorros.bean.LispSymbol;
import com.avejorros.interfaces.Expression;

public class ParserLisp {
  public static Expression<?> parse(String input) {
    List<String> tokens = tokenize(input);
    return parseTokens(tokens);
  }

  private static List<String> tokenize(String input) {
    List<String> tokens = new ArrayList<>();
    StringBuilder currentToken = new StringBuilder();
    boolean inQuotes = false;

    for (char ch : input.toCharArray()) {
      if (ch == ';') {
        // Ignorar el resto de la línea (comentario)
        break;
      }
      if (ch == ' ' && !inQuotes) {
        if (currentToken.length() > 0) {
          tokens.add(currentToken.toString());
          currentToken = new StringBuilder();
        }
      } else if (ch == '(' || ch == ')') {
        if (currentToken.length() > 0) {
          tokens.add(currentToken.toString());
          currentToken = new StringBuilder();
        }
        tokens.add(String.valueOf(ch));
      } else {
        currentToken.append(ch);
      }
    }

    if (currentToken.length() > 0) {
      tokens.add(currentToken.toString());
    }

    return tokens;
  }

  private static Expression<?> parseTokens(List<String> tokens) {
    Stack<List<Expression<?>>> stack = new Stack<>();
    stack.push(new ArrayList<>());

    for (String token : tokens) {
      if (token.equals("(")) {
        stack.push(new ArrayList<>());
      } else if (token.equals(")")) {
        List<Expression<?>> currentList = stack.pop();
        LispList<Object> lispList = new LispList<>();
        for (Expression<?> expr : currentList) {
          lispList.addExpression(expr);
        }
        stack.peek().add(lispList);
      } else {
        stack.peek().add(parseAtom(token));
      }
    }

    if (stack.size() != 1 || stack.peek().size() != 1) {
      throw new RuntimeException("Expresión no válida");
    }

    return stack.peek().get(0);
  }

  private static Expression<?> parseAtom(String token) {
    // Verificar si el token es un operador
    System.out.println("TOKEN: " + token);
    if (isOperator(token)) {
      return new LispSymbol<>(token);
    }

    // Intentar convertir el token en un número
    try {
      double number = Double.parseDouble(token);
      return new LispNumber(number);
    } catch (NumberFormatException e) {
      // Si no es un número, asumir que es un símbolo
      return new LispSymbol<>(token);
    }
  }

  private static boolean isOperator(String token) {
    // Lista de operadores válidos
    return token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/") ||
           token.equals("<") || token.equals("<=") || token.equals(">") || token.equals(">=") || 
           token.equals("=") || token.equals("AND") || token.equals("SQRT") ||
           token.equals("QUOTE") || token.equals("SETQ") || token.equals("COND") ||
           token.equals("DEFUN") || token.equals("ATOM") || token.equals("LIST") ||
           token.equals("EQUAL") || token.equals("CAR") || token.equals("CDR") ||
           token.equals("PRINT");
  }

}
