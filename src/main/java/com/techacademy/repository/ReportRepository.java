package com.techacademy.repository;

import com.techacademy.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Integer> {

    // 同じ日付のレポートが存在するかチェック（削除フラグが立っていない）
    @Query("SELECT r FROM Report r WHERE r.reportDate = :reportDate AND r.deleteFlg = false")
    Optional<Report> findByReportDate(LocalDate reportDate);

    // 同じ日付の重複を除外してチェックする（指定したIDを除外してチェック）
    @Query("SELECT r FROM Report r WHERE r.reportDate = :reportDate AND r.id != :excludeId AND r.deleteFlg = false")
    Optional<Report> findDuplicateReport(LocalDate reportDate, Integer excludeId);

    // 日報IDで日報を取得する（削除フラグが立っていないもの）
    Optional<Report> findByIdAndDeleteFlgFalse(Integer id);

    // 日報IDで日報を物理削除する
    void deleteById(Integer id);  // これで物理削除が可能

    List<Report> findByEmployee_CodeAndReportDate(String employeeCode, LocalDate reportDate);
}
