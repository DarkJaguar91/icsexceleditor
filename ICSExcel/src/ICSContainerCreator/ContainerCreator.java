package ICSContainerCreator;

import GUI.progressgui.ProgressFrame;
import ICSContainerCreator.objects.ContainerMapper;
import ICSContainerCreator.utils.DateUtils;
import ICSContainerCreator.utils.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;

import javax.swing.*;
import java.io.*;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContainerCreator implements Runnable {
    ContainerMapper mapper;
    ProgressFrame progressFrame;
    Map<String, List<Integer>> weekMap;

    public ContainerCreator(ProgressFrame progressFrame) {
        mapper = new ContainerMapper();
        this.progressFrame = progressFrame;
    }

    @Override
    public void run() {
        JFileChooser fileChooser = new JFileChooser();

        int answer = fileChooser.showOpenDialog(null);

        if (answer == JFileChooser.APPROVE_OPTION) {
            collectContainerDetails(fileChooser.getSelectedFile());

            writeContainerFile(fileChooser);
        }
    }

    private void writeContainerFile(JFileChooser fileChooser) {
        Workbook workbook = new HSSFWorkbook();

        //region styles
        CellStyle productHeaderStyle = workbook.createCellStyle();
        productHeaderStyle.setBorderTop(CellStyle.BORDER_MEDIUM);
        productHeaderStyle.setBorderRight(CellStyle.BORDER_MEDIUM);
        productHeaderStyle.setBorderLeft(CellStyle.BORDER_MEDIUM);
        productHeaderStyle.setBorderBottom(CellStyle.BORDER_MEDIUM);

        CellStyle weekHeaderStyle = workbook.createCellStyle();
        weekHeaderStyle.setBorderTop(CellStyle.BORDER_MEDIUM);
        weekHeaderStyle.setBorderRight(CellStyle.BORDER_MEDIUM);
        weekHeaderStyle.setBorderLeft(CellStyle.BORDER_MEDIUM);
        weekHeaderStyle.setBorderBottom(CellStyle.BORDER_MEDIUM);
        weekHeaderStyle.setAlignment(CellStyle.ALIGN_CENTER);

        CellStyle totalsStyle = workbook.createCellStyle();
        totalsStyle.setBorderTop(CellStyle.BORDER_MEDIUM);
        totalsStyle.setBorderRight(CellStyle.BORDER_MEDIUM);
        totalsStyle.setBorderLeft(CellStyle.BORDER_MEDIUM);
        totalsStyle.setBorderBottom(CellStyle.BORDER_MEDIUM);
        totalsStyle.setAlignment(CellStyle.ALIGN_CENTER);
        totalsStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));

        CellStyle weekBehindStyle = workbook.createCellStyle();
        weekBehindStyle.setBorderTop(CellStyle.BORDER_THIN);
        weekBehindStyle.setBorderRight(CellStyle.BORDER_THIN);
        weekBehindStyle.setBorderLeft(CellStyle.BORDER_THIN);
        weekBehindStyle.setBorderBottom(CellStyle.BORDER_THIN);
        weekBehindStyle.setAlignment(CellStyle.ALIGN_CENTER);
        weekBehindStyle.setFillForegroundColor(HSSFColor.RED.index);
        weekBehindStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        weekBehindStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));

        CellStyle weekCurrentStyle = workbook.createCellStyle();
        weekCurrentStyle.setBorderTop(CellStyle.BORDER_THIN);
        weekCurrentStyle.setBorderRight(CellStyle.BORDER_THIN);
        weekCurrentStyle.setBorderLeft(CellStyle.BORDER_THIN);
        weekCurrentStyle.setBorderBottom(CellStyle.BORDER_THIN);
        weekCurrentStyle.setAlignment(CellStyle.ALIGN_CENTER);
        weekCurrentStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        weekCurrentStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        weekCurrentStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));

        CellStyle weekAheadStyle = workbook.createCellStyle();
        weekAheadStyle.setBorderTop(CellStyle.BORDER_THIN);
        weekAheadStyle.setBorderRight(CellStyle.BORDER_THIN);
        weekAheadStyle.setBorderLeft(CellStyle.BORDER_THIN);
        weekAheadStyle.setBorderBottom(CellStyle.BORDER_THIN);
        weekAheadStyle.setAlignment(CellStyle.ALIGN_CENTER);
        weekAheadStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
        //endregion

        progressFrame.setProcess("Creating Workbook");

        Map<String, Map<String, Map<Integer, Double>>> containerMap = mapper.getContainerMap();

        for (String branch : containerMap.keySet()) {
            List<Integer> weekList = weekMap.get(branch);
            Collections.sort(weekList);

            Map<String, Map<Integer, Double>> productMap = containerMap.get(branch);
            Sheet sheet = workbook.createSheet(branch);

            Row rowHeader = sheet.createRow(0);

            Cell cell = rowHeader.createCell(0);
            cell.setCellValue("Product");
            cell.setCellStyle(productHeaderStyle);

            cell = rowHeader.createCell(1 + weekList.size());
            cell.setCellValue("Total Weight in KG");
            cell.setCellStyle(weekHeaderStyle);
            cell = rowHeader.createCell(2 + weekList.size());
            cell.setCellValue("Number Containers");
            cell.setCellStyle(weekHeaderStyle);

            int rowCnt = 0;
            int currentWeek = DateUtils.getWeek(new Date());
            for (String product : productMap.keySet()) {
                Map<Integer, Double> weekMap = productMap.get(product);
                int cellCnt = 0;
                Row row = sheet.createRow(++rowCnt);
                cell = row.createCell(0);

                cell.setCellValue(product);
                cell.setCellStyle(productHeaderStyle);

                for (int week : weekList) {
                    cell = rowHeader.createCell(++cellCnt);
                    int outputWeek = week % 52;
                    outputWeek = outputWeek < 0 ? 52 + outputWeek : outputWeek;
                    cell.setCellValue("Week: " + outputWeek + " - " + DateUtils.getDateFromWeek(week));
                    cell.setCellStyle(weekHeaderStyle);


                    cell = row.createCell(cellCnt);
                    // styles stuff
                    if (week < currentWeek) {
                        cell.setCellStyle(weekBehindStyle);
                    } else if (week == currentWeek) {
                        cell.setCellStyle(weekCurrentStyle);
                    } else {
                        cell.setCellStyle(weekAheadStyle);
                    }
                    if (weekMap.containsKey(week)) {
                        cell.setCellValue(weekMap.get(week));
                    }
                    sheet.autoSizeColumn(cellCnt);
                }
                cell = row.createCell(++cellCnt);
                CellReference ref = new CellReference(row.getRowNum() + 1, cellCnt - 1);
                cell.setCellFormula("SUM(B" + (row.getRowNum() + 1) + ":" + ref.getCellRefParts()[2] + (row.getRowNum() + 1) + ")");
                cell.setCellStyle(totalsStyle);
                sheet.autoSizeColumn(cellCnt);

                cell = row.createCell(++cellCnt);
                ref = new CellReference(row.getRowNum(), cellCnt - 1);
                cell.setCellFormula(ref.getCellRefParts()[2] + (row.getRowNum() + 1) + " / 25000");
                cell.setCellStyle(totalsStyle);
                sheet.autoSizeColumn(cellCnt);
            }
            sheet.autoSizeColumn(0);

            Row totalRow = sheet.createRow(++rowCnt);
            cell = totalRow.createCell(0);
            cell.setCellValue("Total Weight in KG");
            cell.setCellStyle(productHeaderStyle);

            Row containerRow = sheet.createRow(rowCnt + 1);
            cell = containerRow.createCell(0);
            cell.setCellValue("Number Containers");
            cell.setCellStyle(productHeaderStyle);

            for (int i = 0; i < weekList.size() + 2; ++i) {
                CellReference ref = new CellReference(rowCnt, i + 1);
                cell = totalRow.createCell(i + 1);
                cell.setCellFormula("SUM(" + ref.getCellRefParts()[2] + 2 + ":" + ref.getCellRefParts()[2] + rowCnt + ")");
                cell.setCellStyle(totalsStyle);

                if (i != weekList.size() + 1) {
                    cell = containerRow.createCell(i + 1);
                    cell.setCellFormula(ref.getCellRefParts()[2] + (rowCnt + 1) + " / 25000");
                    cell.setCellStyle(totalsStyle);
                }
            }
            sheet.createFreezePane(1, 1);
        }

        try {
            progressFrame.setProcess("Writing Excel file");
            FileOutputStream out =
                    new FileOutputStream(new File("test.xls"));
            workbook.write(out);
            out.close();
            System.out.println("Excel written successfully..");
            progressFrame.setDone();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void collectContainerDetails(File selectedFile) {
        try {
            ExecutorService service = Executors.newCachedThreadPool();
            weekMap = new HashMap<>();

            int numLines = FileUtils.countLines(selectedFile.getAbsolutePath());

            BufferedReader bufferedReader = new BufferedReader(new FileReader(selectedFile));

            progressFrame.setMax(numLines);
            progressFrame.setProcess("Reading");

            String line = null;
            Pattern pattern = Pattern.compile("^(?:H\\d+)?\\s*(?:P\\d+)?\\s*(?:Z\\S*)?\\s*(?:\\d+\\w*)?\\s*(?:.*?)(?:DBN|CPT|JHB)\\s*(DBN|CPT|JHB)\\s*(?:\\S*)?\\s*((?:\\d{2}\\/){2}\\d{2})\\s*((?:\\d{2}\\/){2}\\d{2})?\\s*(?:(?:\\d{2}\\/){2}\\d{2})\\s*(?:\\S+)\\s*(?:\\d+\\.?\\d*)\\s*(\\d+\\.?\\d*)\\s*(?:\\S*)\\s*(.*?)(?:$|\\s+\\*.*$|\\s\\s\\s.*$)");
            while ((line = bufferedReader.readLine()) != null) {
                final String finalLine = line;
                service.execute(() -> {
                    Matcher matcher = pattern.matcher(finalLine);

                    if (matcher.matches()) {
                        String branch = matcher.group(1);
                        String origDate = matcher.group(2);
                        String revisedDate = matcher.group(3);
                        String weight = matcher.group(4);
                        String product = matcher.group(5);

                        int week = getWeek(origDate, revisedDate);
                        synchronized (ContainerCreator.this) {
                            if (!weekMap.containsKey(branch)) {
                                weekMap.put(branch, new ArrayList<>());
                            }
                            List<Integer> weekList = weekMap.get(branch);
                            if (!weekList.contains(week)) {
                                weekList.add(week);
                            }
                        }

                        mapper.addContainer(product, branch, getWeight(weight), week);
                    }
                    progressFrame.increment();
                });
            }

            service.shutdown();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "The selected file does not exist.",
                    "Error: Cant find file.", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Could not read the selected file.\n" +
                            "Please make sure that you have acces to the file.",
                    "Error: Cannot read file", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int getWeek(String origDate, String revisedDate) {
        try {
            if (revisedDate == null) {
                return DateUtils.getWeek(DateUtils.fromString(origDate));
            }
            return DateUtils.getWeek(DateUtils.fromString(revisedDate));
        } catch (ParseException ignored) {
            throw new RuntimeException("Date Parse Error.\nPlease Contact your administrator for help.");
        }
    }

    private Double getWeight(String weight) {
        return Double.parseDouble(weight);
    }
}
