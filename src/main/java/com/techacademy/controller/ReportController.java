package com.techacademy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.constants.ErrorMessage;
import com.techacademy.entity.Report;
import com.techacademy.service.ReportService;
import com.techacademy.service.UserDetail;

@Controller
@RequestMapping("reports")
public class ReportController {

    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // 日報一覧画面
    @GetMapping
    public String list(Model model) {
        model.addAttribute("listSize", reportService.findAllReports().size());
        model.addAttribute("reportList", reportService.findAllReports());
        return "reports/list";
    }

    // 日報詳細画面
    @GetMapping(value = "/{id}/")
    public String detail(@PathVariable Integer id, @AuthenticationPrincipal UserDetail userDetail, Model model) {
        // ログインしていない場合はログイン画面にリダイレクト
        if (userDetail == null) {
            return "redirect:/login";
        }

        Report report = reportService.findById(id);
        if (report == null) {
            return "redirect:/reports";
        }

        model.addAttribute("report", report);
        return "reports/detail";
    }

 // 日報新規登録画面表示
    @GetMapping("/add")
    public String addForm(Model model) {
        // 新規日報の空のインスタンスを作成してビューに渡す
        model.addAttribute("report", new Report());
        return "reports/new";  // ここでは、"reports/new"というThymeleafテンプレートを表示
    }

    // 日報新規登録処理
    @PostMapping(value = "/add")
    public String add(@AuthenticationPrincipal UserDetail userDetail, Report report, BindingResult bindingResult, Model model) {
        // ログインしていない場合はログイン画面にリダイレクト
        if (userDetail == null) {
            return "redirect:/login";
        }

        // バリデーションチェック
        if (bindingResult.hasErrors()) {
            return "reports/detail";  // 入力エラーがあれば詳細画面に戻る
        }

        // 日付重複チェック
        if (reportService.isDateDuplicate(userDetail.getEmployee().getCode(), report.getReportDate(), null)) {
            model.addAttribute("reportDateDuplicate", true);
            return "reports/detail";  // 日付重複エラー
        }

        // 新規登録処理
        ErrorKinds result = reportService.createReport(report, userDetail.getEmployee().getCode());
        if (result != ErrorKinds.SUCCESS) {
            model.addAttribute("errorMessage", ErrorMessage.getErrorValue(result));
            return "reports/detail";  // 登録失敗時も詳細画面に戻る
        }

        return "redirect:/reports";  // 登録成功後は日報一覧に遷移
    }

    // 日報更新処理
    @PostMapping(value = "/{id}/update")
    public String update(@PathVariable Integer id, @AuthenticationPrincipal UserDetail userDetail, Report report, BindingResult bindingResult, Model model) {
        // ログインしていない場合はログイン画面にリダイレクト
        if (userDetail == null) {
            return "redirect:/login";
        }

        // バリデーションチェック
        if (bindingResult.hasErrors()) {
            return "reports/detail";
        }

        // 日付重複チェック
        if (reportService.isDateDuplicate(userDetail.getEmployee().getCode(), report.getReportDate(), id)) {
            model.addAttribute("reportDateDuplicate", true);  // 日付重複エラー
            return "reports/detail";
        }

        // 更新処理
        ErrorKinds result = reportService.updateReport(id, report);
        if (result != ErrorKinds.SUCCESS) {
            model.addAttribute("errorMessage", ErrorMessage.getErrorValue(result));
            return "reports/detail";  // 更新失敗時も詳細画面に戻る
        }

        return "redirect:/reports";  // 更新成功後は日報一覧に遷移
    }

    // 日報削除処理
    @PostMapping(value = "/{id}/delete")
    public String delete(@PathVariable Integer id, @AuthenticationPrincipal UserDetail userDetail, Model model) {
        // ログインしていない場合はログイン画面にリダイレクト
        if (userDetail == null) {
            return "redirect:/login";
        }

        // ErrorKinds型に変更
        ErrorKinds result = reportService.deleteReport(id);
        if (result != ErrorKinds.SUCCESS) {
            // 削除できなかった場合のエラーメッセージを設定
            model.addAttribute("errorMessage", ErrorMessage.getErrorValue(result));
            return "redirect:/reports";  // 削除失敗時も日報一覧に遷移
        }

        return "redirect:/reports";  // 削除後は日報一覧画面に遷移
    }
}
