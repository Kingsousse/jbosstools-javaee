/******************************************************************************* 
 * Copyright (c) 2009 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.tools.cdi.core;

import java.util.Collection;

import org.jboss.tools.common.java.IParametedType;

/**
 * Represents a parameter of a method which is a member of bean class.
 * 
 * @author Alexey Kazakov
 */
public interface IParameter extends IBeanMember {

	/**
	 * Returns the declared type of this parameter.
	 * 
	 * @return the declared type of this parameter.
	 */
	IParametedType getType();

	/**
	 * Returns the name of this parameter.
	 * 
	 * @return the name of this parameter.
	 */
	String getName();

	/**
	 * Returns the bean method which has this parameter.
	 * 
	 * @return the bean method which has this parameter
	 */
	IBeanMethod getBeanMethod();

	/**
	 * 
	 * @return qualifier declarations for this parameter
	 */
	public Collection<IQualifierDeclaration> getQualifierDeclarations();
}