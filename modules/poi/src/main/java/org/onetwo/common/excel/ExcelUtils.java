package org.onetwo.common.excel;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ognl.Ognl;

import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.onetwo.common.exception.ServiceException;
import org.onetwo.common.log.MyLoggerFactory;
import org.onetwo.common.utils.DateUtil;
import org.onetwo.common.utils.LangUtils;
import org.slf4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

@SuppressWarnings("unchecked")
abstract public class ExcelUtils {
	
	private final static Logger logger = MyLoggerFactory.getLogger(ExcelUtils.class);
	

	public static TemplateModel readTemplate(String path){
		Resource config = new ClassPathResource(path);
		return readTemplate(config);
	}
	
	public static TemplateModel readTemplate(Resource config){
		XStream xstream = new XStream(new DomDriver());
		xstream.alias("template", TemplateModel.class);
		xstream.alias("rows", List.class);
		xstream.alias("row", RowModel.class);
		xstream.alias("field", FieldModel.class);
//		xstream.useAttributeFor(Number.class);
//		xstream.useAttributeFor(boolean.class);
//		xstream.useAttributeFor(String.class); 
//		xstream.useAttributeFor(int.class); 
		xstream.useAttributeFor(String.class);
		for(Class<?> btype : LangUtils.getBaseTypeClass()){
			xstream.useAttributeFor(btype);
		}
		
		
		TemplateModel template = null;
		try {
			if(config.exists()){
				template = (TemplateModel) xstream.fromXML(new FileInputStream(config.getFile()));
			}else{
				template = (TemplateModel) xstream.fromXML(config.getInputStream());
			}
		} catch (Exception e) {
			throw new ServiceException("读取模板["+config+"]配置出错：" + e.getMessage(), e);
		} 
		
		return template;
	}
	
	public static void setCellValue(Cell cell, Object value){
		if(value==null){
			cell.setCellValue("");
			return ;
		}
		 
		if(value instanceof Date){
//			cell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
			HSSFRichTextString cellValue = new HSSFRichTextString(DateUtil.formatDateTime((Date)value));
			cell.setCellValue(cellValue);
		}else{
			HSSFRichTextString cellValue = new HSSFRichTextString(value.toString());
			cell.setCellValue(cellValue);
		}
		
	}
	
	public static Object getCellValue(Cell cell){
		if(cell==null)
			return null;
		int type = cell.getCellType();
		Object value = null;
		if(Cell.CELL_TYPE_STRING==type){
			value = cell.getStringCellValue();
		}else if(Cell.CELL_TYPE_NUMERIC==type){
			value = cell.getNumericCellValue();
			if(value!=null && value.toString().indexOf(".")!=-1){
				String str = value.toString();
				int index = str.indexOf(".");
				str = str.substring(index+1);
				if(str.equals("0"))
					value = ((Double)value).longValue();
			}
		}else if(Cell.CELL_TYPE_FORMULA==type){
			value = cell.getCellFormula();
		}else if(Cell.CELL_TYPE_BOOLEAN==type){
			value = cell.getBooleanCellValue();
		}else if(Cell.CELL_TYPE_BLANK==type){
			value = "";
		}
		return value;
	}
	

	public static List<String> getRowValues(Row row){
		int cellCount = row.getPhysicalNumberOfCells();
		List<String> rowValues = new ArrayList<String>();
		
		Cell cell = null;
		Object cellValue = null;
		for(int i=0; i<cellCount; i++){
			cell = row.getCell(i);
			cellValue = ExcelUtils.getCellValue(cell);
			rowValues.add(cellValue.toString());
		}
		return rowValues;
	}

	public static Map mapRow(Row row, List<String> names){
		int cellCount = row.getPhysicalNumberOfCells();
		
		Cell cell = null;
		Object cellValue = null;
		Map<String, Object> rowValue = new HashMap<String, Object>();
		for(int i=0; i<names.size(); i++){
			String colName = names.get(i);
			if(i<=cellCount){
				cell = row.getCell(i);
				cellValue = ExcelUtils.getCellValue(cell);
			}
			rowValue.put(colName, cellValue);
		}
		return rowValue;
	}

	public static Object getValue(String exp, Map context, Object root){
		Object value = null;
		try {
			value = Ognl.getValue(exp, context, root);
		} catch (Exception e) {
			logger.error("["+exp+"] getValue error : " + e.getMessage());
		}
		return value;
	}
	
	public static void main(String[] args){
		String path = "excel.xml";
		TemplateModel template = readTemplate(path);
		System.out.println("name: " + template.getName());
	}

}
