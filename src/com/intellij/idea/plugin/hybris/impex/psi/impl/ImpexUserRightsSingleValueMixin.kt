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

package com.intellij.idea.plugin.hybris.impex.psi.impl

import com.intellij.idea.plugin.hybris.impex.psi.ImpexUserRightsAttributeValue
import com.intellij.idea.plugin.hybris.impex.psi.ImpexUserRightsSingleValue
import com.intellij.idea.plugin.hybris.impex.psi.references.ImpexTSItemReference
import com.intellij.idea.plugin.hybris.impex.psi.references.ImpexUserRightsTSAttributeReference
import com.intellij.idea.plugin.hybris.psi.impl.ASTWrapperReferencePsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.removeUserData
import com.intellij.psi.util.PsiTreeUtil
import java.io.Serial

abstract class ImpexUserRightsSingleValueMixin(astNode: ASTNode) : ASTWrapperReferencePsiElement(astNode), ImpexUserRightsSingleValue {

    override fun createReference() = ImpexTSItemReference(this)

    override fun subtreeChanged() {
        removeUserData(ImpexTSItemReference.CACHE_KEY)
        PsiTreeUtil.getNextSiblingOfType(this, ImpexUserRightsAttributeValue::class.java)
            ?.removeUserData(ImpexUserRightsTSAttributeReference.CACHE_KEY)
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -7538536745431644864L
    }
}
