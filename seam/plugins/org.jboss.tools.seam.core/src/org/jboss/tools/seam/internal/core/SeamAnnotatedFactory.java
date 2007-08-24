 /*******************************************************************************
  * Copyright (c) 2007 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributor:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
package org.jboss.tools.seam.internal.core;

import java.util.List;

import org.eclipse.jdt.core.IMethod;
import org.jboss.tools.seam.core.ISeamAnnotatedFactory;
import org.jboss.tools.seam.core.ISeamXmlComponentDeclaration;
import org.jboss.tools.seam.core.IValueInfo;
import org.jboss.tools.seam.core.ScopeType;
import org.jboss.tools.seam.core.event.Change;

/**
 * @author Viacheslav Kabanovich
 */
public class SeamAnnotatedFactory extends SeamJavaContextVariable implements ISeamAnnotatedFactory {
	SeamJavaComponentDeclaration parentDeclaration = null;
	boolean autoCreate = false;
	
	public SeamAnnotatedFactory() {}

	public IMethod getSourceMethod() {
		return (IMethod)javaSource;
	}
	
	public boolean isAutoCreate() {
		return autoCreate;
	}
	
	public void setAutoCreate(boolean autoCreate) {
		this.autoCreate = autoCreate;
	}

	public List<Change> merge(SeamObject s) {
		List<Change> changes = super.merge(s);
		SeamAnnotatedFactory af = (SeamAnnotatedFactory)s;

		if(autoCreate != af.autoCreate) {
			changes = Change.addChange(changes, new Change(this, ISeamXmlComponentDeclaration.AUTO_CREATE, autoCreate, af.autoCreate));
			autoCreate = af.autoCreate;
		}
	
		return changes;
	}

	public void setAutoCreate(IValueInfo value) {
		attributes.put(ISeamXmlComponentDeclaration.AUTO_CREATE, value);
		setAutoCreate(value != null && "true".equals(value.getValue()));
	}

	@Override
	public ScopeType getScope() {
		ScopeType value = super.getScope();
		if(value == null || value == ScopeType.UNSPECIFIED) {
			if(parentDeclaration != null) value = parentDeclaration.getScope();
		}
		if(value == null || value == ScopeType.UNSPECIFIED) {
			value = ScopeType.EVENT;
		}
		return value;
	}

	public void setParentDeclaration(SeamJavaComponentDeclaration parentDeclaration) {
		this.parentDeclaration = parentDeclaration;
	}

	public SeamAnnotatedFactory clone() throws CloneNotSupportedException {
		SeamAnnotatedFactory c = (SeamAnnotatedFactory)super.clone();
		//we need not new copy here but reference!
		c.parentDeclaration = parentDeclaration == null ? null : parentDeclaration.clone();
		return c;
	}

}
