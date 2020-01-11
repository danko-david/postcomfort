package eu.javaexperience.postcomfort.web;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import eu.javaexperience.cli.CliEntry;
import eu.javaexperience.cli.CliTools;
import eu.javaexperience.file.AbstractFile;
import eu.javaexperience.file.AbstractFileSystem;
import eu.javaexperience.file.FileSystemTools;
import eu.javaexperience.file.fs.combined.CombinedFileSystem;
import eu.javaexperience.file.fs.os.dir.OsDirectoryFilesystem;
import eu.javaexperience.file.fs.zip.ZipFileSystem;
import eu.javaexperience.postcomfort.webapp.PostComfortWebapp;
import eu.javaexperience.prog.Debug;
import eu.javaexperience.reflect.Mirror;

public class PostComfortBackendTools
{
	public static final CliEntry<Integer> WEB_PORT = CliEntry.createFirstArgParserEntry
	(
		(e)->Integer.parseInt(e),
		"Web port. Default: 2100",
		"w", "-web-port"
	);

	public static final CliEntry<String> WEB_APP = CliEntry.createFirstArgParserEntry
	(
		e->e,
		"Web application class. (Default: eu.javaexperience.postcomfort.webapp.PostComfortWebapp)",
		"a", "-web-app-class"
	);
	
	
	public static Class<?> getWebappClass()
	{
		try
		{
			return Class.forName
			(
				WEB_APP.tryParseOrDefault(CliTools.getStoredCliOptions(), PostComfortWebapp.class.getCanonicalName()),
				false,
				Thread.currentThread().getContextClassLoader());
		}
		catch (ClassNotFoundException e)
		{
			Mirror.propagateAnyway(e);
			return null;
		}
	}

	protected static boolean IS_DEBUG = java.lang.management.ManagementFactory.getRuntimeMXBean().
		getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;
		
	public static void forceSetDevelopementMode(boolean mode)
	{
		IS_DEBUG = mode;
	}
	
	public static boolean isSystemInDevelMode()
	{
		return IS_DEBUG;
	}

	protected static List<AbstractFileSystem> collectFs()
	{
		ArrayList<AbstractFileSystem> ret = new ArrayList<>();
		
		ClassLoader cl = ClassLoader.getSystemClassLoader();

		URL[] urls = ((URLClassLoader)cl).getURLs();

		for(URL url: urls)
		{
			File f = new File(url.getFile());
			if(f.exists())
			{
				try
				{
					if(f.toString().endsWith(".jar"))
					{
						ret.add(new ZipFileSystem(f.toString()));
					}
					else
					{
						ret.add(new OsDirectoryFilesystem(f));
					}
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		ret.add(FileSystemTools.SYSTEM_CLASSLOADER_FILESYSTEM);
		ret.add(FileSystemTools.DEFAULT_FILESYSTEM);
		return ret;
	}
	
	public static final AbstractFileSystem FILESYSTEM = new CombinedFileSystem(collectFs().toArray(FileSystemTools.emptyAbstractFileSystemArray));

	
	//TODO or use zip filesystem
	public static AbstractFile resourceFile(String file)
	{
		return (IS_DEBUG?FileSystemTools.DEFAULT_FILESYSTEM:FILESYSTEM).fromUri(file);
	}
}
