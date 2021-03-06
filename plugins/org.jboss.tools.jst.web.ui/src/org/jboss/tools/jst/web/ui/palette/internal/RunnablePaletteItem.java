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

import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.texteditor.ITextEditor;
import org.jboss.tools.common.model.ui.editors.dnd.DropData;
import org.jboss.tools.common.model.ui.internal.editors.PaletteItemResult;
import org.jboss.tools.common.text.IExecutableTextProposal;
import org.jboss.tools.jst.web.kb.taglib.IHTMLLibraryVersion;
import org.jboss.tools.jst.web.ui.WebUiPlugin;
import org.jboss.tools.jst.web.ui.internal.editor.jspeditor.JSPMultiPageEditor;
import org.jboss.tools.jst.web.ui.internal.editor.jspeditor.JSPTextEditor;
import org.jboss.tools.jst.web.ui.internal.editor.jspeditor.dnd.PaletteItemDropCommand;
import org.jboss.tools.jst.web.ui.palette.html.wizard.AbstractNewHTMLWidgetWizard;
import org.jboss.tools.jst.web.ui.palette.internal.html.IPaletteItem;
import org.jboss.tools.jst.web.ui.palette.internal.html.IPaletteItemWizard;

/**
 * Objects of this class are returned by PaletteManager. and items of Palette.
 * It contains category name, version name, item name,
 * and can compute result generated by wizard with default settings.
 * 
 * @author Viacheslav Kabanovich
 *
 */
public class RunnablePaletteItem implements IExecutableTextProposal {
	private IPaletteItem paletteItem;

	public RunnablePaletteItem(IPaletteItem paletteItem) {
		this.paletteItem = paletteItem;
	}

	/**
	 * 
	 * @return category of palette item
	 */
	public String getCategory() {
		return paletteItem.getCategory().getVersionGroup().getGroup().getName();
	}

	/**
	 * 
	 * @return category version of palette item 
	 */
	public IHTMLLibraryVersion getVersion() {
		return paletteItem.getCategory().getVersionGroup().getVersion();
	}

	/**
	 * 
	 * @return name of palette item
	 */
	public String getName() {
		return paletteItem.getName();
	}

	/**
	 * List of logic names by which this item can be invoked.
	 * @return
	 */
	public List<String> getAlternatives() {
		return paletteItem.getKeywords();
	}

	/**
	 * 
	 * @param textEditor
	 * @return result of generation of palette item wizard with default settings
	 */
	public PaletteItemResult getResult(JSPTextEditor textEditor) {
		IPaletteItemWizard wizard = paletteItem.createWizard();
		if(wizard instanceof AbstractNewHTMLWidgetWizard){
			return ((AbstractNewHTMLWidgetWizard) wizard).runWithoutUi(textEditor);
		}else{
			return new PaletteItemResult(paletteItem.getStartText(), paletteItem.getEndText());
		}
	}

	@Override
	public void apply(ITextEditor textEditor, int startOffset, int endOffset) {
		JSPTextEditor jsp = null;
		if(textEditor instanceof JSPTextEditor) {
			jsp = (JSPTextEditor)textEditor;
		} else if(textEditor instanceof JSPMultiPageEditor) {
			jsp = ((JSPMultiPageEditor)textEditor).getJspEditor();
		} else {
			return;
		}
		IDocument document = jsp.getTextViewer().getDocument();
		try {
			if(endOffset > startOffset) {
				document.replace(startOffset, endOffset - startOffset, "");
			}
		} catch (BadLocationException e) {
			WebUiPlugin.getDefault().logError(e);
		} catch (StringIndexOutOfBoundsException e) {
			WebUiPlugin.getDefault().logError(e);
		}

		IPaletteItemWizard wizard = paletteItem.createWizard();
		if(wizard instanceof AbstractNewHTMLWidgetWizard){
			((AbstractNewHTMLWidgetWizard) wizard).applyWithoutUi(jsp);
		}else{
			DropData dropData = new DropData(PaletteItemTransfer.PALETTE_ITEM, "",
					textEditor.getEditorInput(), ((JSPMultiPageEditor)textEditor).getJspEditor().getTextViewer(),
					textEditor.getSelectionProvider());
			PaletteItemDropCommand command = new PaletteItemDropCommand(paletteItem, true);
			command.execute(dropData);
		}
	}

}
