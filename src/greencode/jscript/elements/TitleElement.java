package greencode.jscript.elements;

import greencode.jscript.Element;
import greencode.jscript.ElementHandle;
import greencode.jscript.Window;

public class TitleElement extends Element {		
	protected TitleElement(Window window) { super(window, "title"); }
	
	public static TitleElement cast(Element e) { return ElementHandle.cast(e, TitleElement.class); }
}