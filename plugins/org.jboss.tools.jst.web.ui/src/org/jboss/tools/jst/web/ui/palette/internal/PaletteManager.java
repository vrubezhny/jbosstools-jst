/******************************************************************************* 
 * Copyright (c) 2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.jst.web.ui.palette.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jboss.tools.common.model.XModelObject;
import org.jboss.tools.common.model.XModelObjectConstants;
import org.jboss.tools.common.model.options.PreferenceModelUtilities;
import org.jboss.tools.jst.web.ui.internal.editor.jspeditor.dnd.MobilePaletteInsertHelper;
import org.jboss.tools.jst.web.ui.palette.model.PaletteModel;

/**
 * This class returns categories, versions, and items of Palette.
 * Each returned item contains category name, version name, item name,
 * and can compute result generated by wizard with default settings.
 * 
 * @author Viacheslav Kabanovich
 *
 */
public class PaletteManager {
	private static PaletteManager instance = new PaletteManager();

	public static PaletteManager getInstance() {
		return instance;
	}

	private PaletteManager() {}

	/**
	 * Returns array of category names available in Palette.
	 * Currently available are
	 * 		JQueryConstants.JQM_CATEGORY
	 * 		HTMLConstants.HTML_CATEGORY
	 * @return
	 */
	public String[] getCategories() {
		XModelObject g = PreferenceModelUtilities.getPreferenceModel().getByPath(PaletteModel.MOBILE_PATH);
		XModelObject[] cs = g.getChildren();
		String[] result = new String[cs.length];
		for (int i = 0; i < cs.length; i++) {
			String category = cs[i].getAttributeValue(XModelObjectConstants.ATTR_NAME);
			if(category.indexOf('.') >= 0) {
				category = category.substring(category.indexOf('.') + 1);
			}
			result[i] = category;
		}
		return result;
	}

	/**
	 * Returns list of versions supported by Palette for a given category.
	 * Currently available are
	 * 		For JQueryConstants.JQM_CATEGORY versions are listed in JQueryMobileVersion
	 * 		For HTMLConstants.HTML_CATEGORY  versions are listed in HTMLVersion
	 * @param category
	 * @return
	 */
	public String[] getVersions(String category) {
		XModelObject g = findCategory(category);
		if(g == null) {
			return new String[0];
		}
		XModelObject[] cs = g.getChildren();
		String[] result = new String[cs.length];
		for (int i = 0; i < cs.length; i++) {
			String name = cs[i].getAttributeValue(XModelObjectConstants.ATTR_NAME);
			if(name.startsWith(PaletteModel.VERSION_PREFIX)) {
				result[i] = name.substring(8); 
			}
		}
		return result;
	}

	/**
	 * Returns runnable objects for palette items.
	 * Available versions
	 * 		For JQueryConstants.JQM_CATEGORY versions are listed in JQueryMobileVersion
	 * 		For HTMLConstants.HTML_CATEGORY  versions are listed in HTMLVersion
	 * @param category
	 * @param version
	 * @return
	 */
	public Collection<RunnablePaletteItem> getItems(String category, String version) {
		List<RunnablePaletteItem> result = new ArrayList<RunnablePaletteItem>();
		XModelObject g = findCategory(category);
		if(g != null) {
			g = g.getChildByPath(PaletteModel.VERSION_PREFIX + version);
		}
		if(g == null) {
			return result;
		}
		collectItems(result, category, version, g);
		return result;
	}

	private void collectItems(List<RunnablePaletteItem> result, String category, String version, XModelObject g) {
		XModelObject[] cs = g.getChildren();
		if(cs.length == 0) {
			String name = g.getAttributeValue(XModelObjectConstants.ATTR_NAME);
			String startText = g.getAttributeValue(XModelObjectConstants.START_TEXT);
			if(MobilePaletteInsertHelper.INSERT_JS_CSS_SIGNATURE.equals(startText)) {
				return;
			}
			if(name.indexOf('.') >= 0) {
				name = name.substring(name.indexOf('.') + 1);
			}
			result.add(new RunnablePaletteItem(category, version, name));
		} else {
			for (int i = 0; i < cs.length; i++) {
				collectItems(result, category, version, cs[i]);
			}
		}
	}

	private XModelObject findCategory(String category) {
		XModelObject g = PreferenceModelUtilities.getPreferenceModel().getByPath(PaletteModel.MOBILE_PATH);
		if(g != null) {
			XModelObject[] cs = g.getChildren();
			for (int i = 0; i < cs.length; i++) {
				String name = cs[i].getAttributeValue(XModelObjectConstants.ATTR_NAME);
				if(name.equals(category) || name.endsWith("." + category)) {
					return cs[i];
				}
			}
		}
		return null;
	}
}
