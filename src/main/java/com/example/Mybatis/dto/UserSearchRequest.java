package com.example.Mybatis.dto;

import java.io.Serializable;

import lombok.Data;

/**
 * ユーザー情報 検索用リクエストデータ
 */
@Data
public class UserSearchRequest implements Serializable {

    /**
     * ユーザーID
     */
    private String id;

    /**
     * ユーザー名
     */
    private String name;
}