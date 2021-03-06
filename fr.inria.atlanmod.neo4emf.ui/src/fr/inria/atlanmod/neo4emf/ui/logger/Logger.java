package fr.inria.atlanmod.neo4emf.ui.logger;

/**
 * Copyright (c) 2013 Atlanmod INRIA LINA Mines Nantes
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Atlanmod INRIA LINA Mines Nantes - initial API and implementation
 * Descritpion ! To come
 * @author Amine BENELALLAM
 * */

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import fr.inria.atlanmod.neo4emf.ui.Neo4emfUiPlugin;

public class Logger {

	public static final int SEVERITY_CANCEL = IStatus.CANCEL;
	public static final int SEVERITY_ERROR = IStatus.ERROR;
	public static final int SEVERITY_INFO= IStatus.INFO;
	public static final int SEVERITY_OK = IStatus.OK;
	public static final int SEVERITY_WARNING = IStatus.WARNING;
	
    private static ILog log;
    
    static {
        log = Neo4emfUiPlugin.getDefault().getLog();    
    }
    
    public static void log(int severity, Throwable e) {
        log.log(new Status(severity, Neo4emfUiPlugin.PLUGIN_ID,
        		e.getMessage() != null ? e.getMessage() : e.toString(), e));
    }

    public static void log(int severity, String msg, Throwable e) {
    	log.log(new Status(severity, Neo4emfUiPlugin.PLUGIN_ID, msg, e));
    }
    
    public static void log(int severity, String msg) {
        log.log(new Status(severity, Neo4emfUiPlugin.PLUGIN_ID, msg, null));
    }
    
}
