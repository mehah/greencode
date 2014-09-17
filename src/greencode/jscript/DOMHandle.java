package greencode.jscript;

import java.util.HashMap;

import javax.servlet.http.Part;

import greencode.exception.ConnectionLost;
import greencode.http.ViewSession;
import greencode.kernel.Console;
import greencode.kernel.ElementsScan;
import greencode.kernel.GreenContext;
import greencode.kernel.LogMessage;
import greencode.util.ClassUtils;
import greencode.util.GenericReflection;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public final class DOMHandle {
	private DOMHandle() {}
	
	public static Integer getUID(DOM d) { return d.uid; }
	
	public static void replaceReference(DOM dom, DOM newDom) {
		dom.uid = newDom.uid;
		dom.variables = newDom.variables;
	}
	
	public static void registerElementByCommand(DOM owner, Node e, String name, Object... parameters)
	{ registerReturnByCommand(owner, e.uid, name, parameters); }
	
	public static void registerElementByVector(DOM owner, Node e, int index)
	{ ElementsScan.registerCommand(owner, e.uid+"*vector."+index); }
	
	public static void registerElementByProperty(DOM owner, Node e, String name)
	{ registerReturnByProperty(owner, e.uid, name); }
	
	public static void registerReturnByProperty(DOM owner, int uid, String name)
	{ ElementsScan.registerCommand(owner, uid+"*prop."+name); }
	
	public static void registerReturnByCommand(DOM owner, int uid, String name, Object... parameters)
	{ ElementsScan.registerCommand(owner, uid+"*ref."+name, parameters); }
	
	public static void registerReturnByCommand(DOM owner, int[] uids, String name, Object... parameters) {
		StringBuilder _uids = new StringBuilder("[");
		for (int i = -1; ++i < uids.length;) {
			if(i > 0) _uids.append(',');
			_uids.append(uids[i]);
		}
		_uids.append(']');
		ElementsScan.registerCommand(owner, _uids+"*ref."+name, parameters);
	}

	
	public static void execCommand(DOM dom, String methodName, Object... args) { ElementsScan.registerCommand(dom, methodName, args); }

	public static void setProperty(DOM dom, String name, Object value) {
		dom.variables.put(name, value);
		ElementsScan.registerCommand(dom, "#"+name, value);
	}
	
	public static String getDefaultIdToRegisterReturn(int uid) { return uid+"*ref"; }
	
	public static void removeRegisteredReturn(Window window, int uid) { DOMHandle.execCommand(window, "removeRegisteredReturn", uid); }
	
	@SuppressWarnings("unchecked")
	public static<C> C getVariableValue(DOM owner, String varName, Class<C> cast) { return (C) owner.variables.get(varName); }
	
	public static void setVariableValue(DOM owner, String varName, Object value) { owner.variables.put(varName, value); }
	
	public static void removeVariable(DOM owner, String varName) { owner.variables.remove(varName); }
	
	public static boolean containVariableKey(DOM owner, String key) { return owner.variables.containsKey(key); }
	
	private static Object getSyncValue(DOM owner, String varName, boolean isMethod, String methodOrPropName, Object... parameters) {
		synchronized (owner) {
			GreenContext context = GreenContext.getInstance();
			
			if(!isMethod)
				methodOrPropName = '#'+methodOrPropName;		
			
			JSCommand jsCommand = new JSCommand(owner, methodOrPropName, parameters);

			JsonObject json = new JsonObject();
			json.addProperty("uid", owner.uid);
			json.addProperty("varName", varName);
			json.add("command", context.gsonInstance.toJsonTree(jsCommand));
			JsonObject j = new JsonObject();						
			j.add("sync", json);
			
			try {				
				owner.flush(false);
				
				greencode.kernel.$ElementsScan.send(context, j);
				
				context.getResponse().flushBuffer();
				
				getDOMSync(owner.viewSession).put(owner.uid, owner);
				
				Console.log("Synchronizing: [varName="+varName+", command={uid="+owner.uid+", name="+methodOrPropName+", parameters="+context.gsonInstance.toJson(parameters)+"]");
				
				owner.wait(120000);
			} catch (Exception e) {
				throw new ConnectionLost(LogMessage.getMessage("green-0011"));
			}		
		}
		
		return owner.variables.get(varName);
	}
	
	@SuppressWarnings("unchecked")
	private static<C> C getVariableValue(DOM owner, String varName, Class<C> cast, boolean isMethod, String _name, Object... parameters) {
		GreenContext context = GreenContext.getInstance();
		
		if((greencode.kernel.$GreenContext.forceSynchronization(context) || !owner.variables.containsKey(varName))) {
			Object v = getSyncValue(owner, varName, isMethod, _name, parameters);
			
			if(cast != null && !cast.equals(String.class) && !cast.equals(Part.class)) {
				if(ClassUtils.isPrimitiveOrWrapper(cast)) {
					try {
						v = GenericReflection.getDeclaredMethod(cast, "valueOf", String.class).invoke(null, v);
					} catch (Exception e) {
						Console.error(e);
					}
				}else
					v = context.gsonInstance.fromJson((String) v, cast);
			}
			
			// Substitua o antigo valor String para o novo valor com o formato certo.
			owner.variables.put(varName, v);
			return (C) v;
		}
		
		return (C) owner.variables.get(varName);
	}
	
	static HashMap<Integer, DOM> getDOMSync(ViewSession viewSession) {
		@SuppressWarnings("unchecked")
		HashMap<Integer, DOM> DOMList = (HashMap<Integer, DOM>) viewSession.getAttribute("DOM_SYNC");
		if(DOMList == null)
			viewSession.setAttribute("DOM_SYNC", DOMList = new HashMap<Integer, DOM>());
		
		return DOMList;
	}
	
	public static<C> C getVariableValueByCommand(DOM owner, String varName, Class<C> cast, String commandName, Object... parameters) {
		return getVariableValue(owner, varName, cast, true, commandName, parameters);
	}
	
	public static<C> C getVariableValueByProperty(DOM owner, String varName, Class<C> cast, String propName) {
		return getVariableValue(owner, varName, cast, false, propName);
	}
	
	public static<C> C getVariableValueByCommandNoCache(DOM owner, String varName, Class<C> cast, String commandName, Object... parameters) {
		C v = getVariableValue(owner, varName, cast, true, commandName, parameters);
		DOMHandle.removeVariable(owner, varName);
		return v;
	}
	
	public static<C> C getVariableValueByPropertyNoCache(DOM owner, String varName, Class<C> cast, String propName) {
		C v = getVariableValue(owner, varName, cast, false, propName);
		DOMHandle.removeVariable(owner, varName);
		return v;
	}
	
	public static JsonObject getJSONObject(DOM owner, String varName, String... propertyNames) {
		return getVariableValue(owner, varName, JsonObject.class, false, "", (Object[])propertyNames);
	}
	
	public static JsonArray getJSONArray(DOM owner, String varName, String... propertyNames) {
		return getVariableValue(owner, varName, JsonArray.class, false, "[]", (Object[])propertyNames);
	}
	
	public static JsonObject getJSONObjectByProperty(DOM owner, String varName, String propertyName, String... propertyNames) {
		return getVariableValue(owner, varName, JsonObject.class, false, '#'+propertyName, (Object[])propertyNames);
	}
	
	public static JsonArray getJSONArrayByProperty(DOM owner, String varName, String propertyName, String... propertyNames) {
		return getVariableValue(owner, varName, JsonArray.class, false, "#[]"+propertyName, (Object[])propertyNames);
	}
}