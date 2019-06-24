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

	static String HOST = ""; //smtp������
	static String FROM = ""; //�����˵�ַ
	static String TO = ""; //�ռ��˵�ַ
	static String AFFIX = ""; //����
	static String AFFIXNAME = ""; //��������
	static String USER = ""; //�û���
	static String PWD = ""; //163����Ȩ��
	static String SUBJECT = ""; //�ʼ�����
	static String[] TOS = null;
	
	static {
		try {
			Properties props = new Properties();
			
			props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties"));//���Զ��������ļ���ȡ��ز���
			HOST = props.getProperty("host");
			FROM = props.getProperty("from");
			TO = props.getProperty("to");
			TOS = TO.split(",");
			SUBJECT = props.getProperty("subject");
			AFFIX = props.getProperty("affix");
			AFFIXNAME = props.getProperty("affixname");
			USER = props.getProperty("user");
			PWD = props.getProperty("pwd");
			SUBJECT=new String(SUBJECT.getBytes("ISO-8859-1"), "utf-8");//Properties Ĭ���ǰ�ISO-8859-1��ȡ��,��Ҫת��
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 *�����ʼ���163���䣩
	 *@param host
	 *@param user
	 *@param pwd
	 */
	public static void send_163 (String context){
		Properties props = new Properties();
		props.put("mail.smtp.host",HOST);//���÷����ʼ����ʼ����������ԣ�����ʹ�����׵�smtp��������
		props.put("mail.smtp.auth", "true");//��Ҫ������Ȩ���������û����������У�飨����һ��Ҫ�еģ�
		Session session = Session.getDefaultInstance(props);//��props���󹹽�һ��session
		session.setDebug(true);
		MimeMessage message = new MimeMessage(session);//��sessionΪ����������Ϣ����
		try {
			message.setFrom(new InternetAddress(FROM));//���ط����˵�ַ
			InternetAddress[] sendTo = new InternetAddress[TOS.length];//�����ռ��˵�ַ
			for (int i = 0; i < TOS.length; i ++){
				sendTo[i] = new InternetAddress(TOS[i]);
			}
			message.addRecipients(Message.RecipientType.TO, sendTo);
			message.addRecipients(Message.RecipientType.CC, InternetAddress.parse(FROM));//�����ڷ��͸��ռ���֮ǰ���Լ������ͷ�������һ�ݣ���Ȼ�ᱻ���������ʼ�����554��
			message.setSubject(SUBJECT);//���ر���
			Multipart multipart = new MimeMultipart();//��multipart����������ʼ��ĸ����������ݣ������ı����ݺ͸���
			BodyPart contentPart = new MimeBodyPart();//�����ʼ����ı�����
			contentPart.setText(context);
			multipart.addBodyPart(contentPart);
			if(!AFFIX.isEmpty()){//��Ӹ���
				BodyPart messageBodyPart = new MimeBodyPart();
				DataSource source = new FileDataSource(AFFIX);
				messageBodyPart.setDataHandler(new DataHandler(source));//��Ӹ���������
//				sun.misc.BASE64Encoder enc = new sun.misc.BASE64Encoder();
				messageBodyPart.setFileName("��������");
				multipart.addBodyPart(messageBodyPart);
			}
			message.setContent(multipart);//��multipart����ŵ�message��
			message.saveChanges();//�����ʼ�
			Transport transport = session.getTransport("smtp");//�����ʼ�
			transport.connect(HOST, USER, PWD);//���ӷ���������
			transport.sendMessage(message, message.getAllRecipients());//���ʼ����ͳ�ȥ
			transport.close();//�ر�����
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	/**
	 *�����ʼ���qq���䣩
	 *@param host
	 *@param user
	 *@param pwd
	 * @throws MessagingException 
	 * @throws AddressException 
	 */
	public static void send_qq (String context) throws AddressException, MessagingException{
		Properties props = new Properties();
		props.put("mail.transport.protocol", "smtp");//����Э��
		props.put("mail.smtp.host","smtp.qq.com");//������
		props.put("mail.smtp.port", 465);//�˿ں�
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.ssl.enable", "true");//�����Ƿ�ʹ��ssl��ȫ���� ---һ�㶼ʹ��
		props.put("mail.debug", "true");//�����Ƿ���ʾdebug��Ϣ��true���ڿ���̨��ʾ�����Ϣ
		//�õ��ػ�����
		Session session = Session.getDefaultInstance(props);
		//��ȡ�ʼ�����
		Message message = new MimeMessage(session);
		//���÷����������ַ
		message.setFrom(new InternetAddress(FROM));
		//�����ռ��������ַ
		InternetAddress[] sendTo = new InternetAddress[TOS.length];//�����ռ��˵�ַ
		for (int i = 0; i < TOS.length; i ++){
			sendTo[i] = new InternetAddress(TOS[i]);
		}
		message.addRecipients(Message.RecipientType.TO, sendTo);
		//�����ʼ�����
		message.setSubject("QQ������Ȩ��");
		//�����ʼ�����
		message.setText(context);
		//�õ��ʲ����
		Transport transport = session.getTransport();
		//�����Լ��������˻�
		transport.connect(USER, PWD);//����ΪQQ���俪ͨ��stmp�����õ��Ŀͻ�����Ȩ��
		//�����ʼ�
		transport.sendMessage(message, message.getAllRecipients());
		transport.close();
	}
	
	
	
	
	public static void main(String[] args) throws AddressException, MessagingException {
//		send_163("163�������Ȩ�룺cme1820363");
		send_qq("QQ������Ȩ�룺fiuudaorhphdbecf");
	}
	
	
	
	
	
}
