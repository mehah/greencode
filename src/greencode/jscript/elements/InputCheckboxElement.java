package greencode.jscript.elements;

import greencode.jscript.Element;
import greencode.jscript.ElementHandle;
import greencode.jscript.Window;

public class InputCheckboxElement extends InputElementCheckable {
	protected InputCheckboxElement(Window window) { super("checkbox", window); }
	
	public static InputCheckboxElement cast(Element e) { return ElementHandle.cast(e, InputCheckboxElement.class); }
}