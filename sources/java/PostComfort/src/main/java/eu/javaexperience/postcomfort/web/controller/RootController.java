package eu.javaexperience.postcomfort.web.controller;

import java.lang.reflect.Method;

import eu.javaexperience.collection.set.OneShotList;
import eu.javaexperience.io.file.FileTools;
import eu.javaexperience.postcomfort.web.PostComfortBackendTools;
import eu.javaexperience.postcomfort.web.PostComfortFacilities;
import eu.javaexperience.rpc.SimpleRpcRequest;
import eu.javaexperience.rpc.SimpleRpcSession;
import eu.javaexperience.rpc.web.RpcUrlNode;
import eu.javaexperience.teavm.url.TeaVmUrlNode;
import eu.javaexperience.web.Context;
import eu.javaexperience.web.SessionManager;
import eu.javaexperience.web.dispatch.url.AttachDirectoryURLNode;
import eu.javaexperience.web.dispatch.url.JavaClassURLNode;
import eu.javaexperience.web.dispatch.url.URLNode;
import eu.javaexperience.web.session.InMemorySessionManager;
import eu.javaexperience.web.session.SessionTools;

public class RootController extends JavaClassURLNode
{
	private RootController()
	{
		super("");
	}
	
	public static final RootController ROOT = new RootController();
	
	public static final SessionManager SESSION_MANAGER = new InMemorySessionManager();
	
	public static final URLNode RESOURCES = new AttachDirectoryURLNode
	(
		"resources",
		PostComfortBackendTools.resourceFile
		(
			PostComfortBackendTools.isSystemInDevelMode()?"src/main/resources/web_resources":"web_resources"
		),
		PostComfortBackendTools.isSystemInDevelMode()
	);
	
	public static final RpcUrlNode<SimpleRpcRequest, SimpleRpcSession> API = RpcUrlNode.createSimple("api", "", PostComfortFacilities.getRpcNodes());
	
	public static final TeaVmUrlNode SCRIPT = new TeaVmUrlNode
	(
		!PostComfortBackendTools.isSystemInDevelMode(),
		"/tmp/postcomfort",
		PostComfortBackendTools.resourceFile((PostComfortBackendTools.isSystemInDevelMode()?"src/main/resources/":"")+"web_resources/site/postcomfort_webapp.js"), PostComfortBackendTools.getWebappClass()
	);
	
	static
	{
		ROOT.addChild(RESOURCES);
		ROOT.addChild(SCRIPT.getScriptNode());
		ROOT.addChild(API);
		
		SCRIPT.getScriptNode().setCheckModifiedOnRequest(PostComfortBackendTools.isSystemInDevelMode());
	}
	
	
	@Override
	protected boolean beforeCall(Context ctx, Method m)
	{
		return true;
	}

	@Override
	protected void afterCall(Context ctx, Method m){}

	@Override
	protected void backward(Context ctx)
	{
		endpoint(ctx);
	}

	@Override
	protected boolean endpoint(Context ctx)
	{
		PostComfortBackendTools.renderBootPage(ctx, null, null, new OneShotList<String>(FileTools.normalizeSlashes(SCRIPT.getScriptNode().getSaltedUrl())), null); 
		return true;
	}

	@Override
	protected boolean access(Context ctx)
	{
		SessionTools.sessionStart(SESSION_MANAGER, "postcomfort", ctx);
		return true;
	}
}
