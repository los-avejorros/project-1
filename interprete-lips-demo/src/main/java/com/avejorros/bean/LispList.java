package com.avejorros.bean;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.avejorros.interfaces.Expression;

public class LispList<T> implements Expression<T> {

  private Queue<Expression<?>> expressions;

  public LispList() {
    expressions = new LinkedList<>();
  }

  public void addExpression(Expression<?> expr) {
    expressions.add(expr);
  }

  public Queue<Expression<?>> getExpressions() {
    return expressions;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T evaluate(Environment env) {
    if (expressions.isEmpty()) {
      throw new RuntimeException("Lista vacía");
    }

    // Crear una copia de la cola para no modificar la original
    Queue<Expression<?>> expressionsCopy = new LinkedList<>(expressions);

    // Obtener el operador (primer elemento de la lista)
    Expression<?> first = expressionsCopy.poll();

    if (first instanceof LispSymbol) {
      String operator = ((LispSymbol<?>) first).getName();
      System.out.println("Operador: " + operator);

      // Evaluar todos los operandos restantes
      Queue<Object> evaluatedOperands = new LinkedList<>();
      if (!operator.equals("SETQ") && !operator.equals("DEFUN") && !operator.equals("COND")) {
        while (!expressionsCopy.isEmpty()) {
          Expression<?> expr = expressionsCopy.poll();
          evaluatedOperands.add(expr.evaluate(env));
        }
      } else {
        // Para SETQ, DEFUN, COND, mantener las expresiones sin evaluar
        while (!expressionsCopy.isEmpty()) {
          evaluatedOperands.add(expressionsCopy.poll());
        }
      }

      // Manejar la operación según el operador
      switch (operator) {
        case "QUOTE":
          return (T) evaluatedOperands.poll(); // No evaluar la expresión citada
        case "SETQ":
          return (T) handleSetq(env, evaluatedOperands); // YA ESTA ESTA FUNCIONALIDAD
        case "COND":
          return (T) handleCond(env, evaluatedOperands);
        case "DEFUN":
          return (T) handleDefun(env, evaluatedOperands); // YA ESTA ESTA FUNCIONALIDAD
        case "ATOM":
          return (T) handleAtom(env, evaluatedOperands);
        case "LIST":
          return (T) handleList(env, evaluatedOperands);
        case "EQUAL":
          return (T) handleEqual(env, evaluatedOperands);
        case "CAR":
          return (T) handleCar(env, evaluatedOperands);
        case "CDR":
          return (T) handleCdr(env, evaluatedOperands);
        case "PRINT":
          return (T) handlePrint(env, evaluatedOperands); // YA ESTA ESTA FUNCIONALIDAD
        default:
          // Operaciones aritméticas o funciones definidas
          return (T) handleOperation(env, operator, evaluatedOperands);
      }
    }
    throw new RuntimeException("Expresión no válida: el primer elemento no es un símbolo");
  }

  private Object handleSetq(Environment env, Queue<?> operands) {
    if (operands.size() < 2) {
      throw new RuntimeException("SETQ requiere un símbolo y un valor");
    }

    // Obtener el símbolo (nombre de la variable)
    Object symbolObj = operands.poll();
    if (!(symbolObj instanceof LispSymbol)) {
      throw new RuntimeException("SETQ requiere un símbolo como nombre de variable");
    }
    LispSymbol<?> symbol = (LispSymbol<?>) symbolObj;

    // Obtener el valor de la variable (no evaluar el nombre de la variable)
    Object value = operands.poll();
    if (value instanceof Expression) {
      value = ((Expression<?>) value).evaluate(env);
    }
    ;
    // Definir la variable en el entorno
    env.defineVariable(symbol.getName(), value);

    // Verificar que la variable se haya definido correctamente
    Object definedValue = env.getVariable(symbol.getName());
    System.out.println("Variable definida: " + symbol.getName() + " = " + definedValue);

    // Devolver el valor asignado
    return value;
  }

  private Object handleCond(Environment env, Queue<?> operands) {
    // Crear una copia de los operandos para no modificar la original
    Queue<?> operandsCopy = new LinkedList<>(operands);

    while (!operandsCopy.isEmpty()) {
      // Obtener la cláusula completa (condición + resultados)
      Object clauseObj = operandsCopy.poll();
      if (!(clauseObj instanceof LispList)) {
        throw new RuntimeException("COND requiere listas como cláusulas");
      }

      LispList<?> clause = (LispList<?>) clauseObj;
      Queue<Expression<?>> clauseExprs = new LinkedList<>(clause.getExpressions());

      if (clauseExprs.isEmpty()) {
        throw new RuntimeException("Cláusula vacía en COND");
      }

      // Evaluar la condición (primer elemento de la cláusula)
      Expression<?> test = clauseExprs.poll();
      Object testResult;

      // Manejo especial para T
      if (test instanceof LispSymbol && ((LispSymbol<?>) test).getName().equals("T")) {
        testResult = true;
      } else {
        testResult = test.evaluate(env);
      }

      // Si la condición es verdadera
      if (testResult instanceof Boolean && (Boolean) testResult) {
        // Evaluar todas las expresiones restantes y devolver la última
        Object result = null;
        while (!clauseExprs.isEmpty()) {
          result = clauseExprs.poll().evaluate(env);
        }
        return result != null ? result : testResult;
      }
    }

    // Valor por defecto cuando ninguna condición se cumple (podrías cambiarlo)
    return null;
  }

  /*
   * private Object handleCond(Environment env, Queue<?> operands) {
   * System.out.println("ENTRA A LA FUNCION COND");
   * while (!operands.isEmpty()) {
   * System.out.println("SI ENTRA AL WHILE");
   * // Obtener la condición y la expresión correspondiente
   * Object conditionObj = operands.poll();
   * if (!(conditionObj instanceof LispList)) {
   * throw new RuntimeException("COND requiere una lista de condiciones");
   * }
   * System.out.println("PASA EL IF");
   * LispList<?> conditionList = (LispList<?>) conditionObj;
   * 
   * // Evaluar la condición
   * Expression<?> test = conditionList.getExpressions().poll();
   * Object testResult = test.evaluate(env);
   * 
   * // Si la condición es verdadera, evaluar y devolver la expresión
   * correspondiente
   * if (testResult instanceof Boolean && (Boolean) testResult) {
   * Expression<?> result = conditionList.getExpressions().poll();
   * return result.evaluate(env);
   * }
   * }
   * 
   * // Si ninguna condición es verdadera, lanzar una excepción
   * throw new RuntimeException("Ninguna condición se cumplió");
   * }
   */

  private Object handleDefun(Environment env, Queue<Object> operands) {
    if (operands.size() < 3) {
      throw new RuntimeException("DEFUN requiere un nombre, parámetros y un cuerpo");
    }

    // Obtener el nombre de la función
    Expression<?> nameExpr = (Expression<?>) operands.poll();
    if (!(nameExpr instanceof LispSymbol)) {
      throw new RuntimeException("DEFUN requiere un símbolo como nombre de función");
    }
    String name = ((LispSymbol<?>) nameExpr).getName();

    // Obtener la lista de parámetros
    Expression<?> paramsExpr = (Expression<?>) operands.poll();
    if (!(paramsExpr instanceof LispList)) {
      throw new RuntimeException("DEFUN requiere una lista de parámetros");
    }
    LispList<?> paramsList = (LispList<?>) paramsExpr;
    List<String> parameters = new ArrayList<>();
    for (Expression<?> param : paramsList.getExpressions()) {
      if (!(param instanceof LispSymbol)) {
        throw new RuntimeException("Los parámetros deben ser símbolos");
      }
      parameters.add(((LispSymbol<?>) param).getName());
    }

    // Obtener el cuerpo de la función
    Expression<?> body = (Expression<?>) operands.poll();

    // Crear la función y almacenarla en el entorno
    LispFunction<?> function = new LispFunction<>(parameters, body);
    env.defineFunction(name, function);

    System.out.println("Función definida: " + name);
    return name;
  }

  private Boolean handleAtom(Environment env, Queue<Object> operands) {
    if (operands.isEmpty()) {
      throw new RuntimeException("ATOM requiere una expresión");
    }
    Object value = operands.poll();
    return !(value instanceof LispList);
  }

  private LispList<?> handleList(Environment env, Queue<Object> operands) {
    LispList<Object> list = new LispList<>();
    while (!operands.isEmpty()) {
      list.addExpression((Expression<?>) operands.poll());
    }
    return list;
  }

  private Boolean handleEqual(Environment env, Queue<Object> operands) {
    if (operands.size() < 2) {
      throw new RuntimeException("EQUAL requiere dos expresiones");
    }
    Object expr1 = operands.poll();
    Object expr2 = operands.poll();
    return expr1.equals(expr2);
  }

  private Object handleCar(Environment env, Queue<Object> operands) {
    if (operands.isEmpty()) {
      throw new RuntimeException("CAR requiere una lista");
    }
    Object result = operands.poll();
    if (result instanceof LispList) {
      return ((LispList<?>) result).expressions.poll().evaluate(env);
    }
    throw new RuntimeException("CAR solo funciona con listas");
  }

  private LispList<?> handleCdr(Environment env, Queue<Object> operands) {
    if (operands.isEmpty()) {
      throw new RuntimeException("CDR requiere una lista");
    }
    Object result = operands.poll();
    if (result instanceof LispList) {
      LispList<?> list = (LispList<?>) result;
      list.expressions.poll(); // Eliminar el primer elemento
      return list;
    }
    throw new RuntimeException("CDR solo funciona con listas");
  }

  private Object handlePrint(Environment env, Queue<?> operands) {
    if (operands.isEmpty()) {
      throw new RuntimeException("PRINT requiere una expresión");
    }
    Object result = operands.poll();
    System.out.println(result); // Imprimir el resultado
    return result; // Devolver el resultado para que pueda ser usado en otras expresiones
  }

  private Object handleOperation(Environment env, String operator, Queue<Object> operands) {
    if (operands.isEmpty()) {
      throw new RuntimeException("Faltan operandos para la operación: " + operator);
    }

    // Verificar si el operador es una función definida
    try {
      LispFunction<?> function = env.getFunction(operator);
      // Si es una función definida, llamarla con los operandos
      return callFunction(env, function, operands);
    } catch (RuntimeException e) {
      // Si no es una función definida, aplicar la operación aritmética
      return applyArithmeticOperation(operands, operator);
    }
  }

  private Object callFunction(Environment env, LispFunction<?> function, Queue<Object> operands) {
    // Crear un nuevo entorno local para la función
    Environment localEnv = new Environment();

    // Asignar valores a los parámetros
    List<String> parameters = function.getParameters();
    for (String param : parameters) {
      if (operands.isEmpty()) {
        throw new RuntimeException("Faltan argumentos para la función: " + param);
      }
      Object value = operands.poll();
      localEnv.defineVariable(param, value);
    }

    // Evaluar el cuerpo de la función en el entorno local
    return function.getBody().evaluate(localEnv);
  }

  private Object applyArithmeticOperation(Queue<Object> operands, String operator) {
    // Aplicar la operación aritmética
    Object result = operands.poll();
    while (!operands.isEmpty()) {
      Object operand = operands.poll();
      result = applyOperation(result, operand, operator);
    }
    return result;
  }

  private Object applyOperation(Object left, Object right, String operator) {
    if (left instanceof Double && right instanceof Double) {
      double a = (Double) left;
      double b = (Double) right;
      switch (operator) {
        case "+":
          return a + b;
        case "-":
          return a - b;
        case "*":
          return a * b;
        case "/":
          return a / b;
        case "%":
          return a % b;
        case "<":
          return a < b;
        case "<=":
          return a <= b;
        case ">":
          return a > b;
        case ">=":
          return a >= b;
        case "=":
          return a == b;
        default:
          throw new RuntimeException("Operación no soportada: " + operator);
      }
    }
    throw new RuntimeException("Operación no válida para tipos no numéricos");
  }

  /*
   * @Override
   * 
   * @SuppressWarnings("unchecked")
   * public T evaluate(Environment env) {
   * if (expressions.isEmpty()) {
   * throw new RuntimeException("Lista vacía");
   * }
   * 
   * // Obtener el operador (primer elemento de la lista)
   * Expression<?> first = expressions.pop();
   * System.out.println("Primer elemento de la lista: " + first.toString()); //
   * Depuración
   * 
   * if (first instanceof LispSymbol) {
   * String operator = ((LispSymbol<?>) first).getName();
   * System.out.println("Operador: " + operator); // Depuración
   * 
   * // Evaluar todos los operandos restantes
   * Queue<Object> evaluatedOperands = new Queue<>();
   * while (!expressions.isEmpty()) {
   * Expression<?> expr = expressions.pop();
   * evaluatedOperands.push(expr.evaluate(env));
   * }
   * 
   * // Manejar la operación según el operador
   * switch (operator) {
   * case "QUOTE":
   * return (T) evaluatedOperands.pop(); // No evaluar la expresión citada
   * case "SETQ":
   * return (T) handleSetq(env, evaluatedOperands);
   * case "COND":
   * return (T) handleCond(env, evaluatedOperands);
   * case "DEFUN":
   * return (T) handleDefun(env, evaluatedOperands);
   * case "ATOM":
   * return (T) handleAtom(env, evaluatedOperands);
   * case "LIST":
   * return (T) handleList(env, evaluatedOperands);
   * case "EQUAL":
   * return (T) handleEqual(env, evaluatedOperands);
   * case "CAR":
   * return (T) handleCar(env, evaluatedOperands);
   * case "CDR":
   * return (T) handleCdr(env, evaluatedOperands);
   * case "PRINT":
   * return (T) handlePrint(env, evaluatedOperands);
   * default:
   * // Operaciones aritméticas o funciones definidas
   * return (T) handleOperation(env, operator, evaluatedOperands);
   * }
   * }
   * throw new
   * RuntimeException("Expresión no válida: el primer elemento no es un símbolo");
   * }
   * 
   * private Object handleSetq(Environment env) {
   * if (expressions.size() < 2) {
   * throw new RuntimeException("SETQ requiere un símbolo y un valor");
   * }
   * LispSymbol<?> symbol = (LispSymbol<?>) expressions.pop();
   * Expression<?> valueExpr = expressions.pop();
   * Object value = valueExpr.evaluate(env);
   * env.defineVariable(symbol.getName(), value);
   * return value;
   * }
   * 
   * private Object handleCond(Environment env) {
   * while (!expressions.isEmpty()) {
   * LispList<?> condition = (LispList<?>) expressions.pop();
   * if (condition.expressions.size() < 2) {
   * throw new RuntimeException("COND requiere una condición y un resultado");
   * }
   * Expression<?> test = condition.expressions.pop();
   * Expression<?> result = condition.expressions.pop();
   * 
   * if (test.evaluate(env).equals(true)) {
   * return result.evaluate(env);
   * }
   * }
   * throw new RuntimeException("Ninguna condición se cumplió");
   * }
   * 
   * private Object handleDefun(Environment env) {
   * if (expressions.size() < 3) {
   * throw new
   * RuntimeException("DEFUN requiere un nombre, parámetros y un cuerpo");
   * }
   * String name = ((LispSymbol<?>) expressions.pop()).getName();
   * LispList<?> parameters = (LispList<?>) expressions.pop();
   * Expression<?> body = expressions.pop();
   * LispFunction<?> function = new LispFunction<>(parameters, body);
   * env.defineFunction(name, function);
   * return name;
   * }
   * 
   * private Boolean handleAtom(Environment env) {
   * if (expressions.isEmpty()) {
   * throw new RuntimeException("ATOM requiere una expresión");
   * }
   * Expression<?> expr = expressions.pop();
   * Object value = expr.evaluate(env);
   * return !(value instanceof LispList);
   * }
   * 
   * private LispList<?> handleList(Environment env) {
   * LispList<Object> list = new LispList<>();
   * while (!expressions.isEmpty()) {
   * Expression<?> expr = expressions.pop();
   * list.addExpression(expr);
   * }
   * return list;
   * }
   * 
   * private Boolean handleEqual(Environment env) {
   * if (expressions.size() < 2) {
   * throw new RuntimeException("EQUAL requiere dos expresiones");
   * }
   * Expression<?> expr1 = expressions.pop();
   * Expression<?> expr2 = expressions.pop();
   * return expr1.evaluate(env).equals(expr2.evaluate(env));
   * }
   * 
   * private Object handleCar(Environment env) {
   * if (expressions.isEmpty()) {
   * throw new RuntimeException("CAR requiere una lista");
   * }
   * Expression<?> expr = expressions.pop();
   * Object result = expr.evaluate(env);
   * if (result instanceof LispList) {
   * return ((LispList<?>) result).expressions.pop().evaluate(env);
   * }
   * throw new RuntimeException("CAR solo funciona con listas");
   * }
   * 
   * private LispList<?> handleCdr(Environment env) {
   * if (expressions.isEmpty()) {
   * throw new RuntimeException("CDR requiere una lista");
   * }
   * Expression<?> expr = expressions.pop();
   * Object result = expr.evaluate(env);
   * if (result instanceof LispList) {
   * LispList<?> list = (LispList<?>) result;
   * list.expressions.pop(); // Eliminar el primer elemento
   * return list;
   * }
   * throw new RuntimeException("CDR solo funciona con listas");
   * }
   * 
   * private Object handlePrint(Environment env) {
   * if (expressions.isEmpty()) {
   * throw new RuntimeException("PRINT requiere una expresión");
   * }
   * Expression<?> expr = expressions.pop();
   * Object result = expr.evaluate(env);
   * System.out.println(result); // Imprimir el resultado
   * return result; // Devolver el resultado para que pueda ser usado en otras
   * expresiones
   * }
   * 
   * private Object handleOperation(Environment env, String operator) {
   * if (expressions.isEmpty()) {
   * throw new RuntimeException("Faltan operandos para la operación: " +
   * operator);
   * }
   * 
   * // Evaluar todos los operandos
   * Queue<Object> evaluatedOperands = new Queue<>();
   * while (!expressions.isEmpty()) {
   * Expression<?> expr = expressions.pop();
   * evaluatedOperands.push(expr.evaluate(env));
   * }
   * 
   * // Aplicar la operación
   * Object result = evaluatedOperands.pop();
   * while (!evaluatedOperands.isEmpty()) {
   * Object operand = evaluatedOperands.pop();
   * result = applyOperation(result, operand, operator);
   * }
   * 
   * return result;
   * }
   * 
   * private Object applyOperation(Object left, Object right, String operator) {
   * if (left instanceof Double && right instanceof Double) {
   * double a = (Double) left;
   * double b = (Double) right;
   * switch (operator) {
   * case "+":
   * return a + b;
   * case "-":
   * return a - b;
   * case "*":
   * return a * b;
   * case "/":
   * return a / b;
   * default:
   * throw new RuntimeException("Operación no soportada: " + operator);
   * }
   * }
   * throw new RuntimeException("Operación no válida para tipos no numéricos");
   * }
   */
}