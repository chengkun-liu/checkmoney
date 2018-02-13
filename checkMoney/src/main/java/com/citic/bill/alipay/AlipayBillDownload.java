package com.citic.bill.alipay;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayDataDataserviceBillDownloadurlQueryRequest;
import com.alipay.api.response.AlipayDataDataserviceBillDownloadurlQueryResponse;
import com.citic.bill.IBillDown;
import com.citic.bill.util.ConfigUtil;
import com.citic.bill.util.FileUtil;
/**
 * 支付宝下载对账单
 * @author jial
 *
 */
public class AlipayBillDownload implements IBillDown{
	
	private static Logger logger = Logger.getLogger(AlipayBillDownload.class);
	//初始化实例请求对象 
    public static AlipayClient alipayClient = new DefaultAlipayClient(ConfigUtil.ALIPAY_DOWNLOAD_BILL_URL, 
    		ConfigUtil.ALIPAY_APP_ID, ConfigUtil.ALIPAY_APP_PRIVATE_KEY, "json", "GBK",  ConfigUtil.ALIPAY_PUBLIC_KEY,"RSA"); 
    
    public  Map<String, Object>  billDownload (String billDate)  throws IOException{ 
        
        AlipayDataDataserviceBillDownloadurlQueryRequest request = new AlipayDataDataserviceBillDownloadurlQueryRequest();
        JSONObject json = new JSONObject();
        json.put("bill_type", "signcustomer");
        //昨天的数据 new DateTime().minusDays(1).toString("yyyy-MM-dd")
        json.put("bill_date", billDate);
        request.setBizContent(json.toString());
                
		AlipayDataDataserviceBillDownloadurlQueryResponse response = null;
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			response = alipayClient.execute(request);
			logger.info("response.getBillDownloadUrl() ==>>"+response.getBillDownloadUrl());
		} catch (AlipayApiException e) {
			logger.error("alipayClient execute error ==>>",e);
		}
         if(response.isSuccess()){ 
        	// 将接口返回的对账单下载地址传入urlStr
			String urlStr = response.getBillDownloadUrl();
			
			// 开始下载
			try {
				//读取配置路径
				String filePath = FileUtil.getBillPath();
				File file =new File(filePath);    
	    		//如果文件夹不存在则创建    
	    		if (!file .exists()){  
	    		    file .mkdirs();  
	    		    logger.debug("================>> bill path is create success");
	    		} 
				// 指定希望保存的文件路径
				String newZip = filePath + new Date().getTime() + ".zip";
				FileUtil.downloadNet(urlStr, newZip);
				// 解压到指定目录
				FileUtil.unZip(newZip, filePath);
				
			} catch (Exception e) {
				logger.error("alipay zip error ==>>",e);
			}
			  logger.debug("invoking success ==>>"+JSON.toJSONString(response));
			  resultMap.put("stauts", 1);
			  return resultMap;
		}else {
			logger.debug("invoking error ==>>"+JSON.toJSONString(response));
            resultMap.put("stauts", 0);
            return resultMap;
        }
         
       } 
}
