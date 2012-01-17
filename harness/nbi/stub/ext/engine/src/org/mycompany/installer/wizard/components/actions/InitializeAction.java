/*
*	Copyright (C) 2011 Life Technologies Inc.
*
*   This program is free software: you can redistribute it and/or modify
*   it under the terms of the GNU General Public License as published by
*   the Free Software Foundation, either version 2 of the License, or
*   (at your option) any later version.
*
*   This program is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU General Public License for more details.
*
*   You should have received a copy of the GNU General Public License
*   along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.mycompany.installer.wizard.components.actions;

import org.netbeans.installer.product.Registry;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.components.WizardAction;
import org.netbeans.installer.wizard.components.actions.*;

/**
 *
 * @author Dmitry Lipin
 */
public class InitializeAction extends WizardAction {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public InitializeAction() {
        setProperty(TITLE_PROPERTY, 
                DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY, 
                DEFAULT_DESCRIPTION);

        downloadLogic = new DownloadConfigurationLogicAction();
        initReg = new InitializeRegistryAction();
    }
    private DownloadConfigurationLogicAction downloadLogic;
    private InitializeRegistryAction initReg;
    
    public void execute() {
        final Progress progress = new Progress();
        
        //getWizardUi().setProgress(progress);
        

        progress.setTitle(getProperty(PROGRESS_TITLE_PROPERTY));

        //progress.synchronizeDetails(false);

        if (initReg.canExecuteForward()) {
            initReg.setWizard(getWizard());
            initReg.execute();
        }
    
        if (downloadLogic.canExecuteForward()) {
            downloadLogic.setWizard(getWizard());
            downloadLogic.execute();
        }
    }
    
    @Override
    public boolean isCancelable() {
        return false;
    }

    public WizardActionUi getWizardUi() {
        return null; // this action does not have a ui
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_TITLE = ResourceUtils.getString(
            InitializeAction.class, 
            "IA.title"); // NOI18N
    public static final String PROGRESS_TITLE_PROPERTY = ResourceUtils.getString(
            InitializeAction.class, 
            "IA.progress.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION = ResourceUtils.getString(
            InitializeAction.class, 
            "IA.description"); // NOI18N
    
}
