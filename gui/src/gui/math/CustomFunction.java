package gui.math;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public abstract class CustomFunction implements RealFunction {
    private static class Compiled extends CustomFunction {
        private final CompiledScript script;
        
        private Compiled(ScriptEngine engine, String name,
                CompiledScript script) {
            super(engine, name);
            this.script=script;
        }
        
        @Override
        protected Object valueAt(Bindings bindings) throws ScriptException {
            return script.eval(bindings);
        }
    }
    
    private static class Eval extends CustomFunction {
        private final ScriptEngine engine;
        private final String script;

        public Eval(ScriptEngine engine, String name, String script) {
            super(engine, name);
            this.engine=engine;
            this.script=script;
        }
        
        @Override
        protected Object valueAt(Bindings bindings) throws ScriptException {
            return engine.eval(script, bindings);
        }
    }
    
    private final Bindings bindings;
	private final Object lock=new Object();
    private final String name;
    
    private CustomFunction(ScriptEngine engine, String name) {
        bindings=engine.createBindings();
        this.name=name;
    }
    
    public static RealFunction create(String engineName, String name,
            String script) throws ScriptException {
        ScriptEngine engine=new ScriptEngineManager()
                .getEngineByName(engineName);
        if (engine instanceof Compilable) {
            return new Compiled(engine, name,
                    ((Compilable)engine).compile(script));
        }
        else {
            return new Eval(engine, name, script);
        }
    }
    
    @Override
    public boolean isDefined(double fromX, double toX) {
        try {
            return Double.isFinite(valueAt(fromX))
                    && Double.isFinite(valueAt(toX))
                    && Double.isFinite(valueAt(0.5*(fromX+toX)))
                    && Double.isFinite(valueAt(0.25*fromX+0.75*toX))
                    && Double.isFinite(valueAt(0.75*fromX+0.25*toX));
        }
        catch (ArithmeticException ex) {
            return false;
        }
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    @Override
    public double valueAt(double xx) {
		Object object;
		synchronized (lock) {
			bindings.clear();
			bindings.put("x", xx);
			try {
				object=valueAt(bindings);
			}
			catch (ArithmeticException ex) {
				object=Double.NaN;
			}
			catch (ScriptException ex) {
				throw new RuntimeException(ex);
			}
		}
        double result;
        if (object instanceof Number) {
            result=((Number)object).doubleValue();
        }
        else {
			throw new RuntimeException(String.format(
					"a %1$s függvény értéke %2$s-ben nem szám, hanem %3$s",
					name,
					xx,
					object));
        }
        return Double.isFinite(result)?result:Double.NaN;
    }
    
    protected abstract Object valueAt(Bindings bindings)
            throws ScriptException;
}
