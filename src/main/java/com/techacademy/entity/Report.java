package com.techacademy.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "reports")
public class Report {

    // ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 日付
    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    // タイトル
    @Column(name = "title", length = 100, nullable = false)
    private String title;

    // 内容
    @Column(name = "content", columnDefinition = "LONGTEXT", nullable = false)
    private String content;

    // 社員情報（外部キー）
    @ManyToOne
    @JoinColumn(name = "employee_code", referencedColumnName = "code", nullable = false)
    private Employee employee;

    // 削除フラグ
    @Column(name = "delete_flg", nullable = false)
    private boolean deleteFlg;

    // 登録日時
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 更新日時
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 新規レコード挿入前に createdAt と updatedAt を設定（自動）
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;  // 新規作成時に createdAt を現在時刻で設定
        this.updatedAt = now;  // 新規作成時に updatedAt を現在時刻で設定
    }

    // 更新前に updatedAt を設定（自動）
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();  // 更新時に updatedAt を現在時刻で設定
    }
}
