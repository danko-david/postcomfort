package eu.javaexperience.postcomfort.webapp;

import org.teavm.jso.dom.html.HTMLElement;

import eu.jvx.js.lib.bindings.VanillaTools;
import eu.jvx.js.lib.style.StyleAlaCarteMenu;
import eu.jvx.js.lib.style.StyleDecorator;
import eu.jvx.js.lib.style.StyleDecoratorSource;
import eu.jvx.js.lib.ui.style.CssStyleSheet;
import eu.jvx.js.lib.ui.style.StyleContainer;

/**
 * On board simplified version of skeleton.css
 * 
 *
 * */
public enum SkeletonStyleAlaCarte implements StyleDecoratorSource
{
	CONTAINER
	(
		".container",
			"position: relative;\n" + 
			"width: 100%;\n" + 
			"max-width: 960px;\n" + 
			"margin: 0 auto;\n" + 
			"padding: 0 20px;\n" + 
			"box-sizing: border-box;",
		"container"
	),
	
	COLUMNS
	(
		".columns",
			"width: 100%;\n" + 
			"float: left;\n" + 
			"box-sizing: border-box;",
		"columns"
	),
	
	COL_1
	(
		".col-1.columns",
			"width: 4.66666666667%;",
		"col-1", "columns"
	),
	
	COL_2
	(
		".col-2.columns",
			"width: 13.3333333333%;",
		"col-2", "columns"
	),
	
	COL_3
	(
		".col-3.columns",
			"width: 22%;",
		"col-3", "columns"
	),
	
	COL_4
	(
		".col-4.columns",
			"width: 30.6666666667%;",
		"col-4", "columns"
	),
	
	COL_5
	(
		".col-5.columns",
			"width: 39.3333333333%;",
		"col-5", "columns"
	),
	
	COL_6
	(
		".col-6.columns",
			"width: 48%;",
		"col-6", "columns"
	),
	
	COL_7
	(
		".col-7.columns",
			"width: 56.6666666667%;",
		"col-7", "columns"
	),
	
	COL_8
	(
		".col-8.columns",
			"width: 65.3333333333%;",
		"col-8", "columns"
	),
	
	COL_9
	(
		".col-9.columns",
			"width: 74%;",
		"col-9", "columns"
	),
	
	COL_10
	(
		".col-10.columns",
			"width: 82.6666666667%;",
		"col-10", "columns"
	),
	
	COL_11
	(
		".col-11.columns",
			"width: 91.3333333333%;",
		"col-11", "columns"
	),
	
	COL_12
	(
		".col-12.columns",
			"width: 100%;",
		"col-12", "columns"
	),
	
	
	;
	
	protected final String[] classes;
	protected final String selector;
	protected final String rules;
	protected final StyleAlaCarteMenu decorator;
	
	private SkeletonStyleAlaCarte(String selector, String rules, String... classes)
	{
		this.selector = selector;
		this.classes = classes;
		this.rules = rules;
		this.decorator = new StyleAlaCarteMenu(classes);
	}
	

	protected static StyleContainer style = new StyleContainer();
	
	static
	{
		VanillaTools.addToHeader((HTMLElement) style.getHtml());
		style.refresh();
		
		CssStyleSheet s = style.getStyleSheet();
		
		for(SkeletonStyleAlaCarte ssac:SkeletonStyleAlaCarte.values())
		{
			s.insertRule(ssac.selector+"{"+ssac.rules+"}", s.getLength());
		}
	}

	@Override
	public StyleDecorator getDecorator()
	{
		return decorator;
	}

}
