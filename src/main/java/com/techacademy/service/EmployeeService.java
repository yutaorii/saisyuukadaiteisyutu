package com.techacademy.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.entity.Employee;
import com.techacademy.repository.EmployeeRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 従業員保存
    @Transactional
    public ErrorKinds save(Employee employee) {

        // パスワードチェック
        ErrorKinds result = employeePasswordCheck(employee);
        if (ErrorKinds.CHECK_OK != result) {
            return result;
        }

        // 従業員番号重複チェック
        if (findByCode(employee.getCode()) != null) {
            return ErrorKinds.DUPLICATE_ERROR;
        }

        employee.setDeleteFlg(false);

        LocalDateTime now = LocalDateTime.now();
        employee.setCreatedAt(now);
        employee.setUpdatedAt(now);

        employeeRepository.save(employee);
        return ErrorKinds.SUCCESS;
    }

    // 従業員削除
    @Transactional
    public ErrorKinds delete(String code, UserDetail userDetail) {

        // 自分を削除しようとした場合はエラーメッセージを表示
        if (code.equals(userDetail.getEmployee().getCode())) {
            return ErrorKinds.LOGINCHECK_ERROR;
        }
        Employee employee = findByCode(code);
        LocalDateTime now = LocalDateTime.now();
        employee.setUpdatedAt(now);
        employee.setDeleteFlg(true);

        return ErrorKinds.SUCCESS;
    }

    // 従業員一覧表示処理
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    // 1件を検索
    public Employee findByCode(String code) {
        // findByIdで検索
        Optional<Employee> option = employeeRepository.findById(code);
        // 取得できなかった場合はnullを返す
        Employee employee = option.orElse(null);
        return employee;
    }

    // 従業員パスワードチェック
    private ErrorKinds employeePasswordCheck(Employee employee) {

        // 従業員パスワードの半角英数字チェック処理
        if (isHalfSizeCheckError(employee)) {
            return ErrorKinds.HALFSIZE_ERROR;
        }

        // 従業員パスワードの8文字～16文字チェック処理
        if (isOutOfRangePassword(employee)) {
            return ErrorKinds.RANGECHECK_ERROR;
        }

        // パスワードを暗号化
        employee.setPassword(passwordEncoder.encode(employee.getPassword()));

        return ErrorKinds.CHECK_OK;
    }

    // 従業員パスワードの半角英数字チェック処理
    private boolean isHalfSizeCheckError(Employee employee) {

        // 半角英数字チェック
        Pattern pattern = Pattern.compile("^[A-Za-z0-9]+$");
        Matcher matcher = pattern.matcher(employee.getPassword());
        return !matcher.matches();
    }

    // 従業員パスワードの8文字～16文字チェック処理
    public boolean isOutOfRangePassword(Employee employee) {

        // 桁数チェック
        int passwordLength = employee.getPassword().length();
        return passwordLength < 8 || 16 < passwordLength;
    }

    // 従業員情報の更新
    @Transactional
    public ErrorKinds update(Employee employee) {

        // 従業員情報を取得
        Employee existingEmployee = findByCode(employee.getCode());

        // 氏名の必須チェック
        if (employee.getName() == null || employee.getName().isEmpty()) {
            return ErrorKinds.BLANK_ERROR;  // "値を入力してください"
        }

        // 氏名の桁数チェック（20文字以下）
        if (employee.getName().length() > 20) {
            return ErrorKinds.RANGECHECK_ERROR;  // "20文字以下で入力してください"
        }

        // パスワードの桁数チェック（8～16文字）
        if (employee.getPassword() != null && !employee.getPassword().isEmpty()) {
            int passwordLength = employee.getPassword().length();
            if (passwordLength < 8 || passwordLength > 16) {
                return ErrorKinds.RANGECHECK_ERROR;  // "8文字以上16文字以下で入力してください"
            }

            // パスワードの形式チェック（半角英数字のみ）
            if (!employee.getPassword().matches("^[A-Za-z0-9]+$")) {
                return ErrorKinds.HALFSIZE_ERROR;  // "パスワードは半角英数字のみで入力してください"
            }

            // パスワードが入力されていれば暗号化してセット
            existingEmployee.setPassword(passwordEncoder.encode(employee.getPassword()));
        } else {
            // パスワードが空の場合は、既存のパスワードを維持
            existingEmployee.setPassword(existingEmployee.getPassword());
        }

        // 氏名、権限を更新
        existingEmployee.setName(employee.getName());
        existingEmployee.setRole(employee.getRole());  // 権限の更新

        // 更新日時を現在日時に設定
        existingEmployee.setUpdatedAt(LocalDateTime.now());

        // 更新された従業員情報を保存
        employeeRepository.save(existingEmployee);

        // 更新成功
        return ErrorKinds.SUCCESS;
    }
}
