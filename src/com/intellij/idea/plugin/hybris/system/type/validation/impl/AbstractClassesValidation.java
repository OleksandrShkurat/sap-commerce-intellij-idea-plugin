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

package com.intellij.idea.plugin.hybris.system.type.validation.impl;

import com.intellij.idea.plugin.hybris.system.type.validation.ItemsXmlValidator;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.util.xml.DomElement;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * @author Vlad Bozhenok <vladbozhenok@gmail.com>
 */
public abstract class AbstractClassesValidation<T extends DomElement, M extends DomElement> implements ItemsXmlValidator<T> {

    @Override
    public boolean validate(@Nullable List<? extends T> dom, @NotNull Map<String, ? extends PsiClass> psi) {
        if (dom == null) {
            return false;
        }

        for (final T itemType : dom) {
            if (this.isJavaClassGenerationDisabledForItemType(itemType)) {
                continue;
            }

            final PsiClass javaClass = this.getJavaClassForItemType(itemType, psi);

            if (null == javaClass) {
                return true;
            }

            if (!this.isJavaClassMatchesItemTypeDefinition(javaClass, itemType)) {
                return true;
            }
        }

        return false;
    }

    private boolean isJavaClassMatchesItemTypeDefinition(
        @NotNull final PsiClass javaClass,
        @NotNull final T itemType
    ) {
        final List<M> itemAttributes = this.getDefinedAttributes(itemType);

        final PsiField[] allFields = javaClass.getAllFields();
        for (final M itemAttribute : itemAttributes) {
            if (this.isJavaFieldGenerationDisabled(itemAttribute)) {
                continue;
            }

            if (!this.isJavaFieldGenerated(itemAttribute, allFields)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Finds attribute in generated class for attribute defined for type in items.xml
     */
    private boolean isJavaFieldGenerated(@NotNull final M itemAttribute, final PsiField @NotNull [] allFields) {
        for (final PsiField javaField : allFields) {
            final String itemAttributeName = this.buildJavaFieldName(itemAttribute);

            if (StringUtils.equalsIgnoreCase(javaField.getName(), itemAttributeName)) {
                return true;
            }

        }

        return false;
    }

    /**
     * Finds generated class for type defined in items.xml
     */
    @Nullable
    private PsiClass getJavaClassForItemType(
        @NotNull final T itemType,
        @NotNull final Map<String, ? extends PsiClass> generatedClasses
    ) {
        final String className = this.buildGeneratedClassName(itemType);

        return generatedClasses.get(className);
    }

    @NotNull
    public abstract String buildGeneratedClassName(@NotNull T itemType);

    @NotNull
    public abstract String buildJavaFieldName(@NotNull M itemAttribute);

    @NotNull
    public abstract List<M> getDefinedAttributes(@NotNull final T itemType);

    public abstract boolean isJavaClassGenerationDisabledForItemType(@NotNull final T itemType);

    protected abstract boolean isJavaFieldGenerationDisabled(@NotNull final M itemAttribute);
}