package com.jrender.jscript.dom.elements;

import com.jrender.jscript.dom.Element;
import com.jrender.jscript.dom.ElementHandle;
import com.jrender.jscript.dom.Window;

public class SmallElement extends Element {		
	protected SmallElement(Window window) { super(window, "small"); }
	
	public static SmallElement cast(Element e) { return ElementHandle.cast(e, SmallElement.class); }
}
