package org.onetwo.common.excel;

import org.apache.poi.hssf.usermodel.HSSFSheet;

public class RowDataContext {
	final private RowModel rowModel;
	final private SheetData sheetData;
	
	public RowDataContext(SheetData sheetData, RowModel rowModel) {
		super();
		this.rowModel = rowModel;
		this.sheetData = sheetData;
	}
	public HSSFSheet getSheet() {
		return sheetData.getSheet();
	}
	public RowModel getRowModel() {
		return rowModel;
	}
	public Object getSheetData() {
		return sheetData;
	}
	
	
}
