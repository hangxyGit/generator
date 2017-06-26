/**
 *    Copyright 2006-2017 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.OutputUtilities;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;

import java.util.Iterator;

/**
 * @author Jeff Butler
 */
public class BatchUpdateBySelectiveElementGenerator extends
        AbstractXmlElementGenerator {

    private boolean isSimple;

    public BatchUpdateBySelectiveElementGenerator(boolean isSimple) {
        super();
        this.isSimple = isSimple;
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement answer = new XmlElement("update"); //$NON-NLS-1$

        answer.addAttribute(new Attribute(
                "id", introspectedTable.getBatchUpdateByPrimaryKeySelectiveStatementId())); //$NON-NLS-1$
        answer.addAttribute(new Attribute("parameterType", "java.util.List"));

        context.getCommentGenerator().addComment(answer);

        // <foreach collection="list" item="record" separator=";">
        XmlElement foreachElement = new XmlElement("foreach");
        foreachElement.addAttribute(new Attribute("collection", "list"));
        foreachElement.addAttribute(new Attribute("item", "record"));
        foreachElement.addAttribute(new Attribute("separator", ";"));

        StringBuilder sb = new StringBuilder();
        sb.append("update "); //$NON-NLS-1$
        sb.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
        foreachElement.addElement(new TextElement(sb.toString()));

        // set up for first column
        sb.setLength(0);
        sb.append("set "); //$NON-NLS-1$

        Iterator<IntrospectedColumn> iter;
        if (isSimple) {
            iter = ListUtilities.removeGeneratedAlwaysColumns(introspectedTable.getNonPrimaryKeyColumns()).iterator();
        } else {
            iter = ListUtilities.removeGeneratedAlwaysColumns(introspectedTable.getBaseColumns()).iterator();
        }

        while (iter.hasNext()) {
            IntrospectedColumn introspectedColumn = iter.next();
            sb.setLength(0);
            sb.append(introspectedColumn.getJavaProperty("record.")); //$NON-NLS-1$
            sb.append(" != null"); //$NON-NLS-1$
            XmlElement isNotNullElement = new XmlElement("if"); //$NON-NLS-1$
            isNotNullElement.addAttribute(new Attribute("test", sb.toString())); //$NON-NLS-1$

            sb.setLength(0);
            sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
            sb.append(" = "); //$NON-NLS-1$
            sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, "record."));

            if (iter.hasNext()) {
                sb.append(',');
            }
            isNotNullElement.addElement(new TextElement(sb.toString()));
            foreachElement.addElement(isNotNullElement);

            // set up for the next column
            if (iter.hasNext()) {
                sb.setLength(0);
                OutputUtilities.xmlIndent(sb, 1);
            }
        }

        boolean and = false;
        for (IntrospectedColumn introspectedColumn : introspectedTable
                .getPrimaryKeyColumns()) {
            sb.setLength(0);
            if (and) {
                sb.append("  and ");
            } else {
                sb.append("where ");
                and = true;
            }

            sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
            sb.append(" = "); //$NON-NLS-1$
            sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, "record."));
            foreachElement.addElement(new TextElement(sb.toString()));
        }
        answer.addElement(foreachElement);

        if (context.getPlugins()
                .sqlMapBatchUpdateBySelectiveElementGenerated(answer,
                        introspectedTable)) {
            parentElement.addElement(answer);
        }
    }
}
