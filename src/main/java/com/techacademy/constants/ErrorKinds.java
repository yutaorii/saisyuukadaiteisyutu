package com.techacademy.constants;

// エラーメッセージ定義
public enum ErrorKinds {

    // エラー内容
    // 空白チェックエラー
    BLANK_ERROR, // 既存の空白チェックエラー
    // 半角英数字チェックエラー
    HALFSIZE_ERROR, // 既存の半角英数字エラーチェック
    // 桁数(8桁~16桁以外)チェックエラー
    RANGECHECK_ERROR, // 既存の桁数エラーチェック

    // 氏名に関するエラー（追加）
    NAME_BLANK_ERROR,  // 氏名の空白チェックエラー
    NAME_RANGE_ERROR,  // 氏名の桁数超過チェックエラー

    // パスワードに関するエラー（追加）
    PASSWORD_BLANK_ERROR,  // パスワードの空白チェックエラー
    PASSWORD_RANGE_ERROR,  // パスワードの桁数超過チェックエラー
    PASSWORD_HALFSIZE_ERROR,  // パスワードの半角英数字チェックエラー

    // 重複チェックエラー(例外あり)
    DUPLICATE_EXCEPTION_ERROR,
    // 重複チェックエラー(例外なし)
    DUPLICATE_ERROR,
    // ログイン中削除チェックエラー
    LOGINCHECK_ERROR,
    // 日付チェックエラー
    DATECHECK_ERROR,
    // チェックOK
    CHECK_OK,
    // 正常終了
    SUCCESS;

}
