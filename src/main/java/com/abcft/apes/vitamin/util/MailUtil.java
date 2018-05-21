package com.abcft.apes.vitamin.util;

import com.abcft.apes.vitamin.task.SendMailTask;
import org.apache.log4j.Logger;
import org.bson.Document;

import javax.json.JsonObject;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 * Created by zhyzhu on 17-4-23.
 */
public class MailUtil {

    private static Logger logger = Logger.getLogger(MailUtil.class);

    private static String MAIL_HOST = null;
    private static String MAIL_FROM = null;
    private static String MAIL_ENV = null;
    private static String MAIL_USER = null;
    private static String MAIL_PASSWORD = null;
    private static String BIND_LOGO_URL = "https://data.modeling.ai/images/bind-logo.png";

    private enum EmailTexts {
        Dear("Dear"),
        OrderCreated("order created"),
        OrderPayed("order pay"),
        OrderFailed("order failed"),
        VerifyWorkEmail("Active My Account"),
        VerifyIdentity("Verify My Identity");

        private String name;

        EmailTexts(String s) {
            name = s;
        }

        public static EmailTexts valueOf(int status) {
            for (EmailTexts emailTexts : EmailTexts.values()) {
                if (emailTexts.ordinal() == status)
                    return emailTexts;
            }
            return OrderCreated;
        }
    }

    private enum Langs {
        ZH_CN("zh_CN");

        private String name;

        Langs(String s) {
            name = s;
        }

    }

    private static HashMap<String, String> EmailTextMap = new HashMap<String, String>() {{
        put(EmailTexts.Dear.name, "Dear");
        put(EmailTexts.Dear.name + "_" + Langs.ZH_CN.name, "亲爱的");
        
        put(EmailTexts.OrderCreated.name, "Order Created");
        put(EmailTexts.OrderCreated.name + "_" + Langs.ZH_CN.name, "创建订单");
        
        put(EmailTexts.OrderPayed.name, "Order Payed");
        put(EmailTexts.OrderPayed.name + "_" + Langs.ZH_CN.name, "订单生效");
        
        put(EmailTexts.OrderFailed.name, "Order Failed");
        put(EmailTexts.OrderFailed.name + "_" + Langs.ZH_CN.name, "订单失败");
        
        put(EmailTexts.VerifyIdentity.name, "Verify My Identity");
        put(EmailTexts.VerifyIdentity.name + "_" + Langs.ZH_CN.name, "验证我的账号");
        
        put(EmailTexts.VerifyWorkEmail.name, "Active My Account");
        put(EmailTexts.VerifyWorkEmail.name + "_" + Langs.ZH_CN.name, "激活我的账号");
    }};

    public static void init(String hostName, String from, String env, String user, String password) {
        MAIL_HOST = hostName;
        MAIL_FROM = from;
        MAIL_ENV = env;
        MAIL_USER = user;
        MAIL_PASSWORD = password;
    }

    public static String getEnv() {
        return MAIL_ENV;
    }

    private static String getWorkEmailVerifyCallbackUrl(String userId, String token) {
        return StringUtils.getHostUrl() + "/view/bind/email-verify.html?id=" + userId + "&token=" + token + "&status=1";
    }

    private static String getResetPassCallBackUrl(String userId, String token) {
        return StringUtils.getHostUrl() + "/view/bind/reserpwdconfirm.html?id=" + userId + "&token=" + token;
    }

    private static String getOrderTr(String email, String orderId, String lang) {
        StringBuilder stringBuilder = new StringBuilder();
        String userId = AccountUtil.getIdByEmail(email);

        Document document = OrderUtil.getOrderHistory(
                userId, lang, "", "", "",
                0, Integer.MAX_VALUE, OrderUtil.OrderStatus2.NULL.ordinal());

        List<Document> list = document.get("list", ArrayList.class);
        for (Document doc : list) {
            if (doc.containsKey("id") && doc.getString("id").equalsIgnoreCase(orderId)) {
                String row = StringUtils.formatList(doc.get("company", ArrayList.class), "name", "", ",", "", 0);
                stringBuilder.append("<tr class='tr'><td>").append(doc.get("time")).append("</td><td>")
                        .append(doc.get("plan")).append("</td><td class='abbr'>").append("<span title='")
                        .append(row).append("'>").append(row).append("</span>").append("</td><td>")
                        .append(doc.get("deadline")).append("</td><td>").append("￥").append(doc.get("price"))
                        .append("</td><td>").append(doc.get("status")).append("</td></tr>");
            }
        }
        return stringBuilder.toString();
    }

    public static boolean SendOrderCreatedEmail(String email, String name, String orderId, String lang) {
        return SendOrderEmail(email, name, orderId, EmailTexts.OrderCreated.ordinal(), lang);
    }

    public static boolean SendOrderDoneEmail(String email, String name, String orderId, String lang) {
        return SendOrderEmail(email, name, orderId, EmailTexts.OrderPayed.ordinal(), lang);
    }

    public static boolean SendOrderFailedEmail(String email, String name, String orderId, String lang) {
        return SendOrderEmail(email, name, orderId, EmailTexts.OrderFailed.ordinal(), lang);
    }

    private static boolean SendOrderEmail(String email, String name, String orderId, int status, String lang) {
        if (!lang.isEmpty() && !lang.startsWith("_")) {
            lang = "_" + lang;
        }
        String tr = getOrderTr(email, orderId, lang);
        String version = OrderUtil.getOrderPlanName(orderId, lang);

        String EVERSIGHT_AI_URL = "https://www.eversight.ai";
        String resubmit = "<a href='" + EVERSIGHT_AI_URL + "/#/order'>重新提交订单</a>";
        String pTagC = "<p>我们已经收到您发起的汇款通知，确认汇款到账后，将为您开通" + version + "版本权限。</p>";
        String pTagD = "<p>我们已经收到您发起的汇款，并为您升级到" + version + "版本，您可以登录官网进行查看。</p>";
        String pTagF = "<p>很遗憾，我们没有收到您发起的汇款，请与汇款银行确认是否汇款成功，或" + resubmit + "。</p>";
        String pTag =
                status == EmailTexts.OrderCreated.ordinal() ? pTagC
                        : status == EmailTexts.OrderPayed.ordinal() ? pTagD
                        : pTagF;
        String style = "<style type='text/css'> *{ padding: 0; margin: 0; } body,html{ height: 100%; width: 100%; /* overflow: scroll; */} .header{ width: 68%; height: 100px; background:#091f27;; } .header .img{ width: 60px; height: 60px; float: left; margin-left: 15px; margin-top: 20px; cursor: pointer; } .header .title{ color:#FFFFFF; float: left; margin-top: 120px; margin-left: 100px; } .form-container{ width:100%; height: 500px; margin-left:15px; } .form-container .p{ font-weight: bold; } .form-container p{ font-size:16px; line-height: 40px; } .form-container table{ width: 90%; margin-top: 20px; border-collapse: collapse; table-layout: fixed;} .form-container table tr{ height: 50px; line-height: 50px; } .form-container table tr td{ text-align: center; } .tr td{ border:1px solid #D8D0D0; } .abbr {text-overflow: ellipsis; -moz-text-overflow: ellipsis; white-space: nowrap; overflow: hidden;} .first td{ background: #DBD8D8; border:1px solid #FFFFFF; } .user_form{ width: 70%; margin-top: 20px; } .lineheight{ line-height: 30px !important; }</style>";
        String divs = "<body> <div class='header'>  <img src=" + BIND_LOGO_URL + " class='img'/>  </div>  <div class='user_form'>  <div class='form-container'>  <p class='p'>" + EmailTextMap.get(EmailTexts.Dear + lang) + " " + name + ",</p> <p>感谢您购买<a style='color:deepskyblue;text-decoration: underline;' href=" + EVERSIGHT_AI_URL + ">EVERSIGHT.AI</a>的在线数据服务。</p> " + pTag + "<p class='p'>订单信息:</p>  <table cellpadding='0' cellspacing='0' border='0'>  <tr class='first'>  <td>Time</td>  <td>Plan</td>  <td>Company</td>  <td>Deadline</td>  <td>Price</td>  <td>Status</td>  </tr>" + tr + " </table>  <p style='font-size: 16px;margin-top: 20px;' class='lineheight'>感谢您的使用</p>  <p class='lineheight'>The Eversight.Ai Team</p>  </div>  </div>  </body>";
        String content = style + divs;
        String emailSubject = EmailTexts.valueOf(status).name;
        String emailText = emailSubject + lang;
        logger.info("sending " + emailSubject + " email...");
        boolean res = SendEmail(email, content, EmailTextMap.get(emailText));
        if (!res) {
            logger.warn("send " + emailSubject + " email failed");
            return false;
        }
        logger.info(emailSubject + "email has been sent");
        return true;
    }

    public static boolean SendVerifyIdentityEmail(String email, String lang) {
        return SendVerifyEmail(email, EmailTexts.VerifyIdentity.ordinal(), lang);
    }

    public static boolean SendVerifyWorkEmailEmail(String email, String lang) {
        return SendVerifyEmail(email, EmailTexts.VerifyWorkEmail.ordinal(), lang);
    }

    private static boolean SendVerifyEmail(String email, int status, String lang) {
        if (!lang.isEmpty() && !lang.startsWith("_")) {
            lang = "_" + lang;
        }
        boolean isEn = lang.isEmpty() || lang.equalsIgnoreCase("en_US");

        JsonObject userJson = AccountUtil.getUserByEmail(email);
        String userId = userJson.getString("id");
        String name = userJson.getString("username");
        JsonObject token = TokenUtil.generateToken(userJson, 7 * 24 * 60);
        String auth_token = token.getString("auth_token");

        String url = status == EmailTexts.VerifyIdentity.ordinal() ? getResetPassCallBackUrl(userId, auth_token) : getWorkEmailVerifyCallbackUrl(userId, auth_token);

        String emailSubject = EmailTexts.valueOf(status).name;
        String emailText = emailSubject + lang;
        String subject = EmailTextMap.get(emailText);

        String thanksForCreating = isEn ? "Thank you for creating an account at Eversight.AI." : "感谢您注册Eversight.AI。";
        String forYourSecurity1 = isEn ? "For your security, we will need to make sure you are the owner of this account." : "为了您的账号安全，请点击下面的验证按钮确认您的身份信息。";
        String forYourSecurity2 = isEn ? "For your security, we will need to make sure you are the owner of this account." : "为了您的账号安全，请点击下面的激活按钮确认您的邮箱地址。";
        String pleaseClick = isEn ? "Please click" : "";
        String below = isEn ? "below to reset your password." : "";
        String below1 = isEn ? "below to confirm your business email address." : "";
        String welcome = isEn ? "Welcome to Eversight.AI!" : "欢迎使用EVERSIGHT.AI!";
        String sincerely = isEn ? "Sincerely," : "";
        String eversightTeam = isEn ? "The Eversight.Ai Team" : "Eversight.AI团队";

        String pTagI = " <h3>" + forYourSecurity1 + " </h3>" + (isEn ? "<h3>" + pleaseClick + "&nbsp;<b>" + subject + "</b> " + below + "</h3>" : "");
        String pTagW = "<h3>" + thanksForCreating + "</h3><h3>" + forYourSecurity2 + "</h3>" + (isEn ? "<h3>" + pleaseClick + "&nbsp;<b>" + subject + "</b> " + below1 + "</h3>" : "");
        String pTag = status == EmailTexts.VerifyIdentity.ordinal() ? pTagI : pTagW;
        String aButton = "<div style='border-radius: 10px'> <a id='verify' href='" + url + "' target='_blank'>" + subject + "</a></div>";

        String style = "<style type='text/css'> * { padding: 0; margin: 0; } body, html { height: 100%; width: 100%; /* overflow: scroll; */} .header { width: 660px; height: 100px; background: #091f27; ; } .header .img { width: 60px; height: 60px; float: left; margin-left: 15px; margin-top: 20px; cursor: pointer; } .header .title { color: #FFFFFF; float: left; margin-top: 120px; margin-left: 100px; } .form-container { width: 100%; height: 500px; margin-left: 15px; } .form-container .p { font-weight: bold; line-height: 40px; margin-bottom: 10px; } .form-container p { font-size: 16px; /* line-height: 40px; */ /* font-weight: bold; */} .form-container table { width: 90%; margin-top: 20px; border-collapse: collapse; table-layout: fixed; } .form-container table tr { height: 50px; line-height: 50px; } .form-container table tr td { text-align: center; } .tr td { border: 1px solid #D8D0D0; } .abbr { text-overflow: ellipsis; -moz-text-overflow: ellipsis; white-space: nowrap; overflow: hidden; } .first td { background: #DBD8D8; border: 1px solid #FFFFFF; } .user_form { width: 70%; margin-top: 20px; } .user_form h3 { line-height: 25px; font-size: 16px; font-weight: normal; } #verify { display: block; text-decoration: none; text-indent: 2px; border: none; border-radius: 10px; text-align: center; width: 200px; line-height: 40px; margin-top: 30px; margin-left: 0px; margin-bottom: 30px; background: #355867; color: #92bdd2; } .lineheight { line-height: 20px !important;} </style>";
        String divs = "<body> <div class='header'>  <img src=" + BIND_LOGO_URL + " class='img'/>  </div>  <div class='user_form'>  <div class='form-container'>  <p class='p'>" + EmailTextMap.get(EmailTexts.Dear + lang) + " " + name + ",</p>" + pTag + aButton + "<p style='margin-top: 20px;'>" + welcome + "</p> <p style='font-size: 16px;margin-top: 20px;' class='lineheight'>" + sincerely + "</p>  <p class='lineheight'>" + eversightTeam + "</p>  </div>  </div>  </body>";
        String content = style + divs;
        logger.info("sending " + emailSubject + " email...");
        boolean res = SendEmail(email, content, subject);
        if (!res) {
            logger.warn("send " + emailSubject + " email failed");
            return false;
        }
        logger.info(emailSubject + "email has been sent");
        return true;
    }

    public static boolean SendEmail(String to, String content, String subject) {
        try {
            SendMailTask task = new SendMailTask(to, content, subject);
            SendMailTask.addTask(task);
            return true;
        } catch (Exception e) {
            logger.error("send email error", e);
        }

        return false;
    }

    public static boolean SendSMTPMail(String toAddr, String content, String subject) {
        try {
            final Properties props = new Properties();
            // 表示SMTP发送邮件，需要进行身份验证
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.host", MAIL_HOST);
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.port", "465");

            // 发件人的账号
            // 访问SMTP服务时需要提供的密码
            props.put("mail.user", MAIL_USER);
            props.put("mail.password", MAIL_PASSWORD);

            // 构建授权信息，用于进行SMTP进行身份验证
            Authenticator authenticator = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    // 用户名、密码
                    String userName = props.getProperty("mail.user");
                    String password = props.getProperty("mail.password");
                    return new PasswordAuthentication(userName, password);
                }
            };
            // 使用环境属性和授权信息，创建邮件会话
            Session mailSession = Session.getInstance(props, authenticator);
            // 创建邮件消息
            MimeMessage message = new MimeMessage(mailSession);
            // 设置发件人
            InternetAddress form = new InternetAddress(MAIL_FROM, "Eversight", "UTF8");
            message.setFrom(form);

            // 设置收件人
            InternetAddress to = new InternetAddress(toAddr);
            message.setRecipient(MimeMessage.RecipientType.TO, to);

            // 设置邮件标题
            message.setSubject(subject, "UTF8");

            // 设置邮件的内容体
            message.setContent(content, "text/html;charset=UTF-8");

            // 发送邮件
            Transport.send(message);
            logger.info(String.format("send email to: <%s> [%s]", toAddr, subject));
            return true;
        } catch (Exception e) {
            logger.error("send email error", e);
        }
        return false;
    }

}