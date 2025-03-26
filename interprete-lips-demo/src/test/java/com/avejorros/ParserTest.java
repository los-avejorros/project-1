package com.avejorros;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.avejorros.bean.Environment;
import com.avejorros.bean.LispList;
import com.avejorros.bean.LispSymbol;
import com.avejorros.controller.ParserLisp;
import com.avejorros.interfaces.Expression;

public class ParserTest {
    
    @Test
    public void testParseSymbol() {
        Expression<?> expr = ParserLisp.parse("x");
        assertTrue(expr instanceof LispSymbol);
        assertEquals("x", ((LispSymbol<?>)expr).getName());
    }
    
    @Test
    public void testParseSimpleList() {
        Expression<?> expr = ParserLisp.parse("(+ 1 2)");
        assertTrue(expr instanceof LispList);
        
        Environment env = new Environment();
        assertEquals(3.0, expr.evaluate(env));
    }
    
    @Test(expected = RuntimeException.class)
    public void testInvalidExpression() {
        ParserLisp.parse("(+ 1 2");
    }
}
