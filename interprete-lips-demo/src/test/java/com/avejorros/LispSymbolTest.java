package com.avejorros;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.avejorros.bean.Environment;
import com.avejorros.bean.LispSymbol;

public class LispSymbolTest {
  @Test
    public void testSymbolEvaluation() {
        Environment env = new Environment();
        env.defineVariable("x", 10.0);
        
        LispSymbol<Double> symbol = new LispSymbol<>("x");
        assertEquals(10.0, symbol.evaluate(env), 0.001);
    }
    
    @Test(expected = RuntimeException.class)
    public void testUndefinedSymbol() {
        Environment env = new Environment();
        LispSymbol<Double> symbol = new LispSymbol<>("y");
        symbol.evaluate(env);
    }
}
