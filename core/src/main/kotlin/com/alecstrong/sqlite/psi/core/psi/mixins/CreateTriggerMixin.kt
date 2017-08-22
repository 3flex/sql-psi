package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.SqliteAnnotationHolder
import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteCreateTriggerStmt
import com.alecstrong.sqlite.psi.core.psi.SqliteQueryElement.QueryResult
import com.alecstrong.sqlite.psi.core.psi.SqliteTypes
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil

internal abstract class CreateTriggerMixin(
    node: ASTNode
) : SqliteCompositeElementImpl(node),
    SqliteCreateTriggerStmt {
  override fun queryAvailable(child: PsiElement): List<QueryResult> {
    if (child is MutatorMixin) {
      val table = super.queryAvailable(this).filter { it.table?.name == tableName.name }
      if (hasElement(SqliteTypes.INSERT)) {
        return table.map { QueryResult(TriggerRowElement(node, "new"), it.columns) }
      }
      if (hasElement(SqliteTypes.UPDATE)) {
        return table.map { QueryResult(TriggerRowElement(node, "new"), it.columns) } +
            table.map { QueryResult(TriggerRowElement(node, "old"), it.columns) }
      }
      if (hasElement(SqliteTypes.DELETE)) {
        return table.map { QueryResult(TriggerRowElement(node, "old"), it.columns) }
      }
    }
    return super.queryAvailable(child)
  }

  override fun annotate(annotationHolder: SqliteAnnotationHolder) {
    if (PsiTreeUtil.getParentOfType(this, SqlStmtListMixin::class.java)!!.triggers()
          .any { it != this && it.triggerName.text == triggerName.text }) {
      annotationHolder.createErrorAnnotation(triggerName,
          "Duplicate trigger name ${triggerName.text}")
    }
  }

  private fun hasElement(elementType: IElementType): Boolean {
    val child = node.findChildByType(elementType) ?: return false
    return child.treeParent == node
  }
}

private class TriggerRowElement(
    node: ASTNode,
    var rowName: String
): ASTWrapperPsiElement(node), PsiNamedElement {
  override fun getName() = rowName
  override fun setName(name: String) = apply { rowName = name }
}