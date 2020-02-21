package com.kstefancic.lotterymaster.domain;

public interface Constants {
    String NOT_FOUND_LOTTERY = "not-found.lottery";

    String LOTO_6_OD_45 = "Loto 6 od 45";
    String LOTO_6_OD_45_UK = "CroatiaLoto6od45";
    String LOTO_6_OD_45_URL = "https://www.lutrija.hr/cms/loto6od45";
    String LOTO_7_OD_35 = "Loto 7 od 35";
    String LOTO_7_OD_35_UK = "CroatiaLoto7od35";
    String LOTO_7_OD_35_URL = "https://www.lutrija.hr/cms/loto7";
    String GERMANIA_LINK = "https://www.germaniasport.hr/hr/loto/rezultati/#/";
    String GREEK_KINO_GERMANIA_NAME = "Grčki kino (20/80)";
    String ITALIAN_KENO_GERMANIA_NAME = "Italijanski 10 E (20/90)";
    String GRCKI_KINO = "Grčki kino";
    String GRCKI_KINO_UK = "GrčkiKino20/80";
    String TALIJANSKI_KENO = "Italija 10e";
    String TALIJANSKI_KENO_UK = "Italija10e20/90";
    String EUROJACKPOT = "Eurojackpot";
    String WIN_FOR_LIFE = "ItalijaWinForLife10/20";


    String VERIFICATION_EMAIL= "<meta charset=UTF-8>\n" +
            "<div style='width: 100%%; height: 100%%; background-color: #F3F3F3;'>\n" +
            "    <div style='box-sizing: border-box;\n" +
            "    margin:auto auto;\n" +
            "    height: 70%%;\n" +
            "    width: 70%%; position: absolute;\n" +
            "    top: 15%%;\n" +
            "    left:15%%;\n" +
            "    border: 1px solid transparent;\n" +
            "    border-radius: 4px;\n" +
            "    overflow: auto;\n" +
            "    background-color: white;\n" +
            "    box-shadow: 0 1px 3px 0 #e6ebf1;\n" +
            "    -webkit-transition: box-shadow 150ms ease;\n" +
            "    transition: box-shadow 150ms ease;'>\n" +
            "        <img src='http://lottery-master.com/images/logo.png'\n" +
            "            style='width:40%%; height:auto; display:block; margin:0 auto' />\n" +
            "        <h3\n" +
            "            style='width:100%%; height:auto; display:block; margin:7%% auto 2%% auto; font-family: sans-serif; text-align:center; color:#42210B; font-size: 1.2em;'>\n" +
            "            LotteryMaster E-mail confirmation</h3>\n" +
            "        <h4 style=\"font-family: sans-serif;  color:#42210B; margin-left: 10%%;\">Thank you for registering to\n" +
            "            LotteryMaster!</h4>\n" +
            "        <p style=\"font-family: sans-serif;  color:#42210B; margin-left: 10%%;\">Your verification code is:\n" +
            "            <b>%s</b><br><br>Enter the code directly into the LotteryMaster verification page or just click the button\n" +
            "            below.<br><br><b>Good luck!</b></p>\n" +
            "        <div style='width:100%% ; text-align:center; height: fit-content; padding:10px 0; margin-top: 5%%;'>\n" +
            "            <a style='background-color:#42210B; color:white; border:0; border-radius: 5px; padding:10px; text-decoration: none; font-family: sans-serif;'\n" +
            "                href='http://app.lottery-master.com/#/verify-email/%s/%s'><b>VERIFY E-MAIL</b></a> </div>\n" +
            "\n" +
            "    </div>\n" +
            "</div>";
}
