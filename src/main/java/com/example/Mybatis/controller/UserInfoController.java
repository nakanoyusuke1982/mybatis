
package com.example.Mybatis.controller;

// 【全体像】UserInfoController の役割
// このクラスは Spring MVC の Controller（画面制御） です。
// ・画面の遷移
// ・入力フォームの受け取り
// ・サービス（UserInfoService）への処理依頼
// ・結果を画面へ渡す
// を担当している
// データベース操作は Controller では行いません。
// すべてServiceに任せます

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
// Service を自動で DI（依存性注入）する仕組み。
import org.springframework.stereotype.Controller;
// Spring に「これは Controller だよ」と知らせるアノテーション（これを付けると、URLのリクエストを受けられる）
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.example.Mybatis.dto.UserAddRequest;
// DTO（画面 → Controller に送られるデータ入れ物）
import com.example.Mybatis.dto.UserSearchRequest;
import com.example.Mybatis.dto.UserUpdateRequest;
import com.example.Mybatis.entity.UserInfo;
import com.example.Mybatis.service.UserInfoService;

/**
 * ユーザー情報 Controller
 */
@Controller
public class UserInfoController {
    // Spring がこのクラスを 画面制御の中心（Controller） として認識する。
    // URL を受け取り、画面へ返す役割を持つ。

    /**
     * ユーザー情報 Service
     */
    @Autowired
    private UserInfoService userInfoService;
    // Service 層（ビジネスロジック）を自動で注入して使えるようにする仕組み。
    // Controller は DB 操作をしない → 必ず Service に命令する。

    /**
     * ユーザー情報一覧画面を表示
     * @param model Model
     * @return ユーザー情報一覧画面
     */
    @GetMapping(value = "/user/list")
    public String displayList(Model model) {
        // ユーザー一覧画面（/user/list）
        // GET メソッドで /user/list にアクセスされたらこのメソッドが動く。
        // Model は画面に値を渡すための入れ物。
        List<UserInfo> userList = userInfoService.findAll();
        // DB から全ユーザーを Service 経由で取得。
        model.addAttribute("userlist", userList);
        // 画面（Thymeleaf）で ${userlist} として使える。
        model.addAttribute("userSearchRequest", new UserSearchRequest());
        return "user/search";
    }

    /**
     * ユーザー新規登録画面を表示
     * @param model Model
     * @return ユーザー情報一覧画面
     */
    @GetMapping(value = "/user/add")
    public String displayAdd(Model model) {
        model.addAttribute("userAddRequest", new UserAddRequest());
        return "user/add";
        // 新規登録画面（/user/add）
        // 入力フォームに "空の DTO" を渡す → フォームが表示できる。
    }

    /**
     * ユーザー編集画面を表示
     * @param id ユーザーID
     * @param model Model
     * @return ユーザー編集画面
     */
    @GetMapping("/user/{id}/edit")
    public String displayEdit(@PathVariable Long id, Model model) {
        // 編集画面（/user/{id}/edit）
        // URL の {id} の部分を受け取る　例：/user/5/edit → id=5
        UserInfo user = userInfoService.findById(id);
        // DB から指定ユーザーを取得
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest();
        userUpdateRequest.setId(user.getId());
        // UserUpdateRequest（更新用 DTO）に値を詰め直して画面へ渡す
        // ここ重要！
        // Entity(UserInfo) は直接画面に渡さない
        // 画面には DTO を渡す（セキュリティの都合）
        userUpdateRequest.setId(user.getId());
        userUpdateRequest.setName(user.getName());
        userUpdateRequest.setPhone(user.getPhone());
        userUpdateRequest.setAddress(user.getAddress());
        model.addAttribute("userUpdateRequest", userUpdateRequest);
        return "user/edit";
    }

    /**
     * ユーザー情報検索
     * @param userSearchRequest リクエストデータ
     * @param model Model
     * @return ユーザー情報一覧画面
     */
    @RequestMapping(value = "/user/search", method = RequestMethod.POST)
    public String search(@ModelAttribute UserSearchRequest userSearchRequest, Model model) {
        // ユーザー検索（POST /user/search
        // POST リクエストを受ける
        // 画面の検索フォームから送られた値が userSearchRequest に詰まって入ってくる
        List<UserInfo> userList = userInfoService.search(userSearchRequest);
        model.addAttribute("userlist", userList);
        return "user/search";
        // 検索結果を userlist として画面に返す
    }

    /**
     * ユーザー情報削除（論理削除）
     * @param id ユーザーID
     * @param model Model
     * @return ユーザー情報一覧画面
     */
    @GetMapping("/user/{id}/delete")
    public String delete(@PathVariable Long id, Model model) {
        // ユーザー情報の削除
        userInfoService.delete(id);
        return "redirect:/user/list";
        // 指定 ID のユーザーを Service に削除させ、一覧画面へリダイレクト。
    }

    /**
     * ユーザー新規登録
     * @param userRequest リクエストデータ
     * @param model Model
     * @return ユーザー情報一覧画面
     */
    @RequestMapping(value = "/user/create", method = RequestMethod.POST)
    public String create(@Validated @ModelAttribute UserAddRequest userRequest, BindingResult result, Model model) {
        if (result.hasErrors()) {
        // 新規登録処理（POST /user/create）
        // ポイントは
        // @Validated
        // → バリデーション（@NotNull など）を実行
        // BindingResult
        // → バリデーション結果（エラー）の入れ物

            // 入力チェックエラーの場合
            List<String> errorList = new ArrayList<String>();
            for (ObjectError error : result.getAllErrors()) {
                errorList.add(error.getDefaultMessage());
            }
            model.addAttribute("validationError", errorList);
            return "user/add";
        }
        // ユーザー情報の登録
        userInfoService.save(userRequest);
        // DB に登録するのは Service の役割。
        return "redirect:/user/list";
    }

    /**
     * ユーザー更新
     * @param userRequest リクエストデータ
     * @param model Model
     * @return ユーザー情報詳細画面
     */
    @RequestMapping(value = "/user/update", method = RequestMethod.POST)
    public String update(@Validated @ModelAttribute UserUpdateRequest userUpdateRequest, BindingResult result, Model model) {
        if (result.hasErrors()) {
            //更新処理（POST /user/update）
            // これも新規登録と同じ仕組み
            // （DTO で受け取り → バリデーション → Service で更新 → リダイレクト）
            List<String> errorList = new ArrayList<String>();
            for (ObjectError error : result.getAllErrors()) {
                errorList.add(error.getDefaultMessage());
            }
            model.addAttribute("validationError", errorList);
            return "user/edit";
        }
            userInfoService.update(userUpdateRequest);
         
        return "redirect:/user/list";
    }
}

// 全体の流れ（超重要）
// Controller の責務は次の通り：
// Controller	画面の入力を受け取り、Service に渡し、結果を画面へ返す
// Service	    DB 操作やビジネスロジック
// Repository/Mapper	DB に SQL を発行する
// DTO	        画面とのデータの受け渡し
// Entity	    DB の1レコードを表す Java クラス

// まとめ
// この Controller は：
// ユーザー一覧
// 新規登録
// 更新
// 削除
// 検索
// 編集画面表示
// という CRUD + 検索 の全機能を持っている

// CRUDについて
// Create　データを新しく登録する　userInfoService.save(userRequest);
// Read　　データを取得する　List<UserInfo> userList = userInfoService.findAll();　UserInfo user = userInfoService.findById(id);
// Updata　データを変更する　userInfoService.update(userUpdateRequest);　
// Delete　データを削除する　userInfoService.delete(id);