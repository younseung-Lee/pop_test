package com.example.pop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MartIpVO {
    private String id;
    private String ip;
    private String now;
    private String edate;
    private String nm;
    private String gr;
    private String pw;
    private String insert_dt;
    private String update_dt;
    private String sho_biz_tel;
    private String sho_use_yn;
    private String limited_order_prx;
    private String mart_opening_hours;
    private String mart_dvry_area;
    private String mart_dvry_prx;
    private String mart_address;
    private String mart_bank_account;
    private String mart_bank_account_owner;
    private String mart_bank_nm;
    private String mart_order_time;
    private String mart_order_policyUp;
    private String mart_order_policyDown;
    private String pg_mid;
    private String pg_api_key;
    private String soldout_count;
    private String shopping_img_used;
    private String mart_parking;
    private String att_yn;
    private String use_mem_cmrc_yn;
    private String use_appdc_yn;
    private String use_pg_yn;
    private String sep_pwd;
    private String acct_trsfr_yn;
    private String mart_main_catgy_title;
    private String app_order_yn;

}
