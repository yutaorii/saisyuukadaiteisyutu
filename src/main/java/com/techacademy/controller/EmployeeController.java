package com.techacademy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.constants.ErrorMessage;
import com.techacademy.entity.Employee;
import com.techacademy.service.EmployeeService;
import com.techacademy.service.UserDetail;

@Controller
@RequestMapping("employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // 従業員一覧画面
    @GetMapping
    public String list(Model model) {
        model.addAttribute("listSize", employeeService.findAll().size());
        model.addAttribute("employeeList", employeeService.findAll());
        return "employees/list";
    }

    // 従業員詳細画面
    @GetMapping(value = "/{code}/")
    public String detail(@PathVariable String code, Model model) {
        model.addAttribute("employee", employeeService.findByCode(code));
        return "employees/detail";
    }

    // 従業員更新画面表示
    @GetMapping(value = "/{code}/edit")
    public String edit(@PathVariable String code, Model model) {
        Employee employee = employeeService.findByCode(code);
        employee.setPassword("");  // パスワードを空にする
        model.addAttribute("employee", employee);
        return "employees/Update";  // 更新画面へ遷移
    }

    // 従業員更新処理
    @PostMapping(value = "/{code}/update")
    public String update(@PathVariable String code, @Validated Employee employee, BindingResult bindingResult, Model model) {
        // 入力チェック（エラーがあれば更新画面に戻す）
        if (bindingResult.hasErrors()) {
            model.addAttribute("employee", employee);  // エラー時に入力内容を保持
            return "employees/Update";  // 従業員更新画面に戻る
        }

        // パスワードが空の場合、パスワードを変更しない
        if ("".equals(employee.getPassword())) {
            Employee existingEmployee = employeeService.findByCode(code);
            employee.setPassword(existingEmployee.getPassword());  // DBのパスワードを保持
        }

        // 更新処理
        ErrorKinds result = employeeService.update(employee);

        // エラーメッセージの処理（更新時のエラーをチェック）
        if (result != ErrorKinds.SUCCESS) {  // 成功でない場合にエラーメッセージを処理
            String errorName = ErrorMessage.getErrorName(result); // エラーメッセージの名前を取得
            String errorValue = ErrorMessage.getErrorValue(result); // エラーメッセージの値を取得

            // もしエラーメッセージが取得できなかった場合、デフォルトメッセージを使用
            if (errorName == null) {
                errorName = "Unknown Error";  // デフォルトエラーメッセージ
            }
            if (errorValue == null) {
                errorValue = "An unexpected error occurred.";  // デフォルトエラーメッセージ
            }

            // モデルにエラーメッセージを追加
            model.addAttribute("errorMessage", errorValue);  // エラーメッセージをセット
            model.addAttribute("employee", employee);  // 更新した従業員情報を再表示
            return "employees/Update";  // エラーがあれば更新画面に戻る
        }

        return "redirect:/employees";  // 更新完了後は従業員一覧画面に遷移
    }

    // 従業員新規登録画面
    @GetMapping(value = "/add")
    public String create(@ModelAttribute Employee employee) {
        return "employees/new";
    }

    // 従業員新規登録処理
    @PostMapping(value = "/add")
    public String add(@Validated Employee employee, BindingResult res, Model model) {
        // パスワード空白チェック
        if ("".equals(employee.getPassword())) {
            // パスワードが空白の場合のエラーメッセージ処理
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.BLANK_ERROR),
                    ErrorMessage.getErrorValue(ErrorKinds.BLANK_ERROR));  // エラーメッセージを渡す
            return create(employee);  // 新規登録画面に戻る
        }

        // 入力チェック
        if (res.hasErrors()) {
            return create(employee);  // エラーがあった場合、再度新規登録画面を表示
        }

        // 論理削除を行った従業員番号を指定すると例外となるためtry~catchで対応
        try {
            ErrorKinds result = employeeService.save(employee);

            // 保存結果がエラーの場合の処理
            if (ErrorMessage.contains(result)) {
                String errorName = ErrorMessage.getErrorName(result); // エラーメッセージの名前を取得
                String errorValue = ErrorMessage.getErrorValue(result); // エラーメッセージの値を取得
                model.addAttribute(errorName, errorValue);  // エラーメッセージを渡す
                return create(employee);  // 新規登録画面に戻る
            }

        } catch (DataIntegrityViolationException e) {
            // 例外処理でエラーメッセージを表示
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DUPLICATE_EXCEPTION_ERROR),
                    ErrorMessage.getErrorValue(ErrorKinds.DUPLICATE_EXCEPTION_ERROR));
            return create(employee);  // 新規登録画面に戻る
        }

        return "redirect:/employees";  // 新規登録完了後は従業員一覧画面に遷移
    }

    // 従業員削除処理
    @PostMapping(value = "/{code}/delete")
    public String delete(@PathVariable String code, @AuthenticationPrincipal UserDetail userDetail, Model model) {
        ErrorKinds result = employeeService.delete(code, userDetail);

        // エラーメッセージの処理
        if (ErrorMessage.contains(result)) {  // エラーメッセージが存在する場合のみ処理
            String errorName = ErrorMessage.getErrorName(result); // エラーメッセージの名前を取得
            String errorValue = ErrorMessage.getErrorValue(result); // エラーメッセージの値を取得
            model.addAttribute(errorName, errorValue);  // エラーメッセージを渡す
            model.addAttribute("employee", employeeService.findByCode(code));  // 削除後の従業員情報を表示
            return detail(code, model);  // 詳細画面に戻る
        }

        return "redirect:/employees";  // 削除後は従業員一覧画面に遷移
    }
}
