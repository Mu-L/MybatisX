package com.baomidou.plugin.idea.mybatisx.smartjpa.common.iftest;

import com.baomidou.plugin.idea.mybatisx.dom.model.Mapper;
import com.baomidou.plugin.idea.mybatisx.smartjpa.common.MapperClassGenerateFactory;
import com.baomidou.plugin.idea.mybatisx.smartjpa.common.appender.JdbcTypeUtils;
import com.baomidou.plugin.idea.mybatisx.smartjpa.component.TxField;
import com.baomidou.plugin.idea.mybatisx.smartjpa.operate.generate.EmptyGenerator;
import com.baomidou.plugin.idea.mybatisx.smartjpa.operate.generate.Generator;
import com.baomidou.plugin.idea.mybatisx.smartjpa.operate.generate.MybatisAnnotationGenerator;
import com.baomidou.plugin.idea.mybatisx.smartjpa.operate.generate.MybatisXmlGenerator;
import com.baomidou.plugin.idea.mybatisx.ui.SmartJpaAdvanceUI;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The type Condition if test wrapper.
 *
 * @author ls9527
 */
public class ConditionIfTestWrapper implements ConditionFieldWrapper {
    public static final int DEFAULT_NEWLINE_VALUE = 3;
    private Project project;
    private Set<String> selectedWrapFields;
    private String allFieldsStr;
    private String resultMap;
    private boolean resultType;
    private String resultTypeClass;
    private List<String> resultFields;
    private Map<String, TxField> txFieldMap;
    private List<TxField> allFields;
    /**
     * 默认字段的关键字：  oracle: SYSDATE, mysql: NOW()
     */
    private String defaultDateWord;
    private SmartJpaAdvanceUI.GeneratorEnum generatorType;
    private Mapper mapper;
    private List<String> defaultDateList;

    /**
     * Instantiates a new Condition if test wrapper.
     *
     * @param project
     * @param selectedWrapFields the wrapper fields
     * @param resultFields
     * @param allFields
     * @param defaultDateWord
     */
    public ConditionIfTestWrapper(@NotNull Project project,
                                  Set<String> selectedWrapFields,
                                  List<String> resultFields,
                                  List<TxField> allFields,
                                  String defaultDateWord) {
        this.project = project;
        this.selectedWrapFields = selectedWrapFields;
        this.resultFields = resultFields;
        txFieldMap = allFields.stream().collect(Collectors.toMap(TxField::getFieldName, x -> x, (a, b) -> a));
        this.allFields = allFields;
        this.defaultDateWord = defaultDateWord;
    }

    @Override
    public String wrapConditionText(String fieldName, String templateText) {
        if (selectedWrapFields.contains(fieldName)) {
            templateText = wrapCondition(fieldName, templateText);
        }
        return templateText;
    }

    @NotNull
    private String wrapCondition(String fieldName, String templateText) {
        StringBuilder stringBuilder = new StringBuilder();
        final TxField txField = txFieldMap.get(fieldName);
        stringBuilder.append("<if test=\"").append(getConditionField(fieldName, txField.getFieldType())).append("\">");
        stringBuilder.append("\n").append(templateText);
        stringBuilder.append("\n").append("</if>");
        templateText = stringBuilder.toString();
        return templateText;
    }


    private String getConditionField(String fieldName, String fieldType) {
        String appender = "";
        if (Objects.equals(fieldType, "java.lang.String")) {
            appender = " and " + fieldName + " != ''";
        }
        return fieldName + " != null" + appender;
    }


    @Override
    public String wrapWhere(String content) {
        return "<where>\n" + content + "\n</where>";
    }

    @Override
    public String getAllFields() {
        return allFieldsStr;
    }

    /**
     * Sets all fields.
     *
     * @param allFieldsStr the all fields str
     */
    public void setAllFields(String allFieldsStr) {
        this.allFieldsStr = allFieldsStr;
    }

    @Override
    public String getResultMap() {
        return resultType ? null : resultMap;
    }

    /**
     * Sets result map.
     *
     * @param resultMap the result map
     */
    public void setResultMap(String resultMap) {
        this.resultMap = resultMap;
    }

    @Override
    public String getResultType() {
        return resultTypeClass;
    }

    /**
     * Sets result type.
     *
     * @param resultType the result type
     */
    public void setResultType(boolean resultType) {
        this.resultType = resultType;
    }

    @Override
    public Boolean isResultType() {
        return resultType;
    }

    @Override
    public Generator getGenerator(MapperClassGenerateFactory mapperClassGenerateFactory) {
        if (this.generatorType == SmartJpaAdvanceUI.GeneratorEnum.MYBATIS_ANNOTATION) {
            return new MybatisAnnotationGenerator(mapperClassGenerateFactory, mapper, project);
        } else if (this.generatorType == SmartJpaAdvanceUI.GeneratorEnum.MYBATIS_XML
            && mapper != null) {
            return new MybatisXmlGenerator(mapperClassGenerateFactory, mapper, project);
        }
        return new EmptyGenerator();
    }

    /**
     * Sets result type class.
     *
     * @param resultTypeClass the result type class
     */
    public void setResultTypeClass(String resultTypeClass) {
        this.resultTypeClass = resultTypeClass;
    }

    public void setGeneratorType(SmartJpaAdvanceUI.GeneratorEnum generatorType) {
        this.generatorType = generatorType;
    }

    @Override
    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 对于默认值 create_time,update_time, 在 更新和插入的时候替换为数据库默认值的关键字
     * MYSQL默认时间: NOW()
     * ORACLE默认时间: SYSDATE
     *
     * @param columnName 字段名
     * @param fieldValue
     * @return
     */
    @Override
    public String wrapDefaultDateIfNecessary(String columnName, String fieldValue) {
        if (defaultDateList.contains(columnName)) {
            return defaultDateWord;
        }
        return fieldValue;
    }

    @Override
    public List<String> getDefaultDateList() {
        return defaultDateList;
    }

    @Override
    public List<TxField> getResultTxFields() {
        Set<String> addedFields = new HashSet<>();
        return allFields.stream().filter(field -> resultFields.contains(field.getFieldName()) && addedFields.add(field.getFieldName())).collect(Collectors.toList());
    }

    private int newLine;

    @Override
    public int getNewline() {
        return newLine;
    }

    @Override
    public String wrapperField(String originName, String name, String canonicalTypeText) {
        TxField txField = txFieldMap.get(originName);
        if (txField != null) {
            String jdbcType = txField.getJdbcType();
            if (jdbcType != null) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("#{").append(name);
                stringBuilder.append(",jdbcType=").append(jdbcType);
                stringBuilder.append("}");
                return stringBuilder.toString();
            }
        }
        return JdbcTypeUtils.wrapperField(name, canonicalTypeText);
    }

    @Override
    public String wrapperBetweenCondition(String fieldName, String begin, String end, String templateText) {
        StringBuilder stringBuilder = new StringBuilder();
        final TxField txField = txFieldMap.get(fieldName);
        stringBuilder.append("<if test=\"").append(getConditionField(begin, txField.getFieldType()))
            .append(" and ")
            .append(getConditionField(end, txField.getFieldType()))
            .append("\">");
        stringBuilder.append("\n").append(templateText);
        stringBuilder.append("\n").append("</if>");
        return stringBuilder.toString();
    }

    public void setDefaultDateList(List<String> defaultDateList) {
        this.defaultDateList = defaultDateList;
    }


    public void setNewLine(int newLine) {
        // 如果设置错误的值, 给一个合适的默认值
        if (newLine <= 0) {
            newLine = DEFAULT_NEWLINE_VALUE;
        }
        this.newLine = newLine;
    }
}
