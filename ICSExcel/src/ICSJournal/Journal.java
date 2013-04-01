package ICSJournal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.crypto.Data;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.WritableWorkbook;

public class Journal {

	WritableWorkbook workbook;
	ArrayList<data> dbn;
	ArrayList<Integer> dbnWeek;
	ArrayList<data> cpt;
	ArrayList<Integer> cptWeek;

	public Journal() {
		try {
			Workbook reader = Workbook.getWorkbook(new File("inp.xls"));

			int sheetNum = -1, rowNum = -1;

			int[] d = checkValidity(reader);

			if (d != null) {
				sheetNum = d[0];
				rowNum = d[1];
			}

			d = getColNums(reader, sheetNum, rowNum);

			readData(reader, sheetNum, rowNum, d);

			// close workbooks
			reader.close();
		} catch (BiffException | IOException e) {
			System.out.println("Fail");
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

		int destCol, extRateCol, valueCol, priceCol, dateCol, descCol, qtyCol;
		destCol = d[0];
		extRateCol = d[1];
		valueCol = d[2];
		priceCol = d[3];
		dateCol = d[4];
		descCol = d[5];
		qtyCol = d[6];

		Sheet sheet = w.getSheet(sheetNum);

		for (int r = row + 1; r < sheet.getRows(); ++r) {
			String description = sheet.getCell(descCol, r).getContents();
			Date date = getDate(dateCol, r, sheet);
			String dest = sheet.getCell(destCol, r).getContents().toLowerCase();

			if (dest.contains("dbn")) {
				
			} else {

			}

		}
	}

	@SuppressWarnings("deprecation")
	private void addStuff(ArrayList<data> list, ArrayList<Integer> weeks, String desc, Date date) {
		data dta = new data(desc);

		GregorianCalendar greg = new GregorianCalendar(date.getYear(),
				date.getMonth(), date.getDay());

		if (list.contains(dta)) {
			dta = list.get(list.indexOf(dta));
		} else {
			list.add(dta);
		}
		
		dta.add(0, 0, greg.get(GregorianCalendar.WEEK_OF_YEAR));
	}

	private float getNumber(int c, int row, Sheet s) {
		return Float.parseFloat(s.getCell(c, row).getContents());
	}

	private Date getDate(int c, int r, Sheet s) {
		String tmp = s.getCell(c, r).getContents();

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
				out[6] = c;
			if (test.contains("descr"))
				out[5] = c;
			if (test.contains("e.t.d"))
				if (s.getCell(c, rowNum - 1).getContents().toLowerCase()
						.equals("revised"))
					out[4] = c;
			if (test.contains("price"))
				out[3] = c;
			if (test.contains("value"))
				out[2] = c;
			if (test.contains("rate"))
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
