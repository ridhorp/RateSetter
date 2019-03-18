package com.ciptagrafika.ratesetter.App;

/**
 * Created by IT on 2/18/2017.
 */

public class AppConfig {

    public static String DOMAIN = "http://10.10.40.40/antrian/api/";

    // URL
    public static String URL_GET_SETTING = DOMAIN + "setting.php";
    public static String URL_GET_FILEJADI = DOMAIN + "jadi.php";

    public static String URL_SET_NOSETTING = DOMAIN + "update_antrian.php?setting=";
    public static String URL_SET_NOJADI = DOMAIN + "update_antrian.php?jadi=";

    public static String URL_SET_NOFILEJADI = DOMAIN + "set.php";

    public static String URL_SET_VISITOR_FILEJADI = DOMAIN + "u_peng_jadi.php?pengunjung=";
    public static String URL_SET_VISITOR_SETTING = DOMAIN + "u_peng_setting.php?pengunjung=";

}
