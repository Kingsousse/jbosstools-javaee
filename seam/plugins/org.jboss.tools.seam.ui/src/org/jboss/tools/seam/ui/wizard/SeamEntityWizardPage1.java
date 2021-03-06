/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.tools.seam.ui.wizard;

import java.beans.PropertyChangeEvent;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.jboss.tools.common.model.util.EclipseResourceUtil;
import org.jboss.tools.common.ui.IValidator;
import org.jboss.tools.common.ui.widget.editor.IFieldEditor;
import org.jboss.tools.seam.core.SeamCorePlugin;
import org.jboss.tools.seam.core.SeamProjectsSet;
import org.jboss.tools.seam.core.SeamCoreMessages;
import org.jboss.tools.seam.internal.core.project.facet.ISeamFacetDataModelProperties;
import org.jboss.tools.seam.internal.core.project.facet.SeamValidatorFactory;
import org.jboss.tools.seam.internal.core.validation.SeamProjectPropertyValidator;
import org.jboss.tools.seam.ui.SeamGuiPlugin;

/**
 * @author eskimo
 *
 */
public class SeamEntityWizardPage1 extends SeamBaseWizardPage {

	/**
	 * @param is 
	 * 
	 */
	public SeamEntityWizardPage1(IStructuredSelection is) {
		super("seam.new.entity.page1","Seam Entity", null, is); 
		setMessage(getDefaultMessageText());
	}

	/**
	 * 
	 */
	@Override
	protected void createEditors() {
		rootSeamProject = SeamWizardUtils.getRootSeamProject(initialSelection);
		String selectedProject = (rootSeamProject == null) ? "" : rootSeamProject.getName();
		addEditor(SeamWizardFactory.createSeamProjectSelectionFieldEditor(selectedProject));
		addEditor(SeamWizardFactory.createSeamEntityClasNameFieldEditor());

		String packageName = getDefaultPackageName(selectedProject);

		addEditor(SeamWizardFactory.createSeamJavaPackageSelectionFieldEditor(packageName));
		addEditor(SeamWizardFactory.createSeamMasterPageNameFieldEditor());
		addEditor(SeamWizardFactory.createSeamPageNameFieldEditor());

		setSeamProjectNameData(selectedProject);
	}

	@Override
	public void createControl(Composite parent) {
		setControl(new GridLayoutComposite(parent));
		setPageComplete(false);
		if ("".equals(editorRegistry.get(ISeamParameter.SEAM_PROJECT_NAME).getValue())) { //$NON-NLS-1$
			if(getEditor(ISeamParameter.SEAM_PACKAGE_NAME)!=null) {
				getEditor(ISeamParameter.SEAM_PACKAGE_NAME).setEnabled(false);
			}
		}
	}

	@Override
	public void doFillDefaults(PropertyChangeEvent event) {
		if(event.getPropertyName().equals(ISeamParameter.SEAM_ENTITY_CLASS_NAME)) {
			if(event.getNewValue()==null||"".equals(event.getNewValue().toString().trim())) { 
				setDefaultValue(ISeamParameter.SEAM_ENTITY_CLASS_NAME, ""); 
				setDefaultValue(ISeamParameter.SEAM_MASTER_PAGE_NAME, ""); 
				setDefaultValue(ISeamParameter.SEAM_PAGE_NAME, ""); 
			} else {
				String value = event.getNewValue().toString();
				String valueL = value.substring(0,1).toLowerCase() + value.substring(1);
				setDefaultValue(ISeamParameter.SEAM_MASTER_PAGE_NAME, valueL+"List");
				setDefaultValue(ISeamParameter.SEAM_PAGE_NAME, valueL);
			}
		}
		if(event.getPropertyName().equals(ISeamParameter.SEAM_PROJECT_NAME)) {
			String selectedProject = event.getNewValue().toString();
			setSeamProjectNameData(selectedProject);
			setDefaultValue(ISeamParameter.SEAM_PACKAGE_NAME, getDefaultPackageName(selectedProject));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.jboss.tools.seam.ui.wizard.SeamBaseWizardPage#getDefaultPackageName(org.eclipse.core.runtime.preferences.IEclipsePreferences)
	 */
	@Override
	protected String getDefaultPackageName(IEclipsePreferences seamFacetPrefs) {
		return seamFacetPrefs.get(ISeamFacetDataModelProperties.ENTITY_BEAN_PACKAGE_NAME, "");
	}

	/*
	 * (non-Javadoc)
	 * @see org.jboss.tools.seam.ui.wizard.SeamBaseWizardPage#isProjectSettingsOk()
	 */
	@Override
	protected boolean isProjectSettingsOk() {
		if(rootSeamProject!=null) {
			IEclipsePreferences prefs = SeamCorePlugin.getSeamPreferences(rootSeamProject);
			return SeamProjectPropertyValidator.isFolderPathValid(prefs.get(ISeamFacetDataModelProperties.ENTITY_BEAN_SOURCE_FOLDER, ""), false) &&
				SeamProjectPropertyValidator.isFolderPathValid(prefs.get(ISeamFacetDataModelProperties.SESSION_BEAN_SOURCE_FOLDER, ""), false) &&
				SeamProjectPropertyValidator.isFolderPathValid(prefs.get(ISeamFacetDataModelProperties.WEB_CONTENTS_FOLDER, ""), false) &&
				("false".equals(prefs.get(ISeamFacetDataModelProperties.TEST_CREATING, "false").trim()) || (SeamProjectPropertyValidator.isFolderPathValid(prefs.get(ISeamFacetDataModelProperties.TEST_SOURCE_FOLDER, ""), false) && SeamProjectPropertyValidator.isProjectNameValid(prefs.get(ISeamFacetDataModelProperties.SEAM_TEST_PROJECT, ""), false))) &&
				(ISeamFacetDataModelProperties.DEPLOY_AS_WAR.equals(prefs.get(ISeamFacetDataModelProperties.JBOSS_AS_DEPLOY_AS, ISeamFacetDataModelProperties.DEPLOY_AS_WAR).trim()) || SeamProjectPropertyValidator.isProjectNameValid(prefs.get(ISeamFacetDataModelProperties.SEAM_EJB_PROJECT, ""), false));
		}
		return true;
	}

	@Override
	protected void doValidate(PropertyChangeEvent event) {
		if(!isValidProjectSelected()) return;

		IFieldEditor packageEditor = getEditor(ISeamParameter.SEAM_PACKAGE_NAME);
		if(packageEditor!=null) {
			packageEditor.setEnabled(true);
		}

		IProject project = getSelectedProject();

		if(!isValidRuntimeConfigured(project)) return;

		Map<String, IStatus> errors = SeamValidatorFactory.SEAM_COMPONENT_NAME_VALIDATOR.validate(
				editorRegistry.get(ISeamParameter.SEAM_ENTITY_CLASS_NAME).getValue(), null);

		if(!errors.isEmpty()) {
			setErrorMessage(NLS.bind(errors.get(IValidator.DEFAULT_ERROR).getMessage(),SeamCoreMessages.SEAM_ENTITY_WIZARD_PAGE1_ENTITY_CLASS_NAME));
			setPageComplete(false);
			return;
		}

		IFieldEditor editor = editorRegistry.get(ISeamParameter.SEAM_PACKAGE_NAME);
		if(editor!=null) {
			errors = SeamValidatorFactory.PACKAGE_NAME_VALIDATOR.validate(editor.getValue(), null);
			if(!errors.isEmpty()) {
				setErrorMessage(errors.get(IValidator.DEFAULT_ERROR).getMessage()); //$NON-NLS-1$
				setPageComplete(false);
				return;
			}
		}

		errors = SeamValidatorFactory.FILE_NAME_VALIDATOR.validate(
				editorRegistry.get(ISeamParameter.SEAM_MASTER_PAGE_NAME).getValue(), new Object[]{SeamCoreMessages.SEAM_ENTITY_WIZARD_PAGE1_ENTITY_MASTER_PAGE,project,project});

		if(!errors.isEmpty()) {
			setErrorMessage(errors.get(IValidator.DEFAULT_ERROR).getMessage());
			setPageComplete(false);
			return;
		}

		errors = SeamValidatorFactory.FILE_NAME_VALIDATOR.validate(
				editorRegistry.get(ISeamParameter.SEAM_PAGE_NAME).getValue(), new Object[]{SeamCoreMessages.SEAM_ENTITY_WIZARD_PAGE1_PAGE,project});

		if(!errors.isEmpty()) {
			setErrorMessage(errors.get(IValidator.DEFAULT_ERROR).getMessage());
			setPageComplete(false);
			return;
		}
		
		IJavaProject javaProject = EclipseResourceUtil.getJavaProject(project);
		
		if(javaProject != null){
			try{
				IType component = javaProject.findType((String)editorRegistry.get(ISeamParameter.SEAM_PACKAGE_NAME).getValue()+"."+editorRegistry.get(ISeamParameter.SEAM_ENTITY_CLASS_NAME).getValue());
				if(component != null){
					setErrorMessage(null);
					setMessage(SeamCoreMessages.ENTITY_CLASS_ALREADY_EXISTS, IMessageProvider.WARNING);
					setPageComplete(true);
					return;
				}
			}catch(JavaModelException ex){
				SeamGuiPlugin.getPluginLog().logError(ex);
			}
		}
		
		SeamProjectsSet seamPrjSet = new SeamProjectsSet(project);
		IPath webContent = seamPrjSet.getViewsFolder().getFullPath();
		
		IPath masterPage = webContent.append(editorRegistry.get(ISeamParameter.SEAM_MASTER_PAGE_NAME).getValue()+".xhtml");
		
		IFile masterPageFile = ResourcesPlugin.getWorkspace().getRoot().getFile(masterPage);
		if(masterPageFile.exists()){
			setErrorMessage(null);
			setMessage(SeamCoreMessages.MASTER_PAGE_ALREADY_EXISTS, IMessageProvider.WARNING);
			setPageComplete(true);
			return;
		}
		
		IPath page = webContent.append(editorRegistry.get(ISeamParameter.SEAM_PAGE_NAME).getValue()+".xhtml");
		
		IFile pageFile = ResourcesPlugin.getWorkspace().getRoot().getFile(page);
		if(pageFile.exists()){
			setErrorMessage(null);
			setMessage(SeamCoreMessages.PAGE_ALREADY_EXISTS, IMessageProvider.WARNING);
			setPageComplete(true);
			return;
		}
		
		setErrorMessage(null);
		setMessage(getDefaultMessageText());
		setPageComplete(true);
	}

	/* (non-Javadoc)
	 * @see org.jboss.tools.seam.ui.wizard.SeamBaseWizardPage#getDefaultMessageText()
	 */
	@Override
	public String getDefaultMessageText() {
		return "Create a new Entity";
	}
}