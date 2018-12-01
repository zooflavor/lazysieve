package gui.math;

import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class TestScriptEngineFactory implements ScriptEngineFactory {
	public static final String NAME="testscript";
	
	private ScriptEngineFactory factory;
	
	@Override
	public String getEngineName() {
		return NAME;
	}
	
	@Override
	public String getEngineVersion() {
		return "1.0";
	}
	
	@Override
	public List<String> getExtensions() {
		return Arrays.asList();
	}
	
	@Override
	public List<String> getMimeTypes() {
		return Arrays.asList();
	}
	
	@Override
	public String getLanguageName() {
		return NAME;
	}
	
	@Override
	public String getLanguageVersion() {
		return "1.0";
	}
	
	@Override
	public String getMethodCallSyntax(String obj, String m, String... args) {
		initFactory();
		return factory.getMethodCallSyntax(obj, m, args);
	}
	
	@Override
	public List<String> getNames() {
		return Arrays.asList(NAME);
	}
	
	private void initFactory() {
		if (null!=factory) {
			return;
		}
		for (ScriptEngineFactory factory2:
				new ScriptEngineManager(getClass().getClassLoader())
						.getEngineFactories()) {
			if (factory2.getNames().contains("JavaScript")) {
				factory=factory2;
				break;
			}
		}
		if (null==factory) {
			throw new RuntimeException("no JavaScript factory");
		}
	}
	
	@Override
	public String getOutputStatement(String toDisplay) {
		initFactory();
		return factory.getOutputStatement(toDisplay);
	}
	
	@Override
	public Object getParameter(String key) {
		initFactory();
		return factory.getParameter(key);
	}
	
	@Override
	public String getProgram(String... statements) {
		initFactory();
		return factory.getProgram(statements);
	}
	
	@Override
	public ScriptEngine getScriptEngine() {
		initFactory();
		ScriptEngine engine=factory.getScriptEngine();
		return new ScriptEngine() {
			@Override
			public Bindings createBindings() {
				return engine.createBindings();
			}
			
			@Override
			public Object eval(String script, ScriptContext context)
					throws ScriptException {
				return engine.eval(script, context);
			}
			
			@Override
			public Object eval(Reader reader, ScriptContext context)
					throws ScriptException {
				return engine.eval(reader, context);
			}
			
			@Override
			public Object eval(String script) throws ScriptException {
				return engine.eval(script);
			}
			
			@Override
			public Object eval(Reader reader) throws ScriptException {
				return engine.eval(reader);
			}
			
			@Override
			public Object eval(String script, Bindings n)
					throws ScriptException {
				return engine.eval(script, n);
			}
			
			@Override
			public Object eval(Reader reader, Bindings n)
					throws ScriptException {
				return engine.eval(reader, n);
			}
			
			@Override
			public Object get(String key) {
				return engine.get(key);
			}
			
			@Override
			public Bindings getBindings(int scope) {
				return engine.getBindings(scope);
			}
			
			@Override
			public ScriptContext getContext() {
				return engine.getContext();
			}
			
			@Override
			public ScriptEngineFactory getFactory() {
				return TestScriptEngineFactory.this;
			}
			
			@Override
			public void put(String key, Object value) {
				engine.put(key, value);
			}
			
			@Override
			public void setBindings(Bindings bindings, int scope) {
				engine.setBindings(bindings, scope);
			}
			
			@Override
			public void setContext(ScriptContext context) {
				engine.setContext(context);
			}
		};
	}
}
