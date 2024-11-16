package com.techacademy.service;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.repository.ReportRepository;

import jakarta.transaction.Transactional;

import com.techacademy.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final EmployeeRepository employeeRepository;  // EmployeeRepositoryをインジェクト

    @Autowired
    public ReportService(ReportRepository reportRepository, EmployeeRepository employeeRepository) {
        this.reportRepository = reportRepository;
        this.employeeRepository = employeeRepository;  // コンストラクタでインジェクト
    }

    // 日報一覧表示処理
    public List<Report> findAllReports() {
        return reportRepository.findAll();
    }

    // 日報詳細表示処理
    public Report findById(Integer id) {
        Optional<Report> reportOptional = reportRepository.findById(id);
        return reportOptional.filter(report -> !report.isDeleteFlg()).orElse(null);
    }

 // 日報削除処理
    @Transactional
    public ErrorKinds deleteReport(Integer id) {
        Report report = findById(id);  // IDで日報を検索
        if (report != null) {
            // 日報を物理削除
            reportRepository.delete(report);  // 実際にレコードを削除
            return ErrorKinds.SUCCESS;  // 成功した場合は SUCCESS を返す
        }
        // 日報が見つからなかった場合に REPORT_NOT_FOUND を返す
        return ErrorKinds.BLANK_ERROR;  // 日報が見つからない場合にエラーを返す
    }

    // 日付重複チェック
    public boolean isDateDuplicate(String employeeCode, java.time.LocalDate reportDate, Integer excludeId) {
        Optional<Report> existingReport = reportRepository.findDuplicateReport(reportDate, excludeId);
        return existingReport.isPresent();  // 重複している場合は true を返す
    }

    // 従業員コードで従業員情報を取得する処理
    private Optional<Employee> getEmployeeByCode(String employeeCode) {
        return employeeRepository.findById(employeeCode);
    }

    // 日報新規登録処理
    public ErrorKinds createReport(Report report, String employeeCode) {
        if (report == null || report.getTitle().isBlank() || report.getContent().isBlank()) {
            return ErrorKinds.BLANK_ERROR;  // タイトルまたは内容が空白の場合
        }

        // 日付重複チェック
        List<Report> r = reportRepository.findByEmployee_CodeAndReportDate(employeeCode, report.getReportDate());
        if (r.size()>0) {
            return ErrorKinds.DATECHECK_ERROR;  // 従業員が見つからない場合
        }
        LocalDateTime now = LocalDateTime.now();
        report.setCreatedAt(now);
        report.setUpdatedAt(now);
        // 日報登録処理
        reportRepository.save(report);
        return ErrorKinds.SUCCESS;  // 登録成功
    }

 // 日報更新処理
    public ErrorKinds update(Integer id, Report report) {
        // 日付重複チェック
        List<Report> r = reportRepository.findByEmployee_CodeAndReportDate(report.getEmployee().getCode(), report.getReportDate());

        for (Report existingReport : r) {
            if (!existingReport.getId().equals(id)) {
            }
        }

        // 更新対象の日報が存在するか確認
        Optional<Report> existingReportOpt = reportRepository.findById(id);
        if (existingReportOpt.isPresent()) {
            Report existingReport = existingReportOpt.get();

            // 更新内容の反映
            existingReport.setTitle(report.getTitle());
            existingReport.setContent(report.getContent());
            existingReport.setReportDate(report.getReportDate());

            // 保存（更新）
            reportRepository.save(existingReport);
            return ErrorKinds.SUCCESS;  // 更新成功
        }

        // 更新対象のレポートが存在しない場合
        return ErrorKinds.DUPLICATE_EXCEPTION_ERROR;  // 更新対象の日報が見つからない
    }
}