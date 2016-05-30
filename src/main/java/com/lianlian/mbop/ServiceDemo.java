package com.lianlian.mbop;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lianlian.mbop.util.DateUtils;
import com.lianlian.mbop.util.TripleDESUtil;

/**
 * 
 * @author zhuxl
 *
 */
public class ServiceDemo {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceDemo.class);

	public static String PARTNER_ID = "�滻Ϊ�ҷ��ṩ���̻����"; // �̻����
	public static String ORGCODE = "KX0000"; // �̻���Ӫ����
	public static String AGENT_ID = "�滻Ϊ�ҷ��ṩ�Ĵ����̱��"; // �����̱��
	public static String PASSWORD = "88888888"; // ��½����
	public static String PAY_PASSWORD = "88888888"; // ֧������
	public static String SIGN_TYPE = "md5"; // ǩ����ʽ
	public static String KEY = "FD0762380D980D52"; // ��Կ

	public static String URL_CHARGE = "http://122.224.88.51:18080/mobile/charge.json";
	public static String URL_QUERY = "http://122.224.88.51:18080/common/query.json";
	public static String URL_ACCOUNT = "http://122.224.88.51:18080/common/account.json";

	public static TripleDESUtil TDES = TripleDESUtil.getInstance();

	public static void main(String[] args) throws Exception {
		String order_no = "TEST" + System.currentTimeMillis();
		// �µ�
		charge(order_no, "13411615390", "50");

		// ��ѯ
		query(order_no);
		
		// ���
		account();
	}

	/**
	 * ��ֵ�µ�
	 * 
	 * @param order_no
	 *            ������
	 * @param account_no
	 *            �ͻ�����
	 * @param face_price
	 *            ��ֵ���
	 * @throws Exception
	 */
	public static void charge(String order_no, String account_no, String face_price) throws Exception {
		JsonObject header = new JsonObject();
		header.addProperty("partner_id", PARTNER_ID);
		header.addProperty("orgcode", ORGCODE);
		header.addProperty("agent_id", AGENT_ID);
		header.addProperty("password", TDES.encrypt(PASSWORD, KEY));
		header.addProperty("timestamp", DateUtils.getFormatedDate(new Date(), "yyyyMMddHHmmss"));
		header.addProperty("sign_type", SIGN_TYPE);

		JsonObject body = new JsonObject();
		body.addProperty("pay_password", TDES.encrypt(PAY_PASSWORD, KEY));
		body.addProperty("order_no", order_no);
		body.addProperty("account_no", account_no);
		body.addProperty("face_price", face_price);

		// ����ǩ��
		header.addProperty("sign", sign(header, body));

		JsonObject obj = new JsonObject();
		obj.add("header", header);
		obj.add("body", body);

		String message = obj.toString();

		String retMsg = post(URL_CHARGE, message);

		if (retMsg != null) {
			JsonParser parser = new JsonParser();
			JsonObject ret = parser.parse(retMsg).getAsJsonObject();

			JsonObject result = ret.get("result").getAsJsonObject();
			String retcode = result.get("ret_code").getAsString();
			String retmsg = result.get("ret_msg").getAsString();

			if ("10000000".equals(retcode)) {
				LOGGER.info("����ɹ�");
			} else if ("10990001".equals(retcode) || "11610051".equals(retcode)) {
				// TODO ��2��״̬����ʧ�ܴ��������跢���ѯȷ�϶������
			} else {
				LOGGER.info("����ʧ�ܣ�ʧ��ԭ��[{}]", retmsg);
			}
		}
	}

	/**
	 * ������ѯ
	 * 
	 * @param order_no
	 *            ������
	 */
	public static void query(String order_no) throws Exception {
		JsonObject header = new JsonObject();
		header.addProperty("partner_id", PARTNER_ID);
		header.addProperty("orgcode", ORGCODE);
		header.addProperty("agent_id", AGENT_ID);
		header.addProperty("password", TDES.encrypt(PASSWORD, KEY));
		header.addProperty("timestamp", DateUtils.getFormatedDate(new Date(), "yyyyMMddHHmmss"));
		header.addProperty("sign_type", SIGN_TYPE);

		JsonObject body = new JsonObject();
		body.addProperty("order_no", order_no);
		body.addProperty("order_type", "01");

		// ����ǩ��
		header.addProperty("sign", sign(header, body));

		JsonObject obj = new JsonObject();
		obj.add("header", header);
		obj.add("body", body);

		String message = obj.toString();

		String retMsg = post(URL_QUERY, message);

		if (retMsg != null) {
			JsonParser parser = new JsonParser();
			JsonObject ret = parser.parse(retMsg).getAsJsonObject();

			JsonObject result = ret.get("result").getAsJsonObject();
			String retcode = result.get("ret_code").getAsString();
			String retmsg = result.get("ret_msg").getAsString();

			if ("10000000".equals(retcode)) {
				LOGGER.info("��ѯ�ɹ�");

				JsonObject retbody = ret.get("body").getAsJsonObject();
				String status = retbody.get("status").getAsString();

				if ("SUCCESS".equals(status)) {
					LOGGER.info("����״̬���ɹ�");
				} else if ("FAILED".equals(status)) {
					LOGGER.info("����״̬��ʧ��");
				} else if ("PROCESS".equals(status)) {
					LOGGER.info("����״̬��δȷ��");
				}

			} else if ("11310100".equals(retcode)) {
				LOGGER.info("ϵͳ�޴˶���");
			} else {
				LOGGER.info("��ѯʧ�ܣ�ʧ��ԭ��[{}]", retmsg);
			}
		}
	}

	/**
	 * ����ѯ
	 */
	public static void account() throws Exception {
		JsonObject header = new JsonObject();
		header.addProperty("partner_id", PARTNER_ID);
		header.addProperty("orgcode", ORGCODE);
		header.addProperty("agent_id", AGENT_ID);
		header.addProperty("password", TDES.encrypt(PASSWORD, KEY));
		header.addProperty("timestamp", DateUtils.getFormatedDate(new Date(), "yyyyMMddHHmmss"));
		header.addProperty("sign_type", SIGN_TYPE);

		JsonObject body = new JsonObject();
		body.addProperty("query_type", "1");

		// ����ǩ��
		header.addProperty("sign", sign(header, body));

		JsonObject obj = new JsonObject();
		obj.add("header", header);
		obj.add("body", body);

		String message = obj.toString();

		String retMsg = post(URL_ACCOUNT, message);

		if (retMsg != null) {
			JsonParser parser = new JsonParser();
			JsonObject ret = parser.parse(retMsg).getAsJsonObject();

			JsonObject result = ret.get("result").getAsJsonObject();
			String retcode = result.get("ret_code").getAsString();
			String retmsg = result.get("ret_msg").getAsString();

			if ("10000000".equals(retcode)) {
				LOGGER.info("��ѯ�ɹ�");

				JsonObject retbody = ret.get("body").getAsJsonObject();
				String balance = retbody.get("balance").getAsString();

				LOGGER.info("��{}", balance);

			} else if ("11310100".equals(retcode)) {
				LOGGER.info("ϵͳ�޴˶���");
			} else {
				LOGGER.info("��ѯʧ�ܣ�ʧ��ԭ��[{}]", retmsg);
			}
		}
	}

	public static String post(String url, String message) throws ClientProtocolException, IOException {
		LOGGER.info("������Ϣ��{}", message);

		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost(url);

		StringEntity entity = new StringEntity(message, "UTF-8");
		entity.setContentType("application/json;charset=UTF-8");
		httppost.setEntity(entity);
		httppost.addHeader("Accept", "application/json");
		httppost.addHeader("ContentType", "application/json;charset=UTF-8");

		CloseableHttpResponse response = httpclient.execute(httppost);

		try {
			int code = response.getStatusLine().getStatusCode();
			LOGGER.error("code-->{}", code);
			if (HttpURLConnection.HTTP_OK == code) {
				String retMsg = EntityUtils.toString(response.getEntity(), "UTF-8");
				LOGGER.info("������Ϣ��{}", retMsg);

				return retMsg;
			}
		} catch (Exception e) {
			// TODO ����Բ�ͬ�쳣����Ӧ�Ĵ���
			LOGGER.error(e.getMessage(), e);
		} finally {
			response.close();
		}
		return null;
	}

	public static String sign(JsonObject... jsonObjects) {
		StringBuffer sb = new StringBuffer();

		Map<String, String> attrs = new TreeMap<String, String>();
		for (JsonObject obj : jsonObjects) {
			for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue().getAsString();
				if (value != null && !"".equals(value.trim())) {
					attrs.put(key, value);
				}
			}
		}

		Set<Map.Entry<String, String>> es = attrs.entrySet();
		Iterator<Map.Entry<String, String>> it = es.iterator();

		while (it.hasNext()) {
			Map.Entry<String, String> entry = it.next();
			String key = entry.getKey();
			String value = entry.getValue();
			sb.append(key).append("=").append(value).append("&");
		}
		sb.deleteCharAt(sb.length() - 1).append(KEY);

		LOGGER.info("ǩ��ǰ����{}", sb.toString());

		String sign = DigestUtils.md5Hex(sb.toString().getBytes(Charset.forName("UTF-8")));

		LOGGER.info("ǩ����{}", sign);
		return sign;
	}
}
