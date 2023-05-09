/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.intellij.idea.plugin.hybris.toolwindow.system.bean.forms;

import com.intellij.idea.plugin.hybris.notifications.Notifications;
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSGlobalMetaEnum;
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSMetaClassifier;
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSMetaEnum;
import com.intellij.idea.plugin.hybris.system.bean.model.Enum;
import com.intellij.idea.plugin.hybris.toolwindow.components.AbstractTable;
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.components.BSMetaEnumValuesTable;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import com.intellij.util.xml.DomElement;

import javax.swing.*;
import java.util.Objects;
import java.util.Optional;

public class BSMetaEnumView {

    private final Project myProject;
    private BSMetaClassifier<Enum> myMeta;
    private JPanel myContentPane;
    private JBTextField myDescription;
    private JBTextField myClass;
    private JBCheckBox myDeprecated;
    private JBTextField myTemplate;
    private JPanel myValuesPane;
    private JBPanel myDetailsPane;
    private JPanel myFlagsPane;
    private JBTextField myDeprecatedSince;
    private AbstractTable<BSGlobalMetaEnum, BSMetaEnum.BSMetaEnumValue> myEnumValues;

    public BSMetaEnumView(final Project project) {
        this.myProject = project;
    }

    private void initData(final BSGlobalMetaEnum myMeta) {
        if (Objects.equals(this.myMeta, myMeta)) {
            // same object, no need in re-init
            return;
        }
        this.myMeta = myMeta;

        myClass.setText(myMeta.getName());
        myTemplate.setText(myMeta.getTemplate());
        myDescription.setText(myMeta.getDescription());
        myDeprecatedSince.setText(myMeta.getDeprecatedSince());
        myDeprecated.setSelected(myMeta.isDeprecated());

        myEnumValues.updateModel(myMeta);
    }

    public JPanel getContent(final BSGlobalMetaEnum meta) {
        initData(meta);

        return myContentPane;
    }

    public JPanel getContent(final BSGlobalMetaEnum meta, final BSMetaEnum.BSMetaEnumValue metaValue) {
        initData(meta);

        myEnumValues.select(metaValue);

        return myContentPane;
    }

    private void createUIComponents() {
        myEnumValues = BSMetaEnumValuesTable.Companion.getInstance(myProject);
        myValuesPane = ToolbarDecorator.createDecorator(myEnumValues)
            .disableUpDownActions()
            .setRemoveAction(anActionButton -> Optional.ofNullable(myEnumValues.getCurrentItem())
                .map(BSMetaEnum.BSMetaEnumValue::retrieveDom)
                .map(DomElement::getXmlTag)
                .ifPresent(currentItem -> {
                    WriteCommandAction.runWriteCommandAction(myProject, () -> {
                        final String name = myEnumValues.getCurrentItem().getName();
                        currentItem.delete();

                        Notifications.create(
                            NotificationType.INFORMATION
                            , "Bean System - Enum modified"
                            , "Enum <code>" + myMeta.getName() + "</code>.<code>"+name+"</code> has been removed."
                        )
                            .notify(myProject);
                    });
                }))
            .setRemoveActionUpdater(e -> Optional.ofNullable(myEnumValues.getCurrentItem())
                .map(BSMetaClassifier::isCustom)
                .orElse(false))
            .setPanelBorder(JBUI.Borders.empty())
            .createPanel();
        myDetailsPane = new JBPanel();
        myFlagsPane = new JBPanel();

        myDetailsPane.setBorder(IdeBorderFactory.createTitledBorder("Details"));
        myFlagsPane.setBorder(IdeBorderFactory.createTitledBorder("Flags"));
        myValuesPane.setBorder(IdeBorderFactory.createTitledBorder("Values"));

        PopupHandler.installPopupMenu(myEnumValues, "BSView.ToolWindow.TablePopup", "BSView.ToolWindow.TablePopup");
    }
}
