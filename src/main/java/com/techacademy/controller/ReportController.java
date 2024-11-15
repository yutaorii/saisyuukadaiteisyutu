package com.techacademy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.constants.ErrorMessage;
import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.service.ReportService;
import com.techacademy.service.EmployeeService;  // 追加
import com.techacademy.service.UserDetail;

@Controller
@RequestMapping("reports")
public class ReportController {

    private final ReportService reportService;
    private final EmployeeService employeeService;  // 追加

    @Autowired
    public ReportController(ReportService reportService, EmployeeService employeeService) {  // 修正
        this.reportService = reportService;
        this.employeeService = employeeService;  // 追加
    }

    // 日報一覧画面
    @GetMapping
    public String list(Model model) {
        model.addAttribute("listSize", reportService.findAllReports().size());
        model.addAttribute("reportList", reportService.findAllReports());
        return "reports/list";  // 日報一覧のビュー
    }

    // 日報詳細画面
    @GetMapping(value = "/{id}/details")
    public String detail(@PathVariable Integer id, @AuthenticationPrincipal UserDetail userDetail, Model model) {
        if (userDetail == null) {
            return "redirect:/login";
        }

        Report report = reportService.findById(id);
        if (report == null) {
            return "redirect:/reports";
        }

        model.addAttribute("report", report);
        return "reports/detail";  // 詳細画面表示
    }

    // 日報新規登録画面表示
    @GetMapping("/add")
    public String addForm(Model model, @AuthenticationPrincipal UserDetail userDetail) {
        Report report = new Report();

        report.setEmployee(userDetail.getEmployee());

        model.addAttribute("report", report);
        return "reports/new";  // 新規登録フォームを表示
    }

    // 日報新規登録処理
    @PostMapping(value = "/add")
    public String add(@AuthenticationPrincipal UserDetail userDetail, @Validated Report report, BindingResult bindingResult, Model model) {

        // バリデーションエラーがあれば、登録画面に戻す
        if (bindingResult.hasErrors()) {
            return "reports/new";  // 新規登録画面に戻る
        }

        // 日付重複チェック
        if (reportService.isDateDuplicate(userDetail.getEmployee().getCode(), report.getReportDate(), null)) {
            model.addAttribute("reportDateDuplicate", true);
            return "reports/new";  // 重複エラーがあれば新規登録画面に戻る
        }

        // 新規登録処理
        report.setEmployee(userDetail.getEmployee());
        ErrorKinds result = reportService.createReport(report, userDetail.getEmployee().getCode());
        if (result != ErrorKinds.SUCCESS) {
            String errorName = ErrorMessage.getErrorName(result); // エラーメッセージの名前を取得
            String errorValue = ErrorMessage.getErrorValue(result); // エラーメッセージの値を取得
            model.addAttribute(errorName, errorValue);  // エラーメッセージを渡す
            return "reports/new";  // 新規登録失敗時、新規登録画面に戻る
        }

        return "redirect:/reports";  // 登録成功後は日報一覧に遷移
    }

    // 日報更新画面表示
    @GetMapping("/{id}/update")
    public String updateForm(@PathVariable Integer id, @AuthenticationPrincipal UserDetail userDetail, Model model) {
        if (userDetail == null) {
            return "redirect:/login";
        }

        Report report = reportService.findById(id);
        if (report == null) {
            return "redirect:/reports";  // レポートが見つからない場合は一覧画面にリダイレクト
        }

        model.addAttribute("report", report);
        return "reports/update";  // 更新画面を表示
    }

 // 従業員更新処理
    @PostMapping(value = "/{id}/update")
    public String update(@PathVariable Integer id, @Validated Report reports, BindingResult bindingResult, Model model, @AuthenticationPrincipal UserDetail userDetail) {
        System.out.println("日報更新処理");
        // 入力チェック（エラーがあれば更新画面に戻す）
        if (bindingResult.hasErrors()) {
            System.out.println("入力エラー");
            model.addAttribute("report", reports);  // エラー時に入力内容を保持
            return "reports/Update";  // 従業員更新画面に戻る
        }

        // パスワードが空でも、名前や役職など他の情報が更新される
        // 更新処理
        reports.setEmployee(userDetail.getEmployee());
        ErrorKinds result = reportService.update(id,reports);

        // エラーメッセージの処理（更新時のエラーをチェック）
        if (result != ErrorKinds.SUCCESS) {  // 成功でない場合にエラーメッセージを処理
            String errorName = ErrorMessage.getErrorName(result); // エラーメッセージの名前を取得
            String errorValue = ErrorMessage.getErrorValue(result); // エラーメッセージの値を取得
            model.addAttribute(errorName, errorValue);  // エラーメッセージを渡す

            return "reports/Update";  // エラーがあれば更新画面に戻る
        }

        // 成功メッセージを追加（任意）
        model.addAttribute("successMessage", "従業員情報が正常に更新されました。");

        // 更新後、日報一覧ページ（list.html）に遷移
        return "redirect:/reports";  // 修正箇所: 従業員更新後は日報一覧ページに遷移
    }

    // 日報削除処理
    @PostMapping(value = "/{id}/delete")
    public String delete(@PathVariable Integer id, @AuthenticationPrincipal UserDetail userDetail, Model model) {
        if (userDetail == null) {
            return "redirect:/login";
        }

        // 削除処理
        ErrorKinds result = reportService.deleteReport(id);
        if (result != ErrorKinds.SUCCESS) {
            model.addAttribute("errorMessage", ErrorMessage.getErrorValue(result));
            return "redirect:/reports";  // 削除失敗時も日報一覧に遷移
        }

        return "redirect:/reports";  // 削除後は日報一覧画面に遷移
    }
}
