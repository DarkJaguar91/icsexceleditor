package EtlinFinances;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import jxl.CellView;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Colour;
import jxl.write.Border;
import jxl.write.BorderLineStyle;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.NumberFormat;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

@SuppressWarnings("deprecation")
public class Finances {
	
	dataHolder totalData;
	
	HashMap<String, dataHolder> branchSupliers;
	
	public Finances() {
		totalData = new dataHolder();
		branchSupliers = new HashMap<>();
		
		JFileChooser chooser = new JFileChooser();
		
		int result = chooser.showOpenDialog(null);
		
		if (result == JFileChooser.CANCEL_OPTION)
			return;
		
		readDocument(chooser.getSelectedFile().getAbsolutePath());	
		
		if (branchSupliers.size() == 0){
			JOptionPane.showMessageDialog(null,
					"The file chosen is not of the correct type.", "Error",
					JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		
		GregorianCalendar c = new GregorianCalendar();
		DateFormat frmat = new SimpleDateFormat("ddMMMMyyyy");
		
		String target = chooser.getSelectedFile().getParent() + "\\report" + frmat.format(c.getTime()) + ".xls";
		
		writeFiles(target);		
		
		try {
			Desktop.getDesktop().open(new File(target));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// ///////////////////////// Writing
	// //////////////////////////////////////////////////////////////////////////////////
	// formats
		private static NumberFormat numberFormat = new NumberFormat(
				"#,##0;[RED]#,##0;-;\"no Text\"", NumberFormat.COMPLEX_FORMAT);
		
		public void writeFiles(String document){
			try {
				WritableWorkbook workbook = Workbook.createWorkbook(new File(document));
				
				// write total sheet
				writeSheet(workbook.createSheet("All Branches", 0), totalData);
				
				SortedSet<String> branches = new TreeSet<String>(branchSupliers.keySet());
				
				int cnt = 1;
				for (String branch : branches){
					writeSheet(workbook.createSheet(branch, cnt++), branchSupliers.get(branch));
				}
				
				workbook.write();
				workbook.close();				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (WriteException e) {
				e.printStackTrace();
			} 

		}
		
		public void writeSheet(WritableSheet sheet, dataHolder holder) throws RowsExceededException, WriteException{
			ArrayList<weekInYear> weeks = holder.weeks;
			Collections.sort(weeks);
			
			ArrayList<SuplierData> supliers = holder.supliers;
			Collections.sort(supliers);
			
			DateFormat dformat = new SimpleDateFormat("dd MMMM yyyy");
			GregorianCalendar c = new GregorianCalendar();
			
			weekInYear currentWeek = new weekInYear(c.get(GregorianCalendar.WEEK_OF_YEAR), c.get(GregorianCalendar.YEAR));
			
			
			double backlogC = 0;
			double backlogU = 0;
			
			double totalC = 0;
			double totalU = 0;
			for (int w = 0; w < weeks.size(); ++w){
		
				c.set(GregorianCalendar.WEEK_OF_YEAR, weeks.get(w).week);
				c.set(GregorianCalendar.YEAR, weeks.get(w).year);
				c.set(GregorianCalendar.DAY_OF_WEEK, 0);
				
				Colour col = currentWeek.compareTo(weeks.get(w)) > 0 ? Colour.RED : currentWeek.compareTo(weeks.get(w))< 0 ? null : Colour.YELLOW; 
				
				sheet.mergeCells(w * 2 + 1, 0, w * 2 + 2, 0);
				
				addString(sheet, w * 2 + 1, 0, "Week: " + c.get(GregorianCalendar.WEEK_OF_YEAR) + " - " + dformat.format(c.getTime()), null, Alignment.CENTRE, "TLRb");
				
				for (int i = 0; i < supliers.size(); ++i){
					
					if (supliers.get(i).vals.contains(new values(weeks.get(w)))){
						addNumber(sheet, w * 2 + 1, 2 + i, supliers.get(i).vals.get(supliers.get(i).vals.indexOf(new values(weeks.get(w)))).unconfirmed, Colour.GREY_40_PERCENT, Alignment.RIGHT, "Lr");
						addNumber(sheet, w * 2 + 2, 2 + i, supliers.get(i).vals.get(supliers.get(i).vals.indexOf(new values(weeks.get(w)))).confirmed, col, Alignment.RIGHT, "lR");
						if (weeks.get(w).compareTo(currentWeek) < 0){
							backlogU += supliers.get(i).vals.get(supliers.get(i).vals.indexOf(new values(weeks.get(w)))).unconfirmed;
							backlogC += supliers.get(i).vals.get(supliers.get(i).vals.indexOf(new values(weeks.get(w)))).confirmed;
						}
					}else{
						addNumber(sheet, w * 2 + 1, 2 + i, 0, Colour.GREY_40_PERCENT, Alignment.RIGHT, "Lr");
						addNumber(sheet, w * 2 + 2, 2 + i, 0, col, Alignment.RIGHT, "lR");
					}						
					
					if (w == weeks.size() - 1){ // write out suplier totals once only at end
						addString(sheet, 0, 2 + i, supliers.get(i).name, null, Alignment.LEFT, "LRtb");
						addNumber(sheet, weeks.size() * 2 + 1, 2 + i, supliers.get(i).totalforSuplier.unconfirmed, Colour.GREY_40_PERCENT, Alignment.RIGHT, "Lr" + (i == supliers.size() - 1 ? "B" : ""));
						addNumber(sheet, weeks.size() * 2 + 2, 2 + i, supliers.get(i).totalforSuplier.confirmed, null, Alignment.RIGHT, "lR" + (i == supliers.size() - 1 ? "B" : ""));
					}
				}
				
				addHeading(sheet, w * 2 + 1, 1, "Unconfirmed", 4000, Colour.GREY_40_PERCENT, Alignment.CENTRE, "LtBr");
				addHeading(sheet, w * 2 + 2, 1, "Confirmed", 4000, col, Alignment.CENTRE, "ltBR");
				addNumber(sheet, w * 2 + 1, 2 + supliers.size(), weeks.get(w).unconfTotal, Colour.GREY_40_PERCENT, Alignment.RIGHT, "TBLr");
				addNumber(sheet, w * 2 + 2, 2 + supliers.size(), weeks.get(w).confTotal, col, Alignment.RIGHT, "TBlR");
				totalC += weeks.get(w).confTotal;
				totalU += weeks.get(w).unconfTotal;
			}
			
			// add suplier name heading
			addHeading(sheet, 0, 1, "Supplier Name", 15000, null, Alignment.LEFT, "TLBR");
			// totals headings
			addString(sheet, 0, 2 + supliers.size(), "Totals:", null, Alignment.RIGHT, "TBLR");
			sheet.mergeCells(weeks.size() * 2 + 1, 0, weeks.size() * 2 + 2, 0);
			addString(sheet, weeks.size() * 2 + 1, 0, "Totals:", null, Alignment.CENTRE, "TLRb");
			addHeading(sheet, weeks.size() * 2 + 1, 1, "Unconfirmed", 4000, Colour.GREY_40_PERCENT, Alignment.CENTRE, "LtrB");
			addHeading(sheet, weeks.size() * 2 + 2, 1, "Confirmed", 4000, null, Alignment.CENTRE, "ltRB");
			
			// total totals of all totals
			addNumber(sheet, weeks.size() * 2 + 1, 2 + supliers.size(), totalU, Colour.GREY_40_PERCENT, Alignment.RIGHT, "TLBr");
			addNumber(sheet, weeks.size() * 2 + 2, 2 + supliers.size(), totalC, null, Alignment.RIGHT, "TlBR");
			
			// backlog
			if (weeks.contains(currentWeek)){
				addString(sheet, weeks.indexOf(currentWeek) * 2, 4 + supliers.size(), "Backlog Totals:", null, Alignment.CENTRE, "TBLR");
				addNumber(sheet, weeks.indexOf(currentWeek) * 2 + 1, 4 + supliers.size(), backlogU, Colour.GREY_40_PERCENT, Alignment.RIGHT, "TBLr");
				addNumber(sheet, weeks.indexOf(currentWeek) * 2 + 2, 4 + supliers.size(), backlogC, null, Alignment.RIGHT, "TBlR");
			}
			else{
				addString(sheet, 0, 4 + supliers.size(), "Backlog Totals:", null, Alignment.CENTRE, "TBLR");
				addNumber(sheet, 1, 4 + supliers.size(), backlogU, Colour.GREY_40_PERCENT, Alignment.RIGHT, "TBLr");
				addNumber(sheet, 2, 4 + supliers.size(), backlogC, null, Alignment.RIGHT, "TBlR");
			}
			
			// freeze panes
			sheet.getSettings().setVerticalFreeze(2);
			sheet.getSettings().setHorizontalFreeze(1);
		}
		
	/**
	 * Gets format for the cell
	 * 
	 * @param col
	 *            the colour for the cell
	 * @param align
	 *            the alignment for the cell
	 * @param number
	 *            if the cell should be a number
	 * @param border
	 *            the border string for the cell
	 * @return the cells format
	 */
	private static WritableCellFormat getCellFormat(Colour col,
			Alignment align, boolean number, String border) {
		WritableCellFormat format = new WritableCellFormat();
		try {
			if (col == null)
				col = Colour.WHITE;

			if (number)
				format = new WritableCellFormat(numberFormat);

			// work with border
			if (border != null) {
				if (border.contains("T"))
					format.setBorder(Border.TOP, BorderLineStyle.MEDIUM);
				if (border.contains("t"))
					format.setBorder(Border.TOP, BorderLineStyle.THIN);
				if (border.contains("B"))
					format.setBorder(Border.BOTTOM, BorderLineStyle.MEDIUM);
				if (border.contains("b"))
					format.setBorder(Border.BOTTOM, BorderLineStyle.THIN);
				if (border.contains("L"))
					format.setBorder(Border.LEFT, BorderLineStyle.MEDIUM);
				if (border.contains("l"))
					format.setBorder(Border.LEFT, BorderLineStyle.THIN);
				if (border.contains("R"))
					format.setBorder(Border.RIGHT, BorderLineStyle.MEDIUM);
				if (border.contains("r"))
					format.setBorder(Border.RIGHT, BorderLineStyle.THIN);
			}

			format.setAlignment(align);
			format.setBackground(col);
		} catch (Exception e) {
			JOptionPane
					.showMessageDialog(
							null,
							"Error - The cell format could not be written.\nPlease contact your administrator.",
							"Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		return format;
	}

	/**
	 * Add number to the sheet
	 * 
	 * @param sheet
	 *            sheet to write to
	 * @param c
	 *            column
	 * @param r
	 *            row
	 * @param number
	 *            number to add
	 */
	private void addNumber(WritableSheet sheet, int c, int r, double number,
			Colour col, Alignment align, String border) {
		try {
			Number n = new Number(c, r, number, getCellFormat(col, align, true,
					border));

			sheet.addCell(n);
		} catch (RowsExceededException e) {
			JOptionPane
					.showMessageDialog(
							null,
							"The excel file has exceeded its bounds.\nPlease contact your administrator",
							"Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		} catch (WriteException e) {
			JOptionPane
					.showMessageDialog(
							null,
							"The excel file could not be written to.\nPlease contact your administrator",
							"Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

	/**
	 * Adds a string to the sheet at the given column
	 * 
	 * @param sheet
	 *            Sheet to write to
	 * @param c
	 *            the column number
	 * @param r
	 *            the row number
	 * @param string
	 *            the string to enter
	 */
	private void addString(WritableSheet sheet, int c, int r, String string,
			Colour col, Alignment align, String border) {
		try {
			Label l = new Label(c, r, string, getCellFormat(col, align, false,
					border));

			sheet.addCell(l);
		} catch (RowsExceededException e) {
			JOptionPane
					.showMessageDialog(
							null,
							"The excel file has exceeded its bounds.\nPlease contact your administrator",
							"Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		} catch (WriteException e) {
			JOptionPane
					.showMessageDialog(
							null,
							"The excel file could not be written to.\nPlease contact your administrator",
							"Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
			e.printStackTrace();
		}
	}

	/**
	 * Add a heading (this is the same as addString just with size column)
	 * 
	 * @param sheet
	 *            sheet
	 * @param c
	 *            column
	 * @param r
	 *            row
	 * @param string
	 *            string
	 * @param size
	 *            size
	 */
	private void addHeading(WritableSheet sheet, int c, int r, String string,
			int size, Colour col, Alignment align, String border) {
		try {
			Label l = new Label(c, r, string, getCellFormat(col, align, false,
					border));

			CellView cc = sheet.getColumnView(c);
			cc.setSize(size);
			sheet.setColumnView(c, cc);

			sheet.addCell(l);
		} catch (RowsExceededException e) {
			JOptionPane
					.showMessageDialog(
							null,
							"The excel file has exceeded its bounds.\nPlease contact your administrator",
							"Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
			e.printStackTrace();
		} catch (WriteException e) {
			JOptionPane
					.showMessageDialog(
							null,
							"The excel file could not be written to.\nPlease contact your administrator",
							"Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
			e.printStackTrace();
		}
	}
	
	// ///////////////////////// READING
	// //////////////////////////////////////////////////////////////////////////////////
	int supplier = 0;
	int branch = 2;
	int unconf = 8;
	int conf = 9;
	int dueData = 2;

	public void readDocument(String document) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(document));

			String line = "";
			
			ArrayList<String []> list = new ArrayList<>();

			while ((line = reader.readLine()) != null) {
				if (line.matches("H.*")) {
					String split[] = line.split(" {2,}");
					
					while (split[branch].contains(" ")){						
						split[branch] = split[branch].substring(split[branch].indexOf(' ') + 1);
					}
					
					if (!split[branch].matches("CPT|DBN|MRM|DIS|JHB|BRA")){
//						System.out.println(line);
						split[branch] = split[branch+1];
						split[unconf] = split[unconf+1];
						split[conf] = split[conf+1];
						
					}
					
					split[supplier] = split[supplier].substring(split[supplier].indexOf(' ') + 1);
					split[unconf] = split[unconf].replaceAll(",", "");
					split[conf] = split[conf].replaceAll(",", "");
					
					list.add(split.clone());
				} else if (line.toLowerCase().matches(".*  due date  .*")) {
					String split[] = line.split(" {2,}");
					
					Date date = new SimpleDateFormat("dd/MM/yy").parse(split[dueData]);
					
//					System.out.println(date + " -- " + split[dueData]);
					
					for (String[] S : list){
						addData(S, date);
					}
					list = new ArrayList<>();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} //catch (ParseException e) {
//			e.printStackTrace();
//		}
		catch (Exception e){
			JOptionPane.showMessageDialog(null,
					"An error occured", "Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public void addData(String [] s, Date date){
		String branchName = s[branch];
		String name = s[supplier];
		Double unconfirmed = Double.parseDouble(s[unconf]);
		Double Confirmed = Double.parseDouble(s[conf]);
		
		totalData.adddata(name, date, unconfirmed, Confirmed);
		
		
		if (!branchSupliers.containsKey(branchName)){
			branchSupliers.put(branchName, new dataHolder());
		}
		else {
			branchSupliers.get(branchName).adddata(name, date, unconfirmed, Confirmed);
		}
	}
	
}
