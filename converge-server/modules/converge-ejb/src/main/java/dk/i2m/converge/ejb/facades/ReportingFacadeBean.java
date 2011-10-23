/*
 * Copyright (C) 2011 Interactive Media Management
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.i2m.converge.ejb.facades;

import dk.i2m.converge.core.content.NewsItem;
import dk.i2m.converge.core.reporting.activity.ActivityReport;
import dk.i2m.converge.core.reporting.activity.UserActivity;
import dk.i2m.converge.core.reporting.activity.UserActivitySummary;
import dk.i2m.converge.core.security.UserAccount;
import dk.i2m.converge.core.security.UserRole;
import dk.i2m.converge.core.workflow.WorkflowState;
import dk.i2m.converge.ejb.services.DaoServiceLocal;
import dk.i2m.converge.ejb.services.QueryBuilder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HeaderFooter;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Footer;
import org.apache.poi.ss.usermodel.Header;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.WorkbookUtil;

/**
 * Stateless Enterprise JavaBean implementing the reporting facade.
 *
 * @author Allan Lykke Christensen
 */
@Stateless
public class ReportingFacadeBean implements ReportingFacadeLocal {

    @EJB private DaoServiceLocal daoService;

    private static final Logger LOG = Logger.getLogger(ReportingFacadeBean.class.getName());

    /**
     * Generates an activity report for all user with a given {@link UserRole}.
     * 
     * @param start
     *          Start of activities
     * @param end
     *          End of activities
     * @param userRole
     *          {@link UserRole} for which to capture activities
     * @param state
     *          {@link WorkflowState} to treat as the submitted state
     * @return 
     */
    @Override
    // TODO: Remove - got replaced by generateActivityReport
    public ActivityReport generateActivityReport(Date start, Date end, UserRole userRole, WorkflowState state) {
        ActivityReport report = new ActivityReport(start, end, userRole);

        Map<String, Object> parameters = QueryBuilder.with("userRole", userRole).and("startDate", start).and("endDate", end).and("state", state).parameters();

        List<UserAccount> users = daoService.findWithNamedQuery(UserAccount.FIND_ACTIVE_USERS_BY_ROLE, parameters);

        for (UserAccount user : users) {
            UserActivity activity = new UserActivity();
            activity.setUser(user);
            Map<String, Object> param = QueryBuilder.with("userRole", userRole).and("startDate", start).and("endDate", end).and("user", user).and("state", state).parameters();
            List<NewsItem> items = daoService.findWithNamedQuery(NewsItem.FIND_BY_USER_USER_ROLE_AND_DATE, param);
            activity.setSubmitted(items);

            report.getUserActivity().add(activity);
        }

        return report;
    }
    
    @Override
    public ActivityReport generateActivityReport(Date start, Date end, UserRole userRole) {
        ActivityReport report = new ActivityReport(start, end, userRole);

        Map<String, Object> parameters = QueryBuilder.with("userRole", userRole).and("startDate", start).and("endDate", end).parameters();

        List<UserAccount> users = daoService.findWithNamedQuery(UserAccount.FIND_ACTIVE_USERS_BY_ROLE, parameters);

        for (UserAccount user : users) {
            report.getUserActivity().add(generateUserActivityReport(start, end, user));
        }

        return report;
    }

    /**
     * Generates the activity report for a single user.
     * 
     * @param start
     *          Start of activities
     * @param end
     *          End of activities
     * @param user
     *          {@link UserAccount} for which to retrieve the report
     * @return {@link UserActivity} for the given period and user
     */
    @Override
    public UserActivity generateUserActivityReport(Date start, Date end, UserAccount user) {
        Map<String, Object> param = QueryBuilder.with("startDate", start).and("endDate", end).and("user", user).parameters();
        List<NewsItem> items = daoService.findWithNamedQuery(NewsItem.FIND_SUBMITTED_BY_USER, param);

        UserActivity userActivity = new UserActivity();
        userActivity.setUser(user);
        userActivity.setSubmitted(items);
       
        return userActivity;
    }

    @Override
    public UserActivitySummary generateUserActivitySummary(Date start, Date end, UserAccount user) {
        UserActivitySummary summary = new UserActivitySummary(generateUserActivityReport(start, end, user));
        return summary;
    }

    @Override
    public byte[] convertToExcel(ActivityReport report) {
        SimpleDateFormat format = new SimpleDateFormat("d MMMM yyyy");
        HSSFWorkbook wb = new HSSFWorkbook();

        String sheetName = WorkbookUtil.createSafeSheetName("Overview");
        int overviewSheetRow = 0;
   
        Font headerFont = wb.createFont();
        headerFont.setFontHeightInPoints((short) 14);
        headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);

        Font userFont = wb.createFont();
        userFont.setFontHeightInPoints((short) 12);
        userFont.setBoldweight(Font.BOLDWEIGHT_BOLD);

        Font storyFont = wb.createFont();
        storyFont.setFontHeightInPoints((short) 12);
        storyFont.setBoldweight(Font.BOLDWEIGHT_NORMAL);

        CellStyle headerStyle = createHeaderStyle(wb, headerFont);
        CellStyle headerVerticalStyle = createHeaderVerticalStyle(wb, headerFont);
        CellStyle userStyle = createUserStyle(wb, userFont);
        CellStyle storyCenterStyle = createStoryCenterStyle(wb);
        CellStyle storyStyle = createStoryStyle(wb, storyFont);
        CellStyle percentStyle = createPercentStyle(wb, userFont);

        HSSFSheet overviewSheet = wb.createSheet(sheetName);
        Header sheetHeader = overviewSheet.getHeader();

        sheetHeader.setLeft("CONVERGE " + report.getUserRole().getName() + " Activity Sheet");
        sheetHeader.setRight(format.format(report.getStart()) + " - " + format.format(report.getEnd()));

        overviewSheet.createFreezePane(0, 1, 0, 1);

        Row row = overviewSheet.createRow(0);
        row.createCell(0).setCellValue("ID");
        row.createCell(1).setCellValue("Name / Title");
        row.createCell(2).setCellValue("Word Count");
        row.createCell(3).setCellValue("Submitted");
        row.createCell(4).setCellValue("With Media");
        row.createCell(5).setCellValue("Used");
        row.createCell(6).setCellValue("With Media");
        row.createCell(7).setCellValue("Used %");
        row.createCell(8).setCellValue("With Media %");

        for (int i = 0; i <= 8; i++) {
            if (i < 2) {
                row.getCell(i).setCellStyle(headerStyle);
            } else {
                row.getCell(i).setCellStyle(headerVerticalStyle);
            }
        }

        overviewSheetRow++;
        for (UserActivity userActivity : report.getUserActivity()) {
            row = overviewSheet.createRow(overviewSheetRow);
            row.createCell(0).setCellValue(userActivity.getUser().getUsername());
            row.createCell(1).setCellValue(userActivity.getUser().getFullName());
            row.createCell(2).setCellValue(userActivity.getTotalWordCount());
            row.createCell(3).setCellValue(userActivity.getNumberOfNewsItemsSubmitted());
            row.createCell(4).setCellValue(userActivity.getNumberOfNewsItemsSubmittedWithMediaItems());
            row.createCell(5).setCellValue(userActivity.getNumberOfNewsItemsUsed());
            row.createCell(6).setCellValue(userActivity.getNumberOfNewsItemsUsedWithMedia());
            row.createCell(7).setCellValue(userActivity.getUsage());
            row.createCell(8).setCellValue(userActivity.getUsageWithMedia());

            for (int i = 0; i <= 8; i++) {
                if (i == 7 || i == 8) {
                    row.getCell(i).setCellStyle(percentStyle);
                } else {
                    row.getCell(i).setCellStyle(userStyle);
                }
            }

            overviewSheetRow++;

            int startRow = overviewSheetRow;
            for (NewsItem submitted : userActivity.getSubmitted()) {
                row = overviewSheet.createRow(overviewSheetRow);
                row.createCell(0).setCellValue(submitted.getId());
                row.createCell(1).setCellValue(submitted.getTitle());
                row.createCell(2).setCellValue(submitted.getWordCount());
                row.createCell(3).setCellValue("X");
                if (userActivity.getSubmittedWithMedia().contains(submitted)) {
                    row.createCell(4).setCellValue("X");
                } else {
                    row.createCell(4).setCellValue("");
                }

                if (userActivity.getUsed().contains(submitted)) {
                    row.createCell(5).setCellValue("X");
                } else {
                    row.createCell(5).setCellValue("");
                }

                if (userActivity.getUsedWithMedia().contains(submitted)) {
                    row.createCell(6).setCellValue("X");
                } else {
                    row.createCell(6).setCellValue("");
                }

                for (int i = 0; i <= 6; i++) {
                    row.getCell(i).setCellStyle(storyStyle);
                    if (i == 3 || i == 4 || i == 5 || i == 6) {
                        row.getCell(i).setCellStyle(storyCenterStyle);
                    }
                }

                overviewSheetRow++;
            }
            int endRow = overviewSheetRow - 1;
            overviewSheet.groupRow(startRow, endRow);
            overviewSheet.setRowGroupCollapsed(startRow, true);
        }

        // Auto-size
        for (int i = 0; i <= 8; i++) {
            overviewSheet.autoSizeColumn(i);
        }

        wb.setRepeatingRowsAndColumns(0, 0, 0, 0, 0);
        wb.setPrintArea(0, 0, 8, 0, overviewSheetRow);
        //overviewSheet.setFitToPage(true);        
        overviewSheet.setAutobreaks(true);
        overviewSheet.getPrintSetup().setFitWidth((short) 1);
        overviewSheet.getPrintSetup().setFitHeight((short) 500);

        Footer footer = overviewSheet.getFooter();
        footer.setLeft("Page " + HeaderFooter.page() + " of " + HeaderFooter.numPages());
        footer.setRight("Generated on " + HeaderFooter.date() + " at " + HeaderFooter.time());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            wb.write(baos);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }

        return baos.toByteArray();
    }

    private CellStyle createUserStyle(HSSFWorkbook wb, Font userFont) {
        CellStyle style = wb.createCellStyle();
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderTop(CellStyle.BORDER_THIN);
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setFont(userFont);
        return style;
    }

    private CellStyle createStoryStyle(HSSFWorkbook wb, Font font) {
        CellStyle style = wb.createCellStyle();
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderTop(CellStyle.BORDER_THIN);
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
        style.setFont(font);
        return style;
    }

    private CellStyle createStoryCenterStyle(HSSFWorkbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderTop(CellStyle.BORDER_THIN);
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
        style.setAlignment(CellStyle.ALIGN_CENTER);
        return style;
    }

    private CellStyle createHeaderStyle(HSSFWorkbook wb, Font headerFont) {
        CellStyle header = wb.createCellStyle();
        header.setBorderBottom(CellStyle.BORDER_THIN);
        header.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        header.setBorderTop(CellStyle.BORDER_THIN);
        header.setTopBorderColor(IndexedColors.BLACK.getIndex());
        header.setBorderLeft(CellStyle.BORDER_THIN);
        header.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        header.setBorderRight(CellStyle.BORDER_THIN);
        header.setRightBorderColor(IndexedColors.BLACK.getIndex());
        header.setFont(headerFont);
        return header;
    }

    private CellStyle createHeaderVerticalStyle(HSSFWorkbook wb, Font headerFont) {
        CellStyle header = wb.createCellStyle();
        header.setBorderBottom(CellStyle.BORDER_THIN);
        header.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        header.setBorderTop(CellStyle.BORDER_THIN);
        header.setTopBorderColor(IndexedColors.BLACK.getIndex());
        header.setBorderLeft(CellStyle.BORDER_THIN);
        header.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        header.setBorderRight(CellStyle.BORDER_THIN);
        header.setRightBorderColor(IndexedColors.BLACK.getIndex());
        header.setRotation((short) 90);
        header.setFont(headerFont);
        return header;
    }

    private CellStyle createPercentStyle(HSSFWorkbook wb, Font font) {
        CellStyle style = wb.createCellStyle();
        CreationHelper creationHelper = wb.getCreationHelper();
        style.setDataFormat(creationHelper.createDataFormat().getFormat("0%"));
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderTop(CellStyle.BORDER_THIN);
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setFont(font);
        return style;
    }
}
