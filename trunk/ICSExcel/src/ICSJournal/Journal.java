package ICSJournal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

import jxl.NumberCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class Journal {

	// required globals
	WritableWorkbook workbook;
	ArrayList<data> dbn;
	ArrayList<Integer> dbnWeek;
	ArrayList<data> cpt;
	ArrayList<Integer> cptWeek;

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
			reader.close();
			
			System.out.println("finished");
			
		} catch (BiffException | IOException e) {
			System.out.println("Fail");
			e.printStackTrace();
		}
	}

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
			wrkbk.createSheet("Cape Town", 0);
			wrkbk.createSheet("Durban", 0);
			
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

	@SuppressWarnings("deprecation")
	private void addStuff(ArrayList<data> list, ArrayList<Integer> weeks,
			String desc, Date date, float price, float qty) {
		data dta = new data(desc);

		GregorianCalendar greg = new GregorianCalendar(date.getYear(),
				date.getMonth(), date.getDay());

		if (list.contains(dta)) {
			dta = list.get(list.indexOf(dta));
		} else {
			list.add(dta);
		}

		dta.add(price, qty, greg.get(GregorianCalendar.WEEK_OF_YEAR));
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
		String tmp = s.getCell(c, r).getContents();

		if (tmp.equals(""))
			return null;

		int one = Integer.parseInt(tmp.substring(0, tmp.indexOf("/")));
		tmp = tmp.substring(tmp.indexOf("/") + 1);
		int two = Integer.parseInt(tmp.substring(0, tmp.indexOf("/")));
		tmp = tmp.substring(tmp.indexOf("/") + 1);
		int three = Integer.parseInt(tmp);

		@SuppressWarnings("deprecation")
		Date d = new Date(one, two, three);

		return d;
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
