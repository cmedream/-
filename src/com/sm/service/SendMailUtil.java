package com.sm.service;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class SendMailUtil {

	static String HOST = ""; //smtp服务器
	static String FROM = ""; //发件人地址
	static String TO = ""; //收件人地址
	static String AFFIX = ""; //附件
	static String AFFIXNAME = ""; //附件名称
	static String USER = ""; //用户名
	static String PWD = ""; //163的授权码
	static String SUBJECT = ""; //邮件标题
	static String[] TOS = null;
	
	static {
		try {
			Properties props = new Properties();
			
			props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties"));//从自定义配置文件获取相关参数
			HOST = props.getProperty("host");
			FROM = props.getProperty("from");
			TO = props.getProperty("to");
			TOS = TO.split(",");
			SUBJECT = props.getProperty("subject");
			AFFIX = props.getProperty("affix");
			AFFIXNAME = props.getProperty("affixname");
			USER = props.getProperty("user");
			PWD = props.getProperty("pwd");
			SUBJECT=new String(SUBJECT.getBytes("ISO-8859-1"), "utf-8");//Properties 默认是按ISO-8859-1读取的,需要转码
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 *发送邮件（163邮箱）
	 *@param host
	 *@param user
	 *@param pwd
	 */
	public static void send_163 (String context){
		Properties props = new Properties();
		props.put("mail.smtp.host",HOST);//设置发送邮件的邮件服务器属性（这里使用网易的smtp服务器）
		props.put("mail.smtp.auth", "true");//需要经过授权，就是有用户名和密码的校验（这条一定要有的）
		Session session = Session.getDefaultInstance(props);//用props对象构建一个session
		session.setDebug(true);
		MimeMessage message = new MimeMessage(session);//用session为参数定义消息对象
		try {
			message.setFrom(new InternetAddress(FROM));//加载发件人地址
			InternetAddress[] sendTo = new InternetAddress[TOS.length];//加载收件人地址
			for (int i = 0; i < TOS.length; i ++){
				sendTo[i] = new InternetAddress(TOS[i]);
			}
			message.addRecipients(Message.RecipientType.TO, sendTo);
			message.addRecipients(Message.RecipientType.CC, InternetAddress.parse(FROM));//设置在发送给收件人之前给自己（发送方）抄送一份，不然会被当成垃圾邮件，报554错。
			message.setSubject(SUBJECT);//加载标题
			Multipart multipart = new MimeMultipart();//向multipart对象中添加邮件的各个部分内容，包括文本内容和附件
			BodyPart contentPart = new MimeBodyPart();//设置邮件的文本内容
			contentPart.setText(context);
			multipart.addBodyPart(contentPart);
			if(!AFFIX.isEmpty()){//添加附件
				BodyPart messageBodyPart = new MimeBodyPart();
				DataSource source = new FileDataSource(AFFIX);
				messageBodyPart.setDataHandler(new DataHandler(source));//添加附件的内容
//				sun.misc.BASE64Encoder enc = new sun.misc.BASE64Encoder();
				messageBodyPart.setFileName("附件标题");
				multipart.addBodyPart(messageBodyPart);
			}
			message.setContent(multipart);//将multipart对象放到message中
			message.saveChanges();//保存邮件
			Transport transport = session.getTransport("smtp");//发送邮件
			transport.connect(HOST, USER, PWD);//连接服务器邮箱
			transport.sendMessage(message, message.getAllRecipients());//把邮件发送出去
			transport.close();//关闭连接
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	/**
	 *发送邮件（qq邮箱）
	 *@param host
	 *@param user
	 *@param pwd
	 * @throws MessagingException 
	 * @throws AddressException 
	 */
	public static void send_qq (String context) throws AddressException, MessagingException{
		Properties props = new Properties();
		props.put("mail.transport.protocol", "smtp");//连接协议
		props.put("mail.smtp.host","smtp.qq.com");//主机名
		props.put("mail.smtp.port", 465);//端口号
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.ssl.enable", "true");//设置是否使用ssl安全连接 ---一般都使用
		props.put("mail.debug", "true");//设置是否显示debug信息，true会在控制台显示相关信息
		//得到回话对象
		Session session = Session.getDefaultInstance(props);
		//获取邮件对象
		Message message = new MimeMessage(session);
		//设置发件人邮箱地址
		message.setFrom(new InternetAddress(FROM));
		//设置收件人邮箱地址
		InternetAddress[] sendTo = new InternetAddress[TOS.length];//加载收件人地址
		for (int i = 0; i < TOS.length; i ++){
			sendTo[i] = new InternetAddress(TOS[i]);
		}
		message.addRecipients(Message.RecipientType.TO, sendTo);
		//设置邮件标题
		message.setSubject("QQ邮箱授权码");
		//设置邮件内容
		message.setText(context);
		//得到邮差对象
		Transport transport = session.getTransport();
		//连接自己的邮箱账户
		transport.connect(USER, PWD);//密码为QQ邮箱开通的stmp服务后得到的客户端授权码
		//发送邮件
		transport.sendMessage(message, message.getAllRecipients());
		transport.close();
	}
	
	
	
	
	public static void main(String[] args) throws AddressException, MessagingException {
//		send_163("163邮箱的授权码：cme1820363");
		send_qq("QQ邮箱授权码：fiuudaorhphdbecf");
	}
	
	
	
	
	
}
