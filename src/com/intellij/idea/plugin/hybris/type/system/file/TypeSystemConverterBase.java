/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

package com.intellij.idea.plugin.hybris.type.system.file;

import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaModel;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaModelAccess;
import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.ResolvingConverter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

public abstract class TypeSystemConverterBase<DOM> extends ResolvingConverter<DOM> {

    protected abstract DOM searchForName(
        @NotNull String name, @NotNull ConvertContext context, @NotNull TSMetaModel meta
    );

    protected abstract Collection<? extends DOM> searchAll(@NotNull ConvertContext context, @NotNull TSMetaModel meta);

    @Nullable
    @Override
    public final DOM fromString(
        @Nullable @NonNls final String s, final ConvertContext context
    ) {
        if (s == null) {
            return null;
        }
        return searchForName(s, context, getTypeSystemMeta(context));
    }

    @NotNull
    @Override
    public final Collection<? extends DOM> getVariants(final ConvertContext context) {
        return searchAll(context, getTypeSystemMeta(context));
    }

    protected final TSMetaModel getTypeSystemMeta(@NotNull final ConvertContext convertContext) {
        return getTypeSystemMeta(convertContext.getProject());
    }

    protected final TSMetaModel getTypeSystemMeta(@NotNull final Project project) {
        return TSMetaModelAccess.getInstance(project).getTypeSystemMeta();
    }

    protected static <D extends DomElement> XmlAttributeValue navigateToValue(
        @Nullable final D dom,
        @NotNull final Function<? super D, GenericAttributeValue<?>> attribute
    ) {

        return
            Optional.ofNullable(dom)
                    .map(attribute)
                    .map(GenericAttributeValue::getXmlAttributeValue)
                    .orElse(null);

    }

    protected static <D extends DomElement, R> R useAttributeValue(
        @Nullable final D dom,
        @NotNull final Function<? super D, GenericAttributeValue<R>> attribute
    ) {

        return
            Optional.ofNullable(dom)
                    .map(attribute)
                    .map(GenericAttributeValue::getValue)
                    .orElse(null);

    }
}
