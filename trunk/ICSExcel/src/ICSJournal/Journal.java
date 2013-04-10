package ICSJournal;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import jxl.CellView;
import jxl.DateCell;
import jxl.NumberCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Colour;
import jxl.read.biff.BiffException;
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
public class Journal {

	// required globals
	WritableWorkbook workbook;
	ArrayList<data> dbn;
	ArrayList<ICSDate> dbnWeek;
	ArrayList<data> cpt;
	ArrayList<ICSDate> cptWeek;

	// formats
	private static NumberFormat numberFormat = new NumberFormat(
			"#,##0;[RED]#,##0;-;\"no Text\"", NumberFormat.COMPLEX_FORMAT);
	private static final String[] months = { "January", "February", "March",
			"April", "May", "June", "July", "August", "September", "October",
			"November", "December" };

	/**
	 * Constructor ( runs the entire algorithm)
	 */
	public Journal() {
		try {

			JFileChooser chooser = new JFileChooser();

			int ans = chooser.showOpenDialog(null);

			if (ans == JFileChooser.CANCEL_OPTION)
				System.exit(0);

			File chosenFile = chooser.getSelectedFile();

			// create the reading book
			Workbook reader = Workbook.getWorkbook(chosenFile);

			int sheetNum = -1, rowNum = -1;

			// get sheet number and row number
			int[] d = checkValidity(reader);

			if (d != null) {
				sheetNum = d[0];
				rowNum = d[1];
			} else {
				JOptionPane.showMessageDialog(null,
						"The file chosen is not of the correct type.", "Error",
						JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}

			// get column numbers
			d = getColNums(reader, sheetNum, rowNum);

			// read in the data
			readData(reader, sheetNum, rowNum, d);

			// write new workbook
			writeWorkbook(reader, sheetNum, chosenFile);

			// close workbooks
			// reader.close(); moved to writeWorkbook

			Desktop.getDesktop().open(chosenFile);

		} catch (BiffException | IOException e) {
			JOptionPane.showMessageDialog(null,
					"The file specified does not exist", "Error",
					JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

	/**
	 * Writing the actual data to the sheets
	 * 
	 * @param wrkbk
	 *            The workbook to write to
	 * @param data
	 *            The data to write
	 * @param weeks
	 *            The week data for the data
	 * @param sheetnumber
	 *            The sheet number to write to
	 */
	private void writeData(WritableWorkbook wrkbk, ArrayList<data> data,
			ArrayList<ICSDate> weeks, int sheetnumber) {
		// get the sheet
		WritableSheet sheet = wrkbk.getSheet(sheetnumber);

		// sort the values
		Collections.sort(data);
		Collections.sort(weeks);

		// headings
		addHeading(sheet, 0, 1, "Commodity", 9000, null, Alignment.LEFT, "TBLR");
		addString(sheet, 0, 2 + data.size(), "Total", null, Alignment.LEFT,
				"TLBR");
		addString(sheet, 0, 3 + data.size(), "Containers", null,
				Alignment.LEFT, "TLBR");
		addHeading(sheet, 1 + 2 * weeks.size(), 0, "Total", 3000, null,
				Alignment.CENTRE, "TLR");
		addHeading(sheet, 1 + 2 * weeks.size(), 1, "Value", 3000,
				Colour.GREY_50_PERCENT, Alignment.CENTRE, "BL");
		addHeading(sheet, 2 + 2 * weeks.size(), 1, "Quantity", 3000, null,
				Alignment.CENTRE, "BRT");

		// required data
		valqty[] wtot = new valqty[weeks.size()];
		for (int i = 0; i < weeks.size(); ++i)
			wtot[i] = new valqty();
		double valuebacklog = 0, valueforeward = 0;
		int containersbacklog = 0, containersforeward = 0;
		boolean writenWeekHeadings = false;
		ICSDate today = new ICSDate(new Date());

		// data loop 1
		for (int i = 0; i < data.size(); ++i) {
			// add commodity name
			addString(sheet, 0, i + 2, data.get(i).commodity, null,
					Alignment.LEFT, "LR");

			// add commodity total
			addNumber(sheet, 1 + 2 * weeks.size(), i + 2,
					data.get(i).total.value, Colour.GRAY_50, Alignment.CENTRE,
					i == data.size() - 1 ? "LB" : "L");
			addNumber(sheet, 2 + 2 * weeks.size(), i + 2,
					data.get(i).total.qty, null, Alignment.CENTRE,
					i == data.size() - 1 ? "RB" : "R");

			// write weekly data
			for (int x = 0; x < weeks.size(); ++x) {

				// add headings for columns if not already done
				if (!writenWeekHeadings) {
					addHeading(sheet, 1 + x * 2, 0, "Week: "
							+ weeks.get(x).week, 3000, null, Alignment.CENTRE,
							"TL");
					addHeading(sheet, 1 + x * 2, 1, "Value", 3000,
							Colour.GREY_50_PERCENT, Alignment.CENTRE, "BL");
					weeks.get(x).gc.set(GregorianCalendar.DAY_OF_MONTH, weeks.get(x).gc.get(GregorianCalendar.DAY_OF_MONTH) + (7 - weeks.get(x).gc.get(GregorianCalendar.DAY_OF_WEEK)));
					addHeading(sheet, 2 + x * 2, 0, weeks.get(x).gc.get(GregorianCalendar.DAY_OF_MONTH) + " "+ months[weeks.get(x).gc.get(GregorianCalendar.MONTH)], 3000, null, Alignment.CENTRE,
							"TR");
					addHeading(
							sheet,
							2 + x * 2,
							1,
							"Quantity",
							3000,
							today.compareTo(weeks.get(x)) > 0 ? Colour.RED
									: today.compareTo(weeks.get(x)) == 0 ? Colour.YELLOW
											: Colour.WHITE, Alignment.CENTRE,
							"BR");

					data.get(0);
				}

				// data addition
				addNumber(sheet, 1 + x * 2, 2 + i,
						data.get(i).data[weeks.get(x).week].value,
						Colour.GREY_50_PERCENT, Alignment.CENTRE, "l");
				addNumber(
						sheet,
						2 + x * 2,
						2 + i,
						data.get(i).data[weeks.get(x).week].qty,
						today.compareTo(weeks.get(x)) > 0 ? Colour.RED : today
								.compareTo(weeks.get(x)) == 0 ? Colour.YELLOW
								: Colour.WHITE, Alignment.CENTRE, "r");

				wtot[x].qty += data.get(i).data[weeks.get(x).week].qty;
				wtot[x].value += data.get(i).data[weeks.get(x).week].value;
			} // end of week loop
			writenWeekHeadings = true; // run weeks once, so written the
										// headings
		}// end of data loop

		// print out week totals
		for (int i = 0; i < wtot.length; ++i) {
			addNumber(sheet, 1 + i * 2, 2 + data.size(), wtot[i].value,
					Colour.GREY_50_PERCENT, Alignment.CENTRE, "TLB");
			addNumber(
					sheet,
					2 + i * 2,
					2 + data.size(),
					wtot[i].qty,
					today.compareTo(weeks.get(i)) > 0 ? Colour.RED : today
							.compareTo(weeks.get(i)) == 0 ? Colour.YELLOW
							: Colour.WHITE, Alignment.CENTRE, "TR");
			addNumber(
					sheet,
					2 + i * 2,
					3 + data.size(),
					Math.round(wtot[i].qty / 26000d),
					today.compareTo(weeks.get(i)) > 0 ? Colour.RED : today
							.compareTo(weeks.get(i)) == 0 ? Colour.YELLOW
							: Colour.WHITE, Alignment.CENTRE, "RLB");

			valuebacklog += today.compareTo(weeks.get(i)) > 0 ? wtot[i].value
					: 0;
			valueforeward += today.compareTo(weeks.get(i)) <= 0 ? wtot[i].value
					: 0;
			containersbacklog += today.compareTo(weeks.get(i)) > 0 ? Math
					.round(wtot[i].qty / 26000) : 0;
			containersforeward += today.compareTo(weeks.get(i)) <= 0 ? Math
					.round(wtot[i].qty / 26000) : 0;
		}

		// add last section (backlog and foreward)
		addString(sheet, 0, 5 + data.size(), "Container Value", null,
				Alignment.LEFT, "TLBR");
		addString(sheet, 1, 5 + data.size(), "Value (R)", null,
				Alignment.CENTRE, "TLBR");
		addString(sheet, 2, 5 + data.size(), "Containers", null,
				Alignment.CENTRE, "TLBR");
		addString(sheet, 0, 6 + data.size(), "Backlog", null, Alignment.LEFT,
				"TLB");
		addString(sheet, 0, 7 + data.size(), "Forward", null, Alignment.LEFT,
				"TLB");
		addNumber(sheet, 1, 6 + data.size(), valuebacklog, null,
				Alignment.CENTRE, "TB");
		addNumber(sheet, 2, 6 + data.size(), containersbacklog, null,
				Alignment.CENTRE, "TBR");
		addNumber(sheet, 1, 7 + data.size(), valueforeward, null,
				Alignment.CENTRE, "TB");
		addNumber(sheet, 2, 7 + data.size(), containersforeward, null,
				Alignment.CENTRE, "TBR");

		// add the freeze pane

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

	/**
	 * The main writing process
	 * 
	 * @param old
	 *            The readable workbook to copy from
	 * @param sheetNumber
	 *            the sheet number of the data sheet
	 */
	private void writeWorkbook(Workbook old, int sheetNumber, File chosenFile) {
		try {
			// open workbook
			WritableWorkbook wrkbk = Workbook.createWorkbook(chosenFile, old);

			// delete all sheets but the data
			for (int i = 0; i <= wrkbk.getNumberOfSheets(); ++i)
				if (i != sheetNumber)
					wrkbk.removeSheet(i);

			// set data sheet name to data
			wrkbk.getSheet(0).setName("Data");

			// create new sheets
			wrkbk.createSheet("Report CPT", 0);
			wrkbk.createSheet("Report DBN", 0);

			// write the dbn and cpt data
			writeData(wrkbk, dbn, dbnWeek, 0);
			writeData(wrkbk, cpt, cptWeek, 1);

			// close reader workbook for incase its using the same file
			old.close();
			// write out workbook
			wrkbk.write();
			// close workbook
			wrkbk.close();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,
					"The excel file could not be read from or does not exist",
					"Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
			e.printStackTrace();
		} catch (WriteException e) {
			JOptionPane
					.showMessageDialog(
							null,
							"The file could not be writen into the specified directory.\nPlease contact your administrator",
							"Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
			e.printStackTrace();
		}
	}

	/**
	 * Reads in the needed data
	 * 
	 * @param w
	 *            The workbook
	 * @param sheetNum
	 *            the sheet number
	 * @param row
	 *            the row of headings
	 * @param d
	 *            the array containing the column numbers
	 */
	private void readData(Workbook w, int sheetNum, int row, int[] d) {
		dbn = new ArrayList<>();
		cpt = new ArrayList<>();
		dbnWeek = new ArrayList<>();
		cptWeek = new ArrayList<>();

		int destCol, priceCol, dateCol, descCol, qtyCol;
		destCol = d[0];
		priceCol = d[1];
		dateCol = d[2];
		descCol = d[3];
		qtyCol = d[4];

		Sheet sheet = w.getSheet(sheetNum);

		for (int r = row + 1; r < sheet.getRows(); ++r) {
			String description = sheet.getCell(descCol, r).getContents();

			// if there is no data in row break
			if (description.equals(""))
				break;

			Date date = getDate(dateCol, r, sheet);
			String dest = sheet.getCell(destCol, r).getContents().toLowerCase();

			float price = getNumber(priceCol, r, sheet);
			float qty = getNumber(qtyCol, r, sheet);

			if (date == null)
				break;

			if (dest.contains("dbn")) {
				addStuff(dbn, dbnWeek, description, date, price, qty);
			} else {
				addStuff(cpt, cptWeek, description, date, price, qty);
			}
		}
	}

	private void addStuff(ArrayList<data> list, ArrayList<ICSDate> weeks,
			String desc, Date date, float price, float qty) {
		data dta = new data(desc);

		if (list.contains(dta)) {
			dta = list.get(list.indexOf(dta));
		} else {
			list.add(dta);
		}

		ICSDate ics = new ICSDate(date);

		if (!weeks.contains(ics))
			weeks.add(ics);

		dta.add(price, qty, ics.week);
	}

	/**
	 * Gets a float value from the sheet
	 * 
	 * @param c
	 *            column number
	 * @param row
	 *            row number
	 * @param s
	 *            sheet to use
	 * @return float value in cell
	 */
	private float getNumber(int c, int row, Sheet s) {
		NumberCell cell = (NumberCell) s.getCell(c, row);
		return (float) cell.getValue();
	}

	/**
	 * Returns data in cell
	 * 
	 * @param c
	 *            column
	 * @param r
	 *            row
	 * @param s
	 *            sheet
	 * @return Date
	 */
	private Date getDate(int c, int r, Sheet s) {
		DateCell ddd = (DateCell) s.getCell(c, r);
		return ddd.getDate();
	}

	/**
	 * This method gets the column numbers for each type of data needed
	 * 
	 * @param w
	 *            The workbook to read from
	 * @param sheetNum
	 *            The sheet number to read from
	 * @param rowNum
	 *            The row number for the data
	 * @return an array for the data (destCol, extRateCol, valueCol, priceCol,
	 *         dateCol, descCol, qtyCol)
	 */
	private int[] getColNums(Workbook w, int sheetNum, int rowNum) {
		int out[] = new int[7];

		Sheet s = w.getSheet(sheetNum);

		for (int c = 0; c < s.getColumns(); ++c) {
			String test = s.getCell(c, rowNum).getContents().toLowerCase();

			if (test.contains("qty"))
				out[4] = c;
			if (test.contains("descr"))
				out[3] = c;
			if (test.contains("e.t.a"))
				if (s.getCell(c, rowNum - 1).getContents().toLowerCase()
						.equals("revised"))
					out[2] = c;
			if (test.contains("price"))
				out[1] = c;
			if (test.contains("dest"))
				out[0] = c;
		}

		return out;
	}

	/**
	 * This method checks to see if the current workbook is of the correct type
	 * 
	 * @param w
	 *            The workbook to read from
	 * @return null - if not correct <> array if correct (sheet, row) - of COP
	 */
	private int[] checkValidity(Workbook w) {
		if (w.getSheet(0).getName().toLowerCase().equals("report dbn")
				&& w.getSheet(1).getName().toLowerCase().equals("report cpt")) {
			JOptionPane.showMessageDialog(null,
					"The file chosen has already been converted.", "Error",
					JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}

		for (int i = 0; i < w.getNumberOfSheets(); ++i) {
			Sheet s = w.getSheet(i);
			for (int r = 0; r < s.getRows(); ++r) {
				for (int c = 0; c < s.getColumns(); ++c) {
					String test = s.getCell(c, r).getContents().toLowerCase();
					if (test.equals("cop")) {
						int[] out = new int[3];
						out[0] = i;
						out[1] = r;
						s = null;
						return out;
					}
				}
			}
		}
		return null;
	}
}
