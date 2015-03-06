package qcon.spel;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;


public class SpelCompilationTest {

    public static void main(String... args) {

        int batchSize = 1_000_000;

        out("Creating %s Bar's", batchSize);

        Bar[] array = new Bar[batchSize];
        for (int i=0; i<array.length; i++) {
            array[i] = new Bar();
        }

        out("Done.\n");

        String expressionString = "uuid.leastSignificantBits > uuid.mostSignificantBits";

        evaluate(array, expressionString, SpelCompilerMode.OFF);
        evaluate(array, expressionString, SpelCompilerMode.OFF);
        evaluate(array, expressionString, SpelCompilerMode.OFF);
        evaluate(array, expressionString, SpelCompilerMode.OFF);
        evaluate(array, expressionString, SpelCompilerMode.OFF);
        evaluate(array, expressionString, SpelCompilerMode.OFF);

        evaluate(array, expressionString, SpelCompilerMode.IMMEDIATE);
        evaluate(array, expressionString, SpelCompilerMode.IMMEDIATE);
        evaluate(array, expressionString, SpelCompilerMode.IMMEDIATE);
        evaluate(array, expressionString, SpelCompilerMode.IMMEDIATE);
        evaluate(array, expressionString, SpelCompilerMode.IMMEDIATE);
        evaluate(array, expressionString, SpelCompilerMode.IMMEDIATE);

        // evaluate(array, expressionString, SpelCompilerMode.MIXED);
    }

    private static void evaluate(Bar[] array, String expressionString, SpelCompilerMode compilerMode) {

        SpelParserConfiguration spelParserConfiguration = new SpelParserConfiguration(compilerMode, SpelCompilationTest.class.getClassLoader());

        ExpressionParser parser = new SpelExpressionParser(spelParserConfiguration);
        StandardEvaluationContext context = new StandardEvaluationContext();
        Expression exp = parser.parseExpression(expressionString);

        StopWatch watch = new StopWatch("SpEL Expression runner " + compilerMode.name());
        watch.start("evaluated " + array.length + " expressions");

        // out("Evaluating expression...");

        List<Object> results = new ArrayList<>();

        for (int i=0; i<array.length; i++) {
            Object value = exp.getValue(context, array[i]);
            results.add(value);
        }

        watch.stop();

        out(watch.shortSummary() + "    " + ((array.length * 1000) / watch.getTotalTimeMillis()) + "/s");
    }



    private static void out(String format, Object... args) {
        if (args == null || args.length == 0) {
            System.out.println(format);
        }
        else {
            System.out.printf(format + " %n", args);
        }
    }

}
