package com.autotest.LiuMa.dto;

import com.autotest.LiuMa.database.domain.Element;
import com.excel.poi.annotation.ExportField;
import com.excel.poi.annotation.ImportField;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ElementImportDTO extends Element {
    @ExportField(columnName = "元素名称(必填)", defaultCellValue = "完成编辑")
    @ImportField(required = true)
    private String name;
    @ExportField(columnName = "定位方式(必填)", defaultCellValue = "XPATH")
    @ImportField(required = true)
    private String by;
    @ExportField(columnName = "表达式(必填)", defaultCellValue = "//*[@id=\"divdsc\"]")
    @ImportField(required = true)
    private String expression;
    @ExportField(columnName = "元素描述", defaultCellValue = "这是完成编辑元素定位")
    @ImportField(required = false)
    private String description;
}
