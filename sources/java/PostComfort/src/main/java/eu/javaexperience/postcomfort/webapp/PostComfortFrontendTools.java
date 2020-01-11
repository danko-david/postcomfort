package eu.javaexperience.postcomfort.webapp;

import static eu.jvx.js.lib.bindings.H.H;

import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.html.HTMLElement;

import eu.jvx.js.lib.bindings.H;
import eu.jvx.js.lib.bindings.VanillaTools;
import eu.jvx.js.lib.style.StyleTools.StyleAlaCarte;
import eu.jvx.js.lib.ui.style.CssStyleSheet;
import eu.jvx.js.lib.ui.style.StyleContainer;

public class PostComfortFrontendTools
{
	private PostComfortFrontendTools() {};
	
	protected static H MAIN_FRAME;
	
	protected static final StyleContainer STYLES = new StyleContainer();
	
	static
	{
		VanillaTools.addToHeader((HTMLElement) STYLES.getHtml());
		STYLES.refresh();
	}
	
	public static void initCommonStyle()
	{
		addStyle("body{background-color: #d3d3d3; color:white}");
		addStyle(".container{background-color: #4c4c4c; color:white}");
		addStyle(".content{padding: 20px}");
		addStyle("input{color: black !important}");
		addStyle(".modal{color:black}");
		
		addStyle(".main-site-title {width:100%;text-align:center; background-color:white; color: black; font-family: Helvetica Neue,Helvetica,Arial,sans-serif; }");
		
		addStyle(".link-header a {display: inline-block; padding: 10px; margin: 3px; border: solid 1px #555555; color: white;font-weight: bold;text-decoration: none;}");
		addStyle(".link-header a:hover {text-decoration: undeline;}");
	}
	
	public static void addStyle(String rule)
	{
		CssStyleSheet s = STYLES.getStyleSheet();
		s.insertRule(rule, s.getLength());
	}
	
	public static H getDesignedRoot()
	{
		if(null == MAIN_FRAME)
		{
			initCommonStyle();
			VanillaTools.addToHeader(H("meta", "content", "width=device-width, initial-scale=1.0", "name", "viewport").getHtml());
		
			H(Window.current().getDocument().getBody()).
			addChilds
			(
				H("div").attrs("class", "main-site-title").style(StyleAlaCarte.TEXT_CENTER).addChilds
				(
					H("div").attrs("class", "container", "style", "background-color: white;color: black;").addChilds
					(
						H("h1").attrs("style", "margin: 0;padding: 20px;font-weight: bold;", "#text", "PostComfort")
					)
				),
				H("div").attrs("class", "container").addChilds
				(
					H("div").attrs("class", "row").addChilds
					(
						MAIN_FRAME = H("div").attrs("id", "site_mainframe", "class", "content")
					)
				)
			);
		}
		
		return MAIN_FRAME;
	}
}
