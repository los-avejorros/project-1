package com.avejorros;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.avejorros.bean.Environment;
import com.avejorros.controller.ParserLisp;
import com.avejorros.interfaces.Expression;

public class AdvancedFeaturesTest {
  private Environment env;

  @Before
  public void setUp() {
    env = new Environment();
    // Definir función factorial para pruebas
    String factorialCode = "(DEFUN factorial (n) (COND ((= n 0) 1) (T (* n (factorial (- n 1)))))";
    ParserLisp.parse(factorialCode).evaluate(env);
  }

  @Test
  public void testConditional() {
    // (COND ((< 2 3) 10) (T 20))
    String condCode = "(COND ((< 2 3) 10) (T 20))";
    Expression<?> expr = ParserLisp.parse(condCode);
    assertEquals(10.0, expr.evaluate(env));
  }

  @Test
  public void testFactorial() {
    // (factorial 5)
    String factCode = "(factorial 5)";
    Expression<?> expr = ParserLisp.parse(factCode);
    assertEquals(120.0, expr.evaluate(env));
  }

  @Test
  public void testRecursiveFunction() {
    // (DEFUN fib (n) (COND ((= n 0) 1) ((= n 1) 1) (T (+ (fib (- n 1)) (fib (- n
    // 2))))))
    String fibCode = "(DEFUN fib (n) (COND ((= n 0) 1) ((= n 1) 1) (T (+ (fib (- n 1)) (fib (- n 2))))))";
    ParserLisp.parse(fibCode).evaluate(env);

    Expression<?> fib5 = ParserLisp.parse("(fib 5)");
    assertEquals(8.0, fib5.evaluate(env));
  }

}
