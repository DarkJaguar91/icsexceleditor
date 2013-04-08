package ICSJournal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import jxl.CellView;
import jxl.DateCell;
import jxl.NumberCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class Journal {

	// required globals
	WritableWorkbook workbook;
	ArrayList<data> dbn;
	ArrayList<ICSDate> dbnWeek;
	ArrayList<data> cpt;
	ArrayList<ICSDate> cptWeek;

	// formatting stuff

	/**
	 * Constructor ( runs the entire algorithm)
	 */
	public Journal() {
		try {
			// create the reading book
			Workbook reader = Workbook.getWorkbook(new File("inp.xls"));

			int sheetNum = -1, rowNum = -1;

			// get sheet number and row number
			int[] d = checkValidity(reader);

			if (d != null) {
				sheetNum = d[0];
				rowNum = d[1];
			} else {
				System.out.println("Failed");
				System.exit(1);
			}

			// get column numbers
			d = getColNums(reader, sheetNum, rowNum);

			// read in the data
			readData(reader, sheetNum, rowNum, d);

			// write new workbook
			writeWorkbook(reader, sheetNum);

			// close workbooks
			// reader.close(); moved to writeWorkbook

			System.out.println("finished");

		} catch (BiffException | IOException e) {
			System.out.println("Fail");
			e.printStackTrace();
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
		addString(sheet, 0, 1, "Commodity");

		boolean writenWeekHeadings = false;

		// data loop 1
		for (int i = 0; i < data.size(); ++i) {
			// add commodity name
			addString(sheet, 0, i + 2, data.get(i).commodity);

			// write weekly data
			for (int x = 0; x < weeks.size(); ++x) {

				// add headings for columns if not already done
				if (!writenWeekHeadings) {
					addString(sheet, 1 + x * 2, 0, "Week: " + weeks.get(x).week);
					addString(sheet, 1 + x * 2, 1, "Value");
					addString(sheet, 2 + x * 2, 1, "Quantity");
				}

				addNumber(sheet, 1+x*2, 2 + i, data.get(i).data[weeks.get(x).week].value);
				addNumber(sheet, 2+x*2, 2 + i, data.get(i).data[weeks.get(x).week].qty);
				
			}
			writenWeekHeadings = true; // run weeks once, so written the headings
		}

		// autosize every column
		for (int i = 0; i < sheet.getColumns(); ++i) {
			CellView cell = sheet.getColumnView(i);
			cell.setAutosize(true);
			sheet.setColumnView(i, cell);
		}
	}

	/**
	 * Add number to the sheet
	 * @param sheet sheet to write to
	 * @param c column
	 * @param r row
	 * @param number number to add
	 */
	private void addNumber(WritableSheet sheet, int c, int r, double number) {
		try {
			Number n = new Number(c, r, number);
			
			sheet.addCell(n);
		} catch (RowsExceededException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
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
	private void addString(WritableSheet sheet, int c, int r, String string) {
		try {
			Label l = new Label(c, r, string);

			// formatting here

			sheet.addCell(l);
		} catch (RowsExceededException e) {
			e.printStackTrace();
		} catch (WriteException e) {
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
	private void writeWorkbook(Workbook old, int sheetNumber) {
		try {
			// open workbook
			WritableWorkbook wrkbk = Workbook.createWorkbook(new File(
					"test.xls"), old);

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
			e.printStackTrace();
		} catch (WriteException e) {
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
		DateCell ddd = (DateCell)s.getCell(c,r);
		
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
			if (test.contains("e.t.d"))
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
