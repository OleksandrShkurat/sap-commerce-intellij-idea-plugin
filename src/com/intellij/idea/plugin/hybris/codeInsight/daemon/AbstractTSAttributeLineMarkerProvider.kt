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

package com.intellij.idea.plugin.hybris.codeInsight.daemon

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaItem
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaRelation.RelationEnd
import com.intellij.idea.plugin.hybris.system.type.utils.ModelsUtils
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.xml.XmlElement
import javax.swing.Icon

abstract class AbstractTSAttributeLineMarkerProvider<T : PsiElement> : AbstractTSItemLineMarkerProvider<T>() {

    override fun getTooltipText() = message("hybris.editor.gutter.item.attribute.tooltip.navigate.declaration")
    override fun getPopupTitle() = message("hybris.editor.gutter.bean.attribute.navigate.choose.class.title")
    override fun getEmptyPopupText() = message("hybris.editor.gutter.navigate.no.matching.attributes")

    override fun collectDeclarations(psi: T): RelatedItemLineMarkerInfo<PsiElement>? {
        val psiClass = psi.parent
        if (psiClass !is PsiClass || !ModelsUtils.isModelFile(psiClass)) return null

        return TSMetaModelAccess.getInstance(psi.project).findMetaItemByName(cleanSearchName(psiClass.name))
            ?.let { collect(it, psi) }
    }

    protected abstract fun collect(meta: TSGlobalMetaItem, psi: T): RelatedItemLineMarkerInfo<PsiElement>?

    protected open fun getPsiElementRelatedItemLineMarkerInfo(
        meta: TSGlobalMetaItem, name: String, nameIdentifier: PsiIdentifier
    ) = with(getAttributeElements(meta, name)) {
        if (this.isEmpty()) {
            val groupedRelElements = getRelations(meta, name)
            return@with getRelationMarkers(groupedRelElements, RelationEnd.SOURCE, HybrisIcons.TS_RELATION_SOURCE, nameIdentifier)
                ?: getRelationMarkers(groupedRelElements, RelationEnd.TARGET, HybrisIcons.TS_RELATION_TARGET, nameIdentifier)
        } else {
            return@with createTargetsWithGutterIcon(nameIdentifier, this, HybrisIcons.TS_ATTRIBUTE)
        }
    }

    open fun getAttributeElements(meta: TSGlobalMetaItem, name: String) = meta.allAttributes
        .filter { it.name == name }
        .flatMap { it.declarations }
        .map { it.domAnchor }
        .mapNotNull { it.retrieveDomElement() }
        .mapNotNull { it.xmlElement }

    open fun getRelations(meta: TSGlobalMetaItem, name: String) = meta.allRelationEnds
        .filter { it.qualifier == name }
        .filter { it.isNavigable }
        .filter { it.domAnchor.retrieveDomElement()?.xmlElement != null }
        .groupBy({ it.end }, { it.domAnchor.retrieveDomElement()!!.xmlElement!! })

    open fun getRelationMarkers(
        groupedRelElements: Map<RelationEnd, List<XmlElement?>>,
        target: RelationEnd,
        icon: Icon,
        nameIdentifier: PsiIdentifier
    ) = groupedRelElements[target]
        ?.let { createTargetsWithGutterIcon(nameIdentifier, it, icon) }

}