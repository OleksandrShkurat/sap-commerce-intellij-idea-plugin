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

package com.intellij.idea.plugin.hybris.codeInsight.hints

import com.intellij.codeInsight.completion.CompletionMemory
import com.intellij.codeInsight.completion.JavaMethodCallElement
import com.intellij.codeInsight.hints.declarative.InlayTreeSink
import com.intellij.codeInsight.hints.declarative.InlineInlayPosition
import com.intellij.codeInsight.hints.declarative.SharedBypassCollector
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.model.Attribute
import com.intellij.idea.plugin.hybris.system.type.model.PersistenceType
import com.intellij.idea.plugin.hybris.system.type.utils.ModelsUtils
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiMethodCallExpression

class DynamicAttributeDeclarativeInlayHintsCollector : SharedBypassCollector {

    override fun collectFromElement(element: PsiElement, sink: InlayTreeSink) {
        if (!element.isValid || element.project.isDefault) return
        if (element !is PsiMethodCallExpression) return

        val method = (if(JavaMethodCallElement.isCompletionMode(element)) CompletionMemory.getChosenMethod(element) else null)
            ?: element.resolveMethodGenerics().element
            ?: return

        if (method !is PsiMethod) return
        if (method.containingClass == null) return
        if (!ModelsUtils.isItemModelFile(method.containingClass)) return

        val meta = TSMetaModelAccess.getInstance(element.project).findMetaItemByName(cleanSearchName(method.containingClass?.name)) ?: return
        val annotation = method.getAnnotation("de.hybris.bootstrap.annotations.Accessor") ?: return

        val qualifier = annotation.parameterList.attributes
            .filter { it.name == Attribute.QUALIFIER }
            .map { it.value }
            .filterIsInstance<PsiLiteralExpression>()
            .map { it.value }
            .firstOrNull { it != null } ?: return

        meta.allAttributes
            .filter { it.persistence.type == PersistenceType.DYNAMIC }
            .firstOrNull { it.name == qualifier } ?: return

        val identifier = element.methodExpression.lastChild

        sink.addPresentation(InlineInlayPosition(identifier.textRange.startOffset, true), hasBackground = true) {
            text(message("hybris.ts.type.dynamic"))
        }
    }

    private fun cleanSearchName(searchName: String?): String? {
        if (searchName == null) {
            return null
        }
        val idx = searchName.lastIndexOf(HybrisConstants.MODEL_SUFFIX)
        return if (idx == -1) {
            searchName
        } else searchName.substring(0, idx)
    }

}